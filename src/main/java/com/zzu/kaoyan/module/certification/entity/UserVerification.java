package com.zzu.kaoyan.module.certification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_verification")
public class UserVerification {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String realName;
    private String targetSchool;
    private String targetMajor;
    private Integer admissionYear;
    private String admissionLetterUrl;
    private String xuexinScreenshotUrl;

    /** 审核状态: 0=待审核, 1=已通过, 2=已驳回 */
    private Integer status;

    private Long reviewerId;
    private String reviewComment;
    private LocalDateTime reviewedAt;

    @TableField("is_deleted")
    @TableLogic
    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}