package com.zzu.kaoyan.module.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.annotation.MembershipRequired;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.ai.agent.ReviewAgent;
import com.zzu.kaoyan.module.ai.agent.SupervisorAgent;
import com.zzu.kaoyan.module.ai.agent.TutorAgent;
import com.zzu.kaoyan.module.ai.dto.AiAskDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.module.ai.dto.AiEventDTO;
import com.zzu.kaoyan.module.ai.entity.*;
import com.zzu.kaoyan.module.ai.event.TaskCompletedEvent;
import com.zzu.kaoyan.module.ai.mapper.*;
import com.zzu.kaoyan.module.ai.vo.AiSummaryVO;
import com.zzu.kaoyan.module.ai.vo.ChatMessageVO;
import com.zzu.kaoyan.module.ai.vo.ChatSessionVO;
import com.zzu.kaoyan.module.ai.vo.RecommendationVO;
import com.zzu.kaoyan.module.ai.vo.ReportHistoryVO;
import com.zzu.kaoyan.module.activity.entity.po.UserStudyPO;
import com.zzu.kaoyan.module.activity.mapper.UserStudyMapper;
import com.zzu.kaoyan.module.ai.vo.AiTaskVO;
import com.zzu.kaoyan.module.ai.vo.InterventionVO;
import com.zzu.kaoyan.module.membership.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI 多智能体学习伴侣", description = "任务、干预日志、周报与控制面板")
public class AiAgentController {

    private static final Logger log = LoggerFactory.getLogger(AiAgentController.class);

    private final AiDailyTaskMapper taskMapper;
    private final AiInterventionLogMapper interventionMapper;
    private final AiKnowledgePointMapper knowledgePointMapper;
    private final AiReportMapper reportMapper;
    private final AiUserEventMapper userEventMapper;
    private final AiChatSessionMapper chatSessionMapper;
    private final AiChatMessageMapper chatMessageMapper;
    private final UserAiProfileMapper userProfileMapper;
    private final UserStudyMapper userStudyMapper;
    private final ReviewAgent reviewAgent;
    private final SupervisorAgent supervisorAgent;
    private final TutorAgent tutorAgent;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final MembershipService membershipService;

    public AiAgentController(AiDailyTaskMapper taskMapper,
                             AiInterventionLogMapper interventionMapper,
                             AiKnowledgePointMapper knowledgePointMapper,
                             AiReportMapper reportMapper,
                             AiUserEventMapper userEventMapper,
                             AiChatSessionMapper chatSessionMapper,
                             AiChatMessageMapper chatMessageMapper,
                             UserAiProfileMapper userProfileMapper,
                             UserStudyMapper userStudyMapper,
                             ReviewAgent reviewAgent,
                             SupervisorAgent supervisorAgent,
                             TutorAgent tutorAgent,
                             ApplicationEventPublisher eventPublisher,
                             ObjectMapper objectMapper,
                             MembershipService membershipService) {
        this.taskMapper = taskMapper;
        this.interventionMapper = interventionMapper;
        this.knowledgePointMapper = knowledgePointMapper;
        this.reportMapper = reportMapper;
        this.userEventMapper = userEventMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.userProfileMapper = userProfileMapper;
        this.userStudyMapper = userStudyMapper;
        this.reviewAgent = reviewAgent;
        this.supervisorAgent = supervisorAgent;
        this.tutorAgent = tutorAgent;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.membershipService = membershipService;
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

    // ==================== 4. 答疑 Agent ====================

    @Operation(summary = "向答疑 Agent 提问（RAG 知识库增强）")
    @PostMapping("/ask")
    @SaCheckLogin
    @MembershipRequired("ai_ask")
    public Result<Map<String, String>> askQuestion(@RequestBody @Valid AiAskDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("用户提问 — userId={}, question={}", userId, dto.getQuestion());

        // 处理会话
        SessionInfo sessionInfo = ensureSession(userId, dto.getSessionId(), dto.getQuestion());
        saveMessage(sessionInfo.id, "user", dto.getQuestion(), dto.getImageUrl());

        String answer = tutorAgent.answer(dto.getQuestion(), dto.getSubject(), dto.getImageUrl(), userId);
        saveMessage(sessionInfo.id, "assistant", answer, null);

        Map<String, String> data = new HashMap<>();
        data.put("answer", answer);
        data.put("question", dto.getQuestion());
        data.put("sessionId", String.valueOf(sessionInfo.id));
        data.put("sessionTitle", sessionInfo.title);
        return Result.success(data);
    }

    @Operation(summary = "向答疑 Agent 流式提问（SSE）")
    @PostMapping(value = "/ask/stream", produces = "text/event-stream;charset=UTF-8")
    // 不使用 @SaCheckLogin：SSE 异步 dispatch 时 SaToken ThreadLocal 上下文已丢失，
    // 注解会在 async 回调时二次触发导致 SaTokenContextException
    public SseEmitter askQuestionStream(@RequestBody @Valid AiAskDTO dto) {
        // 超时 5 分钟（先创建 emitter，以便认证失败时也能通过 SSE 返回错误）
        SseEmitter emitter = new SseEmitter(300_000L);

        // 手动校验登录（在同步线程中完成，进入异步前拿到 userId）
        Long userId;
        try {
            StpUtil.checkLogin();
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 认证失败：通过 SSE 事件返回错误，而非抛异常（避免 Content-Type 冲突）
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"code\":401,\"message\":\"未登录或登录已过期\"}"));
            } catch (Exception ignored) {}
            emitter.complete();
            return emitter;
        }
        log.info("用户流式提问 — userId={}, question={}", userId, dto.getQuestion());

        // 会员配额预检（在异步之前，Redis Lua 原子预扣）
        boolean quotaConsumed = membershipService.tryConsume(userId, "ai_ask");
        if (!quotaConsumed) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"code\":402,\"featureKey\":\"ai_ask\",\"message\":\"今日免费次数已用完，明日重置或升级VIP享100次/天\"}"));
            } catch (Exception ignored) {}
            emitter.complete();
            return emitter;
        }

        // 处理会话（在同步线程中完成，避免异步上下文丢失）
        SessionInfo sessionInfo = ensureSession(userId, dto.getSessionId(), dto.getQuestion());
        saveMessage(sessionInfo.id, "user", dto.getQuestion(), dto.getImageUrl());

        // 标志位：防止超时/错误回调与正常流式发送并发操作 emitter
        AtomicBoolean emitterDone = new AtomicBoolean(false);

        // 异步执行，不阻塞请求线程
        CompletableFuture.runAsync(() -> {
            StringBuilder fullAnswer = new StringBuilder();
            try {
                // 先发送会话元数据（sessionId + title），让前端立即知道标题
                if (!emitterDone.get()) {
                    emitter.send(objectMapper.writeValueAsString(
                            Map.of("type", "meta", "sessionId", sessionInfo.id, "title", sessionInfo.title)));
                }

                tutorAgent.answerStream(dto.getQuestion(), dto.getSubject(), dto.getImageUrl(), chunk -> {
                    if (emitterDone.get()) return; // emitter 已关闭，丢弃后续 chunk
                    fullAnswer.append(chunk);
                    try {
                        // 用 JSON 包裹 chunk，确保 \n 等特殊字符正确转义，不会破坏 SSE 协议格式
                        emitter.send(objectMapper.writeValueAsString(Map.of("content", chunk)));
                    } catch (IllegalStateException e) {
                        // emitter 已被超时/错误回调关闭，静默标记完成并中止
                        emitterDone.set(true);
                        log.debug("SSE 发送失败（emitter 已关闭） — userId={}", userId);
                    } catch (Exception e) {
                        log.warn("SSE 发送失败 — userId={}", userId, e);
                    }
                }, userId);

                // 流式完成后保存 AI 回复到数据库
                saveMessage(sessionInfo.id, "assistant", fullAnswer.toString(), null);
                // 记录 MySQL 使用日志（Redis 已预扣，这里做持久化）
                membershipService.recordUsage(userId, "ai_ask");
                if (emitterDone.compareAndSet(false, true)) {
                    emitter.complete();
                }
            } catch (Exception e) {
                log.error("SSE 流式答疑失败 — userId={}", userId, e);
                // 退款配额（AI 调用失败，不应扣用户次数）
                membershipService.refundUsage(userId, "ai_ask");
                // 即使失败也保存已有的部分回复
                if (fullAnswer.length() > 0) {
                    saveMessage(sessionInfo.id, "assistant", fullAnswer.toString(), null);
                }
                if (emitterDone.compareAndSet(false, true)) {
                    emitter.completeWithError(e);
                }
            }
        });

        // 注册超时和错误回调
        emitter.onTimeout(() -> {
            log.warn("SSE 超时 — userId={}", userId);
            if (emitterDone.compareAndSet(false, true)) {
                emitter.complete();
            }
        });
        emitter.onError(e -> log.warn("SSE 错误 — userId={}", userId, e));

        return emitter;
    }

    // ==================== 5. 监督 Agent 手动触发（路演用） ====================

    @Operation(summary = "手动触发监督 Agent（路演演示）")
    @PostMapping("/agent/supervisor/trigger")
    @SaCheckLogin
    public Result<String> triggerSupervisor() {
        log.info("手动触发 SupervisorAgent — operator={}", StpUtil.getLoginIdAsLong());
        supervisorAgent.triggerManually();
        return Result.success("监督 Agent 扫描已触发");
    }

    // ==================== 6. 知识库管理（CRUD） ====================

    @Operation(summary = "搜索知识点")
    @GetMapping("/knowledge")
    @SaCheckLogin
    @MembershipRequired("ai_knowledge")
    public Result<List<KnowledgePoint>> searchKnowledge(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String subject) {
        List<KnowledgePoint> results;
        if (keyword != null && !keyword.isBlank()) {
            List<String> keywords = List.of(keyword.split("[，,\\s]+"));
            results = knowledgePointMapper.searchByKeywords(keywords, subject, 20);
        } else if (subject != null && !subject.isBlank()) {
            results = knowledgePointMapper.selectList(
                    new LambdaQueryWrapper<KnowledgePoint>()
                            .eq(KnowledgePoint::getSubject, subject)
                            .orderByAsc(KnowledgePoint::getImportance));
        } else {
            results = knowledgePointMapper.selectList(
                    new LambdaQueryWrapper<KnowledgePoint>()
                            .orderByAsc(KnowledgePoint::getSubject)
                            .last("LIMIT 50"));
        }
        return Result.success(results);
    }

    @Operation(summary = "新增知识点（管理员）")
    @PostMapping("/knowledge")
    @SaCheckLogin
    public Result<Void> addKnowledgePoint(@RequestBody KnowledgePoint point) {
        point.setId(null);
        point.setCreatedAt(LocalDateTime.now());
        knowledgePointMapper.insert(point);
        log.info("新增知识点 — subject={}, title={}", point.getSubject(), point.getTitle());
        return Result.success();
    }

    @Operation(summary = "修改知识点（管理员）")
    @PutMapping("/knowledge/{id}")
    @SaCheckLogin
    public Result<Void> updateKnowledgePoint(@PathVariable Long id, @RequestBody KnowledgePoint point) {
        KnowledgePoint existing = knowledgePointMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "知识点不存在");
        }
        point.setId(id);
        knowledgePointMapper.updateById(point);
        log.info("更新知识点 — id={}", id);
        return Result.success();
    }

    @Operation(summary = "删除知识点（管理员）")
    @DeleteMapping("/knowledge/{id}")
    @SaCheckLogin
    public Result<Void> deleteKnowledgePoint(@PathVariable Long id) {
        KnowledgePoint existing = knowledgePointMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "知识点不存在");
        }
        knowledgePointMapper.deleteById(id);
        log.info("删除知识点 — id={}", id);
        return Result.success();
    }

    // ==================== 7. 对话历史管理（已废弃，请使用 /chat/sessions） ====================

    @Deprecated
    @Operation(summary = "获取当前用户的 AI 对话历史（已废弃，请使用 /chat/sessions）")
    @GetMapping("/chat/history")
    @SaCheckLogin
    public Result<List<Map<String, String>>> getChatHistory() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Map<String, String>> history = tutorAgent.getHistory(userId);
        return Result.success(history);
    }

    @Deprecated
    @Operation(summary = "清除当前用户的 AI 对话历史（已废弃，请使用 DELETE /chat/sessions/{id}）")
    @DeleteMapping("/chat/history")
    @SaCheckLogin
    public Result<Void> clearChatHistory() {
        Long userId = StpUtil.getLoginIdAsLong();
        tutorAgent.clearHistory(userId);
        return Result.success();
    }

    // ==================== 7.1 对话会话管理 ====================

    @Operation(summary = "获取会话列表")
    @GetMapping("/chat/sessions")
    @SaCheckLogin
    public Result<List<ChatSessionVO>> getChatSessions() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<AiChatSession> sessions = chatSessionMapper.selectList(
                new LambdaQueryWrapper<AiChatSession>()
                        .eq(AiChatSession::getUserId, userId)
                        .eq(AiChatSession::getIsDeleted, 0)
                        .orderByDesc(AiChatSession::getUpdatedAt));

        List<ChatSessionVO> voList = sessions.stream().map(s -> {
            ChatSessionVO vo = new ChatSessionVO();
            vo.setId(s.getId());
            vo.setTitle(s.getTitle());
            vo.setUpdatedAt(s.getUpdatedAt());

            // 查询最后一条消息
            AiChatMessage lastMsg = chatMessageMapper.selectOne(
                    new LambdaQueryWrapper<AiChatMessage>()
                            .eq(AiChatMessage::getSessionId, s.getId())
                            .orderByDesc(AiChatMessage::getCreatedAt)
                            .last("LIMIT 1"));
            vo.setLastMessage(lastMsg != null ? lastMsg.getContent() : null);

            // 查询消息总数
            Long count = chatMessageMapper.selectCount(
                    new LambdaQueryWrapper<AiChatMessage>()
                            .eq(AiChatMessage::getSessionId, s.getId()));
            vo.setMessageCount(count.intValue());

            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "新建会话")
    @PostMapping("/chat/sessions")
    @SaCheckLogin
    public Result<ChatSessionVO> createChatSession() {
        Long userId = StpUtil.getLoginIdAsLong();
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setTitle("新对话");
        session.setIsDeleted(0);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.insert(session);

        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        vo.setTitle(session.getTitle());
        vo.setUpdatedAt(session.getUpdatedAt());
        vo.setMessageCount(0);
        return Result.success(vo);
    }

    @Operation(summary = "获取某会话的消息列表")
    @GetMapping("/chat/sessions/{id}/messages")
    @SaCheckLogin
    public Result<List<ChatMessageVO>> getChatMessages(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        AiChatSession session = chatSessionMapper.selectById(id);
        if (session == null || !session.getUserId().equals(userId) || session.getIsDeleted() == 1) {
            return Result.error(404, "会话不存在");
        }

        List<AiChatMessage> messages = chatMessageMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, id)
                        .orderByAsc(AiChatMessage::getCreatedAt));

        List<ChatMessageVO> voList = messages.stream().map(m -> {
            ChatMessageVO vo = new ChatMessageVO();
            vo.setId(m.getId());
            vo.setRole(m.getRole());
            vo.setContent(m.getContent());
            vo.setImageUrl(m.getImageUrl());
            vo.setCreatedAt(m.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "删除会话（含消息）")
    @DeleteMapping("/chat/sessions/{id}")
    @SaCheckLogin
    public Result<Void> deleteChatSession(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        AiChatSession session = chatSessionMapper.selectById(id);
        if (session == null || !session.getUserId().equals(userId) || session.getIsDeleted() == 1) {
            return Result.error(404, "会话不存在");
        }

        // 逻辑删除会话
        session.setIsDeleted(1);
        chatSessionMapper.updateById(session);

        // 物理删除关联消息
        chatMessageMapper.delete(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, id));

        log.info("删除会话 — userId={}, sessionId={}", userId, id);
        return Result.success();
    }

    // ==================== 8. 社区首页 AI 摘要 ====================

    @Operation(summary = "获取社区首页 AI 摘要数据")
    @GetMapping("/summary")
    @SaCheckLogin
    public Result<AiSummaryVO> getAiSummary() {
        Long userId = StpUtil.getLoginIdAsLong();
        AiSummaryVO vo = new AiSummaryVO();

        // 今日任务统计
        List<AiDailyTask> todayTasks = taskMapper.selectList(
                new LambdaQueryWrapper<AiDailyTask>()
                        .eq(AiDailyTask::getUserId, userId)
                        .eq(AiDailyTask::getTaskDate, LocalDate.now()));
        vo.setTotalTasks(todayTasks.size());
        vo.setCompletedTasks((int) todayTasks.stream()
                .filter(t -> t.getStatus() != null && t.getStatus() == 1).count());

        // 未读 AI 消息数
        Long unreadCount = interventionMapper.selectCount(
                new LambdaQueryWrapper<AiInterventionLog>()
                        .eq(AiInterventionLog::getUserId, userId)
                        .eq(AiInterventionLog::getUserReaction, "UNREAD"));
        vo.setUnreadCount(unreadCount.intValue());

        // 连续打卡天数
        UserStudyPO study = userStudyMapper.selectByUserId(userId);
        vo.setStreakDays(study != null && study.getContinuousDays() != null
                ? study.getContinuousDays() : 0);

        // 今日建议（简单规则：根据任务完成率给出提示）
        if (todayTasks.size() > 0) {
            long completed = todayTasks.stream()
                    .filter(t -> t.getStatus() != null && t.getStatus() == 1).count();
            if (completed < todayTasks.size()) {
                vo.setTodayTip("今日还有 " + (todayTasks.size() - completed) + " 项任务未完成，加油！");
            } else {
                vo.setTodayTip("今日任务已全部完成，继续保持！");
            }
        }

        return Result.success(vo);
    }

    // ==================== 9. 周报历史 ====================

    @Operation(summary = "获取历史周报列表")
    @GetMapping("/report/history")
    @SaCheckLogin
    @MembershipRequired("weekly_report")
    public Result<List<ReportHistoryVO>> getReportHistory(
            @RequestParam(defaultValue = "4") int limit) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<AiReport> reports = reportMapper.selectList(
                new LambdaQueryWrapper<AiReport>()
                        .eq(AiReport::getUserId, userId)
                        .orderByDesc(AiReport::getWeekStart)
                        .last("LIMIT " + Math.min(limit, 12)));
        List<ReportHistoryVO> voList = reports.stream().map(r -> {
            ReportHistoryVO vo = new ReportHistoryVO();
            vo.setId(r.getId());
            vo.setWeekStart(r.getWeekStart());
            vo.setWeekEnd(r.getWeekEnd());
            vo.setMarkdown(r.getMarkdown());
            return vo;
        }).collect(Collectors.toList());
        return Result.success(voList);
    }

    // ==================== 10. 行为事件上报 ====================

    @Operation(summary = "上报用户行为事件（浏览/收藏/搜索/点赞）")
    @PostMapping("/events")
    @SaCheckLogin
    public Result<Void> reportEvent(@RequestBody @Valid AiEventDTO dto) throws JsonProcessingException {
        Long userId = StpUtil.getLoginIdAsLong();

        // 校验事件类型
        String type = dto.getEventType();
        if (!"VIEW_POST".equals(type) && !"COLLECT_POST".equals(type)
                && !"SEARCH".equals(type) && !"LIKE_POST".equals(type)) {
            return Result.error(400, "不支持的事件类型: " + type);
        }

        AiUserEvent event = new AiUserEvent();
        event.setUserId(userId);
        event.setEventType(type);
        event.setEventData(dto.getEventData() != null
                ? objectMapper.writeValueAsString(dto.getEventData()) : null);
        event.setCreatedAt(LocalDateTime.now());
        userEventMapper.insert(event);

        log.info("行为事件上报 — userId={}, type={}", userId, type);
        return Result.success();
    }

    // ==================== 11. 智能推荐 ====================

    @Operation(summary = "基于用户画像推荐知识点")
    @GetMapping("/recommendations")
    @SaCheckLogin
    public Result<RecommendationVO> getRecommendations() throws JsonProcessingException {
        Long userId = StpUtil.getLoginIdAsLong();
        RecommendationVO vo = new RecommendationVO();

        // 从用户画像中获取兴趣关键词
        UserAiProfile profile = userProfileMapper.selectOne(
                new LambdaQueryWrapper<UserAiProfile>()
                        .eq(UserAiProfile::getUserId, userId));

        List<String> interestKeywords = new ArrayList<>();
        if (profile != null && profile.getCognitiveProfile() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> cognitive = objectMapper.readValue(
                        profile.getCognitiveProfile(), Map.class);
                @SuppressWarnings("unchecked")
                List<String> keywords = (List<String>) cognitive.get("interestKeywords");
                if (keywords != null) {
                    interestKeywords = keywords;
                }
            } catch (Exception ignored) {
            }
        }

        List<RecommendationVO.KnowledgeRecommendation> recommendations = new ArrayList<>();

        if (!interestKeywords.isEmpty()) {
            // 基于兴趣关键词推荐知识点
            List<KnowledgePoint> points = knowledgePointMapper.searchByKeywords(
                    interestKeywords, null, 5);
            for (KnowledgePoint point : points) {
                RecommendationVO.KnowledgeRecommendation rec = new RecommendationVO.KnowledgeRecommendation();
                rec.setId(point.getId());
                rec.setTitle(point.getTitle());
                rec.setSubject(point.getSubject());
                rec.setReason("您最近频繁查阅「" + point.getKeywords().split(",")[0] + "」相关内容");
                recommendations.add(rec);
            }
        }

        // 如果推荐不足 3 个，补充高重要性知识点
        if (recommendations.size() < 3) {
            List<KnowledgePoint> highImportance = knowledgePointMapper.selectList(
                    new LambdaQueryWrapper<KnowledgePoint>()
                            .eq(KnowledgePoint::getImportance, "HIGH")
                            .orderByDesc(KnowledgePoint::getCreatedAt)
                            .last("LIMIT " + (3 - recommendations.size())));
            Set<Long> existingIds = recommendations.stream()
                    .map(RecommendationVO.KnowledgeRecommendation::getId)
                    .collect(Collectors.toSet());
            for (KnowledgePoint point : highImportance) {
                if (!existingIds.contains(point.getId())) {
                    RecommendationVO.KnowledgeRecommendation rec = new RecommendationVO.KnowledgeRecommendation();
                    rec.setId(point.getId());
                    rec.setTitle(point.getTitle());
                    rec.setSubject(point.getSubject());
                    rec.setReason("高频考点，建议重点复习");
                    recommendations.add(rec);
                }
            }
        }

        vo.setKnowledgePoints(recommendations);
        return Result.success(vo);
    }

    // ==================== 辅助方法 ====================

    private record SessionInfo(Long id, String title) {}

    /**
     * 确保会话存在：若 sessionId 为 null 则自动创建新会话，标题取用户问题前 20 字。
     */
    private SessionInfo ensureSession(Long userId, Long sessionId, String question) {
        if (sessionId != null) {
            AiChatSession existing = chatSessionMapper.selectById(sessionId);
            if (existing != null && existing.getUserId().equals(userId) && existing.getIsDeleted() == 0) {
                // 如果标题还是默认值，用第一条消息更新标题
                if ("新对话".equals(existing.getTitle())) {
                    existing.setTitle(question.length() > 20 ? question.substring(0, 20) : question);
                }
                existing.setUpdatedAt(LocalDateTime.now());
                chatSessionMapper.updateById(existing);
                return new SessionInfo(sessionId, existing.getTitle());
            }
        }
        // 自动创建新会话
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setTitle(question.length() > 20 ? question.substring(0, 20) : question);
        session.setIsDeleted(0);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionMapper.insert(session);
        return new SessionInfo(session.getId(), session.getTitle());
    }

    /**
     * 保存一条消息到数据库。
     */
    private void saveMessage(Long sessionId, String role, String content, String imageUrl) {
        AiChatMessage msg = new AiChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setImageUrl(imageUrl);
        msg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(msg);
    }
}
