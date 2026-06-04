package com.zzu.kaoyan.module.ai.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.ai.entity.AiDailyTask;
import com.zzu.kaoyan.module.ai.entity.UserAiProfile;
import com.zzu.kaoyan.module.ai.mapper.AiDailyTaskMapper;
import com.zzu.kaoyan.module.ai.mapper.UserAiProfileMapper;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
import com.zzu.kaoyan.module.ai.service.UserAiProfileService;
import com.zzu.kaoyan.module.ai.util.JsonArrayExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 规划 Agent — 根据用户进度量身定制今日推荐任务。
 */
@Component
public class PlannerAgent {

    private static final Logger log = LoggerFactory.getLogger(PlannerAgent.class);

    private static final String SYSTEM_PROMPT =
            """
            你是一位考研规划专家。请根据该用户的当前进度，为他量身定制 3 条今日推荐任务。
            必须且只能输出如下合法的 JSON 数组格式，不要包含任何 markdown 标签或多余解释：
            [{"content":"任务描述","importance":"HIGH/MEDIUM/LOW","tips":"智能体叮嘱"}]
            """;

    private final AiAgentService aiAgentService;
    private final AiDailyTaskMapper taskMapper;
    private final UserAiProfileMapper profileMapper;
    private final UserAiProfileService profileService;

    public PlannerAgent(AiAgentService aiAgentService, AiDailyTaskMapper taskMapper,
                        UserAiProfileMapper profileMapper, UserAiProfileService profileService) {
        this.aiAgentService = aiAgentService;
        this.taskMapper = taskMapper;
        this.profileMapper = profileMapper;
        this.profileService = profileService;
    }

    public void planForUser(Long userId, int continuousDays, int totalCheckDays, int studyHours) {
        log.info("PlannerAgent 开始规划 — userId={}, continuousDays={}", userId, continuousDays);

        UserAiProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserAiProfile>().eq(UserAiProfile::getUserId, userId));
        String cognitive = profile != null && profile.getCognitiveProfile() != null
                ? profile.getCognitiveProfile() : "暂无学习记录";

        String userMessage = String.format(
                "用户连续打卡%d天，总计%d天，今日学习%d小时。学习档案：%s",
                continuousDays, totalCheckDays, studyHours, cognitive);

        String response = aiAgentService.chat(SYSTEM_PROMPT, userMessage);
        log.info("PlannerAgent LLM 返回 — userId={}, response={}", userId, response);

        List<JsonArrayExtractor.Task> tasks = JsonArrayExtractor.extract(response);
        if (tasks.isEmpty()) {
            log.warn("PlannerAgent 未解析出任务 — userId={}", userId);
            return;
        }

        LocalDate today = LocalDate.now();
        for (JsonArrayExtractor.Task t : tasks) {
            AiDailyTask entity = new AiDailyTask();
            entity.setUserId(userId);
            entity.setTaskDate(today);
            entity.setTaskContent(t.getContent());
            entity.setImportance(t.getImportance() != null ? t.getImportance() : "MEDIUM");
            entity.setStatus(0);
            entity.setAgentTips(t.getTips());
            entity.setCreatedAt(LocalDateTime.now());
            taskMapper.insert(entity);
        }

        log.info("PlannerAgent 写入 {} 条任务 — userId={}", tasks.size(), userId);

        // 更新用户认知画像
        profileService.updateCognitiveProfile(userId, continuousDays, totalCheckDays, studyHours);
    }
}
