package com.zzu.kaoyan.module.user.dto;

import com.zzu.kaoyan.module.membership.dto.UserMembershipVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户信息响应")
public class UserVO {

    @Schema(description = "用户ID", example = "1234567890123456789")
    private Long id;

    @Schema(description = "用户名", example = "考研小学霸")
    private String username;

    @Schema(description = "邮箱", example = "xueba@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "角色", example = "USER")
    private String role;

    @Schema(description = "头像URL", example = "https://oss.example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "目标专业", example = "计算机技术")
    private String targetMajor;

    @Schema(description = "目标院校", example = "清华大学")
    private String targetSchool;

    @Schema(description = "总积分", example = "250")
    private Integer points;

    @Schema(description = "是否通过上岸认证", example = "true")
    private Boolean isVerified;

    @Schema(description = "认证录取院校（认证通过后可见）", example = "清华大学")
    private String verifiedSchool;

    @Schema(description = "认证录取专业（认证通过后可见）", example = "计算机技术")
    private String verifiedMajor;

    @Schema(description = "会员状态与配额")
    private UserMembershipVO membership;
}