package com.zzu.kaoyan.module.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.interview.entity.InterviewReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试评估报告 Mapper 接口
 */
@Mapper
public interface InterviewReportMapper extends BaseMapper<InterviewReport> {
}
