package com.zzu.kaoyan.module.schoolselect.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("school_major")
public class SchoolMajor {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long schoolId;
    private String majorName;
    private String majorCode;
    private String category;
    private Integer admissionCount;
    private Integer applicantCount;
    private Integer minScore;
    private Integer avgScore;
    private BigDecimal tuiMianRatio;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
