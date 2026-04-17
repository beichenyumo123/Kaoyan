package com.zzu.kaoyan.module.interact.entity.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private Long replyToId;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentDTO> children;
}