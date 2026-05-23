package com.zzu.kaoyan.module.interview.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "面试问题视图")
public class QuestionVO {

    private Long id;
    private Long sessionId;
    private Integer questionNumber;
    private String questionContent;
    private String questionType;
    private String userAnswer;
    private Integer aiScore;
    private String aiComment;
    private DimensionScoresVO dimensionScores;
}
