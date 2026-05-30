package com.zzu.kaoyan.module.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.interview.entity.InterviewRecord;
import com.zzu.kaoyan.module.interview.entity.InterviewSession;
import com.zzu.kaoyan.module.interview.mapper.InterviewRecordMapper;
import com.zzu.kaoyan.module.interview.mapper.InterviewSessionMapper;
import com.zzu.kaoyan.module.interview.service.InterviewAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * AI 模拟面试官 - Mock 实现（用于开发/测试阶段，不消耗 API 费用）
 * <p>
 * 当 application.properties 中设置 interview.ai.mock=true 时，
 * 此实现会替代真实的 InterviewAiServiceImpl 注入到 Spring 容器中。
 * <p>
 * 启动方式：在 application.properties 中添加 interview.ai.mock=true
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "interview.ai.mock", havingValue = "true")
public class InterviewAiServiceMockImpl implements InterviewAiService {

    private final InterviewSessionMapper sessionMapper;
    private final InterviewRecordMapper recordMapper;

    /** 模拟追问计数器 */
    private static int mockQuestionIndex = 0;

    /** 模拟追问列表（循环使用）*/
    private static final String[] MOCK_QUESTIONS = {
            "请简单介绍一下你自己，包括本科期间的学习经历。",
            "你为什么选择报考这个专业？你对这个方向有什么了解？",
            "在本科期间，你做过哪些科研项目或实践？请详细描述其中一个。",
            "你读过哪些本专业的前沿论文？请谈谈你对其中一篇的理解。",
            "如果你被录取，你计划在研究生阶段研究什么方向？",
            "你觉得自己的优势和劣势分别是什么？",
            "请用英文回答：What are your career goals after graduation?",
            "如果在实验中遇到设备和预期不符的情况，你会怎么处理？",
            "你如何看待团队协作中的分歧？请结合你的经历谈谈。",
            "最后一个问题：你还有什么想对我们面试组说的吗？"
    };

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewRecord generateNextQuestion(Long sessionId, String userLatestAnswer,
                                                 Double speechDuration, java.util.Map<String, Object> demeanor) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new com.zzu.kaoyan.common.exception.BusinessException(404, "面试会话不存在");
        }
        if (!"IN_PROGRESS".equals(session.getStatus())) {
            throw new com.zzu.kaoyan.common.exception.BusinessException(400, "该面试会话已结束，无法继续提问");
        }

        // 保存用户回答
        InterviewRecord userRecord = new InterviewRecord();
        userRecord.setSessionId(sessionId);
        userRecord.setRole("user");
        userRecord.setContent(userLatestAnswer);
        userRecord.setFluencyScore(speechDuration != null && speechDuration > 0
                ? java.math.BigDecimal.valueOf(80) : null);
        userRecord.setCreatedAt(LocalDateTime.now());
        recordMapper.insert(userRecord);

        // 统计当前已有问答轮次
        long count = recordMapper.selectCount(
                new LambdaQueryWrapper<InterviewRecord>()
                        .eq(InterviewRecord::getSessionId, sessionId)
                        .eq(InterviewRecord::getRole, "ai")
        );

        // 从模拟问题列表中选取
        String aiQuestion = MOCK_QUESTIONS[(int) (count % MOCK_QUESTIONS.length)];
        log.info("[Mock模式] AI追问(轮次{}): {}", count + 1, aiQuestion);

        // 保存 AI 追问
        InterviewRecord aiRecord = new InterviewRecord();
        aiRecord.setSessionId(sessionId);
        aiRecord.setRole("ai");
        aiRecord.setContent(aiQuestion);
        aiRecord.setCreatedAt(LocalDateTime.now());
        recordMapper.insert(aiRecord);

        return aiRecord;
    }
}
