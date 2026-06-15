package com.zzu.kaoyan.module.membership.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.module.membership.dto.MembershipPlanVO;
import com.zzu.kaoyan.module.membership.dto.UserMembershipVO;
import com.zzu.kaoyan.module.membership.entity.MembershipPlan;
import com.zzu.kaoyan.module.membership.entity.UserMembership;
import com.zzu.kaoyan.module.membership.entity.UserUsageLog;
import com.zzu.kaoyan.module.membership.mapper.MembershipPlanMapper;
import com.zzu.kaoyan.module.membership.mapper.UserMembershipMapper;
import com.zzu.kaoyan.module.membership.mapper.UserUsageLogMapper;
import com.zzu.kaoyan.module.membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 会员服务实现
 *
 * 配额检查流程：
 * 1. 从 Redis/DB 获取用户当前套餐 → 解析 features JSON
 * 2. 判断 featureKey 对应的配额：0=禁止, -1=无限制, N=限量
 * 3. 限量功能用 Redis Lua 原子 check-and-increment
 * 4. 业务成功后 recordUsage 写入 MySQL（异步可优化）
 */
@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private static final Logger log = LoggerFactory.getLogger(MembershipServiceImpl.class);

    // 按月计费的功能 key
    private static final Set<String> MONTHLY_FEATURES = Set.of(
            "ai_knowledge", "interview", "export_pdf"
    );

    // Redis key 前缀
    private static final String PLAN_CACHE_KEY = "membership:plan:";
    private static final String QUOTA_KEY_PREFIX = "membership:quota:";

    // 默认免费套餐的 features（兜底）
    private static final Map<String, Object> DEFAULT_FREE_FEATURES;

    static {
        DEFAULT_FREE_FEATURES = new HashMap<>();
        DEFAULT_FREE_FEATURES.put("ai_ask", 5);
        DEFAULT_FREE_FEATURES.put("ai_tasks", 0);
        DEFAULT_FREE_FEATURES.put("ai_interventions", 0);
        DEFAULT_FREE_FEATURES.put("ai_knowledge", 10);
        DEFAULT_FREE_FEATURES.put("school_recommend", 2);
        DEFAULT_FREE_FEATURES.put("interview", 2);
        DEFAULT_FREE_FEATURES.put("interview_tts", 0);
        DEFAULT_FREE_FEATURES.put("ocr", 3);
        DEFAULT_FREE_FEATURES.put("export_pdf", 0);
        DEFAULT_FREE_FEATURES.put("ebbinghaus_stats", 0);
        DEFAULT_FREE_FEATURES.put("weekly_report", 0);
    }

    /**
     * Lua 脚本：原子检查+扣减配额
     * KEYS[1] = quota key
     * ARGV[1] = limit (配额上限)
     * ARGV[2] = TTL (秒)
     * 返回: 1 = 放行(已扣减), 0 = 配额耗尽
     */
    private static final String LUA_CONSUME = """
            local current = redis.call('GET', KEYS[1])
            if current == false then
                redis.call('SETEX', KEYS[1], tonumber(ARGV[2]), 1)
                return 1
            end
            local count = tonumber(current)
            if count == nil then
                redis.call('SETEX', KEYS[1], tonumber(ARGV[2]), 1)
                return 1
            end
            if count < tonumber(ARGV[1]) then
                redis.call('INCR', KEYS[1])
                return 1
            end
            return 0
            """;

    /**
     * Lua 脚本：退款配额
     * KEYS[1] = quota key
     * 返回: 退款后的值（不会降到 0 以下）
     */
    private static final String LUA_REFUND = """
            local current = redis.call('GET', KEYS[1])
            if current == false then
                return 0
            end
            local count = tonumber(current)
            if count == nil then
                return 0
            end
            if count > 0 then
                return redis.call('DECR', KEYS[1])
            end
            return 0
            """;

    private final MembershipPlanMapper planMapper;
    private final UserMembershipMapper membershipMapper;
    private final UserUsageLogMapper usageLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // 套餐列表本地缓存（极少变更）
    private volatile List<MembershipPlan> cachedPlans;
    private volatile LocalDateTime plansCachedAt;

    // ==================== 访问检查 ====================

    @Override
    public AccessResult checkAccess(Long userId, String featureKey) {
        Map<String, Object> features = getEffectiveFeatures(userId);
        Object quotaObj = features.get(featureKey);

        // 功能不在套餐定义中 → 视为免费可用（宽松策略）
        if (quotaObj == null) {
            return AccessResult.OK;
        }

        int limit = toInt(quotaObj);

        // 0 = 完全禁止 → 需要 VIP
        if (limit == 0) {
            return AccessResult.VIP_REQUIRED;
        }

        // -1 = 无限制
        if (limit == -1) {
            return AccessResult.OK;
        }

        // 正整数 = 有配额上限 → 检查 Redis 计数器
        String quotaKey = buildQuotaKey(userId, featureKey);
        long ttl = getQuotaTtl(featureKey);
        Long result = stringRedisTemplate.execute(
                new DefaultRedisScript<>(LUA_CONSUME, Long.class),
                Collections.singletonList(quotaKey),
                String.valueOf(limit),
                String.valueOf(ttl)
        );

        if (result != null && result == 1L) {
            return AccessResult.OK;
        }
        return AccessResult.QUOTA_EXHAUSTED;
    }

    @Override
    public boolean tryConsume(Long userId, String featureKey) {
        return checkAccess(userId, featureKey) == AccessResult.OK;
    }

    // ==================== 用量记录 ====================

    @Override
    public void recordUsage(Long userId, String featureKey) {
        // 写入 MySQL（UPSERT 语义：有则 +1，无则插入）
        LocalDate today = LocalDate.now();
        try {
            // 尝试更新已存在的记录
            UserUsageLog existing = usageLogMapper.selectOne(
                    new LambdaQueryWrapper<UserUsageLog>()
                            .eq(UserUsageLog::getUserId, userId)
                            .eq(UserUsageLog::getFeatureKey, featureKey)
                            .eq(UserUsageLog::getUsageDate, today)
            );
            if (existing != null) {
                existing.setCount(existing.getCount() + 1);
                usageLogMapper.updateById(existing);
            } else {
                UserUsageLog log = new UserUsageLog();
                log.setUserId(userId);
                log.setFeatureKey(featureKey);
                log.setUsageDate(today);
                log.setCount(1);
                usageLogMapper.insert(log);
            }
        } catch (Exception e) {
            log.warn("记录使用日志失败 — userId={}, feature={}", userId, featureKey, e);
        }
    }

    @Override
    public void refundUsage(Long userId, String featureKey) {
        String quotaKey = buildQuotaKey(userId, featureKey);
        try {
            stringRedisTemplate.execute(
                    new DefaultRedisScript<>(LUA_REFUND, Long.class),
                    Collections.singletonList(quotaKey)
            );
            log.info("配额退款 — userId={}, feature={}", userId, featureKey);
        } catch (Exception e) {
            log.warn("配额退款失败 — userId={}, feature={}", userId, featureKey, e);
        }
    }

    @Override
    public int getUsage(Long userId, String featureKey) {
        String quotaKey = buildQuotaKey(userId, featureKey);
        try {
            Object val = redisTemplate.opsForValue().get(quotaKey);
            if (val != null) {
                return Integer.parseInt(val.toString());
            }
        } catch (Exception ignored) {}
        return 0;
    }

    // ==================== 会员信息 ====================

    @Override
    public UserMembershipVO getCurrentMembership(Long userId) {
        // 1. 检查 Redis 缓存
        String cacheKey = PLAN_CACHE_KEY + userId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cacheMap = (Map<String, Object>) cached;
                return buildMembershipVO(userId, cacheMap);
            }
        } catch (Exception ignored) {}

        // 2. 从 DB 查询
        Map<String, Object> features = getEffectiveFeatures(userId);
        // 写入缓存
        warmCache(userId);
        return buildMembershipVO(userId, features);
    }

    @Override
    public List<MembershipPlanVO> getPlans() {
        List<MembershipPlan> plans = getCachedPlans();
        List<MembershipPlanVO> vos = new ArrayList<>();
        for (MembershipPlan plan : plans) {
            vos.add(MembershipPlanVO.builder()
                    .id(plan.getId())
                    .planCode(plan.getPlanCode())
                    .planName(plan.getPlanName())
                    .description(plan.getDescription())
                    .price(plan.getPrice())
                    .durationDays(plan.getDurationDays())
                    .features(parseFeatures(plan.getFeatures()))
                    .build());
        }
        return vos;
    }

    @Override
    public void warmCache(Long userId) {
        Map<String, Object> features = getEffectiveFeatures(userId);
        // 获取用户当前 plan 信息
        UserMembership active = getActiveMembership(userId);
        Map<String, Object> cacheData = new HashMap<>(features);
        if (active != null) {
            MembershipPlan plan = planMapper.selectById(active.getPlanId());
            if (plan != null) {
                cacheData.put("_planCode", plan.getPlanCode());
                cacheData.put("_planName", plan.getPlanName());
                cacheData.put("_expiresAt", active.getExpiresAt() != null ?
                        active.getExpiresAt().toString() : null);
                cacheData.put("_autoRenew", active.getAutoRenew());
            }
        } else {
            cacheData.put("_planCode", "free");
            cacheData.put("_planName", "免费版");
            cacheData.put("_expiresAt", null);
            cacheData.put("_autoRenew", false);
        }
        String cacheKey = PLAN_CACHE_KEY + userId;
        redisTemplate.opsForValue().set(cacheKey, cacheData, 1, TimeUnit.HOURS);
    }

    // ==================== 私有方法 ====================

    /**
     * 获取用户生效的功能配额
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getEffectiveFeatures(Long userId) {
        // 优先从 Redis 缓存取
        String cacheKey = PLAN_CACHE_KEY + userId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof Map) {
                Map<String, Object> cacheMap = (Map<String, Object>) cached;
                // 移除内部元数据 key
                cacheMap = new HashMap<>(cacheMap);
                cacheMap.keySet().removeIf(k -> k.startsWith("_"));
                return cacheMap;
            }
        } catch (Exception ignored) {}

        // 查用户激活的订阅
        UserMembership active = getActiveMembership(userId);
        if (active != null) {
            MembershipPlan plan = planMapper.selectById(active.getPlanId());
            if (plan != null && plan.getFeatures() != null) {
                return parseFeatures(plan.getFeatures());
            }
        }

        // 兜底：免费套餐
        return new HashMap<>(DEFAULT_FREE_FEATURES);
    }

    private UserMembership getActiveMembership(Long userId) {
        return membershipMapper.selectOne(
                new LambdaQueryWrapper<UserMembership>()
                        .eq(UserMembership::getUserId, userId)
                        .eq(UserMembership::getStatus, "ACTIVE")
                        .and(w -> w.isNull(UserMembership::getExpiresAt)
                                .or().gt(UserMembership::getExpiresAt, LocalDateTime.now()))
        );
    }

    private String buildQuotaKey(Long userId, String featureKey) {
        String datePart;
        if (MONTHLY_FEATURES.contains(featureKey)) {
            datePart = YearMonth.now().toString(); // 2026-06
        } else {
            datePart = LocalDate.now().toString(); // 2026-06-13
        }
        return QUOTA_KEY_PREFIX + userId + ":" + datePart + ":" + featureKey;
    }

    /**
     * 计算配额 key 的 TTL（秒）
     * 日配额：到今天结束 + 24h 缓冲 = 约 48h
     * 月配额：到本月结束 + 24h 缓冲
     */
    private long getQuotaTtl(String featureKey) {
        if (MONTHLY_FEATURES.contains(featureKey)) {
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
            return ChronoUnit.SECONDS.between(LocalDateTime.now(), endOfMonth) + 86400;
        }
        // 日配额：48 小时足够安全
        return 48 * 3600;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseFeatures(String featuresJson) {
        if (featuresJson == null || featuresJson.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(featuresJson,
                    new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析 features JSON 失败: {}", featuresJson, e);
            return new HashMap<>();
        }
    }

    private List<MembershipPlan> getCachedPlans() {
        if (cachedPlans != null && plansCachedAt != null &&
                plansCachedAt.plusHours(1).isAfter(LocalDateTime.now())) {
            return cachedPlans;
        }
        cachedPlans = planMapper.selectList(
                new LambdaQueryWrapper<MembershipPlan>()
                        .eq(MembershipPlan::getIsActive, true)
                        .orderByAsc(MembershipPlan::getSortOrder)
        );
        plansCachedAt = LocalDateTime.now();
        return cachedPlans;
    }

    private int toInt(Object quotaObj) {
        if (quotaObj instanceof Number n) {
            return n.intValue();
        }
        if (quotaObj instanceof String s) {
            try { return Integer.parseInt(s); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private UserMembershipVO buildMembershipVO(Long userId, Map<String, Object> features) {
        UserMembership active = getActiveMembership(userId);
        String planCode, planName;
        LocalDateTime expiresAt = null;
        Boolean autoRenew = false;

        if (active != null) {
            MembershipPlan plan = planMapper.selectById(active.getPlanId());
            planCode = plan != null ? plan.getPlanCode() : "free";
            planName = plan != null ? plan.getPlanName() : "免费版";
            expiresAt = active.getExpiresAt();
            autoRenew = active.getAutoRenew();
        } else {
            planCode = "free";
            planName = "免费版";
        }

        // 构建各功能配额
        Map<String, UserMembershipVO.FeatureQuotaVO> featureMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : features.entrySet()) {
            String key = entry.getKey();
            int limit = toInt(entry.getValue());
            int used = getUsage(userId, key);

            UserMembershipVO.FeatureQuotaVO quota = UserMembershipVO.FeatureQuotaVO.builder()
                    .allowed(limit != 0 ? used < limit || limit == -1 : false)
                    .used(used)
                    .limit(limit)
                    .build();
            featureMap.put(key, quota);
        }

        return UserMembershipVO.builder()
                .plan(planCode)
                .planName(planName)
                .expiresAt(expiresAt)
                .autoRenew(autoRenew)
                .features(featureMap)
                .build();
    }
}
