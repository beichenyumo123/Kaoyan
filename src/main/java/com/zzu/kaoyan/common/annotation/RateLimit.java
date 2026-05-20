package com.zzu.kaoyan.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解，基于 Redis ZSET 滑动窗口实现。
 * 标在 Controller 方法上即可生效。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 时间窗口（秒），默认 60 */
    int time() default 60;

    /** 窗口内最大请求次数，默认 10 */
    int maxCount() default 10;

    /** 触发限流时的提示信息 */
    String message() default "请求过于频繁，请稍后再试";
}
