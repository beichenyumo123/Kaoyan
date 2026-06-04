package com.zzu.kaoyan.module.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.module.ai.config.AiApiProperties;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AiAgentServiceImpl implements AiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiAgentServiceImpl.class);

    /** 对话历史最大消息数（含 system + 多轮 user/assistant） */
    private static final int MAX_HISTORY_MESSAGES = 11; // 1 system + 5 轮 * 2
    /** Redis key 前缀 */
    private static final String HISTORY_KEY_PREFIX = "ai:chat:history:";
    /** 历史过期时间（小时） */
    private static final long HISTORY_TTL_HOURS = 24;

    private final RestTemplate aiRestTemplate;
    private final AiApiProperties apiProperties;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    public AiAgentServiceImpl(RestTemplate aiRestTemplate, AiApiProperties apiProperties,
                              StringRedisTemplate redisTemplate) {
        this.aiRestTemplate = aiRestTemplate;
        this.apiProperties = apiProperties;
        this.objectMapper = new ObjectMapper();
        this.redisTemplate = redisTemplate;
    }

    /** 最大重试次数 */
    private static final int MAX_RETRIES = 3;
    /** 初始退避时间（毫秒） */
    private static final long INITIAL_BACKOFF_MS = 1000;

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (apiProperties.getKey() == null || apiProperties.getKey().isBlank()) {
            log.warn("AI API Key 未配置，返回占位回复");
            return "【AI 未配置】请设置 ai.api.key";
        }

        Map<String, Object> requestBody = buildRequestBody(systemPrompt, userMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiProperties.getKey());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 重试 + 指数退避
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String response = aiRestTemplate.postForObject(
                        apiProperties.getEndpoint(), entity, String.class);

                if (response == null) {
                    log.error("AI API 返回 null（第{}次尝试）", attempt);
                    if (attempt < MAX_RETRIES) {
                        sleepBeforeRetry(attempt);
                        continue;
                    }
                    return "【AI 返回为空】";
                }

                return extractContent(response);
            } catch (RestClientException e) {
                log.warn("AI API 调用失败（第{}/{}次尝试）— {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    sleepBeforeRetry(attempt);
                } else {
                    log.error("AI API 调用最终失败，已重试{}次", MAX_RETRIES, e);
                    return "【AI 调用失败】" + e.getMessage();
                }
            }
        }

        return "【AI 调用失败】未知错误";
    }

    @Override
    public String chatWithHistory(Long userId, String systemPrompt, String userMessage) {
        if (apiProperties.getKey() == null || apiProperties.getKey().isBlank()) {
            log.warn("AI API Key 未配置，返回占位回复");
            return "【AI 未配置】请设置 ai.api.key";
        }

        String redisKey = HISTORY_KEY_PREFIX + userId;

        // 1. 从 Redis 获取历史
        List<Map<String, String>> messages = loadHistory(redisKey);
        if (messages.isEmpty()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }

        // 2. 添加当前用户消息
        messages.add(Map.of("role", "user", "content", userMessage));

        // 3. 调用 LLM（带历史）
        String response = callLlmWithMessages(messages);

        // 4. 存储 assistant 回复到历史
        if (!response.startsWith("【AI")) {
            messages.add(Map.of("role", "assistant", "content", response));
            // 截断到最大长度
            while (messages.size() > MAX_HISTORY_MESSAGES) {
                messages.remove(1); // 保留 system prompt，从第二条开始删
            }
            saveHistory(redisKey, messages);
        }

        return response;
    }

    @Override
    public List<Map<String, String>> getHistory(Long userId) {
        String redisKey = HISTORY_KEY_PREFIX + userId;
        List<Map<String, String>> all = loadHistory(redisKey);
        // 过滤掉 system 消息，只返回 user/assistant 对话
        return all.stream()
                .filter(m -> "user".equals(m.get("role")) || "assistant".equals(m.get("role")))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void clearHistory(Long userId) {
        String redisKey = HISTORY_KEY_PREFIX + userId;
        redisTemplate.delete(redisKey);
        log.info("已清除用户对话历史 — userId={}", userId);
    }

    /**
     * 带消息列表的 LLM 调用（内部方法）。
     */
    private String callLlmWithMessages(List<Map<String, String>> messages) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", apiProperties.getModel());
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1024);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiProperties.getKey());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String response = aiRestTemplate.postForObject(
                        apiProperties.getEndpoint(), entity, String.class);
                if (response == null) {
                    log.error("AI API 返回 null（第{}次尝试）", attempt);
                    if (attempt < MAX_RETRIES) { sleepBeforeRetry(attempt); continue; }
                    return "【AI 返回为空】";
                }
                return extractContent(response);
            } catch (RestClientException e) {
                log.warn("AI API 调用失败（第{}/{}次）— {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) { sleepBeforeRetry(attempt); }
                else { return "【AI 调用失败】" + e.getMessage(); }
            }
        }
        return "【AI 调用失败】未知错误";
    }

    /**
     * 从 Redis 加载对话历史。
     */
    private List<Map<String, String>> loadHistory(String redisKey) {
        try {
            String json = redisTemplate.opsForValue().get(redisKey);
            if (json == null || json.isBlank()) return new ArrayList<>();
            return objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            log.warn("加载对话历史失败 — key={}", redisKey, e);
            return new ArrayList<>();
        }
    }

    /**
     * 保存对话历史到 Redis（带 TTL）。
     */
    private void saveHistory(String redisKey, List<Map<String, String>> messages) {
        try {
            String json = objectMapper.writeValueAsString(messages);
            redisTemplate.opsForValue().set(redisKey, json, HISTORY_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("保存对话历史失败 — key={}", redisKey, e);
        }
    }

    /**
     * 指数退避等待：第1次1s，第2次2s，第3次4s。
     */
    private void sleepBeforeRetry(int attempt) {
        long backoff = INITIAL_BACKOFF_MS * (1L << (attempt - 1));
        log.info("AI API 重试等待 {}ms", backoff);
        try {
            Thread.sleep(backoff);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("重试等待被中断");
        }
    }

    private Map<String, Object> buildRequestBody(String systemPrompt, String userMessage) {
        return Map.of(
                "model", apiProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.7,
                "max_tokens", 1024
        );
    }

    private String extractContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").get(0)
                    .path("message").path("content")
                    .asText("【AI 无内容】");
        } catch (Exception e) {
            log.error("解析 AI 响应失败 — body={}", responseBody, e);
            return "【AI 解析失败】";
        }
    }
}
