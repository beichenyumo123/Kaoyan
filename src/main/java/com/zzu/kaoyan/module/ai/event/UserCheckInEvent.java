package com.zzu.kaoyan.module.ai.event;

/**
 * 用户打卡事件 — Planner / Psychology / Supervisor Agent 的启动引擎。
 */
public class UserCheckInEvent {

    private final Long userId;
    private final int continuousDays;
    private final int totalCheckDays;
    private final int studyHours;
    private final String notes;

    public UserCheckInEvent(Long userId, int continuousDays, int totalCheckDays, int studyHours, String notes) {
        this.userId = userId;
        this.continuousDays = continuousDays;
        this.totalCheckDays = totalCheckDays;
        this.studyHours = studyHours;
        this.notes = notes;
    }

    public Long getUserId() { return userId; }
    public int getContinuousDays() { return continuousDays; }
    public int getTotalCheckDays() { return totalCheckDays; }
    public int getStudyHours() { return studyHours; }
    public String getNotes() { return notes; }
}
