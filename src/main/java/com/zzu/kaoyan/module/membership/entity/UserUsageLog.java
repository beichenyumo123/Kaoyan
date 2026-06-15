package com.zzu.kaoyan.module.membership.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 功能使用日志（MySQL 持久化，热数据在 Redis）
 */
@Data
@TableName("user_usage_logs")
public class UserUsageLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String featureKey;
    private LocalDate usageDate;
    private Integer count;
}
