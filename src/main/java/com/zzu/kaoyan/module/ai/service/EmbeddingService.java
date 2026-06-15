package com.zzu.kaoyan.module.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.module.ai.config.AiApiProperties;
import com.zzu.kaoyan.module.ai.entity.AiMemoryEmbedding;
import com.zzu.kaoyan.module.ai.mapper.AiMemoryEmbeddingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Embedding 向量服务 — 调用 DashScope text-embedding API，
 * 存储 embedding 到 MySQL，通过余弦相似度检索相似历史。
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    /** DashScope text-embedding API 地址 */
    private static final String EMBEDDING_API_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding";

    /** Embedding 模型：1536 维向量 */
    private static final String EMBEDDING_MODEL = "text-embedding-v2";

    /** 检索时返回的相似结果数 */
    private static final int TOP_K = 3;

    /** 余弦相似度阈值，低于此值不纳入结果 */
    private static final double SIMILARITY_THRESHOLD = 0.65;

    private final AiMemoryEmbeddingMapper embeddingMapper;
    private final AiApiProperties apiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public EmbeddingService(AiMemoryEmbeddingMapper embeddingMapper,
                            AiApiProperties apiProperties,
                            RestTemplate aiRestTemplate) {
        this.embeddingMapper = embeddingMapper;
        this.apiProperties = apiProperties;
        this.restTemplate = aiRestTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // ═══════════════════════════════════════════════════════════════
    // 公开 API
    // ═══════════════════════════════════════════════════════════════

    /**
     * 保存一条语义记忆（异步调用，不阻塞主流程）。
     *
     * @param userId     用户 ID
     * @param content    原文（问题 + 回答摘要）
     * @param sourceType 来源类型（CHAT_QA / MISTAKE_NOTE）
     * @param sourceId   关联的原数据 ID
     * @param subject    学科标签（用于过滤）
     */
    public void saveAsync(Long userId, String content, String sourceType, Long sourceId, String subject) {
        if (content == null || content.isBlank()) return;
        if (apiProperties.getKey() == null || apiProperties.getKey().isBlank()) {
            log.debug("未配置 API Key，跳过 embedding 存储");
            return;
        }

        new Thread(() -> {
            try {
                float[] embedding = embed(content);
                if (embedding == null) return;

                AiMemoryEmbedding entity = new AiMemoryEmbedding();
                entity.setUserId(userId);
                entity.setContent(content.length() > 500 ? content.substring(0, 500) : content);
                entity.setSubject(subject);
                entity.setEmbedding(toJson(embedding));
                entity.setSourceType(sourceType);
                entity.setSourceId(sourceId);
                embeddingMapper.insert(entity);

                log.debug("Embedding 已存储 — userId={}, sourceType={}, contentLength={}",
                        userId, sourceType, content.length());
            } catch (Exception e) {
                log.warn("Embedding 存储失败（不影响主流程） — {}", e.getMessage());
            }
        });
    }

    /**
     * 语义检索：根据当前问题，从用户历史记忆中找出最相似的 TOP-3。
     * 同科目的记忆优先（subject 匹配时加权），不同科目的记忆降低权重。
     *
     * @param userId  用户 ID
     * @param query   当前问题文本
     * @param subject 当前学科（可为 null，null 时不按学科过滤）
     * @return 相似历史文本列表（按相似度降序），无结果时返回空列表
     */
    public List<String> search(Long userId, String query, String subject) {
        if (query == null || query.isBlank()) return Collections.emptyList();
        if (apiProperties.getKey() == null || apiProperties.getKey().isBlank()) {
            return Collections.emptyList();
        }

        try {
            // 1. 计算查询向量
            float[] queryVec = embed(query);
            if (queryVec == null) return Collections.emptyList();

            // 2. 加载用户的所有向量
            List<AiMemoryEmbedding> all = embeddingMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiMemoryEmbedding>()
                            .eq(AiMemoryEmbedding::getUserId, userId)
                            .orderByDesc(AiMemoryEmbedding::getCreatedAt)
                            .last("LIMIT 200")
            );

            if (all.isEmpty()) return Collections.emptyList();

            // 3. 计算余弦相似度，同科目加权 +0.1，排序取 TOP-K
            record Scored(String content, double score) {}

            return all.stream()
                    .map(e -> {
                        float[] vec = parseEmbedding(e.getEmbedding());
                        if (vec == null) return null;
                        double sim = cosineSimilarity(queryVec, vec);
                        // 同科目录每个关联，跨科目录个低关联
                        if (subject != null && !subject.isBlank()
                                && e.getSubject() != null && e.getSubject().equalsIgnoreCase(subject)) {
                            sim += 0.15;  // 同科目加权
                        } else if (subject != null && !subject.isBlank()
                                && e.getSubject() != null && !e.getSubject().equalsIgnoreCase(subject)) {
                            sim -= 0.10;  // 跨科目降权
                        }
                        return new Scored(e.getContent(), sim);
                    })
                    .filter(s -> s != null && s.score >= SIMILARITY_THRESHOLD)
                    .sorted((a, b) -> Double.compare(b.score, a.score))
                    .limit(TOP_K)
                    .map(s -> s.content)
                    .toList();

        } catch (Exception e) {
            log.warn("语义检索失败（不影响主流程） — {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DashScope API 调用
    // ═══════════════════════════════════════════════════════════════

    /**
     * 调用 DashScope text-embedding API，返回浮点向量。
     *
     * @return 1536-dim float[]，失败返回 null
     */
    private float[] embed(String text) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", EMBEDDING_MODEL,
                    "input", Map.of("texts", List.of(text))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiProperties.getKey());

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    EMBEDDING_API_URL, request, String.class);

            if (response.getBody() == null) {
                log.warn("Embedding API 返回空 body");
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());

            // 检查错误
            if (root.has("code") && !"".equals(root.path("code").asText())) {
                log.warn("Embedding API 错误 — code={}, message={}",
                        root.path("code").asText(), root.path("message").asText());
                return null;
            }

            // 提取 embedding 数组
            JsonNode embeddings = root.path("output").path("embeddings");
            if (!embeddings.isArray() || embeddings.isEmpty()) {
                log.warn("Embedding API 返回空 embeddings");
                return null;
            }

            JsonNode vec = embeddings.get(0).path("embedding");
            float[] result = new float[vec.size()];
            for (int i = 0; i < vec.size(); i++) {
                result[i] = vec.get(i).floatValue();
            }
            return result;

        } catch (Exception e) {
            log.warn("Embedding API 调用失败 — {}", e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 向量运算
    // ═══════════════════════════════════════════════════════════════

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // ═══════════════════════════════════════════════════════════════
    // 序列化工具
    // ═══════════════════════════════════════════════════════════════

    private String toJson(float[] vec) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vec.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vec[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private float[] parseEmbedding(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            JsonNode arr = objectMapper.readTree(json);
            float[] result = new float[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                result[i] = arr.get(i).floatValue();
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
