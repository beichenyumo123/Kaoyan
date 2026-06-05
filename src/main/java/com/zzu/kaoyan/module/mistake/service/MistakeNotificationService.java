package com.zzu.kaoyan.module.mistake.service;

import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.module.mistake.entity.vo.MistakeNotificationVO;

public interface MistakeNotificationService {

    /**
     * 创建通知
     */
    void create(Long userId, String type, String title, String content);

    /**
     * 分页获取通知列表（按时间倒序）
     */
    PageInfo<MistakeNotificationVO> list(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取未读数量
     */
    int unreadCount(Long userId);

    /**
     * 标记单条已读
     */
    void markRead(Long notificationId, Long userId);

    /**
     * 标记全部已读
     */
    void markAllRead(Long userId);
}
