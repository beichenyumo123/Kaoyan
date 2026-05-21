package com.zzu.kaoyan.module.certification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "提交认证申请请求体")
public class VerificationSubmitDTO {

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @NotBlank(message = "录取院校不能为空")
    @Schema(description = "录取院校", example = "清华大学")
    private String targetSchool;

    @NotBlank(message = "录取专业不能为空")
    @Schema(description = "录取专业", example = "计算机技术")
    private String targetMajor;

    @NotNull(message = "入学年份不能为空")
    @Schema(description = "入学年份", example = "2026")
    private Integer admissionYear;

    @NotBlank(message = "请上传录取通知书")
    @Schema(description = "录取通知书图片URL")
    private String admissionLetterUrl;

    @NotBlank(message = "请上传学信网截图")
    @Schema(description = "学信网截图URL")
    private String xuexinScreenshotUrl;
}