package com.zzu.kaoyan.module.post.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zzu.kaoyan.mapper.LikeMapper;
import com.zzu.kaoyan.module.post.entity.Like;
import com.zzu.kaoyan.module.post.entity.Post;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.util.SensitiveWordUtil;
import com.zzu.kaoyan.mapper.AuthMapper;
import com.zzu.kaoyan.mapper.PostMapper;
import com.zzu.kaoyan.module.post.dto.PostDTO;
import com.zzu.kaoyan.module.post.service.PostService;
import com.zzu.kaoyan.module.post.vo.PostDetailVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class PostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostMapper postMapper;
    private final AuthMapper authMapper;
    private final LikeMapper likeMapper;
    private final RedisTemplate<String, Object> redisTemplate;


    public PostServiceImpl(PostMapper postMapper, AuthMapper authMapper, LikeMapper likeMapper,
                           RedisTemplate<String, Object> redisTemplate) {
        this.postMapper = postMapper;
        this.authMapper = authMapper;
        this.likeMapper = likeMapper;
        this.redisTemplate = redisTemplate;
    }

    // ===================== 发布帖子 =====================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(PostDTO postDTO, Long userId) {
        Post post = new Post();
        BeanUtils.copyProperties(postDTO, post);

        // 敏感词过滤
        SensitiveWordUtil.FilterResult titleResult = SensitiveWordUtil.filter(post.getTitle());
        if (titleResult.isHasSensitive()) {
            log.warn("帖子标题包含敏感词 — userId={}, matched={}", userId, titleResult.getMatched());
            post.setTitle(titleResult.getFilteredText());
        }
        SensitiveWordUtil.FilterResult contentResult = SensitiveWordUtil.filter(post.getContent());
        if (contentResult.isHasSensitive()) {
            log.warn("帖子内容包含敏感词 — userId={}, matched={}", userId, contentResult.getMatched());
            post.setContent(contentResult.getFilteredText());
        }

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

        PostDetailVO vo = new PostDetailVO();
        BeanUtils.copyProperties(post, vo);

        // ===================== 加固开始 =====================
        User author = null;
        if (post.getUserId() != null && post.getUserId() > 0) {
            author = authMapper.selectById(post.getUserId());
        }

        PostDetailVO.AuthorVO authorVO = new PostDetailVO.AuthorVO();
        if (author != null) {
            authorVO.setUserId(author.getId());
            authorVO.setUsername(author.getUsername() == null ? "匿名用户" : author.getUsername());
            authorVO.setAvatarUrl(author.getAvatarUrl());
        } else {
            authorVO.setUserId(0L);
            authorVO.setUsername("匿名/已注销用户");
            authorVO.setAvatarUrl("");
        }
        vo.setAuthor(authorVO);
        // ===================== 加固结束 =====================

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


    @Override
    public PageInfo<PostDetailVO> getPostsByBoardId(Long boardId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getBoardId, boardId)
                .eq(Post::getIsDeleted, 0)
                .orderByDesc(Post::getCreatedAt);

        List<Post> postList = postMapper.selectList(wrapper);
        PageInfo<Post> pageInfo = new PageInfo<>(postList);

        List<PostDetailVO> voList = pageInfo.getList().stream().map(post -> {
            PostDetailVO vo = new PostDetailVO();
            BeanUtils.copyProperties(post, vo);

            // 脏数据加固
            User author = null;
            if (post.getUserId() != null && post.getUserId() > 0) {
                author = authMapper.selectById(post.getUserId());
            }

            PostDetailVO.AuthorVO authorVO = new PostDetailVO.AuthorVO();
            if (author != null) {
                authorVO.setUserId(author.getId());
                authorVO.setUsername(author.getUsername() == null ? "匿名用户" : author.getUsername());
                authorVO.setAvatarUrl(author.getAvatarUrl());
            } else {
                authorVO.setUserId(0L);
                authorVO.setUsername("匿名/已注销用户");
                authorVO.setAvatarUrl("");
            }
            vo.setAuthor(authorVO);

            // 点赞状态（默认false，不报错）
            vo.setIsLiked(false);
            return vo;
        }).collect(Collectors.toList());

        PageInfo<PostDetailVO> result = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, result);
        result.setList(voList);
        return result;
    }

    /*统计帖子总数实现*/
    @Override
    public Long countUserPost(Long userId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getIsDeleted, 0);
        // MyBatis-Plus 自带count，高效统计
        return postMapper.selectCount(wrapper);
    }

    /*用户发帖分页列表实现（全套加固）*/
    @Override
    public PageInfo<PostDetailVO> listUserPost(Long userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        // 查询条件：指定用户 + 未删除 + 按发布时间最新倒序
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getIsDeleted, 0)
                .orderByDesc(Post::getCreatedAt);

        List<Post> postList = postMapper.selectList(wrapper);
        PageInfo<Post> pageInfo = new PageInfo<>(postList);

        // 封装VO，沿用你全部历史加固逻辑
        List<PostDetailVO> voList = pageInfo.getList().stream().map(post -> {
            PostDetailVO vo = new PostDetailVO();
            BeanUtils.copyProperties(post, vo);

            // ========== 作者信息脏数据兜底（解决之前500崩溃问题） ==========
            User author = null;
            if (post.getUserId() != null && post.getUserId() > 0) {
                author = authMapper.selectById(post.getUserId());
            }
            PostDetailVO.AuthorVO authorVO = new PostDetailVO.AuthorVO();
            if (author != null) {
                authorVO.setUserId(author.getId());
                authorVO.setUsername(author.getUsername() == null ? "匿名用户" : author.getUsername());
                authorVO.setAvatarUrl(author.getAvatarUrl());
            } else {
                authorVO.setUserId(0L);
                authorVO.setUsername("匿名/已注销用户");
                authorVO.setAvatarUrl("");
            }
            vo.setAuthor(authorVO);

            // ========== 点赞状态动态查询（不再写死false，带异常兜底） ==========
            boolean isLiked = false;
            Long currentLoginUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
            if (currentLoginUserId != null && currentLoginUserId > 0) {
                try {
                    Long count = likeMapper.selectCount(
                            new LambdaQueryWrapper<Like>()
                                    .eq(Like::getPostId, post.getId())
                                    .eq(Like::getUserId, currentLoginUserId)
                    );
                    isLiked = count != null && count > 0;
                } catch (Exception ignored) {
                    isLiked = false;
                }
            }
            vo.setIsLiked(isLiked);

            return vo;
        }).collect(Collectors.toList());

        PageInfo<PostDetailVO> result = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, result);
        result.setList(voList);
        return result;
    }

    // ===================== 热门推荐 =====================
    @Override
    public PageInfo<PostDetailVO> getHotPosts(int pageNum, int pageSize) {
        try {
            Long total = redisTemplate.opsForZSet().zCard("hot:posts");
            if (total == null || total == 0) {
                return new PageInfo<>();
            }

            int start = (pageNum - 1) * pageSize;
            int end = start + pageSize - 1;

            Set<ZSetOperations.TypedTuple<Object>> rangeWithScores =
                    redisTemplate.opsForZSet().reverseRangeWithScores("hot:posts", start, end);

            if (rangeWithScores == null || rangeWithScores.isEmpty()) {
                return new PageInfo<>();
            }

            List<Long> postIds = rangeWithScores.stream()
                    .map(t -> toLong(t.getValue()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (postIds.isEmpty()) {
                return new PageInfo<>();
            }

            List<Post> posts = postMapper.selectBatchIds(postIds);
            Map<Long, Post> postMap = posts.stream()
                    .filter(p -> p.getIsDeleted() == 0)
                    .collect(Collectors.toMap(Post::getId, p -> p, (a, b) -> a, LinkedHashMap::new));

            List<PostDetailVO> voList = new ArrayList<>();
            for (Long postId : postIds) {
                Post post = postMap.get(postId);
                if (post == null) continue;
                voList.add(toPostDetailVO(post));
            }

            PageInfo<PostDetailVO> result = new PageInfo<>();
            result.setTotal(total);
            result.setPageNum(pageNum);
            result.setPageSize(pageSize);
            result.setPages((int) Math.ceil((double) total / pageSize));
            result.setList(voList);
            result.setSize(voList.size());
            result.setHasNextPage((long) pageNum * pageSize < total);
            result.setHasPreviousPage(pageNum > 1);
            return result;
        } catch (Exception e) {
            log.error("获取热门帖子失败", e);
            return new PageInfo<>();
        }
    }

    private PostDetailVO toPostDetailVO(Post post) {
        PostDetailVO vo = new PostDetailVO();
        BeanUtils.copyProperties(post, vo);

        User author = null;
        if (post.getUserId() != null && post.getUserId() > 0) {
            author = authMapper.selectById(post.getUserId());
        }

        PostDetailVO.AuthorVO authorVO = new PostDetailVO.AuthorVO();
        if (author != null) {
            authorVO.setUserId(author.getId());
            authorVO.setUsername(author.getUsername() == null ? "匿名用户" : author.getUsername());
            authorVO.setAvatarUrl(author.getAvatarUrl());
        } else {
            authorVO.setUserId(0L);
            authorVO.setUsername("匿名/已注销用户");
            authorVO.setAvatarUrl("");
        }
        vo.setAuthor(authorVO);

        boolean isLiked = false;
        Long currentLoginUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        if (currentLoginUserId != null && currentLoginUserId > 0) {
            try {
                Long count = likeMapper.selectCount(
                        new LambdaQueryWrapper<Like>()
                                .eq(Like::getPostId, post.getId())
                                .eq(Like::getUserId, currentLoginUserId));
                isLiked = count != null && count > 0;
            } catch (Exception ignored) {
                isLiked = false;
            }
        }
        vo.setIsLiked(isLiked);
        return vo;
    }

    private Long toLong(Object value) {
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }
}