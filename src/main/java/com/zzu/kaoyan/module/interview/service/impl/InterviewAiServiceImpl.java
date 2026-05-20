package com.zzu.kaoyan.module.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.module.interview.config.DeepSeekConfig;
import com.zzu.kaoyan.module.interview.entity.InterviewRecord;
import com.zzu.kaoyan.module.interview.entity.InterviewSession;
import com.zzu.kaoyan.module.interview.entity.dto.DeepSeekRequest;
import com.zzu.kaoyan.module.interview.entity.dto.DeepSeekResponse;
import com.zzu.kaoyan.module.interview.mapper.InterviewRecordMapper;
import com.zzu.kaoyan.module.interview.mapper.InterviewSessionMapper;
import com.zzu.kaoyan.module.interview.service.InterviewAiService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 模拟面试官 - 核心业务实现类
 * <p>
 * 核心流程：
 * 1. 保存用户回答 → 2. 查询历史记录 → 3. 构建 Prompt → 4. 调用 DeepSeek → 5. 保存 AI 回复并返回
 * <p>
 * 当 interview.ai.mock=true 时，此 Bean 不会加载，由 Mock 实现替代
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "interview.ai.mock", havingValue = "false", matchIfMissing = true)
public class InterviewAiServiceImpl implements InterviewAiService {

    private final InterviewSessionMapper sessionMapper;
    private final InterviewRecordMapper recordMapper;
    private final RestTemplate deepSeekRestTemplate;

    // ============================================================
    // 面试类型 → System Prompt 映射
    // ============================================================

    /**
     * 英文面试 - 系统人设提示词
     */
    private static final String SYSTEM_PROMPT_ENGLISH = """
            You are a professional English interview examiner for Chinese postgraduate entrance \
            re-examination (考研复试). Your responsibilities are:
            1. Conduct the entire interview in English, speaking at a moderate pace.
            2. Ask questions covering: self-introduction, motivation for postgraduate study, \
            understanding of the target major, research interests, and academic background.
            3. Provide encouraging feedback when the candidate answers well, and ask follow-up \
            questions when the answer is too brief.
            4. Keep each question concise and focused — ask only ONE question at a time.
            5. If the candidate struggles with English expression, offer a gentle hint but \
            continue in English.
            6. At the end of the session, give a brief English summary of the candidate's performance.

            Please begin by greeting the candidate and asking the first question.""";

    /**
     * 专业课面试 - 系统人设提示词
     */
    private static final String SYSTEM_PROMPT_MAJOR = """
            你是一位经验丰富的考研复试专业课面试考官，你的职责如下：
            1. 围绕考生所报考的专业方向，提出有深度、有区分度的专业问题。
            2. 问题应涵盖：专业核心概念、领域前沿热点、本科阶段的核心课程知识、科研实践经历等。
            3. 当考生回答得不够深入时，要适时追问、引导其展开论述。
            4. 每次只提一个问题，保持问题的清晰和聚焦，不要一次性抛出多个问题。
            5. 在面试过程中适度给予鼓励，保持专业但不失亲切的面试氛围。
            6. 面试结束时，对考生的专业素养做出简要的口头评价。

            请从问候考生并询问其报考专业方向开始面试。""";

    /**
     * 综合面试 - 系统人设提示词
     */
    private static final String SYSTEM_PROMPT_COMPREHENSIVE = """
            你是一位考研复试综合面试考官，负责全面评估考生的综合素质，你的职责如下：
            1. 综合考察考生的逻辑思维、语言表达、应变能力、心理素质、时政素养等方面。
            2. 问题类型多样化，可包括：自我介绍引导、报考动机询问、研究生阶段规划、\
            团队协作情景题、社会热点分析、抗压能力测试等。
            3. 根据考生的回答灵活调整追问方向，做到因人而异、因材施问。
            4. 每次只提一个问题，语言表达自然、像真实考官一样对话，不要机械化。
            5. 面试结束时，对考生的综合表现做简短的口头总结。

            请从问候考生并请考生做简短自我介绍开始面试。""";

    // ============================================================
    // 核心方法
    // ============================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewRecord generateNextQuestion(Long sessionId, String userLatestAnswer) {
        // ---------- Step 1：校验会话是否存在 ----------
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "面试会话不存在");
        }
        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该面试会话已结束，无法继续提问");
        }

        // ---------- Step 2：保存用户的最新回答到对话记录表 ----------
        InterviewRecord userRecord = new InterviewRecord();
        userRecord.setSessionId(sessionId);
        userRecord.setRole("user");
        userRecord.setContent(userLatestAnswer);
        userRecord.setFluencyScore(null); // TODO：语音流利度评分暂不实现，后续接入语音识别后填充
        userRecord.setCreatedAt(LocalDateTime.now());
        recordMapper.insert(userRecord);
        log.info("已保存用户回答记录，recordId={}, sessionId={}", userRecord.getId(), sessionId);

        // ---------- Step 3：查询该会话的全部历史对话记录（按时间正序）----------
        List<InterviewRecord> historyRecords = recordMapper.selectList(
                new LambdaQueryWrapper<InterviewRecord>()
                        .eq(InterviewRecord::getSessionId, sessionId)
                        .orderByAsc(InterviewRecord::getCreatedAt)
        );

        // ---------- Step 4：构建发送给 DeepSeek 的 Message 列表 ----------
        List<DeepSeekRequest.Message> messages = new ArrayList<>();

        // 4a. 插入 System Prompt（根据面试类型选择不同的人设）
        String systemPrompt = getSystemPrompt(session.getInterviewType());
        messages.add(DeepSeekRequest.Message.builder()
                .role("system")
                .content(systemPrompt)
                .build());

        // 4b. 追加历史对话记录（user <-> ai 交替）
        for (InterviewRecord record : historyRecords) {
            String role = mapRole(record.getRole());
            messages.add(DeepSeekRequest.Message.builder()
                    .role(role)
                    .content(record.getContent())
                    .build());
        }

        // ---------- Step 5：调用 DeepSeek Chat API ----------
        DeepSeekRequest requestBody = DeepSeekRequest.builder()
                .model(DeepSeekConfig.MODEL)
                .messages(messages)
                .temperature(DeepSeekConfig.TEMPERATURE)
                .maxTokens(DeepSeekConfig.MAX_TOKENS)
                .build();

        String aiResponseContent = callDeepSeekApi(requestBody);
        log.info("DeepSeek API 返回成功，内容长度={}", aiResponseContent != null ? aiResponseContent.length() : 0);

        // ---------- Step 6：将 AI 追问保存到对话记录表 ----------
        InterviewRecord aiRecord = new InterviewRecord();
        aiRecord.setSessionId(sessionId);
        aiRecord.setRole("ai");
        aiRecord.setContent(aiResponseContent);
        aiRecord.setFluencyScore(null); // AI 消息不需要语音流利度得分
        aiRecord.setCreatedAt(LocalDateTime.now());
        recordMapper.insert(aiRecord);
        log.info("已保存AI追问记录，recordId={}, sessionId={}", aiRecord.getId(), sessionId);

        return aiRecord;
    }

    // ============================================================
    // 私有辅助方法
    // ============================================================

    /**
     * 根据面试类型获取对应的 System Prompt
     */
    private String getSystemPrompt(String interviewType) {
        if (interviewType == null) {
            return SYSTEM_PROMPT_COMPREHENSIVE; // 默认使用综合面试
        }
        return switch (interviewType.toUpperCase()) {
            case "ENGLISH"  -> SYSTEM_PROMPT_ENGLISH;
            case "MAJOR"    -> SYSTEM_PROMPT_MAJOR;
            default         -> SYSTEM_PROMPT_COMPREHENSIVE;
        };
    }

    /**
     * 将数据库中的角色映射为 DeepSeek API 期望的角色名
     * interview_record.role: "user" → "user", "ai" → "assistant"
     */
    private String mapRole(String dbRole) {
        if ("ai".equalsIgnoreCase(dbRole)) {
            return "assistant";
        }
        return "user";
    }

    /**
     * 调用 DeepSeek Chat API（阻塞式，非流式）
     *
     * @param requestBody 请求体
     * @return AI 回复的文本内容
     */
    private String callDeepSeekApi(DeepSeekRequest requestBody) {
        // 构造 HTTP 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(DeepSeekConfig.API_KEY);

        HttpEntity<DeepSeekRequest> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            // ============================================================
            // TODO：【重要】此处调用 DeepSeek API
            // DeepSeekConfig.API_URL 当前为占位值，接入前请填写真实地址
            // ============================================================
            ResponseEntity<DeepSeekResponse> response = deepSeekRestTemplate.postForEntity(
                    DeepSeekConfig.API_URL,
                    httpEntity,
                    DeepSeekResponse.class
            );

            DeepSeekResponse body = response.getBody();
            if (body == null || body.getFirstContent() == null) {
                log.error("DeepSeek API 返回空响应，response={}", response);
                throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "AI 服务返回空响应，请稍后重试");
            }

            return body.getFirstContent();
        } catch (BusinessException e) {
            throw e; // 业务异常直接抛出
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "AI 服务调用失败: " + e.getMessage());
        }
    }
}
