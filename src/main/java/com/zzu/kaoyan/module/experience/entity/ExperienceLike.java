package com.zzu.kaoyan.module.experience.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("experience_post_like")
public class ExperienceLike {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long experienceId;
    private Long userId;

    private LocalDateTime createdAt;
}