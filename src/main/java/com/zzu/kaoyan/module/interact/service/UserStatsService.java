package com.zzu.kaoyan.module.interact.service;

import com.zzu.kaoyan.module.interact.entity.vo.UserStatsVO;

public interface UserStatsService {
    /**
     * 根据用户ID获取获赞和发帖统计数据
     */
    UserStatsVO getUserStats(Long userId);
}