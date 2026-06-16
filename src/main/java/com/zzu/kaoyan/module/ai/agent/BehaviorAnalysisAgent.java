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
import com.zzu.kaoyan.module.ai.service.AiAgentService;
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

    private static final String EXERCISE_PROMPT =
            """
            你是一位考研出题老师。请根据学生今天浏览的关键词，生成 3 道针对性练习题。
            每道题必须包含：**题目**、**解析**（详细推导过程）、**答案**。
            答案必须用 HTML <details> 标签包裹（前端已开启 html:true，会渲染为可折叠区域）。
            输出纯 Markdown 格式，不要代码块包裹，不要前缀说明。格式如下：

            ### 1. {知识点名称}
            **题目**：{题目描述，尽量用 LaTeX}

            **解析**：
            {详细推导过程}

            <details>
            <summary><b>查看答案</b></summary>

            **答案**：{最终答案}

            </details>

            ---

            （以此类推，共 3 道）
            """;

    private final AiUserEventMapper userEventMapper;
    private final UserAiProfileMapper profileMapper;
    private final AiInterventionLogMapper interventionMapper;
    private final ObjectMapper objectMapper;
    private final AiAgentService aiAgentService;

    public BehaviorAnalysisAgent(AiUserEventMapper userEventMapper,
                                 UserAiProfileMapper profileMapper,
                                 AiInterventionLogMapper interventionMapper,
                                 ObjectMapper objectMapper,
                                 AiAgentService aiAgentService) {
        this.userEventMapper = userEventMapper;
        this.profileMapper = profileMapper;
        this.interventionMapper = interventionMapper;
        this.objectMapper = objectMapper;
        this.aiAgentService = aiAgentService;
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
        String tip;
        String detailMarkdown;
        String linkTarget = "";
        String linkLabel = "";

        if (!searchKeywords.isEmpty()) {
            String topKeyword = searchKeywords.get(searchKeywords.size() - 1);
            tip = "今天你浏览了 " + viewCount + " 篇帖子，搜索了「" + topKeyword +
                    "」相关内容。系统已根据你的兴趣生成了 3 道练习题，点击查看详情 →";

            // 第一步：构建浏览分析部分
            StringBuilder sb = new StringBuilder();
            sb.append("## 📊 浏览行为分析\n\n");
            sb.append("| 指标 | 今日数据 |\n");
            sb.append("|------|--------|\n");
            sb.append("| 浏览帖子 | ").append(viewCount).append(" 篇 |\n");
            sb.append("\n");
            sb.append("### 🔍 关注主题\n\n");
            for (String kw : searchKeywords) {
                sb.append("- **").append(kw).append("**\n");
            }
            sb.append("\n");
            sb.append("---\n\n");

            // 第二步：调用 LLM 预生成习题内容
            String keywordList = String.join("、", searchKeywords.stream()
                    .distinct().limit(5).toList());
            String exercisePrompt = "学生关注的知识点：" + keywordList;
            String exercises = null;
            try {
                exercises = aiAgentService.chat(EXERCISE_PROMPT, exercisePrompt);
                log.info("BehaviorAnalysisAgent LLM 习题生成 — userId={}, length={}", userId,
                        exercises != null ? exercises.length() : 0);
            } catch (Exception e) {
                log.error("BehaviorAnalysisAgent LLM 习题生成失败 — userId={}", userId, e);
            }

            if (exercises != null && !exercises.isBlank()) {
                sb.append("## 📝 推荐习题\n\n");
                sb.append(exercises.trim());
                sb.append("\n\n");
            } else {
                // LLM 失败时回退到简单推荐
                sb.append("## 📝 推荐习题\n\n");
                for (String kw : searchKeywords.stream().distinct().limit(3).toList()) {
                    sb.append("- **").append(kw).append("** — 建议在知识库中搜索相关习题\n");
                }
                sb.append("\n");
            }

            sb.append("> 💡 还想继续练习？[去 AI 答疑 →](/ai/ask?question=")
              .append(topKeyword).append(" 常见题型)");
            detailMarkdown = sb.toString();

            linkTarget = "/ai/ask?question=" + topKeyword + " 常见题型";
            linkLabel = "去 AI 答疑继续练习 →";
        } else {
            tip = "今天你浏览了 " + viewCount + " 篇帖子，学习很积极！记得及时总结归纳哦。";
            detailMarkdown = "## 📊 浏览行为分析\n\n今日浏览 " + viewCount +
                    " 篇帖子，学习活跃度良好。\n\n> 建议开启错题本整理今日所学知识点。";
        }

        AiInterventionLog logEntity = new AiInterventionLog();
        logEntity.setUserId(userId);
        logEntity.setAgentName("行为分析师");
        logEntity.setTriggerReason("每日行为分析");
        logEntity.setInterventionContent(tip);
        logEntity.setDetailMarkdown(detailMarkdown);
        logEntity.setLinkTarget(linkTarget);
        logEntity.setLinkLabel(linkLabel);
        logEntity.setUserReaction("UNREAD");
        logEntity.setCreatedAt(LocalDateTime.now());
        interventionMapper.insert(logEntity);
    }
}
