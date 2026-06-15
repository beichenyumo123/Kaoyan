-- ============================================================
-- V4: 用户表增加目标院校字段
-- 用于 Agent Memory 匹配上岸经验贴
-- ============================================================

ALTER TABLE sys_user ADD COLUMN target_school VARCHAR(100) DEFAULT NULL COMMENT '目标院校' AFTER target_major;
