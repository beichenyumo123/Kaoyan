package com.zzu.kaoyan.module.mistake.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.mistake.entity.po.MistakeNotificationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MistakeNotificationMapper extends BaseMapper<MistakeNotificationPO> {

    @Select("SELECT COUNT(*) FROM mistake_notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);
}
