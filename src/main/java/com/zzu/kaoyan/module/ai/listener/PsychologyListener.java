package com.zzu.kaoyan.module.ai.listener;

import com.zzu.kaoyan.module.ai.agent.PsychologyAgent;
import com.zzu.kaoyan.module.ai.event.UserCheckInEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PsychologyListener {

    private static final Logger log = LoggerFactory.getLogger(PsychologyListener.class);

    private final PsychologyAgent psychologyAgent;

    public PsychologyListener(PsychologyAgent psychologyAgent) {
        this.psychologyAgent = psychologyAgent;
    }

    @Async
    @EventListener
    public void onUserCheckIn(UserCheckInEvent event) {
        String notes = event.getNotes();
        if (notes == null || notes.isBlank()) {
            log.info("PsychologyListener 跳过 — userId={}, 无打卡感言", event.getUserId());
            return;
        }

        log.info("PsychologyListener 收到打卡事件 — userId={}", event.getUserId());
        try {
            String reason = String.format("打卡感言分析（连续%d天）", event.getContinuousDays());
            psychologyAgent.analyzeAndIntervene(event.getUserId(), notes, reason);
        } catch (Exception e) {
            log.error("PsychologyAgent 执行失败 — userId={}", event.getUserId(), e);
        }
    }
}
