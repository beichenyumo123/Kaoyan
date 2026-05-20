package com.zzu.kaoyan.module.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_group")
public class ChatGroup {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String avatarUrl;
    private Long ownerId;
    private Integer memberCount;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
