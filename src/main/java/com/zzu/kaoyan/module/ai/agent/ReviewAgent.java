package com.zzu.kaoyan.module.ai.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.activity.entity.po.CheckInPO;
import com.zzu.kaoyan.module.activity.mapper.CheckInMapper;
import com.zzu.kaoyan.module.ai.entity.AiDailyTask;
import com.zzu.kaoyan.module.ai.entity.AiInterventionLog;
import com.zzu.kaoyan.module.ai.entity.AiReport;
import com.zzu.kaoyan.module.ai.mapper.AiDailyTaskMapper;
import com.zzu.kaoyan.module.ai.mapper.AiInterventionLogMapper;
import com.zzu.kaoyan.module.ai.mapper.AiReportMapper;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
import com.zzu.kaoyan.module.ai.service.MemoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 复盘 Agent — 聚合用户近 7 天学习数据生成 Markdown 周报。
 */
@Component
public class ReviewAgent {

    private static final Logger log = LoggerFactory.getLogger(ReviewAgent.class);

    private static final String SYSTEM_PROMPT =
            """
            你是一位考研复盘AI导师。
            请根据用户本周的学习数据（打卡天数、学习时间、任务完成率），为其生成一份格式精美、
            多级标题、带有"高光时刻"、"薄弱点警示"和"下周复习大纲建议"的 Markdown 格式周学情透视报告。
            语气要专业、具有高度针对性和人文关怀。
            直接输出 Markdown 文本，不要带任何前缀或解释。
            """;

    private final AiAgentService aiAgentService;
    private final AiDailyTaskMapper taskMapper;
    private final CheckInMapper checkInMapper;
    private final AiInterventionLogMapper interventionMapper;
    private final AiReportMapper reportMapper;
    private final MemoryService memoryService;

    public ReviewAgent(AiAgentService aiAgentService, AiDailyTaskMapper taskMapper,
                       CheckInMapper checkInMapper, AiInterventionLogMapper interventionMapper,
                       AiReportMapper reportMapper, MemoryService memoryService) {
        this.aiAgentService = aiAgentService;
        this.taskMapper = taskMapper;
        this.checkInMapper = checkInMapper;
        this.interventionMapper = interventionMapper;
        this.reportMapper = reportMapper;
        this.memoryService = memoryService;
    }

    public String generateReport(Long userId) {
        log.info("ReviewAgent 开始生成周报 — userId={}", userId);

        try {
            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

            List<AiDailyTask> tasks = taskMapper.selectList(
                    new LambdaQueryWrapper<AiDailyTask>()
                            .eq(AiDailyTask::getUserId, userId)
                            .ge(AiDailyTask::getTaskDate, sevenDaysAgo));

            List<CheckInPO> checkIns = checkInMapper.selectList(
                    new LambdaQueryWrapper<CheckInPO>()
                            .eq(CheckInPO::getUserId, userId)
                            .ge(CheckInPO::getCreatedDate, sevenDaysAgo));

            int totalTasks = tasks.size();
            long completedTasks = tasks.stream()
                    .filter(t -> t.getStatus() != null && t.getStatus() == 1).count();
            double taskRate = totalTasks == 0 ? 0 : (double) completedTasks / totalTasks;

            int checkInDays = checkIns.size();
            int totalStudyHours = checkIns.stream().mapToInt(c ->
                    c.getStudyHours() != null ? c.getStudyHours() : 0).sum();

            // 注入学员档案，让周报关联薄弱知识点和长期学习轨迹
            String memory = memoryService.buildContext(userId);
            String enrichedUserMessage = String.format(
                    "本周数据：打卡%d天，总学习%d小时，AI任务%d项完成%d项（完成率%.0f%%）。",
                    checkInDays, totalStudyHours, totalTasks, completedTasks, taskRate * 100);
            if (memory != null && !memory.isBlank()) {
                enrichedUserMessage += "\n\n学员完整档案（包含薄弱知识点、学习阶段、心理状态）：\n" + memory;
                enrichedUserMessage += "\n请在周报中结合薄弱知识点给出针对性建议，关联长期学习轨迹分析趋势。";
            }

            log.info("ReviewAgent 上下文 — userId={}, {}", userId, enrichedUserMessage);

            String report = aiAgentService.chat(SYSTEM_PROMPT, enrichedUserMessage);
            log.info("ReviewAgent LLM 返回长度={}", report.length());

            // 持久化周报
            saveReport(userId, report);

            return report;
        } catch (Exception e) {
            log.error("ReviewAgent 生成周报失败 — userId={}", userId, e);
            return "【周报生成失败】" + e.getMessage();
        }
    }

    /**
     * 定时任务：每周日 20:00 自动生成周报并写入干预日志。
     */
    @Scheduled(cron = "0 0 20 * * SUN")
    public void scheduledWeeklyReport() {
        log.info("ReviewAgent 定时周报生成启动");
        try {
            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);

            // 收集近 7 天有任务或打卡的用户 ID
            Set<Long> activeUserIds = new HashSet<>();

            List<AiDailyTask> recentTasks = taskMapper.selectList(
                    new LambdaQueryWrapper<AiDailyTask>().ge(AiDailyTask::getTaskDate, sevenDaysAgo));
            recentTasks.forEach(t -> activeUserIds.add(t.getUserId()));

            List<CheckInPO> recentCheckIns = checkInMapper.selectList(
                    new LambdaQueryWrapper<CheckInPO>().ge(CheckInPO::getCreatedDate, sevenDaysAgo));
            recentCheckIns.forEach(c -> activeUserIds.add(c.getUserId()));

            log.info("ReviewAgent 定时周报 — 活跃用户数={}", activeUserIds.size());

            int generated = 0;
            for (Long userId : activeUserIds) {
                try {
                    String report = generateReport(userId);
                    if (report != null && !report.startsWith("【周报生成失败")) {
                        AiInterventionLog logEntity = new AiInterventionLog();
                        logEntity.setUserId(userId);
                        logEntity.setAgentName("Review");
                        logEntity.setTriggerReason("每周自动生成");
                        logEntity.setInterventionContent(report);
                        logEntity.setUserReaction("UNREAD");
                        logEntity.setCreatedAt(LocalDateTime.now());
                        interventionMapper.insert(logEntity);
                        generated++;
                    }
                } catch (Exception e) {
                    log.error("ReviewAgent 定时周报失败 — userId={}", userId, e);
                }
            }

            log.info("ReviewAgent 定时周报完成 — 生成数={}", generated);
        } catch (Exception e) {
            log.error("ReviewAgent 定时周报执行异常", e);
        }
    }

    /**
     * 持久化周报到 ai_report 表（幂等：同一用户同一周只存一份）
     */
    private void saveReport(Long userId, String markdown) {
        try {
            LocalDate today = LocalDate.now();
            // 计算本周一和本周日
            LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate weekEnd = weekStart.plusDays(6);

            // 检查是否已存在
            Long count = reportMapper.selectCount(
                    new LambdaQueryWrapper<AiReport>()
                            .eq(AiReport::getUserId, userId)
                            .eq(AiReport::getWeekStart, weekStart));
            if (count > 0) {
                log.info("ReviewAgent 周报已存在，跳过持久化 — userId={}, weekStart={}", userId, weekStart);
                return;
            }

            AiReport report = new AiReport();
            report.setUserId(userId);
            report.setWeekStart(weekStart);
            report.setWeekEnd(weekEnd);
            report.setMarkdown(markdown);
            report.setCreatedAt(LocalDateTime.now());
            reportMapper.insert(report);
            log.info("ReviewAgent 周报持久化成功 — userId={}, weekStart={}", userId, weekStart);
        } catch (Exception e) {
            log.error("ReviewAgent 周报持久化失败 — userId={}", userId, e);
        }
    }
}
