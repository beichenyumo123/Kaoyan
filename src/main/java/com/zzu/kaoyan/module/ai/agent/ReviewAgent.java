package com.zzu.kaoyan.module.ai.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.activity.entity.po.CheckInPO;
import com.zzu.kaoyan.module.activity.mapper.CheckInMapper;
import com.zzu.kaoyan.module.ai.entity.AiDailyTask;
import com.zzu.kaoyan.module.ai.mapper.AiDailyTaskMapper;
import com.zzu.kaoyan.module.ai.service.AiAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

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

    public ReviewAgent(AiAgentService aiAgentService, AiDailyTaskMapper taskMapper,
                       CheckInMapper checkInMapper) {
        this.aiAgentService = aiAgentService;
        this.taskMapper = taskMapper;
        this.checkInMapper = checkInMapper;
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

            String userMessage = String.format(
                    "本周数据：打卡%d天，总学习%d小时，AI任务%d项完成%d项（完成率%.0f%%）。",
                    checkInDays, totalStudyHours, totalTasks, completedTasks, taskRate * 100);

            log.info("ReviewAgent 上下文 — userId={}, {}", userId, userMessage);

            String report = aiAgentService.chat(SYSTEM_PROMPT, userMessage);
            log.info("ReviewAgent LLM 返回长度={}", report.length());

            return report;
        } catch (Exception e) {
            log.error("ReviewAgent 生成周报失败 — userId={}", userId, e);
            return "【周报生成失败】" + e.getMessage();
        }
    }
}
