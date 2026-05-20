package com.zzu.kaoyan.module.chat.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {
    private Long id;
    private Long groupId;
    private Long userId;
    private String username;
    private String avatarUrl;
    private String content;
    private LocalDateTime createdAt;
}
