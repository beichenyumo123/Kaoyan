package com.zzu.kaoyan.module.interview.service;

import com.zzu.kaoyan.module.interview.entity.vo.ReportVO;

import java.util.Map;

/**
 * AI 面试评估报告 - 业务接口
 */
public interface InterviewReportService {

    /**
     * 根据会话的历史对话记录，生成综合能力评估报告
     *
     * @param sessionId        面试会话ID
     * @param demeanorSummary  视频模式的仪态汇总数据，非视频模式为 null
     * @return 解析后的评估报告 VO
     */
    ReportVO generateFinalReport(Long sessionId, Map<String, Object> demeanorSummary);
}
