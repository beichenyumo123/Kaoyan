package com.zzu.kaoyan.module.membership.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.membership.dto.MembershipPlanVO;
import com.zzu.kaoyan.module.membership.dto.UpgradeRequestDTO;
import com.zzu.kaoyan.module.membership.dto.UserMembershipVO;
import com.zzu.kaoyan.module.membership.entity.MembershipOrder;
import com.zzu.kaoyan.module.membership.entity.MembershipPlan;
import com.zzu.kaoyan.module.membership.mapper.MembershipOrderMapper;
import com.zzu.kaoyan.module.membership.mapper.MembershipPlanMapper;
import com.zzu.kaoyan.module.membership.mapper.UserMembershipMapper;
import com.zzu.kaoyan.module.membership.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 会员服务 API
 */
@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
@Tag(name = "会员服务", description = "套餐查询、会员状态、升级/取消")
public class MembershipController {

    private final MembershipService membershipService;
    private final MembershipPlanMapper planMapper;
    private final MembershipOrderMapper orderMapper;
    private final UserMembershipMapper membershipMapper;

    // ============================================================
    // 套餐
    // ============================================================

    @GetMapping("/plans")
    @Operation(summary = "获取所有可选套餐")
    public Result<List<MembershipPlanVO>> getPlans() {
        return Result.success(membershipService.getPlans());
    }

    // ============================================================
    // 我的会员
    // ============================================================

    @GetMapping("/me")
    @SaCheckLogin
    @Operation(summary = "当前用户会员状态与配额")
    public Result<UserMembershipVO> getMyMembership() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(membershipService.getCurrentMembership(userId));
    }

    @GetMapping("/check/{featureKey}")
    @SaCheckLogin
    @Operation(summary = "检查某功能是否可用")
    public Result<Map<String, Object>> checkFeature(@PathVariable String featureKey) {
        Long userId = StpUtil.getLoginIdAsLong();
        MembershipService.AccessResult result = membershipService.checkAccess(userId, featureKey);
        int used = membershipService.getUsage(userId, featureKey);
        int limit = getQuotaLimit(userId, featureKey);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("featureKey", featureKey);
        data.put("available", result == MembershipService.AccessResult.OK);
        data.put("used", used);
        data.put("limit", limit);
        data.put("remaining", limit == -1 ? -1 : Math.max(0, limit - used));
        data.put("reason", result == MembershipService.AccessResult.VIP_REQUIRED ? "VIP_REQUIRED" :
                           result == MembershipService.AccessResult.QUOTA_EXHAUSTED ? "QUOTA_EXHAUSTED" : "OK");
        return Result.success(data);
    }

    // ============================================================
    // 升级 / 取消
    // ============================================================

    @PostMapping("/upgrade")
    @SaCheckLogin
    @Operation(summary = "升级套餐（创建订单）")
    public Result<Map<String, Object>> upgrade(@RequestBody UpgradeRequestDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        MembershipPlan plan = planMapper.selectById(dto.getPlanId());
        if (plan == null || !plan.getIsActive()) {
            throw new BusinessException(400, "套餐不存在或已下架");
        }
        if ("free".equals(plan.getPlanCode())) {
            throw new BusinessException(400, "免费版无需购买");
        }

        // 创建订单
        String orderNo = generateOrderNo(userId);
        MembershipOrder order = new MembershipOrder();
        order.setUserId(userId);
        order.setPlanId(plan.getId());
        order.setOrderNo(orderNo);
        order.setAmount(plan.getPrice());
        order.setPaymentStatus("PENDING");
        orderMapper.insert(order);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderNo", orderNo);
        data.put("planName", plan.getPlanName());
        data.put("amount", plan.getPrice());
        data.put("status", "PENDING");
        return Result.success(data);
    }

    @PostMapping("/cancel")
    @SaCheckLogin
    @Operation(summary = "取消自动续费")
    public Result<String> cancel() {
        Long userId = StpUtil.getLoginIdAsLong();
        var membership = membershipMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                        com.zzu.kaoyan.module.membership.entity.UserMembership>()
                        .eq(com.zzu.kaoyan.module.membership.entity.UserMembership::getUserId, userId)
                        .eq(com.zzu.kaoyan.module.membership.entity.UserMembership::getStatus, "ACTIVE")
        );
        if (membership == null) {
            throw new BusinessException(404, "未找到激活的会员订阅");
        }
        membership.setAutoRenew(false);
        membershipMapper.updateById(membership);
        return Result.success("已取消自动续费");
    }

    // ==================== 私有方法 ====================

    private String generateOrderNo(Long userId) {
        return "VIP" + System.currentTimeMillis() + String.format("%06d", userId % 1000000);
    }

    private int getQuotaLimit(Long userId, String featureKey) {
        try {
            UserMembershipVO vo = membershipService.getCurrentMembership(userId);
            UserMembershipVO.FeatureQuotaVO fq = vo.getFeatures().get(featureKey);
            if (fq != null) return fq.getLimit();
        } catch (Exception ignored) {}
        return 0;
    }
}
