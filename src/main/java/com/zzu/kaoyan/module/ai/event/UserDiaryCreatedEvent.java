package com.zzu.kaoyan.module.ai.event;

/**
 * 日记创建事件 — 为未来日记模块预留。PsychologyAgent 监听此事件进行情感介入。
 */
public class UserDiaryCreatedEvent {

    private final Long userId;
    private final String content;

    public UserDiaryCreatedEvent(Long userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    public Long getUserId() { return userId; }
    public String getContent() { return content; }
}
