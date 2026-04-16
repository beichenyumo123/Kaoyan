package com.zzu.kaoyan.module.interact.service;

import com.zzu.kaoyan.module.interact.entity.ForumComment;
import com.zzu.kaoyan.module.interact.entity.dto.CommentDTO;
import java.util.List;

public interface CommentService {
    /**
     * 发布评论/回复
     * @param postId 帖子ID
     * @param replyToId 回复的评论ID（可为null，代表顶层评论）
     * @param content 评论内容
     * @return 评论ID
     */
    Long publishComment(Long postId, Long replyToId, String content);

    /**
     * 获取帖子评论列表（楼中楼结构）
     * @param postId 帖子ID
     * @return 楼中楼评论列表
     */
    List<CommentDTO> getCommentTree(Long postId);

    /**
     * 获取帖子评论列表（平铺结构）
     * @param postId 帖子ID
     * @return 平铺评论列表
     */
    List<ForumComment> getCommentList(Long postId);
}