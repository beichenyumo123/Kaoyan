# AI 多智能体学习伴侣（Multi-Agent Study Companion）

## 目录结构

所有路径基于 `/home/db/Documents/Project/Kaoyan/`。

```
src/main/java/com/zzu/kaoyan/
├── module/ai/                          ← AI 多智能体核心模块
│   ├── agent/                          ← 6 个 Agent 实现
│   │   ├── PlannerAgent.java           规划 Agent（每日任务生成）
│   │   ├── TutorAgent.java             答疑 Agent（RAG + Tool Calling）
│   │   ├── SupervisorAgent.java        监督 Agent（断签/进度预警）
│   │   ├── PsychologyAgent.java        心理 Agent（情绪识别与安抚）
│   │   ├── ReviewAgent.java            复盘 Agent（周报生成）
│   │   └── BehaviorAnalysisAgent.java  行为分析 Agent（浏览/搜索画像）
│   │
│   ├── config/                         ← LLM 接入配置
│   │   ├── AiApiProperties.java        API 端点/密钥/模型名
│   │   └── AiConfig.java              RestTemplate + SSE 长连接模板
│   │
│   ├── controller/
│   │   └── AiAgentController.java      统一 REST 入口（/api/ai/*）
│   │
│   ├── service/                        ← LLM 调用层
│   │   ├── AiAgentService.java         接口（chat/chatWithHistory）
│   │   └── impl/AiAgentServiceImpl.java 实现（重试、Redis 对话记忆）
│   │
│   ├── entity/                         ← 数据库实体
│   │   ├── AiDailyTask.java            ai_daily_task
│   │   ├── AiInterventionLog.java      ai_intervention_log
│   │   ├── AiReport.java              ai_report
│   │   ├── AiUserEvent.java           ai_user_event
│   │   ├── KnowledgePoint.java         ai_knowledge_point（RAG 知识库）
│   │   └── UserAiProfile.java          user_ai_profile（长期记忆）
│   │
│   ├── mapper/                         ← MyBatis-Plus Mapper
│   │   ├── AiDailyTaskMapper.java
│   │   ├── AiInterventionLogMapper.java
│   │   ├── AiKnowledgePointMapper.java searchByKeywords() → RAG 检索核心
│   │   ├── AiReportMapper.java
│   │   ├── AiUserEventMapper.java
│   │   └── UserAiProfileMapper.java
│   │
│   ├── event/                          ← Spring 事件（Agent 间消息总线）
│   │   ├── UserCheckInEvent.java       打卡事件 → 触发 Planner + Psychology
│   │   ├── TaskCompletedEvent.java     任务完成事件 → 更新画像
│   │   └── UserDiaryCreatedEvent.java  日记事件（预留）
│   │
│   ├── listener/                       ← 事件监听器（异步触发 Agent）
│   │   ├── PlannerListener.java
│   │   ├── PsychologyListener.java
│   │   ├── DiaryPsychologyListener.java
│   │   └── TaskCompletedListener.java
│   │
│   ├── dto/
│   │   ├── AiAskDTO.java              答疑请求
│   │   └── AiEventDTO.java            行为事件上报
│   │
│   ├── vo/
│   │   ├── AiSummaryVO.java           社区首页 AI 摘要
│   │   ├── AiTaskVO.java              任务视图
│   │   ├── InterventionVO.java         干预日志视图
│   │   ├── RecommendationVO.java       知识点推荐
│   │   └── ReportHistoryVO.java        历史周报
│   │
│   └── util/
│       └── JsonArrayExtractor.java     LLM 输出 JSON 解析器
│
├── module/activity/                    ← 打卡模块（AI 事件源头）
│   ├── controller/CheckInController.java
│   ├── service/impl/CheckInServiceImpl.java  发布 UserCheckInEvent
│   ├── entity/po/{CheckInPO,UserStudyPO}.java
│   └── mapper/{CheckInMapper,UserStudyMapper,PointsLogMapper}.java
│
└── module/interview/                   ← AI 模拟面试模块
    ├── controller/InterviewController.java
    ├── config/QwenConfig.java          DashScope API 配置
    ├── service/
    │   ├── InterviewAiService.java
    │   ├── impl/InterviewAiServiceImpl.java
    │   └── impl/InterviewReportServiceImpl.java
    └── entity/dto/{AiChatRequest,AiChatResponse}.java

src/main/resources/
├── application.properties              ← ai.api.* 配置项
├── com/zzu/kaoyan/mapper/
│   └── KnowledgePointMapper.xml        RAG 检索 SQL（LIKE + 重要度排序）
└── sql/
    └── ai_tables.sql                   6 张 AI 表建表语句 + 种子数据
```

---

## 6 个 Agent 职责

| Agent | 触发方式 | 职责 |
|-------|---------|------|
| **PlannerAgent** | 用户打卡事件（异步） | 根据目标院校、当前进度、剩余时间，LLM 生成每日任务清单 |
| **TutorAgent** | 用户主动提问 `POST /api/ai/ask` | RAG 检索知识库 + Tool Calling，定位考点链路，支持 SSE 流式输出 |
| **SupervisorAgent** | 定时任务每日 21:00 | 扫描近 3 天任务完成率 <50% 的用户，生成督促/警告消息 |
| **PsychologyAgent** | 用户打卡事件（异步） | 分析打卡日记情绪关键词（焦虑/绝望/疲惫等），生成温暖的安抚话术 |
| **ReviewAgent** | 定时任务每周日 20:00 | 聚合 7 天学习数据，生成 Markdown 周报，定位薄弱点 |
| **BehaviorAnalysisAgent** | 定时任务每日 22:00 | 分析用户浏览/搜索/点赞/收藏行为，更新认知画像，生成学习建议 |

---

## 数据流

```
用户打卡 → CheckInServiceImpl
              ├─ publish UserCheckInEvent
              │    ├─ PlannerListener → PlannerAgent（LLM 生成任务）
              │    └─ PsychologyListener → PsychologyAgent（情绪分析）
              └─ publish TaskCompletedEvent
                   └─ TaskCompletedListener → 更新认知画像

定时任务：
  21:00  SupervisorAgent  → 扫描低完成率用户 → ai_intervention_log
  22:00  BehaviorAnalysisAgent → 分析 ai_user_event → 更新画像 + 建议
  周日20:00 ReviewAgent → 聚合7天数据 → ai_report

用户主动：
  POST /api/ai/ask → TutorAgent → RAG 检索 ai_knowledge_point → LLM 回答
```

---

## Agent 间消息总线

采用 **Spring Application Event** 机制，Agent 之间通过事件解耦：

| 事件 | 发布者 | 监听者 |
|------|--------|--------|
| `UserCheckInEvent` | `CheckInServiceImpl` | `PlannerListener`、`PsychologyListener` |
| `TaskCompletedEvent` | `AiAgentController` | `TaskCompletedListener` |
| `UserDiaryCreatedEvent` | （预留） | `DiaryPsychologyListener` |

所有监听器标注 `@Async`，Agent 异步执行不阻塞主流程。

---

## 长期记忆（User AI Profile）

`user_ai_profile` 表存储两个 JSON 字段：

- **cognitiveProfile**：认知画像 — 累计打卡天数、学习时长、完成任务数、上次活跃时间
- **psychologicalProfile**：情感画像 — 最近情绪标签、上次 AI 分析摘要、更新时间

各 Agent 在执行过程中读写画像，实现跨会话的用户记忆。

---

## LLM 接入

项目**未使用** LangChain4j 或 Spring AI，而是通过 `RestTemplate` 直接调用 OpenAI 兼容接口：

| 模块 | API | 模型 |
|------|-----|------|
| AI 模块（Agent） | `https://token-plan-cn.xiaomimimo.com/v1/chat/completions` | `mimo-v2.5` |
| 面试模块 | `https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions` | Qwen |

- `AiAgentServiceImpl`：3 次重试 + 指数退避，Redis 存储对话历史（最多 11 条，24h TTL）
- `TutorAgent`：支持 Tool Calling（`search_knowledge` 工具定义）和 SSE 流式输出

---

## RAG 检索

当前采用**关键词 LIKE 匹配**方案（非向量数据库）：

- 知识库表：`ai_knowledge_point`（学科、章节、标题、内容、关键词、重要度）
- 检索方法：`AiKnowledgePointMapper.searchByKeywords()` — 对 title/keywords/content 做 LIKE 匹配，按 importance 排序
- Tool Calling 模式下，LLM 自行决定调用 `search_knowledge` 工具获取相关知识点

---

## API 端点

`AiAgentController`（`/api/ai`）：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tasks` | 今日 AI 任务列表 |
| POST | `/tasks/{taskId}/complete` | 完成任务（发布事件） |
| GET | `/interventions` | 未读干预消息 |
| PUT | `/interventions/{id}/read` | 标记已读 |
| GET | `/report` | 生成本周周报 |
| GET | `/report/history` | 历史周报 |
| POST | `/ask` | 向答疑 Agent 提问 |
| POST | `/ask/stream` | SSE 流式答疑 |
| GET | `/summary` | 社区首页 AI 摘要 |
| POST | `/events` | 上报用户行为事件 |
| GET | `/recommendations` | AI 知识点推荐 |
| GET/POST/PUT/DELETE | `/knowledge` | 知识点 CRUD |
| GET/DELETE | `/chat/history` | 对话历史管理 |

---

## 数据库表

| 表名 | 对应实体 | 用途 |
|------|---------|------|
| `ai_daily_task` | `AiDailyTask` | 每日任务清单 |
| `ai_intervention_log` | `AiInterventionLog` | Agent 主动干预日志（警告/安抚/建议） |
| `ai_knowledge_point` | `KnowledgePoint` | 考研知识点库（RAG 检索源） |
| `ai_report` | `AiReport` | AI 周报 |
| `ai_user_event` | `AiUserEvent` | 用户行为事件（浏览/搜索/点赞/收藏） |
| `user_ai_profile` | `UserAiProfile` | 用户 AI 画像（认知 + 情感，长期记忆） |

建表 SQL：`src/main/resources/sql/ai_tables.sql`
