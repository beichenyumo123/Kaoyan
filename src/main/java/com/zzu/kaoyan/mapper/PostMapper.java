package com.zzu.kaoyan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.module.post.entity.Post;
import com.zzu.kaoyan.module.post.vo.PostDetailVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 更新帖子浏览量 +1
     */
    @Update("UPDATE forum_post SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = 0")
    int updateViewCount(@Param("id") Long id);

    /**
     * 更新帖子点赞数（点赞/取消点赞）
     * @param id 帖子ID
     * @param delta 增量 (+1 点赞, -1 取消点赞)
     */
    @Update("UPDATE forum_post SET like_count = like_count + #{delta} WHERE id = #{id} AND is_deleted = 0")
    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    @Select("SELECT p.*, u.id as author_user_id, u.username as author_username, u.avatar_url as author_avatar_url " +
            "FROM forum_post p " +
            "INNER JOIN forum_post_collect c ON p.id = c.post_id " +
            "LEFT JOIN sys_user u ON p.user_id = u.id " +
            "WHERE c.user_id = #{userId} AND p.is_deleted = 0 " +
            "ORDER BY c.created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "author.userId", column = "author_user_id"),
            @Result(property = "author.username", column = "author_username"),
            @Result(property = "author.avatarUrl", column = "author_avatar_url")
    })
    List<PostDetailVO> selectCollectedPostsByUserId(@Param("userId") Long userId);
}