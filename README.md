# Kaoyan 考研论坛后端

基于 **Spring Boot 3 + MyBatis-Plus + Sa-Token + MySQL + Redis** 的考研学习社区后端服务，提供帖子社区、互动评论、私信、每日打卡、积分排行、举报审核等完整论坛功能，并集成 **AI 多智能体学习伴侣**、**AI 模拟面试**、**智能错题本**、**AI 择校引擎** 等智能化特性。

---

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 语言 / 运行时 | Java 17 |
| 框架 | Spring Boot 3.2.5 |
| ORM | MyBatis-Plus 3.5.7 |
| 分页 | PageHelper 6.1.1 |
| 鉴权 | Sa-Token 1.45.0（JWT 模式） |
| 数据库 | MySQL 9.x（utf8mb4） |
| 缓存 | Redis（Lettuce 连接池） |
| 接口文档 | SpringDoc OpenAPI 2.6.0（Swagger UI） |
| 实时通信 | WebSocket |
| AI 对话 | mimo API（mimo-v2.5，OpenAI 兼容协议） |
| AI 面试 | 通义千问（DashScope API，qwen-plus） |
| OCR | PaddleOCR（Python 脚本） |
| PDF 导出 | OpenPDF / LibrePDF 2.0.2 |
| Markdown | flexmark 0.64.8 |
| XSS 防护 | Jsoup 1.18.1 |
| 验证码 | Hutool Captcha 5.8.36 |
| 工具 | Lombok 1.18.36 |
| 构建 | Maven (mvnw) |

---

## 项目结构

```
src/main/java/com/zzu/kaoyan
├── KaoyanApplication.java              # 启动入口
├── config/                             # 全局配置：CORS、Jackson、Redis、Sa-Token、WebSocket、调度
├── common/                             # 通用层
│   ├── annotation/                     #   @RateLimit、@SkipXssClean
│   ├── aspect/                         #   限流切面（Redis ZSET 滑动窗口 + Lua）
│   ├── entity/                         #   User 实体
│   ├── exception/                      #   BusinessException、全局异常处理
│   ├── handler/                        #   MyBatis-Plus 自动填充、XSS 反序列化器
│   ├── result/                         #   统一 Result、ResultCode
│   ├── util/                           #   Markdown 渲染、敏感词过滤、XSS 工具
│   └── websocket/                      #   WebSocket 聊天处理器
└── module/                             # 业务模块（按领域拆分）
    ├── auth/                           # 认证：注册 / 登录 / 登出
    ├── user/                           # 用户中心：个人信息、管理员操作
    ├── post/                           # 帖子与板块：发帖、详情、分页、板块管理、热门帖调度
    ├── interact/                       # 互动：评论、点赞、收藏、举报、用户统计
    ├── activity/                       # 活动：每日打卡、积分排行榜
    ├── message/                        # 私信 + 全局搜索
    ├── admin/                          # 管理后台仪表盘
    ├── upload/                         # 文件上传（图片/视频，最大 100MB）
    ├── chat/                           # 小组群聊（WebSocket 实时通信）
    ├── certification/                  # 上岸认证（录取通知书 + 学信网截图审核）
    ├── experience/                     # 结构化经验贴（独立于普通帖子）
    ├── interview/                      # AI 模拟面试（英语/专业/综合，通义千问驱动）
    ├── mistake/                        # 智能错题本（OCR + 艾宾浩斯复习 + PDF 导出）
    ├── schoolselect/                   # AI 智能择校引擎（院校/专业库 + 匹配推荐）
    └── ai/                             # AI 多智能体学习伴侣（6 Agent + 事件驱动）
```

数据库脚本位于 `sql/` 和 `src/main/resources/sql/` 目录，含建表语句与种子数据。

---

## 功能模块

### 1. 认证模块（`/api/auth`）
- 用户注册（密码 BCrypt 加密入库）
- 账号 / 邮箱登录，返回 Sa-Token JWT
- 注销登录

### 2. 用户中心（`/api/users`）
- 获取当前用户信息（`GET /me`）
- 修改个人资料（`PUT /me`）
- 查看其他用户公开主页（`GET /{userId}`）
- 管理员封禁用户、修改角色（USER / MODERATOR / ADMIN）

### 3. 板块与帖子（`/api/boards`、`/api/posts`）
- 板块列表 / 详情 / 新增
- 发布帖子（含富文本、标签、附件 URL）
- 按板块分页帖子、全局分页帖子
- 帖子详情（含浏览量、点赞数、评论数、当前用户点赞 / 收藏状态）
- 查询指定用户发帖总数与发帖列表
- 热门帖定时调度（`HotPostScheduler`）

### 4. 互动模块（`/api/interact`）
- **评论 `/comment`**：发布顶层评论与楼中楼回复，支持树形结构与平铺结构两种返回
- **点赞 `/post/like`**：切换点赞 / 取消点赞，查询当前用户点赞状态
- **收藏 `/collect`**：切换收藏，查询收藏状态，分页查询「我的收藏」
- **举报 `/report`**：用户对帖子 / 评论 / 用户发起举报；管理员分页查询、筛选举报记录
- **用户统计 `/stats`**：获取用户发帖总数、累计获赞数（基于 `sys_user_stats`，由数据库触发器自动维护）

### 5. 活动模块（`/api/activity`）
- **每日打卡 `/checkin`**：记录学习时长 + 笔记，每天每人仅可打卡一次（唯一索引保证）
- 连续打卡天数计算 + 阶梯积分奖励（3/7/30 天有额外加成）
- 个人打卡统计、今日是否打卡查询
- **积分排行榜 `/rank/total`**：按用户总积分排序

### 6. 私信与搜索（`/api/v1`）
- 私信发送 / 会话查询 / 标记已读 / 未读数 / 最近联系人列表
- 全局搜索：按关键词同时模糊匹配帖子标题 / 内容、用户用户名

### 7. 文件上传（`/api/upload`）
- 支持图片、视频等文件上传（最大 100MB）
- 文件存储于 `./uploads` 目录

### 8. 小组群聊（`/api/chat`）
- 创建 / 加入 / 退出群组
- 群组消息收发（WebSocket 实时通信）
- 群成员管理、历史消息查询

### 9. 上岸认证（`/api/certification`）
- 用户提交录取通知书 + 学信网截图进行上岸认证
- 管理员审核认证申请
- 认证用户获得「已上岸」标识（`sys_user.is_verified`）

### 10. 结构化经验贴（`/api/experience`）
- 发布结构化考研经验贴（独立于普通论坛帖子）
- 经验贴点赞 / 收藏
- 经验广场分页浏览

### 11. AI 模拟面试（`/api/interview`）
- 三种面试类型：英语面试、专业课面试、综合面试
- 基于通义千问（qwen-plus）驱动 AI 面试官
- 支持语音录制（前端桩实现）
- 面试完成后自动生成评估报告（雷达图评分、优缺点分析）

### 12. 智能错题本（`/api/mistake`）
- **OCR 识别**：拍照上传题目图片，PaddleOCR 自动提取文字
- **知识点管理**：树形知识点体系，关联错题
- **艾宾浩斯复习**：7 阶段间隔重复（1/2/4/7/15/30 天），自动排程每日复习计划
- **PDF 导出**：错题本导出为 PDF 文档
- **复习提醒**：定时任务检查待复习题目，生成通知

### 13. AI 智能择校（`/api/schoolselect`）
- 院校 / 专业数据库（含历年录取数据）
- 根据用户条件（地区、分数、专业偏好）智能匹配推荐
- 推荐历史记录

### 14. AI 多智能体学习伴侣（`/api/ai`）
- **6 个 AI Agent**：

| Agent | 职能 | 触发时机 |
| --- | --- | --- |
| Planner（规划师） | 生成每日 3 项个性化学习任务 | 用户打卡时 |
| Tutor（答疑导师） | RAG + Tool Calling 考研科目问答 | 用户提问（普通 / SSE 流式） |
| Psychology（心理疏导） | 情绪分析 + 鼓励话语 | 打卡附带笔记 / 日记创建 |
| Supervisor（督学） | 监控低完成度用户，生成提醒 | 每日 21:00 + 手动触发 |
| Review（复盘） | 聚合 7 天数据生成 Markdown 周报 | 用户请求 + 周日 20:00 自动 |
| Behavior（行为分析） | 分析浏览/搜索/收藏行为，更新用户画像 | 每日 22:00 |

- **技术特性**：
  - 基于 mimo-v2.5 大模型（OpenAI 兼容协议）
  - SSE 流式输出（JSON 分块）
  - Redis 多轮对话记忆（5 轮，24h TTL）
  - Spring Event 事件驱动架构
  - 用户 AI 画像持久化

### 15. 管理后台（`/api/admin`）
- 管理员仪表盘：用户管理、举报审核、内容管理

---

## 安全与基础设施

| 特性 | 实现方式 |
| --- | --- |
| 接口限流 | `@RateLimit` 注解 + AOP + Redis ZSET 滑动窗口 + Lua 原子脚本 |
| XSS 防护 | Jsoup 全局 Jackson 反序列化器，`@SkipXssClean` 跳过密码等字段 |
| 敏感词过滤 | `SensitiveWordUtil` 内置词库匹配 |
| 跨域 | `CorsConfig` 全局放开（开发环境） |
| 鉴权 | Sa-Token 拦截 `/api/**`，放行登录/注册/板块列表/Swagger |
| 逻辑删除 | 统一 `is_deleted` 字段 |
| 自动填充 | `created_at` / `updated_at` 由 MyBatis-Plus `MetaObjectHandler` 处理 |

---

## 数据库设计

核心表（详见 `sql/` 和 `src/main/resources/sql/`）：

| 表名 | 说明 |
| --- | --- |
| `sys_user` | 用户主表（含角色、积分、目标专业、上岸认证标识） |
| `sys_user_stats` | 用户统计冗余表（发帖数 / 获赞数，由触发器维护） |
| `forum_board` | 论坛板块（如 408、考研数学、英语、政治） |
| `forum_post` | 帖子主表（富文本、标签 JSON、附件 JSON） |
| `forum_comment` | 评论表（支持楼中楼，`reply_to_id` 自引用） |
| `forum_post_like` | 帖子点赞关联表（`(post_id, user_id)` 唯一） |
| `forum_post_collect` | 帖子收藏关联表（`(post_id, user_id)` 唯一） |
| `forum_report` | 举报与审核记录 |
| `interaction_check_in` | 每日打卡记录（`(user_id, created_date)` 唯一） |
| `interaction_points_log` | 积分变动流水 |
| `interaction_user_study` | 用户连续打卡 / 累计天数 |
| `message` | 私信表 |
| `resource_file` | 学习资料（板块附件） |
| `chat_group` / `group_member` / `group_message` | 小组群聊 |
| `user_verification` | 上岸认证记录 |
| `experience_post` | 结构化经验贴 |
| `interview_session` / `interview_record` / `interview_report` | AI 模拟面试 |
| `mistake_note` / `knowledge_point` / `mistake_review_log` / `mistake_daily_plan` | 智能错题本 |
| `school_info` / `school_major` / `admission_record` / `recommendation_history` | AI 择校引擎 |
| `user_ai_profile` / `ai_daily_task` / `ai_intervention_log` / `ai_report` / `ai_user_event` | AI 学习伴侣 |

---

## 接口路径速览

| 模块 | 前缀 |
| --- | --- |
| 认证 | `/api/auth` |
| 用户 | `/api/users` |
| 板块 | `/api/boards` |
| 帖子 | `/api/posts` |
| 评论 | `/api/interact/comment` |
| 点赞 | `/api/interact/post/like` |
| 收藏 | `/api/interact/collect` |
| 举报 | `/api/interact/report` |
| 用户统计 | `/api/interact/stats` |
| 打卡 | `/api/activity/checkin` |
| 排行榜 | `/api/activity/rank` |
| 私信 | `/api/v1/messages` |
| 搜索 | `/api/v1/search` |
| 文件上传 | `/api/upload` |
| 群聊 | `/api/chat` |
| 上岸认证 | `/api/certification` |
| 经验贴 | `/api/experience` |
| AI 面试 | `/api/interview` |
| 错题本 | `/api/mistake` |
| 智能择校 | `/api/schoolselect` |
| AI 学伴 | `/api/ai` |
| 管理后台 | `/api/admin` |

---

## 启动与运行

### 环境要求
- JDK 17+
- Maven 3.8+（或直接使用项目自带 `./mvnw`）
- MySQL 8.0+（项目脚本基于 9.x，兼容 8.0）
- Redis 6.0+（用于缓存、限流、AI 对话记忆）
- Python 3.8+（可选，仅 OCR 功能需要：`pip install paddleocr`）

### 步骤

1. **初始化数据库**

   ```sql
   CREATE DATABASE kaoyan_forum DEFAULT CHARACTER SET utf8mb4;
   ```

   导入主脚本及各模块脚本：

   ```bash
   mysql -u root -p kaoyan_forum < kaoyan_forum.sql
   mysql -u root -p kaoyan_forum < sql/mistake_notebook.sql
   mysql -u root -p kaoyan_forum < src/main/resources/sql/ai_tables.sql
   mysql -u root -p kaoyan_forum < src/main/resources/sql/interview_schema.sql
   mysql -u root -p kaoyan_forum < src/main/resources/sql/school_select_seed.sql
   mysql -u root -p kaoyan_forum < src/main/resources/sql/D_mysql.sql
   ```

2. **配置环境**

   编辑 `src/main/resources/application.properties`：

   ```properties
   # 数据库
   spring.datasource.url=jdbc:mysql://localhost:3306/kaoyan_forum?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
   spring.datasource.username=<your-user>
   spring.datasource.password=<your-password>

   # Redis
   spring.data.redis.host=localhost
   spring.data.redis.port=6379

   # AI（可选）
   API_KEY=<your-mimo-api-key>
   ```

3. **启动应用**

   ```bash
   ./mvnw spring-boot:run
   ```

   服务默认监听 `http://localhost:8080`。

4. **访问接口文档**

   - Swagger UI: <http://localhost:8080/swagger-ui.html>
   - OpenAPI JSON: <http://localhost:8080/v3/api-docs>

---

## 项目文档

| 文档 | 说明 |
| --- | --- |
| [AI-MODULE-SUMMARY.md](AI-MODULE-SUMMARY.md) | AI 多智能体模块技术设计文档 |
| [功能拓展总结.md](功能拓展总结.md) | 安全与基础设施增强详细说明 |
| [docs/ai-module-api.md](docs/ai-module-api.md) | AI 模块前端对接接口文档 |
| [docs/apifox-mistake-api.json](docs/apifox-mistake-api.json) | 错题本模块 Apifox API 定义 |

---

## 测试账号（脚本内置）

| 用户 | 角色 | 邮箱 |
| --- | --- | --- |
| Admin管理员 | ADMIN | admin@example.com |
| 考研高数版主 | MODERATOR | mod@example.com |
| 408上岸人 / 英语困难户 / 政治背书狂 | USER | user1~3@example.com |

种子用户密码均为 BCrypt 加密占位值，本地测试时建议通过 `/api/auth/register` 重新注册账号。
