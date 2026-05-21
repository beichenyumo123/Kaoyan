package com.zzu.kaoyan.module.certification.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "认证信息响应")
public class VerificationVO {

    @Schema(description = "认证记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "录取院校")
    private String targetSchool;

    @Schema(description = "录取专业")
    private String targetMajor;

    @Schema(description = "入学年份")
    private Integer admissionYear;

    @Schema(description = "录取通知书图片URL")
    private String admissionLetterUrl;

    @Schema(description = "学信网截图URL")
    private String xuexinScreenshotUrl;

    @Schema(description = "审核状态: 0=待审核, 1=已通过, 2=已驳回")
    private Integer status;

    @Schema(description = "审核意见")
    private String reviewComment;

    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;

    @Schema(description = "申请时间")
    private LocalDateTime createdAt;
}