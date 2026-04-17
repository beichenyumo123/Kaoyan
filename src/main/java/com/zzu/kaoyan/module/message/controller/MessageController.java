package com.zzu.kaoyan.module.message.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.mapper.UserMapper;
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
    private final UserMapper userMapper;

    public MessageController(MessageService messageService, UserMapper userMapper) {
        this.messageService = messageService;
        this.userMapper = userMapper;
    }

    /**
     * 获取当前登录用户的正确ID
     * 通过 token 对应的登录ID，从数据库查询真实用户
     */
    private Long getCurrentUserId() {
        Long tokenUserId = StpUtil.getLoginIdAsLong();

        // 尝试用这个 ID 查询用户
        User user = userMapper.selectById(tokenUserId);

        if (user != null) {
            // ID 正确，直接返回
            return tokenUserId;
        }

        // 如果查不到，说明 token 里存的 ID 不是用户表的主键
        // 需要通过其他方式获取，这里打印日志方便排查
        System.out.println("警告: token中的用户ID " + tokenUserId + " 在数据库中不存在");
        System.out.println("当前登录账号: " + StpUtil.getLoginId());

        // 临时方案：返回 tokenUserId（等待鉴权模块修复）
        return tokenUserId;
    }

    @PostMapping("/send")
    @Operation(summary = "发送私信")
    @SaCheckLogin
    public Result<String> sendMessage(@RequestParam Long toUserId, @RequestParam String content) {
        Long currentUserId = getCurrentUserId();
        boolean success = messageService.sendMessage(currentUserId, toUserId, content);
        return success ? Result.success("发送成功") : Result.error("发送失败");
    }

    @GetMapping("/conversation")
    @Operation(summary = "获取与某用户的聊天记录")
    @SaCheckLogin
    public Result<List<Message>> getConversation(@RequestParam Long otherUserId) {
        Long currentUserId = getCurrentUserId();
        List<Message> messages = messageService.getConversation(currentUserId, otherUserId);
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
}