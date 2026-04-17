package com.zzu.kaoyan.module.interact.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.interact.entity.ForumComment;
import com.zzu.kaoyan.module.interact.entity.dto.CommentDTO;
import com.zzu.kaoyan.module.interact.mapper.ForumCommentMapper;
import com.zzu.kaoyan.module.interact.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final ForumCommentMapper commentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishComment(Long postId, Long replyToId, String content) {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 1. 回复评论时，必须保证被回复的评论存在
        if (replyToId != null) {
            ForumComment replyComment = commentMapper.selectById(replyToId);
            if (replyComment == null || replyComment.getIsDeleted() == 1) {
                throw new RuntimeException("回复的评论不存在");
            }
        }

        // 2. 保存评论
        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setReplyToId(replyToId);
        comment.setContent(content);
        comment.setIsDeleted(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(comment);

        return comment.getId();
    }

    @Override
    public List<CommentDTO> getCommentTree(Long postId) {
        // 1. 查询所有有效评论
        List<ForumComment> allComments = commentMapper.selectList(
                new LambdaQueryWrapper<ForumComment>()
                        .eq(ForumComment::getPostId, postId)
                        .eq(ForumComment::getIsDeleted, 0)
                        .orderByAsc(ForumComment::getCreatedAt)
        );

        // 2. 转换为DTO并分组
        Map<Long, List<CommentDTO>> childrenMap = new HashMap<>();
        List<CommentDTO> rootComments = new ArrayList<>();

        for (ForumComment comment : allComments) {
            CommentDTO dto = new CommentDTO();
            dto.setId(comment.getId());
            dto.setPostId(comment.getPostId());
            dto.setUserId(comment.getUserId());
            dto.setReplyToId(comment.getReplyToId());
            dto.setContent(comment.getContent());
            dto.setCreatedAt(comment.getCreatedAt());
            dto.setChildren(new ArrayList<>());

            if (comment.getReplyToId() == null) {
                // 顶层评论
                rootComments.add(dto);
            } else {
                // 回复评论，加入对应父节点的子列表
                childrenMap.computeIfAbsent(comment.getReplyToId(), k -> new ArrayList<>()).add(dto);
            }
        }

        // 3. 递归组装楼中楼结构
        for (CommentDTO root : rootComments) {
            buildTree(root, childrenMap);
        }

        return rootComments;
    }

    private void buildTree(CommentDTO parent, Map<Long, List<CommentDTO>> childrenMap) {
        List<CommentDTO> children = childrenMap.get(parent.getId());
        if (children != null) {
            parent.setChildren(children);
            for (CommentDTO child : children) {
                buildTree(child, childrenMap);
            }
        }
    }

    @Override
    public List<ForumComment> getCommentList(Long postId) {
        return commentMapper.selectList(
                new LambdaQueryWrapper<ForumComment>()
                        .eq(ForumComment::getPostId, postId)
                        .eq(ForumComment::getIsDeleted, 0)
                        .orderByAsc(ForumComment::getCreatedAt)
        );
    }
}