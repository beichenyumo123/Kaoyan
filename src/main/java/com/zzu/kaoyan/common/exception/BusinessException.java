package com.zzu.kaoyan.common.exception;

import com.zzu.kaoyan.common.result.ResultCode;

/**
 * 自定义业务异常
 * 用于在业务逻辑层(Service)主动抛出已知错误，阻断执行流并由全局异常处理器接管
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.SYSTEM_ERROR.getCode();
    }

    public Integer getCode() {
        return code;
    }
}