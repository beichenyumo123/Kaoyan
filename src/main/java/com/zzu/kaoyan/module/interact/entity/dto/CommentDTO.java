package com.zzu.kaoyan.module.interact.entity.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {

    private Long id;

    private Long postId;

    private Long userId;

    // 前端渲染需要的用户信息
    private String username;

    private String avatarUrl;

    // 回复的父评论ID（顶层评论为null）
    private Long replyToId;

    private String content;

    private LocalDateTime createdAt;

    // 子评论列表（楼中楼回复）
    private List<CommentDTO> children;
}