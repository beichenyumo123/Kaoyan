package com.zzu.kaoyan.module.interview.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek Chat API 响应体
 * 兼容 OpenAI Chat Completions 格式（仅提取必要字段）
 */
@Data
public class DeepSeekResponse {

    /**
     * 响应ID
     */
    private String id;

    /**
     * 生成的候选回复列表
     */
    private List<Choice> choices;

    // ---------- 内部类 ----------

    @Data
    public static class Choice {
        /**
         * 候选序号
         */
        private Integer index;

        /**
         * 消息对象
         */
        private Message message;
    }

    @Data
    public static class Message {
        /**
         * 角色，通常为 assistant
         */
        private String role;

        /**
         * AI 回复内容
         */
        private String content;
    }

    // ---------- 便捷方法 ----------

    /**
     * 提取第一个候选回复的文本内容
     */
    public String getFirstContent() {
        if (choices != null && !choices.isEmpty()
                && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().getContent();
        }
        return null;
    }
}
