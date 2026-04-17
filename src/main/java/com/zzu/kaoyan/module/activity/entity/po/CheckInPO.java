package com.zzu.kaoyan.module.activity.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("interaction_check_in")
public class CheckInPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer studyHours;
    private String notes;
    @TableField(exist = false)
    private Integer continuousDays;
    private LocalDate createdDate;
    private LocalDateTime createdAt;
}