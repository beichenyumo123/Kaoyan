package com.zzu.kaoyan.module.mistake.scheduler;

import com.zzu.kaoyan.module.mistake.service.EbbinghausService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EbbinghausScheduler {

    private final EbbinghausService ebbinghausService;

    /**
     * 每天早上 7:00 自动为所有用户生成当日复习计划
     * 考研学生通常早起学习，7点生成计划正好赶上起床后的复习时间
     */
    @Scheduled(cron = "0 0 7 * * ?")
    public void generateDailyPlans() {
        log.info("开始生成每日艾宾浩斯复习计划...");
        try {
            ebbinghausService.generateDailyPlansForAllUsers();
            log.info("每日复习计划生成完成");
        } catch (Exception e) {
            log.error("生成每日复习计划失败", e);
        }
    }
}
