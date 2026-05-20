# API 文档（前端联调参考）

本文档记录后续拓展中新增或修改的、与前端有联调关系的接口。基础接口请参考 Swagger UI（`http://localhost:8081/swagger-ui.html`）。

---

## 1. 图形验证码（2026-05-20 新增）

### 1.1 获取验证码

```
GET /api/auth/captcha
```

**无需登录**。

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uuid": "1032d729fe9e4d8081be3b4bc231e6bb",
    "base64": "data:image/png;base64,iVBORw0KGgo..."
  }
}
```

| 字段 | 说明 |
| --- | --- |
| `uuid` | 验证码唯一标识，登录时需传入 |
| `base64` | 验证码图片 Base64 编码，可直接设为 `<img src="data:image/png;base64,...">` |

**验证码有效期**：2 分钟。

---

### 1.2 登录（已修改）

```
POST /api/auth/login
```

**无需登录**。

**请求体**：

```json
{
  "account": "user@example.com",
  "password": "password123",
  "captchaCode": "m6fO",
  "captchaUuid": "1032d729fe9e4d8081be3b4bc231e6bb"
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `account` | String | 是 | 邮箱或手机号 |
| `password` | String | 是 | 密码 |
| `captchaCode` | String | **是（新增）** | 验证码内容（不区分大小写） |
| `captchaUuid` | String | **是（新增）** | 来自获取验证码接口的 uuid |

**校验规则**：
- 验证码校验在密码校验**之前**执行
- 校验通过后验证码立即失效（不可复用）
- 验证码 2 分钟后自动过期

**错误响应**：

```json
{"code": 400, "message": "验证码错误或已过期", "data": null}
```

---

### 前端对接流程

```
1. 页面加载 / 用户点击登录按钮
   ↓
2. GET /api/auth/captcha → 获取 { uuid, base64 }
   ↓
3. 将 base64 渲染为 <img> 标签
   ↓
4. 用户输入验证码 + 账号 + 密码
   ↓
5. POST /api/auth/login → 携带 captchaCode + captchaUuid
   ↓
6. 成功 → 拿到 token，跳转主页
   失败 → 提示用户，刷新验证码（重新执行步骤 2）
```

---

## 2. 接口限流（2026-05-20 新增）

部分高频接口可能标注 `@RateLimit` 注解实现限流。触发限流时返回：

```json
{"code": 429, "message": "请求过于频繁，请稍后再试", "data": null}
```

前端收到 429 时应提示用户操作过快，并等待 1-2 秒后自动重试（而非立即重试导致持续 429）。

---

## 3. 小组群聊（2026-05-20 新增）

### 3.1 群组 REST API

#### 3.1.1 创建群组

```
POST /api/chat/groups
```

**需登录**。

**请求体**：

```json
{
  "name": "408考研交流群",
  "description": "一起刷题讨论408"
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `name` | String | 是 | 群名称，最多 50 字 |
| `description` | String | 否 | 群简介，最多 255 字 |

**响应**：`data` 为新建群组的 ID。

---

#### 3.1.2 获取我的群组列表

```
GET /api/chat/groups
```

**需登录**。

**响应示例**：

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "408考研交流群",
      "description": "一起刷题讨论408",
      "avatarUrl": null,
      "ownerId": 6,
      "ownerName": "DB",
      "memberCount": 2,
      "createdAt": "2026-05-20T12:34:35"
    }
  ]
}
```

---

#### 3.1.3 获取群组详情

```
GET /api/chat/groups/{groupId}
```

**需登录**。返回字段同 3.1.2。

---

#### 3.1.4 加入群组

```
POST /api/chat/groups/{groupId}/join
```

**需登录**。

**错误响应**：

```json
{"code": 400, "message": "你已在群组中", "data": null}
```

---

#### 3.1.5 退出群组

```
POST /api/chat/groups/{groupId}/leave
```

**需登录**。

**错误响应**：

```json
{"code": 400, "message": "群主不能退群，请先转让群主", "data": null}
```

---

#### 3.1.6 获取群聊历史消息

```
GET /api/chat/groups/{groupId}/messages?pageNum=1&pageSize=20
```

**需登录**。

**响应示例**：

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "groupId": 1,
      "userId": 6,
      "username": "DB",
      "avatarUrl": "",
      "content": "大家好，欢迎加入408考研群！",
      "createdAt": "2026-05-20T12:38:04"
    }
  ]
}
```

> 消息按时间倒序排列，`pageSize` 默认 20。

---

### 3.2 WebSocket 群聊协议

#### 3.2.1 连接地址

```
ws://localhost:8081/ws/chat/{groupId}?satoken={token}
```

| 参数 | 说明 |
| --- | --- |
| `{groupId}` | URL 路径中的群组 ID |
| `satoken` | Query 参数，Sa-Token JWT（登录后获得的 token） |

**握手校验**：
- 校验 `satoken` 有效性（无效则拒绝握手）
- 校验用户是否为该群成员（非成员则拒绝握手）

---

#### 3.2.2 发送消息

客户端 → 服务端，JSON 格式：

```json
{"content": "大家好！"}
```

---

#### 3.2.3 消息广播

服务端 → 同群所有在线客户端（排除发送方本人）。

**系统消息**：

```json
{
  "type": "system",
  "data": {
    "content": "DB 加入了群聊",
    "onlineCount": "2"
  }
}
```

| 字段 | 说明 |
| --- | --- |
| `onlineCount` | 当前群在线人数（字符串类型） |

**聊天消息**：

```json
{
  "type": "message",
  "data": {
    "id": "1",
    "groupId": "1",
    "userId": "6",
    "username": "DB",
    "avatarUrl": "",
    "content": "大家好！",
    "createdAt": "2026-05-20T12:38:04"
  }
}
```

| 字段 | 说明 |
| --- | --- |
| `id` | 消息ID（入库后生成） |
| `content` | 消息内容，已过滤敏感词 |
| `createdAt` | 发送时间（ISO 8601） |

**错误提示**（消息格式错误时）：

```json
{"type": "error", "data": {"content": "消息格式错误"}}
```

---

#### 3.2.4 前端对接流程

```
1. 用户进入群聊页面
   ↓
2. 建立 WebSocket: new WebSocket("ws://host:8081/ws/chat/{groupId}?satoken={token}")
   ↓
3. 收到 type=system 消息 → 显示 "xxx 加入了群聊"
   ↓
4. 用户输入文字点击发送
   ↓
5. ws.send(JSON.stringify({content: "..."}))
   ↓
6. 收到 type=message 消息 → 渲染到聊天面板
   (注意：发送方不会收到自己的消息回显，前端需本地渲染)
   ↓
7. 离开页面 → ws.close() → 其他人收到 "xxx 离开了群聊"
```

> **注意**：历史消息（离线消息）通过 `GET /api/chat/groups/{groupId}/messages` 获取，初次进入群聊页面时应先拉取历史，再建立 WebSocket 接收实时消息。

---

## 4. 管理后台 — 数据看板（2026-05-20 新增）

### 4.1 获取看板数据

```
GET /api/admin/dashboard
```

**需登录 + ADMIN 角色**。

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalUsers": 1280,
    "todayPosts": 56,
    "topActiveUsers": [
      {
        "id": 6,
        "username": "DB",
        "avatar_url": "https://...",
        "total": 89
      },
      {
        "id": 12,
        "username": "考研达人",
        "avatar_url": "https://...",
        "total": 67
      }
    ]
  }
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `totalUsers` | Long | 平台总用户数（未删除） |
| `todayPosts` | Long | 今日新增帖子数 |
| `topActiveUsers` | List | 最活跃 Top 5 用户（发帖数 + 评论数总和） |
| `topActiveUsers[].id` | Long | 用户 ID |
| `topActiveUsers[].username` | String | 用户名 |
| `topActiveUsers[].avatar_url` | String | 头像 URL |
| `topActiveUsers[].total` | Long | 发帖 + 评论总数 |

**权限说明**：仅 `ADMIN` 角色可访问，非管理员返回 403。

---

## 5. 热门推荐（2026-05-20 新增）

### 5.1 获取热门帖子

```
GET /api/posts/hot?pageNum=1&pageSize=10
```

**无需登录**。

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 156,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 16,
    "list": [
      {
        "id": 128,
        "title": "2026 考研数学一真题解析",
        "content": "<p>分享一些解题技巧...</p>",
        "likeCount": 42,
        "viewCount": 356,
        "commentCount": 18,
        "createdAt": "2026-05-20T10:30:00",
        "author": {
          "userId": 6,
          "username": "DB",
          "avatarUrl": "https://..."
        },
        "isLiked": false
      }
    ]
  }
}
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `pageNum` | Integer | 否 | 1 | 页码 |
| `pageSize` | Integer | 否 | 10 | 每页条数 |

**算法说明**：基于 Hacker News 热度衰减公式 — `Score = (likeCount − 1) / (T + 2)^1.8`，其中 T 为发布至今的小时数。每 10 分钟由定时任务重新计算并写入 Redis ZSet，接口从 Redis 直接分页取回，不实时计算。

**冷启动**：若 Redis 中无数据（首次部署），接口返回空的 PageInfo，不报错。

---

## 6. 文件上传（2026-05-20 新增）

### 6.1 上传图片

```
POST /api/upload/image
```

**需登录**。`Content-Type: multipart/form-data`。

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | MultipartFile | 是 | 图片文件，≤ 10MB |

**格式限制**：`jpg`、`jpeg`、`png`、`gif`、`webp`（三重校验：扩展名 + MIME 类型 + 文件头魔数）。

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "/uploads/images/202605/a1b2c3d4e5f67890.png"
  }
}
```

### 6.2 上传视频

```
POST /api/upload/video
```

**需登录**。`Content-Type: multipart/form-data`。

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | MultipartFile | 是 | 视频文件，≤ 100MB |

**格式限制**：`mp4`、`webm`。

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "/uploads/videos/202605/b2c3d4e5f67890ab.mp4"
  }
}
```

### 前端对接流程

```
1. 用户在 Markdown 编辑器中选择/粘贴图片
     ↓
2. POST /api/upload/image (FormData { file })
     ↓
3. 拿到 { url } → 插入编辑器: ![](/uploads/images/202605/xxx.png)
     ↓
4. 编辑器渲染 Markdown → 生成 <img src="/uploads/images/202605/xxx.png">
     ↓
5. 提交帖子 content = 渲染后的 HTML
     ↓
6. Jackson Jsoup 清洗 → 存库（清除 onerror 等事件属性）
     ↓
7. 前端渲染帖子详情: v-html="content" / dangerouslySetInnerHTML
```

> 文件访问 URL = `http://localhost:8081` + 返回的 `url` 路径。存储路径按 `uploads/{images,videos}/{yyyyMM}/{uuid}.{ext}` 组织。

---
