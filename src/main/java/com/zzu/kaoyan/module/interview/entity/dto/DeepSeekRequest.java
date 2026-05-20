package com.zzu.kaoyan.module.interview.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeepSeek Chat API 请求体
 * 兼容 OpenAI Chat Completions 格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekRequest {

    /**
     * 模型名称，例如 deepseek-chat
     */
    private String model;

    /**
     * 对话消息列表，包含 system / user / assistant 三种角色
     */
    private List<Message> messages;

    /**
     * 温度参数，控制回复的随机性，范围 0.0 ~ 2.0
     */
    private Double temperature;

    /**
     * 最大生成 Token 数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    // ---------- 内部类：单条消息 ----------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 角色：system / user / assistant
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;
    }
}
