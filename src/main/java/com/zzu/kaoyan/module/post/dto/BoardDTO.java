package com.zzu.kaoyan.module.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "板块请求参数")
public class BoardDTO {

    @Schema(description = "板块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "板块描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverUrl;
}