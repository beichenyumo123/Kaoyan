package com.zzu.kaoyan.module.ai.service;

import java.util.List;
import java.util.Map;

/**
 * AI Agent 统一服务：封装对 DeepSeek（兼容 OpenAI）的调用。
 */
public interface AiAgentService {

    /**
     * 向 LLM 发送一次单轮对话请求。
     *
     * @param systemPrompt 系统角色设定（决定 Agent 行为）
     * @param userMessage  用户上下文数据
     * @return LLM 回复的文本内容
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * 向 LLM 发送带历史上下文的多轮对话请求（对话记忆）。
     * 自动管理 Redis 中的对话历史，保留最近 10 条消息（5 轮对话）。
     *
     * @param userId       用户 ID（用于隔离不同用户的对话历史）
     * @param systemPrompt 系统角色设定
     * @param userMessage  当前用户消息
     * @return LLM 回复的文本内容
     */
    String chatWithHistory(Long userId, String systemPrompt, String userMessage);

    /**
     * 获取指定用户的对话历史。
     *
     * @param userId 用户 ID
     * @return 消息列表，每条包含 role 和 content
     */
    List<Map<String, String>> getHistory(Long userId);

    /**
     * 清除指定用户的对话历史。
     *
     * @param userId 用户 ID
     */
    void clearHistory(Long userId);
}
