/*
 Navicat Premium Dump SQL

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 90300 (9.3.0)
 Source Host           : localhost:3306
 Source Schema         : kaoyan_forum

 Target Server Type    : MySQL
 Target Server Version : 90300 (9.3.0)
 File Encoding         : 65001

 Date: 18/04/2026 19:08:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for forum_board
-- ----------------------------
DROP TABLE IF EXISTS `forum_board`;
CREATE TABLE `forum_board`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '板块名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '板块简介',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '板块封面图',
  `post_count` bigint NOT NULL DEFAULT 0 COMMENT '冗余: 帖子总数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '讨论区板块表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_board
-- ----------------------------
INSERT INTO `forum_board` VALUES (1, '计算机考研(408)', '包含数据结构、计算机组成原理、操作系统和计算机网络讨论。', 'https://dummyimage.com/400x200/000/fff&text=CS+408', 1, 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `forum_board` VALUES (2, '考研数学', '数学一、数学二、数学三复习交流。', 'https://dummyimage.com/400x200/007bff/fff&text=Math', 1, 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `forum_board` VALUES (3, '考研英语', '英语一、英语二阅读、作文、翻译打卡与交流。', 'https://dummyimage.com/400x200/28a745/fff&text=English', 1, 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `forum_board` VALUES (4, '考研政治', '马原、毛中特、史纲、思修及当代时政讨论。', 'https://dummyimage.com/400x200/dc3545/fff&text=Politics', 1, 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');

-- ----------------------------
-- Table structure for forum_comment
-- ----------------------------
DROP TABLE IF EXISTS `forum_comment`;
CREATE TABLE `forum_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '所属帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论发布者ID',
  `reply_to_id` bigint NULL DEFAULT NULL COMMENT '回复的目标评论ID(顶层评论为空)',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2045435015252168706 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_comment
-- ----------------------------
INSERT INTO `forum_comment` VALUES (1, 1, 4, NULL, '感谢大佬分享，刚好处于迷茫期，太需要这份规划了！', 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `forum_comment` VALUES (2, 1, 5, NULL, '请问计组部分有必要看袁春风老师的教材吗？还是直接上王道？', 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `forum_comment` VALUES (3, 1, 3, 2, '如果现在时间充裕（5月份之前）可以看看课本打基础，时间紧的话直接吃透王道讲义即可。', 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `forum_comment` VALUES (4, 2, 2, NULL, '线代推荐去看看李永乐老师的强化班视频，非常系统，适合理清思路。', 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `forum_comment` VALUES (2045122750531506177, 1, 6, NULL, 'nb', 0, '2026-04-17 20:50:35', '2026-04-17 20:50:35');
INSERT INTO `forum_comment` VALUES (2045122779862274049, 1, 6, NULL, 'nb', 0, '2026-04-17 20:50:42', '2026-04-17 20:50:42');
INSERT INTO `forum_comment` VALUES (2045147261540786178, 2, 6, NULL, 'nb', 0, '2026-04-17 22:27:59', '2026-04-17 22:27:59');
INSERT INTO `forum_comment` VALUES (2045147371746123777, 5, 6, NULL, 'nb', 0, '2026-04-17 22:28:25', '2026-04-17 22:28:25');
INSERT INTO `forum_comment` VALUES (2045147410576990209, 5, 7, NULL, 'nb', 0, '2026-04-17 22:28:34', '2026-04-17 22:28:34');
INSERT INTO `forum_comment` VALUES (2045335555956387842, 5, 6, NULL, '？？', 0, '2026-04-18 10:56:12', '2026-04-18 10:56:12');
INSERT INTO `forum_comment` VALUES (2045335583622017026, 5, 6, NULL, '我的用户名呢？', 0, '2026-04-18 10:56:18', '2026-04-18 10:56:18');
INSERT INTO `forum_comment` VALUES (2045335628350074881, 1, 6, NULL, '用户名呢', 0, '2026-04-18 10:56:29', '2026-04-18 10:56:29');
INSERT INTO `forum_comment` VALUES (2045336144836669441, 1, 6, NULL, 'NB', 0, '2026-04-18 10:58:32', '2026-04-18 10:58:32');
INSERT INTO `forum_comment` VALUES (2045351143416119298, 5, 6, NULL, 'nb', 0, '2026-04-18 11:58:08', '2026-04-18 11:58:08');
INSERT INTO `forum_comment` VALUES (2045352270488518657, 5, 6, NULL, '牛逼', 0, '2026-04-18 12:02:37', '2026-04-18 12:02:37');
INSERT INTO `forum_comment` VALUES (2045376997667979265, 6, 6, NULL, 'nb', 0, '2026-04-18 13:40:52', '2026-04-18 13:40:52');
INSERT INTO `forum_comment` VALUES (2045381396712734721, 6, 6, NULL, 'nb', 0, '2026-04-18 13:58:21', '2026-04-18 13:58:21');
INSERT INTO `forum_comment` VALUES (2045384489915404289, 7, 6, NULL, 'nb', 0, '2026-04-18 14:10:38', '2026-04-18 14:10:38');
INSERT INTO `forum_comment` VALUES (2045384511004364802, 7, 6, 2045384489915404289, 'nb', 0, '2026-04-18 14:10:43', '2026-04-18 14:10:43');
INSERT INTO `forum_comment` VALUES (2045384648929857537, 6, 6, 2045376997667979265, 'nb', 0, '2026-04-18 14:11:16', '2026-04-18 14:11:16');
INSERT INTO `forum_comment` VALUES (2045385386498215938, 7, 6, 2045384489915404289, 'NB', 0, '2026-04-18 14:14:12', '2026-04-18 14:14:12');
INSERT INTO `forum_comment` VALUES (2045385414990123009, 7, 6, 2045384489915404289, '吃回复', 0, '2026-04-18 14:14:19', '2026-04-18 14:14:19');
INSERT INTO `forum_comment` VALUES (2045386841577771009, 7, 6, 2045384511004364802, 'NB', 0, '2026-04-18 14:19:59', '2026-04-18 14:19:59');
INSERT INTO `forum_comment` VALUES (2045390912309186561, 7, 6, 2045384511004364802, 'NB', 0, '2026-04-18 14:36:10', '2026-04-18 14:36:10');
INSERT INTO `forum_comment` VALUES (2045390931523293186, 7, 6, 2045390912309186561, 'NB', 0, '2026-04-18 14:36:14', '2026-04-18 14:36:14');
INSERT INTO `forum_comment` VALUES (2045396380482031617, 7, 6, 2045384489915404289, 'NB', 0, '2026-04-18 14:57:53', '2026-04-18 14:57:53');
INSERT INTO `forum_comment` VALUES (2045418841785257985, 7, 6, 2045384489915404289, 'nb', 0, '2026-04-18 16:27:09', '2026-04-18 16:27:09');
INSERT INTO `forum_comment` VALUES (2045435002438569986, 7, 6, 2045384511004364802, 'NB', 0, '2026-04-18 17:31:22', '2026-04-18 17:31:22');
INSERT INTO `forum_comment` VALUES (2045435015252168705, 7, 6, NULL, 'NB', 0, '2026-04-18 17:31:25', '2026-04-18 17:31:25');

-- ----------------------------
-- Table structure for forum_post
-- ----------------------------
DROP TABLE IF EXISTS `forum_post`;
CREATE TABLE `forum_post`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `board_id` bigint NOT NULL COMMENT '所属板块ID',
  `user_id` bigint NOT NULL COMMENT '发帖人用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子内容(富文本HTML)',
  `tags` json NULL COMMENT '标签列表',
  `attachment_urls` json NULL COMMENT '附件列表(OSS链接数组)',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT 0 COMMENT '评论数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_board_id`(`board_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_post
-- ----------------------------
INSERT INTO `forum_post` VALUES (1, 1, 3, '2026年408复习规划（个人向分享）', '<p>大家好，这是我的408复习规划，基础阶段建议看王道，强化阶段多刷真题...</p><p>附上了我的复习时间表，供大家参考！</p>', '[\"408\", \"复习规划\", \"经验贴\"]', '[\"https://oss.example.com/file/plan.pdf\"]', 1527, 3, 3, 0, '2026-04-17 20:44:07', '2026-04-18 19:01:49');
INSERT INTO `forum_post` VALUES (2, 2, 4, '求助！张宇18讲的线代部分看不懂怎么办？', '<p>特别是特征值和特征向量那一部分，做题完全没有思路，感觉非常吃力，求大佬指点迷津！</p>', '[\"数学一\", \"线性代数\", \"求助\"]', '[]', 330, 1, 1, 0, '2026-04-17 20:44:07', '2026-04-18 18:15:38');
INSERT INTO `forum_post` VALUES (3, 3, 5, '分享一份自己整理的英语大小作文万能句型', '<p>背熟这些句型，考试的时候直接套用，亲测有效！详见附件下载，完全免费分享给大家~</p>', '[\"英语一\", \"作文\", \"干货\"]', '[\"https://oss.example.com/file/english_writing.docx\"]', 2000, 2, 0, 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `forum_post` VALUES (4, 4, 2, '肖秀荣1000题刷题打卡集中贴（2026版）', '<p>欢迎大家每天在这里打卡自己的刷题进度，互相监督，共同进步！每天坚持100道题！</p>', '[\"政治\", \"打卡\", \"官方贴\"]', '[]', 501, 0, 0, 0, '2026-04-17 20:44:07', '2026-04-18 15:24:39');
INSERT INTO `forum_post` VALUES (5, 1, 6, '测试', '测试', '[]', NULL, 16, 1, 0, 0, '2026-04-17 22:28:18', '2026-04-18 18:30:46');
INSERT INTO `forum_post` VALUES (6, 2, 6, '测试', '测试', '[]', NULL, 11, 1, 0, 0, '2026-04-18 12:02:59', '2026-04-18 18:47:39');
INSERT INTO `forum_post` VALUES (7, 1, 6, '测试2', '测试2', '[]', NULL, 30, 1, 0, 0, '2026-04-18 12:11:10', '2026-04-18 18:28:23');
INSERT INTO `forum_post` VALUES (8, 1, 6, '测试', '测试', '[]', NULL, 19, 1, 0, 0, '2026-04-18 17:07:34', '2026-04-18 18:58:35');
INSERT INTO `forum_post` VALUES (9, 1, 6, '测试', '测试', '[]', NULL, 19, 1, 0, 0, '2026-04-18 18:04:30', '2026-04-18 19:02:55');

-- ----------------------------
-- Table structure for forum_post_collect
-- ----------------------------
DROP TABLE IF EXISTS `forum_post_collect`;
CREATE TABLE `forum_post_collect`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '收藏用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子收藏关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_post_collect
-- ----------------------------
INSERT INTO `forum_post_collect` VALUES (6, 1, 6, '2026-04-18 18:47:53');
INSERT INTO `forum_post_collect` VALUES (7, 8, 6, '2026-04-18 18:48:46');
INSERT INTO `forum_post_collect` VALUES (8, 9, 6, '2026-04-18 18:49:24');

-- ----------------------------
-- Table structure for forum_post_like
-- ----------------------------
DROP TABLE IF EXISTS `forum_post_like`;
CREATE TABLE `forum_post_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE COMMENT '防止重复点赞'
) ENGINE = InnoDB AUTO_INCREMENT = 2045443773990727682 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点赞关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_post_like
-- ----------------------------
INSERT INTO `forum_post_like` VALUES (1, 1, 4, '2026-04-17 20:44:08');
INSERT INTO `forum_post_like` VALUES (2, 1, 5, '2026-04-17 20:44:08');
INSERT INTO `forum_post_like` VALUES (3, 3, 3, '2026-04-17 20:44:08');
INSERT INTO `forum_post_like` VALUES (4, 3, 4, '2026-04-17 20:44:08');
INSERT INTO `forum_post_like` VALUES (2045122250729869314, 1, 6, '2026-04-17 20:48:36');
INSERT INTO `forum_post_like` VALUES (2045333316848168961, 5, 6, '2026-04-18 10:47:18');
INSERT INTO `forum_post_like` VALUES (2045428795762036738, 7, 6, '2026-04-18 17:06:42');
INSERT INTO `forum_post_like` VALUES (2045431325170618370, 8, 6, '2026-04-18 17:16:45');
INSERT INTO `forum_post_like` VALUES (2045443401733664770, 9, 6, '2026-04-18 18:04:44');
INSERT INTO `forum_post_like` VALUES (2045443684639469570, 6, 6, '2026-04-18 18:05:52');
INSERT INTO `forum_post_like` VALUES (2045443773990727681, 2, 6, '2026-04-18 18:06:13');

-- ----------------------------
-- Table structure for forum_report
-- ----------------------------
DROP TABLE IF EXISTS `forum_report`;
CREATE TABLE `forum_report`  (
  `id` bigint NOT NULL COMMENT '主键',
  `reporter_id` bigint NOT NULL COMMENT '举报人ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '举报目标类型: POST(帖子), COMMENT(评论), USER(用户)',
  `target_id` bigint NOT NULL COMMENT '举报目标的主键ID',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '举报原因描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态: 0-待处理, 1-已处理(封禁/删除), 2-已驳回(正常)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '处理时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '举报与审核记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of forum_report
-- ----------------------------

-- ----------------------------
-- Table structure for interaction_check_in
-- ----------------------------
DROP TABLE IF EXISTS `interaction_check_in`;
CREATE TABLE `interaction_check_in`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '打卡用户ID',
  `study_hours` int NOT NULL COMMENT '学习时长(小时)',
  `notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '打卡日记/笔记',
  `created_date` date NOT NULL COMMENT '打卡归属日期(YYYY-MM-DD)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '具体打卡时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id` ASC, `created_date` ASC) USING BTREE COMMENT '每天每人只能打卡一次'
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '每日学习打卡表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of interaction_check_in
-- ----------------------------
INSERT INTO `interaction_check_in` VALUES (1, 3, 8, '今天完成了数据结构前三章的课后题，错了不少，明天继续复盘错题。', '2026-04-16', '2026-04-17 20:44:08');
INSERT INTO `interaction_check_in` VALUES (2, 4, 10, '高数刷了50题，英语背了100个单词，充实的一天！继续保持！', '2026-04-16', '2026-04-17 20:44:08');
INSERT INTO `interaction_check_in` VALUES (3, 5, 6, '今天有点感冒，状态不好，只背了2个单元的政治，明天要补回来。', '2026-04-16', '2026-04-17 20:44:08');
INSERT INTO `interaction_check_in` VALUES (4, 3, 9, '计组真题第一套完成，大题还要加强，感觉指令系统那块还是不熟。', '2026-04-17', '2026-04-17 20:44:08');
INSERT INTO `interaction_check_in` VALUES (5, 6, 24, '状态极佳，暴力学习24小时', '2026-04-18', '2026-04-18 11:42:38');

-- ----------------------------
-- Table structure for interaction_points_log
-- ----------------------------
DROP TABLE IF EXISTS `interaction_points_log`;
CREATE TABLE `interaction_points_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points` int NOT NULL COMMENT '变动积分（正数增加）',
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：CHECK_IN/POST/LIKE/COMMENT/RESOURCE',
  `rel_id` bigint NULL DEFAULT NULL COMMENT '关联ID（打卡ID/帖子ID/评论ID）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分变动日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of interaction_points_log
-- ----------------------------
INSERT INTO `interaction_points_log` VALUES (1, 6, 3, 'CHECK_IN', NULL, '打卡+3分', '2026-04-18 11:42:38');

-- ----------------------------
-- Table structure for interaction_user_study
-- ----------------------------
DROP TABLE IF EXISTS `interaction_user_study`;
CREATE TABLE `interaction_user_study`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `continuous_days` int NOT NULL DEFAULT 0 COMMENT '连续打卡天数',
  `total_check_days` int NOT NULL DEFAULT 0 COMMENT '累计打卡天数',
  `last_check_date` date NULL DEFAULT NULL COMMENT '最后一次打卡日期',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户学习打卡统计' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of interaction_user_study
-- ----------------------------
INSERT INTO `interaction_user_study` VALUES (1, 6, 1, 1, '2026-04-18', '2026-04-18 11:42:38');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint NOT NULL COMMENT '发送者ID',
  `to_user_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '私信内容',
  `is_read` int NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_from_user`(`from_user_id` ASC) USING BTREE,
  INDEX `idx_to_user`(`to_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私信表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (1, 7, 6, '呼呼呼', 1, NULL);
INSERT INTO `message` VALUES (2, 7, 6, '学长在吗', 1, NULL);
INSERT INTO `message` VALUES (3, 7, 6, '呼呼呼', 1, NULL);
INSERT INTO `message` VALUES (4, 6, 7, '发生什么事了', 1, NULL);
INSERT INTO `message` VALUES (5, 7, 6, '呼呼呼学长在吗？', 1, NULL);
INSERT INTO `message` VALUES (6, 6, 7, '怎么了？', 1, NULL);
INSERT INTO `message` VALUES (7, 7, 6, '66', 1, NULL);
INSERT INTO `message` VALUES (8, 6, 7, 'nb', 1, NULL);
INSERT INTO `message` VALUES (9, 7, 6, '什么情况', 1, NULL);
INSERT INTO `message` VALUES (10, 6, 7, '为什么看不大', 1, NULL);
INSERT INTO `message` VALUES (11, 6, 7, '哭了', 1, NULL);
INSERT INTO `message` VALUES (12, 7, 6, '舒服', 1, NULL);
INSERT INTO `message` VALUES (13, 6, 7, '学长在吗？', 1, NULL);
INSERT INTO `message` VALUES (14, 7, 6, '好像还有点问题', 1, NULL);
INSERT INTO `message` VALUES (15, 6, 7, '你人呢？', 1, NULL);
INSERT INTO `message` VALUES (16, 7, 6, '信息提示好像还有些bug', 1, NULL);
INSERT INTO `message` VALUES (17, 7, 6, '明天再看看', 1, NULL);
INSERT INTO `message` VALUES (18, 6, 7, 'ok', 1, NULL);
INSERT INTO `message` VALUES (19, 6, 7, '我下了', 1, NULL);
INSERT INTO `message` VALUES (20, 7, 6, '拜拜', 1, NULL);
INSERT INTO `message` VALUES (21, 6, 7, '👋', 1, NULL);
INSERT INTO `message` VALUES (22, 7, 6, '为什么？', 1, NULL);
INSERT INTO `message` VALUES (23, 7, 6, '为什么？', 1, NULL);
INSERT INTO `message` VALUES (24, 7, 6, '为什么', 1, NULL);
INSERT INTO `message` VALUES (25, 7, 6, '为什么？', 1, NULL);
INSERT INTO `message` VALUES (26, 6, 7, '为啥', 1, NULL);
INSERT INTO `message` VALUES (27, 7, 6, '这是怎么回事', 1, NULL);
INSERT INTO `message` VALUES (28, 6, 7, '我哭了', 1, NULL);
INSERT INTO `message` VALUES (29, 6, 7, '泪目了', 1, NULL);
INSERT INTO `message` VALUES (30, 6, 7, '这TM', 1, NULL);
INSERT INTO `message` VALUES (31, 6, 7, '你他妈又反转了？', 1, NULL);
INSERT INTO `message` VALUES (32, 6, 7, '不是', 1, NULL);
INSERT INTO `message` VALUES (33, 6, 7, '我服了', 1, NULL);
INSERT INTO `message` VALUES (34, 6, 7, '就这吧', 1, NULL);
INSERT INTO `message` VALUES (35, 6, 7, '哭了', 1, NULL);
INSERT INTO `message` VALUES (36, 7, 6, '特娘的', 1, NULL);
INSERT INTO `message` VALUES (37, 6, 7, 'NB', 1, NULL);
INSERT INTO `message` VALUES (38, 7, 6, 'NB', 1, NULL);
INSERT INTO `message` VALUES (39, 6, 3, '学长在吗', 0, NULL);
INSERT INTO `message` VALUES (40, 6, 7, 'NB', 1, NULL);
INSERT INTO `message` VALUES (41, 6, 3, 'NB', 0, NULL);
INSERT INTO `message` VALUES (42, 6, 3, '喂喂喂', 0, NULL);
INSERT INTO `message` VALUES (43, 7, 6, '发什么什么事了？', 1, NULL);
INSERT INTO `message` VALUES (44, 6, 7, '收到', 1, NULL);

-- ----------------------------
-- Table structure for resource_file
-- ----------------------------
DROP TABLE IF EXISTS `resource_file`;
CREATE TABLE `resource_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `board_id` bigint NOT NULL COMMENT '所属板块ID',
  `uploader_id` bigint NOT NULL COMMENT '上传者用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资料标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资料描述',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OSS/对象存储链接',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件格式(pdf, docx等)',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小(字节)',
  `download_count` int NOT NULL DEFAULT 0 COMMENT '下载次数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_board_id`(`board_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学习资料表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resource_file
-- ----------------------------
INSERT INTO `resource_file` VALUES (1, 1, 2, '2010-2025年408真题及详细解析', '包含了近十几年408统考真题，排版非常清晰，可直接打印。', 'https://oss.example.com/resources/408_exams.pdf', 'pdf', 52428800, 350, 0, '2026-04-17 20:44:08');
INSERT INTO `resource_file` VALUES (2, 2, 2, '高等数学核心公式速记表', '冲刺阶段必备公式大全，建议打印下来每天早上背诵。', 'https://oss.example.com/resources/math_formula.pdf', 'pdf', 2048000, 1200, 0, '2026-04-17 20:44:08');
INSERT INTO `resource_file` VALUES (3, 3, 5, '英语长难句分析100例', '从历年真题中提取的经典长难句，带语法树分析。', 'https://oss.example.com/resources/english_sentences.docx', 'docx', 1048576, 480, 0, '2026-04-17 20:44:08');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名/昵称',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码(BCrypt)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱(可用于登录)',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号(可用于登录)',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER' COMMENT '角色: USER, MODERATOR, ADMIN',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像链接(存OSS链接)',
  `target_major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标专业',
  `points` int NOT NULL DEFAULT 0 COMMENT '总积分',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删, 1已删',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'Admin管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'admin@example.com', '13800000001', 'ADMIN', 'https://dummyimage.com/100x100/000/fff&text=Admin', NULL, 9999, 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `sys_user` VALUES (2, '考研高数版主', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'mod@example.com', '13800000002', 'MODERATOR', 'https://dummyimage.com/100x100/007bff/fff&text=Mod', '数学与应用数学', 5000, 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `sys_user` VALUES (3, '408上岸人', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'user1@example.com', '13800000003', 'USER', 'https://dummyimage.com/100x100/28a745/fff&text=U1', '计算机科学与技术', 150, 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `sys_user` VALUES (4, '英语困难户', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'user2@example.com', '13800000004', 'USER', 'https://dummyimage.com/100x100/dc3545/fff&text=U2', '金融学', 200, 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `sys_user` VALUES (5, '政治背书狂', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'user3@example.com', '13800000005', 'USER', 'https://dummyimage.com/100x100/ffc107/000&text=U3', '法学', 300, 0, '2026-04-17 20:44:07', '2026-04-17 20:44:07');
INSERT INTO `sys_user` VALUES (6, 'DB', '$2a$10$PYWXnjNRZ0qPWpYwQdU2aO.VKorN3YQyAhoof8D4E.vp5DVNWAdsu', '1@qq.com', '15713775462', 'USER', '', '408', 3, 0, '2026-04-17 20:46:42', '2026-04-18 11:42:38');
INSERT INTO `sys_user` VALUES (7, '吴东博', '$2a$10$DMcxnrmr8prWhiQxc5UmAuvtd0nVVv6gk.ASAvXP8B.HWIKEBHCTm', '2@qq.com', NULL, 'USER', NULL, NULL, 0, 0, '2026-04-17 22:01:13', '2026-04-17 22:01:13');

-- ----------------------------
-- Table structure for sys_user_stats
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_stats`;
CREATE TABLE `sys_user_stats`  (
  `user_id` bigint NOT NULL COMMENT '关联 sys_user 表的主键 ID',
  `post_count` int NOT NULL DEFAULT 0 COMMENT '发帖总数',
  `like_received_count` int NOT NULL DEFAULT 0 COMMENT '获得的获赞总数',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户数据统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_stats
-- ----------------------------
INSERT INTO `sys_user_stats` VALUES (2, 1, 0, '2026-04-18 18:15:26');
INSERT INTO `sys_user_stats` VALUES (3, 1, 3, '2026-04-18 18:15:26');
INSERT INTO `sys_user_stats` VALUES (4, 1, 1, '2026-04-18 18:15:26');
INSERT INTO `sys_user_stats` VALUES (5, 1, 2, '2026-04-18 18:15:26');
INSERT INTO `sys_user_stats` VALUES (6, 5, 5, '2026-04-18 18:15:26');

-- ----------------------------
-- Triggers structure for table forum_post
-- ----------------------------
DROP TRIGGER IF EXISTS `trg_forum_post_after_insert`;
delimiter ;;
CREATE TRIGGER `trg_forum_post_after_insert` AFTER INSERT ON `forum_post` FOR EACH ROW BEGIN
    INSERT INTO sys_user_stats (user_id, post_count, like_received_count)
    VALUES (NEW.user_id, 1, 0)
    ON DUPLICATE KEY UPDATE
        post_count = post_count + 1;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
