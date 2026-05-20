package com.zzu.kaoyan.module.chat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.chat.dto.CreateGroupDTO;
import com.zzu.kaoyan.module.chat.service.ChatGroupService;
import com.zzu.kaoyan.module.chat.service.GroupMessageService;
import com.zzu.kaoyan.module.chat.vo.ChatGroupVO;
import com.zzu.kaoyan.module.chat.vo.ChatMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/groups")
@Tag(name = "小组群聊", description = "群组管理与消息历史接口")
public class ChatGroupController {

    private final ChatGroupService chatGroupService;
    private final GroupMessageService groupMessageService;

    public ChatGroupController(ChatGroupService chatGroupService,
                               GroupMessageService groupMessageService) {
        this.chatGroupService = chatGroupService;
        this.groupMessageService = groupMessageService;
    }

    @Operation(summary = "创建群组")
    @PostMapping
    @SaCheckLogin
    public Result<Long> createGroup(@Valid @RequestBody CreateGroupDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(chatGroupService.createGroup(dto, userId));
    }

    @Operation(summary = "获取我的群组列表")
    @GetMapping
    @SaCheckLogin
    public Result<List<ChatGroupVO>> listMyGroups() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(chatGroupService.listMyGroups(userId));
    }

    @Operation(summary = "获取群组详情")
    @GetMapping("/{groupId}")
    @SaCheckLogin
    public Result<ChatGroupVO> getGroupDetail(@PathVariable Long groupId) {
        return Result.success(chatGroupService.getGroupDetail(groupId));
    }

    @Operation(summary = "加入群组")
    @PostMapping("/{groupId}/join")
    @SaCheckLogin
    public Result<Void> joinGroup(@PathVariable Long groupId) {
        Long userId = StpUtil.getLoginIdAsLong();
        chatGroupService.joinGroup(groupId, userId);
        return Result.success();
    }

    @Operation(summary = "退出群组")
    @PostMapping("/{groupId}/leave")
    @SaCheckLogin
    public Result<Void> leaveGroup(@PathVariable Long groupId) {
        Long userId = StpUtil.getLoginIdAsLong();
        chatGroupService.leaveGroup(groupId, userId);
        return Result.success();
    }

    @Operation(summary = "获取群聊历史消息")
    @GetMapping("/{groupId}/messages")
    @SaCheckLogin
    public Result<List<ChatMessageVO>> getHistory(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(groupMessageService.getHistory(groupId, pageNum, pageSize));
    }
}
