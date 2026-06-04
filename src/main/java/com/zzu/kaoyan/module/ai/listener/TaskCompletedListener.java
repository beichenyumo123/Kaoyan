package com.zzu.kaoyan.module.ai.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.ai.entity.AiDailyTask;
import com.zzu.kaoyan.module.ai.entity.UserAiProfile;
import com.zzu.kaoyan.module.ai.event.TaskCompletedEvent;
import com.zzu.kaoyan.module.ai.mapper.AiDailyTaskMapper;
import com.zzu.kaoyan.module.ai.mapper.UserAiProfileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 监听任务完成事件 — 更新用户认知画像中的累计完成任务数。
 */
@Component
public class TaskCompletedListener {

    private static final Logger log = LoggerFactory.getLogger(TaskCompletedListener.class);

    private final AiDailyTaskMapper taskMapper;
    private final UserAiProfileMapper profileMapper;

    public TaskCompletedListener(AiDailyTaskMapper taskMapper, UserAiProfileMapper profileMapper) {
        this.taskMapper = taskMapper;
        this.profileMapper = profileMapper;
    }

    @Async
    @EventListener
    public void onTaskCompleted(TaskCompletedEvent event) {
        log.info("TaskCompletedListener 收到事件 — userId={}, taskId={}", event.getUserId(), event.getTaskId());
        try {
            // 统计该用户已完成的任务总数
            long completedCount = taskMapper.selectCount(
                    new LambdaQueryWrapper<AiDailyTask>()
                            .eq(AiDailyTask::getUserId, event.getUserId())
                            .eq(AiDailyTask::getStatus, 1));

            // 更新认知画像中的完成任务数
            UserAiProfile profile = profileMapper.selectOne(
                    new LambdaQueryWrapper<UserAiProfile>().eq(UserAiProfile::getUserId, event.getUserId()));

            if (profile != null && profile.getCognitiveProfile() != null) {
                // 在现有 JSON 末尾追加 completedTaskCount 字段（简单拼接，避免引入完整 JSON 库）
                String cognitive = profile.getCognitiveProfile();
                // 移除末尾的 }，追加新字段
                if (cognitive.endsWith("}")) {
                    cognitive = cognitive.substring(0, cognitive.length() - 1)
                            + ",\"completedTaskCount\":" + completedCount + "}";
                }
                profile.setCognitiveProfile(cognitive);
                profile.setUpdatedAt(LocalDateTime.now());
                profileMapper.updateById(profile);
                log.info("TaskCompletedListener 已更新认知画像 — userId={}, completedTaskCount={}",
                        event.getUserId(), completedCount);
            }
        } catch (Exception e) {
            log.error("TaskCompletedListener 执行失败 — userId={}", event.getUserId(), e);
        }
    }
}
