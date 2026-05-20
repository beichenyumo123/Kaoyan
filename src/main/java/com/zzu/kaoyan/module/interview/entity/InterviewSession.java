package com.zzu.kaoyan.module.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模拟面试会话实体类
 * 对应数据库表 interview_session
 */
@Data
@TableName("interview_session")
public class InterviewSession {

    /**
     * 主键ID，数据库自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联 sys_user.id
     */
    private Long userId;

    /**
     * 目标院校
     */
    private String targetSchool;

    /**
     * 目标专业
     */
    private String targetMajor;

    /**
     * 面试类型：ENGLISH / MAJOR / COMPREHENSIVE
     */
    private String interviewType;

    /**
     * 会话状态：IN_PROGRESS(进行中) / REPORTED(已出报告)
     */
    private String status;

    /**
     * 综合评分，范围 0.0 ~ 100.0，未出报告时为 null
     */
    private BigDecimal overallScore;

    /**
     * 创建时间，由 MyBatis-Plus 自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间，由 MyBatis-Plus 自动填充（插入和更新时均触发）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
