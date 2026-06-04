package com.zzu.kaoyan.module.ai.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.module.ai.entity.AiInterventionLog;
import com.zzu.kaoyan.module.ai.entity.AiUserEvent;
import com.zzu.kaoyan.module.ai.entity.UserAiProfile;
import com.zzu.kaoyan.module.ai.mapper.AiInterventionLogMapper;
import com.zzu.kaoyan.module.ai.mapper.AiUserEventMapper;
import com.zzu.kaoyan.module.ai.mapper.UserAiProfileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 行为分析 Agent — 每天 22:00 分析用户行为数据，更新画像，生成学习建议。
 */
@Component
public class BehaviorAnalysisAgent {

    private static final Logger log = LoggerFactory.getLogger(BehaviorAnalysisAgent.class);

    private final AiUserEventMapper userEventMapper;
    private final UserAiProfileMapper profileMapper;
    private final AiInterventionLogMapper interventionMapper;
    private final ObjectMapper objectMapper;

    public BehaviorAnalysisAgent(AiUserEventMapper userEventMapper,
                                 UserAiProfileMapper profileMapper,
                                 AiInterventionLogMapper interventionMapper,
                                 ObjectMapper objectMapper) {
        this.userEventMapper = userEventMapper;
        this.profileMapper = profileMapper;
        this.interventionMapper = interventionMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 定时任务：每天 22:00 分析当日用户行为
     */
    @Scheduled(cron = "0 0 22 * * ?")
    public void analyzeDailyBehavior() {
        log.info("BehaviorAnalysisAgent 每日行为分析启动");
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime dayStart = today.atStartOfDay();
            LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

            // 获取今日有行为事件的用户 ID
            List<AiUserEvent> todayEvents = userEventMapper.selectList(
                    new LambdaQueryWrapper<AiUserEvent>()
                            .ge(AiUserEvent::getCreatedAt, dayStart)
                            .lt(AiUserEvent::getCreatedAt, dayEnd));

            Set<Long> userIds = todayEvents.stream()
                    .map(AiUserEvent::getUserId)
                    .collect(Collectors.toSet());

            log.info("BehaviorAnalysisAgent 今日活跃用户数={}", userIds.size());

            int analyzed = 0;
            for (Long userId : userIds) {
                try {
                    analyzeUser(userId, todayEvents.stream()
                            .filter(e -> e.getUserId().equals(userId))
                            .collect(Collectors.toList()));
                    analyzed++;
                } catch (Exception e) {
                    log.error("BehaviorAnalysisAgent 用户分析失败 — userId={}", userId, e);
                }
            }

            log.info("BehaviorAnalysisAgent 每日行为分析完成 — 分析用户数={}", analyzed);
        } catch (Exception e) {
            log.error("BehaviorAnalysisAgent 每日行为分析执行异常", e);
        }
    }

    private void analyzeUser(Long userId, List<AiUserEvent> events) throws JsonProcessingException {
        // 统计行为数据
        long viewCount = events.stream()
                .filter(e -> "VIEW_POST".equals(e.getEventType())).count();
        long collectCount = events.stream()
                .filter(e -> "COLLECT_POST".equals(e.getEventType())).count();
        long searchCount = events.stream()
                .filter(e -> "SEARCH".equals(e.getEventType())).count();
        long likeCount = events.stream()
                .filter(e -> "LIKE_POST".equals(e.getEventType())).count();

        // 提取搜索关键词
        List<String> searchKeywords = events.stream()
                .filter(e -> "SEARCH".equals(e.getEventType()) && e.getEventData() != null)
                .map(e -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = objectMapper.readValue(e.getEventData(), Map.class);
                        return (String) data.get("keyword");
                    } catch (Exception ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 更新用户画像
        updateBehaviorProfile(userId, viewCount, collectCount, searchCount, likeCount, searchKeywords);

        // 如果浏览量较高但任务完成率低，生成提醒
        if (viewCount >= 5) {
            generateSuggestion(userId, viewCount, searchKeywords);
        }
    }

    private void updateBehaviorProfile(Long userId, long viewCount, long collectCount,
                                       long searchCount, long likeCount,
                                       List<String> searchKeywords) throws JsonProcessingException {
        UserAiProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserAiProfile>()
                        .eq(UserAiProfile::getUserId, userId));
        if (profile == null) {
            return;
        }

        // 解析现有认知画像
        Map<String, Object> cognitive;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> existing = objectMapper.readValue(
                    profile.getCognitiveProfile() != null ? profile.getCognitiveProfile() : "{}", Map.class);
            cognitive = existing;
        } catch (Exception e) {
            cognitive = new HashMap<>();
        }

        // 更新行为数据
        Map<String, Object> browsePattern = new HashMap<>();
        browsePattern.put("todayViews", viewCount);
        browsePattern.put("todayCollects", collectCount);
        browsePattern.put("todaySearches", searchCount);
        browsePattern.put("todayLikes", likeCount);
        cognitive.put("browsePattern", browsePattern);
        cognitive.put("lastActiveAt", LocalDateTime.now().toString());

        // 记录兴趣关键词
        if (!searchKeywords.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<String> existingKeywords = (List<String>) cognitive.getOrDefault("interestKeywords", new ArrayList<>());
            Set<String> allKeywords = new LinkedHashSet<>(existingKeywords);
            allKeywords.addAll(searchKeywords);
            // 只保留最近 20 个关键词
            List<String> limited = new ArrayList<>(allKeywords);
            if (limited.size() > 20) {
                limited = limited.subList(limited.size() - 20, limited.size());
            }
            cognitive.put("interestKeywords", limited);
        }

        profile.setCognitiveProfile(objectMapper.writeValueAsString(cognitive));
        profileMapper.updateById(profile);
    }

    private void generateSuggestion(Long userId, long viewCount, List<String> searchKeywords) {
        // 简单规则：浏览量高但可能没有完成任务，生成提醒
        String tip;
        if (!searchKeywords.isEmpty()) {
            String topKeyword = searchKeywords.get(searchKeywords.size() - 1);
            tip = "今天你浏览了 " + viewCount + " 篇帖子，搜索了「" + topKeyword +
                    "」相关内容。建议把搜索到的知识点整理成笔记，加深记忆。";
        } else {
            tip = "今天你浏览了 " + viewCount + " 篇帖子，学习很积极！记得及时总结归纳哦。";
        }

        AiInterventionLog logEntity = new AiInterventionLog();
        logEntity.setUserId(userId);
        logEntity.setAgentName("Behavior");
        logEntity.setTriggerReason("每日行为分析");
        logEntity.setInterventionContent(tip);
        logEntity.setUserReaction("UNREAD");
        logEntity.setCreatedAt(LocalDateTime.now());
        interventionMapper.insert(logEntity);
    }
}
