package com.zzu.kaoyan.module.interact.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.interact.service.PostCollectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子收藏控制器
 */
@RestController
@RequestMapping("/api/interact/collect")
@RequiredArgsConstructor
@Tag(name = "互动模块-帖子收藏", description = "处理用户收藏/取消收藏帖子的逻辑")
public class PostCollectController {

    private final PostCollectService postCollectService;

    @Operation(summary = "收藏或取消收藏", description = "已收藏则取消，未收藏则新增。返回 true 表示收藏成功，false 表示取消收藏。")
    @PostMapping("/{postId}")
    @SaCheckLogin
    public Result<Boolean> toggleCollect(@PathVariable Long postId) {
        // 调用 Service 层处理收藏逻辑
        boolean result = postCollectService.toggleCollect(postId);
        return Result.success(result);
    }

    @Operation(summary = "查询当前用户是否收藏该帖子")
    @GetMapping("/status/{postId}")
    @SaCheckLogin
    public Result<Boolean> getCollectStatus(@PathVariable Long postId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isCollected = postCollectService.isCollected(userId, postId);
        return Result.success(isCollected);
    }
}