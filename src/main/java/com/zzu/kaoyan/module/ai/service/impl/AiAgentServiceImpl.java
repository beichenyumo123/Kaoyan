package com.zzu.kaoyan.module.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.module.ai.config.AiApiProperties;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiAgentServiceImpl implements AiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiAgentServiceImpl.class);

    private final RestTemplate aiRestTemplate;
    private final AiApiProperties apiProperties;
    private final ObjectMapper objectMapper;

    public AiAgentServiceImpl(RestTemplate aiRestTemplate, AiApiProperties apiProperties) {
        this.aiRestTemplate = aiRestTemplate;
        this.apiProperties = apiProperties;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (apiProperties.getKey() == null || apiProperties.getKey().isBlank()) {
            log.warn("AI API Key 未配置，返回占位回复");
            return "【AI 未配置】请设置 ai.api.key";
        }

        try {
            Map<String, Object> requestBody = buildRequestBody(systemPrompt, userMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiProperties.getKey());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String response = aiRestTemplate.postForObject(
                    apiProperties.getEndpoint(), entity, String.class);

            if (response == null) {
                log.error("AI API 返回 null");
                return "【AI 返回为空】";
            }

            return extractContent(response);
        } catch (RestClientException e) {
            log.error("AI API 调用失败", e);
            return "【AI 调用失败】" + e.getMessage();
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
