package com.zzu.kaoyan.module.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI 对话会话视图")
public class ChatSessionVO {

    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "最后一条消息预览")
    private String lastMessage;

    @Schema(description = "最后更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "消息总数")
    private Integer messageCount;
}
