package com.zzu.kaoyan.module.mistake.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.mistake.entity.po.DailyPlanPO;
import com.zzu.kaoyan.module.mistake.entity.po.MistakeNotePO;
import com.zzu.kaoyan.module.mistake.mapper.DailyPlanMapper;
import com.zzu.kaoyan.module.mistake.mapper.MistakeNoteMapper;
import com.zzu.kaoyan.module.mistake.service.EbbinghausService;
import com.zzu.kaoyan.module.mistake.service.MistakeNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EbbinghausServiceImpl implements EbbinghausService {

    private static final String[] STAGE_TEXTS = {
            "新录入", "第1次复习(1天后)", "第2次复习(1天后)", "第3次复习(2天后)",
            "第4次复习(3天后)", "第5次复习(8天后)", "第6次复习(15天后)", "第7次复习(30天后)",
            "已掌握"
    };

    private final MistakeNoteMapper mistakeNoteMapper;
    private final DailyPlanMapper dailyPlanMapper;
    private final MistakeNotificationService notificationService;

    public EbbinghausServiceImpl(MistakeNoteMapper mistakeNoteMapper,
                                 DailyPlanMapper dailyPlanMapper,
                                 MistakeNotificationService notificationService) {
        this.mistakeNoteMapper = mistakeNoteMapper;
        this.dailyPlanMapper = dailyPlanMapper;
        this.notificationService = notificationService;
    }

    @Override
    public int nextStage(int currentStage) {
        return Math.min(currentStage + 1, MAX_STAGE);
    }

    @Override
    public LocalDate calcNextReviewDate(int nextStage) {
        if (nextStage >= MAX_STAGE) {
            return null; // 已达最终阶段，不再需要复习
        }
        if (nextStage < 1 || nextStage > INTERVALS.length) {
            return LocalDate.now().plusDays(1);
        }
        return LocalDate.now().plusDays(INTERVALS[nextStage - 1]);
    }

    @Override
    public int calcMastery(int currentMastery, boolean isCorrect) {
        if (isCorrect) {
            return Math.min(currentMastery + 15, 100);
        } else {
            return Math.max(currentMastery - 10, 0);
        }
    }

    @Override
    public String getStageText(int stage) {
        if (stage < 0 || stage > MAX_STAGE) {
            return "未知";
        }
        return STAGE_TEXTS[Math.min(stage, STAGE_TEXTS.length - 1)];
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateDailyPlan(Long userId) {
        LocalDate today = LocalDate.now();

        // 查询今天到期的待复习错题
        List<MistakeNotePO> dueNotes = mistakeNoteMapper.selectList(
                new LambdaQueryWrapper<MistakeNotePO>()
                        .eq(MistakeNotePO::getUserId, userId)
                        .eq(MistakeNotePO::getIsDeleted, 0)
                        .le(MistakeNotePO::getNextReviewDate, today)
                        .lt(MistakeNotePO::getReviewStage, MAX_STAGE)
        );

        if (dueNotes.isEmpty()) {
            return;
        }

        // 创建复习提醒通知
        try {
            int newCount = dueNotes.size();
            notificationService.create(userId, "REVIEW_REMINDER",
                    "今日复习提醒",
                    "你今天有 " + newCount + " 道错题待复习，点击查看详情");
        } catch (Exception e) {
            log.warn("创建复习提醒通知失败: userId={}, {}", userId, e.getMessage());
        }

        List<Long> noteIds = dueNotes.stream()
                .map(MistakeNotePO::getId)
                .collect(Collectors.toList());

        // 检查是否已有今日计划
        DailyPlanPO existing = dailyPlanMapper.selectOne(
                new LambdaQueryWrapper<DailyPlanPO>()
                        .eq(DailyPlanPO::getUserId, userId)
                        .eq(DailyPlanPO::getPlanDate, today)
        );

        if (existing != null) {
            // 合并去重
            List<Long> merged = new ArrayList<>(existing.getNoteIds());
            for (Long noteId : noteIds) {
                if (!merged.contains(noteId)) {
                    merged.add(noteId);
                }
            }
            existing.setNoteIds(merged);
            existing.setTotalCount(merged.size());
            existing.setIsCompleted(merged.isEmpty() ? 1 : 0);
            dailyPlanMapper.updateById(existing);
        } else {
            DailyPlanPO plan = new DailyPlanPO();
            plan.setUserId(userId);
            plan.setPlanDate(today);
            plan.setNoteIds(noteIds);
            plan.setCompletedIds(new ArrayList<>());
            plan.setTotalCount(noteIds.size());
            plan.setCompletedCount(0);
            plan.setIsCompleted(0);
            dailyPlanMapper.insert(plan);
        }
    }

    @Override
    public void generateDailyPlansForAllUsers() {
        List<Long> userIds = dailyPlanMapper.selectUsersWithPendingReviews();
        log.info("生成每日复习计划，涉及 {} 个用户", userIds.size());
        for (Long userId : userIds) {
            try {
                generateDailyPlan(userId);
            } catch (Exception e) {
                log.error("生成用户 {} 的复习计划失败", userId, e);
            }
        }
    }
}
