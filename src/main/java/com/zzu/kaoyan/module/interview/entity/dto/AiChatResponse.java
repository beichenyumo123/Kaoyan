package com.zzu.kaoyan.module.interview.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * AI Chat API 通用响应体
 * 兼容 OpenAI Chat Completions 格式
 */
@Data
public class AiChatResponse {

    private String id;

    private List<Choice> choices;

    // ---------- 内部类 ----------

    @Data
    public static class Choice {
        private Integer index;
        private Message message;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }

    // ---------- 便捷方法 ----------

    /** 提取第一个候选回复的文本内容 */
    public String getFirstContent() {
        if (choices != null && !choices.isEmpty()
                && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().getContent();
        }
        return null;
    }
}
