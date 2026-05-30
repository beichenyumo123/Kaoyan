package com.zzu.kaoyan.module.interview.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI Chat API 通用请求体
 * 兼容 OpenAI Chat Completions 格式，同时适用于 DeepSeek / 通义千问 / GLM 等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    private String model;

    private List<Message> messages;

    private Double temperature;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    // ---------- 内部类 ----------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
