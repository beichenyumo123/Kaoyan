package com.zzu.kaoyan.common.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String phone;

    private String role;          // USER, MODERATOR, ADMIN
    private String avatarUrl;
    private String targetMajor;
    private Integer points;

    @TableField("is_deleted")
    @TableLogic
    private Boolean deleted;      // 0未删, 1已删

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}