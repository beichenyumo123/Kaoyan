package com.zzu.kaoyan.common.util;

import java.util.*;

/**
 * 敏感词过滤工具，基于 DFA（Trie 树）算法实现。
 * 匹配方式：最长匹配原则，命中替换为 ***。
 */
public class SensitiveWordUtil {

    private static final TrieNode ROOT = new TrieNode();
    private static final String REPLACEMENT = "***";

    static {
        // === 色情低俗 ===
        addWord("色情");
        addWord("淫秽");
        addWord("裸聊");
        addWord("成人视频");
        addWord("约炮");
        addWord("黄片");
        addWord("av女优");

        // === 赌博 ===
        addWord("赌博");
        addWord("博彩");
        addWord("赌场");
        addWord("六合彩");
        addWord("押注");
        addWord("时时彩");

        // === 广告引流 ===
        addWord("兼职日结");
        addWord("加微信");
        addWord("扫码领");
        addWord("点击领取");
        addWord("免费红包");

        // === 政治敏感 ===
        addWord("法轮功");
        addWord("台独");
        addWord("藏独");
        addWord("疆独");
        addWord("falungong");

        // === 辱骂攻击 ===
        addWord("傻逼");
        addWord("他妈的");
        addWord("操你妈");
        addWord("去死吧");

        // === 违法 ===
        addWord("代办证件");
        addWord("枪支弹药");
        addWord("毒品");
    }

    private SensitiveWordUtil() {
    }

    /**
     * 添加单个敏感词到 Trie 树
     */
    public static void addWord(String word) {
        if (word == null || word.isEmpty()) return;
        TrieNode node = ROOT;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEnd = true;
    }

    /**
     * 批量添加
     */
    public static void addWords(Collection<String> words) {
        if (words != null) {
            words.forEach(SensitiveWordUtil::addWord);
        }
    }

    /**
     * 过滤文本，返回 FilterResult。
     * DFA 最长匹配：同一位置若多个词命中，取最长的那个。
     */
    public static FilterResult filter(String text) {
        if (text == null || text.isEmpty()) {
            return new FilterResult(text, false, Collections.emptyList());
        }

        StringBuilder out = new StringBuilder();
        List<String> matched = new ArrayList<>();
        int i = 0;
        int len = text.length();

        while (i < len) {
            TrieNode node = ROOT;
            int matchEnd = -1;

            // 从当前位置向后尝试，记录最长匹配
            for (int j = i; j < len; j++) {
                node = node.children.get(text.charAt(j));
                if (node == null) break;
                if (node.isEnd) {
                    matchEnd = j; // 记录匹配终点，继续找更长的
                }
            }

            if (matchEnd >= 0) {
                String matchedWord = text.substring(i, matchEnd + 1);
                matched.add(matchedWord);
                out.append(REPLACEMENT);
                i = matchEnd + 1;
            } else {
                out.append(text.charAt(i));
                i++;
            }
        }

        return new FilterResult(out.toString(), !matched.isEmpty(), matched);
    }


    // ==================== DFA Trie 节点 ====================

    private static class TrieNode {
        final Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd;
    }

    // ==================== 结果对象 ====================

    public static class FilterResult {
        private final String filteredText;
        private final boolean hasSensitive;
        private final List<String> matched;

        FilterResult(String filteredText, boolean hasSensitive, List<String> matched) {
            this.filteredText = filteredText;
            this.hasSensitive = hasSensitive;
            this.matched = matched;
        }

        public String getFilteredText() {
            return filteredText;
        }

        public boolean isHasSensitive() {
            return hasSensitive;
        }

        public List<String> getMatched() {
            return matched;
        }
    }
}
