package com.zzu.kaoyan.module.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "私信联系人信息")
public class MessageContactVO {

    @Schema(description = "对方用户ID")
    private Long userId;

    @Schema(description = "对方用户名")
    private String username;

    @Schema(description = "对方头像URL")
    private String avatarUrl;

    @Schema(description = "最后一条消息内容")
    private String lastMessage;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "未读消息数量")
    private Long unreadCount;
}