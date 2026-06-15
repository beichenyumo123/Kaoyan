# AI 多智能体学习伴侣模块 — 实施总结

> 最后更新：2026-06-04

---

## 一、模块概览

为考研社区平台构建了一套 **5 Agent 协同** 的 AI 学习伴侣系统，基于 Spring Event 驱动架构，支持 RAG 知识库检索、Tool Calling、SSE 流式输出、Redis 多轮对话记忆。

### 5 个 Agent

| Agent | 职责 | 触发方式 |
|-------|------|---------|
| **规划 Agent** (Planner) | 每日生成 3 条个性化任务 | 用户打卡时自动触发 |
| **答疑 Agent** (Tutor) | RAG + Tool Calling 回答学科问题 | 用户主动提问（普通/SSE 流式） |
| **心理 Agent** (Psychology) | 分析打卡感言/日记情绪，生成安抚寄语 | 打卡（有感言）/ 日记创建时 |
| **监督 Agent** (Supervisor) | 检测任务完成率低的用户，生成催学警示 | 每天 21:00 自动 + 手动触发 |
| **复盘 Agent** (Review) | 聚合 7 天数据生成 Markdown 周报 | 用户查看 + 每周日 20:00 自动生成 |
| **行为分析 Agent** (Behavior) | 分析用户浏览/搜索/收藏行为，更新画像 | 每天 22:00 自动 |

---

## 二、核心能力

### 2.1 RAG 知识库检索
- `ai_knowledge_point` 表存储 9 大学科知识点（数据结构、操作系统、计算机网络、计组、高数、线代、概率、英语、政治）
- MySQL FULLTEXT 索引 + ngram 分词器支持中文关键词检索
- 答疑 Agent 优先使用 Tool Calling 自主检索，降级为 RAG 模式

### 2.2 Tool Calling
- 答疑 Agent 定义 `search_knowledge` 工具，LLM 可自主决定是否调用知识库
- 支持多轮工具调用（最多 3 轮）
- 兼容 mimo API（OpenAI 协议）

### 2.3 SSE 流式输出
- `POST /api/ai/ask/stream` 返回 `text/event-stream`
- JSON 格式发送 chunk：`data:{"content":"..."}`，确保 `\n` 等特殊字符不破坏协议
- `SseEmitter` + `CompletableFuture.runAsync()` 异步执行

### 2.4 Redis 多轮对话记忆
- `ai:chat:history:{userId}` 存储对话历史
- 保留最近 5 轮（10 条消息）+ 1 条 system prompt
- TTL 24 小时自动过期

### 2.5 行为数据采集
- `POST /api/ai/events` 上报浏览/收藏/搜索/点赞事件
- `ai_user_event` 表存储，JSON 格式的 `event_data`
- 每日 22:00 行为分析 Agent 自动统计，更新用户画像的 `interestKeywords` 和 `browsePattern`

### 2.6 周报持久化
- `ai_report` 表存储历史周报（`user_id` + `week_start` 唯一约束）
- 支持 `GET /api/ai/report/history` 查询最近 N 周

### 2.7 智能推荐
- `GET /api/ai/recommendations` 基于用户画像推荐知识点
- 优先推荐与搜索/浏览相关的知识点，不足时补充高频考点

---

## 三、API 接口清单

### 任务管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ai/tasks` | 获取今日 AI 任务 |
| POST | `/api/ai/tasks/{taskId}/complete` | 完成任务（触发 TaskCompletedEvent） |

### 干预消息
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ai/interventions` | 获取未读干预日志 |
| PUT | `/api/ai/interventions/{id}/read` | 标记已读 |

### 答疑
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/ai/ask` | 普通提问（RAG + Tool Calling） |
| POST | `/api/ai/ask/stream` | SSE 流式提问 |
| GET | `/api/ai/chat/history` | 获取对话历史 |
| DELETE | `/api/ai/chat/history` | 清除对话历史 |

### 周报
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ai/report` | 生成本周周报 |
| GET | `/api/ai/report/history?limit=4` | 历史周报列表 |

### 社区首页
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ai/summary` | AI 摘要（任务进度/未读消息/打卡天数/今日建议） |
| GET | `/api/ai/recommendations` | 智能推荐知识点 |

### 行为采集
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/ai/events` | 上报行为事件（VIEW_POST/COLLECT_POST/SEARCH/LIKE_POST） |

### 知识库管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ai/knowledge` | 搜索知识点 |
| POST | `/api/ai/knowledge` | 新增知识点 |
| PUT | `/api/ai/knowledge/{id}` | 修改知识点 |
| DELETE | `/api/ai/knowledge/{id}` | 删除知识点 |

### 监督 Agent
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/ai/agent/supervisor/trigger` | 手动触发监督扫描（路演用） |

---

## 四、数据库表

| 表名 | 说明 |
|------|------|
| `ai_daily_task` | AI 每日任务 |
| `ai_intervention_log` | AI 干预日志（心理寄语/催学警示/周报） |
| `user_ai_profile` | 用户 AI 档案（认知画像 + 心理画像，JSON） |
| `ai_knowledge_point` | 考研知识点库（RAG 数据源） |
| `ai_report` | 周报持久化存储 |
| `ai_user_event` | 用户行为事件采集 |

---

## 五、Spring Event 驱动架构

| 事件 | 发布者 | 监听者 | 效果 |
|------|--------|--------|------|
| `UserCheckInEvent` | CheckInService | PlannerListener | 生成每日任务 |
| `UserCheckInEvent` | CheckInService | PsychologyListener | 分析情绪生成寄语（有感言时） |
| `TaskCompletedEvent` | AiAgentController | TaskCompletedListener | 更新认知画像完成任务数 |
| `UserDiaryCreatedEvent` | 日记模块（预留） | DiaryPsychologyListener | 分析日记情绪 |

---

## 六、定时任务

| Cron | Agent | 说明 |
|------|-------|------|
| `0 0 21 * * ?` | Supervisor | 每晚扫描任务完成率低的用户，生成催学警示 |
| `0 0 20 * * SUN` | Review | 每周日生成周报，写入 `ai_report` + `ai_intervention_log` |
| `0 0 22 * * ?` | Behavior | 每晚分析当日行为事件，更新画像，生成学习建议 |

---

## 七、关键技术决策

| 决策 | 说明 |
|------|------|
| mimo API（OpenAI 协议） | 从 DeepSeek 切换到 mimo，模型 `mimo-v2.5` |
| SSE 流式输出 | `SseEmitter` + JSON 格式 chunk，确保特殊字符不破坏协议 |
| max_tokens = 4096 | 从 1024 提升到 4096，避免长回答被截断 |
| SaToken SSE 兼容 | 全局拦截器 catch `SaTokenContextException`，SSE 端点手动校验 |
| JSON 格式 SSE chunk | `data:{"content":"..."}` 格式，`\n` 自动转义，前端 `JSON.parse` 还原 |

---

## 八、文件清单

```
src/main/java/com/zzu/kaoyan/module/ai/
├── agent/
│   ├── BehaviorAnalysisAgent.java    ← NEW: 行为分析定时任务
│   ├── PlannerAgent.java             ← 生成每日任务
│   ├── PsychologyAgent.java          ← 情绪分析 + 心理寄语
│   ├── ReviewAgent.java              ← MODIFIED: 周报持久化
│   ├── SupervisorAgent.java          ← 催学警示
│   └── TutorAgent.java               ← MODIFIED: max_tokens=4096
├── config/
│   ├── AiApiProperties.java          ← mimo API 配置
│   └── AiConfig.java                 ← RestTemplate beans（普通 + SSE 流式）
├── controller/
│   └── AiAgentController.java        ← MODIFIED: 新增 4 个接口 + SSE JSON 格式
├── dto/
│   ├── AiAskDTO.java
│   └── AiEventDTO.java               ← NEW: 行为事件上报 DTO
├── entity/
│   ├── AiDailyTask.java
│   ├── AiInterventionLog.java
│   ├── AiReport.java                 ← NEW: 周报持久化实体
│   ├── AiUserEvent.java              ← NEW: 行为事件实体
│   ├── KnowledgePoint.java
│   └── UserAiProfile.java
├── event/
│   ├── TaskCompletedEvent.java
│   ├── UserCheckInEvent.java
│   └── UserDiaryCreatedEvent.java
├── listener/
│   ├── DiaryPsychologyListener.java
│   ├── PlannerListener.java
│   ├── PsychologyListener.java
│   └── TaskCompletedListener.java
├── mapper/
│   ├── AiDailyTaskMapper.java
│   ├── AiInterventionLogMapper.java
│   ├── AiReportMapper.java           ← NEW
│   ├── AiUserEventMapper.java        ← NEW
│   ├── KnowledgePointMapper.java
│   └── UserAiProfileMapper.java
├── service/
│   ├── AiAgentService.java
│   ├── UserAiProfileService.java
│   └── impl/
│       ├── AiAgentServiceImpl.java   ← MODIFIED: mimo API + Redis 历史 + 重试
│       └── UserAiProfileServiceImpl.java
├── util/
│   └── JsonArrayExtractor.java
└── vo/
    ├── AiSummaryVO.java              ← NEW: 社区首页摘要
    ├── AiTaskVO.java
    ├── InterventionVO.java
    ├── RecommendationVO.java         ← NEW: 智能推荐
    └── ReportHistoryVO.java          ← NEW: 历史周报

src/main/resources/
├── sql/ai_tables.sql                 ← MODIFIED: 新增 ai_report + ai_user_event 建表
└── com/zzu/kaoyan/mapper/
    └── KnowledgePointMapper.xml

src/main/java/com/zzu/kaoyan/config/
└── SaTokenConfig.java                ← MODIFIED: SSE async dispatch 兼容

docs/
└── ai-module-api.md                  ← MODIFIED: 新增 9~11 节文档
```

---

## 九、已知限制 & 后续迭代

1. **行为采集前端埋点**：后端接口已就绪，前端需在 `PostDetail.vue`、`SearchResult.vue` 等页面接入
2. **智能推荐**：当前基于关键词匹配，后续可引入向量相似度检索
3. **Agent 协同**：各 Agent 目前独立运行，后续可引入共享上下文（如答疑 Agent 感知用户今日任务）
4. **周报历史**：`ai_report` 表已建好，旧周报需手动从 `ai_intervention_log` 迁移
5. **悬浮球**：纯前端功能，后端无需配合
