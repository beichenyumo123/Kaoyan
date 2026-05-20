package com.zzu.kaoyan.module.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("group_message")
public class GroupMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;
    private Long userId;
    private String content;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
