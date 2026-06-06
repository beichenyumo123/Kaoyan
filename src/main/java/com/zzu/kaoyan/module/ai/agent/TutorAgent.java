package com.zzu.kaoyan.module.ai.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.module.ai.config.AiApiProperties;
import com.zzu.kaoyan.module.ai.entity.KnowledgePoint;
import com.zzu.kaoyan.module.ai.mapper.AiKnowledgePointMapper;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
import com.zzu.kaoyan.module.mistake.entity.vo.OCRResultVO;
import com.zzu.kaoyan.module.mistake.mapper.MistakeNoteMapper;
import com.zzu.kaoyan.module.mistake.service.OCRService;
import org.slf4j.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 答疑 Agent — 基于知识库 RAG + Tool Calling 定位考点链路，回答用户学科问题。
 *
 * <p>支持两种模式：
 * <ul>
 *   <li>RAG 模式（默认）：先检索知识库，再用 LLM 生成回答</li>
 *   <li>Tool Calling 模式：LLM 自主决定是否调用知识库检索工具，支持多轮工具调用</li>
 * </ul>
 */
@Component
public class TutorAgent {

    private static final Logger log = LoggerFactory.getLogger(TutorAgent.class);

    private static final String SYSTEM_PROMPT =
            """
            你是一位资深考研辅导专家，擅长408计算机综合（数据结构、操作系统、计算机网络、计算机组成原理）以及数学、英语、政治等考研科目。

            你可以调用以下工具来辅助回答：
            - search_knowledge: 搜索考研知识库，获取相关知识点。当你需要查找具体考点、定义、公式时使用。

            回答要求：
            1. 优先使用工具检索到的知识库内容回答，确保准确性；
            2. 如果知识库中没有相关内容，可以用自己的知识补充，但要标注"（补充知识）"；
            3. 回答要条理清晰，使用编号或分点；
            4. 在回答末尾标注"📚 考点出处"，列出涉及的知识点标题和学科；
            5. 如果问题涉及多个知识点，要指出它们之间的关联（考点链路）；
            6. 语言风格：专业但易懂，适合备考学生阅读。

            直接输出回答文本，不要带任何前缀说明。
            """;

    private static final int MAX_SEARCH_RESULTS = 5;
    /** Tool Calling 最大轮次（防止无限循环） */
    private static final int MAX_TOOL_ROUNDS = 3;

    private final AiKnowledgePointMapper knowledgePointMapper;
    private final AiAgentService aiAgentService;
    private final RestTemplate aiRestTemplate;
    private final RestTemplate aiStreamRestTemplate;
    private final AiApiProperties apiProperties;
    private final ObjectMapper objectMapper;
    private final OCRService ocrService;
    private final MistakeNoteMapper mistakeNoteMapper;

    public TutorAgent(AiKnowledgePointMapper knowledgePointMapper,
                      AiAgentService aiAgentService,
                      RestTemplate aiRestTemplate,
                      @Qualifier("aiStreamRestTemplate") RestTemplate aiStreamRestTemplate,
                      AiApiProperties apiProperties,
                      OCRService ocrService,
                      MistakeNoteMapper mistakeNoteMapper) {
        this.knowledgePointMapper = knowledgePointMapper;
        this.aiAgentService = aiAgentService;
        this.aiRestTemplate = aiRestTemplate;
        this.aiStreamRestTemplate = aiStreamRestTemplate;
        this.apiProperties = apiProperties;
        this.objectMapper = new ObjectMapper();
        this.ocrService = ocrService;
        this.mistakeNoteMapper = mistakeNoteMapper;
    }

    /**
     * 根据用户错题历史，构建薄弱知识点上下文。
     * 查询最近 30 条错题的 knowledgePoints，按出现频率聚合。
     *
     * @return 薄弱点文本，无错题时返回 null
     */
    private String buildWeaknessContext(Long userId, String subject) {
        try {
            List<Map<String, Object>> rows = mistakeNoteMapper.selectRecentKnowledgePoints(userId);
            if (rows == null || rows.isEmpty()) return null;

            // 统计每个知识点出现次数
            Map<String, Integer> kpCount = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) {
                String kp = (String) row.get("knowledge_points");
                String rowSubject = (String) row.get("subject");
                if (kp == null || kp.isBlank()) continue;
                // 如果指定了学科，过滤
                if (subject != null && !subject.isBlank() && !subject.equals(rowSubject)) continue;
                for (String name : kp.split("[,，、\\s]+")) {
                    name = name.trim();
                    if (!name.isEmpty()) {
                        kpCount.merge(name, 1, Integer::sum);
                    }
                }
            }

            if (kpCount.isEmpty()) return null;

            // 按频率降序，取前 5 个
            StringBuilder sb = new StringBuilder();
            kpCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> sb.append("- ").append(e.getKey())
                            .append("（出错 ").append(e.getValue()).append(" 次）\n"));

            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("构建薄弱知识点上下文失败 — {}", e.getMessage());
            return null;
        }
    }

    /**
     * 回答用户问题（Tool Calling 模式）。
     *
     * @param question 用户提问
     * @param subject  限定学科（可为 null 则全学科检索）
     * @return RAG + Tool Calling 增强的回答文本
     */
    public String answer(String question, String subject) {
        return answer(question, subject, null, null);
    }

    /**
     * 回答用户问题（支持图片，双轨制：多模态优先 + OCR 降级）。
     *
     * @param question 用户提问
     * @param subject  限定学科
     * @param imageUrl 图片URL（可选）
     * @return 回答文本
     */
    public String answer(String question, String subject, String imageUrl) {
        return answer(question, subject, imageUrl, null);
    }

    /**
     * 回答用户问题（支持图片 + 用户画像）。
     */
    public String answer(String question, String subject, String imageUrl, Long userId) {
        log.info("TutorAgent 开始答疑 — question={}, subject={}, hasImage={}", question, subject, imageUrl != null);

        // 构建动态 system prompt（注入薄弱知识点）
        String systemPrompt = SYSTEM_PROMPT;
        if (userId != null) {
            String weakness = buildWeaknessContext(userId, subject);
            if (weakness != null) {
                systemPrompt = SYSTEM_PROMPT + "\n\n【用户薄弱知识点】\n" + weakness
                        + "\n回答时注意强化这些关联，适当回顾基础概念。";
            }
        }

        // 有图片时：双轨制
        if (imageUrl != null && !imageUrl.isBlank()) {
            // 轨道1：尝试多模态直接发图
            try {
                String result = answerWithMultimodal(question, subject, imageUrl, systemPrompt);
                if (result != null && !result.isBlank() && !result.startsWith("【AI")) {
                    log.info("多模态路径成功 — question={}", question);
                    return result;
                }
            } catch (Exception e) {
                log.warn("多模态路径失败，降级为 OCR — {}", e.getMessage());
            }

            // 轨道2：OCR 降级
            try {
                String ocrText = performOcr(imageUrl, subject);
                if (ocrText != null && !ocrText.isBlank()) {
                    String enhancedQuestion = String.format(
                            "【题目图片OCR识别内容】\n%s\n\n【学生问题】\n%s", ocrText, question);
                    log.info("OCR 降级路径 — ocrTextLength={}", ocrText.length());
                    return answerWithToolCalling(enhancedQuestion, subject, systemPrompt);
                }
            } catch (Exception e) {
                log.warn("OCR 降级也失败 — {}", e.getMessage());
            }

            return "【图片识别失败】无法识别图片内容，请尝试文字描述问题。";
        }

        // 无图片：原有逻辑
        try {
            return answerWithToolCalling(question, subject, systemPrompt);
        } catch (Exception e) {
            log.warn("Tool Calling 失败，降级为 RAG 模式 — {}", e.getMessage());
            return answerWithRag(question, subject, systemPrompt);
        }
    }

    /**
     * 流式回答用户问题（SSE 模式）。
     * 每收到一个 token 就通过 onChunk 回调推送给前端。
     *
     * @param question 用户提问
     * @param subject  限定学科
     * @param onChunk  每收到一个文本片段时的回调
     */
    public void answerStream(String question, String subject, Consumer<String> onChunk) {
        answerStream(question, subject, null, onChunk, null);
    }

    /**
     * 流式回答用户问题（支持图片，双轨制）。
     *
     * @param question 用户提问
     * @param subject  限定学科
     * @param imageUrl 图片URL（可选）
     * @param onChunk  每收到一个文本片段时的回调
     */
    public void answerStream(String question, String subject, String imageUrl, Consumer<String> onChunk) {
        answerStream(question, subject, imageUrl, onChunk, null);
    }

    public void answerStream(String question, String subject, String imageUrl, Consumer<String> onChunk, Long userId) {
        log.info("TutorAgent 流式答疑 — question={}, subject={}, hasImage={}", question, subject, imageUrl != null);

        // 构建动态 system prompt（注入薄弱知识点）
        String systemPrompt = SYSTEM_PROMPT;
        if (userId != null) {
            String weakness = buildWeaknessContext(userId, subject);
            if (weakness != null) {
                systemPrompt = SYSTEM_PROMPT + "\n\n【用户薄弱知识点】\n" + weakness
                        + "\n回答时注意强化这些关联，适当回顾基础概念。";
            }
        }

        String effectiveQuestion = question;

        // 有图片时：流式多模态优先，失败则 OCR 降级
        if (imageUrl != null && !imageUrl.isBlank()) {
            // 轨道1：多模态流式调用，token 即到即推，前端无需等待完整响应
            try {
                boolean ok = answerStreamWithMultimodal(question, subject, imageUrl, onChunk, systemPrompt);
                if (ok) {
                    log.info("多模态流式路径成功 — question={}", question);
                    return;
                }
            } catch (Exception e) {
                log.warn("多模态流式路径失败，降级为 OCR — {}", e.getMessage());
            }

            // 轨道2：OCR 降级
            try {
                String ocrText = performOcr(imageUrl, subject);
                if (ocrText != null && !ocrText.isBlank()) {
                    effectiveQuestion = String.format(
                            "【题目图片OCR识别内容】\n%s\n\n【学生问题】\n%s", ocrText, question);
                    log.info("OCR 降级流式路径 — ocrTextLength={}", ocrText.length());
                }
            } catch (Exception e) {
                log.warn("OCR 降级也失败，使用原始问题 — {}", e.getMessage());
            }
        }

        // 标准流式路径（无图片 或 OCR 降级后）
        // 先做 RAG 检索
        List<String> keywords = extractKeywords(effectiveQuestion);
        List<KnowledgePoint> results = knowledgePointMapper.searchByKeywords(
                keywords, subject, MAX_SEARCH_RESULTS);
        String context = buildContext(results);
        String userMessage = String.format("【知识库参考内容】\n%s\n\n【学生问题】\n%s", context, effectiveQuestion);

        // 构建流式请求
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", apiProperties.getModel());
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4096);
        requestBody.put("stream", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiProperties.getKey());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            aiStreamRestTemplate.execute(
                    apiProperties.getEndpoint(),
                    org.springframework.http.HttpMethod.POST,
                    request -> {
                        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        request.getHeaders().setBearerAuth(apiProperties.getKey());
                        objectMapper.writeValue(request.getBody(), requestBody);
                    },
                    response -> {
                        try (InputStream is = response.getBody();
                             BufferedReader reader = new BufferedReader(
                                     new InputStreamReader(is, StandardCharsets.UTF_8))) {

                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.isBlank()) continue;
                                if (!line.startsWith("data: ")) continue;

                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) break;

                                try {
                                    JsonNode node = objectMapper.readTree(data);
                                    String delta = node.path("choices").get(0)
                                            .path("delta").path("content").asText("");
                                    if (!delta.isEmpty()) {
                                        onChunk.accept(delta);
                                    }
                                } catch (Exception parseEx) {
                                    log.debug("SSE 解析跳过 — data={}", data);
                                }
                            }
                        } catch (Exception e) {
                            log.error("SSE 流读取异常", e);
                        }
                        return null;
                    });
        } catch (Exception e) {
            log.error("流式 LLM 调用失败", e);
            onChunk.accept("\n\n【AI 调用失败】" + e.getMessage());
        }

        log.info("TutorAgent 流式答疑完成 — question={}", question);
    }

    /**
     * 获取指定用户的对话历史（user/assistant 消息）。
     */
    public List<Map<String, String>> getHistory(Long userId) {
        return aiAgentService.getHistory(userId);
    }

    /**
     * 清除指定用户的对话历史。
     */
    public void clearHistory(Long userId) {
        aiAgentService.clearHistory(userId);
    }

    // ==================== 多模态 + OCR 双轨 ====================

    /**
     * 多模态非流式调用：将图片以 OpenAI vision 格式发送给模型。
     *
     * @return AI 回答文本，失败返回 null
     */
    private String answerWithMultimodal(String question, String subject, String imageUrl, String systemPrompt) throws Exception {
        // RAG 检索
        List<String> keywords = extractKeywords(question);
        List<KnowledgePoint> results = knowledgePointMapper.searchByKeywords(
                keywords, subject, MAX_SEARCH_RESULTS);
        String context = buildContext(results);

        String textPart = String.format(
                "【知识库参考内容】\n%s\n\n【学生问题】\n%s", context, question);

        // 将图片转为 base64 data URI（本地 URL 远程 API 无法访问）
        String imageDataUri = imageUrlToBase64(imageUrl);
        if (imageDataUri == null) {
            log.warn("图片转 base64 失败，跳过多模态路径");
            return null;
        }
        log.info("图片 base64 长度: {} 字符 (约 {} KB)", imageDataUri.length(), imageDataUri.length() * 3 / 4 / 1024);
        List<Map<String, Object>> contentParts = new ArrayList<>();
        contentParts.add(Map.of("type", "text", "text", textPart));
        contentParts.add(Map.of("type", "image_url", "image_url", Map.of("url", imageDataUri)));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", apiProperties.getModel());
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", contentParts)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 32768);
        requestBody.put("reasoning_effort", "low");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiProperties.getKey());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String response = aiRestTemplate.postForObject(
                apiProperties.getEndpoint(), entity, String.class);
        if (response == null) {
            log.warn("多模态 API 返回 null");
            return null;
        }

        log.info("多模态 API 响应 (前500字符): {}", response.substring(0, Math.min(500, response.length())));

        JsonNode root = objectMapper.readTree(response);
        // 检查是否有错误（模型不支持 vision 时会返回 error）
        if (root.has("error")) {
            String errorMsg = root.path("error").path("message").asText("unknown");
            log.warn("多模态 API 返回错误 — {}", errorMsg);
            return null;
        }

        JsonNode choices = root.path("choices");
        if (choices.isEmpty() || choices.get(0) == null) {
            log.warn("多模态 API 返回空 choices — response={}", response.substring(0, Math.min(300, response.length())));
            return null;
        }

        JsonNode choice = choices.get(0);
        JsonNode message = choice.path("message");
        JsonNode content = message.path("content");
        JsonNode finishReason = choice.path("finish_reason");
        JsonNode usage = root.path("usage");

        // 详细日志：finish_reason + usage + message 全结构
        log.info("多模态 finish_reason={}, usage={}", finishReason.asText("N/A"), usage.toString());
        log.info("多模态 message 完整结构: {}", message.toString());

        if (content.isMissingNode() || content.isNull()) {
            log.warn("多模态 API content 为空 — finish_reason={}", finishReason.asText("N/A"));
            return null;
        }

        String result = content.asText("");
        log.info("多模态 API 内容长度={}", result.length());
        return result.isEmpty() ? null : result;
    }

    /**
     * 多模态流式调用：将图片以 OpenAI vision 格式发送给模型。
     *
     * @return true 表示成功，false 表示模型不支持需降级
     */
    private boolean answerStreamWithMultimodal(String question, String subject,
                                                String imageUrl, Consumer<String> onChunk,
                                                String systemPrompt) {
        // RAG 检索
        List<String> keywords = extractKeywords(question);
        List<KnowledgePoint> results = knowledgePointMapper.searchByKeywords(
                keywords, subject, MAX_SEARCH_RESULTS);
        String context = buildContext(results);

        String textPart = String.format(
                "【知识库参考内容】\n%s\n\n【学生问题】\n%s", context, question);

        // 将图片转为 base64 data URI
        String imageDataUri = imageUrlToBase64(imageUrl);
        if (imageDataUri == null) {
            log.warn("图片转 base64 失败，跳过多模态流式路径");
            return false;
        }

        List<Map<String, Object>> contentParts = new ArrayList<>();
        contentParts.add(Map.of("type", "text", "text", textPart));
        contentParts.add(Map.of("type", "image_url", "image_url", Map.of("url", imageDataUri)));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", apiProperties.getModel());
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", contentParts)
        ));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4096);
        requestBody.put("stream", true);

        final boolean[] hadError = {false};

        try {
            aiStreamRestTemplate.execute(
                    apiProperties.getEndpoint(),
                    org.springframework.http.HttpMethod.POST,
                    request -> {
                        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        request.getHeaders().setBearerAuth(apiProperties.getKey());
                        objectMapper.writeValue(request.getBody(), requestBody);
                    },
                    response -> {
                        try (InputStream is = response.getBody();
                             BufferedReader reader = new BufferedReader(
                                     new InputStreamReader(is, StandardCharsets.UTF_8))) {

                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.isBlank()) continue;
                                if (!line.startsWith("data: ")) continue;

                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) break;

                                try {
                                    JsonNode node = objectMapper.readTree(data);
                                    // 检查 error（某些 API 在流式中返回 error chunk）
                                    if (node.has("error")) {
                                        hadError[0] = true;
                                        log.warn("多模态流式 API 返回错误 — {}",
                                                node.path("error").path("message").asText());
                                        return null;
                                    }
                                    String delta = node.path("choices").get(0)
                                            .path("delta").path("content").asText("");
                                    if (!delta.isEmpty()) {
                                        onChunk.accept(delta);
                                    }
                                } catch (Exception parseEx) {
                                    log.debug("SSE 解析跳过 — data={}", data);
                                }
                            }
                        } catch (Exception e) {
                            hadError[0] = true;
                            log.error("多模态 SSE 流读取异常", e);
                        }
                        return null;
                    });
        } catch (Exception e) {
            log.warn("多模态流式调用异常 — {}", e.getMessage());
            return false;
        }

        return !hadError[0];
    }

    /**
     * 调用 OCR 服务识别图片文字。
     */
    private String performOcr(String imageUrl, String subject) {
        try {
            OCRResultVO result = ocrService.recognize(imageUrl, subject);
            return result.getText();
        } catch (Exception e) {
            log.error("OCR 识别失败 — imageUrl={}", imageUrl, e);
            return null;
        }
    }

    /**
     * 将图片URL转为 base64 data URI（供多模态 API 使用）。
     * 支持本地文件路径（/uploads/...）和 http URL。
     */
    private String imageUrlToBase64(String imageUrl) {
        try {
            byte[] imageBytes;
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                // 远程 URL：通过 RestTemplate 下载
                imageBytes = aiRestTemplate.getForObject(imageUrl, byte[].class);
                if (imageBytes == null) return null;
            } else {
                // 本地路径：/uploads/images/202606/xxx.jpg → ./uploads/images/202606/xxx.jpg
                String relativePath = imageUrl.startsWith("/uploads/") ? imageUrl.substring(1) : imageUrl;
                Path filePath = Path.of(relativePath);
                if (!Files.exists(filePath)) {
                    log.warn("图片文件不存在 — {}", filePath);
                    return null;
                }
                imageBytes = Files.readAllBytes(filePath);
            }

            // 推断 MIME 类型
            String mime = "image/jpeg"; // 默认
            String lower = imageUrl.toLowerCase();
            if (lower.endsWith(".png")) mime = "image/png";
            else if (lower.endsWith(".gif")) mime = "image/gif";
            else if (lower.endsWith(".webp")) mime = "image/webp";

            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            return "data:" + mime + ";base64," + base64;
        } catch (Exception e) {
            log.error("图片转 base64 失败 — imageUrl={}", imageUrl, e);
            return null;
        }
    }

    // ==================== Tool Calling 模式 ====================

    private String answerWithToolCalling(String question, String subject, String systemPrompt) throws Exception {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", question));

        List<Map<String, Object>> tools = buildToolDefinitions();

        for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", apiProperties.getModel());
            requestBody.put("messages", messages);
            requestBody.put("tools", tools);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 4096);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiProperties.getKey());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String response = aiRestTemplate.postForObject(
                    apiProperties.getEndpoint(), entity, String.class);

            if (response == null) {
                log.error("Tool Calling API 返回 null");
                break;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode choice = root.path("choices").get(0);
            JsonNode message = choice.path("message");

            // 检查是否有 tool_calls
            if (message.has("tool_calls") && !message.path("tool_calls").isEmpty()) {
                // 将 assistant 消息（含 tool_calls）加入历史
                try {
                    Map<String, Object> assistantMsg = objectMapper.convertValue(message,
                            new TypeReference<Map<String, Object>>() {});
                    messages.add(assistantMsg);
                } catch (Exception ex) {
                    messages.add(Map.of("role", "assistant", "content",
                            message.path("content").asText("")));
                }

                // 执行每个 tool_call
                JsonNode toolCalls = message.path("tool_calls");
                for (JsonNode toolCall : toolCalls) {
                    String toolCallId = toolCall.path("id").asText();
                    String functionName = toolCall.path("function").path("name").asText();
                    String argsJson = toolCall.path("function").path("arguments").asText();

                    String toolResult = executeTool(functionName, argsJson, subject);
                    log.info("Tool 执行结果 — function={}, resultLength={}", functionName, toolResult.length());

                    messages.add(Map.of(
                            "role", "tool",
                            "tool_call_id", toolCallId,
                            "content", toolResult));
                }
                // 继续下一轮
            } else {
                // 无 tool_calls，返回最终回答
                String content = message.path("content").asText("【AI 无内容】");
                log.info("TutorAgent Tool Calling 完成 — rounds={}, length={}", round + 1, content.length());
                return content;
            }
        }

        log.warn("Tool Calling 达到最大轮次，降级为 RAG");
        return answerWithRag(question, subject, systemPrompt);
    }

    /**
     * 定义可用工具。
     */
    private List<Map<String, Object>> buildToolDefinitions() {
        Map<String, Object> searchTool = new HashMap<>();
        searchTool.put("type", "function");

        Map<String, Object> function = new HashMap<>();
        function.put("name", "search_knowledge");
        function.put("description", "搜索考研知识库，获取与指定关键词相关的知识点。用于查找考点定义、公式、原理等。");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("query", Map.of(
                "type", "string",
                "description", "搜索关键词，如'B+树'、'死锁条件'、'TCP握手'等"));
        parameters.put("properties", properties);
        parameters.put("required", List.of("query"));

        function.put("parameters", parameters);
        searchTool.put("function", function);

        return List.of(searchTool);
    }

    /**
     * 执行工具调用。
     */
    private String executeTool(String functionName, String argsJson, String subject) {
        try {
            if ("search_knowledge".equals(functionName)) {
                JsonNode args = objectMapper.readTree(argsJson);
                String query = args.path("query").asText("");

                List<String> keywords = Arrays.asList(query.split("[，,\\s]+"));
                List<KnowledgePoint> results = knowledgePointMapper.searchByKeywords(
                        keywords, subject, MAX_SEARCH_RESULTS);

                if (results.isEmpty()) {
                    return "未找到与「" + query + "」相关的知识点。";
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < results.size(); i++) {
                    KnowledgePoint p = results.get(i);
                    sb.append(String.format("【%d】[%s] %s - %s\n%s\n\n",
                            i + 1, p.getSubject(), p.getChapter(), p.getTitle(), p.getContent()));
                }
                return sb.toString();
            }
            return "未知工具：" + functionName;
        } catch (Exception e) {
            log.error("工具执行失败 — function={}", functionName, e);
            return "工具执行出错：" + e.getMessage();
        }
    }

    // ==================== RAG 模式（降级方案） ====================

    private String answerWithRag(String question, String subject, String systemPrompt) {
        log.info("TutorAgent 使用 RAG 模式 — question={}", question);

        List<String> keywords = extractKeywords(question);
        List<KnowledgePoint> results = knowledgePointMapper.searchByKeywords(
                keywords, subject, MAX_SEARCH_RESULTS);

        String context = buildContext(results);
        String userMessage = String.format("【知识库参考内容】\n%s\n\n【学生问题】\n%s", context, question);

        // 用简单单轮调用
        Map<String, Object> requestBody = Map.of(
                "model", apiProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.7,
                "max_tokens", 4096
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiProperties.getKey());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String response = aiRestTemplate.postForObject(
                    apiProperties.getEndpoint(), entity, String.class);
            if (response == null) return "【AI 返回为空】";
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText("【AI 无内容】");
        } catch (Exception e) {
            log.error("RAG 模式 LLM 调用失败", e);
            return "【AI 调用失败】" + e.getMessage();
        }
    }

    private List<String> extractKeywords(String question) {
        String[] tokens = question.split("[，。？！、\\s,.?!;；]+");
        return Arrays.stream(tokens)
                .map(String::trim)
                .filter(t -> t.length() >= 2)
                .collect(Collectors.toList());
    }

    private String buildContext(List<KnowledgePoint> points) {
        if (points.isEmpty()) return "（知识库中暂无相关内容，请基于你的专业知识回答）";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            KnowledgePoint p = points.get(i);
            sb.append(String.format("【%d】[%s] %s - %s\n%s\n\n",
                    i + 1, p.getSubject(), p.getChapter(), p.getTitle(), p.getContent()));
        }
        return sb.toString();
    }
}
