package com.zzu.kaoyan.module.interview.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "复试评估报告视图")
public class InterviewReportVO {

    private Long id;
    private Long sessionId;
    private Long userId;

    @Schema(description = "综合总分")
    private Integer totalScore;

    @Schema(description = "内容深度评分")
    private Integer contentDepthScore;

    @Schema(description = "语言表达评分")
    private Integer languageExpressScore;

    @Schema(description = "心理状态评分")
    private Integer psychologyScore;

    @Schema(description = "综合素养评分")
    private Integer comprehensiveScore;

    @Schema(description = "综合评价总结")
    private String summary;

    @Schema(description = "优势分析")
    private String strengths;

    @Schema(description = "不足之处")
    private String weaknesses;

    @Schema(description = "改进建议")
    private String improvementAdvice;

    @Schema(description = "建议练习方向")
    private String suggestedPractice;

    private LocalDateTime createdAt;
}
