package com.zzu.kaoyan.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.annotation.RateLimit;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Aspect
@Component
@ConditionalOnBean(RedisConnectionFactory.class)
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private static final String KEY_PREFIX = "rate_limit:";

    /**
     * Lua 脚本：滑动窗口限流
     * KEYS[1]  — 限流 key
     * ARGV[1]  — 窗口左边界时间戳 (now - windowMs)
     * ARGV[2]  — 最大请求数
     * ARGV[3]  — 当前时间戳（作为 ZSET score）
     * ARGV[4]  — 唯一 member 值
     * ARGV[5]  — key 过期时间（秒）
     * 返回 1: 放行, 0: 拦截
     */
    private static final String LUA_SCRIPT = """
            redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1])
            local count = redis.call('ZCARD', KEYS[1])
            if count < tonumber(ARGV[2]) then
                redis.call('ZADD', KEYS[1], ARGV[3], ARGV[4])
                redis.call('EXPIRE', KEYS[1], ARGV[5])
                return 1
            else
                return 0
            end
            """;

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> redisScript;

    public RateLimitAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisScript = new DefaultRedisScript<>();
        this.redisScript.setScriptText(LUA_SCRIPT);
        this.redisScript.setResultType(Long.class);
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint);

        long now = System.currentTimeMillis();
        long windowStart = now - rateLimit.time() * 1000L;
        String member = now + ":" + ThreadLocalRandom.current().nextInt(100000);

        List<String> keys = Collections.singletonList(key);
        Long result = redisTemplate.execute(
                redisScript,
                keys,
                windowStart,
                rateLimit.maxCount(),
                now,
                member,
                rateLimit.time()
        );

        if (result != null && result == 1L) {
            return joinPoint.proceed();
        }

        log.warn("接口限流触发 — key={}, maxCount={}, window={}s", key, rateLimit.maxCount(), rateLimit.time());
        throw new BusinessException(ResultCode.RATE_LIMITED.getCode(), rateLimit.message());
    }

    private String buildKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getMethod().getDeclaringClass().getSimpleName();
        String methodName = signature.getName();

        String userIdentity;
        try {
            userIdentity = String.valueOf(StpUtil.getLoginIdAsLong());
        } catch (Exception e) {
            userIdentity = getClientIp();
        }

        return KEY_PREFIX + className + ":" + methodName + ":" + userIdentity;
    }

    private String getClientIp() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多级代理 IP，只取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
