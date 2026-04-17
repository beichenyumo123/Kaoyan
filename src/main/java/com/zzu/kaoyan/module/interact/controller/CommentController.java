package com.zzu.kaoyan.module.interact.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.interact.entity.ForumComment;
import com.zzu.kaoyan.module.interact.entity.dto.CommentDTO;
import com.zzu.kaoyan.module.interact.entity.dto.CommentPublishDTO; // 注意导入新建的 DTO
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

    // 💡 只有这里改了：换成了 @RequestBody 接收 DTO
    @PostMapping("/publish/{postId}")
    @Operation(summary = "发布评论/回复", description = "前端请传入JSON，replyToId为null表示发布顶层评论，不为null表示回复评论")
    public Result<Long> publishComment(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @RequestBody CommentPublishDTO dto) { // 替换为 @RequestBody
            
        // 从 DTO 中提取参数，直接调用你原来的 Service 方法，这样 Service 层一行代码都不用改！
        Long commentId = commentService.publishComment(postId, dto.getReplyToId(), dto.getContent());
        
        return Result.success(commentId);
    }

    // 👇 下面这两个 Get 查询接口完美契合 RESTful 规范，千万别动！
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