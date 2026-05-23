package com.zzu.kaoyan.module.interview.service;

import com.zzu.kaoyan.module.interview.entity.dto.StartInterviewDTO;
import com.zzu.kaoyan.module.interview.entity.dto.SubmitAnswerDTO;
import com.zzu.kaoyan.module.interview.entity.vo.InterviewReportVO;
import com.zzu.kaoyan.module.interview.entity.vo.InterviewSessionVO;
import com.zzu.kaoyan.module.interview.entity.vo.QuestionVO;

import java.util.List;

public interface InterviewService {

    /**
     * 创建新的模拟复试会话并返回第一个问题
     */
    QuestionVO startInterview(Long userId, StartInterviewDTO dto);

    /**
     * 提交回答、获得评分并返回下一个问题（如果没有更多问题则返回null）
     */
    QuestionVO submitAnswer(Long userId, SubmitAnswerDTO dto);

    /**
     * 主动结束面试并生成评估报告
     */
    InterviewReportVO endInterview(Long userId, Long sessionId);

    /**
     * 获取会话信息
     */
    InterviewSessionVO getSession(Long userId, Long sessionId);

    /**
     * 获取用户的历史面试记录
     */
    List<InterviewSessionVO> getHistory(Long userId, int pageNum, int pageSize);

    /**
     * 获取评估报告
     */
    InterviewReportVO getReport(Long userId, Long sessionId);

    /**
     * 获取会话中所有问答记录
     */
    List<QuestionVO> getSessionQuestions(Long userId, Long sessionId);
}
