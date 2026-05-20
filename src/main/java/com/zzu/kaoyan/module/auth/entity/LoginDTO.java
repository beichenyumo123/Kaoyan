package com.zzu.kaoyan.module.auth.entity;

import com.zzu.kaoyan.common.annotation.SkipXssClean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "登录请求参数")
public class LoginDTO {
    @SkipXssClean
    @NotBlank(message = "账号不能为空")
    @Schema(description = "支持邮箱/手机号", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String account;

    @SkipXssClean
    @NotBlank(message = "密码不能为空")
    @Schema(description = "用户密码", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码内容", example = "A3x9", requiredMode = Schema.RequiredMode.REQUIRED)
    private String captchaCode;

    @NotBlank(message = "验证码标识不能为空")
    @Schema(description = "验证码UUID", example = "a1b2c3d4e5f6", requiredMode = Schema.RequiredMode.REQUIRED)
    private String captchaUuid;
}
