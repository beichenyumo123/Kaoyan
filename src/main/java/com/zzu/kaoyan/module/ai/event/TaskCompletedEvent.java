package com.zzu.kaoyan.module.ai.event;

/**
 * AI 任务完成事件 — PlannerAgent 后续迭代使用。
 */
public class TaskCompletedEvent {

    private final Long userId;
    private final Long taskId;
    private final String taskContent;

    public TaskCompletedEvent(Long userId, Long taskId, String taskContent) {
        this.userId = userId;
        this.taskId = taskId;
        this.taskContent = taskContent;
    }

    public Long getUserId() { return userId; }
    public Long getTaskId() { return taskId; }
    public String getTaskContent() { return taskContent; }
}
