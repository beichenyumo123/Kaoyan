package com.zzu.kaoyan.module.membership.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员套餐定义
 */
@Data
@TableName("membership_plans")
public class MembershipPlan {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String planCode;
    private String planName;
    private String description;
    private BigDecimal price;
    private Integer durationDays;

    /** features JSON: {"ai_ask":5,"ai_tasks":0,...} */
    private String features;

    private Boolean isActive;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
