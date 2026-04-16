package com.zzu.kaoyan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.post.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 更新帖子浏览量 +1
     */
    @Update("UPDATE forum_post SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = 0")
    int updateViewCount(@Param("id") Long id);
}