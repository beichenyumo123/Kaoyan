package com.zzu.kaoyan.module.interact.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "提交举报请求参数")
public class SubmitReportDTO {

    @Schema(description = "举报目标类型 (POST: 帖子, COMMENT: 评论, USER: 用户)", example = "POST", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetType;

    @Schema(description = "被举报目标的ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetId;

    @Schema(description = "举报原因", example = "含有广告营销等垃圾信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}