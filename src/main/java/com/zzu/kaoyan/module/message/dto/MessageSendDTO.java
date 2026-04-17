package com.zzu.kaoyan.module.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "发送私信请求")
public class MessageSendDTO {

    @NotNull(message = "接收者ID不能为空")
    @Schema(description = "接收者用户ID", example = "1002", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long toUserId;

    @NotBlank(message = "私信内容不能为空")
    @Schema(description = "私信内容", example = "你好，请问考研资料在哪里下载？", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}