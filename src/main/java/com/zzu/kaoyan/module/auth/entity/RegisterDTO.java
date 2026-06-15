package com.zzu.kaoyan.module.auth.entity;

import com.zzu.kaoyan.common.annotation.SkipXssClean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "注册请求参数")
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @SkipXssClean
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    @Schema(description = "密码", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zs@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "手机号(选填)", example = "13800138000")
    // 使用正则校验手机号，允许为空（^$）或符合11位手机号规则
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码内容", example = "A3x9", requiredMode = Schema.RequiredMode.REQUIRED)
    private String captchaCode;

    @NotBlank(message = "验证码标识不能为空")
    @Schema(description = "验证码UUID", example = "a1b2c3d4e5f6", requiredMode = Schema.RequiredMode.REQUIRED)
    private String captchaUuid;
}
