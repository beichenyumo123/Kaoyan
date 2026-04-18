package com.zzu.kaoyan.module.interact.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.interact.entity.ForumPostCollect;
import com.zzu.kaoyan.module.interact.mapper.ForumPostCollectMapper;
import com.zzu.kaoyan.module.interact.service.PostCollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostCollectServiceImpl implements PostCollectService {

    private final ForumPostCollectMapper collectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleCollect(Long postId) {
        // 1. 获取当前登录用户 ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 2. 构造查询条件
        LambdaQueryWrapper<ForumPostCollect> wrapper = new LambdaQueryWrapper<ForumPostCollect>()
                .eq(ForumPostCollect::getPostId, postId)
                .eq(ForumPostCollect::getUserId, userId);

        // 3. 判断是否已经收藏
        boolean hasCollected = collectMapper.exists(wrapper);

        if (hasCollected) {
            // 4. 如果已收藏，执行取消收藏操作
            collectMapper.delete(wrapper);
            return false;
        } else {
            // 5. 如果未收藏，执行新增收藏操作
            ForumPostCollect collect = new ForumPostCollect();
            collect.setPostId(postId);
            collect.setUserId(userId);
            collect.setCreatedAt(LocalDateTime.now());
            collectMapper.insert(collect);
            return true;
        }
    }

    @Override
    public boolean isCollected(Long userId, Long postId) {
        if (userId == null) return false;
        return collectMapper.exists(new LambdaQueryWrapper<ForumPostCollect>()
                .eq(ForumPostCollect::getPostId, postId)
                .eq(ForumPostCollect::getUserId, userId));
    }
}