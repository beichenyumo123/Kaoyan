package com.zzu.kaoyan.module.interview.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_session")
public class InterviewSession {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String schoolStyle;

    private String major;

    private String interviewType;

    private Integer status;

    private Integer totalQuestions;

    private Integer answeredQuestions;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
