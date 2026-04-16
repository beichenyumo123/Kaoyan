package com.zzu.kaoyan.module.post.service;


import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.module.post.dto.PostDTO;
import com.zzu.kaoyan.module.post.vo.PostDetailVO;

public interface PostService {
    Long createPost(PostDTO postDTO, Long userId);
    PostDetailVO getPostDetail(Long postId, Long userId);
    PageInfo<PostDetailVO> page(Integer pageNum, Integer pageSize);
}