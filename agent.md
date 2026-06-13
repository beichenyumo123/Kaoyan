# AI 多智能体学习伴侣（Multi-Agent Study Companion）

> 最后更新：2026-06-07 | 模型：`qwen3.5-omni-flash`（DashScope） | Agent 数量：6

---

## 一、模块概览

为考研社区平台构建了一套 **6 Agent 协同** 的 AI 学习伴侣系统，基于 Spring Event 驱动架构，支持 RAG 知识库检索、Tool Calling、SSE 流式输出、多模态（图片+视频）识别、Redis 多轮对话记忆、用户长期画像。

### 6 个 Agent

| Agent | 职责 | 触发方式 | LLM 调用 |
|-------|------|---------|----------|
| **TutorAgent**（答疑） | RAG + Tool Calling + 多模态回答学科问题 | 用户主动提问 `POST /api/ai/ask` | 直接调用 RestTemplate |
| **PlannerAgent**（规划） | 每日生成 3 条个性化学习任务 | 用户打卡事件（异步） | 通过 AiAgentService |
| **PsychologyAgent**（心理） | 分析打卡感言/日记情绪，生成安抚寄语 | 打卡事件/日记事件（异步） | 通过 AiAgentService |
| **SupervisorAgent**（监督） | 检测任务完成率低的用户，生成催学警示 | 定时 21:00 + 手动触发 | 通过 AiAgentService |
| **ReviewAgent**（复盘） | 聚合 7 天数据生成 Markdown 周报 | 定时周日 20:00 + 用户查看 | 通过 AiAgentService |
| **BehaviorAnalysisAgent**（行为分析） | 分析浏览/搜索/收藏行为，更新画像 | 定时 22:00 | **不调用 LLM**（纯规则引擎） |

---

## 二、目录结构

所有路径基于 `src/main/java/com/zzu/kaoyan/module/ai/`，共 45 个 Java 文件。

```
module/ai/
├── agent/                                  ← 6 个 Agent 实现
│   ├── TutorAgent.java                    答疑 Agent（RAG + Tool Calling + 多模态 + 流式）
│   ├── PlannerAgent.java                  规划 Agent（每日任务 JSON 生成）
│   ├── PsychologyAgent.java               心理 Agent（情绪分析 + 安抚话术）
│   ├── SupervisorAgent.java               监督 Agent（低完成率警告）
│   ├── ReviewAgent.java                   复盘 Agent（Markdown 周报）
│   └── BehaviorAnalysisAgent.java         行为分析 Agent（浏览/搜索画像，无 LLM）
│
├── config/                                 ← LLM 接入配置
│   ├── AiApiProperties.java               @ConfigurationProperties(prefix="ai.api")
│   └── AiConfig.java                      RestTemplate bean（普通 + SSE 流式）
│
├── controller/
│   └── AiAgentController.java             统一 REST 入口（/api/ai/*，20+ 端点）
│
├── dto/                                    ← 入站请求
│   ├── AiAskDTO.java                      答疑请求（question, subject, sessionId, imageUrl）
│   └── AiEventDTO.java                    行为事件上报（eventType, eventData）
│
├── entity/                                 ← 数据库实体（8 张表）
│   ├── AiChatMessage.java                 ai_chat_message — 会话消息
│   ├── AiChatSession.java                 ai_chat_session — 答疑会话
│   ├── AiDailyTask.java                   ai_daily_task — 每日规划任务
│   ├── AiInterventionLog.java             ai_intervention_log — 干预日志
│   ├── AiReport.java                      ai_report — 周报持久化
│   ├── AiUserEvent.java                   ai_user_event — 用户行为事件
│   ├── KnowledgePoint.java                ai_knowledge_point — RAG 知识库
│   └── UserAiProfile.java                 user_ai_profile — 用户 AI 画像
│
├── event/                                  ← Spring 事件（Agent 间消息总线）
│   ├── UserCheckInEvent.java              打卡事件 → Planner + Psychology
│   ├── TaskCompletedEvent.java            任务完成事件 → 更新画像
│   └── UserDiaryCreatedEvent.java         日记事件（预留）
│
├── listener/                               ← 事件监听器（@Async 异步）
│   ├── PlannerListener.java               监听 UserCheckInEvent → PlannerAgent
│   ├── PsychologyListener.java            监听 UserCheckInEvent → PsychologyAgent
│   ├── DiaryPsychologyListener.java       监听 UserDiaryCreatedEvent → PsychologyAgent
│   └── TaskCompletedListener.java         监听 TaskCompletedEvent → 更新认知画像
│
├── mapper/                                 ← MyBatis-Plus Mapper（8 个）
│   ├── AiChatMessageMapper.java
│   ├── AiChatSessionMapper.java
│   ├── AiDailyTaskMapper.java
│   ├── AiInterventionLogMapper.java
│   ├── AiKnowledgePointMapper.java        searchByKeywords() → RAG 检索核心
│   ├── AiReportMapper.java
│   ├── AiUserEventMapper.java
│   └── UserAiProfileMapper.java
│
├── service/                                ← LLM 调用层
│   ├── AiAgentService.java                接口（chat / chatWithHistory / getHistory / clearHistory）
│   ├── UserAiProfileService.java          接口（ensureProfile / updateCognitive / updatePsychological）
│   └── impl/
│       ├── AiAgentServiceImpl.java        实现（重试+退避，Redis 对话记忆）
│       └── UserAiProfileServiceImpl.java  实现（JSON 画像管理）
│
├── util/
│   └── JsonArrayExtractor.java            LLM JSON 输出解析器
│
└── vo/                                     ← 出站视图
    ├── AiSummaryVO.java                   社区首页 AI 摘要
    ├── AiTaskVO.java                      任务视图
    ├── ChatMessageVO.java                 聊天消息视图
    ├── ChatSessionVO.java                 会话列表视图
    ├── InterventionVO.java                干预日志视图
    ├── RecommendationVO.java              知识点推荐（含 KnowledgeRecommendation）
    └── ReportHistoryVO.java               历史周报视图
```

### 相关资源文件

```
src/main/resources/
├── application.properties                 ← ai.api.* 配置
├── com/zzu/kaoyan/mapper/
│   └── KnowledgePointMapper.xml           RAG 检索 SQL（LIKE 匹配 + 重要度排序）
└── sql/
    ├── ai_tables.sql                      6 张核心表 DDL + 种子数据
    └── ai_chat_tables.sql                 2 张会话表 DDL
```

---

## 三、各 Agent 详细说明

### 3.1 TutorAgent（答疑 Agent）— 最复杂的 Agent

**触发方式**：`POST /api/ai/ask`（非流式）和 `POST /api/ai/ask/stream`（SSE 流式）

**核心能力**：
- **RAG 检索**：从用户问题中提取关键词 → `AiKnowledgePointMapper.searchByKeywords()` LIKE 匹配 → 注入 LLM 上下文
- **Tool Calling**：定义 `search_knowledge` 工具，LLM 自主决定是否检索知识库，最多 3 轮工具循环
- **多模态问答**：支持图片 Base64 输入，调用视觉模型分析题目截图
- **OCR 降级**：多模态失败时回退到 PaddleOCR 文字识别
- **薄弱点注入**：从错题模块获取用户最近 30 条错题的知识点频率，Top 5 注入系统提示
- **流式输出**：SSE 逐 token 推送，前端实时渲染

**LLM 参数**：
| 模式 | temperature | max_tokens | 其他 |
|------|------------|------------|------|
| RAG 纯文本 | 0.7 | 4096 | — |
| 工具调用 | 0.7 | 4096 | tools=[search_knowledge] |
| 多模态 | 0.7 | 32768 | reasoning_effort=low |
| 知识点提取 | 0.1 | 100 | — |

**系统提示要点**：
- 角色：资深考研辅导专家（408 计算机 + 数学 + 英语 + 政治）
- 要求引用「📚 考点出处」标注知识来源
- 动态追加「用户薄弱知识点」区块（从错题数据聚合）

**与错题模块的集成**：
- 注入 `MistakeNoteMapper`，调用 `selectRecentKnowledgePoints(userId)` 获取最近 30 条错题
- 按知识点频率统计，取 Top 5 写入系统提示
- 多模态图片问答时注入 `OCRService` 作为降级方案

**双轨 RestTemplate**：
- 非流式：`aiRestTemplate`（readTimeout=120s）
- 流式：`aiStreamRestTemplate`（readTimeout=300s，禁用错误抛出）

---

### 3.2 PlannerAgent（规划 Agent）

**触发方式**：`UserCheckInEvent` → `PlannerListener`（@Async 异步）

**流程**：
1. 读取 `UserAiProfile.cognitiveProfile`（学习统计）
2. 构造 prompt：根据目标院校、当前进度、剩余时间生成 3 项任务
3. 调用 `AiAgentService.chat()` → 解析 JSON 数组
4. 写入 `ai_daily_task` 表

**LLM 参数**：temperature=0.7, max_tokens=1024

**输出格式**（严格 JSON 数组，无 markdown）：
```json
[{"title":"...", "description":"...", "subject":"...", "estimatedMinutes":30}]
```

---

### 3.3 PsychologyAgent（心理 Agent）

**触发方式**：`UserCheckInEvent`（打卡带感言时）→ `PsychologyListener`（@Async）

**流程**：
1. 用关键词检测情绪标签（焦虑/绝望/疲惫/开心/满足/平淡）
2. 调用 `AiAgentService.chat()` 生成安抚/鼓励话术
3. 写入 `ai_intervention_log`（type=PSYCHOLOGY）
4. 更新 `UserAiProfile.psychologicalProfile`

**LLM 参数**：temperature=0.7, max_tokens=1024

**系统提示**：角色为「富有同理心的心理辅导员」，负面情绪生成温暖安慰，正面情绪生成赞美鼓励。

---

### 3.4 SupervisorAgent（监督 Agent）

**触发方式**：定时 `0 0 21 * * ?`（每晚 21:00）+ 手动 `POST /api/ai/agent/supervisor/trigger`

**流程**：
1. 查询 `ai_daily_task` 近 3 天任务完成率 < 50% 的用户
2. 按 userId 分组统计
3. 调用 `AiAgentService.chat()` 生成严厉警告
4. 写入 `ai_intervention_log`（type=WARNING）

**LLM 参数**：temperature=0.7, max_tokens=1024

**系统提示**：角色为「408 考研面试官组长」，语气严厉、权威。

---

### 3.5 ReviewAgent（复盘 Agent）

**触发方式**：定时 `0 0 20 * * SUN`（每周日 20:00）+ 用户手动 `GET /api/ai/report`

**流程**：
1. 聚合 7 天数据：打卡天数（`CheckInMapper`）、学习时长、AI 任务完成统计（`AiDailyTaskMapper`）
2. 调用 `AiAgentService.chat()` 生成 Markdown 周报
3. 写入 `ai_report`（user_id + week_start 唯一约束）
4. 同时写入 `ai_intervention_log`（type=WEEKLY_REPORT）

**LLM 参数**：temperature=0.7, max_tokens=1024

**报告结构**：
- 📊 本周亮点
- ⚠️ 薄弱环节
- 📝 下周学习计划

---

### 3.6 BehaviorAnalysisAgent（行为分析 Agent）

**触发方式**：定时 `0 0 22 * * ?`（每晚 22:00）

**流程**：
1. 查询 `ai_user_event` 当日行为事件（VIEW_POST / COLLECT_POST / SEARCH / LIKE_POST）
2. **纯规则统计**（不调用 LLM）：计数各类事件，从 SEARCH 事件提取兴趣关键词
3. 更新 `UserAiProfile.cognitiveProfile` 的 `browsePattern` 和 `interestKeywords`
4. 生成学习建议写入 `ai_intervention_log`（type=SUGGESTION）

**注意**：这是唯一不调用 LLM 的 Agent，完全基于规则。

---

## 四、事件驱动架构

采用 Spring `ApplicationEventPublisher` 机制，Agent 之间通过事件解耦：

```
用户打卡 → CheckInServiceImpl
              ├─ publish UserCheckInEvent
              │    ├─ PlannerListener（@Async）→ PlannerAgent → ai_daily_task
              │    └─ PsychologyListener（@Async）→ PsychologyAgent → ai_intervention_log
              └─ (打卡本身写入 check_in 表)

用户完成任务 → AiAgentController.completeTask()
              └─ publish TaskCompletedEvent
                   └─ TaskCompletedListener（@Async）→ 更新 UserAiProfile.cognitiveProfile

用户写日记 → (预留)
              └─ publish UserDiaryCreatedEvent
                   └─ DiaryPsychologyListener（@Async）→ PsychologyAgent
```

| 事件 | 发布者 | 监听者 | 效果 |
|------|--------|--------|------|
| `UserCheckInEvent` | `CheckInServiceImpl` | `PlannerListener`、`PsychologyListener` | 生成任务 + 情绪分析 |
| `TaskCompletedEvent` | `AiAgentController` | `TaskCompletedListener` | 更新认知画像 |
| `UserDiaryCreatedEvent` | 日记模块（预留） | `DiaryPsychologyListener` | 分析日记情绪 |

所有监听器标注 `@Async`，Agent 异步执行不阻塞主流程。

---

## 五、LLM 接入

### 5.1 当前配置

项目**未使用** LangChain4j 或 Spring AI SDK，通过 `RestTemplate` 直接调用 OpenAI 兼容接口。

| 模块 | 端点 | 模型 | API Key |
|------|------|------|---------|
| AI 多智能体 | `https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions` | `qwen3.5-omni-flash` | `${DASHSCOPE_API_KEY}` |
| 模拟面试 | 同上 | `qwen3.7-plus`（QwenConfig 硬编码） | `${DASHSCOPE_API_KEY}` |

两个模块共享同一个 DashScope 端点，使用同一个环境变量。

### 5.2 配置结构

`application.properties`：
```properties
ai.api.endpoint=https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
ai.api.key=${DASHSCOPE_API_KEY:}
ai.api.model=qwen3.5-omni-flash
```

`AiApiProperties.java`（`@ConfigurationProperties(prefix="ai.api")`）：
- `endpoint` — Java 默认值 `https://api.deepseek.com/chat/completions`（被 properties 覆盖）
- `key` — 默认为空
- `model` — Java 默认值 `deepseek-chat`（被 properties 覆盖为 `qwen3.5-omni-flash`）

### 5.3 两个 RestTemplate Bean

| Bean | 连接超时 | 读取超时 | 错误处理 | 用途 |
|------|---------|---------|---------|------|
| `aiRestTemplate` | 10s | 120s（2min） | 默认（抛异常） | 非流式 LLM 调用 |
| `aiStreamRestTemplate` | 10s | 300s（5min） | 覆盖（不抛异常） | SSE 流式调用 |

### 5.4 调用封装

`AiAgentServiceImpl`：
- 3 次重试 + 指数退避（1s, 2s, 4s）
- Redis 存储对话历史：key=`ai:chat:history:{userId}`，TTL=24h
- 保留最近 11 条消息（1 system + 5 轮 user/assistant）

`TutorAgent`：
- **绕过** `AiAgentService`，直接使用 `RestTemplate`
- 自行构造 OpenAI 格式请求体（含 tools 定义、多模态 content 数组）
- 流式使用 `ResponseExtractor` 逐行解析 SSE

---

## 六、RAG 检索系统

### 6.1 知识库

表：`ai_knowledge_point`，字段：
- `subject` — 学科（数据结构、操作系统、计网、计组、高数、线代、概率、英语、政治）
- `chapter` — 章节
- `title` — 知识点标题
- `content` — 详细内容
- `keywords` — 关键词（逗号分隔）
- `importance` — 重要度（HIGH / MEDIUM / LOW）

种子数据：SQL 中包含 20 条初始知识点，覆盖 9 大学科。

### 6.2 检索方式

**当前方案：MySQL LIKE 匹配**（非向量数据库）

`AiKnowledgePointMapper.searchByKeywords()` → `KnowledgePointMapper.xml`：
- 对 `title`、`keywords`、`content` 三列做 `LIKE '%keyword%'` 匹配
- 多关键词用 `OR` 连接
- 按 importance 排序（HIGH > MEDIUM > LOW）
- 限制返回 5 条（控制器 knowledge 搜索接口为 20 条）

### 6.3 两种检索触发方式

| 模式 | 触发者 | 说明 |
|------|--------|------|
| **RAG 模式** | TutorAgent 自动 | 从用户问题提取关键词 → 检索 → 注入上下文 |
| **Tool Calling 模式** | LLM 自主决定 | LLM 判断需要检索时调用 `search_knowledge` 工具 |

Tool Calling 最多 3 轮，超限后降级为纯 RAG 模式。

---

## 七、会话管理系统

### 7.1 数据库会话（持久化）

表 `ai_chat_session` + `ai_chat_message`：

- **创建**：`POST /api/ai/chat/sessions` → 新建会话，title 为 "新对话"
- **提问**：`POST /api/ai/ask` 可带 `sessionId` 恢复会话；不带则自动创建新会话
- **消息列表**：`GET /api/ai/chat/sessions/{id}/messages` → `ChatMessageVO` 列表
- **会话列表**：`GET /api/ai/chat/sessions` → `ChatSessionVO`（含 lastMessage 预览 + messageCount）
- **删除**：`DELETE /api/ai/chat/sessions/{id}` → 软删除

### 7.2 Redis 对话记忆（运行时）

`AiAgentServiceImpl` 维护 key=`ai:chat:history:{userId}` 的 Redis List：
- 保存最近 11 条消息（1 system + 5 轮对话）
- TTL 24 小时自动过期
- 用于多轮对话上下文

### 7.3 流式会话处理

`POST /api/ai/ask/stream` 的 SSE 流程：
1. 同步线程：SaToken 认证 + 会话创建/恢复
2. 发送 `meta` 事件（sessionId, title）
3. 异步线程 `CompletableFuture.runAsync()`：
   - 调用 `tutorAgent.answerStream(chunk -> emitter.send(...))`
   - 每个 token 包装为 `{"content":"..."}` 发送
   - 完成后保存完整 assistant 回复到 `ai_chat_message`

---

## 八、用户 AI 画像（长期记忆）

表 `user_ai_profile`，两个 JSON 字段：

### cognitiveProfile（认知画像）
```json
{
  "totalCheckDays": 45,
  "continuousDays": 12,
  "totalStudyHours": 320.5,
  "lastActive": "2026-06-07",
  "browsePattern": "偏好数据结构与操作系统",
  "interestKeywords": ["B+树", "死锁", "虚拟内存"],
  "completedTaskCount": 28
}
```
更新者：`PlannerAgent`、`TaskCompletedListener`、`BehaviorAnalysisAgent`

### psychologicalProfile（心理画像）
```json
{
  "recentEmotion": "焦虑",
  "lastAnalysis": "用户近期压力较大...",
  "updatedAt": "2026-06-07T21:30:00"
}
```
更新者：`PsychologyAgent`

---

## 九、跨模块集成

### 9.1 AI ↔ 错题模块

| 方向 | 机制 | 说明 |
|------|------|------|
| AI 读取错题 | `TutorAgent` 注入 `MistakeNoteMapper` | `selectRecentKnowledgePoints(userId)` 获取最近 30 条错题知识点 |
| AI 注入薄弱点 | `TutorAgent.buildWeaknessContext()` | 统计知识点频率，Top 5 写入系统提示 |
| 错题调用 AI | `MistakeNoteServiceImpl` 注入 `AiApiProperties` + `RestTemplate` | `extractKnowledgePoints()` 调用 LLM 自动提取知识点 |
| AI 读取 OCR | `TutorAgent` 注入 `OCRService` | 多模态失败时降级为 OCR 文字识别 |
| 错题关联消息 | `AiChatMessage.id` ↔ `MistakeNotePO.chatMessageId` | 防止重复快速保存，追踪来源 |

### 9.2 AI ↔ 打卡模块

| 方向 | 机制 | 说明 |
|------|------|------|
| 打卡 → AI | `CheckInServiceImpl` 发布 `UserCheckInEvent` | 触发 Planner + Psychology |
| AI 读取打卡 | `ReviewAgent` 注入 `CheckInMapper` | 周报聚合打卡天数和学习时长 |
| AI 读取学习统计 | `AiAgentController` 注入 `UserStudyMapper` | 社区首页摘要获取连续天数 |

### 9.3 AI ↔ 面试模块

面试模块独立运行，不通过事件总线与 Agent 通信。两者共享 DashScope API 但使用不同的 RestTemplate bean（`qwenRestTemplate` vs `aiRestTemplate`）和不同的模型（`qwen3.7-plus` vs `qwen3.5-omni-flash`）。

### 9.4 两个知识系统

项目中存在两套独立的知识点体系，互不交叉：

| 系统 | 表 | 结构 | 用途 |
|------|-----|------|------|
| 错题模块 | `knowledge_point` | 树形结构（parent-child, level, sort） | 错题归类、按章节浏览 |
| AI 模块 | `ai_knowledge_point` | 扁平结构（subject, title, content） | RAG 检索、LLM 上下文 |

`MistakeNotePO.knowledgePoints` 字段是自由文本（LLM 提取或手动输入的逗号分隔字符串），不与任何一方自动对齐。

---

## 十、定时任务

`@EnableScheduling` 在 `SchedulingConfig.java` 中启用。

| Cron | Agent | 方法 | 说明 |
|------|-------|------|------|
| `0 0 21 * * ?` | SupervisorAgent | `scheduledScan()` | 每晚 21:00 扫描低完成率用户 |
| `0 0 20 * * SUN` | ReviewAgent | `scheduledWeeklyReport()` | 每周日 20:00 自动生成周报 |
| `0 0 22 * * ?` | BehaviorAnalysisAgent | `analyzeDailyBehavior()` | 每晚 22:00 分析行为事件 |

---

## 十一、API 端点全表

所有端点位于 `AiAgentController`，前缀 `/api/ai`，除 `/ask/stream` 外均需 `@SaCheckLogin`。

### 任务
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/tasks` | 今日 AI 任务列表 |
| POST | `/tasks/{taskId}/complete` | 完成任务（发布 TaskCompletedEvent） |

### 答疑
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ask` | 非流式提问（返回完整 JSON） |
| POST | `/ask/stream` | SSE 流式提问（text/event-stream） |

### 会话
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/chat/sessions` | 会话列表 |
| POST | `/chat/sessions` | 创建新会话 |
| GET | `/chat/sessions/{id}/messages` | 会话消息列表 |
| DELETE | `/chat/sessions/{id}` | 软删除会话 |
| GET | `/chat/history` | [已废弃] Redis 对话历史 |
| DELETE | `/chat/history` | [已废弃] 清除 Redis 历史 |

### 干预消息
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/interventions` | 未读干预消息 |
| PUT | `/interventions/{id}/read` | 标记已读 |

### 周报
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/report` | 本周周报（Markdown） |
| GET | `/report/history?limit=4` | 历史周报列表 |

### 知识库
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/knowledge?keyword=&subject=` | 搜索知识点（20 条） |
| POST | `/knowledge` | 新增知识点 |
| PUT | `/knowledge/{id}` | 更新知识点 |
| DELETE | `/knowledge/{id}` | 删除知识点 |

### 社区首页
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/summary` | AI 摘要（任务进度 / 未读消息 / 打卡天数 / 今日建议） |
| GET | `/recommendations` | 智能推荐知识点 |

### 行为采集 & 监督
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/events` | 上报行为事件（VIEW_POST / COLLECT_POST / SEARCH / LIKE_POST） |
| POST | `/agent/supervisor/trigger` | 手动触发监督扫描 |

---

## 十二、数据库表

| 表名 | 实体 | 用途 |
|------|------|------|
| `ai_daily_task` | `AiDailyTask` | 每日 AI 规划的任务 |
| `ai_intervention_log` | `AiInterventionLog` | Agent 主动干预日志（安抚/警告/建议/周报） |
| `ai_knowledge_point` | `KnowledgePoint` | 考研知识点库（RAG 数据源） |
| `ai_report` | `AiReport` | 周报持久化（user_id + week_start 唯一） |
| `ai_user_event` | `AiUserEvent` | 用户行为事件（浏览/搜索/点赞/收藏） |
| `user_ai_profile` | `UserAiProfile` | 用户 AI 画像（认知 + 心理，JSON 字段） |
| `ai_chat_session` | `AiChatSession` | 答疑会话 |
| `ai_chat_message` | `AiChatMessage` | 会话消息（含 imageUrl 支持多模态） |

DDL 文件：
- `src/main/resources/sql/ai_tables.sql` — 前 6 张表 + 种子数据
- `src/main/resources/sql/ai_chat_tables.sql` — 后 2 张会话表

---

## 十三、技术决策与限制

### 关键决策
| 决策 | 说明 |
|------|------|
| 无 AI SDK | 不依赖 Spring AI / LangChain4j，直接 RestTemplate HTTP 调用 |
| OpenAI 兼容协议 | 请求/响应格式与 OpenAI Chat Completions API 一致 |
| 模型中立 | 通过 `application.properties` 切换模型，无需改代码 |
| SSE JSON 格式 | `data:{"content":"..."}` ，确保 `\n` 等特殊字符不破坏协议 |
| MySQL LIKE RAG | 当前使用 SQL LIKE 匹配而非向量检索 |
| 两套知识体系 | 错题模块的树形知识树与 AI 模块的扁平知识库独立维护 |
| SaToken SSE 兼容 | 全局拦截器 catch `SaTokenContextException`，SSE 端点手动校验 |

### 已知限制
1. **RAG 精度**：LIKE 匹配缺乏语义理解，后续可引入向量相似度检索
2. **薄弱点分析无状态**：每次提问重新查询错题表统计，不持久化薄弱点画像
3. **周报无错题数据**：ReviewAgent 只看打卡和任务完成率，不分析错题掌握情况
4. **两套知识体系不互通**：错题模块的知识树、AI 模块的 RAG 知识库、错题的 knowledge_points 文本字段三者从不互相对齐
5. **无复习反馈闭环**：用户复习错题提高掌握度后，Agent 无感知（`completeReview` 不发布事件）
6. **BehaviorAnalysisAgent 纯规则**：不调用 LLM，兴趣关键词提取较粗糙
7. **行为采集前端埋点**：后端接口已就绪，前端需接入
8. **面试模块独立**：面试模块不使用多 Agent 事件总线，两套 LLM 配置独立维护
