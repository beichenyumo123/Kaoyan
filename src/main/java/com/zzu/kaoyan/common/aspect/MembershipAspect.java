package com.zzu.kaoyan.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.annotation.MembershipRequired;
import com.zzu.kaoyan.common.exception.MembershipException;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.module.membership.service.MembershipService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 会员功能访问控制切面
 *
 * 拦截 @MembershipRequired 注解的方法，检查：
 * 1. 用户是否登录（从 Sa-Token 上下文获取 userId）
 * 2. 用户是否有访问该功能的权限（VIP / 配额）
 *
 * 配额在当前方法执行成功后才扣减（避免业务失败误扣）
 */
@Aspect
@Component
public class MembershipAspect {

    private static final Logger log = LoggerFactory.getLogger(MembershipAspect.class);

    private final MembershipService membershipService;

    public MembershipAspect(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @Around("@annotation(membershipRequired)")
    public Object around(ProceedingJoinPoint joinPoint, MembershipRequired membershipRequired) throws Throwable {
        String featureKey = membershipRequired.value();
        Long userId = StpUtil.getLoginIdAsLong();

        // 检查访问权限（此步骤在 Redis 中原子预扣配额）
        MembershipService.AccessResult access = membershipService.checkAccess(userId, featureKey);

        if (access == MembershipService.AccessResult.VIP_REQUIRED) {
            log.info("会员拦截 — userId={}, feature={}, reason=VIP_REQUIRED", userId, featureKey);
            String message = membershipRequired.message().isEmpty()
                    ? ResultCode.MEMBERSHIP_REQUIRED.getMessage()
                    : membershipRequired.message();
            throw new MembershipException(ResultCode.MEMBERSHIP_REQUIRED, featureKey, message);
        }

        if (access == MembershipService.AccessResult.QUOTA_EXHAUSTED) {
            log.info("配额耗尽 — userId={}, feature={}, reason=QUOTA_EXHAUSTED", userId, featureKey);
            String message = membershipRequired.message().isEmpty()
                    ? ResultCode.QUOTA_EXHAUSTED.getMessage()
                    : membershipRequired.message();
            throw new MembershipException(ResultCode.QUOTA_EXHAUSTED, featureKey, message);
        }

        // 放行 → 执行业务逻辑
        // 注意：checkAccess 中已经通过 Redis Lua 原子预扣了配额
        // 如果业务失败 → 退款
        try {
            Object result = joinPoint.proceed();
            // 业务成功 → 记录 MySQL 使用日志
            membershipService.recordUsage(userId, featureKey);
            return result;
        } catch (Throwable t) {
            // 业务失败 → 退款配额
            membershipService.refundUsage(userId, featureKey);
            throw t;
        }
    }
}
