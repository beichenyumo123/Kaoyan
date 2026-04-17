package com.zzu.kaoyan.module.activity.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("interaction_user_study")
public class UserStudyPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer continuousDays;
    private Integer totalCheckDays;
    private LocalDate lastCheckDate;
    private LocalDateTime updatedAt;
}