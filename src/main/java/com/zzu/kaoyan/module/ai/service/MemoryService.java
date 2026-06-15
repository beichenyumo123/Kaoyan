package com.zzu.kaoyan.module.ai.service;

/**
 * Agent 记忆服务 — 聚合用户多维度数据，构建统一的「学员档案」上下文，
 * 注入每个 Agent 的 LLM 提示词中，让 AI 感知用户的学习状态与历史。
 */
public interface MemoryService {

    /**
     * 构建用户记忆上下文（Markdown 格式），用于注入 Agent 系统提示词。
     *
     * @param userId 用户 ID
     * @return 格式化的学员档案文本，数据不足时返回简短摘要
     */
    String buildContext(Long userId);

    /**
     * 构建用户记忆上下文，限定学科（用于答疑 Agent 按学科过滤薄弱知识点）。
     *
     * @param userId  用户 ID
     * @param subject 限定学科（可为 null）
     * @return 格式化的学员档案文本
     */
    String buildContext(Long userId, String subject);

    /**
     * 语义检索相似历史对话（Embedding 向量余弦相似度）。
     * 在 AI 答疑时实时调用，用当前问题检索历史记忆。
     *
     * @param userId   用户 ID
     * @param question 当前问题
     * @return 相似历史文本，无结果时返回空字符串
     */
    String enrichWithSemanticMemory(Long userId, String question);
}
