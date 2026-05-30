package com.zzu.kaoyan.module.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.module.interview.config.QwenConfig;
import com.zzu.kaoyan.module.interview.entity.InterviewRecord;
import com.zzu.kaoyan.module.interview.entity.InterviewReport;
import com.zzu.kaoyan.module.interview.entity.InterviewSession;
import com.zzu.kaoyan.module.interview.entity.dto.AiChatRequest;
import com.zzu.kaoyan.module.interview.entity.dto.AiChatResponse;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 面试评估报告 - 业务实现类
 * <p>
 * 核心流程：
 * 1. 查历史对话 → 2. 拼 Prompt → 3. 调千问 → 4. 解析 JSON → 5. 入库 + 更新会话状态
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
    private final QwenConfig qwenConfig;
    private final RestTemplate qwenRestTemplate;
    private final ObjectMapper objectMapper;

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

            ## 违规扣分规则（一票否决制）
            - 考生每透露一次个人信息（姓名/本科院校），语言表达扣20分，且总评上限降10分。
            - 若考官已明确提醒"不要透露个人信息"后，考生仍然再次透露：\
            每再犯一次，所有维度各额外扣15分，总评上限再降15分。
            - 被考官提醒3次及以上仍继续违规者，五个维度全部不超过30分，totalScore不超过25分。\
            这不是"心理素质好"——这是无视考试规则，应被评为严重违纪倾向。
            - "心理素质"维度不能独立于行为评判：如果考生反复无视考官指令、\
            不回答问题实质内容、或态度敷衍，则心理素质不得高于30分，因为真正的心理素质包含\
            "规则意识"和"对考试的基本尊重"。

            ## 评分校准
            - 如果对话记录中考生从未回答任何实质性问题（全程只说了个人信息或无效内容），\
            知识/逻辑/表达三维度应直接给0-10分。
            - 不要因为考生"说话平稳、没哭没跑"就给高心理素质分。\
            无视规则 ≠ 心理素质好。遵守规则、认真作答、有实质输出，才配得上高分。
            - 评分必须有区分度：优秀85+、及格60-70、严重违规25以下。

            ## 重要提醒
            - 只输出 JSON，**绝对不要**输出 markdown 代码块标记（如 \\`\\`\\`json）
            - 只输出 JSON，**绝对不要**在 JSON 前后添加任何解释性文字
            - 确保 JSON 中所有字符串使用双引号，且不包含未转义的特殊字符""";

    // ============================================================
    // 核心方法
    // ============================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO generateFinalReport(Long sessionId, java.util.Map<String, Object> demeanorSummary) {
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

        // 过滤掉开场占位消息，统计真实用户回答轮次
        List<InterviewRecord> realRecords = records.stream()
                .filter(r -> !"（面试开始）".equals(r.getContent()))
                .toList();
        long realUserAnswers = realRecords.stream()
                .filter(r -> "user".equals(r.getRole()))
                .count();

        log.info("开始生成评估报告，sessionId={}, 总记录数={}, 有效记录数={}, 用户有效回答数={}",
                sessionId, records.size(), realRecords.size(), realUserAnswers);

        // ---------- Step 3：不足3轮有效回答 → 生成 0 分无效报告 ----------
        if (realUserAnswers < 3) {
            return buildInsufficientReport(sessionId, session, (int) realUserAnswers);
        }

        // ---------- Step 4：构建发往千问的 Message 列表 ----------
        List<AiChatRequest.Message> messages = List.of(
                AiChatRequest.Message.builder()
                        .role("system")
                        .content(REPORT_SYSTEM_PROMPT)
                        .build(),
                AiChatRequest.Message.builder()
                        .role("user")
                        .content(buildConversationContext(session, realRecords))
                        .build()
        );

        // ---------- Step 5：调用千问 API ----------
        String rawJson = callQwenForReport(messages);
        log.info("千问返回报告 JSON，长度={}", rawJson.length());

        // ---------- Step 6：解析 JSON 为 ReportVO（含异常处理）----------
        ReportVO reportVO = parseReportJson(rawJson);

        // ---------- Step 7：报告入库 ----------
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

        // ---------- Step 8：回填 interview_session 的综合评分 & 更新状态 ----------
        session.setOverallScore(reportVO.getTotalScore());
        session.setStatus("REPORTED");
        sessionMapper.updateById(session);

        // ---------- Step 9：仪态分析（仅视频模式，Java 侧计算，不依赖 AI）----------
        if (demeanorSummary != null && !demeanorSummary.isEmpty()) {
            reportVO.setDemeanorAnalysis(buildDemeanorAnalysis(demeanorSummary));
        }

        log.info("评估报告生成完毕，reportId={}, sessionId={}, totalScore={}",
                report.getId(), sessionId, reportVO.getTotalScore());

        return reportVO;
    }

    // ============================================================
    // 私有辅助方法
    // ============================================================

    /**
     * 将整场面试的对话记录拼装成一段结构化的上下文文本，
     * 方便 AI 阅读并生成报告
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
     * 调用千问 API 获取报告 JSON（阻塞式）
     * <p>
     * 使用较低的 temperature（0.3）以提升 JSON 格式输出的稳定性
     */
    private String callQwenForReport(List<AiChatRequest.Message> messages) {
        AiChatRequest requestBody = AiChatRequest.builder()
                .model(QwenConfig.MODEL)
                .messages(messages)
                .temperature(0.3)
                .maxTokens(2048)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(qwenConfig.apiKey);
        HttpEntity<AiChatRequest> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<AiChatResponse> response = qwenRestTemplate.postForEntity(
                    qwenConfig.apiUrl,
                    httpEntity,
                    AiChatResponse.class
            );

            AiChatResponse body = response.getBody();
            if (body == null || body.getFirstContent() == null) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "AI 服务返回空响应");
            }
            return body.getFirstContent();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用千问生成报告失败", e);
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

    /**
     * 根据前端采集的仪态数据，直接生成仪态分析建议（不依赖 AI，避免幻觉）
     */
    private ReportVO.DemeanorAnalysis buildDemeanorAnalysis(java.util.Map<String, Object> summary) {
        ReportVO.DemeanorAnalysis analysis = new ReportVO.DemeanorAnalysis();

        Integer eyeContact = toInt(summary.get("averageEyeContact"));
        Integer posture = toInt(summary.get("averagePosture"));
        Integer blinkRate = toInt(summary.get("averageBlinkRate"));
        String dominantExpr = summary.get("dominantExpression") != null
                ? summary.get("dominantExpression").toString() : "neutral";
        Integer snapshots = toInt(summary.get("totalSnapshots"));

        analysis.setAverageEyeContact(eyeContact);
        analysis.setAveragePosture(posture);
        analysis.setAverageBlinkRate(blinkRate);
        analysis.setDominantExpression(mapExpressionLabel(dominantExpr));
        analysis.setTotalSnapshots(snapshots);

        // 根据指标生成中文建议文字
        StringBuilder sb = new StringBuilder();

        // 眼神分析
        if (eyeContact != null) {
            if (eyeContact >= 75) {
                sb.append("眼神交流良好，大部分时间保持与镜头的对视，展现出自信和专注。");
            } else if (eyeContact >= 50) {
                sb.append("眼神交流一般，存在一定程度的视线漂移。建议在回答问题时尽量注视镜头，"
                        + "这会让你显得更加自信和真诚。可以尝试把摄像头想象成考官的眼睛。");
            } else {
                sb.append("眼神交流偏弱，频繁看向屏幕外或下方。这在真实复试中会被解读为准备不充分或"
                        + "缺乏自信。建议多进行对着镜头的模拟练习，培养与考官对视的习惯。");
            }
        }

        // 坐姿分析
        if (posture != null) {
            sb.append("坐姿方面，");
            if (posture >= 75) {
                sb.append("整体保持端正，头部稳定，给考官留下良好的第一印象。");
            } else if (posture >= 50) {
                sb.append("存在一定程度的身体晃动或头部倾斜。建议在面试时保持上身挺直、双肩放松，"
                        + "避免频繁调整坐姿或晃动身体。可以录制自己的练习视频来发现问题。");
            } else {
                sb.append("身体晃动或倾斜较为明显，这会影响考官对你的印象。"
                        + "强烈建议在日常练习中有意识地保持端正坐姿，形成肌肉记忆。");
            }
        }

        // 表情分析
        if (dominantExpr != null) {
            sb.append("表情方面，");
            if ("smiling".equals(dominantExpr)) {
                sb.append("面带微笑、表情自然放松，给考官传递出积极亲和的态度。继续保持。");
            } else if ("nervous".equals(dominantExpr)) {
                sb.append("面部表情偏紧张，嘴唇紧绷或频繁抿嘴。紧张是正常的，但过度紧张会影响发挥。"
                        + "建议面试前做几次深呼吸，给自己积极的心理暗示，把注意力放在回答内容上而非紧张情绪上。");
            } else {
                sb.append("表情以自然为主，这是好的。可以在适当时机加入微笑，让沟通更有温度。");
            }
        }

        // 眨眼分析
        if (blinkRate != null) {
            sb.append("眨眼频率约每分钟").append(blinkRate).append("次。");
            if (blinkRate > 30) {
                sb.append("眨眼偏频繁，通常与紧张或眼睛疲劳有关。建议面试前减少屏幕使用时间，"
                        + "面试中可以有意识地放慢语速，紧张缓解后眨眼频率自然会下降。");
            } else if (blinkRate < 8) {
                sb.append("眨眼偏少，可能是长时间盯着屏幕导致的。适当眨眼有助于保持眼部湿润和自然神态。");
            } else {
                sb.append("眨眼频率在正常范围内（正常约10-25次/分钟）。");
            }
        }

        analysis.setSuggestion(sb.toString());
        return analysis;
    }

    private Integer toInt(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof String) {
            try { return Integer.parseInt((String) obj); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private String mapExpressionLabel(String expr) {
        return switch (expr) {
            case "smiling" -> "微笑";
            case "nervous" -> "紧张";
            default -> "自然";
        };
    }

    /**
     * 当真实回答不足 3 轮时，生成 0 分无效报告（不调用 AI，直接构造）
     */
    private ReportVO buildInsufficientReport(Long sessionId, InterviewSession session, int answerCount) {
        ReportVO vo = new ReportVO();
        vo.setTotalScore(BigDecimal.ZERO);
        vo.setRadarChart(List.of(
                dim0("语言表达"), dim0("专业知识"), dim0("逻辑思维"),
                dim0("应变能力"), dim0("心理素质")
        ));
        vo.setStrengthAnalysis("无有效回答，无法评估优势。");
        vo.setWeaknessAnalysis("考生仅回答了" + answerCount + "个问题，未达到最低3轮的有效问答要求。"
                + "复试面试通常包含10-20分钟的深度交流，你需要在每轮提问中都给出完整的回答。");
        vo.setSuggestion("1. 重新参加一次完整的模拟面试，至少回答5个以上的问题；\n"
                + "2. 每个问题的回答应在3-5句话以上，避免一句话带过；\n"
                + "3. 面试中要主动展示自己的专业能力和综合素质，抓住每一次作答机会。");
        vo.setSummary("因有效回答不足（仅" + answerCount + "轮），本次面试评估无效，综合评分为0分。"
                + "建议充分准备后重新参加面试。");

        InterviewReport report = new InterviewReport();
        report.setSessionId(sessionId);
        report.setTotalScore(BigDecimal.ZERO);
        report.setRadarChart(toJsonString(vo.getRadarChart()));
        report.setStrengthAnalysis(vo.getStrengthAnalysis());
        report.setWeaknessAnalysis(vo.getWeaknessAnalysis());
        report.setSuggestion(vo.getSuggestion());
        report.setSummary(vo.getSummary());
        report.setRawJson("[无效报告] 有效回答仅" + answerCount + "轮，未达到3轮最低要求");
        reportMapper.insert(report);

        session.setOverallScore(BigDecimal.ZERO);
        session.setStatus("REPORTED");
        sessionMapper.updateById(session);

        log.info("0分无效报告已生成，sessionId={}, 有效回答数={}", sessionId, answerCount);
        return vo;
    }

    private ReportVO.RadarDimension dim0(String name) {
        ReportVO.RadarDimension d = new ReportVO.RadarDimension();
        d.setDimension(name);
        d.setScore(BigDecimal.ZERO);
        return d;
    }
}
