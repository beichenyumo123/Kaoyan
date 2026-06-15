package com.zzu.kaoyan.module.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 套餐展示对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlanVO {

    private Long id;
    private String planCode;
    private String planName;
    private String description;
    private BigDecimal price;
    private Integer durationDays;

    /** 功能配额摘要: {"ai_ask": 100, "ocr": 30, ...} */
    private Map<String, Object> features;
}
