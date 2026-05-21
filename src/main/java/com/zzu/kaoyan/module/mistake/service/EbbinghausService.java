package com.zzu.kaoyan.module.mistake.service;

import java.time.LocalDate;

public interface EbbinghausService {

    /**
     * 艾宾浩斯复习间隔（天）
     * Stage 0->1: 1天, 1->2: 1天, 2->3: 2天, 3->4: 3天
     * 4->5: 8天, 5->6: 15天, 6->7: 30天, 7=已掌握
     */
    int[] INTERVALS = {1, 1, 2, 3, 8, 15, 30};

    int MAX_STAGE = 8;

    /**
     * 计算下一阶段编号
     */
    int nextStage(int currentStage);

    /**
     * 计算下次复习日期
     */
    LocalDate calcNextReviewDate(int nextStage);

    /**
     * 计算掌握程度变化：答对加分，答错扣分
     */
    int calcMastery(int currentMastery, boolean isCorrect);

    /**
     * 获取阶段文本描述
     */
    String getStageText(int stage);

    /**
     * 为指定用户生成今日复习计划
     */
    void generateDailyPlan(Long userId);

    /**
     * 为所有用户生成今日复习计划（定时任务调用）
     */
    void generateDailyPlansForAllUsers();
}
