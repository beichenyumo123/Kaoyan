package com.zzu.kaoyan.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "修改个人信息请求")
public class UserUpdateDTO {

    @Schema(description = "用户名", example = "新昵称")
    private String username;

    @Schema(description = "头像URL", example = "https://oss.example.com/new-avatar.jpg")
    private String avatarUrl;

    @Schema(description = "目标专业", example = "软件工程")
    private String targetMajor;

    @Schema(description = "手机号", example = "13912345678")
    private String phone;
}