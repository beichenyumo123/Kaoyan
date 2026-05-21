package com.zzu.kaoyan.module.ai.service;

/**
 * AI Agent 统一服务：封装对 DeepSeek（兼容 OpenAI）的调用。
 */
public interface AiAgentService {

    /**
     * 向 LLM 发送一次对话请求。
     *
     * @param systemPrompt 系统角色设定（决定 Agent 行为）
     * @param userMessage  用户上下文数据
     * @return LLM 回复的文本内容
     */
    String chat(String systemPrompt, String userMessage);
}
