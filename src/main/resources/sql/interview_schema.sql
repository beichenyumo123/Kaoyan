-- ====================================================
-- AI 模拟复试官模块 - 数据库建表语句
-- 数据库：kaoyan_forum
-- ====================================================

-- 1. 面试会话表 (interview_session)
-- 记录用户每一次 AI 模拟面试的元信息
-- ====================================================
DROP TABLE IF EXISTS `interview_session`;
CREATE TABLE `interview_session` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID，自增',
    `user_id`        BIGINT       NOT NULL                 COMMENT '用户ID，关联 sys_user.id',
    `target_school`  VARCHAR(100) DEFAULT NULL             COMMENT '目标院校，例如：北京大学',
    `target_major`   VARCHAR(100) DEFAULT NULL             COMMENT '目标专业，例如：计算机科学与技术',
    `interview_type` VARCHAR(20)  NOT NULL                 COMMENT '面试类型：ENGLISH(英文面试) / MAJOR(专业课面试) / COMPREHENSIVE(综合面试)',
    `status`         VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '会话状态：IN_PROGRESS(进行中) / REPORTED(已出报告)',
    `overall_score`  DECIMAL(3,1) DEFAULT NULL             COMMENT '综合评分，范围 0.0 ~ 100.0，未出报告时为 NULL',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模拟面试会话表';


-- 2. 面试对话明细表 (interview_record)
-- 记录面试中每一轮的用户与 AI 对话内容
-- ====================================================
DROP TABLE IF EXISTS `interview_record`;
CREATE TABLE `interview_record` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID，自增',
    `session_id`     BIGINT       NOT NULL                 COMMENT '所属会话ID，关联 interview_session.id',
    `role`           VARCHAR(10)  NOT NULL                 COMMENT '角色：user(用户发言) / ai(人工智能考官)',
    `content`        TEXT         NOT NULL                 COMMENT '对话内容文本，支持长文本',
    `fluency_score`  DECIMAL(3,1) DEFAULT NULL             COMMENT '语音流利度得分（仅 role=user 时有值），范围 0.0 ~ 100.0',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模拟面试对话明细表';


-- 3. 面试评估报告表 (interview_report)
-- 存储 AI 基于全程对话生成的综合能力评估报告
-- ====================================================
DROP TABLE IF EXISTS `interview_report`;
CREATE TABLE `interview_report` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID，自增',
    `session_id`         BIGINT       NOT NULL                 COMMENT '所属会话ID，关联 interview_session.id，一对一关系',
    `total_score`        DECIMAL(3,1) NOT NULL                 COMMENT '综合评分，范围 0.0 ~ 100.0',
    `radar_chart`        JSON         DEFAULT NULL             COMMENT '雷达图能力维度数据（JSON格式），包含各维度的名称与分值',
    `strength_analysis`  TEXT         DEFAULT NULL             COMMENT '优势分析，AI 对考生表现得好的方面进行总结',
    `weakness_analysis`  TEXT         DEFAULT NULL             COMMENT '薄弱项分析，AI 指出考生需要改进的地方',
    `suggestion`         TEXT         DEFAULT NULL             COMMENT '改进建议，AI 给出的针对性备考建议',
    `summary`            TEXT         DEFAULT NULL             COMMENT '综合评价总结，一段话概括整场面试表现',
    `raw_json`           TEXT         DEFAULT NULL             COMMENT 'AI 返回的原始完整 JSON 字符串，用于问题回溯与调试',
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报告生成时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_session_id` (`session_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模拟面试评估报告表';
