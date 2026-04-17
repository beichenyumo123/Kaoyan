package com.zzu.kaoyan.module.interact.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.interact.entity.ForumComment;
import com.zzu.kaoyan.module.interact.entity.dto.CommentDTO;
import com.zzu.kaoyan.module.interact.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interact/comment")
@RequiredArgsConstructor
@Tag(name = "互动模块-评论接口")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/publish/{postId}")
    @Operation(summary = "发布评论/回复", description = "replyToId为null表示发布顶层评论，不为null表示回复评论")
    public Result<Long> publishComment(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "回复的评论ID，可为null") @RequestParam(required = false) Long replyToId,
            @Parameter(description = "评论内容") @RequestParam String content) {
        Long commentId = commentService.publishComment(postId, replyToId, content);
        return Result.success(commentId);
    }

    @GetMapping("/tree/{postId}")
    @Operation(summary = "获取帖子评论列表（楼中楼结构）")
    public Result<List<CommentDTO>> getCommentTree(@PathVariable Long postId) {
        return Result.success(commentService.getCommentTree(postId));
    }

    @GetMapping("/list/{postId}")
    @Operation(summary = "获取帖子评论列表（平铺结构）")
    public Result<List<ForumComment>> getCommentList(@PathVariable Long postId) {
        return Result.success(commentService.getCommentList(postId));
    }
}