package com.zzu.kaoyan.module.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.ai.agent.ReviewAgent;
import com.zzu.kaoyan.module.ai.agent.SupervisorAgent;
import com.zzu.kaoyan.module.ai.entity.AiDailyTask;
import com.zzu.kaoyan.module.ai.entity.AiInterventionLog;
import com.zzu.kaoyan.module.ai.event.TaskCompletedEvent;
import com.zzu.kaoyan.module.ai.mapper.AiDailyTaskMapper;
import com.zzu.kaoyan.module.ai.mapper.AiInterventionLogMapper;
import com.zzu.kaoyan.module.ai.vo.AiTaskVO;
import com.zzu.kaoyan.module.ai.vo.InterventionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI 多智能体学习伴侣", description = "任务、干预日志、周报与控制面板")
public class AiAgentController {

    private static final Logger log = LoggerFactory.getLogger(AiAgentController.class);

    private final AiDailyTaskMapper taskMapper;
    private final AiInterventionLogMapper interventionMapper;
    private final ReviewAgent reviewAgent;
    private final SupervisorAgent supervisorAgent;
    private final ApplicationEventPublisher eventPublisher;

    public AiAgentController(AiDailyTaskMapper taskMapper,
                             AiInterventionLogMapper interventionMapper,
                             ReviewAgent reviewAgent,
                             SupervisorAgent supervisorAgent,
                             ApplicationEventPublisher eventPublisher) {
        this.taskMapper = taskMapper;
        this.interventionMapper = interventionMapper;
        this.reviewAgent = reviewAgent;
        this.supervisorAgent = supervisorAgent;
        this.eventPublisher = eventPublisher;
    }

    // ==================== 1. AI 任务 ====================

    @Operation(summary = "获取今日 AI 规划任务")
    @GetMapping("/tasks")
    @SaCheckLogin
    public Result<List<AiTaskVO>> getTodayTasks() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<AiDailyTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<AiDailyTask>()
                        .eq(AiDailyTask::getUserId, userId)
                        .eq(AiDailyTask::getTaskDate, LocalDate.now())
                        .orderByAsc(AiDailyTask::getCreatedAt));
        List<AiTaskVO> voList = tasks.stream().map(t -> {
            AiTaskVO vo = new AiTaskVO();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.success(voList);
    }

    @Operation(summary = "完成某条 AI 任务")
    @PostMapping("/tasks/{taskId}/complete")
    @SaCheckLogin
    public Result<Void> completeTask(@PathVariable Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();
        AiDailyTask task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            return Result.error(404, "任务不存在");
        }
        if (task.getStatus() != null && task.getStatus() == 1) {
            return Result.error(400, "任务已完成，无需重复操作");
        }
        task.setStatus(1);
        taskMapper.updateById(task);

        eventPublisher.publishEvent(new TaskCompletedEvent(
                userId, taskId, task.getTaskContent()));
        log.info("任务完成 — userId={}, taskId={}", userId, taskId);
        return Result.success();
    }

    // ==================== 2. 干预日志 ====================

    @Operation(summary = "获取未读干预日志")
    @GetMapping("/interventions")
    @SaCheckLogin
    public Result<List<InterventionVO>> getUnreadInterventions() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<AiInterventionLog> logs = interventionMapper.selectList(
                new LambdaQueryWrapper<AiInterventionLog>()
                        .eq(AiInterventionLog::getUserId, userId)
                        .eq(AiInterventionLog::getUserReaction, "UNREAD")
                        .orderByDesc(AiInterventionLog::getCreatedAt));
        List<InterventionVO> voList = logs.stream().map(l -> {
            InterventionVO vo = new InterventionVO();
            BeanUtils.copyProperties(l, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.success(voList);
    }

    @Operation(summary = "标记干预日志已读")
    @PutMapping("/interventions/{id}/read")
    @SaCheckLogin
    public Result<Void> markInterventionRead(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        AiInterventionLog logEntity = interventionMapper.selectById(id);
        if (logEntity == null || !logEntity.getUserId().equals(userId)) {
            return Result.error(404, "干预日志不存在");
        }
        logEntity.setUserReaction("READ");
        interventionMapper.updateById(logEntity);
        return Result.success();
    }

    // ==================== 3. 周报 ====================

    @Operation(summary = "获取本周 AI 学情透视周报")
    @GetMapping("/report")
    @SaCheckLogin
    public Result<Map<String, String>> getWeeklyReport() {
        Long userId = StpUtil.getLoginIdAsLong();
        String markdown = reviewAgent.generateReport(userId);
        Map<String, String> data = new HashMap<>();
        data.put("markdown", markdown);
        return Result.success(data);
    }

    // ==================== 4. 监督 Agent 手动触发（路演用） ====================

    @Operation(summary = "手动触发监督 Agent（路演演示）")
    @PostMapping("/agent/supervisor/trigger")
    @SaCheckLogin
    public Result<String> triggerSupervisor() {
        log.info("手动触发 SupervisorAgent — operator={}", StpUtil.getLoginIdAsLong());
        supervisorAgent.triggerManually();
        return Result.success("监督 Agent 扫描已触发");
    }
}
