package com.zzu.kaoyan.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.activity.entity.po.CheckInPO;
import com.zzu.kaoyan.module.activity.entity.po.UserStudyPO;
import com.zzu.kaoyan.module.activity.mapper.CheckInMapper;
import com.zzu.kaoyan.module.activity.mapper.UserStudyMapper;
import com.zzu.kaoyan.module.ai.entity.*;
import com.zzu.kaoyan.module.ai.mapper.*;
import com.zzu.kaoyan.module.membership.entity.UserMembership;
import com.zzu.kaoyan.module.mistake.entity.po.*;
import com.zzu.kaoyan.module.mistake.mapper.*;
import com.zzu.kaoyan.module.membership.mapper.MembershipPlanMapper;
import com.zzu.kaoyan.module.membership.mapper.UserMembershipMapper;
import com.zzu.kaoyan.module.membership.service.MembershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 测试账号初始化器 — 每次启动自动重置测试账号与种子数据
 *
 * 仅在 dev profile 激活时运行，生产环境不受影响。
 * 启动参数：-Dspring.profiles.active=dev
 */
@Component
@Profile("dev")
public class TestDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TestDataInitializer.class);

    // ────────────────── 测试账号常量 ──────────────────
    private static final String TEST_EMAIL = "test@kaoyan.com";
    private static final String TEST_PASSWORD = "test123456";
    private static final String TEST_USERNAME = "Agent测试员";

    // ────────────────── Mapper 注入 ──────────────────
    private final UserMapper userMapper;
    private final UserMembershipMapper userMembershipMapper;
    private final MembershipPlanMapper membershipPlanMapper;
    private final AiDailyTaskMapper aiDailyTaskMapper;
    private final AiChatSessionMapper aiChatSessionMapper;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final AiInterventionLogMapper aiInterventionLogMapper;
    private final AiReportMapper aiReportMapper;
    private final AiUserEventMapper aiUserEventMapper;
    private final CheckInMapper checkInMapper;
    private final UserStudyMapper userStudyMapper;
    private final UserAiProfileMapper userAiProfileMapper;
    private final MistakeNoteMapper mistakeNoteMapper;
    private final ReviewLogMapper reviewLogMapper;
    private final DailyPlanMapper dailyPlanMapper;
    private final MistakeNotificationMapper mistakeNotificationMapper;

    // ────────────────── Service 注入 ──────────────────
    private final MembershipService membershipService;
    private final StringRedisTemplate stringRedisTemplate;

    public TestDataInitializer(UserMapper userMapper,
                               UserMembershipMapper userMembershipMapper,
                               MembershipPlanMapper membershipPlanMapper,
                               AiDailyTaskMapper aiDailyTaskMapper,
                               AiChatSessionMapper aiChatSessionMapper,
                               AiChatMessageMapper aiChatMessageMapper,
                               AiInterventionLogMapper aiInterventionLogMapper,
                               AiReportMapper aiReportMapper,
                               AiUserEventMapper aiUserEventMapper,
                               CheckInMapper checkInMapper,
                               UserStudyMapper userStudyMapper,
                               UserAiProfileMapper userAiProfileMapper,
                               MistakeNoteMapper mistakeNoteMapper,
                               ReviewLogMapper reviewLogMapper,
                               DailyPlanMapper dailyPlanMapper,
                               MistakeNotificationMapper mistakeNotificationMapper,
                               MembershipService membershipService,
                               StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.userMembershipMapper = userMembershipMapper;
        this.membershipPlanMapper = membershipPlanMapper;
        this.aiDailyTaskMapper = aiDailyTaskMapper;
        this.aiChatSessionMapper = aiChatSessionMapper;
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.aiInterventionLogMapper = aiInterventionLogMapper;
        this.aiReportMapper = aiReportMapper;
        this.aiUserEventMapper = aiUserEventMapper;
        this.checkInMapper = checkInMapper;
        this.userStudyMapper = userStudyMapper;
        this.userAiProfileMapper = userAiProfileMapper;
        this.mistakeNoteMapper = mistakeNoteMapper;
        this.reviewLogMapper = reviewLogMapper;
        this.dailyPlanMapper = dailyPlanMapper;
        this.mistakeNotificationMapper = mistakeNotificationMapper;
        this.membershipService = membershipService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("🧪 [TestData] 开发环境 — 开始重置测试账号...");

        try {
            // Phase 1: 创建/重置测试用户
            Long userId = createOrResetUser();
            log.info("  ✅ Phase 1 — 测试用户就绪 (userId={})", userId);

            // Phase 0: 清除旧数据
            clearOldData(userId);
            log.info("  ✅ Phase 0 — 旧数据已清除");

            // 清除 Redis 配额 key
            clearRedisKeys(userId);
            log.info("  ✅ Redis — 配额 key 已清除");

            // Phase 2: 授予 VIP 会员
            grantVip(userId);
            log.info("  ✅ Phase 2 — VIP 会员已授予");

            // Phase 3: 种子测试数据
            seedP0Data(userId);
            log.info("  ✅ Phase 3-P0 — 种子数据已写入 (tasks + chat sessions/messages)");

            seedP1Data(userId);
            log.info("  ✅ Phase 3-P1 — 种子数据已写入 (interventions + reports)");

            seedP2Data(userId);
            log.info("  ✅ Phase 3-P2 — 种子数据已写入 (check-ins + study stats + profile + events)");

            log.info("🎉 [TestData] 测试账号完全就绪: {} (userId={}, VIP)", TEST_EMAIL, userId);

        } catch (Exception e) {
            log.error("❌ [TestData] 测试账号初始化失败", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 1: 创建/重置用户
    // ═══════════════════════════════════════════════════════════════

    private Long createOrResetUser() {
        // 查询是否已存在
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, TEST_EMAIL)
        );

        if (existing != null) {
            // 已存在 → 重置（包括可能被软删除的情况）
            existing.setUsername(TEST_USERNAME);
            existing.setPassword(BCrypt.hashpw(TEST_PASSWORD, BCrypt.gensalt()));
            existing.setRole("USER");
            existing.setDeleted(false);  // 恢复软删除
            userMapper.updateById(existing);
            log.info("  🔄 已重置现有测试用户 id={}", existing.getId());
            return existing.getId();
        } else {
            // 不存在 → 新建
            User newUser = new User();
            newUser.setEmail(TEST_EMAIL);
            newUser.setPassword(BCrypt.hashpw(TEST_PASSWORD, BCrypt.gensalt()));
            newUser.setUsername(TEST_USERNAME);
            newUser.setRole("USER");
            newUser.setPoints(0);
            newUser.setDeleted(false);
            userMapper.insert(newUser);
            log.info("  ✨ 已创建测试用户 id={}", newUser.getId());
            return newUser.getId();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 0: 清除旧测试数据
    // ═══════════════════════════════════════════════════════════════

    private void clearOldData(Long userId) {
        // 先删子表（外键依赖）
        aiChatMessageMapper.delete(
                new LambdaQueryWrapper<AiChatMessage>()
                        .inSql(AiChatMessage::getSessionId,
                                "SELECT id FROM ai_chat_session WHERE user_id = " + userId)
        );
        aiChatSessionMapper.delete(
                new LambdaQueryWrapper<AiChatSession>().eq(AiChatSession::getUserId, userId)
        );
        aiDailyTaskMapper.delete(
                new LambdaQueryWrapper<AiDailyTask>().eq(AiDailyTask::getUserId, userId)
        );
        aiInterventionLogMapper.delete(
                new LambdaQueryWrapper<AiInterventionLog>().eq(AiInterventionLog::getUserId, userId)
        );
        aiReportMapper.delete(
                new LambdaQueryWrapper<AiReport>().eq(AiReport::getUserId, userId)
        );
        aiUserEventMapper.delete(
                new LambdaQueryWrapper<AiUserEvent>().eq(AiUserEvent::getUserId, userId)
        );
        checkInMapper.delete(
                new LambdaQueryWrapper<CheckInPO>().eq(CheckInPO::getUserId, userId)
        );
        userStudyMapper.delete(
                new LambdaQueryWrapper<UserStudyPO>().eq(UserStudyPO::getUserId, userId)
        );
        userAiProfileMapper.delete(
                new LambdaQueryWrapper<UserAiProfile>().eq(UserAiProfile::getUserId, userId)
        );
        userMembershipMapper.delete(
                new LambdaQueryWrapper<UserMembership>().eq(UserMembership::getUserId, userId)
        );
        // 错题本相关
        reviewLogMapper.delete(
                new LambdaQueryWrapper<ReviewLogPO>().eq(ReviewLogPO::getUserId, userId)
        );
        dailyPlanMapper.delete(
                new LambdaQueryWrapper<DailyPlanPO>().eq(DailyPlanPO::getUserId, userId)
        );
        mistakeNotificationMapper.delete(
                new LambdaQueryWrapper<MistakeNotificationPO>().eq(MistakeNotificationPO::getUserId, userId)
        );
        mistakeNoteMapper.delete(
                new LambdaQueryWrapper<MistakeNotePO>().eq(MistakeNotePO::getUserId, userId)
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // Redis 清理
    // ═══════════════════════════════════════════════════════════════

    private void clearRedisKeys(Long userId) {
        // 删除会员缓存 key
        stringRedisTemplate.delete("membership:plan:" + userId);

        // 删除所有配额 key
        Set<String> quotaKeys = stringRedisTemplate.keys("membership:quota:" + userId + ":*");
        if (quotaKeys != null && !quotaKeys.isEmpty()) {
            stringRedisTemplate.delete(quotaKeys);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 2: 授予 VIP 会员
    // ═══════════════════════════════════════════════════════════════

    private void grantVip(Long userId) {
        // 查询 VIP 月卡套餐
        Long vipPlanId = membershipPlanMapper.selectList(null).stream()
                .filter(p -> "vip_monthly".equals(p.getPlanCode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("VIP 套餐不存在，请先执行 V1__membership 迁移"))
                .getId();

        // 插入会员记录（永久有效）
        UserMembership membership = new UserMembership();
        membership.setUserId(userId);
        membership.setPlanId(vipPlanId);
        membership.setStatus("ACTIVE");
        membership.setStartedAt(LocalDateTime.now());
        membership.setExpiresAt(null);    // 永不过期
        membership.setAutoRenew(false);
        userMembershipMapper.insert(membership);
        log.info("  💎 VIP 会员已激活 (planId={}, 永久有效)", vipPlanId);

        // 预热 Redis 缓存
        membershipService.warmCache(userId);
        log.info("  🔥 Redis 会员缓存已预热");
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 3-P0: 核心种子数据
    // ═══════════════════════════════════════════════════════════════

    private void seedP0Data(Long userId) {
        seedDailyTasks(userId);
        seedChatSessions(userId);
    }

    private void seedDailyTasks(Long userId) {
        LocalDate today = LocalDate.now();

        // Task 1 — HIGH, 待办
        AiDailyTask t1 = new AiDailyTask();
        t1.setUserId(userId);
        t1.setTaskDate(today);
        t1.setTaskContent("今天复习完高数第三章：微分中值定理");
        t1.setImportance("HIGH");
        t1.setStatus(0);
        t1.setAgentTips("规划伴侣建议：本章是高数核心考点，占分比约15%");
        aiDailyTaskMapper.insert(t1);

        // Task 2 — HIGH, 待办
        AiDailyTask t2 = new AiDailyTask();
        t2.setUserId(userId);
        t2.setTaskDate(today);
        t2.setTaskContent("完成408数据结构「二叉树遍历」相关习题10道");
        t2.setImportance("HIGH");
        t2.setStatus(0);
        t2.setAgentTips("规划伴侣建议：二叉树是408必考题，建议配合真题练习");
        aiDailyTaskMapper.insert(t2);

        // Task 3 — MEDIUM, 已完成
        AiDailyTask t3 = new AiDailyTask();
        t3.setUserId(userId);
        t3.setTaskDate(today);
        t3.setTaskContent("背诵英语考研核心词汇List 5（abandon ~ ambition）");
        t3.setImportance("MEDIUM");
        t3.setStatus(1);  // 已完成
        t3.setAgentTips("规划伴侣建议：利用艾宾浩斯记忆法，及时回顾前4个List");
        aiDailyTaskMapper.insert(t3);

        // Task 4 — LOW, 待办
        AiDailyTask t4 = new AiDailyTask();
        t4.setUserId(userId);
        t4.setTaskDate(today);
        t4.setTaskContent("浏览目标院校最新招生简章，关注专业课变动");
        t4.setImportance("LOW");
        t4.setStatus(0);
        t4.setAgentTips("规划伴侣建议：每周关注一次即可，避免信息焦虑");
        aiDailyTaskMapper.insert(t4);

        log.info("  📋 已种子 4 条每日任务 (2 HIGH + 1 MEDIUM[已完成] + 1 LOW)");
    }

    private void seedChatSessions(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        // ──── Session 1: 高数极限问题 ────
        AiChatSession s1 = new AiChatSession();
        s1.setUserId(userId);
        s1.setTitle("高数极限问题");
        s1.setIsDeleted(0);
        s1.setCreatedAt(now.minusHours(2));
        s1.setUpdatedAt(now.minusHours(1));
        aiChatSessionMapper.insert(s1);

        // 消息 1-1: user 提问
        AiChatMessage m1a = new AiChatMessage();
        m1a.setSessionId(s1.getId());
        m1a.setRole("user");
        m1a.setContent("什么是拉格朗日中值定理？");
        m1a.setImageUrl(null);
        m1a.setCreatedAt(now.minusHours(2));
        aiChatMessageMapper.insert(m1a);

        // 消息 1-2: assistant 回答
        AiChatMessage m1b = new AiChatMessage();
        m1b.setSessionId(s1.getId());
        m1b.setRole("assistant");
        m1b.setContent("拉格朗日中值定理（Lagrange Mean Value Theorem）是微积分中的重要定理：\n\n**定理内容：** 如果函数 $f(x)$ 满足：\n1. 在闭区间 $[a, b]$ 上连续\n2. 在开区间 $(a, b)$ 内可导\n\n则至少存在一点 $\\xi \\in (a, b)$，使得：\n\n$$f'(\\xi) = \\frac{f(b) - f(a)}{b - a}$$\n\n**几何意义：** 曲线上至少存在一点，该点处的切线斜率等于割线斜率。\n\n**考研常考题型：**\n- 证明中值等式\n- 证明不等式\n- 求极限中的参数");
        m1b.setImageUrl(null);
        m1b.setCreatedAt(now.minusHours(2).plusSeconds(3));
        aiChatMessageMapper.insert(m1b);

        // 消息 1-3: user 追问
        AiChatMessage m1c = new AiChatMessage();
        m1c.setSessionId(s1.getId());
        m1c.setRole("user");
        m1c.setContent("能举个具体的例题吗？");
        m1c.setImageUrl(null);
        m1c.setCreatedAt(now.minusHours(1));
        aiChatMessageMapper.insert(m1c);

        // ──── Session 2: 数据结构答疑 ────
        AiChatSession s2 = new AiChatSession();
        s2.setUserId(userId);
        s2.setTitle("数据结构答疑");
        s2.setIsDeleted(0);
        s2.setCreatedAt(now.minusDays(1));
        s2.setUpdatedAt(now.minusDays(1).plusMinutes(10));
        aiChatSessionMapper.insert(s2);

        // 消息 2-1: user 提问
        AiChatMessage m2a = new AiChatMessage();
        m2a.setSessionId(s2.getId());
        m2a.setRole("user");
        m2a.setContent("B树和B+树有什么区别？408考试重点是什么？");
        m2a.setImageUrl(null);
        m2a.setCreatedAt(now.minusDays(1));
        aiChatMessageMapper.insert(m2a);

        // 消息 2-2: assistant 回答
        AiChatMessage m2b = new AiChatMessage();
        m2b.setSessionId(s2.getId());
        m2b.setRole("assistant");
        m2b.setContent("B树和B+树的核心区别：\n\n| 特性 | B树 | B+树 |\n|------|-----|------|\n| 数据存储 | 所有节点都存数据 | 仅叶子节点存数据 |\n| 叶子指针 | 无 | 叶子节点形成有序链表 |\n| 查找效率 | 不稳定 | 稳定（都到叶子） |\n| 范围查询 | 需中序遍历 | 直接遍历叶子链表 |\n\n**408 考试重点：**\n1. B+树是数据库索引的底层结构，理解为什么\n2. B树的插入/删除过程（分裂/合并）\n3. 阶数与子树数量的关系（$\\lceil m/2 \\rceil \\le 子树数 \\le m$）");
        m2b.setImageUrl(null);
        m2b.setCreatedAt(now.minusDays(1).plusSeconds(3));
        aiChatMessageMapper.insert(m2b);

        // 消息 2-3: user 追问
        AiChatMessage m2c = new AiChatMessage();
        m2c.setSessionId(s2.getId());
        m2c.setRole("user");
        m2c.setContent("那3阶B树最多有多少个关键字？");
        m2c.setImageUrl(null);
        m2c.setCreatedAt(now.minusDays(1).plusMinutes(10));
        aiChatMessageMapper.insert(m2c);

        log.info("  💬 已种子 2 个聊天会话 + 6 条消息 (2 问答 + 2 回答 + 2 追问)");
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 3-P1: 次优先种子数据
    // ═══════════════════════════════════════════════════════════════

    private void seedP1Data(Long userId) {
        seedInterventions(userId);
        seedReports(userId);
    }

    private void seedInterventions(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        // Intervention 1 — Psychology (粉色卡片)
        AiInterventionLog i1 = new AiInterventionLog();
        i1.setUserId(userId);
        i1.setAgentName("Psychology");  // 心理树洞 → 前端显示粉色
        i1.setInterventionContent("检测到你连续3天学习时长下降，今天是不是有点累了？适当休息也是备考的一部分，调整好状态再出发！");
        i1.setTriggerReason("学习时长连续3天下降超过30%");
        i1.setUserReaction("UNREAD");
        i1.setCreatedAt(now.minusHours(5));
        aiInterventionLogMapper.insert(i1);

        // Intervention 2 — Behavior (蓝色卡片)
        AiInterventionLog i2 = new AiInterventionLog();
        i2.setUserId(userId);
        i2.setAgentName("Behavior");  // 行为分析师 → 前端显示蓝色
        i2.setInterventionContent("你今天浏览了大量「高等数学」相关帖子，建议将这些知识点加入错题复习计划，系统已为你生成3道相关习题。");
        i2.setTriggerReason("浏览模式分析：高数相关帖子占比超过80%");
        i2.setUserReaction("UNREAD");
        i2.setCreatedAt(now.minusMinutes(30));
        aiInterventionLogMapper.insert(i2);

        log.info("  🔔 已种子 2 条干预消息 (Psychology粉色 + Behavior蓝色)");
    }

    private void seedReports(Long userId) {
        LocalDate today = LocalDate.now();

        // 找到本周一
        LocalDate thisMonday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        // 上周: 本周一 - 7 天
        LocalDate lastMonday = thisMonday.minusDays(7);
        LocalDate lastSunday = thisMonday.minusDays(1);
        // 上上周
        LocalDate twoWeeksAgoMonday = thisMonday.minusDays(14);
        LocalDate twoWeeksAgoSunday = thisMonday.minusDays(8);

        // Report 1 — 上周
        AiReport r1 = new AiReport();
        r1.setUserId(userId);
        r1.setWeekStart(lastMonday);
        r1.setWeekEnd(lastSunday);
        r1.setMarkdown("""
                # 本周学情报告

                ## 📊 学习总览
                - ✅ 完成 AI 任务：**12 个**（完成率 85.7%）
                - 📅 打卡天数：**6 天**
                - ⏱️ 总学习时长：**32.5 小时**
                - 🔥 连续打卡：**7 天**

                ## 🎯 各科分析
                | 学科 | 任务完成 | 掌握度变化 |
                |------|---------|-----------|
                | 高等数学 | 5/6 | ↑ 12% |
                | 408 计算机 | 4/5 | ↑ 8% |
                | 英语 | 2/2 | → 持平 |
                | 政治 | 1/1 | → 持平 |

                ## 💪 亮点
                1. 高数中值定理掌握度提升显著，连续3天做题正确率 > 80%
                2. 数据结构二叉树遍历全部按时完成

                ## ⚠️ 薄弱点
                1. 英语阅读理解耗时偏长，建议限时训练
                2. 408 计组部分题目未按计划完成

                ## 📝 下周建议
                - 重点攻克英语阅读速度问题
                - 保持高数当前节奏
                - 开始政治思修部分的学习
                """);
        aiReportMapper.insert(r1);

        // Report 2 — 上上周
        AiReport r2 = new AiReport();
        r2.setUserId(userId);
        r2.setWeekStart(twoWeeksAgoMonday);
        r2.setWeekEnd(twoWeeksAgoSunday);
        r2.setMarkdown("""
                # 本周学情报告

                ## 📊 学习总览
                - ✅ 完成 AI 任务：**9 个**（完成率 75.0%）
                - 📅 打卡天数：**5 天**
                - ⏱️ 总学习时长：**28 小时**
                - 🔥 连续打卡：**3 天**

                ## 🎯 各科分析
                | 学科 | 任务完成 | 掌握度变化 |
                |------|---------|-----------|
                | 高等数学 | 4/5 | ↑ 5% |
                | 408 计算机 | 3/5 | ↑ 3% |
                | 英语 | 1/1 | → 持平 |
                | 政治 | 1/1 | → 持平 |

                ## 💪 亮点
                1. 开始使用艾宾浩斯记忆法背单词，效果初显
                2. 错题本使用频率提升

                ## ⚠️ 薄弱点
                1. 学习连续打卡天数不稳定，建议设定每日最低学习时长
                2. 概率统计章节练习题偏少

                ## 📝 下周建议
                - 坚持每日打卡，目标连续7天
                - 增加概率统计的练习量
                """);
        aiReportMapper.insert(r2);

        log.info("  📄 已种子 2 份周报 (上周 + 上上周)");
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 3-P2: 锦上添花种子数据
    // ═══════════════════════════════════════════════════════════════

    private void seedP2Data(Long userId) {
        seedCheckIns(userId);
        seedUserStudy(userId);
        seedUserAiProfile(userId);
        seedUserEvents(userId);
        seedMistakeNotes(userId);
    }

    private void seedCheckIns(Long userId) {
        LocalDate today = LocalDate.now();

        // 过去 7 天内 3 天打卡
        int[][] checkInDays = {
                {1, 3},   // 1 天前, 学习 3 小时
                {3, 5},   // 3 天前, 学习 5 小时
                {6, 2},   // 6 天前, 学习 2 小时
        };

        for (int[] entry : checkInDays) {
            LocalDate date = today.minusDays(entry[0]);
            CheckInPO checkIn = new CheckInPO();
            checkIn.setUserId(userId);
            checkIn.setStudyHours(entry[1]);
            checkIn.setNotes("打卡学习，状态不错");
            checkIn.setCreatedDate(date);
            checkInMapper.insert(checkIn);
        }

        log.info("  📅 已种子 3 条打卡记录 (过去7天内)");
    }

    private void seedUserStudy(Long userId) {
        UserStudyPO study = new UserStudyPO();
        study.setUserId(userId);
        study.setContinuousDays(7);
        study.setTotalCheckDays(30);
        study.setLastCheckDate(LocalDate.now().minusDays(1));
        userStudyMapper.insert(study);

        log.info("  📊 已种子学习统计 (连续7天 + 累计30天)");
    }

    private void seedUserAiProfile(Long userId) {
        UserAiProfile profile = new UserAiProfile();
        profile.setUserId(userId);
        // cognitive_profile JSON: PlannerAgent 读取，BehaviorAnalysisAgent 更新
        profile.setCognitiveProfile("""
                {
                  "totalCheckDays": 30,
                  "continuousDays": 7,
                  "totalStudyHours": 145,
                  "lastActive": "2026-06-15",
                  "interestKeywords": ["高等数学", "数据结构", "C语言", "计算机组成原理"],
                  "browsePattern": {
                    "todayViews": 12,
                    "todayCollects": 2,
                    "todaySearches": 5,
                    "todayLikes": 3
                  }
                }""");
        // psychological_profile JSON: PsychologyAgent 更新
        profile.setPsychologicalProfile("""
                {
                  "recentEmotion": "积极",
                  "lastAnalysis": "该用户整体学习状态良好，连续打卡稳定，建议保持当前节奏。",
                  "updatedAt": "2026-06-15T10:00:00"
                }""");
        userAiProfileMapper.insert(profile);

        log.info("  🧠 已种子 AI 用户画像 (cognitive + psychological profile)");
    }

    private void seedUserEvents(Long userId) {
        // Event 1: VIEW_POST
        AiUserEvent e1 = new AiUserEvent();
        e1.setUserId(userId);
        e1.setEventType("VIEW_POST");
        e1.setEventData("{\"postId\": 42, \"duration\": 120}");
        aiUserEventMapper.insert(e1);

        // Event 2: SEARCH
        AiUserEvent e2 = new AiUserEvent();
        e2.setUserId(userId);
        e2.setEventType("SEARCH");
        e2.setEventData("{\"keyword\": \"极限\", \"resultCount\": 15}");
        aiUserEventMapper.insert(e2);

        // Event 3: COLLECT_POST
        AiUserEvent e3 = new AiUserEvent();
        e3.setUserId(userId);
        e3.setEventType("COLLECT_POST");
        e3.setEventData("{\"postId\": 42}");
        aiUserEventMapper.insert(e3);

        log.info("  📡 已种子 3 条行为事件 (VIEW_POST + SEARCH + COLLECT_POST)");
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 3-P2: 错题本种子数据
    // ═══════════════════════════════════════════════════════════════

    private void seedMistakeNotes(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // ──── 错题 1: 高数 — 艾宾浩斯阶段 3，掌握度 65 ────
        MistakeNotePO n1 = new MistakeNotePO();
        n1.setUserId(userId);
        n1.setSubject("高等数学");
        n1.setQuestionContent("求极限 $\\lim_{x \\to 0} \\frac{\\sin x - x}{x^3}$");
        n1.setAnswer("使用泰勒展开：$\\sin x = x - \\frac{x^3}{6} + o(x^3)$\n\n代入得：\n$$\\lim_{x \\to 0} \\frac{(x - \\frac{x^3}{6} + o(x^3)) - x}{x^3} = \\lim_{x \\to 0} \\frac{-\\frac{x^3}{6} + o(x^3)}{x^3} = -\\frac{1}{6}$$\n\n**考点：** 泰勒公式、等价无穷小替换");
        n1.setImageUrl(null);
        n1.setKnowledgePoints("泰勒公式,等价无穷小");
        n1.setSource("2023 数学一真题 T1");
        n1.setDifficulty(4);
        n1.setMasteryLevel(65);
        n1.setReviewStage(3);
        n1.setReviewCount(3);
        n1.setNextReviewDate(today.plusDays(3));
        n1.setLastReviewDate(today.minusDays(2));
        n1.setSourceType("MANUAL");
        n1.setIsDeleted(0);
        mistakeNoteMapper.insert(n1);

        // ──── 错题 2: 408 — 艾宾浩斯阶段 1，掌握度 40 ────
        MistakeNotePO n2 = new MistakeNotePO();
        n2.setUserId(userId);
        n2.setSubject("408计算机");
        n2.setQuestionContent("已知一棵 3 阶 B 树如下图所示，插入关键字 25 后，根节点中的关键字是？\n\n```\n     [20]\n    /    \\\n[10,15]  [30,40]\n```");
        n2.setAnswer("插入过程：\n1. 25 应插入右子树 [30,40]\n2. 节点变为 [25,30,40]，阶数 m=3，最多 2 个关键字 → **需要分裂**\n3. 中间关键字 30 上升至父节点，[25] 为左子，[40] 为右子\n4. 根节点变为 **[20,30]**\n\n**答案：根节点关键字为 20 和 30**");
        n2.setImageUrl(null);
        n2.setKnowledgePoints("B树,插入,分裂");
        n2.setSource("408 统考真题 2022-T42");
        n2.setDifficulty(5);
        n2.setMasteryLevel(40);
        n2.setReviewStage(1);
        n2.setReviewCount(1);
        n2.setNextReviewDate(today.plusDays(1));
        n2.setLastReviewDate(today.minusDays(1));
        n2.setSourceType("MANUAL");
        n2.setIsDeleted(0);
        mistakeNoteMapper.insert(n2);

        // ──── 错题 3: 英语 — 艾宾浩斯阶段 5，掌握度 85 ────
        MistakeNotePO n3 = new MistakeNotePO();
        n3.setUserId(userId);
        n3.setSubject("英语");
        n3.setQuestionContent("翻译：The fact that the apple fell down toward the earth and not up into the tree answered the question he had been asking himself about those larger fruits of the heavens, the moon and the planets.");
        n3.setAnswer("**参考译文：** 苹果向下落到地面而非向上飞入树中，这一事实回答了他一直在问自己的、关于那些天空中更大的果实——月球和行星——的问题。\n\n**考点分析：**\n- 同位语从句：`the fact that...`\n- 现在完成进行时：`had been asking`\n- 暗喻修辞：`larger fruits of the heavens` 指代月球和行星");
        n3.setImageUrl(null);
        n3.setKnowledgePoints("翻译,同位语从句,完成进行时");
        n3.setSource("英语一 2021 翻译真题");
        n3.setDifficulty(3);
        n3.setMasteryLevel(85);
        n3.setReviewStage(5);
        n3.setReviewCount(5);
        n3.setNextReviewDate(today.plusDays(15));
        n3.setLastReviewDate(today.minusDays(8));
        n3.setSourceType("AI_CHAT");
        n3.setChatMessageId(null);  // 不关联具体消息
        n3.setIsDeleted(0);
        mistakeNoteMapper.insert(n3);

        // ──── 错题 4: 政治 — 艾宾浩斯阶段 0（新题），掌握度 30 ────
        MistakeNotePO n4 = new MistakeNotePO();
        n4.setUserId(userId);
        n4.setSubject("政治");
        n4.setQuestionContent("【单选】马克思主义中国化的第一次历史性飞跃发生在（ ）\n\nA. 新民主主义革命时期\nB. 社会主义革命和建设时期\nC. 改革开放新时期\nD. 中国特色社会主义新时代");
        n4.setAnswer("**答案：A**\n\n**解析：** 毛泽东思想是马克思主义中国化的第一次历史性飞跃，形成于新民主主义革命时期。第二次飞跃是中国特色社会主义理论体系（改革开放后）。\n\n**易错点：** B 选项为干扰项，社会主义革命和建设时期是毛泽东思想进一步发展的阶段，但第一次飞跃发生在新民主主义革命时期。");
        n4.setImageUrl(null);
        n4.setKnowledgePoints("马克思主义中国化,毛泽东思想,史纲");
        n4.setSource("2024 政治冲刺卷");
        n4.setDifficulty(2);
        n4.setMasteryLevel(30);
        n4.setReviewStage(0);
        n4.setReviewCount(0);
        n4.setNextReviewDate(today);  // 今日待复习
        n4.setLastReviewDate(null);
        n4.setSourceType("OCR");
        n4.setIsDeleted(0);
        mistakeNoteMapper.insert(n4);

        // ──── 错题 5: 高数 — 已掌握（阶段 7），掌握度 95 ────
        MistakeNotePO n5 = new MistakeNotePO();
        n5.setUserId(userId);
        n5.setSubject("高等数学");
        n5.setQuestionContent("判断级数 $\\sum_{n=1}^{\\infty} \\frac{n}{3^n}$ 的敛散性。");
        n5.setAnswer("**比值判别法：**\n\n$$\\lim_{n \\to \\infty} \\left|\\frac{a_{n+1}}{a_n}\\right| = \\lim_{n \\to \\infty} \\frac{n+1}{3^{n+1}} \\cdot \\frac{3^n}{n} = \\lim_{n \\to \\infty} \\frac{n+1}{3n} = \\frac{1}{3} < 1$$\n\n故级数**收敛**。\n\n**补充：** 也可用根值判别法，$\\lim_{n \\to \\infty} \\sqrt[n]{\\frac{n}{3^n}} = \\frac{1}{3} < 1$。");
        n5.setImageUrl(null);
        n5.setKnowledgePoints("级数,比值判别法,根值判别法");
        n5.setSource("数学一 2020 真题 T10");
        n5.setDifficulty(3);
        n5.setMasteryLevel(95);
        n5.setReviewStage(7);
        n5.setReviewCount(7);
        n5.setNextReviewDate(null);  // 已掌握，无需复习
        n5.setLastReviewDate(today.minusDays(30));
        n5.setSourceType("MANUAL");
        n5.setIsDeleted(0);
        mistakeNoteMapper.insert(n5);

        log.info("  📝 已种子 5 条错题 (高数×2 + 408×1 + 英语×1 + 政治×1)");

        // ──── 复习日志（3 条） ────
        ReviewLogPO rl1 = new ReviewLogPO();
        rl1.setNoteId(n1.getId());
        rl1.setUserId(userId);
        rl1.setReviewStage(2);
        rl1.setMasteryBefore(50);
        rl1.setMasteryAfter(65);
        rl1.setIsCorrect(1);
        rl1.setReviewedAt(now.minusDays(2));
        reviewLogMapper.insert(rl1);

        ReviewLogPO rl2 = new ReviewLogPO();
        rl2.setNoteId(n1.getId());
        rl2.setUserId(userId);
        rl2.setReviewStage(1);
        rl2.setMasteryBefore(35);
        rl2.setMasteryAfter(50);
        rl2.setIsCorrect(0);
        rl2.setReviewedAt(now.minusDays(4));
        reviewLogMapper.insert(rl2);

        ReviewLogPO rl3 = new ReviewLogPO();
        rl3.setNoteId(n2.getId());
        rl3.setUserId(userId);
        rl3.setReviewStage(0);
        rl3.setMasteryBefore(20);
        rl3.setMasteryAfter(40);
        rl3.setIsCorrect(1);
        rl3.setReviewedAt(now.minusDays(1));
        reviewLogMapper.insert(rl3);

        log.info("  📊 已种子 3 条复习日志");

        // ──── 今日复习计划 ────
        DailyPlanPO plan = new DailyPlanPO();
        plan.setUserId(userId);
        plan.setPlanDate(today);
        plan.setNoteIds(List.of(n4.getId(), n1.getId(), n2.getId()));
        plan.setCompletedIds(List.of());
        plan.setTotalCount(3);
        plan.setCompletedCount(0);
        plan.setIsCompleted(0);
        dailyPlanMapper.insert(plan);

        log.info("  📅 已种子今日复习计划 (3 题待复习)");

        // ──── 通知（3 条） ────
        MistakeNotificationPO notif1 = new MistakeNotificationPO();
        notif1.setUserId(userId);
        notif1.setType("REVIEW_REMINDER");
        notif1.setTitle("今日有 3 道错题待复习");
        notif1.setContent("高数「泰勒展开求极限」已到第 3 次复习节点，408「B树插入」需要巩固，政治「马克思主义中国化」为新题首次复习。建议上午完成错题复习。");
        notif1.setIsRead(0);
        notif1.setCreatedAt(now.minusMinutes(10));
        mistakeNotificationMapper.insert(notif1);

        MistakeNotificationPO notif2 = new MistakeNotificationPO();
        notif2.setUserId(userId);
        notif2.setType("MASTERY_MILESTONE");
        notif2.setTitle("🎉 恭喜！「级数敛散性判别」已完全掌握");
        notif2.setContent("高数「判断级数 ∑n/3^n 的敛散性」经过 7 次艾宾浩斯复习，掌握度达到 95%，已标记为已掌握。继续保持！");
        notif2.setIsRead(0);
        notif2.setCreatedAt(now.minusHours(3));
        mistakeNotificationMapper.insert(notif2);

        MistakeNotificationPO notif3 = new MistakeNotificationPO();
        notif3.setUserId(userId);
        notif3.setType("STAGE_MASTERED");
        notif3.setTitle("📈 错题本掌握度周报已生成");
        notif3.setContent("本周复习 12 题，正确率 75%，平均掌握度从 48% 提升至 62%。英语翻译题进步最大（+15%），408 B树类题型仍需加强。");
        notif3.setIsRead(1);  // 已读
        notif3.setCreatedAt(now.minusDays(1));
        mistakeNotificationMapper.insert(notif3);

        log.info("  🔔 已种子 3 条通知 (2 未读 + 1 已读)");
    }
}
