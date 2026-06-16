package com.zzu.kaoyan.module.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ai_daily_task")
public class AiDailyTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate taskDate;

    private String taskContent;

    private String importance;

    private Integer status;

    private String agentTips;

    private String detailMarkdown;

    private String linkTarget;

    private String linkLabel;

    private LocalDateTime createdAt;
}
