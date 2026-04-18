package com.zzu.kaoyan.module.interact.service;

/**
 * 帖子收藏服务接口
 */
public interface PostCollectService {
    /**
     * 切换收藏状态：已收藏则取消，未收藏则新增
     * @param postId 帖子ID
     * @return true: 已收藏, false: 取消收藏
     */
    boolean toggleCollect(Long postId);

    /**
     * 检查用户是否收藏了某个帖子
     */
    boolean isCollected(Long userId, Long postId);


}