package com.zzu.kaoyan.module.post.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zzu.kaoyan.module.post.entity.Post;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.mapper.AuthMapper;
import com.zzu.kaoyan.mapper.PostMapper;
import com.zzu.kaoyan.module.post.dto.PostDTO;
import com.zzu.kaoyan.module.post.service.PostService;
import com.zzu.kaoyan.module.post.vo.PostDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final AuthMapper authMapper;

    public PostServiceImpl(PostMapper postMapper, AuthMapper authMapper) {
        this.postMapper = postMapper;
        this.authMapper = authMapper;
    }

    // ===================== 发布帖子 =====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(PostDTO postDTO, Long userId) {
        Post post = new Post();
        BeanUtils.copyProperties(postDTO, post);

        post.setUserId(userId);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsDeleted(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        postMapper.insert(post);
        return post.getId();
    }

    // ===================== 帖子详情 =====================
    @Override
    public PostDetailVO getPostDetail(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException(404, "帖子不存在");
        }

        LambdaUpdateWrapper<Post> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Post::getId, postId).setSql("view_count = view_count + 1");
        postMapper.update(null, wrapper);

        User author = authMapper.selectById(post.getUserId());
        if (author == null) {
            throw new BusinessException(500, "作者信息异常");
        }

        PostDetailVO vo = new PostDetailVO();
        BeanUtils.copyProperties(post, vo);

        PostDetailVO.AuthorVO authorVO = new PostDetailVO.AuthorVO();
        authorVO.setUserId(author.getId());
        authorVO.setUsername(author.getUsername());
        authorVO.setAvatarUrl(author.getAvatarUrl());
        vo.setAuthor(authorVO);

        vo.setIsLiked(false);
        return vo;
    }

    // ===================== 分页查询 =====================
    @Override
    public PageInfo<PostDetailVO> page(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Post> postList = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getIsDeleted, 0)
                .orderByDesc(Post::getCreatedAt));

        PageInfo<Post> pageInfo = new PageInfo<>(postList);

        List<PostDetailVO> voList = pageInfo.getList().stream().map(post -> {
            PostDetailVO vo = new PostDetailVO();
            BeanUtils.copyProperties(post, vo);

            User author = authMapper.selectById(post.getUserId());
            if (author != null) {
                PostDetailVO.AuthorVO authorVO = new PostDetailVO.AuthorVO();
                authorVO.setUserId(author.getId());
                authorVO.setUsername(author.getUsername());
                authorVO.setAvatarUrl(author.getAvatarUrl());
                vo.setAuthor(authorVO);
            }

            vo.setIsLiked(false);
            return vo;
        }).collect(Collectors.toList());

        PageInfo<PostDetailVO> result = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, result);
        result.setList(voList);
        return result;
    }
}