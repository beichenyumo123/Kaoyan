package com.zzu.kaoyan.module.mistake.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "mistake_daily_plan", autoResultMap = true)
public class DailyPlanPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate planDate;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> noteIds;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> completedIds;

    private Integer totalCount;
    private Integer completedCount;
    private Integer isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
