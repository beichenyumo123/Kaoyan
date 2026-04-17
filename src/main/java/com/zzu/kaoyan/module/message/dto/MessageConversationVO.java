package com.zzu.kaoyan.module.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "聊天记录响应")
public class MessageConversationVO {

    @Schema(description = "私信ID", example = "1")
    private Long id;

    @Schema(description = "发送者ID", example = "1001")
    private Long fromUserId;

    @Schema(description = "接收者ID", example = "1002")
    private Long toUserId;

    @Schema(description = "私信内容", example = "你好")
    private String content;

    @Schema(description = "是否已读：0-未读，1-已读", example = "1")
    private Integer isRead;

    @Schema(description = "发送时间", example = "2025-04-17 10:30:00")
    private LocalDateTime createTime;
}