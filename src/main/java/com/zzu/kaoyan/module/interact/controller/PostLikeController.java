package com.zzu.kaoyan.module.interact.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.interact.service.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interact/post")
@RequiredArgsConstructor
@Tag(name = "互动模块-帖子点赞接口")
public class PostLikeController {

    private final PostLikeService likeService;

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/like/{postId}")
    public Result<Boolean> toggleLike(@PathVariable Long postId) {
        boolean result = likeService.toggleLike(postId);
        return Result.success(result);
    }

    @Operation(summary = "获取当前用户对帖子的点赞状态")
    @GetMapping("/status")
    public Result<Boolean> getLikeStatus(@RequestParam Long postId) {
        // 1. 获取当前登录状态
        // 如果未登录，前端拿到 false 即可，不进行高亮
        if (!StpUtil.isLogin()) {
            return Result.success(false);
        }

        Long userId = StpUtil.getLoginIdAsLong();

        // 2. 调用 Service 查询状态 (统一使用已注入的变量名 likeService)
        boolean isLiked = likeService.checkIsLiked(userId, postId);

        return Result.success(isLiked);
    }
}