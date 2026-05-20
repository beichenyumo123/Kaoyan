package com.zzu.kaoyan.module.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模拟面试对话明细实体类
 * 对应数据库表 interview_record
 */
@Data
@TableName("interview_record")
public class InterviewRecord {

    /**
     * 主键ID，数据库自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属会话ID，关联 interview_session.id
     */
    private Long sessionId;

    /**
     * 发言角色：user(用户) / ai(人工智能考官)
     */
    private String role;

    /**
     * 对话内容文本，支持长文本
     */
    private String content;

    /**
     * 语音流利度得分，仅 role='user' 时有值，范围 0.0 ~ 100.0
     */
    private BigDecimal fluencyScore;

    /**
     * 记录创建时间，由 MyBatis-Plus 自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
