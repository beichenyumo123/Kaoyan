package com.zzu.kaoyan.module.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_intervention_log")
public class AiInterventionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String agentName;

    private String triggerReason;

    private String interventionContent;

    private String detailMarkdown;

    private String linkTarget;

    private String linkLabel;

    private String userReaction;

    private LocalDateTime createdAt;
}
