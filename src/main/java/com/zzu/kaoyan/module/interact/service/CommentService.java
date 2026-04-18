package com.zzu.kaoyan.module.interact.service;

import com.zzu.kaoyan.module.interact.entity.ForumComment;
import com.zzu.kaoyan.module.interact.entity.dto.CommentDTO;
import com.zzu.kaoyan.module.interact.entity.vo.CommentPublishVO; // 新增 VO
import java.util.List;

public interface CommentService {
    /**
     * 发布评论/回复
     * 修改点：返回类型改为 VO，包含评论ID、用户名、头像
     */
    CommentPublishVO publishComment(Long postId, Long replyToId, String content);

    /**
     * 获取帖子评论列表（楼中楼结构）
     * 逻辑修改点：内部需要注入当前用户对每条评论的点赞状态(isLiked)和用户信息
     */
    List<CommentDTO> getCommentTree(Long postId);

    /**
     * 获取帖子评论列表（平铺结构）
     */
    List<ForumComment> getCommentList(Long postId);
}