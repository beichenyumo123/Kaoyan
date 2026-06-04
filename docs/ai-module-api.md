# AI 多智能体学习伴侣 — 前端对接文档

> 基础路径：`/api/ai`  
> 认证方式：所有接口均需登录（Header 携带 Sa-Token），未登录返回 `401`  
> 统一返回格式：`{ "code": 200, "message": "success", "data": ... }`

---

## 一、模块概览

系统包含 **5 个 AI Agent**，协同工作：

| Agent | 角色 | 触发方式 | 对前端的意义 |
|-------|------|---------|-------------|
| **规划 Agent** (Planner) | 每日生成 3 条个性化任务 | 用户打卡时自动触发 | 展示「今日任务」列表 |
| **答疑 Agent** (Tutor) | 基于知识库 RAG 回答学科问题 | 用户主动提问 | AI 对话/问答页面 |
| **心理 Agent** (Psychology) | 分析打卡感言/日记情绪，生成安抚寄语 | 打卡时（有感言）/ 日记创建时 自动触发 | 展示「AI 寄语」通知 |
| **监督 Agent** (Supervisor) | 检测任务完成率低的用户，生成催学警示 | 每天 21:00 自动 + 手动触发 | 展示「警示通知」 |
| **复盘 Agent** (Review) | 生成周学习报告（Markdown） | 用户主动查看 + 每周日 20:00 自动生成 | 展示「周报」页面 |

---

## 二、接口列表

### 2.1 获取今日 AI 任务

```
GET /api/ai/tasks
```

**说明**：获取当天由规划 Agent 生成的个性化学习任务。每天打卡后自动生成 3 条。

**请求参数**：无

**响应 `data`**：`AiTaskVO[]`

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1001,
      "taskDate": "2026-06-04",
      "taskContent": "复习数据结构第三章二叉树遍历，完成5道课后习题",
      "importance": "HIGH",
      "status": 0,
      "agentTips": "二叉树是408高频考点，建议结合真题练习",
      "createdAt": "2026-06-04T08:30:00"
    },
    {
      "id": 2,
      "userId": 1001,
      "taskDate": "2026-06-04",
      "taskContent": "背诵英语阅读高频词汇30个",
      "importance": "MEDIUM",
      "status": 1,
      "agentTips": "利用碎片时间反复记忆",
      "createdAt": "2026-06-04T08:30:01"
    }
  ]
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 任务 ID（完成任务时需要） |
| `taskDate` | String (Date) | 任务日期，格式 `yyyy-MM-dd` |
| `taskContent` | String | 任务描述 |
| `importance` | String | 重要程度：`HIGH` / `MEDIUM` / `LOW` |
| `status` | Integer | 完成状态：`0` = 未完成，`1` = 已完成 |
| `agentTips` | String | Agent 给的学习建议 |

---

### 2.2 完成任务

```
POST /api/ai/tasks/{taskId}/complete
```

**说明**：标记某条任务为已完成。

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `taskId` | Long | 任务 ID |

**响应 `data`**：`null`（成功时）

**错误码**：
- `404` — 任务不存在或不属于当前用户
- `400` — 任务已完成，无需重复操作

---

### 2.3 向答疑 Agent 提问

```
POST /api/ai/ask
```

**说明**：向答疑 Agent 提问，基于考研知识库 RAG 增强回答，返回带考点出处的解答。

**请求体**：

```json
{
  "question": "B+树和B树有什么区别？",
  "subject": "数据结构"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `question` | String | ✅ | 用户提问内容 |
| `subject` | String | ❌ | 限定学科（为空则全学科检索） |

**可选的 `subject` 值**：`数据结构` / `操作系统` / `计算机网络` / `计算机组成原理` / `高等数学` / `线性代数` / `概率论` / `英语` / `政治`

**响应 `data`**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "question": "B+树和B树有什么区别？",
    "answer": "B树与B+树的主要区别如下：\n\n1. **数据存储位置**：B树所有节点都存储数据，B+树只有叶子节点存储数据...\n\n📚 **考点出处**：\n- [数据结构] 树 — B树与B+树的区别"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `question` | String | 原始问题（回显） |
| `answer` | String | Agent 回答（Markdown 格式，含考点出处） |

---

### 2.3b 向答疑 Agent 流式提问（SSE）

```
POST /api/ai/ask/stream
Content-Type: application/json
Accept: text/event-stream
```

**说明**：与 `/ask` 功能相同，但以 SSE 流式返回，AI 逐字输出，体验更好。

**请求体**：与 2.3 完全相同

**响应格式**：`text/event-stream`

```
B树与B+树的主要区别如下：

1. **数据存储位置**：B树所有节点都存储数据，B+树只有叶子节点存储数据...
```

**前端对接示例**：

```javascript
const response = await fetch('/api/ai/ask/stream', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ question: 'B+树和B树的区别', subject: '数据结构' })
});

const reader = response.body.getReader();
const decoder = new TextDecoder();

while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  const text = decoder.decode(value, { stream: true });
  // 追加到页面显示
  appendToChat(text);
}
```

**注意事项**：
- 超时时间：5 分钟
- 响应中每个 chunk 是一个文本片段（非 JSON），直接拼接即可
- 网络断开时连接自动结束

---

### 2.4 获取未读干预消息

```
GET /api/ai/interventions
```

**说明**：获取当前用户所有未读的 AI 干预消息（心理寄语 + 监督警示）。

**请求参数**：无

**响应 `data`**：`InterventionVO[]`

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1001,
      "agentName": "Psychology",
      "triggerReason": "打卡感言分析（连续3天）",
      "interventionContent": "你已经连续坚持3天了，非常棒！焦虑是正常的，说明你在乎...",
      "userReaction": "UNREAD",
      "createdAt": "2026-06-04T08:35:00"
    },
    {
      "id": 2,
      "userId": 1001,
      "agentName": "Supervisor",
      "triggerReason": "近3天任务完成率33%（3/9）",
      "interventionContent": "你最近三天只完成了三分之一的任务，考研不会等人！",
      "userReaction": "UNREAD",
      "createdAt": "2026-06-04T21:00:00"
    }
  ]
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 干预日志 ID（标记已读时需要） |
| `agentName` | String | 来源 Agent：`Psychology`（心理）/ `Supervisor`（监督）/ `Tutor`（答疑） |
| `triggerReason` | String | 触发原因描述（如"打卡感言分析"、"日记情感分析"、"近3天任务完成率33%"等） |
| `interventionContent` | String | 干预内容文本 |
| `userReaction` | String | 阅读状态：`UNREAD` / `READ` |
| `createdAt` | String (DateTime) | 生成时间 |

---

### 2.5 标记干预消息已读

```
PUT /api/ai/interventions/{id}/read
```

**说明**：将某条干预消息标记为已读。

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 干预日志 ID |

**响应 `data`**：`null`（成功时）

**错误码**：
- `404` — 干预日志不存在或不属于当前用户

---

### 2.6 获取周报

```
GET /api/ai/report
```

**说明**：获取由复盘 Agent 生成的本周学习报告（Markdown 格式）。

**请求参数**：无

**响应 `data`**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "markdown": "# 📊 本周学情透视报告\n\n## ✨ 高光时刻\n- 连续打卡 5 天，毅力值得肯定！\n...\n\n## ⚠️ 薄弱点警示\n- AI 任务完成率仅 40%，需加强执行力\n...\n\n## 📋 下周复习大纲建议\n..."
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `markdown` | String | Markdown 格式的周报内容 |

---

### 2.7 手动触发监督 Agent（路演演示用）

```
POST /api/ai/agent/supervisor/trigger
```

**说明**：手动触发监督 Agent 扫描（正常流程为每天 21:00 自动执行）。用于演示场景。

**请求参数**：无

**响应 `data`**：`String`

```json
{
  "code": 200,
  "message": "success",
  "data": "监督 Agent 扫描已触发"
}
```

---

### 2.8 搜索知识点

```
GET /api/ai/knowledge?keyword=B%2B树&subject=数据结构
```

**说明**：搜索考研知识库，支持按关键词和学科筛选。

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | String | ❌ | 搜索关键词（多个用逗号分隔） |
| `subject` | String | ❌ | 学科筛选 |

- 两个参数都为空时，返回最近 50 条知识点
- 只传 `subject` 时，返回该学科全部知识点
- 传 `keyword` 时，按关键词模糊匹配标题/关键词/内容

**响应 `data`**：`KnowledgePoint[]`

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "subject": "数据结构",
      "chapter": "树",
      "title": "B树与B+树的区别",
      "content": "1. B树所有节点都存储数据...",
      "keywords": "B树,B+树,索引,数据库",
      "importance": "HIGH",
      "createdAt": "2026-06-04T00:00:00"
    }
  ]
}
```

---

### 2.9 新增知识点（管理员）

```
POST /api/ai/knowledge
```

**请求体**：

```json
{
  "subject": "数据结构",
  "chapter": "排序",
  "title": "堆排序",
  "content": "堆排序利用二叉堆结构...",
  "keywords": "堆排序,二叉堆,大顶堆,小顶堆",
  "importance": "HIGH"
}
```

---

### 2.10 修改知识点（管理员）

```
PUT /api/ai/knowledge/{id}
```

**路径参数**：`id` — 知识点 ID

**请求体**：同 2.9，字段可选（传什么更新什么）

---

### 2.11 删除知识点（管理员）

```
DELETE /api/ai/knowledge/{id}
```

---

### 2.12 获取对话历史

```
GET /api/ai/chat/history
```

**说明**：获取当前用户的 AI 对话历史（答疑 Agent 的多轮记忆）。返回最近 5 轮（10 条）user/assistant 消息。

**响应 `data`**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "role": "user", "content": "B+树和B树有什么区别？" },
    { "role": "assistant", "content": "B树与B+树的主要区别如下：\n\n1. ..." },
    { "role": "user", "content": "那红黑树呢？" },
    { "role": "assistant", "content": "红黑树是一种自平衡二叉搜索树..." }
  ]
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `role` | String | `user`（用户提问）/ `assistant`（AI 回答） |
| `content` | String | 消息内容 |

**前端用法**：页面加载时调用此接口恢复对话历史，渲染为对话气泡。

---

### 2.13 清除对话历史

```
DELETE /api/ai/chat/history
```

**说明**：清除当前用户的 AI 对话历史（答疑 Agent 的多轮记忆）。

**响应 `data`**：`null`

---

## 三、通用数据模型

### 统一返回结构 `Result<T>`

```typescript
interface Result<T> {
  code: number;      // 200=成功, 400=参数错误, 401=未登录, 403=无权限, 404=不存在, 500=服务器异常
  message: string;
  data: T;
}
```

### AiTaskVO（AI 任务）

```typescript
interface AiTaskVO {
  id: number;
  userId: number;
  taskDate: string;        // "2026-06-04"
  taskContent: string;
  importance: "HIGH" | "MEDIUM" | "LOW";
  status: 0 | 1;           // 0=未完成, 1=已完成
  agentTips: string;
  createdAt: string;       // ISO DateTime
}
```

### InterventionVO（干预消息）

```typescript
interface InterventionVO {
  id: number;
  userId: number;
  agentName: "Psychology" | "Supervisor" | "Tutor";
  triggerReason: string;
  interventionContent: string;
  userReaction: "UNREAD" | "READ";
  createdAt: string;       // ISO DateTime
}
```

### AiAskDTO（答疑请求）

```typescript
interface AiAskDTO {
  question: string;        // 必填
  subject?: string;        // 可选，限定学科
}
```

### KnowledgePoint（知识点）

```typescript
interface KnowledgePoint {
  id: number;
  subject: string;         // 学科
  chapter: string;         // 章节
  title: string;           // 知识点标题
  content: string;         // 详细内容
  keywords: string;        // 关键词（逗号分隔）
  importance: "HIGH" | "MEDIUM" | "LOW";
  createdAt: string;       // ISO DateTime
}
```

---

## 四、前端页面建议

### 页面 1：AI 学习任务（嵌入打卡页或独立页）

- **接口**：`GET /tasks` + `POST /tasks/{id}/complete`
- **交互**：
  - 展示今日 3 条任务卡片，按 `importance` 排序（HIGH > MEDIUM > LOW）
  - 每条任务显示：内容、重要度标签、Agent Tips、完成按钮
  - 点击完成按钮 → 调用 complete 接口 → 刷新列表
  - 若今日无任务（未打卡），提示"请先打卡，AI 将为你规划今日任务"

### 页面 2：AI 答疑（对话式问答页）

- **接口**：`POST /ask`
- **交互**：
  - 顶部学科筛选下拉框（可选）
  - 对话式 UI：用户输入问题 → 显示"思考中..." → 展示回答
  - 回答中的"📚 考点出处"部分建议用不同样式高亮

### 页面 3：AI 消息中心（通知列表页）

- **接口**：`GET /interventions` + `PUT /interventions/{id}/read`
- **交互**：
  - 按时间倒序展示消息列表
  - 区分消息来源图标/颜色：Psychology（💚 绿色/暖心）/ Supervisor（🔴 红色/警示）
  - 点击消息展开详情，同时调用 read 接口标记已读
  - 支持"全部标为已读"

### 页面 4：AI 周报（报告展示页）

- **接口**：`GET /report`
- **交互**：
  - 将返回的 Markdown 渲染为富文本（可用 `markdown-it` / `marked` 等库）
  - 展示在卡片容器中，支持分享/截图

---

## 五、Agent 自动触发说明（前端无需调用，仅供理解）

以下行为由后端自动执行，前端只需读取结果：

| 事件 | 触发的 Agent | 产生的数据 |
|------|-------------|-----------|
| 用户打卡 | Planner + Psychology | `ai_daily_task`（今日任务）+ `ai_intervention_log`（心理寄语，有感言时） |
| 完成 AI 任务 | TaskCompletedListener | 更新 `user_ai_profile.cognitive_profile`（累计完成任务数） |
| 日记创建 | Psychology（DiaryPsychologyListener） | `ai_intervention_log`（日记情感分析寄语） |
| 每天 21:00 | Supervisor | `ai_intervention_log`（催学警示，完成率 < 50% 时） |
| 每周日 20:00 | Review | `ai_intervention_log`（周报，自动生成，`agentName="Review"`） |

前端在以下时机应主动刷新数据：
- 打卡成功后 → 刷新 `/tasks`（可能有新任务）+ `/interventions`（可能有心理寄语）
- 完成任务后 → 刷新 `/tasks`（状态变更）
- 进入消息中心时 → 刷新 `/interventions`
- 进入周报页时 → 调用 `/report`

---

## 六、LLM 调用健壮性（后端实现细节，前端无需关心）

后端 `AiAgentServiceImpl` 已实现重试机制：
- **最大重试**：3 次
- **退避策略**：指数退避（1s → 2s → 4s）
- **重试条件**：仅对网络异常（`RestClientException`）重试，解析失败不重试
- **降级处理**：重试耗尽后返回带 `【AI 调用失败】` 前缀的 fallback 文本

前端处理建议：
- 检查返回的 `answer` / `interventionContent` 是否以 `【AI` 开头，若是则为降级结果，可展示友好提示
- 答疑接口 (`/ask`) 调用时间可能较长（含重试 + Tool Calling），建议前端展示 loading 状态，超时设为 60s

---

## 七、答疑 Agent 技术说明

### Tool Calling（工具调用）

答疑 Agent 支持 DeepSeek 的 Tool Calling 能力：
- Agent 可自主决定是否调用 `search_knowledge` 工具检索知识库
- 支持多轮工具调用（最多 3 轮）
- 若 Tool Calling 失败，自动降级为 RAG 模式（先检索后生成）

**对前端的影响**：无额外操作，`POST /api/ai/ask` 接口不变，但响应时间可能更长（含工具调用轮次）。

### 对话历史（多轮记忆）

答疑 Agent 支持多轮对话记忆：
- 对话历史存储在 Redis，TTL 24 小时
- 保留最近 5 轮对话（10 条消息）
- 不同用户的历史完全隔离

**前端注意事项**：
- 答疑页面建议显示对话气泡（用户问题 + AI 回答），形成连续对话感
- 提供"清除对话"按钮 → 调用 `DELETE /api/ai/chat/history`
- 历史在 24 小时后自动过期，无需前端管理

---

## 八、知识库管理

### 学科列表

系统内置 9 个学科的知识点：

| 学科 | 说明 |
|------|------|
| 数据结构 | 408 核心科目 |
| 操作系统 | 408 核心科目 |
| 计算机网络 | 408 核心科目 |
| 计算机组成原理 | 408 核心科目 |
| 高等数学 | 数学科目 |
| 线性代数 | 数学科目 |
| 概率论 | 数学科目 |
| 英语 | 公共科目 |
| 政治 | 公共科目 |

### 管理建议

- 知识点可通过 `POST /api/ai/knowledge` 批量导入
- 建议为每个知识点填写 `keywords` 字段以提高检索命中率

---

## 九、新增接口（Phase 2/3）

### 9.1 社区首页 AI 摘要

```
GET /api/ai/summary
```

**说明**：为社区首页的 AI 摘要横幅提供数据，包含今日任务进度、未读消息数、连续打卡天数。

**响应 `data`**：`AiSummaryVO`

```json
{
  "code": 200,
  "data": {
    "totalTasks": 3,
    "completedTasks": 1,
    "unreadCount": 2,
    "streakDays": 5,
    "todayTip": "今日还有 2 项任务未完成，加油！"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalTasks` | int | 今日任务总数 |
| `completedTasks` | int | 今日已完成任务数 |
| `unreadCount` | int | 未读 AI 消息数 |
| `streakDays` | int | 连续打卡天数 |
| `todayTip` | String | 今日学习建议（可为空） |

---

### 9.2 历史周报列表

```
GET /api/ai/report/history?limit=4
```

**说明**：获取用户的历史周报列表，支持分页。

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `limit` | int | 否 | 返回数量，默认 4，最大 12 |

**响应 `data`**：`ReportHistoryVO[]`

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "weekStart": "2026-06-02",
      "weekEnd": "2026-06-08",
      "markdown": "# 本周学习报告\n\n..."
    },
    {
      "id": 2,
      "weekStart": "2026-05-26",
      "weekEnd": "2026-06-01",
      "markdown": "# 本周学习报告\n\n..."
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 周报 ID |
| `weekStart` | String (Date) | 周报周期起始日（周一） |
| `weekEnd` | String (Date) | 周报周期结束日（周日） |
| `markdown` | String | 周报 Markdown 内容 |

---

### 9.3 行为事件上报

```
POST /api/ai/events
```

**说明**：上报用户行为事件，供 Agent 感知用户兴趣和薄弱点。

**请求体**：

```json
{
  "eventType": "VIEW_POST",
  "eventData": {
    "postId": 123,
    "boardId": 2,
    "duration": 120
  }
}
```

**eventType 取值**：

| 事件类型 | 触发时机 | eventData 字段 |
|---------|---------|---------------|
| `VIEW_POST` | 用户浏览帖子详情页 | `postId`, `boardId`, `duration`（停留秒数） |
| `COLLECT_POST` | 用户收藏帖子 | `postId`, `boardId` |
| `SEARCH` | 用户搜索 | `keyword`, `resultCount` |
| `LIKE_POST` | 用户点赞 | `postId` |

**响应**：`{ "code": 200 }`

**前端埋点示例**：

```javascript
// 帖子详情页挂载时
onMounted(() => {
  request('/api/ai/events', {
    method: 'POST',
    body: JSON.stringify({
      eventType: 'VIEW_POST',
      eventData: { postId: route.params.id }
    })
  })
})

// 收藏成功后
const handleCollect = async (postId) => {
  // ... 原有收藏逻辑 ...
  request('/api/ai/events', {
    method: 'POST',
    body: JSON.stringify({
      eventType: 'COLLECT_POST',
      eventData: { postId }
    })
  })
}
```

---

### 9.4 智能推荐

```
GET /api/ai/recommendations
```

**说明**：基于用户行为画像推荐相关知识点。优先推荐与用户搜索/浏览相关的知识点，不足时补充高频考点。

**响应 `data`**：`RecommendationVO`

```json
{
  "code": 200,
  "data": {
    "knowledgePoints": [
      {
        "id": 10,
        "title": "红黑树的旋转操作",
        "subject": "数据结构",
        "reason": "您最近频繁查阅「红黑树」相关内容"
      },
      {
        "id": 5,
        "title": "进程与线程的区别",
        "subject": "操作系统",
        "reason": "高频考点，建议重点复习"
      }
    ]
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `knowledgePoints` | Array | 推荐知识点列表 |
| `knowledgePoints[].id` | Long | 知识点 ID |
| `knowledgePoints[].title` | String | 知识点标题 |
| `knowledgePoints[].subject` | String | 学科 |
| `knowledgePoints[].reason` | String | 推荐理由 |

---

## 十、新增 Agent

### 行为分析 Agent (Behavior)

每天 22:00 自动执行，分析用户当日行为事件：

1. 统计浏览、收藏、搜索、点赞数量
2. 提取搜索关键词，更新用户画像的 `interestKeywords`
3. 如果浏览量 ≥ 5，生成学习建议写入 `ai_intervention_log`（`agentName="Behavior"`）

**前端展示**：在 AI 消息中心展示行为分析 Agent 生成的建议，图标可用 📊 蓝色。

---

## 十一、数据库变更

### 新增表

| 表名 | 说明 |
|------|------|
| `ai_report` | 周报持久化存储（`user_id` + `week_start` 唯一） |
| `ai_user_event` | 用户行为事件采集 |

### 扩展字段

`user_ai_profile.cognitive_profile` JSON 结构扩展：

```json
{
  "completedTasks": 15,
  "browsePattern": {
    "todayViews": 12,
    "todayCollects": 3,
    "todaySearches": 5,
    "todayLikes": 8
  },
  "interestKeywords": ["B树", "红黑树", "TCP"],
  "lastActiveAt": "2026-06-04T22:00:00"
}
```
- `importance` 为 `HIGH` 的知识点在搜索结果中优先展示
