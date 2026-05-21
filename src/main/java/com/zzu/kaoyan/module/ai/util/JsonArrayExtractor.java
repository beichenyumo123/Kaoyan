package com.zzu.kaoyan.module.ai.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从 LLM 返回文本中提取 JSON 数组的工具。
 * 使用正则强匹配 {@code [...]}，兼容 LLM 在前后附带说明文字的情况。
 */
public final class JsonArrayExtractor {

    private static final Logger log = LoggerFactory.getLogger(JsonArrayExtractor.class);

    private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[[\\s\\S]*\\]");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonArrayExtractor() {}

    /**
     * 从 LLM 原始返回中提取 task 列表。
     * 期望 JSON 格式：[{"content":"...","importance":"HIGH/MEDIUM/LOW","tips":"..."}, ...]
     */
    public static List<Task> extract(String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        Matcher m = ARRAY_PATTERN.matcher(raw);
        if (!m.find()) {
            log.warn("未找到 JSON 数组 — raw={}", raw);
            return List.of();
        }

        String json = m.group();
        try {
            List<Map<String, String>> list = MAPPER.readValue(json,
                    new TypeReference<List<Map<String, String>>>() {});
            List<Task> tasks = new ArrayList<>();
            for (Map<String, String> item : list) {
                Task t = new Task();
                t.setContent(item.get("content"));
                t.setImportance(item.get("importance"));
                t.setTips(item.get("tips"));
                tasks.add(t);
            }
            return tasks;
        } catch (Exception e) {
            log.error("JSON 数组解析失败 — json={}", json, e);
            return List.of();
        }
    }

    public static class Task {
        private String content;
        private String importance;
        private String tips;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getImportance() { return importance; }
        public void setImportance(String importance) { this.importance = importance; }
        public String getTips() { return tips; }
        public void setTips(String tips) { this.tips = tips; }
    }
}
