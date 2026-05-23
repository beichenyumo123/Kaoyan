-- AI模拟复试官模块 - 数据库建表脚本
-- 在 kaoyan_forum 库中执行

-- 复试会话表
DROP TABLE IF EXISTS `interview_session`;
CREATE TABLE `interview_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '考生用户ID',
  `school_style` varchar(50) NOT NULL COMMENT '院校风格：清华/同济/普通985/211/双非',
  `major` varchar(50) NOT NULL COMMENT '报考专业',
  `interview_type` varchar(30) NOT NULL COMMENT '面试类型：english_self/english_qa/professional/comprehensive/stress',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0进行中, 1已结束',
  `total_questions` int NOT NULL DEFAULT '0' COMMENT '总问题数',
  `answered_questions` int NOT NULL DEFAULT '0' COMMENT '已回答数',
  `started_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `ended_at` datetime DEFAULT NULL COMMENT '结束时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_user_status` (`user_id`, `status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模拟复试会话表';

-- 复试问答记录表
DROP TABLE IF EXISTS `interview_question`;
CREATE TABLE `interview_question` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '所属会话ID',
  `question_number` int NOT NULL COMMENT '问题序号',
  `question_content` text NOT NULL COMMENT '面试官提问内容',
  `question_type` varchar(30) NOT NULL COMMENT '问题类型：intro/professional/english/behavioral/stress',
  `user_answer` text COMMENT '考生回答内容',
  `ai_score` int DEFAULT NULL COMMENT 'AI评分：0-100',
  `ai_comment` text COMMENT 'AI点评',
  `dimension_scores` json DEFAULT NULL COMMENT '多维度评分JSON',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_session_id` (`session_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='复试问答记录表';

-- 复试评估报告表
DROP TABLE IF EXISTS `interview_report`;
CREATE TABLE `interview_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '所属会话ID',
  `user_id` bigint NOT NULL COMMENT '考生用户ID',
  `total_score` int NOT NULL COMMENT '综合总分：0-100',
  `content_depth_score` int NOT NULL COMMENT '内容深度评分',
  `language_express_score` int NOT NULL COMMENT '语言表达评分',
  `psychology_score` int NOT NULL COMMENT '心理状态评分',
  `comprehensive_score` int NOT NULL COMMENT '综合素养评分',
  `summary` text NOT NULL COMMENT '综合评价总结',
  `strengths` text COMMENT '优势分析',
  `weaknesses` text COMMENT '不足之处',
  `improvement_advice` text COMMENT '改进建议',
  `suggested_practice` text COMMENT '建议练习方向',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_session_id` (`session_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  UNIQUE KEY `uk_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='复试评估报告表';
