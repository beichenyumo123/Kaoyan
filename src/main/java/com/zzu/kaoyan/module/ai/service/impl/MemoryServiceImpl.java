package com.zzu.kaoyan.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.activity.entity.po.UserStudyPO;
import com.zzu.kaoyan.module.activity.mapper.UserStudyMapper;
import com.zzu.kaoyan.module.ai.entity.AiChatSession;
import com.zzu.kaoyan.module.ai.entity.AiDailyTask;
import com.zzu.kaoyan.module.ai.entity.UserAiProfile;
import com.zzu.kaoyan.module.ai.mapper.AiChatSessionMapper;
import com.zzu.kaoyan.module.ai.mapper.AiDailyTaskMapper;
import com.zzu.kaoyan.module.ai.mapper.UserAiProfileMapper;
import com.zzu.kaoyan.module.ai.service.EmbeddingService;
import com.zzu.kaoyan.module.ai.service.MemoryService;
import com.zzu.kaoyan.module.ai.entity.UserAiProfile;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.experience.entity.ExperiencePost;
import com.zzu.kaoyan.module.experience.mapper.ExperiencePostMapper;
import com.zzu.kaoyan.module.interview.entity.InterviewReport;
import com.zzu.kaoyan.module.interview.entity.InterviewSession;
import com.zzu.kaoyan.module.interview.mapper.InterviewReportMapper;
import com.zzu.kaoyan.module.interview.mapper.InterviewSessionMapper;
import com.zzu.kaoyan.module.mistake.mapper.MistakeNoteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 记忆服务实现 — 聚合多表数据 + 语义检索为统一的学员档案上下文。
 *
 * <p>数据来源：
 * <ul>
 *   <li>user_ai_profile → 认知画像 + 心理状态</li>
 *   <li>mistake_note → 薄弱知识点 TOP5</li>
 *   <li>ai_daily_task → 今日任务进度</li>
 *   <li>interaction_user_study → 连续打卡天数</li>
 *   <li>ai_chat_session → 最近对话标题</li>
 *   <li>embeddingService → 语义检索相似历史对话</li>
 * </ul>
 */
@Service
public class MemoryServiceImpl implements MemoryService {

    private static final Logger log = LoggerFactory.getLogger(MemoryServiceImpl.class);

    private final UserAiProfileMapper profileMapper;
    private final MistakeNoteMapper mistakeNoteMapper;
    private final AiDailyTaskMapper taskMapper;
    private final UserStudyMapper userStudyMapper;
    private final AiChatSessionMapper chatSessionMapper;
    private final EmbeddingService embeddingService;
    private final InterviewReportMapper interviewReportMapper;
    private final InterviewSessionMapper interviewSessionMapper;
    private final UserMapper userMapper;
    private final ExperiencePostMapper experiencePostMapper;

    public MemoryServiceImpl(UserAiProfileMapper profileMapper,
                             MistakeNoteMapper mistakeNoteMapper,
                             AiDailyTaskMapper taskMapper,
                             UserStudyMapper userStudyMapper,
                             AiChatSessionMapper chatSessionMapper,
                             EmbeddingService embeddingService,
                             InterviewReportMapper interviewReportMapper,
                             InterviewSessionMapper interviewSessionMapper,
                             UserMapper userMapper,
                             ExperiencePostMapper experiencePostMapper) {
        this.profileMapper = profileMapper;
        this.mistakeNoteMapper = mistakeNoteMapper;
        this.taskMapper = taskMapper;
        this.userStudyMapper = userStudyMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.embeddingService = embeddingService;
        this.interviewReportMapper = interviewReportMapper;
        this.interviewSessionMapper = interviewSessionMapper;
        this.userMapper = userMapper;
        this.experiencePostMapper = experiencePostMapper;
    }

    @Override
    public String buildContext(Long userId) {
        return buildContext(userId, null);
    }

    @Override
    public String buildContext(Long userId, String subject) {
        StringBuilder ctx = new StringBuilder();
        ctx.append("\n\n【学员档案】\n");

        try {
            // 1. 认知画像 + 心理状态
            UserAiProfile profile = profileMapper.selectOne(
                    new LambdaQueryWrapper<UserAiProfile>().eq(UserAiProfile::getUserId, userId));

            if (profile != null) {
                // 认知画像
                if (profile.getCognitiveProfile() != null && !profile.getCognitiveProfile().isBlank()
                        && !profile.getCognitiveProfile().equals("{}")) {
                    ctx.append("## 学习画像\n");
                    appendCognitiveSummary(ctx, profile.getCognitiveProfile());
                }

                // 心理状态
                if (profile.getPsychologicalProfile() != null && !profile.getPsychologicalProfile().isBlank()
                        && !profile.getPsychologicalProfile().equals("{}")) {
                    ctx.append("## 心理状态\n");
                    appendPsychologicalSummary(ctx, profile.getPsychologicalProfile());
                }
            }

            // 2. 薄弱知识点（来自错题本）
            String weakness = buildWeaknessSection(userId, subject);
            if (weakness != null) {
                ctx.append("## 薄弱知识点（近期错题统计）\n").append(weakness);
            }

            // 3. 面试报告（最近一次）
            String interview = buildInterviewSection(userId);
            if (interview != null) {
                ctx.append("## 模拟面试记录\n").append(interview);
            }

            // 4. 上岸经验贴（匹配目标院校）
            String experience = buildExperienceSection(userId);
            if (experience != null) {
                ctx.append("## 上岸经验（同目标院校学长）\n").append(experience);
            }

            // 5. 今日学习进度
            String progress = buildTodayProgress(userId);
            if (progress != null) {
                ctx.append("## 今日学习进度\n").append(progress);
            }

            // 4. 学习统计
            String stats = buildStudyStats(userId);
            if (stats != null) {
                ctx.append("## 学习统计\n").append(stats);
            }

            // 5. 最近对话
            String recentChats = buildRecentChats(userId);
            if (recentChats != null) {
                ctx.append("## 最近对话\n").append(recentChats);
            }

        } catch (Exception e) {
            log.warn("构建 Memory 上下文异常 — userId={}, error={}", userId, e.getMessage());
        }

        return ctx.toString().trim();
    }

    /**
     * 追加语义检索结果到已有上下文（在 TutorAgent 提问时调用）。
     * 需要 question 参数来检索相似历史。
     */
    public String enrichWithSemanticMemory(Long userId, String question, String subject) {
        List<String> similar = embeddingService.search(userId, question, subject);
        if (similar.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n【语义记忆 — 你可能相关的历史记录】\n");
        for (int i = 0; i < similar.size(); i++) {
            sb.append(i + 1).append(". ").append(similar.get(i)).append("\n");
        }
        sb.append("\n如果以上历史记录与当前问题相关，请引用并关联讲解。");
        return sb.toString();
    }

    // ────────────────── 各模块构建 ──────────────────

    /**
     * 认知画像摘要 — 提取关键字段，避免直接丢原始 JSON 给 LLM。
     */
    private void appendCognitiveSummary(StringBuilder ctx, String cognitiveJson) {
        try {
            // 简单解析 JSON 字段（避免依赖 Jackson 对残缺 JSON 的严格校验）
            Integer totalDays = extractInt(cognitiveJson, "totalCheckDays");
            Integer continuousDays = extractInt(cognitiveJson, "continuousDays");
            Integer totalHours = extractInt(cognitiveJson, "totalStudyHours");
            String lastActive = extractString(cognitiveJson, "lastActive");
            String keywords = extractArray(cognitiveJson, "interestKeywords");

            if (totalDays != null && totalDays > 0) {
                ctx.append("- 累计学习：").append(totalDays).append(" 天");
                if (totalHours != null && totalHours > 0) {
                    ctx.append("，").append(totalHours).append(" 小时");
                }
                ctx.append("\n");
            }
            if (continuousDays != null && continuousDays > 0) {
                ctx.append("- 连续打卡：").append(continuousDays).append(" 天\n");
            }
            if (lastActive != null && !lastActive.isBlank() && !"null".equals(lastActive)) {
                ctx.append("- 最近活跃：").append(lastActive).append("\n");
            }
            if (keywords != null && !keywords.isBlank()) {
                ctx.append("- 兴趣方向：").append(keywords).append("\n");
            }
        } catch (Exception e) {
            // 解析失败就跳过，不影响主流程
            log.debug("解析认知画像失败 — {}", e.getMessage());
        }
    }

    /**
     * 心理画像摘要。
     */
    private void appendPsychologicalSummary(StringBuilder ctx, String psychJson) {
        try {
            String emotion = extractString(psychJson, "recentEmotion");
            String analysis = extractString(psychJson, "lastAnalysis");

            if (emotion != null && !emotion.isBlank() && !"未知".equals(emotion)) {
                ctx.append("- 近期情绪：").append(emotion).append("\n");
            }
            if (analysis != null && !analysis.isBlank()) {
                // 限制长度，避免 prompt 过长
                String shortAnalysis = analysis.length() > 100 ? analysis.substring(0, 100) + "..." : analysis;
                ctx.append("- 分析：").append(shortAnalysis).append("\n");
            }
        } catch (Exception e) {
            log.debug("解析心理画像失败 — {}", e.getMessage());
        }
    }

    /**
     * 薄弱知识点 — 复用 TutorAgent 的逻辑，从错题本统计 TOP5。
     */
    private String buildWeaknessSection(Long userId, String subject) {
        try {
            List<Map<String, Object>> rows = mistakeNoteMapper.selectRecentKnowledgePoints(userId);
            if (rows == null || rows.isEmpty()) return null;

            Map<String, Integer> kpCount = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) {
                String kp = (String) row.get("knowledge_points");
                String rowSubject = (String) row.get("subject");
                if (kp == null || kp.isBlank()) continue;
                if (subject != null && !subject.isBlank() && !subject.equals(rowSubject)) continue;
                for (String name : kp.split("[,，、\\s]+")) {
                    name = name.trim();
                    if (!name.isEmpty()) {
                        kpCount.merge(name, 1, Integer::sum);
                    }
                }
            }

            if (kpCount.isEmpty()) return null;

            StringBuilder sb = new StringBuilder();
            kpCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> sb.append("- ").append(e.getKey())
                            .append("（出错 ").append(e.getValue()).append(" 次）\n"));

            return sb.toString();
        } catch (Exception e) {
            log.warn("构建薄弱知识点失败 — {}", e.getMessage());
            return null;
        }
    }

    /**
     * 匹配目标院校的上岸经验贴（高质量，有一定收藏量）。
     */
    private String buildExperienceSection(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null || user.getTargetSchool() == null || user.getTargetSchool().isBlank()) {
                return null;
            }

            String targetSchool = user.getTargetSchool();
            List<ExperiencePost> posts = experiencePostMapper.selectList(
                    new LambdaQueryWrapper<ExperiencePost>()
                            .eq(ExperiencePost::getTargetSchool, targetSchool)
                            .eq(ExperiencePost::getStatus, 1)
                            .ge(ExperiencePost::getCollectCount, 3)
                            .orderByDesc(ExperiencePost::getCollectCount)
                            .last("LIMIT 3"));

            if (posts.isEmpty()) {
                // 降级：只要有 1 条收藏的就展示
                posts = experiencePostMapper.selectList(
                        new LambdaQueryWrapper<ExperiencePost>()
                                .eq(ExperiencePost::getTargetSchool, targetSchool)
                                .eq(ExperiencePost::getStatus, 1)
                                .ge(ExperiencePost::getCollectCount, 1)
                                .orderByDesc(ExperiencePost::getCollectCount)
                                .last("LIMIT 2"));
            }

            if (posts.isEmpty()) return null;

            StringBuilder sb = new StringBuilder();
            sb.append("共找到 ").append(posts.size()).append(" 条目标同为「").append(targetSchool).append("」的上岸经验：\n");
            for (ExperiencePost post : posts) {
                String major = post.getTargetMajor() != null ? post.getTargetMajor() : "未知专业";
                sb.append("- **").append(major).append("**");
                if (post.getInitialExamTotal() != null) {
                    sb.append("（初试 ").append(post.getInitialExamTotal()).append(" 分）");
                }
                sb.append(" | 👍").append(post.getLikeCount() != null ? post.getLikeCount() : 0);
                sb.append(" ⭐").append(post.getCollectCount() != null ? post.getCollectCount() : 0);
                sb.append("\n");
                if (post.getTips() != null && !post.getTips().isBlank()) {
                    String tip = post.getTips().length() > 200
                            ? post.getTips().substring(0, 200) + "..."
                            : post.getTips();
                    sb.append("  > ").append(tip.replace("\n", " ")).append("\n");
                }
            }
            sb.append("\n以上是真实上岸经验，回答时可以作为参考素材提供给用户。");

            return sb.toString();
        } catch (Exception e) {
            log.debug("构建经验贴摘要失败 — {}", e.getMessage());
            return null;
        }
    }

    /**
     * 最近一次模拟面试报告摘要。
     */
    private String buildInterviewSection(Long userId) {
        try {
            // 查询用户的面试会话
            List<InterviewSession> sessions = interviewSessionMapper.selectList(
                    new LambdaQueryWrapper<InterviewSession>()
                            .eq(InterviewSession::getUserId, userId)
                            .eq(InterviewSession::getStatus, "REPORTED")
                            .orderByDesc(InterviewSession::getId)
                            .last("LIMIT 1"));

            if (sessions.isEmpty()) return null;

            InterviewSession session = sessions.get(0);
            InterviewReport report = interviewReportMapper.selectOne(
                    new LambdaQueryWrapper<InterviewReport>()
                            .eq(InterviewReport::getSessionId, session.getId()));

            if (report == null) return null;

            StringBuilder sb = new StringBuilder();
            // 面试类型 + 目标
            String typeLabel = switch (session.getInterviewType() != null ? session.getInterviewType() : "") {
                case "ENGLISH" -> "英语面试";
                case "MAJOR" -> "专业课面试";
                case "COMPREHENSIVE" -> "综合面试";
                default -> "面试";
            };
            sb.append("- 最近").append(typeLabel);
            if (session.getTargetSchool() != null && !session.getTargetSchool().isBlank()) {
                sb.append("（目标：").append(session.getTargetSchool());
                if (session.getTargetMajor() != null && !session.getTargetMajor().isBlank()) {
                    sb.append(" ").append(session.getTargetMajor());
                }
                sb.append("）");
            }
            sb.append("\n");

            // 综合评分
            if (report.getTotalScore() != null) {
                sb.append("- 综合评分：").append(report.getTotalScore()).append("/100\n");
            }

            // 薄弱项分析（关键）
            if (report.getWeaknessAnalysis() != null && !report.getWeaknessAnalysis().isBlank()) {
                String weakness = report.getWeaknessAnalysis().length() > 150
                        ? report.getWeaknessAnalysis().substring(0, 150) + "..."
                        : report.getWeaknessAnalysis();
                sb.append("- 薄弱项：").append(weakness).append("\n");
            }

            // 改进建议（取前 2 条）
            if (report.getSuggestion() != null && !report.getSuggestion().isBlank()) {
                String suggestion = report.getSuggestion().length() > 120
                        ? report.getSuggestion().substring(0, 120) + "..."
                        : report.getSuggestion();
                sb.append("- 改进建议：").append(suggestion).append("\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.debug("构建面试报告摘要失败 — {}", e.getMessage());
            return null;
        }
    }

    /**
     * 今日任务进度。
     */
    private String buildTodayProgress(Long userId) {
        try {
            LocalDate today = LocalDate.now();
            List<AiDailyTask> tasks = taskMapper.selectList(
                    new LambdaQueryWrapper<AiDailyTask>()
                            .eq(AiDailyTask::getUserId, userId)
                            .eq(AiDailyTask::getTaskDate, today));

            if (tasks.isEmpty()) return null;

            long completed = tasks.stream().filter(t -> t.getStatus() != null && t.getStatus() == 1).count();
            int total = tasks.size();

            StringBuilder sb = new StringBuilder();
            sb.append("- 今日任务：已完成 ").append(completed).append("/").append(total).append(" 项\n");

            // 列出前 3 个未完成任务
            tasks.stream()
                    .filter(t -> t.getStatus() == null || t.getStatus() == 0)
                    .limit(3)
                    .forEach(t -> sb.append("  - ⏳ ").append(t.getTaskContent()).append("\n"));

            return sb.toString();
        } catch (Exception e) {
            log.warn("构建今日进度失败 — {}", e.getMessage());
            return null;
        }
    }

    /**
     * 学习统计（连续打卡 + 累计天数）。
     */
    private String buildStudyStats(Long userId) {
        try {
            UserStudyPO study = userStudyMapper.selectOne(
                    new LambdaQueryWrapper<UserStudyPO>().eq(UserStudyPO::getUserId, userId));

            if (study == null) return null;

            StringBuilder sb = new StringBuilder();
            if (study.getContinuousDays() != null && study.getContinuousDays() > 0) {
                sb.append("- 连续打卡：").append(study.getContinuousDays()).append(" 天\n");
            }
            if (study.getTotalCheckDays() != null && study.getTotalCheckDays() > 0) {
                sb.append("- 累计打卡：").append(study.getTotalCheckDays()).append(" 天\n");
            }

            return sb.length() > 0 ? sb.toString() : null;
        } catch (Exception e) {
            log.warn("构建学习统计失败 — {}", e.getMessage());
            return null;
        }
    }

    /**
     * 最近 3 个对话会话标题。
     */
    private String buildRecentChats(Long userId) {
        try {
            List<AiChatSession> sessions = chatSessionMapper.selectList(
                    new LambdaQueryWrapper<AiChatSession>()
                            .eq(AiChatSession::getUserId, userId)
                            .eq(AiChatSession::getIsDeleted, 0)
                            .orderByDesc(AiChatSession::getUpdatedAt)
                            .last("LIMIT 3"));

            if (sessions.isEmpty()) return null;

            StringBuilder sb = new StringBuilder();
            for (AiChatSession s : sessions) {
                String title = s.getTitle() != null ? s.getTitle() : "新对话";
                sb.append("- ").append(title).append("\n");
            }

            return sb.toString();
        } catch (Exception e) {
            log.warn("构建最近对话失败 — {}", e.getMessage());
            return null;
        }
    }

    // ────────────────── 简易 JSON 解析工具（不依赖 Jackson，容错性好） ──────────────────

    private static String extractString(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static Integer extractInt(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*(\\d+)";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private static String extractArray(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\\[([^\\]]*)\\]";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (!m.find()) return null;
        String inner = m.group(1);
        // 去掉引号和多余空白
        return inner.replaceAll("\"", "").replaceAll("\\s+", " ").trim();
    }
}
