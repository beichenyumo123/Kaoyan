package com.zzu.kaoyan.mapper; // ✅ 保持根目录mapper包

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.post.entity.Board;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardMapper extends BaseMapper<Board> {

}