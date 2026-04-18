package com.zzu.kaoyan.module.message.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.message.dto.MessageContactVO;
import com.zzu.kaoyan.module.message.dto.MessageConversationVO;
import com.zzu.kaoyan.module.message.dto.MessageSendDTO;
import com.zzu.kaoyan.module.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    private Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    @PostMapping("/send")
    @Operation(summary = "发送私信")
    @SaCheckLogin
    public Result<String> sendMessage(@Valid @RequestBody MessageSendDTO sendDTO) {
        Long currentUserId = getCurrentUserId();
        boolean success = messageService.sendMessage(currentUserId, sendDTO);
        return success ? Result.success("发送成功") : Result.error("发送失败");
    }

    @GetMapping("/conversation")
    @Operation(summary = "获取与某用户的聊天记录")
    @SaCheckLogin
    public Result<List<MessageConversationVO>> getConversation(@RequestParam Long otherUserId) {
        Long currentUserId = getCurrentUserId();
        List<MessageConversationVO> messages = messageService.getConversation(currentUserId, otherUserId);
        return Result.success(messages);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读私信数量")
    @SaCheckLogin
    public Result<Long> getUnreadCount() {
        Long currentUserId = getCurrentUserId();
        Long count = messageService.getUnreadCount(currentUserId);
        return Result.success(count);
    }

    @PutMapping("/read/{messageId}")
    @Operation(summary = "标记私信为已读")
    @SaCheckLogin
    public Result<String> markAsRead(@PathVariable Long messageId) {
        Long currentUserId = getCurrentUserId();
        boolean success = messageService.markAsRead(messageId, currentUserId);
        return success ? Result.success("已标记为已读") : Result.error("操作失败");
    }

    @GetMapping("/contacts")
    @Operation(summary = "获取私信联系人列表", description = "返回当前用户最近聊过天的联系人列表")
    @SaCheckLogin
    public Result<List<MessageContactVO>> getContactList() {
        Long currentUserId = getCurrentUserId();
        List<MessageContactVO> contacts = messageService.getContactList(currentUserId);
        return Result.success(contacts);
    }
}