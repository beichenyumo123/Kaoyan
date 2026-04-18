package com.zzu.kaoyan.module.interact.service;

public interface PostLikeService {
    /**
     * 点赞/取消点赞切换
     * @param postId 帖子ID
     * @return true=点赞成功, false=取消点赞成功
     */
    boolean toggleLike(Long postId);

    boolean checkIsLiked(Long userId, Long postId);
}