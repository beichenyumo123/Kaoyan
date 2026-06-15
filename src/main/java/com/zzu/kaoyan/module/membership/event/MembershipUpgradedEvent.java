package com.zzu.kaoyan.module.membership.event;

/**
 * 会员升级事件 — 支付成功后发布，供其他模块监听
 * 例如：发送站内信通知、解锁徽章等
 */
public record MembershipUpgradedEvent(Long userId, Long oldPlanId, Long newPlanId) {
}
