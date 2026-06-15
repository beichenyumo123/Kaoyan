-- ============================================================
-- V3: AI 语义记忆 — Embedding 向量存储
-- 用于跨会话语义检索，让 AI "记住"几个月前的对话
-- ============================================================

CREATE TABLE IF NOT EXISTS `ai_memory_embedding` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '原文（问题+回答摘要）',
    `embedding` JSON NOT NULL COMMENT 'float[] 向量，1536维 (text-embedding-v2)',
    `source_type` VARCHAR(32) DEFAULT 'CHAT_QA' COMMENT 'CHAT_QA / MISTAKE_NOTE / KNOWLEDGE',
    `source_id` BIGINT COMMENT '关联的 chat_message_id 或 mistake_note_id',
    `created_at` DATETIME DEFAULT NOW(),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_source_type` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 语义记忆向量表';

-- 为 cosine similarity 检索优化：按 user_id 过滤后再计算
ALTER TABLE `ai_memory_embedding` ADD INDEX `idx_user_source` (`user_id`, `source_type`);
