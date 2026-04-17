package com.zzu.kaoyan.module.interact.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "举报列表查询参数")
public class ReportQueryDTO {

    @Schema(description = "页码，默认 1", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量，默认 10", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "处理状态 (0-待处理, 1-已处理, 2-已驳回)")
    private Integer status;

    @Schema(description = "目标类型 (POST/COMMENT/USER)")
    private String targetType;
}