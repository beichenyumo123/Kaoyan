package com.zzu.kaoyan.module.ai.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.activity.mapper.CheckInMapper;
import com.zzu.kaoyan.module.ai.entity.AiDailyTask;
import com.zzu.kaoyan.module.ai.entity.AiInterventionLog;
import com.zzu.kaoyan.module.ai.mapper.AiDailyTaskMapper;
import com.zzu.kaoyan.module.ai.mapper.AiInterventionLogMapper;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
import com.zzu.kaoyan.module.ai.service.MemoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 监督 Agent — 每天 21:00 自动检查进度落后用户，生成严厉催促警示。
 */
@Component
public class SupervisorAgent {

    private static final Logger log = LoggerFactory.getLogger(SupervisorAgent.class);

    private static final String SYSTEM_PROMPT =
            """
            你是一位严厉、极具威严的 408 考研面试组长。
            如果发现学生近期进度严重落后或懈怠，请生成一句 60 字以内的严厉催学警示，
            一针见血，直击痛点，促使其警醒。
            直接输出警示文本，不要带任何前缀或引号。
            """;

    private final AiAgentService aiAgentService;
    private final AiDailyTaskMapper taskMapper;
    private final AiInterventionLogMapper interventionMapper;
    private final MemoryService memoryService;

    public SupervisorAgent(AiAgentService aiAgentService, AiDailyTaskMapper taskMapper,
                           AiInterventionLogMapper interventionMapper, MemoryService memoryService) {
        this.aiAgentService = aiAgentService;
        this.taskMapper = taskMapper;
        this.interventionMapper = interventionMapper;
        this.memoryService = memoryService;
    }

    /**
     * 定时任务：每晚 21:00 自动扫描进度落后用户。
     */
    @Scheduled(cron = "0 0 21 * * ?")
    public void scheduledScan() {
        log.info("SupervisorAgent 定时扫描启动");
        scanAndWarn();
    }

    /**
     * 手动触发（路演演示用）。
     */
    public void triggerManually() {
        log.info("SupervisorAgent 手动触发");
        scanAndWarn();
    }

    private void scanAndWarn() {
        try {
            LocalDate threeDaysAgo = LocalDate.now().minusDays(3);

            List<AiDailyTask> recentTasks = taskMapper.selectList(
                    new LambdaQueryWrapper<AiDailyTask>()
                            .ge(AiDailyTask::getTaskDate, threeDaysAgo));

            if (recentTasks.isEmpty()) {
                log.info("SupervisorAgent 无近期任务，跳过");
                return;
            }

            // 按 userId 分组计算完成率
            Map<Long, List<AiDailyTask>> grouped = recentTasks.stream()
                    .collect(Collectors.groupingBy(AiDailyTask::getUserId));

            int warned = 0;
            for (Map.Entry<Long, List<AiDailyTask>> entry : grouped.entrySet()) {
                Long userId = entry.getKey();
                List<AiDailyTask> userTasks = entry.getValue();
                long completed = userTasks.stream().filter(t -> t.getStatus() != null && t.getStatus() == 1).count();
                double rate = (double) completed / userTasks.size();

                if (rate >= 0.5) continue;

                log.info("SupervisorAgent 命中用户 — userId={}, 完成率={}%, 总任务={}",
                        userId, Math.round(rate * 100), userTasks.size());

                // 构建个性化系统提示（注入学员档案）
                String personalizedPrompt = SYSTEM_PROMPT;
                String memory = memoryService.buildContext(userId);
                if (memory != null && !memory.isBlank()) {
                    personalizedPrompt = SYSTEM_PROMPT + "\n\n该学生的背景信息：\n" + memory
                            + "\n请结合学生背景，给出更有针对性的警示，直击其薄弱学科和懈怠原因。";
                }

                String warning = aiAgentService.chat(personalizedPrompt,
                        String.format("该考生近3天 %d 项任务仅完成 %d 项，完成率 %.0f%%。",
                                userTasks.size(), completed, rate * 100));

                AiInterventionLog entry2 = new AiInterventionLog();
                entry2.setUserId(userId);
                entry2.setAgentName("Supervisor");
                entry2.setTriggerReason(String.format("近3天任务完成率%.0f%%（%d/%d）",
                        rate * 100, completed, userTasks.size()));
                entry2.setInterventionContent(warning.trim());
                entry2.setUserReaction("UNREAD");
                entry2.setCreatedAt(LocalDateTime.now());
                interventionMapper.insert(entry2);
                warned++;
            }

            log.info("SupervisorAgent 扫描完成 — 警告用户数={}", warned);
        } catch (Exception e) {
            log.error("SupervisorAgent 执行失败", e);
        }
    }
}
