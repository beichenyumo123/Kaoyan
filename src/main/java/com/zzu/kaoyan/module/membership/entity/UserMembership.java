package com.zzu.kaoyan.module.membership.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户订阅记录
 */
@Data
@TableName("user_memberships")
public class UserMembership {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long planId;

    /** ACTIVE / EXPIRED / CANCELLED */
    private String status;

    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime cancelledAt;
    private Boolean autoRenew;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
