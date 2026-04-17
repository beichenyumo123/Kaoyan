package com.zzu.kaoyan.module.activity.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckInDTO {
    @NotNull(message = "学习时长不能为空")
    @Min(value = 1, message = "最少1小时")
    @Max(value = 24, message = "最多24小时")
    private Integer studyHours;

    private String notes;
}