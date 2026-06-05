package com.zzu.kaoyan.module.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 答疑 Agent 请求 DTO。
 */
@Data
@Schema(description = "AI 答疑请求")
public class AiAskDTO {

    @NotBlank(message = "问题不能为空")
    @Schema(description = "用户提问内容", example = "B+树和B树有什么区别？")
    private String question;

    @Schema(description = "限定学科（可选，为空则全学科检索）", example = "数据结构")
    private String subject;

    @Schema(description = "会话ID（可选，为空则自动创建新会话）")
    private Long sessionId;
}
