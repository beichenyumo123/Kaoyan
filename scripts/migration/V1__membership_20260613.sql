-- ============================================================
-- 考研论坛会员增值服务 - 数据库迁移脚本
-- Version: V1
-- Date: 2026-06-13
-- 兼容: MySQL 8.0+ / utf8mb4
-- ============================================================

-- 1. 套餐定义表
CREATE TABLE IF NOT EXISTS membership_plans (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_code    VARCHAR(50)  NOT NULL UNIQUE COMMENT '套餐编码: free, vip_monthly, vip_yearly',
    plan_name    VARCHAR(100) NOT NULL COMMENT '套餐显示名称',
    description  TEXT         COMMENT '套餐描述',
    price        DECIMAL(10,2) DEFAULT 0 COMMENT '价格（元）',
    duration_days INT         DEFAULT -1 COMMENT '有效期天数，-1=永久',
    features     JSON         COMMENT '各功能配额JSON: {"ai_ask":5,"ocr":3,...}，-1=无限制，0=禁止',
    is_active    TINYINT(1)   DEFAULT 1 COMMENT '是否启用',
    sort_order   INT          DEFAULT 0 COMMENT '排序权重',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员套餐定义';

-- 2. 用户订阅记录表
CREATE TABLE IF NOT EXISTS user_memberships (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    plan_id      BIGINT       NOT NULL COMMENT '套餐ID',
    status       VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/EXPIRED/CANCELLED',
    started_at   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '订阅开始时间',
    expires_at   DATETIME     COMMENT '过期时间，NULL=永久',
    cancelled_at DATETIME     COMMENT '取消时间',
    auto_renew   TINYINT(1)   DEFAULT 0 COMMENT '是否自动续费',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX        idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户订阅记录';

-- 3. 功能使用日志表（MySQL 持久化，热数据在 Redis）
CREATE TABLE IF NOT EXISTS user_usage_logs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    feature_key  VARCHAR(50)  NOT NULL COMMENT '功能标识: ai_ask, ocr, export_pdf ...',
    usage_date   DATE         NOT NULL COMMENT '使用日期',
    count        INT          DEFAULT 1 COMMENT '当日使用次数',
    UNIQUE KEY   uk_user_feature_date (user_id, feature_key, usage_date),
    INDEX        idx_user_date (user_id, usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='功能使用日志';

-- 4. 订单记录表
CREATE TABLE IF NOT EXISTS membership_orders (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    plan_id         BIGINT       NOT NULL,
    order_no        VARCHAR(64)  NOT NULL UNIQUE COMMENT '订单号',
    amount          DECIMAL(10,2) NOT NULL COMMENT '金额（元）',
    payment_method  VARCHAR(50)  COMMENT '支付方式: ALIPAY/WECHAT/...',
    payment_status  VARCHAR(20)  DEFAULT 'PENDING' COMMENT '支付状态: PENDING/PAID/REFUNDED/CANCELLED',
    transaction_id  VARCHAR(128) COMMENT '第三方交易号',
    paid_at         DATETIME     COMMENT '支付时间',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX           idx_user (user_id),
    INDEX           idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员订单';

-- ============================================================
-- 种子数据
-- ============================================================

-- 免费版（所有用户默认）
INSERT IGNORE INTO membership_plans (plan_code, plan_name, description, price, duration_days, features, sort_order)
VALUES (
    'free', '免费版', '基础功能，每日限量使用 AI 答疑、OCR 识别等核心功能',
    0, -1,
    '{
        "ai_ask": 5,
        "ai_tasks": 0,
        "ai_interventions": 0,
        "ai_knowledge": 10,
        "school_recommend": 2,
        "interview": 2,
        "interview_tts": 0,
        "ocr": 3,
        "export_pdf": 0,
        "ebbinghaus_stats": 0,
        "weekly_report": 0
    }',
    1
);

-- VIP 月度会员
INSERT IGNORE INTO membership_plans (plan_code, plan_name, description, price, duration_days, features, sort_order)
VALUES (
    'vip_monthly', 'VIP月度会员', '畅享所有 AI 功能，每日 100 次 AI 答疑，无限制知识库检索，VIP 专属高级统计',
    29.90, 30,
    '{
        "ai_ask": 100,
        "ai_tasks": -1,
        "ai_interventions": -1,
        "ai_knowledge": -1,
        "school_recommend": 20,
        "interview": 10,
        "interview_tts": 50,
        "ocr": 30,
        "export_pdf": 10,
        "ebbinghaus_stats": -1,
        "weekly_report": -1
    }',
    2
);
