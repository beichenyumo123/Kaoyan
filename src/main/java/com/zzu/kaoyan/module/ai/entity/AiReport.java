package com.zzu.kaoyan.module.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 周报持久化实体
 */
@Data
@TableName("ai_report")
public class AiReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 周报周期起始日（周一） */
    private LocalDate weekStart;

    /** 周报周期结束日（周日） */
    private LocalDate weekEnd;

    /** 周报 Markdown 内容 */
    private String markdown;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
