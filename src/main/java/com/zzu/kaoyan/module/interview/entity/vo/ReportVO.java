package com.zzu.kaoyan.module.interview.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 面试评估报告 - 解析后的 VO 对象
 * 对应 DeepSeek 返回的 JSON 结构，同时也作为前端展示模型
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略 AI 可能返回的未知字段，防止反序列化失败
public class ReportVO {

    /**
     * 综合评分，范围 0.0 ~ 100.0
     */
    private BigDecimal totalScore;

    /**
     * 雷达图各维度数据
     */
    private List<RadarDimension> radarChart;

    /**
     * 优势分析文本
     */
    private String strengthAnalysis;

    /**
     * 薄弱项分析文本
     */
    private String weaknessAnalysis;

    /**
     * 改进建议文本
     */
    private String suggestion;

    /**
     * 综合评价总结
     */
    private String summary;

    // ---------- 内部类 ----------

    /**
     * 雷达图单个维度
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RadarDimension {
        /**
         * 维度名称，例如：语言表达、专业知识、逻辑思维、沟通能力、心理素质
         */
        private String dimension;

        /**
         * 该维度的评分，范围 0 ~ 100
         */
        private BigDecimal score;
    }
}
