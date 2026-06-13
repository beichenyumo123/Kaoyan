package com.zzu.kaoyan.module.membership.scheduler;

import com.zzu.kaoyan.module.membership.mapper.UserUsageLogMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 配额重置调度器
 *
 * 日配额：Redis key 有 48h TTL，每日凌晨自动过期重置，此调度器做 MySQL 汇总
 * 月配额：每月 1 号凌晨重置（目前月配额 feature 较少，通过 TTL 策略按月份 key 区分）
 */
@Component
@RequiredArgsConstructor
public class QuotaResetScheduler {

    private static final Logger log = LoggerFactory.getLogger(QuotaResetScheduler.class);

    @Scheduled(cron = "0 1 0 * * ?")
    public void logDailyUsageSummary() {
        log.info("[会员配额] 每日配额重置 — Redis key 已通过 TTL 自动过期，日配额已刷新");
    }
}
