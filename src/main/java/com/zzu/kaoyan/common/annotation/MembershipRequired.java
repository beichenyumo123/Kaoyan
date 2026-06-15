package com.zzu.kaoyan.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 会员功能访问控制注解
 * 加在 Controller 方法上，AOP 自动检查会员权限和配额
 *
 * <pre>
 *   @MembershipRequired("ai_ask")
 *   public Result<String> askQuestion(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MembershipRequired {

    /** 功能标识，对应 membership_plans.features JSON 中的 key */
    String value();

    /** 自定义错误信息（可选，覆盖默认 message） */
    String message() default "";
}
