package com.zzu.kaoyan.module.post.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDetailVO {
    private Long id;
    private Long boardId;
    private String title;
    private String content;
    private AuthorVO author;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;
    private LocalDateTime createdAt;


    // ===================== 【新增】标签 =====================
    private List<String> tags;
    @Data
    public static class AuthorVO {
        private Long userId;
        private String username;
        private String avatarUrl;
    }


}