package com.zzu.kaoyan.module.chat.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatGroupVO {
    private Long id;
    private String name;
    private String description;
    private String avatarUrl;
    private Long ownerId;
    private String ownerName;
    private Integer memberCount;
    private LocalDateTime createdAt;
}
