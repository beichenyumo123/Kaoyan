-- ============================================================
-- V5: 语义记忆添加学科标签 — 支持按学科过滤相关记忆
-- ============================================================

ALTER TABLE ai_memory_embedding ADD COLUMN subject VARCHAR(32) DEFAULT NULL COMMENT '学科标签（MATH/408/ENGLISH/POLITICS）' AFTER content;
CREATE INDEX idx_user_subject ON ai_memory_embedding (user_id, subject);
