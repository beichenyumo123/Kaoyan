-- ======================================================
-- 考研交流论坛 - 数据库初始化脚本
-- 数据库: kaoyan_forum
-- 字符集: utf8mb4 (支持表情符号和中文)
-- ======================================================

-- 1. 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `kaoyan_forum`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2. 使用该数据库
USE `kaoyan_forum`;

-- ======================================================
-- 3. 创建用户与权限模块表
-- ======================================================

-- 用户信息表
CREATE TABLE IF NOT EXISTS `sys_user` (
                                          `id`             BIGINT(20)   NOT NULL COMMENT '用户主键',
                                          `username`       VARCHAR(50)  NOT NULL COMMENT '用户名/昵称',
                                          `password`       VARCHAR(255) NOT NULL COMMENT '加密后的密码(BCrypt)',
                                          `email`          VARCHAR(100)          DEFAULT NULL COMMENT '邮箱(可用于登录)',
                                          `phone`          VARCHAR(20)           DEFAULT NULL COMMENT '手机号(可用于登录)',
                                          `role`           VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色: USER, MODERATOR, ADMIN',
                                          `avatar_url`     VARCHAR(255)          DEFAULT NULL COMMENT '头像链接(存OSS链接)',
                                          `target_major`   VARCHAR(100)          DEFAULT NULL COMMENT '目标专业',
                                          `points`         INT(11)      NOT NULL DEFAULT 0 COMMENT '总积分',
                                          `is_deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删, 1已删',
                                          `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          PRIMARY KEY (`id`),
                                          UNIQUE KEY `uk_email` (`email`),
                                          UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户信息表';

-- ======================================================
-- 4. 创建核心论坛模块表
-- ======================================================

-- 讨论区板块表
CREATE TABLE IF NOT EXISTS `forum_board` (
                                             `id`           BIGINT(20)   NOT NULL COMMENT '板块主键',
                                             `name`         VARCHAR(50)  NOT NULL COMMENT '板块名称',
                                             `description`  VARCHAR(255)          DEFAULT NULL COMMENT '板块简介',
                                             `cover_url`    VARCHAR(255)          DEFAULT NULL COMMENT '板块封面图',
                                             `post_count`   BIGINT(20)   NOT NULL DEFAULT 0 COMMENT '冗余: 帖子总数',
                                             `is_deleted`   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                             `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                             PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='讨论区板块表';

-- 帖子主表
CREATE TABLE IF NOT EXISTS `forum_post` (
                                            `id`              BIGINT(20)    NOT NULL COMMENT '帖子主键',
                                            `board_id`        BIGINT(20)    NOT NULL COMMENT '所属板块ID',
                                            `user_id`         BIGINT(20)    NOT NULL COMMENT '发帖人用户ID',
                                            `title`           VARCHAR(100)  NOT NULL COMMENT '帖子标题',
                                            `content`         LONGTEXT      NOT NULL COMMENT '帖子内容(富文本HTML)',
                                            `attachment_urls` JSON                   DEFAULT NULL COMMENT '附件列表(OSS链接数组)',
                                            `view_count`      INT(11)       NOT NULL DEFAULT 0 COMMENT '浏览量',
                                            `like_count`      INT(11)       NOT NULL DEFAULT 0 COMMENT '点赞数',
                                            `comment_count`   INT(11)       NOT NULL DEFAULT 0 COMMENT '评论数',
                                            `is_deleted`      TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                            `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                            PRIMARY KEY (`id`),
                                            KEY `idx_board_id` (`board_id`),
                                            KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子主表';

-- 帖子点赞关联表
CREATE TABLE IF NOT EXISTS `forum_post_like` (
                                                 `id`         BIGINT(20)   NOT NULL COMMENT '主键',
                                                 `post_id`    BIGINT(20)   NOT NULL COMMENT '帖子ID',
                                                 `user_id`    BIGINT(20)   NOT NULL COMMENT '点赞用户ID',
                                                 `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
                                                 PRIMARY KEY (`id`),
                                                 UNIQUE KEY `uk_post_user` (`post_id`, `user_id`) COMMENT '防止重复点赞'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子点赞关联表';

-- 评论表
CREATE TABLE IF NOT EXISTS `forum_comment` (
                                               `id`          BIGINT(20)    NOT NULL COMMENT '评论主键',
                                               `post_id`     BIGINT(20)    NOT NULL COMMENT '所属帖子ID',
                                               `user_id`     BIGINT(20)    NOT NULL COMMENT '评论发布者ID',
                                               `reply_to_id` BIGINT(20)             DEFAULT NULL COMMENT '回复的目标评论ID(顶层评论为空)',
                                               `content`     VARCHAR(1000) NOT NULL COMMENT '评论内容',
                                               `is_deleted`  TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                               `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                               PRIMARY KEY (`id`),
                                               KEY `idx_post_id` (`post_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='帖子评论表';

-- ======================================================
-- 5. 创建互动激励与资源模块表
-- ======================================================

-- 每日打卡记录表
CREATE TABLE IF NOT EXISTS `interaction_check_in` (
                                                      `id`           BIGINT(20)   NOT NULL COMMENT '主键',
                                                      `user_id`      BIGINT(20)   NOT NULL COMMENT '打卡用户ID',
                                                      `study_hours`  INT(11)      NOT NULL COMMENT '学习时长(小时)',
                                                      `notes`        VARCHAR(500)          DEFAULT NULL COMMENT '打卡日记/笔记',
                                                      `created_date` DATE         NOT NULL COMMENT '打卡归属日期(YYYY-MM-DD)',
                                                      `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '具体打卡时间',
                                                      PRIMARY KEY (`id`),
                                                      `continuous_days` INT(11)      NOT NULL DEFAULT 1 COMMENT '连续打卡天数',
                                                      UNIQUE KEY `uk_user_date` (`user_id`, `created_date`) COMMENT '每天每人只能打卡一次'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='每日学习打卡表';

-- 用户学习统计表
CREATE TABLE IF NOT EXISTS `interaction_user_study` (
    `id`               BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`          BIGINT(20)  NOT NULL COMMENT '用户ID',
    `continuous_days`   INT(11)     NOT NULL DEFAULT 0 COMMENT '当前连续打卡天数',
    `total_check_days`  INT(11)     NOT NULL DEFAULT 0 COMMENT '累计打卡总天数',
    `last_check_date`   DATE                 DEFAULT NULL COMMENT '最后一次打卡日期',
    `updated_at`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户学习统计表';

-- 积分变动日志表
CREATE TABLE IF NOT EXISTS `interaction_points_log` (
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT(20)   NOT NULL COMMENT '用户ID',
    `points`      INT(11)      NOT NULL COMMENT '积分变动值(正为增加,负为扣减)',
    `type`        VARCHAR(30)  NOT NULL COMMENT '积分类型: CHECK_IN, POST, COMMENT等',
    `rel_id`      BIGINT(20)            DEFAULT NULL COMMENT '关联业务ID(如帖子ID)',
    `description` VARCHAR(200)          DEFAULT NULL COMMENT '描述',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='积分变动日志表';

-- 学习资料表
CREATE TABLE IF NOT EXISTS `resource_file` (
                                               `id`             BIGINT(20)   NOT NULL COMMENT '资料主键',
                                               `board_id`       BIGINT(20)   NOT NULL COMMENT '所属板块ID',
                                               `uploader_id`    BIGINT(20)   NOT NULL COMMENT '上传者用户ID',
                                               `title`          VARCHAR(100) NOT NULL COMMENT '资料标题',
                                               `description`    VARCHAR(500)          DEFAULT NULL COMMENT '资料描述',
                                               `file_url`       VARCHAR(255) NOT NULL COMMENT 'OSS/对象存储链接',
                                               `file_type`      VARCHAR(20)           DEFAULT NULL COMMENT '文件格式(pdf, docx等)',
                                               `file_size`      BIGINT(20)            DEFAULT NULL COMMENT '文件大小(字节)',
                                               `download_count` INT(11)      NOT NULL DEFAULT 0 COMMENT '下载次数',
                                               `is_deleted`     TINYINT(1)   NOT NULL DEFAULT 0,
                                               `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               PRIMARY KEY (`id`),
                                               KEY `idx_board_id` (`board_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学习资料表';

  -- ======================================================
  -- 考研论坛 - 打卡与积分激励模块 SQL
  -- 模块：5号 - 打卡积分
  -- 表数量：3张
  -- ======================================================
  USE kaoyan_forum;

  -- 1. 每日学习打卡表
  CREATE TABLE IF NOT EXISTS `interaction_check_in`
  (
      `id`              BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `user_id`         BIGINT(20) NOT NULL COMMENT '打卡用户ID',
      `study_hours`     INT(11)    NOT NULL COMMENT '学习时长(小时)',
      `notes`           VARCHAR(500)     DEFAULT NULL COMMENT '打卡日记/笔记',
      `continuous_days`  INT(11)    NOT NULL DEFAULT 0 COMMENT '打卡时的连续天数',
      `created_date`    DATE       NOT NULL COMMENT '打卡归属日期(YYYY-MM-DD)',
      `created_at`      DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '具体打卡时间',
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_user_date` (`user_id`, `created_date`) COMMENT '每天每人只能打卡一次'
  ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4 COMMENT ='每日学习打卡表';

  -- 2. 用户学习打卡统计表（连续天数/累计天数）
  CREATE TABLE IF NOT EXISTS `interaction_user_study`
  (
      `id`               BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `user_id`          BIGINT(20) NOT NULL COMMENT '用户ID',
      `continuous_days`  INT(11)    NOT NULL DEFAULT 0 COMMENT '连续打卡天数',
      `total_check_days` INT(11)    NOT NULL DEFAULT 0 COMMENT '累计打卡天数',
      `last_check_date`  DATE                DEFAULT NULL COMMENT '最后一次打卡日期',
      `updated_at`       DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (`id`),
      UNIQUE KEY `uk_user_id` (`user_id`)
  ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4 COMMENT ='用户学习打卡统计';

  -- 3. 积分变动日志表
  CREATE TABLE IF NOT EXISTS `interaction_points_log`
  (
      `id`          BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '日志ID',
      `user_id`     BIGINT(20)  NOT NULL COMMENT '用户ID',
      `points`      INT(11)     NOT NULL COMMENT '变动积分（正数增加）',
      `type`        VARCHAR(30) NOT NULL COMMENT '类型：CHECK_IN/POST/LIKE/COMMENT/RESOURCE',
      `rel_id`      BIGINT(20) DEFAULT NULL COMMENT '关联ID（打卡ID/帖子ID/评论ID）',
      `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
      `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (`id`),
      KEY `idx_user_id` (`user_id`)
  ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4 COMMENT ='积分变动日志表';
   ALTER TABLE interaction_check_in MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;