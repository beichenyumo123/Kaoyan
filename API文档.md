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
