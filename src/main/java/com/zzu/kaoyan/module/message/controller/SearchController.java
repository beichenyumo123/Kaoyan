package com.zzu.kaoyan.module.message.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.post.entity.Post;
import com.zzu.kaoyan.module.post.mapper.PostMapper;
import com.zzu.kaoyan.module.message.dto.SearchResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "搜索模块")
public class SearchController {

    private final PostMapper postMapper;
    private final UserMapper userMapper;

    public SearchController(PostMapper postMapper, UserMapper userMapper) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    @Operation(summary = "全局搜索", description = "根据关键字搜索帖子和用户")
    @SaCheckLogin
    public Result<SearchResultVO> search(@RequestParam String keyword) {
        SearchResultVO result = new SearchResultVO();

        // 1. 搜索帖子（标题或内容包含关键字）
        LambdaQueryWrapper<Post> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.and(w -> w
                .like(Post::getTitle, keyword)
                .or()
                .like(Post::getContent, keyword)
        ).eq(Post::getIsDeleted, 0);
        List<Post> posts = postMapper.selectList(postWrapper);
        result.setPosts(posts);

        // 2. 搜索用户（用户名包含关键字）
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.like(User::getUsername, keyword);
        List<User> users = userMapper.selectList(userWrapper);
        result.setUsers(users);

        return Result.success(result);
    }
}