package com.zzu.kaoyan.module.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.activity.entity.po.CheckInPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;

public interface CheckInMapper extends BaseMapper<CheckInPO> {
    @Select("SELECT COUNT(*) > 0 FROM interaction_check_in WHERE user_id = #{userId} AND created_date = #{date}")
    boolean existsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}