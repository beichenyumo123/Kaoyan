package com.zzu.kaoyan.module.experience.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("experience_post_collect")
public class ExperienceCollect {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long experienceId;
    private Long userId;

    private LocalDateTime createdAt;
}