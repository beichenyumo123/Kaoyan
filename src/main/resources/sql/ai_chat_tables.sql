-- AI 对话会话表
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NOT NULL COMMENT '用户ID',
  `title`       VARCHAR(100) NOT NULL DEFAULT '新对话' COMMENT '会话标题（取用户第一条消息前20字）',
  `is_deleted`  TINYINT(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_updated` (`user_id`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话会话表';

-- AI 对话消息表
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT,
  `session_id`  BIGINT NOT NULL COMMENT '所属会话ID',
  `role`        VARCHAR(20) NOT NULL COMMENT '角色: user / assistant',
  `content`     TEXT NOT NULL COMMENT '消息内容',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_created` (`session_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话消息表';
