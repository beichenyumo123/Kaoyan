package com.zzu.kaoyan.module.ai.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AiTaskVO {
    private Long id;
    private Long userId;
    private LocalDate taskDate;
    private String taskContent;
    private String importance;
    private Integer status;
    private String agentTips;
    private LocalDateTime createdAt;
}
