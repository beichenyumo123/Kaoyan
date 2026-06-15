# 考研论坛会员增值服务 — API 文档

> **版本**: v1.0  
> **日期**: 2026-06-13  
> **面向**: 前端开发  
> **配套 SQL**: `scripts/migration/V1__membership_20260613.sql`

---

## 一、概述

引入「免费版 / VIP 月卡会员」两层增值服务体系，对 AI 高频功能、学习工具实施差异化配额控制。社区基础功能（浏览帖子、基础打卡、基础私信、AI 智能体事件驱动功能）保持免费。

### 核心概念

| 概念 | 说明 |
|------|------|
| `feature_key` | 功能标识字符串，如 `ai_ask`, `ocr`, `export_pdf` |
| `配额（quota）` | 某功能的可用次数，按天或月重置 |
| `-1` | 无限制（VIP 专属） |
| `0` | 完全禁止（需升级 VIP） |
| `402` | HTTP 状态码，表示会员不足或配额耗尽 |

### 错误码协议

所有会员相关的拒绝统一返回 **HTTP 200 + `code: 402`**（非 HTTP 402 状态码），保持与现有 `Result<T>` 包装器一致：

```json
{
  "code": 402,
  "message": "今日免费次数已用完，明日重置或升级VIP享更多次数",
  "data": {
    "featureKey": "ai_ask"
  }
}
```

> SSE 流式端点返回格式不同，见 [SSE 特殊处理](#sse-特殊处理)。

---

## 二、功能配额表

| feature_key | 功能 | 免费额度 | VIP 额度 | 重置周期 |
|-------------|------|---------|---------|---------|
| `ai_ask` | AI 答疑（同步 + SSE 流式） | 5 次/天 | 100 次/天 | 每日 |
| `ai_knowledge` | AI 知识库搜索 | 10 次/月 | 无限制 | 每月 |
| `ai_tasks` | AI 智能任务 | 禁止 | 无限制 | — |
| `ai_interventions` | AI 干预消息 | 禁止 | 无限制 | — |
| `weekly_report` | AI 历史周报 | 禁止 | 无限制 | — |
| `school_recommend` | 智能择校推荐 | 2 次/天 | 20 次/天 | 每日 |
| `interview` | 模拟面试（创建会话） | 2 次/月 | 10 次/月 | 每月 |
| `interview_tts` | 面试 TTS 语音合成 | 禁止 | 50 次/天 | 每日 |
| `ocr` | OCR 题目识别 | 3 次/天 | 30 次/天 | 每日 |
| `export_pdf` | 服务端高清 PDF 导出 | 禁止 | 10 次/月 | 每月 |
| `ebbinghaus_stats` | 艾宾浩斯高级统计 | 禁止 | 启用 | — |

> **免费功能（不加付费墙）**：AI 规划伴侣（PlannerAgent）、心理树洞（PsychologyAgent）、铁面教官（SupervisorAgent）、透视专家（BehaviorAnalysisAgent）— 这些由事件驱动，不占用配额。

---

## 三、API 接口

### 基础路径: `/api/membership`

---

### 3.1 获取套餐列表

```
GET /api/membership/plans
```

**认证**: 公开（无需 Token）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "planCode": "free",
      "planName": "免费版",
      "description": "基础功能，每日限量使用 AI 答疑、OCR 识别等核心功能",
      "price": 0,
      "durationDays": -1,
      "features": {
        "ai_ask": 5,
        "ocr": 3,
        "school_recommend": 2,
        "interview": 2
      }
    },
    {
      "id": 2,
      "planCode": "vip_monthly",
      "planName": "VIP月度会员",
      "description": "畅享所有 AI 功能，每日 100 次 AI 答疑，无限制知识库检索",
      "price": 29.90,
      "durationDays": 30,
      "features": {
        "ai_ask": 100,
        "ocr": 30,
        "interview_tts": 50,
        "export_pdf": 10
      }
    }
  ]
}
```

**前端用法**:
```typescript
// src/api/membership.ts
export async function getPlans(): Promise<MembershipPlan[]> {
  const res = await fetch('/api/membership/plans');
  const data = await res.json();
  return data.data;
}
```

---

### 3.2 获取当前会员状态

```
GET /api/membership/me
```

**认证**: 需要 Token（`@SaCheckLogin`）

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "plan": "free",
    "planName": "免费版",
    "expiresAt": null,
    "autoRenew": false,
    "features": {
      "ai_ask":    { "allowed": true,  "used": 3,  "limit": 5 },
      "ocr":       { "allowed": true,  "used": 2,  "limit": 3 },
      "export_pdf":{ "allowed": false, "used": 0,  "limit": 0 },
      "interview_tts":{ "allowed": false, "used": 0, "limit": 0 },
      ...
    }
  }
}
```

**字段说明**:
- `allowed`: 当前是否可用（剩余配额 > 0 或无限）
- `used`: 本周期已用量
- `limit`: 配额上限（-1 = 无限制，0 = 禁止）

**前端用法（登录后调一次，存入 store）**:
```typescript
// 在 auth store 的 fetchProfile 中调用
const data = await api.get('/api/membership/me');
store.membership = data.data;
```

---

### 3.3 检查某功能是否可用（轻量）

```
GET /api/membership/check/{featureKey}
```

**认证**: 需要 Token

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "featureKey": "ai_ask",
    "available": true,
    "used": 4,
    "limit": 5,
    "remaining": 1,
    "reason": "OK"
  }
}
```

**reason 取值**: `OK` | `VIP_REQUIRED` | `QUOTA_EXHAUSTED`

**前端用法（可选预检优化）**:
```typescript
// 在发送 AI 问题前先预检
const check = await api.get('/api/membership/check/ai_ask');
if (!check.data.available) {
  showUpgradePrompt();  // 直接弹升级窗，不建 SSE 连接
  return;
}
// 余额充足，发送请求
await sendAiAsk(question);
```

---

### 3.4 升级套餐（创建订单）

```
POST /api/membership/upgrade
Content-Type: application/json

{ "planId": 2 }
```

**认证**: 需要 Token

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "orderNo": "VIP1718275200000000001",
    "planName": "VIP月度会员",
    "amount": 29.90,
    "status": "PENDING"
  }
}
```

**:warning: 当前为支付占位** — 订单创建后状态为 PENDING，支付网关对接后自动流转。

---

### 3.5 取消自动续费

```
POST /api/membership/cancel
```

**认证**: 需要 Token

**响应**: `{"code": 200, "message": "success", "data": "已取消自动续费"}`

---

### 3.6 用户信息中的会员字段

`GET /api/users/me` 的响应现在包含 `membership` 字段：

```json
{
  "code": 200,
  "data": {
    "id": 8,
    "username": "db886",
    "role": "USER",
    ...
    "membership": {
      "plan": "free",
      "planName": "免费版",
      "expiresAt": null,
      "autoRenew": false,
      "features": { ... }
    }
  }
}
```

---

## 四、受保护接口的 402 响应

以下接口在配额不足或 VIP 禁止时返回 402：

| 接口 | feature_key | 返回格式 |
|------|------------|---------|
| `POST /api/ai/ask` | `ai_ask` | `Result<T>` 402 |
| `POST /api/ai/ask/stream` | `ai_ask` | **SSE error 事件** |
| `GET /api/ai/knowledge` | `ai_knowledge` | `Result<T>` 402 |
| `GET /api/ai/report/history` | `weekly_report` | `Result<T>` 402 |
| `POST /api/interview/session/create` | `interview` | `Result<T>` 402 |
| `POST /api/interview/tts` | `interview_tts` | `Result<T>` 402 |
| `POST /api/school-select/recommend` | `school_recommend` | `Result<T>` 402 |
| `POST /api/mistake/ocr` | `ocr` | `Result<T>` 402 |
| `POST /api/mistake/export/pdf` | `export_pdf` | `Result<T>` 402 |
| `GET /api/mistake/stats/ebbinghaus` | `ebbinghaus_stats` | `Result<T>` 402 |

**标准 402 响应**:
```json
{
  "code": 402,
  "message": "今日免费次数已用完，明日重置或升级VIP享更多次数",
  "data": {
    "featureKey": "ai_ask"
  }
}
```

或（VIP 专属功能）:
```json
{
  "code": 402,
  "message": "该功能需要VIP会员",
  "data": {
    "featureKey": "export_pdf"
  }
}
```

---

## 五、SSE 特殊处理

### 5.1 后端行为

`POST /api/ai/ask/stream` 的配额检查在 **异步 AI 调用之前** 完成：

```
前端请求 → 后端收到 → 认证检查 → Redis Lua 原子预扣配额
  ├─ 预扣成功 → CompletableFuture.runAsync() → SSE 流式返回
  │   └─ AI 调用异常 → 退款配额（Redis DECR）
  └─ 预扣失败 → SSE error 事件 + 关闭连接
```

### 5.2 SSE Error 事件格式

```json
{
  "type": "error",
  "code": 402,
  "featureKey": "ai_ask",
  "message": "今日免费次数已用完，明日重置或升级VIP享100次/天"
}
```

> **注意**: SSE error 事件的格式是 `event: error\ndata: <json>\n\n`，不是标准的 HTTP 响应 body。

### 5.3 前端 SSE 处理代码

```typescript
// AiAsk.vue — sendMessage() 中
const eventSource = new EventSource(/* ... */);

eventSource.addEventListener('error', (event) => {
  const parsed = JSON.parse(event.data);
  
  if (parsed.code === 402) {
    // 配额不足
    abortStream();                          // 中止 SSE
    removeLastHalfAiMessage();              // 移除半条 AI 消息
    showUpgradePrompt({                     // 弹升级弹窗
      featureKey: parsed.featureKey,
      message: parsed.message
    });
    return;
  }
  
  if (parsed.code === 401) {
    // 认证过期
    redirectToLogin();
  }
});
```

### 5.4 前端预检（可选优化）

发消息前先调 `GET /api/membership/check/ai_ask`，余额不足时直接弹升级窗，避免无意义的 SSE 建连：

```typescript
// composables/useMembership.ts
export function useMembership(featureKey: string) {
  const canAccess = ref(true);
  const remaining = ref(0);
  
  async function checkBeforeUse(): Promise<boolean> {
    const res = await api.get(`/api/membership/check/${featureKey}`);
    const { available, remaining: rem } = res.data;
    remaining.value = rem;
    canAccess.value = available;
    return available;
  }
  
  return { canAccess, remaining, checkBeforeUse };
}

// 在组件中使用
const { remaining, checkBeforeUse } = useMembership('ai_ask');

async function handleSend() {
  if (!await checkBeforeUse()) {
    showUpgradePrompt();
    return;
  }
  await startSSEStream();
}
```

---

## 六、前端集成 Checklist

### 6.1 类型定义 (`src/types/membership.ts`)

```typescript
export interface FeatureQuota {
  allowed: boolean;
  used: number;
  limit: number;  // -1 = 无限, 0 = 禁止
}

export interface MembershipInfo {
  plan: string;
  planName: string;
  expiresAt: string | null;
  autoRenew: boolean;
  features: Record<string, FeatureQuota>;
}

export interface MembershipPlan {
  id: number;
  planCode: string;
  planName: string;
  description: string;
  price: number;
  durationDays: number;
  features: Record<string, number>;
}
```

### 6.2 API 拦截器

在 `src/api/index.ts` 的 `request()` 中增加 402 处理：

```typescript
if (res.status === 200) {
  const json = await res.json();
  if (json.code === 402) {
    // 触发会员升级弹窗
    window.dispatchEvent(new CustomEvent('membership-upgrade-prompt', {
      detail: { featureKey: json.data?.featureKey, message: json.message }
    }));
    throw new ApiError(402, json.message, json.data);
  }
  return json;
}
```

### 6.3 路由守卫

```typescript
// router/index.ts
{
  path: '/ai/dashboard',
  meta: { requiresMembership: true },
  // ...
}

// beforeEach 中
if (to.meta.requiresMembership && !authStore.isPremium) {
  return { path: '/pricing', query: { redirect: to.fullPath } };
}
```

### 6.4 Composable (`src/composables/useMembership.ts`)

```typescript
export function useMembership(featureKey?: string) {
  const authStore = useAuthStore();
  
  const isPremium = computed(() => authStore.membership?.plan !== 'free');
  const currentPlan = computed(() => authStore.membership?.planName ?? '免费版');
  
  const featureQuota = computed(() => {
    if (!featureKey) return null;
    return authStore.membership?.features?.[featureKey];
  });
  
  const canAccess = computed(() => featureQuota.value?.allowed ?? true);
  const used = computed(() => featureQuota.value?.used ?? 0);
  const limit = computed(() => featureQuota.value?.limit ?? 0);
  const remaining = computed(() => {
    if (limit.value === -1) return Infinity;
    return Math.max(0, limit.value - used.value);
  });
  
  function showUpgradePrompt() {
    window.dispatchEvent(new CustomEvent('membership-upgrade-prompt', {
      detail: { featureKey }
    }));
  }
  
  return { isPremium, currentPlan, canAccess, used, limit, remaining, showUpgradePrompt };
}
```

---

## 七、数据库迁移

```bash
# 执行迁移 SQL（幂等，可重复运行）
mysql -u root -p kaoyan_forum < scripts/migration/V1__membership_20260613.sql
```

迁移内容：
- 创建 4 张表：`membership_plans`, `user_memberships`, `user_usage_logs`, `membership_orders`
- 插入种子数据：免费版（plan_code=free）+ VIP 月卡（plan_code=vip_monthly）
- 所有用户默认为免费版（无需在 `user_memberships` 表中有记录）

---

## 八、测试用例

### 8.1 免费用户配额耗尽

```bash
# 1. 登录免费用户
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"account":"test@test.com","password":"123456"}' | jq -r '.data.token')

# 2. 连续调用 AI 问答 5 次（全部成功）
for i in {1..5}; do
  curl -s -X POST http://localhost:8080/api/ai/ask \
    -H "Authorization: $TOKEN" \
    -H 'Content-Type: application/json' \
    -d '{"question":"什么是数据结构？"}' | jq '.code'
done
# 5 次全部返回 200

# 3. 第 6 次 → 402
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H "Authorization: $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"question":"什么是算法？"}' | jq .
# 返回: {"code":402,"message":"今日免费次数已用完...","data":{"featureKey":"ai_ask"}}
```

### 8.2 VIP 专享功能被拦截

```bash
# 免费用户调导出 PDF → 402
curl -s -X POST http://localhost:8080/api/mistake/export/pdf \
  -H "Authorization: $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"noteIds":[1,2,3]}' | jq '.code'
# 返回: 402

# 免费用户调艾宾浩斯统计 → 402
curl -s http://localhost:8080/api/mistake/stats/ebbinghaus \
  -H "Authorization: $TOKEN" | jq '.code'
# 返回: 402
```

### 8.3 会员状态查询

```bash
# 查看当前会员状态
curl -s http://localhost:8080/api/membership/me \
  -H "Authorization: $TOKEN" | jq '.data.plan'
# 返回: "free"

# 查看套餐列表
curl -s http://localhost:8080/api/membership/plans | jq '.data[].planCode'
# 返回: "free", "vip_monthly"
```

### 8.4 VIP 用户无限制

```bash
# 手动授予 VIP（测试用 SQL）
mysql -u root -p kaoyan_forum -e "
  INSERT INTO user_memberships (user_id, plan_id, status, started_at, expires_at)
  VALUES (2, 2, 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));
"

# 重新调用 AI 问答 → 200（不再受 5 次/天限制）
curl -s -X POST http://localhost:8080/api/ai/ask \
  -H "Authorization: $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"question":"继续提问..."}' | jq '.code'
# 返回: 200
```

---

## 九、Redis Key 参考

运维/调试时查看 Redis 中的会员数据：

```bash
# 用户会员缓存（TTL 1h）
redis-cli GET "membership:plan:2"

# 用户今日 AI 问答配额（TTL 48h）
redis-cli GET "membership:quota:2:2026-06-13:ai_ask"

# 用户本月面试配额
redis-cli GET "membership:quota:2:2026-06:interview"
```

---

## 十、后续待办

- [ ] 对接支付网关（支付宝/微信支付回调 → 更新 `membership_orders` → 激活 `user_memberships`）
- [ ] 会员升级成功后发布 `MembershipUpgradedEvent`，触发站内信通知
- [ ] 管理后台：套餐管理 CRUD（可复用 `AdminDashboardController` 模式）
- [ ] 前端 PricingPage 接入 `/api/membership/plans` 动态渲染套餐
- [ ] 前端 AiAsk.vue 接入 `useMembership('ai_ask')` + SSE 402 处理
