package com.zzu.kaoyan.module.interview.service;

import com.zzu.kaoyan.module.interview.entity.vo.ReportVO;

/**
 * AI 面试评估报告 - 业务接口
 */
public interface InterviewReportService {

    /**
     * 根据会话的历史对话记录，生成综合能力评估报告
     * <p>
     * 该方法会：
     * 1. 提取该会话的所有对话历史
     * 2. 将历史发送给 DeepSeek，要求其生成 JSON 格式的评估报告
     * 3. 解析并校验 JSON 后存入 interview_report 表
     * 4. 更新 interview_session 的状态为 REPORTED，填充综合评分
     *
     * @param sessionId 面试会话ID
     * @return 解析后的评估报告 VO
     */
    ReportVO generateFinalReport(Long sessionId);
}
