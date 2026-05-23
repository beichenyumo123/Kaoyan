package com.zzu.kaoyan.module.interview.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "多维度评分视图")
public class DimensionScoresVO {

    @Schema(description = "内容深度")
    private Integer contentDepth;

    @Schema(description = "逻辑清晰度")
    private Integer logicClarity;

    @Schema(description = "语言表达")
    private Integer languageExpress;

    @Schema(description = "专业知识")
    private Integer professionalKnowledge;

    @Schema(description = "应变能力")
    private Integer adaptability;

    @Schema(description = "心理素质")
    private Integer psychology;
}
