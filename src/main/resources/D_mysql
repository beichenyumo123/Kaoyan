

-- ============================================================
-- D. 上岸认证 + 可信经验数据库 — 数据库变更脚本
-- 执行前请确认当前数据库名称
-- ============================================================

-- -----------------------------------------------------------
-- 1. sys_user 表新增 is_verified 字段（不存在时才添加）
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS add_is_verified_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_is_verified_if_not_exists()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'is_verified'
    ) THEN
        ALTER TABLE sys_user ADD COLUMN is_verified TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否通过上岸认证';
    END IF;
END //
DELIMITER ;
CALL add_is_verified_if_not_exists();
DROP PROCEDURE add_is_verified_if_not_exists;

-- -----------------------------------------------------------
-- 2. 上岸认证记录表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS user_verification;
CREATE TABLE user_verification (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id         BIGINT          NOT NULL                 COMMENT '用户ID，关联 sys_user.id',
    real_name       VARCHAR(50)     NOT NULL                 COMMENT '真实姓名',
    target_school   VARCHAR(100)    NOT NULL                 COMMENT '录取院校',
    target_major    VARCHAR(100)    NOT NULL                 COMMENT '录取专业',
    admission_year  INT             NOT NULL                 COMMENT '入学年份',
    admission_letter_url  VARCHAR(500)  NOT NULL             COMMENT '录取通知书图片URL（/uploads/...）',
    xuexin_screenshot_url VARCHAR(500) NOT NULL             COMMENT '学信网截图URL（/uploads/...）',
    status          TINYINT         NOT NULL DEFAULT 0       COMMENT '审核状态: 0=待审核, 1=已通过, 2=已驳回',
    reviewer_id     BIGINT          DEFAULT NULL             COMMENT '审核人ID，关联 sys_user.id',
    review_comment  VARCHAR(500)    DEFAULT NULL             COMMENT '审核意见',
    reviewed_at     DATETIME        DEFAULT NULL             COMMENT '审核时间',
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_status (status),
    KEY idx_target_school (target_school),
    KEY idx_target_major (target_major)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上岸认证记录表';

-- -----------------------------------------------------------
-- 3. 结构化经验贴表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS experience_post;
CREATE TABLE experience_post (
    id                  BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id             BIGINT          NOT NULL                 COMMENT '作者ID，关联 sys_user.id',
    forum_post_id       BIGINT          DEFAULT NULL             COMMENT '关联论坛帖子ID（null=独立经验贴）',
    undergrad_school    VARCHAR(100)    NOT NULL                 COMMENT '本科院校',
    undergrad_major     VARCHAR(100)    NOT NULL                 COMMENT '本科专业',
    is_cross_major      TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '是否跨考: 0=否, 1=是',
    is_second_attempt   TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '是否二战: 0=否, 1=是',
    target_school       VARCHAR(100)    NOT NULL                 COMMENT '目标院校',
    target_major        VARCHAR(100)    NOT NULL                 COMMENT '目标专业',
    initial_exam_total  DECIMAL(5,1)    DEFAULT NULL             COMMENT '初试总分',
    initial_exam_politics DECIMAL(4,1)  DEFAULT NULL             COMMENT '政治',
    initial_exam_english  DECIMAL(4,1)  DEFAULT NULL             COMMENT '英语',
    initial_exam_math     DECIMAL(4,1)  DEFAULT NULL             COMMENT '数学',
    initial_exam_major    DECIMAL(4,1)  DEFAULT NULL             COMMENT '专业课',
    re_exam_score       DECIMAL(5,1)    DEFAULT NULL             COMMENT '复试分',
    timeline_json       JSON            DEFAULT NULL             COMMENT '备考时间线 [{"phase":"基础","start":"3月","end":"6月","desc":"..."}]',
    books_json          JSON            DEFAULT NULL             COMMENT '用书推荐 [{"subject":"数学","name":"复习全书","rating":5}]',
    tips                TEXT            DEFAULT NULL             COMMENT '备考心得/建议',
    is_verified         TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '发布时作者是否已认证（快照）',
    status              TINYINT         NOT NULL DEFAULT 1       COMMENT '状态: 0=草稿, 1=已发布',
    view_count          INT             NOT NULL DEFAULT 0       COMMENT '浏览数',
    like_count          INT             NOT NULL DEFAULT 0       COMMENT '点赞数',
    collect_count       INT             NOT NULL DEFAULT 0       COMMENT '收藏数',
    is_deleted          TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_forum_post_id (forum_post_id),
    KEY idx_target_school_major (target_school, target_major),
    KEY idx_undergrad_school (undergrad_school),
    KEY idx_is_verified (is_verified),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结构化经验贴表';

-- -----------------------------------------------------------
-- 4. 经验贴点赞表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS experience_post_like;
CREATE TABLE experience_post_like (
    id              BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    experience_id   BIGINT      NOT NULL                 COMMENT '经验贴ID',
    user_id         BIGINT      NOT NULL                 COMMENT '点赞用户ID',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_experience_user (experience_id, user_id),
    KEY idx_experience_id (experience_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经验贴点赞表';

-- -----------------------------------------------------------
-- 5. 经验贴收藏表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS experience_post_collect;
CREATE TABLE experience_post_collect (
    id              BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    experience_id   BIGINT      NOT NULL                 COMMENT '经验贴ID',
    user_id         BIGINT      NOT NULL                 COMMENT '收藏用户ID',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_experience_user (experience_id, user_id),
    KEY idx_experience_id (experience_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经验贴收藏表';

-- -----------------------------------------------------------
-- 6. 经验贴触发器（点赞/收藏时自动更新 like_count / collect_count）
-- -----------------------------------------------------------

DELIMITER //

DROP TRIGGER IF EXISTS trg_experience_like_insert//
CREATE TRIGGER trg_experience_like_insert
AFTER INSERT ON experience_post_like
FOR EACH ROW
BEGIN
    UPDATE experience_post SET like_count = like_count + 1 WHERE id = NEW.experience_id;
END//

DROP TRIGGER IF EXISTS trg_experience_like_delete//
CREATE TRIGGER trg_experience_like_delete
AFTER DELETE ON experience_post_like
FOR EACH ROW
BEGIN
    UPDATE experience_post SET like_count = like_count - 1 WHERE id = OLD.experience_id;
END//

DROP TRIGGER IF EXISTS trg_experience_collect_insert//
CREATE TRIGGER trg_experience_collect_insert
AFTER INSERT ON experience_post_collect
FOR EACH ROW
BEGIN
    UPDATE experience_post SET collect_count = collect_count + 1 WHERE id = NEW.experience_id;
END//

DROP TRIGGER IF EXISTS trg_experience_collect_delete//
CREATE TRIGGER trg_experience_collect_delete
AFTER DELETE ON experience_post_collect
FOR EACH ROW
BEGIN
    UPDATE experience_post SET collect_count = collect_count - 1 WHERE id = OLD.experience_id;
END//

DELIMITER ;

-- -----------------------------------------------------------
-- 7. 用户统计表新增经验贴数字段（不存在时才添加）
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS add_experience_count_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_experience_count_if_not_exists()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user_stats' AND COLUMN_NAME = 'experience_count'
    ) THEN
        ALTER TABLE sys_user_stats ADD COLUMN experience_count INT NOT NULL DEFAULT 0 COMMENT '经验贴数';
    END IF;
END //
DELIMITER ;
CALL add_experience_count_if_not_exists();
DROP PROCEDURE add_experience_count_if_not_exists;

-- -----------------------------------------------------------
-- 8. 测试种子数据
-- -----------------------------------------------------------

-- 8.1 给部分用户打上"已认证"标识
UPDATE sys_user SET is_verified = 1 WHERE id IN (3, 6);

-- 8.2 插入认证记录（408上岸人和DB通过认证，英语困难户被驳回）
INSERT INTO user_verification (user_id, real_name, target_school, target_major, admission_year, admission_letter_url, xuexin_screenshot_url, status, reviewer_id, review_comment, reviewed_at) VALUES
(3, '张上岸', '浙江大学', '计算机科学与技术', 2025, '/uploads/images/202505/admission_letter_3.png', '/uploads/images/202505/xuexin_3.png', 1, 1, '审核通过，恭喜上岸！', '2025-06-15 10:30:00'),
(6, 'DB', '哈尔滨工业大学', '计算机技术', 2025, '/uploads/images/202505/admission_letter_6.png', '/uploads/images/202505/xuexin_6.png', 1, 1, '审核通过', '2025-06-16 14:20:00');

INSERT INTO user_verification (user_id, real_name, target_school, target_major, admission_year, admission_letter_url, xuexin_screenshot_url, status, reviewer_id, review_comment, reviewed_at) VALUES
(4, '李英语', '北京外国语大学', '英语笔译', 2025, '/uploads/images/202505/admission_letter_4.png', '/uploads/images/202505/xuexin_4.png', 2, 1, '录取通知书图片模糊，请重新上传', '2025-06-15 11:00:00');

-- 8.3 插入经验贴
INSERT INTO experience_post (user_id, forum_post_id, undergrad_school, undergrad_major, is_cross_major, is_second_attempt, target_school, target_major, initial_exam_total, initial_exam_politics, initial_exam_english, initial_exam_math, initial_exam_major, re_exam_score, timeline_json, books_json, tips, is_verified, status, view_count, like_count, collect_count) VALUES

-- 408上岸人的经验贴（已认证）
(3, NULL, '郑州大学', '软件工程', 0, 0, '浙江大学', '计算机科学与技术', 385.0, 72.0, 78.0, 125.0, 110.0, 88.5,
 '[{"phase":"基础阶段","startDate":"3月","endDate":"6月","description":"过完408四门课教材第一遍，英语每天背100个单词"},{"phase":"强化阶段","startDate":"7月","endDate":"9月","description":"王道408全套刷题，数学660+880，政治开始看徐涛"},{"phase":"冲刺阶段","startDate":"10月","endDate":"12月","description":"真题模拟，408真题刷3遍，英语作文模板整理"}]',
 '[{"subject":"数据结构","name":"王道数据结构","rating":5},{"subject":"计算机组成原理","name":"王道计组","rating":4},{"subject":"操作系统","name":"王道操作系统","rating":5},{"subject":"计算机网络","name":"王道计网","rating":4},{"subject":"数学","name":"李永乐660题","rating":5},{"subject":"政治","name":"肖秀荣1000题","rating":5}]',
 '408复习的核心是反复刷真题，尤其是计组和操作系统的综合题。建议暑假结束前完成第一轮全面复习，9月后集中刷真题。不要买太多辅导书，王道+真题足够。数学每天保持3小时以上做题量，保持手感很重要。',
 1, 1, 3256, 428, 156),

-- DB的经验贴（已认证）
(6, NULL, '河南大学', '计算机科学与技术', 0, 0, '哈尔滨工业大学', '计算机技术', 395.0, 75.0, 82.0, 130.0, 108.0, 90.0,
 '[{"phase":"基础阶段","startDate":"3月","endDate":"6月","description":"教材+王道基础，每天单词+数学"},{"phase":"强化阶段","startDate":"7月","endDate":"9月","description":"王道强化课+李永乐线代+英语真题"},{"phase":"冲刺阶段","startDate":"10月","endDate":"12月","description":"真题模拟+查漏补缺"}]',
 '[{"subject":"数据结构","name":"王道数据结构","rating":5},{"subject":"数学","name":"张宇30讲","rating":5},{"subject":"数学","name":"李永乐线代","rating":5}]',
 '哈工大计科复试很看重机试，建议提前刷LeetCode。初试408一定要拿到120+才有竞争力。政治不用开始太早，9月开始跟徐涛强化课即可。',
 1, 1, 2180, 312, 98),

-- 英语困难户的经验贴（未认证）
(4, NULL, '河南科技大学', '英语', 0, 1, '北京外国语大学', '英语笔译', 378.0, 70.0, 88.0, NULL, 115.0, 85.0,
 '[{"phase":"一战失败复盘","startDate":"3月","endDate":"5月","description":"分析一战失利原因，重新制定计划"},{"phase":"系统复习","startDate":"6月","endDate":"10月","description":"翻译基础+百科知识+政治+二外日语"},{"phase":"冲刺","startDate":"11月","endDate":"12月","description":"真题模拟+作文模板"}]',
 '[{"subject":"翻译基础","name":"张培基散文翻译","rating":5},{"subject":"百科","name":"中国文化读本","rating":4},{"subject":"政治","name":"肖秀荣精讲精练","rating":5}]',
 '二战最重要的是心态调整。不要因为一战失利就否定自己，分析清楚薄弱点再出发。北外笔译特别看重翻译基本功，每天至少练一篇英译汉和汉译英。',
 0, 1, 1890, 215, 67),

-- 政治背书狂的经验贴（未认证）
(5, NULL, '河南师范大学', '思想政治教育', 0, 0, '武汉大学', '马克思主义理论', 390.0, 82.0, 72.0, NULL, 120.0, 92.0,
 '[{"phase":"基础阶段","startDate":"7月","endDate":"8月","description":"徐涛强化课+精讲精练"},{"phase":"背诵阶段","startDate":"9月","endDate":"10月","description":"背诵手册+1000题二刷"},{"phase":"冲刺阶段","startDate":"11月","endDate":"12月","description":"肖八肖四+徐涛20题"}]',
 '[{"subject":"政治","name":"肖秀荣精讲精练","rating":5},{"subject":"政治","name":"徐涛背诵手册","rating":5},{"subject":"政治","name":"肖四肖八","rating":5}]',
 '政治不用太早开始，但一定要认真对待。1000题建议刷两遍，错题重点标记。肖四一到手立刻开始背，每天背2-3小时。考研政治大题基本都在肖四范围内。',
 0, 1, 1560, 198, 52);

-- 8.4 插入点赞数据（模拟真实互动）
INSERT INTO experience_post_like (experience_id, user_id) VALUES
(1, 4), (1, 5), (1, 6), (1, 7),
(2, 3), (2, 4), (2, 5),
(3, 3), (3, 5), (3, 6),
(4, 3), (4, 4);

-- 8.5 插入收藏数据
INSERT INTO experience_post_collect (experience_id, user_id) VALUES
(1, 4), (1, 5), (1, 7),
(2, 3), (2, 5),
(3, 5),
(4, 3), (4, 6);

-- 8.6 同步 like_count / collect_count
UPDATE experience_post SET like_count = (SELECT COUNT(*) FROM experience_post_like WHERE experience_id = experience_post.id);
UPDATE experience_post SET collect_count = (SELECT COUNT(*) FROM experience_post_collect WHERE experience_id = experience_post.id);
