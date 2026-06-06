package com.zzu.kaoyan.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI 对话消息视图")
public class ChatMessageVO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "角色: user / assistant")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "图片URL（用户发送的图片）")
    private String imageUrl;

    @Schema(description = "发送时间")
    private LocalDateTime createdAt;
}
