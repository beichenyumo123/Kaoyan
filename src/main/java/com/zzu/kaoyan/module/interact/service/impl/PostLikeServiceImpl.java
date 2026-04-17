package com.zzu.kaoyan.module.interact.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.interact.entity.ForumPostLike;
import com.zzu.kaoyan.module.interact.mapper.ForumPostLikeMapper;
import com.zzu.kaoyan.module.interact.service.PostLikeService;
import com.zzu.kaoyan.mapper.PostMapper;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long postId) {
        // 1. 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 2. 查询是否已点赞
        boolean hasLiked = likeMapper.exists(new LambdaQueryWrapper<ForumPostLike>()
                .eq(ForumPostLike::getPostId, postId)
                .eq(ForumPostLike::getUserId, userId));

        if (hasLiked) {
            // 3. 取消点赞
            likeMapper.delete(new LambdaQueryWrapper<ForumPostLike>()
                    .eq(ForumPostLike::getPostId, postId)
                    .eq(ForumPostLike::getUserId, userId));
            postMapper.updateLikeCount(postId, -1);
            return false;
        } else {
            // 4. 新增点赞
            ForumPostLike like = new ForumPostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            like.setCreatedAt(LocalDateTime.now());
            likeMapper.insert(like);
            postMapper.updateLikeCount(postId, 1);
            return true;
        }
    }

    @Override
    public boolean checkIsLiked(Long userId, Long postId) {
        // ✅ 修改点：使用类中已注入的 likeMapper 代替 baseMapper
        // ✅ 修改点：MyBatis-Plus 的 selectCount 返回类型建议使用 Long
        Long count = likeMapper.selectCount(
                new LambdaQueryWrapper<ForumPostLike>()
                        .eq(ForumPostLike::getPostId, postId)
                        .eq(ForumPostLike::getUserId, userId)
        );
        return count > 0;
    }
}