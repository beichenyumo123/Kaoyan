package com.zzu.kaoyan.module.interview.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.module.interview.entity.InterviewRecord;
import com.zzu.kaoyan.module.interview.entity.InterviewReport;
import com.zzu.kaoyan.module.interview.entity.InterviewSession;
import com.zzu.kaoyan.module.interview.entity.vo.ReportVO;
import com.zzu.kaoyan.module.interview.mapper.InterviewRecordMapper;
import com.zzu.kaoyan.module.interview.mapper.InterviewReportMapper;
import com.zzu.kaoyan.module.interview.mapper.InterviewSessionMapper;
import com.zzu.kaoyan.module.interview.service.InterviewReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 面试报告 - Mock 实现（用于开发/测试阶段）
 * <p>
 * 当 application.properties 中设置 interview.ai.mock=true 时自动启用。
 * 返回一份固定结构的模拟报告，不调用 DeepSeek API。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "interview.ai.mock", havingValue = "true")
public class InterviewReportServiceMockImpl implements InterviewReportService {

    private final InterviewSessionMapper sessionMapper;
    private final InterviewRecordMapper recordMapper;
    private final InterviewReportMapper reportMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO generateFinalReport(Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(404, "面试会话不存在");
        }
        if ("REPORTED".equals(session.getStatus())) {
            throw new BusinessException(400, "该会话已生成过报告，请勿重复操作");
        }

        // 生成模拟报告
        ReportVO reportVO = buildMockReport();

        // 入库
        InterviewReport report = new InterviewReport();
        report.setSessionId(sessionId);
        report.setTotalScore(reportVO.getTotalScore());
        try {
            report.setRadarChart(objectMapper.writeValueAsString(reportVO.getRadarChart()));
        } catch (Exception e) {
            log.error("序列化雷达图失败", e);
        }
        report.setStrengthAnalysis(reportVO.getStrengthAnalysis());
        report.setWeaknessAnalysis(reportVO.getWeaknessAnalysis());
        report.setSuggestion(reportVO.getSuggestion());
        report.setSummary(reportVO.getSummary());
        report.setRawJson("[Mock] 此报告为 Mock 模式生成，未经过 AI 评估");
        reportMapper.insert(report);

        // 更新会话
        session.setOverallScore(reportVO.getTotalScore());
        session.setStatus("REPORTED");
        sessionMapper.updateById(session);

        log.info("[Mock模式] 报告已生成，reportId={}, totalScore={}", report.getId(), reportVO.getTotalScore());
        return reportVO;
    }

    private ReportVO buildMockReport() {
        ReportVO vo = new ReportVO();
        vo.setTotalScore(new BigDecimal("82.5"));

        vo.setRadarChart(List.of(
                dim("语言表达", "78"),
                dim("专业知识", "85"),
                dim("逻辑思维", "82"),
                dim("应变能力", "80"),
                dim("心理素质", "83")
        ));

        vo.setStrengthAnalysis("考生专业知识基础扎实，对报考方向有清晰的认识，能够有条理地阐述自己的科研经历。回答问题时结构清晰，能够抓住问题核心并展开论述。");
        vo.setWeaknessAnalysis("英语口语表达流畅度有待提升，部分专业术语的英文表述不够准确。面对开放性问题时思考时间偏长，需要加强临场组织语言的能力。");
        vo.setSuggestion("1. 坚持每天30分钟英文口语练习，重点积累报考专业的高频英文术语；\n2. 收集整理近三年目标院校的复试真题，进行针对性模拟训练；\n3. 加强时政热点和专业前沿动态的关注，拓宽知识面；\n4. 进行1-2次完整的全真模拟面试，提升临场心理素质。");
        vo.setSummary("整体表现良好，专业知识掌握扎实，逻辑表达清晰。建议在英语口语和应变速度方面重点加强，进一步提升综合竞争力。");

        return vo;
    }

    private ReportVO.RadarDimension dim(String name, String score) {
        ReportVO.RadarDimension d = new ReportVO.RadarDimension();
        d.setDimension(name);
        d.setScore(new BigDecimal(score));
        return d;
    }
}
