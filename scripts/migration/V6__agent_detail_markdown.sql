-- ============================================
-- V6: Agent 详情 Markdown + 跳转链接
-- 为 ai_daily_task 和 ai_intervention_log 增加详情字段
-- 前端统一用 renderMarkdown() 渲染
-- ============================================

-- 1. ai_daily_task: 新增 detail_markdown / link_target / link_label
ALTER TABLE ai_daily_task
    ADD COLUMN detail_markdown TEXT COMMENT '任务详情(Markdown格式)' AFTER agent_tips,
    ADD COLUMN link_target    VARCHAR(256) COMMENT '跳转路由，如 /ai/knowledge?keyword=极限' AFTER detail_markdown,
    ADD COLUMN link_label     VARCHAR(64)  COMMENT '跳转按钮文案，如「去刷相关习题 →」' AFTER link_target;

-- 2. ai_intervention_log: 新增 detail_markdown / link_target / link_label
ALTER TABLE ai_intervention_log
    ADD COLUMN detail_markdown TEXT COMMENT '干预详情(Markdown格式)' AFTER intervention_content,
    ADD COLUMN link_target     VARCHAR(256) COMMENT '跳转路由' AFTER detail_markdown,
    ADD COLUMN link_label      VARCHAR(64)  COMMENT '跳转按钮文案' AFTER link_target;
