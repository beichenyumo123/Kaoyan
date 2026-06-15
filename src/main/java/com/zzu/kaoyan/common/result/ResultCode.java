package com.zzu.kaoyan.common.result;

public enum ResultCode {
    SUCCESS(200, "success"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    RATE_LIMITED(429, "请求过于频繁，请稍后再试"),
    SYSTEM_ERROR(500, "服务器异常"),
    ALREADY_VERIFIED(400, "你已提交过认证申请"),
    VERIFICATION_PENDING(400, "你已有待审核的认证申请"),
    NOT_VERIFIED(400, "你尚未通过上岸认证，通过认证后才能发布经验贴"),
    MEMBERSHIP_REQUIRED(402, "该功能需要VIP会员"),
    QUOTA_EXHAUSTED(402, "今日免费次数已用完");


    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
