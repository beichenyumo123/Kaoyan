package com.zzu.kaoyan.module.interview.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_report")
public class InterviewReport {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long userId;

    private Integer totalScore;

    private Integer contentDepthScore;

    private Integer languageExpressScore;

    private Integer psychologyScore;

    private Integer comprehensiveScore;

    private String summary;

    private String strengths;

    private String weaknesses;

    private String improvementAdvice;

    private String suggestedPractice;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
