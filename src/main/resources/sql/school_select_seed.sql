-- =============================================
-- Feature G: AI 智能择校引擎 - 建表 + 种子数据
-- =============================================

DROP TABLE IF EXISTS `recommendation_history`;
DROP TABLE IF EXISTS `admission_record`;
DROP TABLE IF EXISTS `school_major`;
DROP TABLE IF EXISTS `school_info`;

-- 1. 院校信息表
CREATE TABLE `school_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '院校名称',
  `level` varchar(30) NOT NULL COMMENT '院校层次: C9/985/211/DOUBLE_FIRST_CLASS/DOUBLE_NON/ORDINARY',
  `location` varchar(50) DEFAULT NULL COMMENT '所在地',
  `logo_url` varchar(255) DEFAULT NULL COMMENT '校徽URL',
  `website` varchar(255) DEFAULT NULL COMMENT '官网链接',
  `is_self_line` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否34所自划线院校',
  `avg_admission_score` int DEFAULT NULL COMMENT '往年录取均分(估测)',
  `min_admission_score` int DEFAULT NULL COMMENT '往年录取最低分',
  `hot_level` int NOT NULL DEFAULT '1' COMMENT '热度等级1-10',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_level` (`level`),
  KEY `idx_location` (`location`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考研院校信息表';

-- 2. 院校招生专业表
CREATE TABLE `school_major` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `school_id` bigint NOT NULL COMMENT '关联school_info.id',
  `major_name` varchar(100) NOT NULL COMMENT '专业名称',
  `major_code` varchar(20) DEFAULT NULL COMMENT '专业代码',
  `category` varchar(50) DEFAULT NULL COMMENT '学科门类',
  `admission_count` int DEFAULT NULL COMMENT '往年招生人数',
  `applicant_count` int DEFAULT NULL COMMENT '往年报考人数',
  `min_score` int DEFAULT NULL COMMENT '复试最低分',
  `avg_score` int DEFAULT NULL COMMENT '录取平均分',
  `tui_mian_ratio` decimal(5,2) DEFAULT NULL COMMENT '推免比例',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_school_id` (`school_id`),
  KEY `idx_major_name` (`major_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='院校招生专业表';

-- 3. 上岸录取记录表 (Feature D 基础)
CREATE TABLE `admission_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '上岸用户ID',
  `school_id` bigint NOT NULL COMMENT '录取院校ID',
  `major_name` varchar(100) NOT NULL COMMENT '录取专业',
  `undergrad_school` varchar(100) DEFAULT NULL COMMENT '本科院校',
  `undergrad_gpa` decimal(3,2) DEFAULT NULL COMMENT '本科GPA',
  `english_level` varchar(10) DEFAULT NULL COMMENT '英语水平: CET4/CET6/TEM4/TEM8/NONE',
  `prep_duration` int DEFAULT NULL COMMENT '备考时长(月)',
  `mock_exam_score` int DEFAULT NULL COMMENT '模考平均分',
  `exam_score_total` int DEFAULT NULL COMMENT '考研总分',
  `exam_score_politics` int DEFAULT NULL COMMENT '政治',
  `exam_score_english` int DEFAULT NULL COMMENT '英语',
  `exam_score_biz1` int DEFAULT NULL COMMENT '业务课一',
  `exam_score_biz2` int DEFAULT NULL COMMENT '业务课二',
  `is_verified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已认证',
  `verification_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  `verified_at` datetime DEFAULT NULL COMMENT '认证通过时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_school_id` (`school_id`),
  KEY `idx_verified` (`is_verified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上岸录取记录表';

-- 4. 择校推荐历史表
CREATE TABLE `recommendation_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `input_json` json NOT NULL COMMENT '用户输入快照',
  `result_json` json NOT NULL COMMENT '推荐结果快照',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='择校推荐历史表';

-- =============================================
-- 种子数据
-- =============================================

-- 院校数据 (12所各层次代表院校)
INSERT INTO `school_info` (name, level, location, is_self_line, avg_admission_score, min_admission_score, hot_level) VALUES
('清华大学',       'C9',                '北京', 1, 420, 395, 10),
('北京大学',       'C9',                '北京', 1, 418, 393, 10),
('浙江大学',       'C9',                '浙江', 1, 400, 375, 9),
('上海交通大学',   'C9',                '上海', 1, 405, 378, 9),
('华中科技大学',   '985',               '湖北', 1, 385, 358, 8),
('武汉大学',       '985',               '湖北', 1, 380, 352, 8),
('郑州大学',       '211',               '河南', 0, 350, 318, 7),
('南昌大学',       '211',               '江西', 0, 340, 308, 6),
('河南大学',       'DOUBLE_FIRST_CLASS', '河南', 0, 325, 298, 5),
('深圳大学',       'DOUBLE_NON',         '广东', 0, 355, 322, 7),
('杭州电子科技大学','DOUBLE_NON',        '浙江', 0, 338, 310, 6),
('重庆邮电大学',   'DOUBLE_NON',         '重庆', 0, 330, 300, 6),
('桂林电子科技大学','ORDINARY',          '广西', 0, 288, 265, 4),
('河南理工大学',   'ORDINARY',           '河南', 0, 295, 270, 4);

-- 专业数据 (计算机相关)
INSERT INTO `school_major` (school_id, major_name, major_code, category, admission_count, applicant_count, min_score, avg_score, tui_mian_ratio) VALUES
(1,  '计算机科学与技术', '081200', '工学', 12, 580, 400, 420, 0.65),
(1,  '软件工程',         '083500', '工学', 18, 480, 392, 412, 0.55),
(2,  '计算机科学与技术', '081200', '工学', 10, 620, 398, 418, 0.70),
(3,  '计算机科学与技术', '081200', '工学', 25, 750, 378, 400, 0.50),
(3,  '软件工程',         '083500', '工学', 30, 600, 370, 392, 0.45),
(5,  '计算机科学与技术', '081200', '工学', 38, 780, 360, 385, 0.35),
(5,  '网络空间安全',     '083900', '工学', 22, 380, 355, 378, 0.30),
(6,  '计算机科学与技术', '081200', '工学', 35, 720, 352, 378, 0.30),
(7,  '计算机科学与技术', '081200', '工学', 55, 950, 325, 350, 0.20),
(7,  '软件工程',         '083500', '工学', 42, 780, 318, 342, 0.18),
(8,  '计算机科学与技术', '081200', '工学', 40, 650, 315, 340, 0.15),
(9,  '计算机应用技术',   '081203', '工学', 28, 480, 305, 325, 0.12),
(10, '计算机科学与技术', '081200', '工学', 48, 1100, 340, 355, 0.18),
(11, '计算机科学与技术', '081200', '工学', 45, 700, 318, 338, 0.15),
(12, '计算机科学与技术', '081200', '工学', 35, 420, 300, 328, 0.10),
(13, '计算机科学与技术', '081200', '工学', 30, 200, 270, 288, 0.05),
(14, '计算机科学与技术', '081200', '工学', 40, 350, 278, 295, 0.05);

-- 上岸记录 (4条已认证样例)
INSERT INTO `admission_record` (user_id, school_id, major_name, undergrad_school, undergrad_gpa, english_level, prep_duration, mock_exam_score, exam_score_total, exam_score_politics, exam_score_english, exam_score_biz1, exam_score_biz2, is_verified, verification_status, verified_at) VALUES
(2, 7,  '计算机科学与技术', '河南理工大学',   3.10, 'CET6', 8,  340, 355, 68, 72, 115, 100, 1, 'APPROVED', '2025-06-15 10:00:00'),
(3, 5,  '软件工程',         '武汉科技大学',   3.50, 'CET6', 10, 375, 392, 72, 78, 125, 117, 1, 'APPROVED', '2025-06-20 14:00:00'),
(4, 3,  '计算机科学与技术', '南京邮电大学',   3.70, 'CET6', 12, 395, 412, 75, 82, 130, 125, 1, 'APPROVED', '2025-07-01 09:00:00'),
(5, 9,  '计算机应用技术',   '河南农业大学',   2.90, 'CET4', 6,  305, 318, 65, 60, 100, 93,  1, 'APPROVED', '2025-07-10 16:00:00');
