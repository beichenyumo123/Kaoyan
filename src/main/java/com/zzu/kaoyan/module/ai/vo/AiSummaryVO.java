package com.zzu.kaoyan.module.ai.vo;

import lombok.Data;

/**
 * 社区首页 AI 摘要 VO
 */
@Data
public class AiSummaryVO {

    /** 今日任务总数 */
    private int totalTasks;

    /** 今日已完成任务数 */
    private int completedTasks;

    /** 未读 AI 消息数 */
    private int unreadCount;

    /** 连续打卡天数 */
    private int streakDays;

    /** 今日学习建议（可为空） */
    private String todayTip;
}
