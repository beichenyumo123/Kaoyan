package com.zzu.kaoyan.module.membership.service;

import com.zzu.kaoyan.module.membership.dto.MembershipPlanVO;
import com.zzu.kaoyan.module.membership.dto.UserMembershipVO;

import java.util.List;

/**
 * 会员服务接口
 *
 * 核心职责：配额检查（Redis）、用量记录、会员信息查询
 */
public interface MembershipService {

    /** 访问检查结果 */
    enum AccessResult { OK, VIP_REQUIRED, QUOTA_EXHAUSTED }

    /**
     * 检查用户对某功能是否有访问权限
     * @param userId 用户 ID
     * @param featureKey 功能标识，如 "ai_ask", "ocr"
     * @return 检查结果
     */
    AccessResult checkAccess(Long userId, String featureKey);

    /**
     * 原子扣减配额（Redis Lua）
     * @param userId 用户 ID
     * @param featureKey 功能标识
     * @return true=扣减成功, false=配额不足
     */
    boolean tryConsume(Long userId, String featureKey);

    /**
     * 记录功能使用（在业务成功后调用）
     * @param userId 用户 ID
     * @param featureKey 功能标识
     */
    void recordUsage(Long userId, String featureKey);

    /**
     * 退款配额（SSE 流失败时调用）
     * @param userId 用户 ID
     * @param featureKey 功能标识
     */
    void refundUsage(Long userId, String featureKey);

    /**
     * 获取当前用户会员状态
     */
    UserMembershipVO getCurrentMembership(Long userId);

    /**
     * 获取所有可选套餐
     */
    List<MembershipPlanVO> getPlans();

    /**
     * 获取某个功能的当日已用量
     */
    int getUsage(Long userId, String featureKey);

    /**
     * 预热会员缓存（登录后调用）
     */
    void warmCache(Long userId);
}
