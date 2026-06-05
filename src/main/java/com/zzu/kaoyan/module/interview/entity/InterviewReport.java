package com.zzu.kaoyan.module.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模拟面试评估报告实体类
 * 对应数据库表 interview_report
 */
@Data
@TableName("interview_report")
public class InterviewReport {

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
     * 综合评分，范围 0.0 ~ 100.0
     */
    private BigDecimal totalScore;

    /**
     * 雷达图能力维度数据，MySQL JSON 类型列
     * 格式示例：[{"dimension":"语言表达","score":80},{"dimension":"专业知识","score":90},...]
     */
    private String radarChart;

    /**
     * 优势分析
     */
    private String strengthAnalysis;

    /**
     * 薄弱项分析
     */
    private String weaknessAnalysis;

    /**
     * 改进建议
     */
    private String suggestion;

    /**
     * 综合评价总结
     */
    private String summary;

    /**
     * AI 返回的原始完整 JSON，用于问题回溯与调试
     */
    private String rawJson;

    /**
     * 报告生成时间，由 MyBatis-Plus 自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
