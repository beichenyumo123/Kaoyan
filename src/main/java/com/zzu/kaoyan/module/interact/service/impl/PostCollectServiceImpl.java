package com.zzu.kaoyan.module.interact.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.module.interact.entity.ForumPostCollect;
import com.zzu.kaoyan.module.interact.mapper.ForumPostCollectMapper;
import com.zzu.kaoyan.module.interact.service.PostCollectService;
import com.zzu.kaoyan.module.post.vo.PostDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zzu.kaoyan.mapper.PostMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCollectServiceImpl implements PostCollectService {

    private final ForumPostCollectMapper collectMapper;
    private final PostMapper postMapper;

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

    // 在 PostCollectServiceImpl.java 中实现
    @Override
    public PageInfo<PostDetailVO> getUserCollectedPosts(Long userId, Integer pageNum, Integer pageSize) {
        // 1. 开启分页
        PageHelper.startPage(pageNum, pageSize);

        // 2. 这里的逻辑通常有两种：
        // 方案 A：在 PostMapper 中写一个专门的 Join SQL（性能最好）
        // 方案 B：先查出收藏的 post_id 列表，再通过 postService 查详情（开发快）

        // 推荐方案 A，我们需要在 PostMapper 中自定义查询
        List<PostDetailVO> list = postMapper.selectCollectedPostsByUserId(userId);

        return new PageInfo<>(list);
    }
}