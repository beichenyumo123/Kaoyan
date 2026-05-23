package com.zzu.kaoyan.module.interview.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "面试会话视图")
public class InterviewSessionVO {

    private Long id;
    private Long userId;
    private String schoolStyle;
    private String schoolStyleName;
    private String major;
    private String interviewType;
    private String interviewTypeName;
    private Integer status;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
