package com.zzu.kaoyan.module.post.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.post.dto.PostDTO;
import com.zzu.kaoyan.module.post.service.PostService;
import com.zzu.kaoyan.module.post.vo.PostDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "帖子管理", description = "发布帖子、获取帖子详情、分页查询帖子")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // ===================== 1. 分页接口（放最前面，防止冲突） =====================
    @Operation(summary = "分页查询所有板块帖子")
    @GetMapping("/page")
    public Result<PageInfo<PostDetailVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(postService.page(pageNum, pageSize));
    }

    // ===================== 2. 发布帖子（需登录） =====================
    @Operation(summary = "发布帖子")
    @PostMapping
    @SaCheckLogin
    public Result<Long> createPost(@Validated @RequestBody PostDTO postDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(postService.createPost(postDTO, userId));
    }

    // ===================== 3. 帖子详情（放最后 + 正则，彻底解决报错） =====================
    @Operation(summary = "获取帖子详情")
    @GetMapping("/{postId:\\d+}")
    public Result<PostDetailVO> getPostDetail(@PathVariable Long postId) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return Result.success(postService.getPostDetail(postId, userId));
    }

    @Operation(summary = "根据板块ID分页查询帖子")
    @GetMapping("/board/{boardId}")
    public Result<PageInfo<PostDetailVO>> getPostsByBoardId(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(postService.getPostsByBoardId(boardId, pageNum, pageSize));
    }


}