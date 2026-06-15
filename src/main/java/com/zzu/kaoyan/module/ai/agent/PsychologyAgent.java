package com.zzu.kaoyan.module.ai.agent;

import com.zzu.kaoyan.module.ai.entity.AiInterventionLog;
import com.zzu.kaoyan.module.ai.mapper.AiInterventionLogMapper;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
import com.zzu.kaoyan.module.ai.service.UserAiProfileService;
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
    private final UserAiProfileService profileService;

    public PsychologyAgent(AiAgentService aiAgentService, AiInterventionLogMapper interventionMapper,
                           UserAiProfileService profileService) {
        this.aiAgentService = aiAgentService;
        this.interventionMapper = interventionMapper;
        this.profileService = profileService;
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

        // 更新用户心理画像
        String emotionLabel = detectEmotionLabel(notes);
        profileService.updatePsychologicalProfile(userId, emotionLabel, response.trim());
    }

    /**
     * 简单的情绪标签检测（基于关键词匹配）。
     */
    private String detectEmotionLabel(String notes) {
        if (notes == null) return "未知";
        String lower = notes.toLowerCase();
        if (lower.contains("焦虑") || lower.contains("紧张") || lower.contains("担心")) return "焦虑";
        if (lower.contains("绝望") || lower.contains("崩溃") || lower.contains("放弃")) return "绝望";
        if (lower.contains("疲惫") || lower.contains("累") || lower.contains("困")) return "疲惫";
        if (lower.contains("开心") || lower.contains("进步") || lower.contains("满意")) return "积极";
        if (lower.contains("迷茫") || lower.contains("困惑") || lower.contains("不知道")) return "迷茫";
        return "一般";
    }
}
