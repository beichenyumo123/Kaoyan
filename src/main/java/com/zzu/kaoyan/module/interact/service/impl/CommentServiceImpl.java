package com.zzu.kaoyan.module.interact.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.util.SensitiveWordUtil;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.interact.entity.ForumComment;
import com.zzu.kaoyan.module.interact.entity.dto.CommentDTO;
import com.zzu.kaoyan.module.interact.entity.vo.CommentPublishVO;
import com.zzu.kaoyan.module.interact.mapper.ForumCommentMapper;
import com.zzu.kaoyan.module.interact.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final ForumCommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentPublishVO publishComment(Long postId, Long replyToId, String content) {
        Long userId = StpUtil.getLoginIdAsLong();

        if (replyToId != null) {
            ForumComment replyComment = commentMapper.selectById(replyToId);
            if (replyComment == null || Objects.equals(replyComment.getIsDeleted(), 1)) {
                throw new RuntimeException("无法回复：目标评论已被删除或不存在");
            }
        }

        // 敏感词过滤
        SensitiveWordUtil.FilterResult result = SensitiveWordUtil.filter(content);
        if (result.isHasSensitive()) {
            log.warn("评论包含敏感词 — userId={}, matched={}", userId, result.getMatched());
        }

        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setReplyToId(replyToId);
        comment.setContent(result.getFilteredText());
        comment.setIsDeleted(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(comment);

        User user = userMapper.selectById(userId);
        CommentPublishVO vo = new CommentPublishVO();
        vo.setCommentId(comment.getId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setAvatarUrl(user.getAvatarUrl());
        }
        return vo;
    }

    @Override
    public List<CommentDTO> getCommentTree(Long postId) {
        List<ForumComment> allComments = commentMapper.selectList(
                new LambdaQueryWrapper<ForumComment>()
                        .eq(ForumComment::getPostId, postId)
                        .eq(ForumComment::getIsDeleted, 0)
                        .orderByAsc(ForumComment::getCreatedAt)
        );

        if (allComments.isEmpty()) return new ArrayList<>();

        // 批量准备用户信息
        List<Long> userIds = allComments.stream().map(ForumComment::getUserId).distinct().toList();
        List<User> userList = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = new HashMap<>();
        if (userList != null) {
            userList.forEach(u -> userMap.put(u.getId(), u));
        }

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

            User u = userMap.get(comment.getUserId());
            dto.setUsername(u != null ? u.getUsername() : "用户已注销");
            dto.setAvatarUrl(u != null ? u.getAvatarUrl() : null);

            dto.setChildren(new ArrayList<>());

            if (comment.getReplyToId() == null) {
                rootComments.add(dto);
            } else {
                childrenMap.computeIfAbsent(comment.getReplyToId(), k -> new ArrayList<>()).add(dto);
            }
        }

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
        return commentMapper.selectList(new LambdaQueryWrapper<ForumComment>()
                .eq(ForumComment::getPostId, postId).eq(ForumComment::getIsDeleted, 0));
    }
}