package com.zzu.kaoyan.module.ai.listener;

import com.zzu.kaoyan.module.ai.agent.PlannerAgent;
import com.zzu.kaoyan.module.ai.event.UserCheckInEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PlannerListener {

    private static final Logger log = LoggerFactory.getLogger(PlannerListener.class);

    private final PlannerAgent plannerAgent;

    public PlannerListener(PlannerAgent plannerAgent) {
        this.plannerAgent = plannerAgent;
    }

    @Async
    @EventListener
    public void onUserCheckIn(UserCheckInEvent event) {
        log.info("PlannerListener 收到打卡事件 — userId={}", event.getUserId());
        try {
            plannerAgent.planForUser(event.getUserId(), event.getContinuousDays(),
                    event.getTotalCheckDays(), event.getStudyHours());
        } catch (Exception e) {
            log.error("PlannerAgent 执行失败 — userId={}", event.getUserId(), e);
        }
    }
}
