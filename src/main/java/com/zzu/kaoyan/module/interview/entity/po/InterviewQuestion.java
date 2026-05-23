package com.zzu.kaoyan.module.interview.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_question")
public class InterviewQuestion {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Integer questionNumber;

    private String questionContent;

    private String questionType;

    private String userAnswer;

    private Integer aiScore;

    private String aiComment;

    private String dimensionScores;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
