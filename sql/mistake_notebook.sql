-- ============================================
-- OCR 智能错题本 + 艾宾浩斯复习计划
-- ============================================

-- 错题本主表
CREATE TABLE IF NOT EXISTS `mistake_note` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `subject` VARCHAR(50) DEFAULT '其他' COMMENT '科目：政治/英语(一)/英语(二)/数学(一)/数学(二)/数学(三)/408计算机/其他',
    `question_content` TEXT COMMENT '题目内容（OCR或手动输入）',
    `answer` TEXT COMMENT '答案与解析',
    `image_url` VARCHAR(500) COMMENT '原题图片URL',
    `knowledge_points` VARCHAR(500) COMMENT '知识点标签（逗号分隔，如：操作系统-进程调度,数据结构-二叉树）',
    `source` VARCHAR(200) COMMENT '来源（如：2023真题、张宇1000题、王道408等）',
    `difficulty` TINYINT DEFAULT 3 COMMENT '难度 1-5',
    `mastery_level` TINYINT DEFAULT 0 COMMENT '掌握程度 0-100',
    `review_stage` INT DEFAULT 0 COMMENT '当前艾宾浩斯复习阶段（0-7），0=未复习，7=已掌握',
    `review_count` INT DEFAULT 0 COMMENT '累计复习次数',
    `next_review_date` DATE COMMENT '下次复习日期',
    `last_review_date` DATE COMMENT '上次复习日期',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_next_review` (`user_id`, `next_review_date`),
    INDEX `idx_subject` (`user_id`, `subject`),
    INDEX `idx_mastery` (`user_id`, `mastery_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本';

-- 复习日志表
CREATE TABLE IF NOT EXISTS `mistake_review_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `note_id` BIGINT NOT NULL COMMENT '错题ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `review_stage` INT COMMENT '复习时处于第几阶段',
    `mastery_before` TINYINT COMMENT '复习前掌握程度',
    `mastery_after` TINYINT COMMENT '复习后掌握程度',
    `is_correct` TINYINT DEFAULT 0 COMMENT '本次是否答对',
    `reviewed_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_note_id` (`note_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_reviewed_at` (`user_id`, `reviewed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复习日志';

-- 每日复习计划表
CREATE TABLE IF NOT EXISTS `mistake_daily_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `plan_date` DATE NOT NULL COMMENT '计划日期',
    `note_ids` JSON COMMENT '当天待复习的错题ID列表',
    `completed_ids` JSON COMMENT '已完成的错题ID列表',
    `total_count` INT DEFAULT 0 COMMENT '计划总数',
    `completed_count` INT DEFAULT 0 COMMENT '已完成数',
    `is_completed` TINYINT DEFAULT 0 COMMENT '是否全部完成',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日复习计划';
