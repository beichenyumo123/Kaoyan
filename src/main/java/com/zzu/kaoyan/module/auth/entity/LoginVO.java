package com.zzu.kaoyan.module.auth.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginVO {
    @Schema(description = "JWT 令牌", example = "eyJhbGciOiJIUzI1Ni...")
    private String token;

    @Schema(description = "用户唯一ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "角色: USER(普通), MODERATOR(版主), ADMIN(管理员)", example = "USER")
    private String role;

    @Schema(description = "头像URL", example = "https://...")
    private String avatarUrl;
}
