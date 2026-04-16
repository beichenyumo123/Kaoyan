package com.zzu.kaoyan.module.post.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.mapper.PostMapper;
import com.zzu.kaoyan.module.post.dto.PostDTO;
import com.zzu.kaoyan.module.post.entity.Post;
import com.zzu.kaoyan.module.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "帖子接口")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper; // 注入PostMapper

    @Operation(summary = "发布帖子")
    @PostMapping
    public Result<Long> publish(@RequestBody PostDTO dto) {
        Post post = new Post();
        post.setBoardId(dto.getBoardId());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setUserId(1L);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsDeleted(0);

        boolean success = postService.save(post);
        return success ? Result.success(post.getId())
                : Result.error(ResultCode.SYSTEM_ERROR.getCode(), "发布失败");
    }

    @Operation(summary = "分页查询帖子")
    @GetMapping("/page")
    public Result<List<Post>> page(@RequestParam(required = false) Long boardId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getIsDeleted, 0);
        if (boardId != null) {
            wrapper.eq(Post::getBoardId, boardId);
        }
        wrapper.orderByDesc(Post::getCreatedAt);

        return Result.success(postService.list(wrapper));
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/{id}")
    public Result<Post> detail(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null || post.getIsDeleted() == 1) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "帖子不存在");
        }

        // 正确调用：直接注入PostMapper，调用自定义方法
        postMapper.updateViewCount(id);
        return Result.success(post);
    }
}