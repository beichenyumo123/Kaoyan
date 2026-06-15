package com.zzu.kaoyan.module.ai.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterventionVO {
    private Long id;
    private Long userId;
    private String agentName;
    private String triggerReason;
    private String interventionContent;
    private String userReaction;
    private LocalDateTime createdAt;
}
