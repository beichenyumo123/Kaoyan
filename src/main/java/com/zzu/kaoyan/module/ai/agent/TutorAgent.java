package com.zzu.kaoyan.module.ai.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.module.ai.config.AiApiProperties;
import com.zzu.kaoyan.module.ai.entity.KnowledgePoint;
import com.zzu.kaoyan.module.ai.mapper.KnowledgePointMapper;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
import org.slf4j.Logger;
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

    private final KnowledgePointMapper knowledgePointMapper;
    private final AiAgentService aiAgentService;
    private final RestTemplate aiRestTemplate;
    private final RestTemplate aiStreamRestTemplate;
    private final AiApiProperties apiProperties;
    private final ObjectMapper objectMapper;

    public TutorAgent(KnowledgePointMapper knowledgePointMapper,
                      AiAgentService aiAgentService,
                      RestTemplate aiRestTemplate,
                      @Qualifier("aiStreamRestTemplate") RestTemplate aiStreamRestTemplate,
                      AiApiProperties apiProperties) {
        this.knowledgePointMapper = knowledgePointMapper;
        this.aiAgentService = aiAgentService;
        this.aiRestTemplate = aiRestTemplate;
        this.aiStreamRestTemplate = aiStreamRestTemplate;
        this.apiProperties = apiProperties;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 回答用户问题（Tool Calling 模式）。
     *
     * @param question 用户提问
     * @param subject  限定学科（可为 null 则全学科检索）
     * @return RAG + Tool Calling 增强的回答文本
     */
    public String answer(String question, String subject) {
        log.info("TutorAgent 开始答疑 — question={}, subject={}", question, subject);

        // 优先使用 Tool Calling 模式
        try {
            return answerWithToolCalling(question, subject);
        } catch (Exception e) {
            log.warn("Tool Calling 失败，降级为 RAG 模式 — {}", e.getMessage());
            return answerWithRag(question, subject);
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
        log.info("TutorAgent 流式答疑 — question={}, subject={}", question, subject);

        // 先做 RAG 检索
        List<String> keywords = extractKeywords(question);
        List<KnowledgePoint> results = knowledgePointMapper.searchByKeywords(
                keywords, subject, MAX_SEARCH_RESULTS);
        String context = buildContext(results);
        String userMessage = String.format("【知识库参考内容】\n%s\n\n【学生问题】\n%s", context, question);

        // 构建流式请求
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", apiProperties.getModel());
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
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

    // ==================== Tool Calling 模式 ====================

    private String answerWithToolCalling(String question, String subject) throws Exception {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
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
        return answerWithRag(question, subject);
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

    private String answerWithRag(String question, String subject) {
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
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
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
