-- MySQL dump 10.13  Distrib 9.6.0, for Linux (x86_64)
--
-- Host: localhost    Database: kaoyan_forum
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `kaoyan_forum`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `kaoyan_forum` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `kaoyan_forum`;

--
-- Table structure for table `chat_group`
--

DROP TABLE IF EXISTS `chat_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '群名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '群简介',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '群头像URL',
  `owner_id` bigint NOT NULL COMMENT '群主用户ID',
  `member_count` int NOT NULL DEFAULT '1' COMMENT '冗余：当前群成员数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_owner_id` (`owner_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='群组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_group`
--

LOCK TABLES `chat_group` WRITE;
/*!40000 ALTER TABLE `chat_group` DISABLE KEYS */;
INSERT INTO `chat_group` VALUES (1,'408考研交流群','一起刷题讨论408',NULL,6,4,0,'2026-05-20 12:34:35','2026-05-20 12:34:35'),(2,'测试','',NULL,11,1,0,'2026-05-20 14:36:28','2026-05-20 14:36:28'),(3,'测试2','',NULL,8,1,0,'2026-05-20 15:16:22','2026-05-20 15:16:22');
/*!40000 ALTER TABLE `chat_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_board`
--

DROP TABLE IF EXISTS `forum_board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_board` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '板块名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '板块简介',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '板块封面图',
  `post_count` bigint NOT NULL DEFAULT '0' COMMENT '冗余: 帖子总数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='讨论区板块表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_board`
--

LOCK TABLES `forum_board` WRITE;
/*!40000 ALTER TABLE `forum_board` DISABLE KEYS */;
INSERT INTO `forum_board` VALUES (1,'计算机考研(408)','包含数据结构、计算机组成原理、操作系统和计算机网络讨论。','https://dummyimage.com/400x200/000/fff&text=CS+408',1,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(2,'考研数学','数学一、数学二、数学三复习交流。','https://dummyimage.com/400x200/007bff/fff&text=Math',1,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(3,'考研英语','英语一、英语二阅读、作文、翻译打卡与交流。','https://dummyimage.com/400x200/28a745/fff&text=English',1,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(4,'考研政治','马原、毛中特、史纲、思修及当代时政讨论。','https://dummyimage.com/400x200/dc3545/fff&text=Politics',1,0,'2026-04-17 20:44:07','2026-04-17 20:44:07');
/*!40000 ALTER TABLE `forum_board` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_comment`
--

DROP TABLE IF EXISTS `forum_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '所属帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论发布者ID',
  `reply_to_id` bigint DEFAULT NULL COMMENT '回复的目标评论ID(顶层评论为空)',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_post_id` (`post_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2056945882768285698 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='帖子评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_comment`
--

LOCK TABLES `forum_comment` WRITE;
/*!40000 ALTER TABLE `forum_comment` DISABLE KEYS */;
INSERT INTO `forum_comment` VALUES (1,1,4,NULL,'感谢大佬分享，刚好处于迷茫期，太需要这份规划了！',0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(2,1,5,NULL,'请问计组部分有必要看袁春风老师的教材吗？还是直接上王道？',0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(3,1,3,2,'如果现在时间充裕（5月份之前）可以看看课本打基础，时间紧的话直接吃透王道讲义即可。',0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(4,2,2,NULL,'线代推荐去看看李永乐老师的强化班视频，非常系统，适合理清思路。',0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(2045122750531506177,1,6,NULL,'nb',0,'2026-04-17 20:50:35','2026-04-17 20:50:35'),(2045122779862274049,1,6,NULL,'nb',0,'2026-04-17 20:50:42','2026-04-17 20:50:42'),(2045147261540786178,2,6,NULL,'nb',0,'2026-04-17 22:27:59','2026-04-17 22:27:59'),(2045147371746123777,5,6,NULL,'nb',0,'2026-04-17 22:28:25','2026-04-17 22:28:25'),(2045147410576990209,5,7,NULL,'nb',0,'2026-04-17 22:28:34','2026-04-17 22:28:34'),(2045335555956387842,5,6,NULL,'？？',0,'2026-04-18 10:56:12','2026-04-18 10:56:12'),(2045335583622017026,5,6,NULL,'我的用户名呢？',0,'2026-04-18 10:56:18','2026-04-18 10:56:18'),(2045335628350074881,1,6,NULL,'用户名呢',0,'2026-04-18 10:56:29','2026-04-18 10:56:29'),(2045336144836669441,1,6,NULL,'NB',0,'2026-04-18 10:58:32','2026-04-18 10:58:32'),(2045351143416119298,5,6,NULL,'nb',0,'2026-04-18 11:58:08','2026-04-18 11:58:08'),(2045352270488518657,5,6,NULL,'牛逼',0,'2026-04-18 12:02:37','2026-04-18 12:02:37'),(2045376997667979265,6,6,NULL,'nb',0,'2026-04-18 13:40:52','2026-04-18 13:40:52'),(2045381396712734721,6,6,NULL,'nb',0,'2026-04-18 13:58:21','2026-04-18 13:58:21'),(2045384489915404289,7,6,NULL,'nb',0,'2026-04-18 14:10:38','2026-04-18 14:10:38'),(2045384511004364802,7,6,2045384489915404289,'nb',0,'2026-04-18 14:10:43','2026-04-18 14:10:43'),(2045384648929857537,6,6,2045376997667979265,'nb',0,'2026-04-18 14:11:16','2026-04-18 14:11:16'),(2045385386498215938,7,6,2045384489915404289,'NB',0,'2026-04-18 14:14:12','2026-04-18 14:14:12'),(2045385414990123009,7,6,2045384489915404289,'吃回复',0,'2026-04-18 14:14:19','2026-04-18 14:14:19'),(2045386841577771009,7,6,2045384511004364802,'NB',0,'2026-04-18 14:19:59','2026-04-18 14:19:59'),(2045390912309186561,7,6,2045384511004364802,'NB',0,'2026-04-18 14:36:10','2026-04-18 14:36:10'),(2045390931523293186,7,6,2045390912309186561,'NB',0,'2026-04-18 14:36:14','2026-04-18 14:36:14'),(2045396380482031617,7,6,2045384489915404289,'NB',0,'2026-04-18 14:57:53','2026-04-18 14:57:53'),(2045418841785257985,7,6,2045384489915404289,'nb',0,'2026-04-18 16:27:09','2026-04-18 16:27:09'),(2045435002438569986,7,6,2045384511004364802,'NB',0,'2026-04-18 17:31:22','2026-04-18 17:31:22'),(2045435015252168705,7,6,NULL,'NB',0,'2026-04-18 17:31:25','2026-04-18 17:31:25'),(2056943796508868609,9,8,NULL,'',0,'2026-05-20 11:43:12','2026-05-20 11:43:12'),(2056945882768285697,1,10,NULL,'*********',0,'2026-05-20 11:51:29','2026-05-20 11:51:29');
/*!40000 ALTER TABLE `forum_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_post`
--

DROP TABLE IF EXISTS `forum_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_post` (
  `id` int NOT NULL AUTO_INCREMENT,
  `board_id` bigint NOT NULL COMMENT '所属板块ID',
  `user_id` bigint NOT NULL COMMENT '发帖人用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子内容(富文本HTML)',
  `tags` json DEFAULT NULL COMMENT '标签列表',
  `attachment_urls` json DEFAULT NULL COMMENT '附件列表(OSS链接数组)',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览量',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_board_id` (`board_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='帖子主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_post`
--

LOCK TABLES `forum_post` WRITE;
/*!40000 ALTER TABLE `forum_post` DISABLE KEYS */;
INSERT INTO `forum_post` VALUES (1,1,3,'2026年408复习规划（个人向分享）','<p>大家好，这是我的408复习规划，基础阶段建议看王道，强化阶段多刷真题...</p><p>附上了我的复习时间表，供大家参考！</p>','[\"408\", \"复习规划\", \"经验贴\"]','[\"https://oss.example.com/file/plan.pdf\"]',1527,3,3,0,'2026-04-17 20:44:07','2026-04-18 19:01:49'),(2,2,4,'求助！张宇18讲的线代部分看不懂怎么办？','<p>特别是特征值和特征向量那一部分，做题完全没有思路，感觉非常吃力，求大佬指点迷津！</p>','[\"数学一\", \"线性代数\", \"求助\"]','[]',330,1,1,0,'2026-04-17 20:44:07','2026-04-18 18:15:38'),(3,3,5,'分享一份自己整理的英语大小作文万能句型','<p>背熟这些句型，考试的时候直接套用，亲测有效！详见附件下载，完全免费分享给大家~</p>','[\"英语一\", \"作文\", \"干货\"]','[\"https://oss.example.com/file/english_writing.docx\"]',2000,2,0,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(4,4,2,'肖秀荣1000题刷题打卡集中贴（2026版）','<p>欢迎大家每天在这里打卡自己的刷题进度，互相监督，共同进步！每天坚持100道题！</p>','[\"政治\", \"打卡\", \"官方贴\"]','[]',501,0,0,0,'2026-04-17 20:44:07','2026-04-18 15:24:39'),(5,1,6,'测试','测试','[]',NULL,17,2,0,0,'2026-04-17 22:28:18','2026-05-20 15:30:14'),(6,2,6,'测试','测试','[]',NULL,11,1,0,0,'2026-04-18 12:02:59','2026-04-18 18:47:39'),(7,1,6,'测试2','测试2','[]',NULL,30,1,0,0,'2026-04-18 12:11:10','2026-04-18 18:28:23'),(8,1,6,'测试','测试','[]',NULL,19,1,0,0,'2026-04-18 17:07:34','2026-04-18 18:58:35'),(9,1,6,'测试','测试','[]',NULL,22,2,0,0,'2026-04-18 18:04:30','2026-05-20 15:30:03'),(10,1,10,'这是一条***广告***信息','************快来',NULL,NULL,3,0,0,0,'2026-05-20 11:51:29','2026-05-20 15:30:19');
/*!40000 ALTER TABLE `forum_post` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_forum_post_after_insert` AFTER INSERT ON `forum_post` FOR EACH ROW BEGIN
    INSERT INTO sys_user_stats (user_id, post_count, like_received_count)
    VALUES (NEW.user_id, 1, 0)
    ON DUPLICATE KEY UPDATE
        post_count = post_count + 1;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `forum_post_collect`
--

DROP TABLE IF EXISTS `forum_post_collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_post_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '收藏用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='帖子收藏关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_post_collect`
--

LOCK TABLES `forum_post_collect` WRITE;
/*!40000 ALTER TABLE `forum_post_collect` DISABLE KEYS */;
INSERT INTO `forum_post_collect` VALUES (6,1,6,'2026-04-18 18:47:53'),(7,8,6,'2026-04-18 18:48:46'),(8,9,6,'2026-04-18 18:49:24');
/*!40000 ALTER TABLE `forum_post_collect` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_post_like`
--

DROP TABLE IF EXISTS `forum_post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`) USING BTREE COMMENT '防止重复点赞'
) ENGINE=InnoDB AUTO_INCREMENT=2057000932500729859 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='帖子点赞关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_post_like`
--

LOCK TABLES `forum_post_like` WRITE;
/*!40000 ALTER TABLE `forum_post_like` DISABLE KEYS */;
INSERT INTO `forum_post_like` VALUES (1,1,4,'2026-04-17 20:44:08'),(2,1,5,'2026-04-17 20:44:08'),(3,3,3,'2026-04-17 20:44:08'),(4,3,4,'2026-04-17 20:44:08'),(2045122250729869314,1,6,'2026-04-17 20:48:36'),(2045333316848168961,5,6,'2026-04-18 10:47:18'),(2045428795762036738,7,6,'2026-04-18 17:06:42'),(2045431325170618370,8,6,'2026-04-18 17:16:45'),(2045443401733664770,9,6,'2026-04-18 18:04:44'),(2045443684639469570,6,6,'2026-04-18 18:05:52'),(2045443773990727681,2,6,'2026-04-18 18:06:13'),(2056943694058799106,9,8,'2026-05-20 11:42:47'),(2057000932500729858,5,8,'2026-05-20 15:30:14');
/*!40000 ALTER TABLE `forum_post_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_report`
--

DROP TABLE IF EXISTS `forum_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_report` (
  `id` bigint NOT NULL COMMENT '主键',
  `reporter_id` bigint NOT NULL COMMENT '举报人ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '举报目标类型: POST(帖子), COMMENT(评论), USER(用户)',
  `target_id` bigint NOT NULL COMMENT '举报目标的主键ID',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '举报原因描述',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态: 0-待处理, 1-已处理(封禁/删除), 2-已驳回(正常)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '处理时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='举报与审核记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_report`
--

LOCK TABLES `forum_report` WRITE;
/*!40000 ALTER TABLE `forum_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `forum_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_member`
--

DROP TABLE IF EXISTS `group_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `user_id` bigint NOT NULL COMMENT '成员用户ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MEMBER' COMMENT '群内角色：OWNER(群主), ADMIN(管理员), MEMBER(普通成员)',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除（退群）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入群时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`) USING BTREE COMMENT '防止重复加群',
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='群成员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_member`
--

LOCK TABLES `group_member` WRITE;
/*!40000 ALTER TABLE `group_member` DISABLE KEYS */;
INSERT INTO `group_member` VALUES (1,1,6,'OWNER',0,'2026-05-20 12:34:35','2026-05-20 12:34:35'),(2,1,7,'MEMBER',0,'2026-05-20 12:34:35','2026-05-20 12:34:35'),(3,2,11,'OWNER',0,'2026-05-20 14:36:28','2026-05-20 14:36:28'),(4,3,8,'OWNER',0,'2026-05-20 15:16:22','2026-05-20 15:16:22'),(5,1,11,'MEMBER',0,'2026-05-20 15:20:33','2026-05-20 15:20:33'),(6,1,8,'MEMBER',0,'2026-05-20 15:20:42','2026-05-20 15:20:42');
/*!40000 ALTER TABLE `group_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_message`
--

DROP TABLE IF EXISTS `group_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL COMMENT '所属群组ID',
  `user_id` bigint NOT NULL COMMENT '发送者用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除（撤回）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_group_id` (`group_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='群聊消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_message`
--

LOCK TABLES `group_message` WRITE;
/*!40000 ALTER TABLE `group_message` DISABLE KEYS */;
INSERT INTO `group_message` VALUES (1,1,6,'大家好，欢迎加入408考研群！',0,'2026-05-20 12:38:05','2026-05-20 12:38:05'),(2,1,7,'谢谢群主！',0,'2026-05-20 12:38:05','2026-05-20 12:38:05'),(3,1,7,'*********',0,'2026-05-20 12:38:05','2026-05-20 12:38:05'),(4,2,11,'hello',0,'2026-05-20 14:36:41','2026-05-20 14:36:41'),(5,2,11,'有人吗',0,'2026-05-20 14:36:50','2026-05-20 14:36:50'),(6,2,11,'hello',0,'2026-05-20 14:38:48','2026-05-20 14:38:48'),(7,2,11,'hello',0,'2026-05-20 14:52:05','2026-05-20 14:52:05'),(8,2,11,'啥情况',0,'2026-05-20 14:52:15','2026-05-20 14:52:15'),(9,2,11,'你好',0,'2026-05-20 14:52:18','2026-05-20 14:52:18'),(10,2,11,'？？？？？？？',0,'2026-05-20 14:52:25','2026-05-20 14:52:25'),(11,2,11,'hello',0,'2026-05-20 15:01:40','2026-05-20 15:01:40'),(12,2,11,'hello',0,'2026-05-20 15:01:45','2026-05-20 15:01:45'),(13,2,11,'hello',0,'2026-05-20 15:01:48','2026-05-20 15:01:48'),(14,2,11,'hello',0,'2026-05-20 15:01:54','2026-05-20 15:01:54'),(15,2,11,'hello',0,'2026-05-20 15:01:56','2026-05-20 15:01:56'),(16,2,11,'hello',0,'2026-05-20 15:03:36','2026-05-20 15:03:36'),(17,2,11,'hello',0,'2026-05-20 15:03:43','2026-05-20 15:03:43'),(18,2,11,'hello',0,'2026-05-20 15:03:49','2026-05-20 15:03:49'),(19,2,11,'hello',0,'2026-05-20 15:05:20','2026-05-20 15:05:20'),(20,2,11,'hello\'',0,'2026-05-20 15:05:32','2026-05-20 15:05:32'),(21,2,11,'hello',0,'2026-05-20 15:09:32','2026-05-20 15:09:32'),(22,2,11,'hello',0,'2026-05-20 15:09:40','2026-05-20 15:09:40'),(23,2,11,'?',0,'2026-05-20 15:09:45','2026-05-20 15:09:45'),(24,2,11,'怎么还是这样子？？？？？？？？？？？？？？',0,'2026-05-20 15:09:51','2026-05-20 15:09:51'),(25,2,11,'你好',0,'2026-05-20 15:13:33','2026-05-20 15:13:33'),(26,2,11,'？？？？？？',0,'2026-05-20 15:13:38','2026-05-20 15:13:38'),(27,2,11,'日志呢？',0,'2026-05-20 15:13:41','2026-05-20 15:13:41'),(28,2,11,'日志呢?',0,'2026-05-20 15:13:57','2026-05-20 15:13:57'),(29,2,11,'????????????????',0,'2026-05-20 15:13:59','2026-05-20 15:13:59'),(30,3,8,'？？？？？？？？？',0,'2026-05-20 15:16:31','2026-05-20 15:16:31'),(31,3,8,'？？？',0,'2026-05-20 15:16:37','2026-05-20 15:16:37'),(32,3,8,'啥意思啊',0,'2026-05-20 15:16:39','2026-05-20 15:16:39'),(33,3,8,'？',0,'2026-05-20 15:17:00','2026-05-20 15:17:00'),(34,3,8,'为啥啊',0,'2026-05-20 15:17:03','2026-05-20 15:17:03'),(35,3,8,'？？？？？？？？',0,'2026-05-20 15:17:18','2026-05-20 15:17:18'),(36,3,8,'为啥啊',0,'2026-05-20 15:17:20','2026-05-20 15:17:20'),(37,1,8,'谢谢群主',0,'2026-05-20 15:20:50','2026-05-20 15:20:50'),(38,1,11,'这是什么情况？？？？？？？？？？？',0,'2026-05-20 15:20:59','2026-05-20 15:20:59');
/*!40000 ALTER TABLE `group_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interaction_check_in`
--

DROP TABLE IF EXISTS `interaction_check_in`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interaction_check_in` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '打卡用户ID',
  `study_hours` int NOT NULL COMMENT '学习时长(小时)',
  `notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打卡日记/笔记',
  `created_date` date NOT NULL COMMENT '打卡归属日期(YYYY-MM-DD)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '具体打卡时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_date` (`user_id`,`created_date`) USING BTREE COMMENT '每天每人只能打卡一次'
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='每日学习打卡表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interaction_check_in`
--

LOCK TABLES `interaction_check_in` WRITE;
/*!40000 ALTER TABLE `interaction_check_in` DISABLE KEYS */;
INSERT INTO `interaction_check_in` VALUES (1,3,8,'今天完成了数据结构前三章的课后题，错了不少，明天继续复盘错题。','2026-04-16','2026-04-17 20:44:08'),(2,4,10,'高数刷了50题，英语背了100个单词，充实的一天！继续保持！','2026-04-16','2026-04-17 20:44:08'),(3,5,6,'今天有点感冒，状态不好，只背了2个单元的政治，明天要补回来。','2026-04-16','2026-04-17 20:44:08'),(4,3,9,'计组真题第一套完成，大题还要加强，感觉指令系统那块还是不熟。','2026-04-17','2026-04-17 20:44:08'),(5,6,24,'状态极佳，暴力学习24小时','2026-04-18','2026-04-18 11:42:38');
/*!40000 ALTER TABLE `interaction_check_in` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interaction_points_log`
--

DROP TABLE IF EXISTS `interaction_points_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interaction_points_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points` int NOT NULL COMMENT '变动积分（正数增加）',
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：CHECK_IN/POST/LIKE/COMMENT/RESOURCE',
  `rel_id` bigint DEFAULT NULL COMMENT '关联ID（打卡ID/帖子ID/评论ID）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='积分变动日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interaction_points_log`
--

LOCK TABLES `interaction_points_log` WRITE;
/*!40000 ALTER TABLE `interaction_points_log` DISABLE KEYS */;
INSERT INTO `interaction_points_log` VALUES (1,6,3,'CHECK_IN',NULL,'打卡+3分','2026-04-18 11:42:38');
/*!40000 ALTER TABLE `interaction_points_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interaction_user_study`
--

DROP TABLE IF EXISTS `interaction_user_study`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interaction_user_study` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `continuous_days` int NOT NULL DEFAULT '0' COMMENT '连续打卡天数',
  `total_check_days` int NOT NULL DEFAULT '0' COMMENT '累计打卡天数',
  `last_check_date` date DEFAULT NULL COMMENT '最后一次打卡日期',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户学习打卡统计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interaction_user_study`
--

LOCK TABLES `interaction_user_study` WRITE;
/*!40000 ALTER TABLE `interaction_user_study` DISABLE KEYS */;
INSERT INTO `interaction_user_study` VALUES (1,6,1,1,'2026-04-18','2026-04-18 11:42:38');
/*!40000 ALTER TABLE `interaction_user_study` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint NOT NULL COMMENT '发送者ID',
  `to_user_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '私信内容',
  `is_read` int DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_from_user` (`from_user_id`) USING BTREE,
  KEY `idx_to_user` (`to_user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='私信表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (1,7,6,'呼呼呼',1,NULL),(2,7,6,'学长在吗',1,NULL),(3,7,6,'呼呼呼',1,NULL),(4,6,7,'发生什么事了',1,NULL),(5,7,6,'呼呼呼学长在吗？',1,NULL),(6,6,7,'怎么了？',1,NULL),(7,7,6,'66',1,NULL),(8,6,7,'nb',1,NULL),(9,7,6,'什么情况',1,NULL),(10,6,7,'为什么看不大',1,NULL),(11,6,7,'哭了',1,NULL),(12,7,6,'舒服',1,NULL),(13,6,7,'学长在吗？',1,NULL),(14,7,6,'好像还有点问题',1,NULL),(15,6,7,'你人呢？',1,NULL),(16,7,6,'信息提示好像还有些bug',1,NULL),(17,7,6,'明天再看看',1,NULL),(18,6,7,'ok',1,NULL),(19,6,7,'我下了',1,NULL),(20,7,6,'拜拜',1,NULL),(21,6,7,'👋',1,NULL),(22,7,6,'为什么？',1,NULL),(23,7,6,'为什么？',1,NULL),(24,7,6,'为什么',1,NULL),(25,7,6,'为什么？',1,NULL),(26,6,7,'为啥',1,NULL),(27,7,6,'这是怎么回事',1,NULL),(28,6,7,'我哭了',1,NULL),(29,6,7,'泪目了',1,NULL),(30,6,7,'这TM',1,NULL),(31,6,7,'你他妈又反转了？',1,NULL),(32,6,7,'不是',1,NULL),(33,6,7,'我服了',1,NULL),(34,6,7,'就这吧',1,NULL),(35,6,7,'哭了',1,NULL),(36,7,6,'特娘的',1,NULL),(37,6,7,'NB',1,NULL),(38,7,6,'NB',1,NULL),(39,6,3,'学长在吗',0,NULL),(40,6,7,'NB',1,NULL),(41,6,3,'NB',0,NULL),(42,6,3,'喂喂喂',0,NULL),(43,7,6,'发什么什么事了？',1,NULL),(44,6,7,'收到',1,NULL),(45,10,7,'*********出售',0,NULL);
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource_file`
--

DROP TABLE IF EXISTS `resource_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resource_file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `board_id` bigint NOT NULL COMMENT '所属板块ID',
  `uploader_id` bigint NOT NULL COMMENT '上传者用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资料标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '资料描述',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OSS/对象存储链接',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件格式(pdf, docx等)',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `download_count` int NOT NULL DEFAULT '0' COMMENT '下载次数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_board_id` (`board_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学习资料表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resource_file`
--

LOCK TABLES `resource_file` WRITE;
/*!40000 ALTER TABLE `resource_file` DISABLE KEYS */;
INSERT INTO `resource_file` VALUES (1,1,2,'2010-2025年408真题及详细解析','包含了近十几年408统考真题，排版非常清晰，可直接打印。','https://oss.example.com/resources/408_exams.pdf','pdf',52428800,350,0,'2026-04-17 20:44:08'),(2,2,2,'高等数学核心公式速记表','冲刺阶段必备公式大全，建议打印下来每天早上背诵。','https://oss.example.com/resources/math_formula.pdf','pdf',2048000,1200,0,'2026-04-17 20:44:08'),(3,3,5,'英语长难句分析100例','从历年真题中提取的经典长难句，带语法树分析。','https://oss.example.com/resources/english_sentences.docx','docx',1048576,480,0,'2026-04-17 20:44:08');
/*!40000 ALTER TABLE `resource_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名/昵称',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码(BCrypt)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱(可用于登录)',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号(可用于登录)',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER' COMMENT '角色: USER, MODERATOR, ADMIN',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像链接(存OSS链接)',
  `target_major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '目标专业',
  `points` int NOT NULL DEFAULT '0' COMMENT '总积分',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0未删, 1已删',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_email` (`email`) USING BTREE,
  UNIQUE KEY `uk_phone` (`phone`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'Admin管理员','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','admin@example.com','13800000001','ADMIN','https://dummyimage.com/100x100/000/fff&text=Admin',NULL,9999,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(2,'考研高数版主','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','mod@example.com','13800000002','MODERATOR','https://dummyimage.com/100x100/007bff/fff&text=Mod','数学与应用数学',5000,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(3,'408上岸人','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','user1@example.com','13800000003','USER','https://dummyimage.com/100x100/28a745/fff&text=U1','计算机科学与技术',150,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(4,'英语困难户','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','user2@example.com','13800000004','USER','https://dummyimage.com/100x100/dc3545/fff&text=U2','金融学',200,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(5,'政治背书狂','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','user3@example.com','13800000005','USER','https://dummyimage.com/100x100/ffc107/000&text=U3','法学',300,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(6,'DB','$2a$10$PYWXnjNRZ0qPWpYwQdU2aO.VKorN3YQyAhoof8D4E.vp5DVNWAdsu','1@qq.com','15713775462','USER','','408',3,0,'2026-04-17 20:46:42','2026-04-18 11:42:38'),(7,'吴东博','$2a$10$DMcxnrmr8prWhiQxc5UmAuvtd0nVVv6gk.ASAvXP8B.HWIKEBHCTm','2@qq.com',NULL,'USER',NULL,NULL,0,0,'2026-04-17 22:01:13','2026-04-17 22:01:13'),(8,'wdb','$2a$10$OSRZaL2uNGWAgpWspS.W4uyL30h9kae4btbzuJ8ro2XCiSaDsLDYC','123@qq.com',NULL,'USER',NULL,NULL,0,0,'2026-05-20 11:42:11','2026-05-20 11:42:11'),(9,'swtest','$2a$10$r6ZOziJTHeLeFoHH4zw.ueO3GqVij9IAC/DB21Q6OqwLxtWqC9ec.','swtest@test.com',NULL,'USER',NULL,NULL,0,0,'2026-05-20 11:50:26','2026-05-20 11:50:26'),(10,'swtest2','$2a$10$aRLrJm/wUBqhPxsU6wR2luz5sxFl7OekZIbQVf6hQV1m.HmBD6SN6','swtest2@test.com',NULL,'USER',NULL,NULL,0,0,'2026-05-20 11:50:54','2026-05-20 11:50:54'),(11,'wdb2','$2a$10$DZ73yHUtXMCtOpl3AihWy.Ho/4Ju8aLeGykEUSaHCk7xN0cQW4cXy','1234@qq.com',NULL,'USER',NULL,NULL,0,0,'2026-05-20 14:35:40','2026-05-20 14:35:40');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_stats`
--

DROP TABLE IF EXISTS `sys_user_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_stats` (
  `user_id` bigint NOT NULL COMMENT '关联 sys_user 表的主键 ID',
  `post_count` int NOT NULL DEFAULT '0' COMMENT '发帖总数',
  `like_received_count` int NOT NULL DEFAULT '0' COMMENT '获得的获赞总数',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户数据统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_stats`
--

LOCK TABLES `sys_user_stats` WRITE;
/*!40000 ALTER TABLE `sys_user_stats` DISABLE KEYS */;
INSERT INTO `sys_user_stats` VALUES (2,1,0,'2026-04-18 18:15:26'),(3,1,3,'2026-04-18 18:15:26'),(4,1,1,'2026-04-18 18:15:26'),(5,1,2,'2026-04-18 18:15:26'),(6,5,7,'2026-05-20 15:30:14'),(10,1,0,'2026-05-20 11:51:29');
/*!40000 ALTER TABLE `sys_user_stats` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-20 15:41:39
