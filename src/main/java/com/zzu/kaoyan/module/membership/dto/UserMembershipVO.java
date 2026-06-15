package com.zzu.kaoyan.module.membership.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 当前用户会员状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMembershipVO {

    private String plan;
    private String planName;
    private LocalDateTime expiresAt;
    private Boolean autoRenew;

    /** 各功能的使用情况: {"ai_ask": {"allowed": true, "used": 3, "limit": 5}, ...} */
    private Map<String, FeatureQuotaVO> features;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureQuotaVO {
        private Boolean allowed;
        private Integer used;
        private Integer limit;  // -1 = 无限制, 0 = 禁止
    }
}
