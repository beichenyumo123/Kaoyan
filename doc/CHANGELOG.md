# 版本更新说明

> 版本：v2.3  
> 日期：2026-06-16  
> 主题：Agent 系统改善 — 详情 Markdown + 学伴团命名统一 + 习题预生成

---

## 一、API 字段扩展

### 1.1 任务详情 Markdown

**问题**：`GET /api/ai/tasks` 只返回 `taskContent` + `agentTips` 纯文本，前端无法展示富文本详情。

**方案**：`ai_daily_task` 新增 3 个字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `detailMarkdown` | TEXT | 任务详情，Markdown 格式。前端统一 `renderMarkdown()` 渲染 |
| `linkTarget` | VARCHAR(256) | 跳转路由，如 `/ai/knowledge?keyword=极限` |
| `linkLabel` | VARCHAR(64) | 按钮文案，如「去刷相关习题 →」 |

**效果**：前端点击「查看详情 →」→ 弹 Modal 渲染 Markdown 详情。

### 1.2 干预详情 Markdown

**问题**：`GET /api/ai/interventions` 只有 `interventionContent` 纯文本。

**方案**：`ai_intervention_log` 同样新增 `detailMarkdown` / `linkTarget` / `linkLabel` 3 个字段。

**效果**：不同 Agent 生成不同风格的 Markdown — 行为分析师写习题列表，心理树洞写情绪建议，铁面教官写补漏计划，前端只需 `renderMarkdown()` 统一渲染。

### 1.3 向后兼容

新字段为 `null` 时前端不展示对应按钮，完全向后兼容。

---

## 二、Agent 名称统一（学伴团体系）

| Agent | 旧名称 | 新名称 | 角色 |
|-------|--------|--------|------|
| Planner | *(无)* | **规划伴侣** | 每日任务规划 |
| Tutor | *(无)* | **答疑导师** | 知识问答 |
| Behavior | `Behavior` | **行为分析师** | 浏览行为分析 + 习题推荐 |
| Psychology | `Psychology` | **心理树洞** | 情绪分析 + 放松建议 |
| Supervisor | `Supervisor` | **铁面教官** | 完成率预警 + 补漏计划 |
| Review | `Review` | **透视专家** | 周报生成 + 趋势分析 |

6 个 Agent 统一为中文「学伴团」命名风格。

---

## 三、各 Agent DetailMarkdown 生成策略

### 规划伴侣 (PlannerAgent)
LLM prompt 要求输出 `detailMarkdown`（复习知识点列表、推荐习题、建议用时）+ `linkTarget`（知识库搜索链接）+ `linkLabel`。

### 行为分析师 (BehaviorAnalysisAgent)
LLM 预生成习题：注入 `AiAgentService`，根据浏览关键词调用 LLM 生成 3 道完整习题（**题目** + **解析** + **答案**），直接嵌入 `detailMarkdown`。答案区用 `<details>` 标签折叠。`linkTarget`/`linkLabel` 降级为补充入口。

### 心理树洞 (PsychologyAgent)
LLM prompt 改为 JSON 输出 `{"message":"...","detailMarkdown":"## 情绪分析\n..."}`，包含情绪标签、趋势分析和放松建议。解析失败自动回退纯文本。

### 铁面教官 (SupervisorAgent)
LLM prompt 改为 JSON 输出，`detailMarkdown` 包含完成率表格和 3 条补漏建议。`linkTarget` 指向 `/ai/tasks`。

### 透视专家 (ReviewAgent)
干预内容改为摘要提示（"本周学情报告已生成"），`detailMarkdown` 取周报前 500 字，`linkLabel` 为「查看完整周报 →」。

### 3.1 行为分析师习题预生成 🔥

**问题**：v2.3 第一版的行为分析师推荐习题是 `/ai/ask?question=...` 链接。用户点击后等于让 AI 重新回答——行为分析师做了行为分析，却把生成习题内容这份活甩给了答疑导师，体验差。

**修复**：
1. 注入 `AiAgentService`，调用 LLM 预先生成习题内容（**题目** + **解析** + **答案**）
2. LLM 响应直接嵌入 `detailMarkdown`，用户在弹窗里读完，不需要跳转
3. 答案区使用 `<details>` 标签折叠（markdown-it `html:true` 原生渲染），点击「查看答案」展开
4. `linkTarget`/`linkLabel` 改为补充性质：「去 AI 答疑继续练习 →」

**效果**：前端弹窗内完整展示 3 道题（题目 + 推导过程 + 可折叠答案），用户无需离开弹窗。

---

## 四、文件变更汇总

### 新增（1 个）
| 文件 | 说明 |
|------|------|
| `scripts/migration/V6__agent_detail_markdown.sql` | DDL 加列 |

### 修改（14 个）
| 文件 | 改动 |
|------|------|
| `entity/AiDailyTask.java` | +3 字段 |
| `entity/AiInterventionLog.java` | +3 字段 |
| `vo/AiTaskVO.java` | +3 字段 |
| `vo/InterventionVO.java` | +3 字段 |
| `util/JsonArrayExtractor.java` | Task 类 +3 字段，tryParse 提取 |
| `agent/PlannerAgent.java` | LLM prompt 输出 detailMarkdown/linkTarget/linkLabel |
| `agent/BehaviorAnalysisAgent.java` | LLM 预生成习题 + `<details>` 折叠答案；改名「行为分析师」 |
| `agent/PsychologyAgent.java` | JSON prompt + fallback；改名「心理树洞」 |
| `agent/SupervisorAgent.java` | JSON prompt + fallback；改名「铁面教官」 |
| `agent/ReviewAgent.java` | 干预加周报摘要；改名「透视专家」 |
| `config/TestDataInitializer.java` | 种子数据补新字段 + agentTips Markdown 详情 |
| `resources/kaoyan_forum_v2.sql` | 重新导出（1605 行，含 V6 新列） |
| `doc/CHANGELOG.md` | 本文档 |

### API 契约变更
- `GET /api/ai/tasks` → 新增 `detailMarkdown`、`linkTarget`、`linkLabel`（向后兼容）
- `GET /api/ai/interventions` → 新增 `detailMarkdown`、`linkTarget`、`linkLabel`，`agentName` 改为中文（**破坏性变更** — 前端需适配新名称）

---

## 五、升级步骤

```sql
source scripts/migration/V6__agent_detail_markdown.sql;
```

重启后端即可。如有前端需同步更新 API 类型定义（`src/types/ai.ts`）和 agentName 映射。

---

> 版本：v2.2  
> 日期：2026-06-15  
> 主题：Agent Memory 系统 + 知识库体系重构 + 测试基础设施 + 上岸经验联动 + 学科过滤

---

## 一、Agent Memory 记忆系统（新增）

### 1.3 面试报告接入

**问题**：用户完成 AI 模拟面试后，面试报告（五维评分/薄弱项/改进建议）没有被 Agent 利用。

**方案**：MemoryService 接入 `interview_report` 表，查询最近一次已完成的面试报告，提取综合评分、薄弱项分析和改进建议，拼入「学员档案」上下文。

**修改**：`MemoryServiceImpl.java` — 新增 `buildInterviewSection()`，注入 `InterviewReportMapper` + `InterviewSessionMapper`。

### 1.4 上岸经验贴联动

**问题**：经验广场（`experience_post`）有大量高质量 UGC 内容（备考心得/书单/时间线/分数），但 Agent 完全不知道这些数据的存在。

**方案**：
1. `sys_user` 加 `target_school` 列，`User` 实体 + `UserVO` + `UserUpdateDTO` 同步新增
2. `MemoryServiceImpl` 新增 `buildExperienceSection()` — 匹配目标院校相同的经验贴，筛选收藏量 ≥ 3 的高质量帖子，取 TOP-3 提取备考心得注入 LLM 上下文

**效果**：用户 targetSchool="清华大学" → Agent 引用清华上岸学长的备考经验。

**新增**：`scripts/migration/V4__user_target_school.sql`

**修改**：`User.java`、`UserVO.java`、`UserUpdateDTO.java`、`UserServiceImpl.java`、`MemoryServiceImpl.java`

### 1.5 Memory 学科过滤 + 相关性权重

**问题**：用户问数学「二重积分」，Agent 却强行关联数据结构「B树分裂」。因为 Memory 把所有薄弱知识点无差别塞给 LLM，LLM 觉得"都告诉我就都得用"。

**修复（三层防护）**：
1. **语义记忆加学科标签** — `ai_memory_embedding` 新增 `subject` 列，存 embedding 时记录学科
2. **同科目加权** — `EmbeddingService.search()` 同科目记忆 +0.15 余弦相似度，跨科目 -0.10
3. **Prompt 指令** — TutorAgent system prompt 末尾追加「只关联当前学科相关的薄弱知识点，不要强行跨学科建立联系」

**新增**：`scripts/migration/V5__memory_subject.sql`

**修改**：`AiMemoryEmbedding.java`、`EmbeddingService.java`、`MemoryService.java`、`MemoryServiceImpl.java`、`TutorAgent.java`、`AiAgentController.java`、`MistakeNoteServiceImpl.java`

### 1.1 MemoryService — 统一学员档案（Phase 1）

**问题**：6 个 AI Agent 各自孤立运行，只知道单次请求的数据，不感知用户整体学习状态。

**方案**：新建 `MemoryService`，聚合 5 张已有表的数据，构建统一「学员档案」上下文，注入每个 Agent 的 LLM 提示词。

```
MemoryService.buildContext(userId)
  ├─ user_ai_profile → 认知画像 + 心理状态
  ├─ mistake_note → 薄弱知识点 TOP5
  ├─ ai_daily_task → 今日任务进度
  ├─ interaction_user_study → 连续打卡天数
  └─ ai_chat_session → 最近 3 个对话标题
```

**效果**：
- TutorAgent — 答疑时知道用户薄弱点、学习阶段、心理状态
- PlannerAgent — 规划任务时参考薄弱知识点和最近对话
- PsychologyAgent — 心理关怀结合学习背景，更有针对性
- SupervisorAgent — 催学警示能点名具体薄弱学科
- ReviewAgent — 周报关联薄弱知识点+长期趋势

**新增文件**：
- `module/ai/service/MemoryService.java`
- `module/ai/service/impl/MemoryServiceImpl.java`

**修改文件**：
- `module/ai/agent/TutorAgent.java`
- `module/ai/agent/PlannerAgent.java`
- `module/ai/agent/PsychologyAgent.java`
- `module/ai/agent/SupervisorAgent.java`
- `module/ai/agent/ReviewAgent.java`

### 1.2 Embedding 语义记忆（Phase 2）

**问题**：考研周期长达 6-12 个月，用户会问几百个问题。当前 AI 完全不记得历史对话，没法做「这个问题你 3 个月前问过类似的」这种关联。

**方案**：`EmbeddingService` — 调 DashScope text-embedding API 将问答转为 1536 维向量，存入 MySQL `ai_memory_embedding` 表。每次新提问时，用余弦相似度检索 TOP-3 相似历史，拼入 LLM 上下文。

```
用户提问 → embed(question) → 1536-dim vector
         → SELECT user vectors → cosine similarity TOP-3
         → system prompt: "你可能相关的历史记录：..."
         → AI 回答 → async save(question+answer embedding)
```

**新增文件**：
- `scripts/migration/V3__memory_embedding.sql` — DDL
- `module/ai/entity/AiMemoryEmbedding.java`
- `module/ai/mapper/AiMemoryEmbeddingMapper.java`
- `module/ai/service/EmbeddingService.java`

**修改文件**：
- `module/ai/service/MemoryService.java` — 新增 `enrichWithSemanticMemory()`
- `module/ai/service/impl/MemoryServiceImpl.java` — 注入 EmbeddingService
- `module/ai/agent/TutorAgent.java` — 答疑时调用语义检索
- `module/ai/controller/AiAgentController.java` — 答疑完成后异步存 embedding
- `module/mistake/service/impl/MistakeNoteServiceImpl.java` — 收藏错题时异步存 embedding

**部署**：
```sql
mysql -u root -p kaoyan_forum < scripts/migration/V3__memory_embedding.sql
```

---

## 二、知识库体系重构

### 2.1 按考研大纲系统补全

**问题**：原 AI 知识库（`ai_knowledge_point`）只有 17 条，每科仅 1-2 个知识点，RAG 检索基本无效果。

**方案**：按数学一、408 计算机、英语一、政治考研大纲，系统补全至 74 条。

| 学科 | 原有 | 新增 | 合计 | 覆盖 |
|------|------|------|------|------|
| 数据结构 | 4 | 6 | **10** | 线性表/树/图/查找/排序/哈希 |
| 计算机组成原理 | 2 | 6 | **8** | 浮点/运算/存储/流水线/总线/中断 |
| 操作系统 | 3 | 6 | **9** | 调度/PV/置换/文件/磁盘/线程 |
| 计算机网络 | 2 | 6 | **8** | IP/路由/TCP/以太网/DNS/编码 |
| 高等数学 | 2 | 11 | **13** | 极限→导数→积分→微分方程→多元→级数 |
| 线性代数 | 1 | 6 | **7** | 行列式→矩阵→向量→方程组→特征值→二次型 |
| 概率论 | 1 | 5 | **6** | 条件概率/多维/数字特征/大数定律/参数估计 |
| 英语 | 1 | 4 | **5** | 完形/长难句/翻译/写作 |
| 政治 | 1 | 7 | **8** | 认识论/唯物史观/毛思想/邓论/史纲/思修/当代 |
| **合计** | **17** | **57** | **74** | |

每条含：Markdown 格式的详细内容 + 关键词标签 + HIGH/MEDIUM 重要度。

**新增文件**：
- `scripts/migration/V2__expand_knowledge_points.sql`

### 2.2 知识库去重修复

**问题**：`kaoyan_forum_v2.sql` 导入后 `ai_knowledge_point` 表有 34 条数据——17 条被重复插入了一次。`GET /api/ai/knowledge?keyword=进程` 返回两条完全相同的"进程与线程的区别"。

**修复**：
- SQL dump 去重：删除 ID 18-34 的重复行，保留 17 条
- 查询层防重复：`searchByKeywords` SQL 加 `WHERE id IN (SELECT MIN(id) FROM ai_knowledge_point GROUP BY title)`

---

## 三、XSS 过滤器 Markdown 破坏修复

**问题**：Jackson 全局 `XssStringDeserializer` 对所有 String 字段执行 `Jsoup.clean()`，导致错题 Markdown 内容的 `\n` 换行符被当作 HTML 空白规范化为空格，格式全毁。

**修复**：3 个错题 DTO 的 `questionContent` 和 `answer` 字段加 `@SkipXssClean` 注解，跳过 Jsoup HTML 清洗：
- `QuickSaveDTO.java`
- `MistakeNoteCreateDTO.java`
- `MistakeNoteUpdateDTO.java`

---

## 四、测试基础设施

### 4.1 测试账号自动重置

**问题**：没有专用 VIP 测试账号，配额用完后无法方便重置，测试数据会累积污染。

**方案**：`TestDataInitializer` — `ApplicationRunner` + `@Profile("dev")`，每次启动自动：
1. 重置测试用户 `test@kaoyan.com` / `test123456`
2. 授予永久 VIP 会员
3. 清除 Redis 配额缓存
4. 预热会员缓存
5. 种子全套测试数据（覆盖 4 个前端页面、11 个接口）

**使用**：IDEA VM options 加 `-Dspring.profiles.active=dev`

### 4.2 种子数据覆盖

| 页面 | 数据 |
|------|------|
| AiDashboard | 4 条任务 + 2 条干预消息 + 打卡 7 天 + 2 份周报 |
| AiAsk | 2 个会话 × 3 条消息（含 LaTeX/Markdown） |
| AiKnowledge | 74 条按大纲补全的知识点 |
| CommunityHome | AI 摘要 + 知识点推荐 |
| 错题本 | 5 道错题（5 科）+ 3 条复习日志 + 今日计划 + 3 条通知 |
| 语义记忆 | 5 条记忆占位（待真实对话产生向量） |

**新增文件**：
- `config/TestDataInitializer.java`
- `resources/application-dev.yml`
- `doc/测试账号说明.md`

---

## 五、文件变更汇总

### 新增文件（18 个）

| 文件 | 说明 |
|------|------|
| `config/TestDataInitializer.java` | 测试账号自动重置 + 全套种子数据 |
| `resources/application-dev.yml` | 开发环境 profile |
| `doc/测试账号说明.md` | 测试账号完整文档 |
| `doc/CHANGELOG.md` | 本文档 |
| `scripts/migration/V2__expand_knowledge_points.sql` | 知识库扩充至 74 条 |
| `scripts/migration/V3__memory_embedding.sql` | 语义记忆向量表 DDL |
| `scripts/migration/V4__user_target_school.sql` | 用户表增加目标院校字段 |
| `scripts/migration/V5__memory_subject.sql` | 语义记忆增加学科标签 |
| `module/ai/service/MemoryService.java` | 记忆服务接口 |
| `module/ai/service/impl/MemoryServiceImpl.java` | 记忆聚合实现 |
| `module/ai/service/EmbeddingService.java` | DashScope embedding + 余弦搜索 |
| `module/ai/entity/AiMemoryEmbedding.java` | 语义记忆实体 |
| `module/ai/mapper/AiMemoryEmbeddingMapper.java` | 语义记忆 Mapper |

### 修改文件（18 个）

| 文件 | 改动 |
|------|------|
| `Agent/TutorAgent.java` | Memory 上下文 + 语义检索 + 学科过滤指令 |
| `Agent/PlannerAgent.java` | 统一 Memory 上下文 |
| `Agent/PsychologyAgent.java` | 新增学员档案上下文 |
| `Agent/SupervisorAgent.java` | 个性化催学警示 |
| `Agent/ReviewAgent.java` | 周报关联薄弱点 |
| `controller/AiAgentController.java` | 注入 EmbeddingService，异步存 embedding |
| `service/impl/MistakeNoteServiceImpl.java` | 收藏存 embedding |
| `service/impl/MemoryServiceImpl.java` | 面试报告 + 经验贴 + 学科过滤 |
| `common/entity/User.java` | 加 targetSchool 字段 |
| `user/dto/UserVO.java` | 加 targetSchool 字段 |
| `user/dto/UserUpdateDTO.java` | 加 targetSchool 字段 |
| `user/service/impl/UserServiceImpl.java` | 支持 targetSchool 更新 |
| `dto/QuickSaveDTO.java` | `@SkipXssClean` 防 Markdown 破坏 |
| `dto/MistakeNoteCreateDTO.java` | `@SkipXssClean` |
| `dto/MistakeNoteUpdateDTO.java` | `@SkipXssClean` |
| `mapper/KnowledgePointMapper.xml` | 查询去重 |
| `entity/AiMemoryEmbedding.java` | 加 subject 字段 |
| `resources/kaoyan_forum_v2.sql` | 知识点去重 + 更新至最新 |

### 配置变更

- `application-dev.yml` — 新增 dev profile 配置
- 无 `application.properties` 变更
- `GET /api/users/me` 新增 `targetSchool` 字段（API 增强，向后兼容）
- 其余无前端 API 契约变更

---

## 六、升级步骤

1. 执行 SQL 迁移：
   ```sql
   source scripts/migration/V2__expand_knowledge_points.sql;
   source scripts/migration/V3__memory_embedding.sql;
   source scripts/migration/V4__user_target_school.sql;
   source scripts/migration/V5__memory_subject.sql;
   ```
2. 清理数据库重复知识点（如已有）：
   ```sql
   DELETE FROM ai_knowledge_point WHERE id > 17;
   ```
3. 重启后端即可，前端无需改动
4. 开发环境启用 `-Dspring.profiles.active=dev` 获取测试账号
4. 开发环境建议启用 `-Dspring.profiles.active=dev` 以自动获取测试账号
