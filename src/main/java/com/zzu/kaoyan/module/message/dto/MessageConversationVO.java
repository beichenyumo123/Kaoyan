package com.zzu.kaoyan.module.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "聊天记录响应")
public class MessageConversationVO {

    @Schema(description = "私信ID")
    private Long id;

    @Schema(description = "发送者ID")
    private Long fromUserId;

    @Schema(description = "发送者用户名")
    private String fromUsername;

    @Schema(description = "发送者头像")
    private String fromAvatarUrl;

    @Schema(description = "接收者ID")
    private Long toUserId;

    @Schema(description = "接收者用户名")
    private String toUsername;

    @Schema(description = "接收者头像")
    private String toAvatarUrl;

    @Schema(description = "私信内容")
    private String content;

    @Schema(description = "是否已读：0-未读，1-已读")
    private Integer isRead;

    @Schema(description = "发送时间")
    private LocalDateTime createTime;
}