package com.zzu.kaoyan.module.interview.service;

import com.zzu.kaoyan.module.interview.entity.po.InterviewQuestion;
import com.zzu.kaoyan.module.interview.entity.vo.DimensionScoresVO;

/**
 * AI面试服务接口 — 可替换为真实LLM实现
 */
public interface AIInterviewService {

    /**
     * 生成下一个面试问题
     * @param schoolStyle 院校风格
     * @param major 报考专业
     * @param interviewType 面试类型
     * @param questionNumber 问题序号
     * @param previousQAs 之前的问答记录（用于追问上下文）
     * @return 生成的面试官问题文本
     */
    String generateQuestion(String schoolStyle, String major, String interviewType,
                            int questionNumber, String previousQAs);

    /**
     * 评估考生回答
     * @param question 当前问题
     * @param answer 考生回答
     * @param schoolStyle 院校风格
     * @return 填充了评分和点评的question对象
     */
    void evaluateAnswer(InterviewQuestion question, String answer, String schoolStyle);

    /**
     * 生成多维度评分
     */
    DimensionScoresVO calculateDimensionScores(String questionType, String answer);

    /**
     * 生成最终复试评估报告文本
     */
    String generateSummaryReport(String schoolStyle, String major, String interviewType, String allQAs);
}
