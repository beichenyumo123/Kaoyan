package com.zzu.kaoyan.module.membership.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员订单
 */
@Data
@TableName("membership_orders")
public class MembershipOrder {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long planId;
    private String orderNo;
    private BigDecimal amount;
    private String paymentMethod;

    /** PENDING / PAID / REFUNDED / CANCELLED */
    private String paymentStatus;

    private String transactionId;
    private LocalDateTime paidAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
