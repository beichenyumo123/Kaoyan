package com.zzu.kaoyan.module.certification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "审核认证请求体")
public class ReviewDTO {

    @NotNull(message = "审核状态不能为空")
    @Schema(description = "审核状态: 1=通过, 2=驳回", example = "1")
    private Integer status;

    @Schema(description = "审核意见", example = "审核通过，恭喜上岸！")
    private String comment;
}
