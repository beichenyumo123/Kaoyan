package com.zzu.kaoyan.module.ai.listener;

import com.zzu.kaoyan.module.ai.agent.PsychologyAgent;
import com.zzu.kaoyan.module.ai.event.UserDiaryCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 监听日记创建事件 — 触发 PsychologyAgent 对日记内容进行情感分析。
 */
@Component
public class DiaryPsychologyListener {

    private static final Logger log = LoggerFactory.getLogger(DiaryPsychologyListener.class);

    private final PsychologyAgent psychologyAgent;

    public DiaryPsychologyListener(PsychologyAgent psychologyAgent) {
        this.psychologyAgent = psychologyAgent;
    }

    @Async
    @EventListener
    public void onDiaryCreated(UserDiaryCreatedEvent event) {
        log.info("DiaryPsychologyListener 收到日记事件 — userId={}", event.getUserId());
        try {
            psychologyAgent.analyzeAndIntervene(event.getUserId(), event.getContent(), "日记情感分析");
        } catch (Exception e) {
            log.error("PsychologyAgent（日记）执行失败 — userId={}", event.getUserId(), e);
        }
    }
}
