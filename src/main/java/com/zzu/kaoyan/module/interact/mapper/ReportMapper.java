package com.zzu.kaoyan.module.interact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.interact.entity.ForumReport;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportMapper extends BaseMapper<ForumReport> {
}