package com.zzu.kaoyan.module.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_ai_profile")
public class UserAiProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String cognitiveProfile;

    private String psychologicalProfile;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
