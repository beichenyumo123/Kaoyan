package com.zzu.kaoyan.module.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.interview.entity.InterviewRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试对话明细 Mapper 接口
 */
@Mapper
public interface InterviewRecordMapper extends BaseMapper<InterviewRecord> {
}
