package com.zzu.kaoyan.module.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.module.interview.config.DeepSeekConfig;
import com.zzu.kaoyan.module.interview.entity.InterviewRecord;
import com.zzu.kaoyan.module.interview.entity.InterviewReport;
import com.zzu.kaoyan.module.interview.entity.InterviewSession;
import com.zzu.kaoyan.module.interview.entity.dto.DeepSeekRequest;
import com.zzu.kaoyan.module.interview.entity.dto.DeepSeekResponse;
import com.zzu.kaoyan.module.interview.entity.vo.ReportVO;
import com.zzu.kaoyan.module.interview.mapper.InterviewRecordMapper;
import com.zzu.kaoyan.module.interview.mapper.InterviewReportMapper;
import com.zzu.kaoyan.module.interview.mapper.InterviewSessionMapper;
import com.zzu.kaoyan.module.interview.service.InterviewReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 面试评估报告 - 业务实现类
 * <p>
 * 核心流程：
 * 1. 查历史对话 → 2. 拼 Prompt → 3. 调 DeepSeek → 4. 解析 JSON → 5. 入库 + 更新会话状态
 * <p>
 * 当 interview.ai.mock=true 时，此 Bean 不会加载，由 Mock 实现替代
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "interview.ai.mock", havingValue = "false", matchIfMissing = true)
public class InterviewReportServiceImpl implements InterviewReportService {

    private final InterviewSessionMapper sessionMapper;
    private final InterviewRecordMapper recordMapper;
    private final InterviewReportMapper reportMapper;
    private final RestTemplate deepSeekRestTemplate;
    private final ObjectMapper objectMapper; // Spring Boot 自动配置的 Jackson ObjectMapper

    // ============================================================
    // 报告生成的 System Prompt
    // ============================================================

    /**
     * 评估报告生成专用的 System Prompt
     * 硬性要求 AI 返回合法 JSON，并详细定义了 JSON 的字段结构
     */
    private static final String REPORT_SYSTEM_PROMPT = """
            你是一位资深的考研复试评委专家。现在你需要根据一场模拟面试的完整对话记录，\
            对考生的表现进行综合评估，并**严格**按以下 JSON 格式输出评估报告。

            ## JSON 输出格式要求（必须严格遵守，不要添加任何额外的文字说明）

            ```json
            {
              "totalScore": 85.5,
              "radarChart": [
                { "dimension": "语言表达", "score": 80 },
                { "dimension": "专业知识", "score": 85 },
                { "dimension": "逻辑思维", "score": 82 },
                { "dimension": "应变能力", "score": 78 },
                { "dimension": "心理素质", "score": 80 }
              ],
              "strengthAnalysis": "考生在专业知识方面表现扎实，能够清晰阐述核心概念...",
              "weaknessAnalysis": "英语口语表达能力有待提升，部分专业术语的英文表述不够准确...",
              "suggestion": "1. 加强英文文献阅读，积累专业术语的英文表达；2. 进行更多模拟面试训练...",
              "summary": "总体而言，考生具备较好的专业基础..."
            }
            ```

            ## 评分说明
            - totalScore：综合评分，范围 0.0 ~ 100.0，是五项雷达图维度的加权平均
            - radarChart 五个维度及满分均为 100：
              1. 语言表达：中/英文口语流利度、用词准确性、表达清晰度
              2. 专业知识：对报考专业的理论掌握程度、前沿动态了解程度
              3. 逻辑思维：回答问题时的条理性、论证的严谨性、分析的深度
              4. 应变能力：面对追问时的反应速度、灵活性、能否自圆其说
              5. 心理素质：面试中的自信程度、抗压能力、沟通态度
            - strengthAnalysis：总结考生的优势，100 ~ 200 字
            - weaknessAnalysis：指出考生需要改进的薄弱项，100 ~ 200 字
            - suggestion：给出 3 ~ 5 条具体可操作的备考建议
            - summary：一段话概括整体表现，80 ~ 150 字

            ## 重要提醒
            - 只输出 JSON，**绝对不要**输出 markdown 代码块标记（如 \\`\\`\\`json）
            - 只输出 JSON，**绝对不要**在 JSON 前后添加任何解释性文字
            - 确保 JSON 中所有字符串使用双引号，且不包含未转义的特殊字符
            - 评分务必客观公正，避免全部满分或全部不及格""";

    // ============================================================
    // 核心方法
    // ============================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO generateFinalReport(Long sessionId) {
        // ---------- Step 1：校验会话状态 ----------
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "面试会话不存在");
        }
        if ("REPORTED".equals(session.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该会话已生成过报告，请勿重复操作");
        }

        // ---------- Step 2：提取该会话的全部对话记录 ----------
        List<InterviewRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<InterviewRecord>()
                        .eq(InterviewRecord::getSessionId, sessionId)
                        .orderByAsc(InterviewRecord::getCreatedAt)
        );

        if (records.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该会话暂无对话记录，无法生成报告");
        }

        log.info("开始生成评估报告，sessionId={}, 对话记录数={}", sessionId, records.size());

        // ---------- Step 3：构建发往 DeepSeek 的 Message 列表 ----------
        List<DeepSeekRequest.Message> messages = List.of(
                // System 消息：定义报告输出格式
                DeepSeekRequest.Message.builder()
                        .role("system")
                        .content(REPORT_SYSTEM_PROMPT)
                        .build(),
                // User 消息：将全部对话记录作为上下文传入，要求 AI 生成报告
                DeepSeekRequest.Message.builder()
                        .role("user")
                        .content(buildConversationContext(session, records))
                        .build()
        );

        // ---------- Step 4：调用 DeepSeek API ----------
        String rawJson = callDeepSeekForReport(messages);
        log.info("DeepSeek 返回报告 JSON，长度={}", rawJson.length());

        // ---------- Step 5：解析 JSON 为 ReportVO（含异常处理）----------
        ReportVO reportVO = parseReportJson(rawJson);

        // ---------- Step 6：报告入库 ----------
        InterviewReport report = new InterviewReport();
        report.setSessionId(sessionId);
        report.setTotalScore(reportVO.getTotalScore());
        report.setRadarChart(toJsonString(reportVO.getRadarChart()));   // 雷达图序列化为 JSON 字符串存 MySQL JSON 列
        report.setStrengthAnalysis(reportVO.getStrengthAnalysis());
        report.setWeaknessAnalysis(reportVO.getWeaknessAnalysis());
        report.setSuggestion(reportVO.getSuggestion());
        report.setSummary(reportVO.getSummary());
        report.setRawJson(rawJson); // 保留 AI 原始返回值，便于排查问题
        reportMapper.insert(report);

        // ---------- Step 7：回填 interview_session 的综合评分 & 更新状态 ----------
        session.setOverallScore(reportVO.getTotalScore());
        session.setStatus("REPORTED");
        sessionMapper.updateById(session);

        log.info("评估报告生成完毕，reportId={}, sessionId={}, totalScore={}",
                report.getId(), sessionId, reportVO.getTotalScore());

        return reportVO;
    }

    // ============================================================
    // 私有辅助方法
    // ============================================================

    /**
     * 将整场面试的对话记录拼装成一段结构化的上下文文本，
     * 方便 DeepSeek 阅读并生成报告
     */
    private String buildConversationContext(InterviewSession session, List<InterviewRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 面试基本信息\n");
        sb.append("- 目标院校：").append(nullToEmpty(session.getTargetSchool())).append("\n");
        sb.append("- 目标专业：").append(nullToEmpty(session.getTargetMajor())).append("\n");
        sb.append("- 面试类型：").append(nullToEmpty(session.getInterviewType())).append("\n\n");

        sb.append("## 面试对话记录（按时间顺序）\n");
        for (InterviewRecord record : records) {
            String roleLabel = "ai".equals(record.getRole()) ? "AI面试官" : "考生";
            sb.append("【").append(roleLabel).append("】")
                    .append(record.getContent())
                    .append("\n\n");
        }

        sb.append("## 要求\n");
        sb.append("请根据以上对话记录，按 System Prompt 中指定的 JSON 格式生成评估报告。");
        return sb.toString();
    }

    /**
     * 调用 DeepSeek API 获取报告 JSON（阻塞式）
     * <p>
     * 此处使用较低的 temperature（0.3），以提升 JSON 格式输出的稳定性
     */
    private String callDeepSeekForReport(List<DeepSeekRequest.Message> messages) {
        DeepSeekRequest requestBody = DeepSeekRequest.builder()
                .model(DeepSeekConfig.MODEL)
                .messages(messages)
                .temperature(0.3)   // 低温度以获得更确定性的 JSON 输出
                .maxTokens(2048)    // 报告需要更多 Token
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(DeepSeekConfig.API_KEY);
        HttpEntity<DeepSeekRequest> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            // ============================================================
            // TODO：【重要】DeepSeekConfig.API_URL 当前为占位值，接入前请填写真实地址
            // ============================================================
            ResponseEntity<DeepSeekResponse> response = deepSeekRestTemplate.postForEntity(
                    DeepSeekConfig.API_URL,
                    httpEntity,
                    DeepSeekResponse.class
            );

            DeepSeekResponse body = response.getBody();
            if (body == null || body.getFirstContent() == null) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "AI 服务返回空响应");
            }
            return body.getFirstContent();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 DeepSeek 生成报告失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "AI 报告生成失败: " + e.getMessage());
        }
    }

    /**
     * 解析 AI 返回的 JSON 字符串为 ReportVO
     * <p>
     * 包含多层容错机制：
     * 1. 直接解析原始文本
     * 2. 若失败，尝试从 markdown 代码块（```json ... ```）中提取 JSON
     * 3. 若仍失败，抛出业务异常提示用户稍后重试
     */
    private ReportVO parseReportJson(String rawJson) {
        // --- 第一层：直接解析 ---
        try {
            return objectMapper.readValue(rawJson, ReportVO.class);
        } catch (JsonProcessingException e) {
            log.warn("直接解析 AI 返回 JSON 失败，尝试从 markdown 代码块中提取。原始内容: {}", rawJson);
        }

        // --- 第二层：从 markdown 代码块中提取 JSON ---
        String extracted = extractJsonFromMarkdown(rawJson);
        if (extracted != null) {
            try {
                return objectMapper.readValue(extracted, ReportVO.class);
            } catch (JsonProcessingException e) {
                log.warn("从 markdown 代码块提取的 JSON 仍然解析失败: {}", extracted);
            }
        }

        // --- 第三层：校验必填字段，给出明确错误提示 ---
        log.error("无法解析 AI 返回的报告 JSON，原始内容: {}", rawJson);
        throw new BusinessException(
                ResultCode.SYSTEM_ERROR.getCode(),
                "AI 生成的报告格式异常，请稍后重试或联系管理员查看原始数据"
        );
    }

    /**
     * 尝试从文本中提取被 markdown 代码块包裹的 JSON
     * 匹配模式：```json ... ```  或  ``` ... ```
     */
    private String extractJsonFromMarkdown(String text) {
        if (text == null) return null;
        // 匹配 ```json 或 ``` 开头的代码块
        Pattern pattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 尝试找到第一个 { 和最后一个 } 之间的内容
        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1).trim();
        }

        return null;
    }

    /**
     * 将 Java 对象序列化为 JSON 字符串
     */
    private String toJsonString(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON 序列化失败", e);
            return null;
        }
    }

    /**
     * null 安全转空字符串
     */
    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
