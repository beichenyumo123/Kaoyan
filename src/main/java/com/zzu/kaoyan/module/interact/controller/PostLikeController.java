package com.zzu.kaoyan.module.interact.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.interact.service.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interact/post")
@RequiredArgsConstructor
@Tag(name = "互动模块-帖子点赞接口")
public class PostLikeController {

    private final PostLikeService likeService;

    @Operation(summary = "点赞/取消点赞")
    //@SaIgnore //临时测验接口，稍后注释
    @PostMapping("/like/{postId}")
    public Result<Boolean> toggleLike(@PathVariable Long postId) {
        boolean result = likeService.toggleLike(postId);
        return Result.success(result);
    }
}