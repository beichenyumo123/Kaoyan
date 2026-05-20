package com.zzu.kaoyan.module.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.interview.entity.InterviewSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试会话 Mapper 接口
 */
@Mapper
public interface InterviewSessionMapper extends BaseMapper<InterviewSession> {
}
