package com.zzu.kaoyan.module.interact.service.impl;

import com.zzu.kaoyan.module.interact.entity.UserStats;
import com.zzu.kaoyan.module.interact.mapper.UserStatsMapper;
import com.zzu.kaoyan.module.interact.service.UserStatsService;
import com.zzu.kaoyan.module.interact.entity.vo.UserStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements UserStatsService {

    private final UserStatsMapper userStatsMapper;

    @Override
    public UserStatsVO getUserStats(Long userId) {
        // 1. 从数据库查询统计记录
        UserStats stats = userStatsMapper.selectById(userId);
        
        UserStatsVO vo = new UserStatsVO();
        vo.setUserId(userId);
        
        // 2. 逻辑处理：如果数据库没有记录（新用户），则返回全 0，不报错
        if (stats != null) {
            BeanUtils.copyProperties(stats, vo);
        } else {
            vo.setPostCount(0);
            vo.setLikeReceivedCount(0);
        }
        
        return vo;
    }
}