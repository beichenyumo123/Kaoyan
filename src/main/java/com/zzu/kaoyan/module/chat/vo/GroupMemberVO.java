package com.zzu.kaoyan.module.chat.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupMemberVO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String role;
    private LocalDateTime joinedAt;
}
