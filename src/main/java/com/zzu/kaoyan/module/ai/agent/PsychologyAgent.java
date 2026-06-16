package com.zzu.kaoyan.module.ai.agent;

import com.zzu.kaoyan.module.ai.entity.AiInterventionLog;
import com.zzu.kaoyan.module.ai.mapper.AiInterventionLogMapper;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
import com.zzu.kaoyan.module.ai.service.MemoryService;
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
            你是一位富有同理心的心理辅导老师，代号「心理树洞」。
            请根据学生的打卡感言或日记内容，分析其情绪状态并生成关怀内容。

            必须且只能输出如下合法的 JSON 格式，不要包含任何 markdown 标签或多余解释：
            {"message":"安抚寄语（100字以内）","detailMarkdown":"## 🎯 情绪分析\\n\\n- 当前情绪：...\\n- 情绪趋势：...\\n\\n## 💆 放松建议\\n\\n1. **建议1** — 说明\\n2. **建议2** — 说明\\n\\n> 适当休息也是备考的一部分"}

            其中：
            - message: 温暖的安抚寄语（如情感积极则为肯定赞美），50-150字
            - detailMarkdown: 情绪分析 + 放松建议的 Markdown 详情，至少包含情绪标签和 2 条建议
            """;

    private final AiAgentService aiAgentService;
    private final AiInterventionLogMapper interventionMapper;
    private final UserAiProfileService profileService;
    private final MemoryService memoryService;

    public PsychologyAgent(AiAgentService aiAgentService, AiInterventionLogMapper interventionMapper,
                           UserAiProfileService profileService, MemoryService memoryService) {
        this.aiAgentService = aiAgentService;
        this.interventionMapper = interventionMapper;
        this.profileService = profileService;
        this.memoryService = memoryService;
    }

    public void analyzeAndIntervene(Long userId, String notes, String triggerReason) {
        log.info("PsychologyAgent 开始分析 — userId={}, notes={}", userId, notes);

        // 注入学员档案，让心理老师了解用户整体状态
        String memory = memoryService.buildContext(userId);
        String enrichedSystemPrompt = SYSTEM_PROMPT;
        if (memory != null && !memory.isBlank()) {
            enrichedSystemPrompt = SYSTEM_PROMPT + "\n\n该学生的近期学习档案：\n" + memory
                    + "\n请结合学习状态给出更有针对性的心理关怀。";
        }

        String response = aiAgentService.chat(enrichedSystemPrompt, notes);
        log.info("PsychologyAgent LLM 返回 — userId={}, response={}", userId, response);

        if (response == null || response.isBlank()) {
            log.warn("PsychologyAgent 未生成寄语 — userId={}", userId);
            return;
        }

        // 尝试解析 JSON 格式：{"message":"...", "detailMarkdown":"..."}
        String message;
        String detailMarkdown = null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, String> parsed = mapper.readValue(response.trim(), java.util.Map.class);
            message = parsed.getOrDefault("message", response.trim());
            detailMarkdown = parsed.getOrDefault("detailMarkdown", null);
        } catch (Exception e) {
            // JSON 解析失败，回退到纯文本模式
            message = response.trim();
            log.info("PsychologyAgent JSON 解析失败，使用纯文本模式 — userId={}", userId);
        }

        AiInterventionLog logEntity = new AiInterventionLog();
        logEntity.setUserId(userId);
        logEntity.setAgentName("心理树洞");
        logEntity.setTriggerReason(triggerReason);
        logEntity.setInterventionContent(message);
        logEntity.setDetailMarkdown(detailMarkdown);
        logEntity.setUserReaction("UNREAD");
        logEntity.setCreatedAt(LocalDateTime.now());
        interventionMapper.insert(logEntity);

        log.info("PsychologyAgent 已写入干预日志 — userId={}", userId);

        // 更新用户心理画像
        String emotionLabel = detectEmotionLabel(notes);
        profileService.updatePsychologicalProfile(userId, emotionLabel, message);
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
