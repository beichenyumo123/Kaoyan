package com.zzu.kaoyan.module.ai.agent;

import com.zzu.kaoyan.module.ai.entity.AiInterventionLog;
import com.zzu.kaoyan.module.ai.mapper.AiInterventionLogMapper;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 心理 Agent — 分析打卡感言中的情绪并生成治愈系寄语。
 */
@Component
public class PsychologyAgent {

    private static final Logger log = LoggerFactory.getLogger(PsychologyAgent.class);

    private static final String SYSTEM_PROMPT =
            """
            你是一位富有同理心的心理辅导老师。
            如果发现考生的言语中带有焦虑、绝望、疲惫等负面情绪，请生成一段 100 字以内极其温暖、
            具有针对性复习建议的安抚寄语。
            如果情感积极，则生成一段简短的肯定和赞美（50 字以内）。
            直接输出寄语文本，不要带任何前缀或引号。
            """;

    private final AiAgentService aiAgentService;
    private final AiInterventionLogMapper interventionMapper;

    public PsychologyAgent(AiAgentService aiAgentService, AiInterventionLogMapper interventionMapper) {
        this.aiAgentService = aiAgentService;
        this.interventionMapper = interventionMapper;
    }

    public void analyzeAndIntervene(Long userId, String notes, String triggerReason) {
        log.info("PsychologyAgent 开始分析 — userId={}, notes={}", userId, notes);

        String response = aiAgentService.chat(SYSTEM_PROMPT, notes);
        log.info("PsychologyAgent LLM 返回 — userId={}, response={}", userId, response);

        if (response == null || response.isBlank()) {
            log.warn("PsychologyAgent 未生成寄语 — userId={}", userId);
            return;
        }

        AiInterventionLog logEntity = new AiInterventionLog();
        logEntity.setUserId(userId);
        logEntity.setAgentName("Psychology");
        logEntity.setTriggerReason(triggerReason);
        logEntity.setInterventionContent(response.trim());
        logEntity.setUserReaction("UNREAD");
        logEntity.setCreatedAt(LocalDateTime.now());
        interventionMapper.insert(logEntity);

        log.info("PsychologyAgent 已写入干预日志 — userId={}", userId);
    }
}
