package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Markdown 渲染结果")
public class MarkdownRenderVO {

    @Schema(description = "渲染后的HTML内容（已做XSS清洗）")
    private String html;
}
