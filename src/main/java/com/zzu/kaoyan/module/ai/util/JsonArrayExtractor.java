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
 * 支持：纯 JSON、markdown code fence 包裹、前后附带说明文字、多个数组取最后一个。
 */
public final class JsonArrayExtractor {

    private static final Logger log = LoggerFactory.getLogger(JsonArrayExtractor.class);

    // 匹配 markdown code fence 中的 JSON 数组（优先）
    private static final Pattern CODE_FENCE_PATTERN = Pattern.compile("```(?:json)?\\s*(\\[\\s*\\{[\\s\\S]*?\\}\\s*])\\s*```");
    // 匹配所有 JSON 数组
    private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[\\s*\\{[\\s\\S]*?\\}\\s*]");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonArrayExtractor() {}

    /**
     * 从 LLM 原始返回中提取 task 列表。
     * 期望 JSON 格式：[{"content":"...","importance":"HIGH/MEDIUM/LOW","tips":"..."}, ...]
     *
     * 提取策略：
     * 1. 优先匹配 markdown code fence 中的 JSON 数组
     * 2. 若无 code fence，匹配所有 JSON 数组，取最后一个（LLM 通常先解释再给结果）
     * 3. 解析失败时逐个尝试
     */
    public static List<Task> extract(String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        // 策略1：优先匹配 code fence 中的内容
        Matcher fenceMatcher = CODE_FENCE_PATTERN.matcher(raw);
        if (fenceMatcher.find()) {
            List<Task> result = tryParse(fenceMatcher.group(1));
            if (!result.isEmpty()) return result;
        }

        // 策略2：匹配所有 JSON 数组，收集后取最后一个有效结果
        Matcher m = ARRAY_PATTERN.matcher(raw);
        List<String> candidates = new ArrayList<>();
        while (m.find()) {
            candidates.add(m.group());
        }

        if (candidates.isEmpty()) {
            log.warn("未找到 JSON 数组 — raw={}", raw);
            return List.of();
        }

        // 从后往前尝试解析（最后一个最可能是最终答案）
        for (int i = candidates.size() - 1; i >= 0; i--) {
            List<Task> result = tryParse(candidates.get(i));
            if (!result.isEmpty()) return result;
        }

        log.error("所有 JSON 候选均解析失败 — candidates={}", candidates);
        return List.of();
    }

    /**
     * 尝试将 JSON 字符串解析为 Task 列表。
     */
    private static List<Task> tryParse(String json) {
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
            log.debug("JSON 解析尝试失败 — json={}", json);
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
