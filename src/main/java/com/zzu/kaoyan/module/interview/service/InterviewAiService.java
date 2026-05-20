package com.zzu.kaoyan.module.interview.service;

import com.zzu.kaoyan.module.interview.entity.InterviewRecord;

/**
 * AI 模拟面试官 - 核心业务接口
 */
public interface InterviewAiService {

    /**
     * 根据用户的最新回答，生成 AI 面试官的下一个追问
     *
     * @param sessionId         面试会话ID
     * @param userLatestAnswer  用户最新一轮的回答内容
     * @return 保存后的 AI 对话记录（包含追问内容）
     */
    InterviewRecord generateNextQuestion(Long sessionId, String userLatestAnswer);
}
