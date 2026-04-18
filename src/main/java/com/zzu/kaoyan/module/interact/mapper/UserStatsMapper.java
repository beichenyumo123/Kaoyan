package com.zzu.kaoyan.module.interact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.interact.entity.UserStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {

    @Update("UPDATE sys_user_stats SET like_received_count = like_received_count + #{delta} WHERE user_id = #{userId}")
    int updateLikeReceivedCount(@Param("userId") Long userId, @Param("delta") int delta);

    @Update("UPDATE sys_user_stats SET post_count = post_count + #{delta} WHERE user_id = #{userId}")
    int updatePostCount(@Param("userId") Long userId, @Param("delta") int delta);
}