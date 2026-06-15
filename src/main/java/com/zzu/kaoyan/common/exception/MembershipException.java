package com.zzu.kaoyan.common.exception;

import com.zzu.kaoyan.common.result.ResultCode;
import lombok.Getter;

/**
 * 会员/配额相关异常
 * 由 MembershipAspect 或 MembershipService 抛出，
 * 被 GlobalExceptionHandler 统一处理为 402 响应
 */
@Getter
public class MembershipException extends BusinessException {

    private final String featureKey;

    public MembershipException(ResultCode resultCode, String featureKey) {
        super(resultCode);
        this.featureKey = featureKey;
    }

    public MembershipException(ResultCode resultCode, String featureKey, String message) {
        super(resultCode);
        this.featureKey = featureKey;
    }
}
