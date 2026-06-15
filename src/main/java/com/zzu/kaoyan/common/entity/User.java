package com.zzu.kaoyan.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(value = "id", type = IdType.AUTO)  // ✅ 改成 AUTO，使用数据库自增
    private Long id;

    private String username;
    private String password;
    private String email;
    private String phone;

    private String role;
    private String avatarUrl;
    private String targetMajor;
    private String targetSchool;
    private Integer points;
    private Boolean isVerified;

    @TableField("is_deleted")
    @TableLogic
    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}