package com.zzu.kaoyan.module.activity.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("interaction_points_log")
public class PointsLogPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer points;
    private String type;
    private Long relId;
    private String description;
    private LocalDateTime createdAt;
}