package com.zzu.kaoyan.module.mistake.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.mistake.entity.po.DailyPlanPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DailyPlanMapper extends BaseMapper<DailyPlanPO> {

    @Select("SELECT DISTINCT user_id FROM mistake_note WHERE is_deleted = 0 AND next_review_date IS NOT NULL")
    List<Long> selectUsersWithPendingReviews();
}
