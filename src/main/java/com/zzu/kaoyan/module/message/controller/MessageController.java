package com.zzu.kaoyan.module.message.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.message.entity.Message;
import com.zzu.kaoyan.module.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@Tag(name = "私信模块")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    @Operation(summary = "发送私信")
    @SaCheckLogin
    public Result<String> sendMessage(@RequestParam Long toUserId, @RequestParam String content) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        boolean success = messageService.sendMessage(currentUserId, toUserId, content);
        return success ? Result.success("发送成功") : Result.error("发送失败");
    }

    @GetMapping("/conversation")
    @Operation(summary = "获取与某用户的聊天记录")
    @SaCheckLogin
    public Result<List<Message>> getConversation(@RequestParam Long otherUserId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<Message> messages = messageService.getConversation(currentUserId, otherUserId);
        return Result.success(messages);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读私信数量")
    @SaCheckLogin
    public Result<Long> getUnreadCount() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Long count = messageService.getUnreadCount(currentUserId);
        return Result.success(count);
    }

    @PutMapping("/read/{messageId}")
    @Operation(summary = "标记私信为已读")
    @SaCheckLogin
    public Result<String> markAsRead(@PathVariable Long messageId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        boolean success = messageService.markAsRead(messageId, currentUserId);
        return success ? Result.success("已标记为已读") : Result.error("操作失败");
    }
}