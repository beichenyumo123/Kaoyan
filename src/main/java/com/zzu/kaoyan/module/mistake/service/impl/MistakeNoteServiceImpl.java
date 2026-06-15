package com.zzu.kaoyan.module.mistake.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.common.util.MarkdownRenderUtil;
import com.zzu.kaoyan.module.ai.service.EmbeddingService;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteCreateDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.MistakeNoteUpdateDTO;
import com.zzu.kaoyan.module.mistake.entity.dto.QuickSaveDTO;
import com.zzu.kaoyan.module.mistake.entity.po.DailyPlanPO;
import com.zzu.kaoyan.module.mistake.entity.po.MistakeNotePO;
import com.zzu.kaoyan.module.mistake.entity.po.ReviewLogPO;
import com.zzu.kaoyan.module.mistake.entity.vo.*;
import com.zzu.kaoyan.module.mistake.mapper.DailyPlanMapper;
import com.zzu.kaoyan.module.mistake.mapper.MistakeNoteMapper;
import com.zzu.kaoyan.module.mistake.mapper.ReviewLogMapper;
import com.zzu.kaoyan.module.mistake.service.EbbinghausService;
import com.zzu.kaoyan.module.mistake.service.MistakeNoteService;
import com.zzu.kaoyan.module.mistake.service.MistakeNotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.module.ai.config.AiApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MistakeNoteServiceImpl implements MistakeNoteService {

    private final MistakeNoteMapper mistakeNoteMapper;
    private final ReviewLogMapper reviewLogMapper;
    private final DailyPlanMapper dailyPlanMapper;
    private final EbbinghausService ebbinghausService;
    private final MistakeNotificationService notificationService;
    private final RestTemplate aiRestTemplate;
    private final AiApiProperties aiApiProperties;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MistakeNoteServiceImpl(MistakeNoteMapper mistakeNoteMapper,
                                  ReviewLogMapper reviewLogMapper,
                                  DailyPlanMapper dailyPlanMapper,
                                  EbbinghausService ebbinghausService,
                                  MistakeNotificationService notificationService,
                                  @org.springframework.beans.factory.annotation.Qualifier("aiRestTemplate") RestTemplate aiRestTemplate,
                                  AiApiProperties aiApiProperties,
                                  EmbeddingService embeddingService) {
        this.mistakeNoteMapper = mistakeNoteMapper;
        this.reviewLogMapper = reviewLogMapper;
        this.dailyPlanMapper = dailyPlanMapper;
        this.ebbinghausService = ebbinghausService;
        this.notificationService = notificationService;
        this.aiRestTemplate = aiRestTemplate;
        this.aiApiProperties = aiApiProperties;
        this.embeddingService = embeddingService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MistakeNoteVO create(MistakeNoteCreateDTO dto, Long userId) {
        MistakeNotePO note = new MistakeNotePO();
        BeanUtils.copyProperties(dto, note);
        note.setUserId(userId);
        note.setReviewStage(0);
        note.setReviewCount(0);
        note.setMasteryLevel(0);
        note.setNextReviewDate(LocalDate.now().plusDays(1)); // 明天首次复习
        mistakeNoteMapper.insert(note);
        return toVO(note, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MistakeNoteVO update(Long noteId, MistakeNoteUpdateDTO dto, Long userId) {
        MistakeNotePO note = getOwnedNote(noteId, userId);
        if (dto.getQuestionContent() != null) note.setQuestionContent(dto.getQuestionContent());
        if (dto.getAnswer() != null) note.setAnswer(dto.getAnswer());
        if (dto.getImageUrl() != null) note.setImageUrl(dto.getImageUrl());
        if (dto.getKnowledgePoints() != null) note.setKnowledgePoints(dto.getKnowledgePoints());
        if (dto.getSource() != null) note.setSource(dto.getSource());
        if (dto.getDifficulty() != null) note.setDifficulty(dto.getDifficulty());
        if (dto.getMasteryLevel() != null) note.setMasteryLevel(dto.getMasteryLevel());
        mistakeNoteMapper.updateById(note);
        return toVO(note, false);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long noteId, Long userId) {
        MistakeNotePO note = getOwnedNote(noteId, userId);
        note.setIsDeleted(1);
        mistakeNoteMapper.updateById(note);
    }

    @Override
    public MistakeNoteVO getById(Long noteId, Long userId) {
        return toVO(getOwnedNote(noteId, userId), false);
    }

    @Override
    public MistakeNoteVO getById(Long noteId, Long userId, boolean renderHtml) {
        return toVO(getOwnedNote(noteId, userId), renderHtml);
    }

    @Override
    public String renderMarkdown(String markdown) {
        return MarkdownRenderUtil.render(markdown);
    }

    @Override
    public PageInfo<MistakeNoteVO> page(Integer pageNum, Integer pageSize, String subject, Long userId) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<MistakeNotePO> wrapper = new LambdaQueryWrapper<MistakeNotePO>()
                .eq(MistakeNotePO::getUserId, userId)
                .eq(MistakeNotePO::getIsDeleted, 0)
                .orderByDesc(MistakeNotePO::getCreatedAt);
        if (subject != null && !subject.isEmpty()) {
            wrapper.eq(MistakeNotePO::getSubject, subject);
        }
        List<MistakeNotePO> list = mistakeNoteMapper.selectList(wrapper);
        PageInfo<MistakeNotePO> poPageInfo = new PageInfo<>(list);
        List<MistakeNoteVO> voList = list.stream().map(n -> toVO(n, false)).collect(Collectors.toList());
        PageInfo<MistakeNoteVO> result = new PageInfo<>(voList);
        result.setTotal(poPageInfo.getTotal());
        result.setPageNum(poPageInfo.getPageNum());
        result.setPageSize(poPageInfo.getPageSize());
        return result;
    }

    @Override
    public PageInfo<ReviewTaskVO> getTodayReviewTasks(Long userId, Integer pageNum, Integer pageSize) {
        // 先确保今天的复习计划已生成（文档承诺"手动调用此接口触发生成"）
        ebbinghausService.generateDailyPlan(userId);

        LocalDate today = LocalDate.now();
        DailyPlanPO plan = dailyPlanMapper.selectOne(
                new LambdaQueryWrapper<DailyPlanPO>()
                        .eq(DailyPlanPO::getUserId, userId)
                        .eq(DailyPlanPO::getPlanDate, today)
        );

        if (plan == null || plan.getNoteIds() == null || plan.getNoteIds().isEmpty()) {
            return new PageInfo<>(Collections.emptyList());
        }

        List<Long> noteIds = plan.getNoteIds();
        Set<Long> completedSet = plan.getCompletedIds() != null
                ? new HashSet<>(plan.getCompletedIds()) : Collections.emptySet();

        // 分页在内存中做
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, noteIds.size());
        if (start >= noteIds.size()) {
            return new PageInfo<>(Collections.emptyList());
        }

        List<Long> pageIds = noteIds.subList(start, end);
        List<MistakeNotePO> notes = mistakeNoteMapper.selectBatchIds(pageIds);

        List<ReviewTaskVO> voList = new ArrayList<>();
        for (MistakeNotePO note : notes) {
            ReviewTaskVO vo = new ReviewTaskVO();
            vo.setId(plan.getId()); // plan id
            vo.setNoteId(note.getId());
            vo.setSubject(note.getSubject());
            vo.setQuestionContent(note.getQuestionContent());
            vo.setAnswer(note.getAnswer());
            vo.setKnowledgePoints(note.getKnowledgePoints());
            vo.setDifficulty(note.getDifficulty());
            vo.setMasteryLevel(note.getMasteryLevel());
            vo.setReviewStage(note.getReviewStage());
            vo.setReviewStageText(ebbinghausService.getStageText(note.getReviewStage()));
            vo.setReviewCount(note.getReviewCount());
            vo.setIsCompleted(completedSet.contains(note.getId()));
            vo.setPlanDate(today);
            voList.add(vo);
        }

        PageInfo<ReviewTaskVO> result = new PageInfo<>(voList);
        result.setTotal(noteIds.size());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResultVO completeReview(Long noteId, Integer masteryAfter, Integer isCorrect, Long userId) {
        MistakeNotePO note = getOwnedNote(noteId, userId);
        int oldStage = note.getReviewStage();
        int oldMastery = note.getMasteryLevel();

        // 记录复习日志
        ReviewLogPO reviewLog = new ReviewLogPO();
        reviewLog.setNoteId(noteId);
        reviewLog.setUserId(userId);
        reviewLog.setReviewStage(oldStage);
        reviewLog.setMasteryBefore(oldMastery);
        reviewLog.setMasteryAfter(masteryAfter);
        reviewLog.setIsCorrect(isCorrect);
        reviewLog.setReviewedAt(LocalDateTime.now());
        reviewLogMapper.insert(reviewLog);

        // 更新错题状态
        int nextStage = ebbinghausService.nextStage(oldStage);
        note.setReviewStage(nextStage);
        note.setMasteryLevel(masteryAfter);
        note.setReviewCount(note.getReviewCount() + 1);
        note.setLastReviewDate(LocalDate.now());
        note.setNextReviewDate(ebbinghausService.calcNextReviewDate(nextStage));
        mistakeNoteMapper.updateById(note);

        // 通知：掌握度里程碑（跨过 80 分）
        if (oldMastery < 80 && masteryAfter >= 80) {
            try {
                notificationService.create(userId, "MASTERY_MILESTONE",
                        "知识点已掌握！",
                        (note.getKnowledgePoints() != null ? note.getKnowledgePoints() : note.getSubject())
                                + " 掌握度达到 " + masteryAfter + "%");
            } catch (Exception e) {
                log.warn("创建掌握度里程碑通知失败", e);
            }
        }
        // 通知：达到阶段 8（长期记忆）
        if (nextStage >= EbbinghausService.MAX_STAGE) {
            try {
                String question = note.getQuestionContent() != null
                        ? note.getQuestionContent().substring(0, Math.min(30, note.getQuestionContent().length())) : "";
                notificationService.create(userId, "STAGE_MASTERED",
                        "恭喜！错题已通关",
                        "「" + question + "…」已通过全部 7 次复习，进入长期记忆！");
            } catch (Exception e) {
                log.warn("创建阶段完成通知失败", e);
            }
        }

        // 同步更新每日计划
        LocalDate today = LocalDate.now();
        DailyPlanPO plan = dailyPlanMapper.selectOne(
                new LambdaQueryWrapper<DailyPlanPO>()
                        .eq(DailyPlanPO::getUserId, userId)
                        .eq(DailyPlanPO::getPlanDate, today)
        );
        if (plan != null) {
            List<Long> completedIds = plan.getCompletedIds() != null
                    ? new ArrayList<>(plan.getCompletedIds()) : new ArrayList<>();
            if (!completedIds.contains(noteId)) {
                completedIds.add(noteId);
            }
            plan.setCompletedIds(completedIds);
            plan.setCompletedCount(completedIds.size());
            plan.setIsCompleted(completedIds.size() >= plan.getTotalCount() ? 1 : 0);
            dailyPlanMapper.updateById(plan);
        }

        ReviewResultVO result = new ReviewResultVO();
        result.setNoteId(noteId);
        result.setReviewStage(nextStage);
        result.setReviewStageText(ebbinghausService.getStageText(nextStage));
        result.setMasteryLevel(masteryAfter);
        result.setNextReviewDate(note.getNextReviewDate());
        result.setReviewCount(note.getReviewCount());
        result.setIsCorrect(isCorrect);
        return result;
    }

    @Override
    public MistakeStatsVO getStats(Long userId) {
        // 总错题数
        Long totalNotes = mistakeNoteMapper.selectCount(
                new LambdaQueryWrapper<MistakeNotePO>()
                        .eq(MistakeNotePO::getUserId, userId)
                        .eq(MistakeNotePO::getIsDeleted, 0)
        );

        // 今日复习任务
        LocalDate today = LocalDate.now();
        DailyPlanPO plan = dailyPlanMapper.selectOne(
                new LambdaQueryWrapper<DailyPlanPO>()
                        .eq(DailyPlanPO::getUserId, userId)
                        .eq(DailyPlanPO::getPlanDate, today)
        );
        int todayReviewCount = plan != null && plan.getTotalCount() != null ? plan.getTotalCount() : 0;
        int reviewedToday = plan != null && plan.getCompletedCount() != null ? plan.getCompletedCount() : 0;

        // 平均掌握度
        List<MistakeNotePO> allNotes = mistakeNoteMapper.selectList(
                new LambdaQueryWrapper<MistakeNotePO>()
                        .eq(MistakeNotePO::getUserId, userId)
                        .eq(MistakeNotePO::getIsDeleted, 0)
        );
        double avgMastery = allNotes.stream()
                .mapToInt(n -> n.getMasteryLevel() != null ? n.getMasteryLevel() : 0)
                .average().orElse(0);

        // 科目分布
        Map<String, Integer> subjectDist = allNotes.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getSubject() != null ? n.getSubject() : "未分类",
                        Collectors.summingInt(x -> 1)
                ));

        // 阶段分布
        Map<Integer, Integer> stageDist = allNotes.stream()
                .collect(Collectors.groupingBy(
                        n -> n.getReviewStage() != null ? n.getReviewStage() : 0,
                        Collectors.summingInt(x -> 1)
                ));

        return new MistakeStatsVO(
                totalNotes.intValue(), todayReviewCount, reviewedToday,
                Math.round(avgMastery * 100.0) / 100.0,
                subjectDist, stageDist
        );
    }

    @Override
    public CalendarMonthVO getCalendarMonth(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        LocalDate today = LocalDate.now();

        // 查询当月所有每日计划
        List<DailyPlanPO> plans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlanPO>()
                        .eq(DailyPlanPO::getUserId, userId)
                        .between(DailyPlanPO::getPlanDate, start, end)
        );

        // 构建日期 → 计划 映射
        Map<LocalDate, DailyPlanPO> planMap = plans.stream()
                .collect(Collectors.toMap(DailyPlanPO::getPlanDate, p -> p, (a, b) -> a));

        int daysInMonth = end.getDayOfMonth();
        List<CalendarDayVO> days = new ArrayList<>();
        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = LocalDate.of(year, month, d);
            DailyPlanPO plan = planMap.get(date);
            int count = plan != null && plan.getTotalCount() != null ? plan.getTotalCount() : 0;
            int completed = plan != null && plan.getCompletedCount() != null ? plan.getCompletedCount() : 0;
            days.add(new CalendarDayVO(d, count, completed, date.equals(today)));
        }

        return new CalendarMonthVO(year, month, days);
    }

    @Override
    public PageInfo<ReviewTaskVO> getCalendarDayNotes(Long userId, LocalDate date, Integer pageNum, Integer pageSize) {
        // 先生成当天计划
        ebbinghausService.generateDailyPlan(userId);

        DailyPlanPO plan = dailyPlanMapper.selectOne(
                new LambdaQueryWrapper<DailyPlanPO>()
                        .eq(DailyPlanPO::getUserId, userId)
                        .eq(DailyPlanPO::getPlanDate, date)
        );

        if (plan == null || plan.getNoteIds() == null || plan.getNoteIds().isEmpty()) {
            return new PageInfo<>(Collections.emptyList());
        }

        List<Long> noteIds = plan.getNoteIds();
        Set<Long> completedSet = plan.getCompletedIds() != null
                ? new HashSet<>(plan.getCompletedIds()) : Collections.emptySet();

        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, noteIds.size());
        if (start >= noteIds.size()) {
            return new PageInfo<>(Collections.emptyList());
        }

        List<Long> pageIds = noteIds.subList(start, end);
        List<MistakeNotePO> notes = mistakeNoteMapper.selectBatchIds(pageIds);

        List<ReviewTaskVO> voList = new ArrayList<>();
        for (MistakeNotePO note : notes) {
            ReviewTaskVO vo = new ReviewTaskVO();
            vo.setId(plan.getId());
            vo.setNoteId(note.getId());
            vo.setSubject(note.getSubject());
            vo.setQuestionContent(note.getQuestionContent());
            vo.setAnswer(note.getAnswer());
            vo.setKnowledgePoints(note.getKnowledgePoints());
            vo.setDifficulty(note.getDifficulty());
            vo.setMasteryLevel(note.getMasteryLevel());
            vo.setReviewStage(note.getReviewStage());
            vo.setReviewStageText(ebbinghausService.getStageText(note.getReviewStage()));
            vo.setReviewCount(note.getReviewCount());
            vo.setIsCompleted(completedSet.contains(note.getId()));
            vo.setPlanDate(date);
            voList.add(vo);
        }

        PageInfo<ReviewTaskVO> result = new PageInfo<>(voList);
        result.setTotal(noteIds.size());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public EbbinghausStatsVO getEbbinghausStats(Long userId, int days) {
        EbbinghausStatsVO stats = new EbbinghausStatsVO();

        // 1. 各阶段分布（用于遗忘曲线柱状图）
        List<MistakeNotePO> allNotes = mistakeNoteMapper.selectList(
                new LambdaQueryWrapper<MistakeNotePO>()
                        .eq(MistakeNotePO::getUserId, userId)
                        .eq(MistakeNotePO::getIsDeleted, 0)
        );

        Map<Integer, List<MistakeNotePO>> groupedByStage = allNotes.stream()
                .collect(Collectors.groupingBy(n -> n.getReviewStage() != null ? n.getReviewStage() : 0));

        int[] INTERVALS = {1, 1, 2, 3, 8, 15, 30};
        for (int stage = 0; stage <= 7; stage++) {
            List<MistakeNotePO> stageNotes = groupedByStage.getOrDefault(stage, Collections.emptyList());
            double avgMastery = stageNotes.stream()
                    .mapToInt(n -> n.getMasteryLevel() != null ? n.getMasteryLevel() : 0)
                    .average().orElse(0);
            int intervalDays = stage == 0 ? 1 : (stage <= 7 ? INTERVALS[stage - 1] : 0);
            stats.getStageDistribution().add(new StageIntervalVO(
                    stage,
                    ebbinghausService.getStageText(stage),
                    intervalDays,
                    stageNotes.size(),
                    Math.round(avgMastery * 100.0) / 100.0
            ));
        }

        // 2. 最近N天复习准确率趋势
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        List<ReviewLogPO> recentLogs = reviewLogMapper.selectList(
                new LambdaQueryWrapper<ReviewLogPO>()
                        .eq(ReviewLogPO::getUserId, userId)
                        .ge(ReviewLogPO::getReviewedAt, startDate.atStartOfDay())
        );

        Map<LocalDate, int[]> dailyMap = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            dailyMap.put(LocalDate.now().minusDays(days - 1 - i), new int[]{0, 0});
        }
        for (ReviewLogPO log : recentLogs) {
            LocalDate logDate = log.getReviewedAt().toLocalDate();
            int[] counts = dailyMap.get(logDate);
            if (counts != null) {
                counts[0]++;
                if (log.getIsCorrect() != null && log.getIsCorrect() == 1) {
                    counts[1]++;
                }
            }
        }
        dailyMap.forEach((date, counts) -> {
            stats.getDailyAccuracyTrend().add(new DailyAccuracyVO(date, counts[0], counts[1]));
        });

        // 3. 掌握度分布
        int[] ranges = {0, 21, 41, 61, 81};
        String[] labels = {"0-20", "21-40", "41-60", "61-80", "81-100"};
        for (int i = 0; i < ranges.length; i++) {
            int low = ranges[i];
            int high = (i < ranges.length - 1) ? ranges[i + 1] - 1 : 100;
            final int fLow = low, fHigh = high;
            long count = allNotes.stream()
                    .filter(n -> {
                        int m = n.getMasteryLevel() != null ? n.getMasteryLevel() : 0;
                        return m >= fLow && m <= fHigh;
                    })
                    .count();
            stats.getMasteryDistribution().add(new MasteryDistributionVO(labels[i], (int) count));
        }

        return stats;
    }

    private MistakeNotePO getOwnedNote(Long noteId, Long userId) {
        MistakeNotePO note = mistakeNoteMapper.selectById(noteId);
        if (note == null || note.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "错题不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作此错题");
        }
        return note;
    }

    // ==================== AI 对话快速收藏 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> quickSave(QuickSaveDTO dto, Long userId) {
        // 1. 检查是否已有收藏（基于 chatMessageIds）
        List<Long> existingIds = checkSaved(dto.getChatMessageIds(), userId);
        if (!existingIds.isEmpty()) {
            return Map.of("saved", false, "duplicateIds", existingIds);
        }

        // 2. 创建错题
        MistakeNotePO note = new MistakeNotePO();
        note.setUserId(userId);
        note.setSubject(dto.getSubject());
        note.setQuestionContent(dto.getQuestionContent());
        note.setAnswer(dto.getAnswer());
        note.setImageUrl(dto.getImageUrl());
        note.setSourceType(dto.getSourceType() != null ? dto.getSourceType() : "AI_CHAT");
        // chatMessageId 记录最后一条选中的消息ID
        List<Long> msgIds = dto.getChatMessageIds();
        if (msgIds != null && !msgIds.isEmpty()) {
            note.setChatMessageId(msgIds.get(msgIds.size() - 1));
        }
        note.setSource("AI答疑");
        // 自动提取知识点
        if (dto.getKnowledgePoints() != null && !dto.getKnowledgePoints().isBlank()) {
            note.setKnowledgePoints(dto.getKnowledgePoints());
        } else {
            String kp = extractKnowledgePoints(dto.getQuestionContent(), dto.getAnswer());
            if (kp != null) {
                note.setKnowledgePoints(kp);
            }
        }
        note.setReviewStage(0);
        note.setReviewCount(0);
        note.setMasteryLevel(0);
        note.setNextReviewDate(LocalDate.now().plusDays(1));
        note.setIsDeleted(0);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        mistakeNoteMapper.insert(note);

        // 异步保存 embedding（语义记忆）
        embeddingService.saveAsync(userId,
                (dto.getQuestionContent() != null ? dto.getQuestionContent() : "")
                        + "\n" + (dto.getAnswer() != null ? dto.getAnswer().substring(0, Math.min(300, dto.getAnswer().length())) : ""),
                "MISTAKE_NOTE", note.getId(), dto.getSubject());

        log.info("AI快速收藏成功 — userId={}, noteId={}, chatMessageIds={}", userId, note.getId(), dto.getChatMessageIds());
        return Map.of("saved", true, "noteId", note.getId());
    }

    /**
     * 调用 LLM 从题目+答案中提取知识点，逗号分隔返回。
     * 提取失败返回 null，不阻塞保存。
     */
    private String extractKnowledgePoints(String questionContent, String answer) {
        if (aiApiProperties.getKey() == null || aiApiProperties.getKey().isBlank()) {
            return null;
        }
        try {
            String content = (questionContent != null ? questionContent : "") +
                    (answer != null ? "\n解析：" + answer : "");
            // 截断避免过长
            if (content.length() > 1500) {
                content = content.substring(0, 1500);
            }

            String prompt = "从以下考研题目中提取涉及的知识点名称（1-5个），用逗号分隔返回，不要有其他内容。\n\n" + content;

            Map<String, Object> body = new HashMap<>();
            body.put("model", aiApiProperties.getModel());
            body.put("messages", List.of(
                    Map.of("role", "system", "content", "你是考研知识点提取助手。只返回知识点名称，逗号分隔，不要解释。"),
                    Map.of("role", "user", "content", prompt)
            ));
            body.put("temperature", 0.1);
            body.put("max_tokens", 100);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiApiProperties.getKey());
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String response = aiRestTemplate.postForObject(
                    aiApiProperties.getEndpoint(), entity, String.class);
            if (response == null) return null;

            JsonNode root = objectMapper.readTree(response);
            String result = root.path("choices").path(0).path("message").path("content").asText("").trim();
            if (result.isBlank() || result.startsWith("【")) return null;

            log.info("自动提取知识点 — question={}, result={}",
                    questionContent != null && questionContent.length() > 30
                            ? questionContent.substring(0, 30) + "..." : questionContent, result);
            return result;
        } catch (Exception e) {
            log.warn("知识点自动提取失败，跳过 — {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<Long> checkSaved(List<Long> chatMessageIds, Long userId) {
        if (chatMessageIds == null || chatMessageIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 查询该用户已收藏的消息ID
        List<MistakeNotePO> existing = mistakeNoteMapper.selectList(
                new LambdaQueryWrapper<MistakeNotePO>()
                        .eq(MistakeNotePO::getUserId, userId)
                        .eq(MistakeNotePO::getIsDeleted, 0)
                        .in(MistakeNotePO::getChatMessageId, chatMessageIds));
        return existing.stream()
                .map(MistakeNotePO::getChatMessageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private MistakeNoteVO toVO(MistakeNotePO note, boolean renderHtml) {
        MistakeNoteVO vo = new MistakeNoteVO();
        BeanUtils.copyProperties(note, vo);
        vo.setReviewStageText(ebbinghausService.getStageText(note.getReviewStage()));
        if (renderHtml) {
            if (note.getQuestionContent() != null) {
                vo.setQuestionContentHtml(MarkdownRenderUtil.render(note.getQuestionContent()));
            }
            if (note.getAnswer() != null) {
                vo.setAnswerHtml(MarkdownRenderUtil.render(note.getAnswer()));
            }
        }
        return vo;
    }
}
