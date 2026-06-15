package com.zzu.kaoyan.module.schoolselect.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("admission_record")
public class AdmissionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long schoolId;
    private String majorName;
    private String undergradSchool;
    private BigDecimal undergradGpa;
    private String englishLevel;
    private Integer prepDuration;
    private Integer mockExamScore;
    private Integer examScoreTotal;
    private Integer examScorePolitics;
    private Integer examScoreEnglish;
    private Integer examScoreBiz1;
    private Integer examScoreBiz2;
    private Boolean isVerified;
    private String verificationStatus;
    private LocalDateTime verifiedAt;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
