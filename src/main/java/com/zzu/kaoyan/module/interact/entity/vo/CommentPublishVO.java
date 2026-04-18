package com.zzu.kaoyan.module.interact.entity.vo;

import lombok.Data;

@Data
public class CommentPublishVO {
    
    // 刚生成的评论的ID
    private Long commentId;
    
    // 评论者的用户名
    private String username;
    
    // 评论者的头像
    private String avatarUrl;
    
}