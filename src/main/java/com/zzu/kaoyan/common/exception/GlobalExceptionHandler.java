package com.zzu.kaoyan.common.exception;

import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 * 拦截所有 Controller 层抛出的异常，统一封装为 Result 格式返回给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义的业务异常 (例如：密码错误、用户被封禁等)
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常提示: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理 Spring MVC 参数校验异常 (即 Controller 使用 @Valid / @Validated 时触发的异常)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidationException(Exception e) {
        String errorMessage = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException) {
            FieldError fieldError = ((MethodArgumentNotValidException) e).getBindingResult().getFieldError();
            if (fieldError != null) {
                errorMessage = fieldError.getDefaultMessage();
            }
        } else if (e instanceof BindException) {
            FieldError fieldError = ((BindException) e).getBindingResult().getFieldError();
            if (fieldError != null) {
                errorMessage = fieldError.getDefaultMessage();
            }
        }
        log.warn("参数校验异常: {}", errorMessage);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), errorMessage);
    }

    /**
     * 处理静态资源 404 (如 favicon.ico)，这类请求不经过 Controller，直接返回 404 即可
     * 不需要打印堆栈，也不会被兜底 handler 误报为系统严重异常
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
        log.debug("静态资源未找到: {}", e.getMessage());
        return Result.error(ResultCode.NOT_FOUND);
    }

    /**
     * 兜底处理器：处理所有未预料到的系统异常 (如 NullPointerException, 数据库异常等)
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleSystemException(Exception e) {
        // 对于未知异常，必须打印完整堆栈信息以便排查
        log.error("系统内部发生严重异常: ", e);
        // 返回500，保护后端底层代码报错信息不直接暴露给前端
        return Result.error(ResultCode.SYSTEM_ERROR);
    }
}