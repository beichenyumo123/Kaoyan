package com.zzu.kaoyan.module.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.activity.entity.po.CheckInPO;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;

public interface CheckInMapper extends BaseMapper<CheckInPO> {
    boolean existsByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}