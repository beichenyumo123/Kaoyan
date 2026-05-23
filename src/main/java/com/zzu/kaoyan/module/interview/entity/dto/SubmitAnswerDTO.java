package com.zzu.kaoyan.module.interview.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "提交回答请求")
public class SubmitAnswerDTO {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "面试会话ID", example = "1")
    private Long sessionId;

    @NotBlank(message = "回答内容不能为空")
    @Schema(description = "考生回答内容", example = "我本科期间参与了...")
    private String answer;
}
