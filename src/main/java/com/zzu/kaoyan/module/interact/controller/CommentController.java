package com.zzu.kaoyan.module.interact.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.interact.entity.ForumComment;
import com.zzu.kaoyan.module.interact.entity.dto.CommentDTO;
import com.zzu.kaoyan.module.interact.entity.dto.CommentPublishDTO;
import com.zzu.kaoyan.module.interact.entity.vo.CommentPublishVO; // 必须导入 VO
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
    @Operation(summary = "发布评论/回复", description = "前端请传入JSON，replyToId为null表示发布顶层评论，不为null表示回复评论")
    public Result<CommentPublishVO> publishComment(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @RequestBody CommentPublishDTO dto) {

        // ✅ 修正点：使用 CommentPublishVO 接收 Service 的返回值
        // 因为你的 Service 实现类返回的是 CommentPublishVO 对象
        CommentPublishVO vo = commentService.publishComment(postId, dto.getReplyToId(), dto.getContent());

        // 返回完整的 VO 结果，这样前端发完评论能立刻拿到自己的用户名和头像进行渲染
        return Result.success(vo);
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