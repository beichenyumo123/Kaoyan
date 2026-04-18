package com.zzu.kaoyan.module.interact.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.interact.entity.ForumPostLike;
import com.zzu.kaoyan.module.interact.entity.UserStats;
import com.zzu.kaoyan.module.post.entity.Post;
import com.zzu.kaoyan.module.interact.mapper.ForumPostLikeMapper;
import com.zzu.kaoyan.mapper.PostMapper;
import com.zzu.kaoyan.module.interact.mapper.UserStatsMapper;
import com.zzu.kaoyan.module.interact.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Primary
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final ForumPostLikeMapper likeMapper;
    private final PostMapper postMapper;
    private final UserStatsMapper userStatsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long postId) {
        // 1. 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 2. 获取帖子信息，确定贴主（作者）是谁
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        Long authorId = post.getUserId();

        // 3. 查询是否已点赞
        boolean hasLiked = likeMapper.exists(new LambdaQueryWrapper<ForumPostLike>()
                .eq(ForumPostLike::getPostId, postId)
                .eq(ForumPostLike::getUserId, userId));

        if (hasLiked) {
            // 4. 执行取消点赞流程
            likeMapper.delete(new LambdaQueryWrapper<ForumPostLike>()
                    .eq(ForumPostLike::getPostId, postId)
                    .eq(ForumPostLike::getUserId, userId));

            // 4.1 帖子点赞数 -1
            postMapper.updateLikeCount(postId, -1);

            // 4.2 贴主总获赞数 -1 (带自动检查插入逻辑)
            this.incrUserLikeCount(authorId, -1);

            return false;
        } else {
            // 5. 执行点赞流程
            ForumPostLike like = new ForumPostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            like.setCreatedAt(LocalDateTime.now());
            likeMapper.insert(like);

            // 5.1 帖子点赞数 +1
            postMapper.updateLikeCount(postId, 1);

            // 5.2 贴主总获赞数 +1 (带自动检查插入逻辑)
            this.incrUserLikeCount(authorId, 1);

            return true;
        }
    }

    /**
     * 原子增加/减少用户总获赞数
     * 如果用户在统计表中无记录，则自动创建
     */
    private void incrUserLikeCount(Long userId, int delta) {
        // 1. 尝试直接更新
        int affectedRows = userStatsMapper.updateLikeReceivedCount(userId, delta);

        // 2. 如果影响行数为 0，说明统计表里还没该用户的记录
        if (affectedRows == 0) {
            UserStats stats = new UserStats();
            stats.setUserId(userId);
            // 初始获赞数：如果是点赞则为1，如果是取消点赞则至少为0
            stats.setLikeReceivedCount(Math.max(0, delta));
            stats.setPostCount(0); // 初始发帖数设为 0，等待发帖逻辑触发更新
            userStatsMapper.insert(stats);
        }
    }

    @Override
    public boolean checkIsLiked(Long userId, Long postId) {
        Long count = likeMapper.selectCount(
                new LambdaQueryWrapper<ForumPostLike>()
                        .eq(ForumPostLike::getPostId, postId)
                        .eq(ForumPostLike::getUserId, userId)
        );
        return count > 0;
    }
}