package com.zzu.kaoyan.module.interview.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "开始模拟复试请求")
public class StartInterviewDTO {

    @NotBlank(message = "院校风格不能为空")
    @Schema(description = "院校风格：tsinghua/tongji/985/211/other", example = "tsinghua")
    private String schoolStyle;

    @NotBlank(message = "报考专业不能为空")
    @Schema(description = "报考专业", example = "计算机科学与技术")
    private String major;

    @NotBlank(message = "面试类型不能为空")
    @Schema(description = "面试类型：english_self/english_qa/professional/comprehensive/stress", example = "comprehensive")
    private String interviewType;
}
