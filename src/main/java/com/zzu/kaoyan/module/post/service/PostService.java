package com.zzu.kaoyan.module.post.service;


import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.module.post.dto.PostDTO;
import com.zzu.kaoyan.module.post.vo.PostDetailVO;

public interface PostService {
    Long createPost(PostDTO postDTO, Long userId);
    PostDetailVO getPostDetail(Long postId, Long userId);
    PageInfo<PostDetailVO> page(Integer pageNum, Integer pageSize);
    PageInfo<PostDetailVO> getPostsByBoardId(Long boardId, Integer pageNum, Integer pageSize);

    /**
     * 统计指定用户发布的帖子总数
     * @param userId 用户ID
     * @return 帖子总数量
     */
    Long countUserPost(Long userId);

    /**
     * 分页查询指定用户发布的所有帖子
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 帖子分页列表
     */
    PageInfo<PostDetailVO> listUserPost(Long userId, Integer pageNum, Integer pageSize);
}