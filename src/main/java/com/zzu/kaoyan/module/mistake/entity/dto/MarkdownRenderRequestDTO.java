package com.zzu.kaoyan.module.mistake.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Markdown 渲染请求")
public class MarkdownRenderRequestDTO {

    @NotBlank(message = "Markdown内容不能为空")
    @Schema(description = "原始Markdown文本", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "# 题目\n\n已知 $f(x) = x^2$，求 $\\int_0^1 f(x) dx$")
    private String markdown;
}
