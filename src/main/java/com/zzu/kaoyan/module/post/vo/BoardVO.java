package com.zzu.kaoyan.module.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "板块列表返回VO")
public class BoardVO {
    @Schema(description = "板块ID")
    private Long id;

    @Schema(description = "板块名称")
    private String name;

    @Schema(description = "板块描述")
    private String description;

    @Schema(description = "板块封面图URL")
    private String coverUrl;

    @Schema(description = "帖子总数")
    private Long postCount;
}