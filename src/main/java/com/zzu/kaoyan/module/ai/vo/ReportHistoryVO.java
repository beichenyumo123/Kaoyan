package com.zzu.kaoyan.module.ai.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 历史周报 VO
 */
@Data
public class ReportHistoryVO {

    private Long id;

    /** 周报周期起始日（周一） */
    private LocalDate weekStart;

    /** 周报周期结束日（周日） */
    private LocalDate weekEnd;

    /** 周报 Markdown 内容 */
    private String markdown;
}
