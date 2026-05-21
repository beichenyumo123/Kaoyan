package com.zzu.kaoyan.module.ai.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.activity.mapper.CheckInMapper;
import com.zzu.kaoyan.module.ai.entity.AiDailyTask;
import com.zzu.kaoyan.module.ai.entity.AiInterventionLog;
import com.zzu.kaoyan.module.ai.mapper.AiDailyTaskMapper;
import com.zzu.kaoyan.module.ai.mapper.AiInterventionLogMapper;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
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

    public SupervisorAgent(AiAgentService aiAgentService, AiDailyTaskMapper taskMapper,
                           AiInterventionLogMapper interventionMapper) {
        this.aiAgentService = aiAgentService;
        this.taskMapper = taskMapper;
        this.interventionMapper = interventionMapper;
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

                log.info("SupervisorAgent 命中用户 — userId={}, 完成率={:.0%}, 总任务={}",
                        userId, rate, userTasks.size());

                String warning = aiAgentService.chat(SYSTEM_PROMPT,
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
