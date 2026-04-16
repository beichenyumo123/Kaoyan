package com.zzu.kaoyan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// 必须和你现在的导入路径一致
import com.zzu.kaoyan.module.post.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}