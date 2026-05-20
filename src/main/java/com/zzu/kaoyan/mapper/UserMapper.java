package com.zzu.kaoyan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.common.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    void addPoints(@Param("userId") Long userId, @Param("points") int points);

    Integer getPointsById(@Param("userId") Long userId);

    List<Map<String, Object>> selectRankList();

    List<Map<String, Object>> selectTopActiveUsers(@Param("limit") int limit);
}
