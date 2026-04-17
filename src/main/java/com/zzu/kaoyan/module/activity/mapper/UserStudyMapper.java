package com.zzu.kaoyan.module.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.activity.entity.po.UserStudyPO;
import org.apache.ibatis.annotations.Param;

public interface UserStudyMapper extends BaseMapper<UserStudyPO> {
    UserStudyPO selectByUserId(@Param("userId") Long userId);
}