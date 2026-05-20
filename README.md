# Kaoyan 考研论坛后端

基于 **Spring Boot 3 + MyBatis-Plus + Sa-Token + MySQL** 的考研学习社区后端服务，提供帖子社区、互动评论、私信、每日打卡、积分排行、举报审核等完整论坛功能。

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
| 接口文档 | SpringDoc OpenAPI 2.6.0（Swagger UI） |
| 工具 | Lombok 1.18.36 |
| 构建 | Maven (mvnw) |

---

## 项目结构

```
src/main/java/com/zzu/kaoyan
├── KaoyanApplication.java           # 启动入口
├── config/                          # 全局配置：CORS、Jackson、Sa-Token 拦截器
├── common/                          # 通用：User 实体、统一 Result、全局异常、MybatisPlus 自动填充
└── module/                          # 业务模块（按领域拆分）
    ├── auth/                        # 认证模块：注册 / 登录 / 登出
    ├── user/                        # 用户中心：个人信息、管理员操作
    ├── post/                        # 帖子与板块：发帖、详情、分页、板块管理
    ├── interact/                    # 互动：评论、点赞、收藏、举报、用户统计
    ├── activity/                    # 活动：每日打卡、积分排行榜
    └── message/                     # 私信 + 全局搜索
```

数据库脚本位于 `src/main/resources/kaoyan_forum_v2.sql`，含建表语句与种子数据。

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

---

## 数据库设计

核心表（详见 `kaoyan_forum_v2.sql`）：

| 表名 | 说明 |
| --- | --- |
| `sys_user` | 用户主表（含角色、积分、目标专业） |
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

主要约定：
- 统一使用 `is_deleted` 逻辑删除
- 时间字段 `created_at` / `updated_at` 由 MyBatis-Plus `MetaObjectHandler` 自动填充
- `forum_post` 插入时由触发器 `trg_forum_post_after_insert` 同步更新 `sys_user_stats.post_count`

---

## 全局约定

### 统一响应

所有接口返回 `Result<T>`：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

状态码：`200` 成功 / `400` 参数错误 / `401` 未登录 / `403` 无权限 / `404` 资源不存在 / `500` 服务器异常。

### 鉴权

- Sa-Token 拦截 `/api/**`，放行：`/api/auth/login`、`/api/auth/register`、`/api/boards`、Swagger 相关路径。
- 需登录的接口标注 `@SaCheckLogin`；前端请求需在 Header 携带 `satoken`。

### 跨域

`CorsConfig` 全局放开，开发期间允许所有来源 / 方法 / 头部，生产环境请改为具体域名白名单。

---

## 启动与运行

### 环境要求
- JDK 17+
- Maven 3.8+（或直接使用项目自带 `./mvnw`）
- MySQL 8.0+（项目脚本基于 9.x，兼容 8.0）

### 步骤

1. **初始化数据库**

   ```sql
   CREATE DATABASE kaoyan_forum DEFAULT CHARACTER SET utf8mb4;
   ```

   导入脚本 `src/main/resources/kaoyan_forum_v2.sql`。

2. **修改数据源**

   编辑 `src/main/resources/application.properties`：

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/kaoyan_forum?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
   spring.datasource.username=<your-user>
   spring.datasource.password=<your-password>
   ```

3. **启动应用**

   ```bash
   ./mvnw spring-boot:run
   ```

   服务默认监听 `http://localhost:8081`。

4. **访问接口文档**

   - Swagger UI: <http://localhost:8081/swagger-ui.html>
   - OpenAPI JSON: <http://localhost:8081/v3/api-docs>

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

---

## 测试账号（脚本内置）

| 用户 | 角色 | 邮箱 |
| --- | --- | --- |
| Admin管理员 | ADMIN | admin@example.com |
| 考研高数版主 | MODERATOR | mod@example.com |
| 408上岸人 / 英语困难户 / 政治背书狂 | USER | user1~3@example.com |

种子用户密码均为 BCrypt 加密占位值，本地测试时建议通过 `/api/auth/register` 重新注册账号。
