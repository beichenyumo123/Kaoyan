package com.zzu.kaoyan.module.schoolselect.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "智能择校请求参数")
public class RecommendationRequestDTO {

    @NotBlank(message = "本科院校不能为空")
    @Schema(description = "本科院校名称", example = "河南理工大学")
    private String undergradSchool;

    @NotNull(message = "GPA不能为空")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "4.0", message = "GPA范围0.0-4.0")
    @Schema(description = "本科GPA(4.0制)", example = "3.2")
    private BigDecimal gpa;

    @NotBlank(message = "英语等级不能为空")
    @Schema(description = "英语等级", example = "CET6", allowableValues = {"CET4", "CET6", "TEM4", "TEM8", "NONE"})
    private String englishLevel;

    @NotNull(message = "备考时长不能为空")
    @Min(value = 1, message = "备考至少1个月")
    @Max(value = 36, message = "备考不超过36个月")
    @Schema(description = "已备考时长(月)", example = "6")
    private Integer prepDuration;

    @Schema(description = "最近一次模考分数(满分500)", example = "320")
    private Integer mockExamScore;

    @NotBlank(message = "风险偏好不能为空")
    @Schema(description = "风险偏好", example = "moderate", allowableValues = {"conservative", "moderate", "aggressive"})
    private String riskPreference;
}
