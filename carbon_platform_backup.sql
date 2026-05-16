-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: carbon_platform
-- ------------------------------------------------------
-- Server version	8.0.44

-- 选择数据库
USE carbon_platform;

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
-- Table structure for table `activities`
--

DROP TABLE IF EXISTS `activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `requirement` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '参与要求',
  `reward_points` decimal(15,2) NOT NULL COMMENT '奖励积分',
  `status` tinyint DEFAULT '1' COMMENT '1进行中 0已结束',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '线下活动' COMMENT '活动类型',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activities`
--

LOCK TABLES `activities` WRITE;
/*!40000 ALTER TABLE `activities` DISABLE KEYS */;
INSERT INTO `activities` VALUES (1,'绿色出行月','参与本月低碳出行挑战，累计骑行50km即可获得奖励！活动期间每天记录骑行数据，系统会自动计算您的碳减排量。完成挑战的用户将获得100积分和「低碳达人」称号。','/uploads/activities/helmet.jpg','2026-03-01','2026-03-31','累计骑行50km',100.00,1,'2026-03-19 07:54:47','2026-03-20 13:55:15','线下活动'),(2,'无车日挑战','选择公共交通或自行车出行，记录一天的绿色出行。成功完成挑战的用户可获得50积分奖励。','/uploads/activities/raincoat.jpg','2026-03-15','2026-03-22','记录一天出行',50.00,1,'2026-03-19 07:54:47','2026-03-20 13:55:15','线上活动'),(3,'低碳知识竞赛','参与低碳知识问答，答对8题以上获得30积分奖励。每位用户每天有3次答题机会，取最高分计入排名。','/uploads/activities/knowledge_quiz.jpg','2026-03-10','2026-03-20','答对8题以上',30.00,1,'2026-03-19 07:54:47','2026-03-20 13:55:15','线上工作坊'),(4,'周末骑行活动','每周六组织城市骑行，路线经过精心设计，沿途设有补给点。完成全程可获得80积分。','/uploads/activities/cycling_event.jpg','2026-03-07','2026-12-31','完成全程约30km骑行',80.00,1,'2026-03-19 07:54:47','2026-03-20 13:55:15','线下峰会'),(5,'春节绿色出行','春节期间选择绿色出行方式，累计记录10次出行可获得新春礼包。','/uploads/activities/spring_travel.jpg','2026-01-28','2026-02-15','累计10次绿色出行',150.00,1,'2026-03-19 07:54:47','2026-03-20 13:55:15','线下活动'),(6,'元宵节特别活动','元宵节当天骑行赏灯，完成灯会路线打卡。','/uploads/activities/lantern_festival.jpg','2026-02-12','2026-02-12','完成灯会路线打卡',60.00,1,'2026-03-19 07:54:47','2026-03-20 13:55:15','线下活动'),(7,'地球一小时活动','响应全球环保行动，参与熄灯骑行活动。','/uploads/activities/earth_hour.jpg','2026-04-20','2026-04-21','骑行并完成打卡',40.00,1,'2026-03-19 07:54:47','2026-03-20 13:55:15','线上讲座');
/*!40000 ALTER TABLE `activities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_participations`
--

DROP TABLE IF EXISTS `activity_participations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_participations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `evidence_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '参与凭证URL',
  `status` tinyint DEFAULT '0' COMMENT '0待审核 1已通过 2已驳回',
  `review_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reviewed_by` bigint DEFAULT NULL,
  `reviewed_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_participation` (`activity_id`,`user_id`),
  KEY `user_id` (`user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `activity_participations_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`),
  CONSTRAINT `activity_participations_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_participations`
--

LOCK TABLES `activity_participations` WRITE;
/*!40000 ALTER TABLE `activity_participations` DISABLE KEYS */;
INSERT INTO `activity_participations` VALUES (1,1,2,'/uploads/activities/helmet.jpg',1,'骑行数据核实无误，奖励已发放',NULL,NULL,'2026-03-14 07:54:47'),(2,1,3,'/uploads/activities/cycling_event.jpg',1,'审核通过',NULL,NULL,'2026-03-19 07:54:47'),(3,2,4,'/uploads/activities/raincoat.jpg',1,'完成审核',NULL,NULL,'2026-03-16 07:54:47'),(4,2,2,'/uploads/activities/raincoat.jpg',1,'Approved',NULL,NULL,'2026-03-19 07:54:47'),(5,3,3,'/uploads/activities/knowledge_quiz.jpg',2,'Rejected',NULL,NULL,'2026-03-12 07:54:47'),(6,1,8,'https://example.com/proof.jpg',1,'Great',1,'2026-03-19 14:27:38','2026-03-19 14:27:25'),(10,2,8,'https://test.com/proof.jpg',1,'Approved',NULL,NULL,'2026-03-20 07:40:27'),(11,2,9,NULL,1,'审核通过',NULL,NULL,'2026-03-20 11:18:43'),(12,1,9,'{\"description\":\"谢谢\",\"images\":[]}',1,'审核通过',NULL,NULL,'2026-03-20 11:48:03'),(13,3,8,'final_test',1,'审核通过',NULL,NULL,'2026-03-20 12:17:10'),(14,4,9,'{\"description\":\"谢谢\",\"images\":[]}',1,'审核通过',NULL,NULL,'2026-03-21 10:25:37');
/*!40000 ALTER TABLE `activity_participations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `announcements`
--

DROP TABLE IF EXISTS `announcements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` tinyint DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcements`
--

LOCK TABLES `announcements` WRITE;
/*!40000 ALTER TABLE `announcements` DISABLE KEYS */;
INSERT INTO `announcements` VALUES (2,'积分兑换规则说明','商城积分兑换已全面开放！积分永久有效，放心积累。目前支持兑换骑行装备、自行车配件等多种商品，更多好物持续上架中。',1,'2026-03-19 07:54:47','2026-03-19 07:54:47'),(3,'绿色出行月活动开启','三月绿色出行月活动正式开始！本月累计骑行50km即可获得100积分和「低碳达人」称号。快来参与挑战，赢取丰厚奖励！',1,'2026-03-19 07:54:47','2026-03-19 07:54:47'),(4,'周末骑行活动预告','本周六将组织城市骑行活动，路线全程约30km，沿途设有补给点。报名请在活动页面提交申请，完成全程可获得80积分。',1,'2026-03-19 07:54:47','2026-03-19 07:54:47');
/*!40000 ALTER TABLE `announcements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carbon_points`
--

DROP TABLE IF EXISTS `carbon_points`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carbon_points` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `total_points` decimal(15,2) DEFAULT '0.00' COMMENT '总积分',
  `available_points` decimal(15,2) DEFAULT '0.00' COMMENT '可用积分',
  `used_points` decimal(15,2) DEFAULT '0.00' COMMENT '已使用积分',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `total_carbon` decimal(10,4) DEFAULT '0.0000' COMMENT '总减碳量',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `carbon_points_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carbon_points`
--

LOCK TABLES `carbon_points` WRITE;
/*!40000 ALTER TABLE `carbon_points` DISABLE KEYS */;
INSERT INTO `carbon_points` VALUES (1,1,0.00,0.00,0.00,'2026-03-19 07:54:46',0.0000),(2,2,563.30,83.30,480.00,'2026-03-19 08:08:50',8.9900),(3,3,319.00,159.00,160.00,'2026-03-19 08:08:50',4.2600),(4,4,502.00,502.00,0.00,'2026-03-19 08:08:50',7.7000),(5,5,0.00,0.00,0.00,'2026-03-19 08:10:33',0.0000),(6,6,0.00,0.00,0.00,'2026-03-19 08:10:33',0.0000),(7,7,0.00,0.00,0.00,'2026-03-19 14:17:50',0.0000),(8,8,255.00,155.00,100.00,'2026-03-19 14:24:35',3.2600),(9,9,1000261.70,999011.70,1250.00,'2026-03-20 03:07:08',21001.6400),(10,12,0.00,0.00,0.00,'2026-03-21 09:48:05',0.0000),(11,13,1227.76,1227.76,0.00,'2026-03-23 09:07:46',32.2300);
/*!40000 ALTER TABLE `carbon_points` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_comments`
--

DROP TABLE IF EXISTS `forum_comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_comments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `likes` int DEFAULT '0',
  `status` tinyint DEFAULT '1' COMMENT '1正常 0删除',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `report_count` int DEFAULT '0' COMMENT '举报次数',
  `report_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '举报原因',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `forum_comments_ibfk_1` FOREIGN KEY (`post_id`) REFERENCES `forum_posts` (`id`),
  CONSTRAINT `forum_comments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_comments`
--

LOCK TABLES `forum_comments` WRITE;
/*!40000 ALTER TABLE `forum_comments` DISABLE KEYS */;
INSERT INTO `forum_comments` VALUES (1,1,3,'写得真好！我也想开始骑行通勤了，请问有什么入门建议吗？',0,1,'2026-03-16 07:54:47',0,NULL),(2,1,2,'回复楼上：建议先从短距离开始，5公里以内，等身体适应了再逐步增加距离。',0,1,'2026-03-17 07:54:47',0,NULL),(4,4,4,'太实用了！周末就去试试那条老城探秘线',0,1,'2026-03-14 07:54:47',0,NULL),(5,4,3,'建议楼主再出一期进阶路线推荐！',0,1,'2026-03-11 07:54:47',0,NULL),(6,1,2,'nice post',0,1,'2026-03-19 10:40:40',0,NULL),(7,1,2,'Test comment',0,1,'2026-03-19 14:15:55',0,NULL),(8,9,8,'This is a test comment.',0,1,'2026-03-19 14:28:17',0,NULL),(9,10,9,'Great post!',0,1,'2026-03-20 03:13:09',0,NULL),(10,1,2,'谢谢',0,1,'2026-03-20 06:16:23',0,NULL),(11,1,8,'Test comment',0,1,'2026-03-20 07:38:57',0,NULL);
/*!40000 ALTER TABLE `forum_comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_posts`
--

DROP TABLE IF EXISTS `forum_posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_posts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `section_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` longtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `images` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'JSON数组存储图片URL',
  `views` int DEFAULT '0',
  `likes` int DEFAULT '0',
  `comments_count` int DEFAULT '0',
  `is_top` tinyint DEFAULT '0' COMMENT '是否置顶',
  `status` tinyint DEFAULT '1' COMMENT '1正常 0删除',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_section_id` (`section_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `forum_posts_ibfk_1` FOREIGN KEY (`section_id`) REFERENCES `forum_sections` (`id`),
  CONSTRAINT `forum_posts_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_posts`
--

LOCK TABLES `forum_posts` WRITE;
/*!40000 ALTER TABLE `forum_posts` DISABLE KEYS */;
INSERT INTO `forum_posts` VALUES (1,1,2,'我的骑行通勤日记：从开车族到骑行达人','分享我的骑行通勤经历。坚持骑行半年，从最初的几公里到现在每天20公里，不仅身体更健康了，每个月的交通费也省了不少。最重要的是，为环保贡献了自己的一份力量！\n\n骑行装备推荐：\n1. 头盔是必选项，安全第一\n2. 骑行手套可以有效减震\n3. 车灯夜间必备\n4. 背包要选透气的',NULL,8,160,27,1,1,'2026-03-15 07:54:47','2026-03-19 07:54:47'),(2,1,3,'公共交通也能很舒适 - 我的公交通勤经验','很多人觉得公交拥挤不舒服，其实选对线路和时间很重要。\n\n1. 错峰出行，避开早高峰\n2. 选靠近前门的座位，下车方便\n3. 准备一本书或播客，时间过得很快\n4. 公交+步行组合，健康又环保\n\n一个月下来，我发现不仅节省了油钱，还多了很多碎片时间可以用来学习。',NULL,0,89,15,0,1,'2026-03-13 07:54:47','2026-03-19 07:54:47'),(3,1,4,'周末骑行的快乐，你根本想象不到！','最近迷上了周末长距离骑行，发现了城市里很多平时开车注意不到的美景。\n\n推荐一条我常骑的路线：从滨江公园出发，沿着江边骑行到湿地公园，全程约25公里，风景超好！\n\n骑行的好处：\n- 锻炼身体\n- 放松心情\n- 低碳环保\n- 省钱',NULL,0,234,42,1,0,'2026-03-15 07:54:47','2026-03-19 07:54:47'),(4,2,2,'城市骑行路线推荐：适合新手的5条安全路线','整理了5条适合新手的安全骑行路线，都是我亲自骑过验证过的：\n\n1. 滨江休闲线（约8公里）- 特点：路况好，人少景美\n2. 公园环线（约12公里）- 特点：全程有自行车道\n3. 高校穿越线（约10公里）- 特点：经过多所高校，氛围好\n4. 河堤风光线（约15公里）- 特点：视野开阔，空气好\n5. 老城探秘线（约8公里）- 特点：可以发现很多隐藏的美食',NULL,0,312,56,0,1,'2026-03-14 07:54:47','2026-03-19 07:54:47'),(5,2,3,'地铁+步行：我的黄金通勤组合','家离地铁站1.5公里，地铁站离公司800米，这段距离我选择步行。\n\n实测数据：\n- 每天步行约5000步\n- 每周减少碳排放约5kg\n- 每月节省交通费约200元\n\n步行上班不仅锻炼身体，还能让我保持清醒的工作状态。强烈推荐！',NULL,0,145,28,0,1,'2026-03-10 07:54:47','2026-03-19 07:54:47'),(6,3,4,'骑行100公里能减少多少碳排放？算给你看！','很多人问我骑行到底环保在哪里，今天来算一笔账：\n\n骑行100公里：\n- 碳排放：0 kg\n- 消耗热量：约2000卡路里\n\n开车100公里：\n- 碳排放：约20 kg CO2\n\n骑行不仅零排放，还锻炼身体，一举两得！',NULL,0,456,78,0,1,'2026-03-06 07:54:47','2026-03-19 07:54:47'),(7,3,2,'下雨天怎么绿色出行？这些方法学起来','最近雨季来了，很多人问下雨天怎么坚持绿色出行：\n\n1. 地铁+步行 - 最稳定的选择\n2. 公交+共享单车 - 组合出行\n3. 电动滑板车 - 轻便快捷\n4. 实在不行，在家办公一天也是环保的选择哦！',NULL,0,198,34,0,1,'2026-03-10 07:54:47','2026-03-19 07:54:47'),(8,1,2,'Test Post Fixed','Test content',NULL,0,0,0,1,2,'2026-03-19 11:12:22','2026-03-19 11:12:22'),(9,1,8,'Test Post Title','This is a test post content for the forum.',NULL,0,1,1,0,1,'2026-03-19 14:28:00','2026-03-19 14:28:00'),(10,1,9,'My First Post','This is my first post about cycling.',NULL,1,1,1,0,-1,'2026-03-20 03:12:20','2026-03-20 03:12:20'),(12,1,8,'Test Post','This is a test',NULL,0,0,0,0,1,'2026-03-20 07:38:57','2026-03-20 07:38:57'),(14,1,9,'谢谢','谢谢',NULL,1,0,0,0,1,'2026-03-23 07:32:22','2026-03-23 07:32:22');
/*!40000 ALTER TABLE `forum_posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_sections`
--

DROP TABLE IF EXISTS `forum_sections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_sections` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_sections`
--

LOCK TABLES `forum_sections` WRITE;
/*!40000 ALTER TABLE `forum_sections` DISABLE KEYS */;
INSERT INTO `forum_sections` VALUES (1,'出行分享','分享你的低碳出行故事',NULL,1,1,'2026-03-17 00:53:06'),(2,'路线攻略','推荐优质出行路线',NULL,2,1,'2026-03-17 00:53:06'),(3,'低碳问答','讨论低碳出行相关问题',NULL,3,1,'2026-03-17 00:53:06');
/*!40000 ALTER TABLE `forum_sections` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NOT NULL,
  `receiver_id` bigint NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'system',
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_read` tinyint DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
  CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `messages`
--

LOCK TABLES `messages` WRITE;
/*!40000 ALTER TABLE `messages` DISABLE KEYS */;
INSERT INTO `messages` VALUES (2,1,1,NULL,'system','您的出行记录已审核通过，获得 25.0000 积分',1,'2026-03-19 05:25:27'),(3,1,1,NULL,'system','您的出行记录已审核通过，获得 40.0000 积分',1,'2026-03-19 05:25:36'),(4,2,2,NULL,'system','您已成功兑换 智能骑行头盔，订单号：b017626696d14980ab79',1,'2026-03-19 10:40:26'),(5,8,8,NULL,'system','您的出行记录已审核通过，获得 55.0000 积分',0,'2026-03-19 14:25:45'),(6,8,8,NULL,'system','您已成功兑换 便携打气筒，订单号：9af2853e9a614cbdac21',0,'2026-03-19 14:26:08'),(7,2,2,NULL,'system','您的出行记录已审核通过，获得 30.0000 积分',1,'2026-03-20 02:49:48'),(8,2,2,NULL,'system','您的出行记录未通过审核。原因：Distance seems unreasonable',1,'2026-03-20 02:49:54'),(9,9,9,NULL,'system','您的出行记录已审核通过，获得 50.0000 积分',1,'2026-03-20 03:09:48'),(10,9,9,NULL,'system','您已成功兑换 便携打气筒，订单号：e562b7d858fd4c0cba4e',1,'2026-03-20 03:10:26'),(11,9,9,NULL,'system','您的出行记录未通过审核。原因：Rejected',1,'2026-03-20 03:17:42'),(12,9,9,NULL,'system','您的出行记录未通过审核。原因：Invalid data',1,'2026-03-20 03:33:41'),(13,9,9,NULL,'system','您的出行记录已审核通过，获得 160.0000 积分',1,'2026-03-20 03:34:02'),(14,8,8,NULL,'system','您的出行记录已审核通过，获得 50.0000 积分',0,'2026-03-20 07:40:26'),(15,8,8,NULL,'system','您已成功兑换 便携打气筒，订单号：4577219697754ddf993f',0,'2026-03-20 07:40:27'),(16,2,2,NULL,'system','您的出行记录已审核通过，获得 10.3000 积分',0,'2026-03-20 09:38:41'),(17,9,9,NULL,'system','您的出行记录已审核通过，获得 999990.0000 积分',1,'2026-03-20 09:38:47'),(18,9,9,NULL,'system','您的出行记录已审核通过，获得 0.0000 积分',1,'2026-03-20 09:38:49'),(19,9,9,NULL,'system','您的出行记录已审核通过，获得 -50.0000 积分',1,'2026-03-20 09:38:51'),(22,1,1,'新的出行记录待审核','system','用户提交了 骑行 出行记录，距离 5 km',1,'2026-03-20 10:17:56'),(23,9,9,'兑换成功','exchange','您已成功兑换 折叠自行车，订单号：72a9d65927a0457cb7cc',1,'2026-03-20 11:17:57'),(24,1,1,'新的兑换订单','system','用户兑换了「折叠自行车」，消耗 500.00 积分',1,'2026-03-20 11:17:57'),(25,1,1,'新的活动参与待审核','system','用户报名了活动「无车日挑战」',1,'2026-03-20 11:18:43'),(26,1,1,'新的出行记录待审核','system','用户提交了 骑行 出行记录，距离 0.9008543913826519 km',1,'2026-03-20 11:22:29'),(27,9,9,NULL,'system','您的出行记录已审核通过，获得 9.0000 积分',1,'2026-03-20 11:22:40'),(28,8,8,NULL,'system','您的出行记录已审核通过，获得 50.0000 积分',0,'2026-03-20 11:22:43'),(29,1,1,'新的活动参与待审核','system','用户报名了活动「绿色出行月」',1,'2026-03-20 11:48:03'),(30,9,9,'兑换成功','exchange','您已成功兑换 折叠自行车，订单号：6fa48ab7f7844261bf78',1,'2026-03-20 11:48:58'),(31,1,1,'新的兑换订单','system','用户兑换了「折叠自行车」，消耗 500.00 积分',1,'2026-03-20 11:48:58'),(32,9,9,'兑换成功','exchange','您已成功兑换 智能骑行头盔，订单号：033ce6cea9de46bfbe45',1,'2026-03-20 12:10:43'),(33,1,1,'新的兑换订单','system','用户兑换了「智能骑行头盔」，消耗 200.00 积分',1,'2026-03-20 12:10:43'),(34,1,1,'新的活动参与待审核','system','用户报名了活动「低碳知识竞赛」',1,'2026-03-20 12:17:10'),(35,1,1,'新的出行记录待审核','system','用户提交了 骑行 出行记录，距离 0.27130552042777906 km',1,'2026-03-20 12:43:49'),(36,2,2,'点赞通知','forum_like','积分富翁 点赞了您的帖子「我的骑行通勤日记：从开车族到骑行达人」',0,'2026-03-20 12:44:09'),(37,9,9,NULL,'system','您的出行记录已审核通过，获得 2.7000 积分',0,'2026-03-20 12:46:55'),(38,9,9,NULL,'system','您的出行记录已审核通过，获得 100.0000 积分',0,'2026-03-20 12:47:01'),(39,9,9,'帖子审核通过','forum_approved','您发布的帖子「测试」已通过审核',1,'2026-03-20 12:47:49'),(40,8,8,'帖子审核通过','forum_approved','您发布的帖子「Test Post Title」已通过审核',0,'2026-03-20 13:18:08'),(41,2,2,'帖子审核未通过','forum_rejected','您发布的帖子「Test Post Fixed」未通过审核。原因：不符合社区规范',0,'2026-03-20 13:18:12'),(42,1,1,'新的活动参与待审核','system','用户报名了活动「周末骑行活动」',1,'2026-03-21 10:25:37'),(43,1,1,'新帖子待审核','system','用户发布了帖子「谢谢」',1,'2026-03-23 07:32:22'),(44,9,9,'帖子审核通过','forum_approved','您发布的帖子「谢谢」已通过审核',0,'2026-03-23 07:32:33'),(45,1,1,'新的出行记录待审核','system','用户提交了 骑行 出行记录，距离 153.4729047370387 km',0,'2026-03-23 09:13:53'),(46,13,13,NULL,'system','您的出行记录已审核通过，获得 1227.7600 积分',0,'2026-03-23 09:14:07');
/*!40000 ALTER TABLE `messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `quantity` int DEFAULT '1',
  `points_spent` decimal(15,2) NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0待发货 1已发货 2已收货 3已取消',
  `delivery_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `delivery_no` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '快递单号',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'ORD1773906886910001',2,3,1,200.00,2,'北京市朝阳区建国路88号','SF1234567890','2026-03-09 07:54:47','2026-03-19 07:54:47'),(2,'ORD1773906886915002',2,7,1,80.00,1,'北京市朝阳区建国路88号','YT9876543210','2026-03-17 07:54:47','2026-03-19 07:54:47'),(3,'ORD1773906886919003',3,4,2,160.00,1,'上海市浦东新区世纪大道100号','1111111111','2026-03-19 04:54:47','2026-03-19 07:54:47'),(4,'b017626696d14980ab79',2,3,1,200.00,1,'test','SF888888888','2026-03-19 10:40:26','2026-03-19 10:40:26'),(5,'9af2853e9a614cbdac21',8,10,1,50.00,2,'Beijing','SF123456789','2026-03-19 14:26:08','2026-03-19 14:26:08'),(6,'e562b7d858fd4c0cba4e',9,10,1,50.00,2,'TestAddress','SF888888888','2026-03-20 03:10:26','2026-03-20 03:10:26'),(7,'4577219697754ddf993f',8,10,1,50.00,2,'TestAddress','SF123456789','2026-03-20 07:40:27','2026-03-20 07:40:27'),(8,'72a9d65927a0457cb7cc',9,1,1,500.00,1,'xxx','xx','2026-03-20 11:17:57','2026-03-20 11:17:57'),(9,'6fa48ab7f7844261bf78',9,1,1,500.00,0,'xx',NULL,'2026-03-20 11:48:58','2026-03-20 11:48:58'),(10,'033ce6cea9de46bfbe45',9,3,1,200.00,2,'xx','信息','2026-03-20 12:10:43','2026-03-20 12:10:43');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `points_details`
--

DROP TABLE IF EXISTS `points_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `points_details` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `points` decimal(15,2) NOT NULL COMMENT '积分数量',
  `type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型: travel出行 activity活动 purchase购物 exchange兑换',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID(出行记录ID/活动ID等)',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `points_details_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `points_details`
--

LOCK TABLES `points_details` WRITE;
/*!40000 ALTER TABLE `points_details` DISABLE KEYS */;
INSERT INTO `points_details` VALUES (1,2,155.00,'travel',1,'出行获得积分','2026-03-18 07:54:47'),(2,2,200.00,'travel',2,'出行获得积分','2026-03-16 07:54:47'),(3,2,68.00,'travel',3,'出行获得积分','2026-03-14 07:54:47'),(4,3,25.00,'travel',4,'出行获得积分','2026-03-18 07:54:47'),(5,3,84.00,'travel',5,'出行获得积分','2026-03-17 07:54:47'),(6,3,180.00,'travel',6,'出行获得积分','2026-03-15 07:54:47'),(7,4,300.00,'travel',8,'出行获得积分','2026-03-18 07:54:47'),(8,4,80.00,'travel',9,'出行获得积分','2026-03-13 07:54:47'),(9,4,72.00,'travel',10,'出行获得积分','2026-03-11 07:54:47'),(10,2,100.00,'activity',1,'参与活动「绿色出行月」获得积分','2026-03-14 07:54:47'),(11,4,50.00,'activity',2,'参与活动「无车日挑战」获得积分','2026-03-16 07:54:47'),(12,3,30.00,'activity',3,'参与活动「低碳知识竞赛」获得积分','2026-03-12 07:54:47'),(13,3,-160.00,'exchange',3,'兑换商品「骑行手套」消耗积分','2026-03-19 04:54:47'),(14,2,-80.00,'exchange',2,'兑换商品「便携雨衣」消耗积分','2026-03-17 07:54:47'),(15,2,-200.00,'exchange',1,'兑换商品「智能骑行头盔」消耗积分','2026-03-09 07:54:47'),(16,2,-200.00,'exchange',4,'兑换商品：智能骑行头盔','2026-03-19 10:40:26'),(17,8,55.00,'travel',14,'出行记录审核通过，获得积分','2026-03-19 14:25:45'),(18,8,-50.00,'exchange',5,'兑换商品：便携打气筒','2026-03-19 14:26:08'),(19,8,100.00,'activity',1,'参与活动：绿色出行月','2026-03-19 14:27:38'),(20,2,30.00,'travel',13,'出行记录审核通过，获得积分','2026-03-20 02:49:48'),(21,9,50.00,'travel',16,'出行记录审核通过，获得积分','2026-03-20 03:09:48'),(22,9,-50.00,'exchange',6,'兑换商品：便携打气筒','2026-03-20 03:10:26'),(23,9,160.00,'travel',21,'出行记录审核通过，获得积分','2026-03-20 03:34:02'),(24,8,50.00,'travel',27,'出行记录审核通过，获得积分','2026-03-20 07:40:26'),(25,8,-50.00,'exchange',7,'兑换商品：便携打气筒','2026-03-20 07:40:27'),(26,2,10.30,'travel',26,'出行记录审核通过，获得积分','2026-03-20 09:38:41'),(27,9,999990.00,'travel',25,'出行记录审核通过，获得积分','2026-03-20 09:38:47'),(28,9,0.00,'travel',24,'出行记录审核通过，获得积分','2026-03-20 09:38:49'),(29,9,-50.00,'travel',23,'出行记录审核通过，获得积分','2026-03-20 09:38:51'),(30,9,-500.00,'exchange',8,'兑换商品：折叠自行车','2026-03-20 11:17:57'),(31,9,9.00,'travel',31,'出行记录审核通过，获得积分','2026-03-20 11:22:40'),(32,8,50.00,'travel',30,'出行记录审核通过，获得积分','2026-03-20 11:22:43'),(33,9,-500.00,'exchange',9,'兑换商品：折叠自行车','2026-03-20 11:48:58'),(34,9,-200.00,'exchange',10,'兑换商品：智能骑行头盔','2026-03-20 12:10:43'),(35,9,2.70,'travel',32,'出行记录审核通过，获得积分','2026-03-20 12:46:55'),(36,9,100.00,'travel',20,'出行记录审核通过，获得积分','2026-03-20 12:47:01'),(37,13,1227.76,'travel',33,'出行记录审核通过，获得积分','2026-03-23 09:14:07');
/*!40000 ALTER TABLE `points_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `points_rules`
--

DROP TABLE IF EXISTS `points_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `points_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `travel_mode_id` bigint DEFAULT NULL COMMENT '出行方式ID',
  `points_per_km` decimal(10,2) DEFAULT NULL COMMENT '每公里积分',
  `carbon_reduction` decimal(10,4) DEFAULT NULL COMMENT '每公里减碳量(kg)',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_travel_mode` (`travel_mode_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `points_rules`
--

LOCK TABLES `points_rules` WRITE;
/*!40000 ALTER TABLE `points_rules` DISABLE KEYS */;
INSERT INTO `points_rules` VALUES (1,1,5.00,0.0000,1,'步行积分规则','2026-03-19 07:54:47','2026-03-19 07:54:47'),(2,2,10.00,0.2100,1,'骑行积分规则','2026-03-19 07:54:47','2026-03-19 07:54:47'),(3,3,8.00,0.0800,1,'公交积分规则','2026-03-19 07:54:47','2026-03-19 07:54:47'),(4,4,7.00,0.0400,1,'地铁积分规则','2026-03-19 07:54:47','2026-03-19 07:54:47'),(5,5,6.00,0.0500,1,'电动车积分规则','2026-03-19 07:54:47','2026-03-19 07:54:47');
/*!40000 ALTER TABLE `points_rules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_categories`
--

DROP TABLE IF EXISTS `product_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类编码',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标URL',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '1启用 0禁用',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_code` (`code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_categories`
--

LOCK TABLES `product_categories` WRITE;
/*!40000 ALTER TABLE `product_categories` DISABLE KEYS */;
INSERT INTO `product_categories` VALUES (1,'自行车','bike',NULL,'各类自行车及骑行装备',1,1,'2026-03-23 06:09:53','2026-03-23 06:09:53'),(2,'装备','equipment',NULL,'骑行安全装备',2,1,'2026-03-23 06:09:53','2026-03-23 06:09:53'),(3,'配件','accessories',NULL,'骑行配件及周边',3,1,'2026-03-23 06:09:53','2026-03-23 06:09:53'),(4,'环保用品','eco',NULL,'环保生活用品',4,1,'2026-03-23 06:09:53','2026-03-23 06:09:53'),(5,'其他','other',NULL,'其他商品',99,1,'2026-03-23 06:09:53','2026-03-23 06:09:53');
/*!40000 ALTER TABLE `product_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类: bike自行车 equipment装备 accessories配件',
  `points_required` decimal(15,2) NOT NULL COMMENT '所需积分',
  `stock` int DEFAULT '0' COMMENT '库存',
  `carbon_label` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '低碳属性标签',
  `status` tinyint DEFAULT '1' COMMENT '1上架 0下架',
  `sort_order` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'折叠自行车','轻便折叠，通勤首选，低碳出行好伴侣','/uploads/products/bike_folding.jpg','bike',500.00,8,'低碳环保',1,1,'2026-03-19 07:54:47','2026-03-20 13:55:15'),(2,'山地自行车','专业级山地骑行，坚固耐用','/uploads/products/bike_mountain.jpg','bike',800.00,5,'运动健身',1,2,'2026-03-19 07:54:47','2026-03-20 13:55:15'),(3,'智能骑行头盔','骑行安全必备，内置LED灯，夜间骑行更安全','/uploads/products/helmet.jpg','equipment',200.00,18,'安全骑行',1,3,'2026-03-19 07:54:47','2026-03-20 13:55:15'),(4,'骑行手套','减震防滑，透气舒适','/uploads/products/gloves.jpg','equipment',80.00,50,'舒适骑行',1,4,'2026-03-19 07:54:47','2026-03-20 13:55:15'),(5,'骑行背包','防水透气，多功能收纳','/uploads/products/backpack.jpg','equipment',120.00,30,'实用便捷',1,5,'2026-03-19 07:54:47','2026-03-20 13:55:15'),(6,'防晒骑行服','透气速干，UV防护，夏日骑行必备','/uploads/products/gloves.jpg','accessories',150.00,30,'环保材质',1,6,'2026-03-19 07:54:47','2026-03-20 13:55:15'),(7,'便携雨衣','折叠收纳，随身携带，应对突发天气','/uploads/products/raincoat.jpg','accessories',80.00,50,'可回收材料',1,7,'2026-03-19 07:54:47','2026-03-20 13:55:15'),(8,'铝合金水壶架','轻量化设计，通用型安装','/uploads/products/helmet.jpg','accessories',60.00,100,'铝合金环保',1,8,'2026-03-19 07:54:47','2026-03-20 13:55:15'),(9,'自行车车灯','USB充电，强光远射，安全夜骑','/uploads/products/helmet.jpg','accessories',90.00,40,'夜间安全',1,9,'2026-03-19 07:54:47','2026-03-20 13:55:15'),(10,'便携打气筒','小巧便携，随时补气','','accessories',50.00,60,'维护必备',1,10,'2026-03-19 07:54:47','2026-03-20 13:45:06');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `config_value` text COLLATE utf8mb4_unicode_ci,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_config`
--

LOCK TABLES `system_config` WRITE;
/*!40000 ALTER TABLE `system_config` DISABLE KEYS */;
INSERT INTO `system_config` VALUES (1,'validity_type','rolling','积分有效期类型','2026-03-23 09:59:24'),(2,'holiday_multiplier','2.0','节假日奖励倍数','2026-03-23 09:59:24'),(3,'daily_limit','500','每日积分上限','2026-03-23 09:59:24'),(4,'max_distance_per_trip','50','单次最大距离','2026-03-23 09:59:24');
/*!40000 ALTER TABLE `system_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `track_points`
--

DROP TABLE IF EXISTS `track_points`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `track_points` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `record_id` bigint NOT NULL,
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `altitude` decimal(10,2) DEFAULT NULL COMMENT '海拔(m)',
  `speed` decimal(6,2) DEFAULT NULL COMMENT '速度(m/s)',
  `accuracy` int DEFAULT NULL COMMENT '精度(m)',
  `timestamp` datetime DEFAULT NULL COMMENT '时间戳',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_record_id` (`record_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `track_points`
--

LOCK TABLES `track_points` WRITE;
/*!40000 ALTER TABLE `track_points` DISABLE KEYS */;
INSERT INTO `track_points` VALUES (1,2,13,39.900000,116.400000,NULL,NULL,NULL,'2026-03-19 22:15:20','2026-03-19 14:15:20');
/*!40000 ALTER TABLE `track_points` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `travel_modes`
--

DROP TABLE IF EXISTS `travel_modes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `travel_modes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '出行方式名称',
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标URL',
  `carbon_reduction` decimal(10,2) NOT NULL COMMENT '单位碳减排量(kg/km)',
  `points_per_km` decimal(10,2) NOT NULL COMMENT '每公里获得积分',
  `sort_order` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `max_points_per_trip` decimal(10,2) DEFAULT '100.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `travel_modes`
--

LOCK TABLES `travel_modes` WRITE;
/*!40000 ALTER TABLE `travel_modes` DISABLE KEYS */;
INSERT INTO `travel_modes` VALUES (1,'步行',NULL,0.00,10.00,1,1,'2026-03-17 00:53:06',100.00),(2,'骑行',NULL,0.21,8.00,2,1,'2026-03-17 00:53:06',100.00),(3,'公交',NULL,0.08,5.00,3,1,'2026-03-17 00:53:06',100.00),(4,'地铁',NULL,0.04,6.00,4,1,'2026-03-17 00:53:06',100.00),(5,'电动车',NULL,0.05,8.00,5,1,'2026-03-17 00:53:06',100.00),(7,'New Mode',NULL,0.15,8.00,99,1,'2026-03-20 03:30:18',100.00);
/*!40000 ALTER TABLE `travel_modes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `travel_records`
--

DROP TABLE IF EXISTS `travel_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `travel_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `travel_mode_id` bigint NOT NULL,
  `start_location` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '起点',
  `end_location` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '终点',
  `distance` decimal(10,2) NOT NULL COMMENT '距离(km)',
  `carbon_reduction` decimal(10,2) NOT NULL COMMENT '碳减排量(kg)',
  `points_earned` decimal(10,2) DEFAULT '0.00' COMMENT '获得积分',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0待审核 1已通过 2已驳回',
  `review_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核意见',
  `reviewed_by` bigint DEFAULT NULL COMMENT '审核人ID',
  `reviewed_at` timestamp NULL DEFAULT NULL COMMENT '审核时间',
  `travel_date` date NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `track_points` longtext COLLATE utf8mb4_unicode_ci COMMENT '轨迹点JSON数据',
  PRIMARY KEY (`id`),
  KEY `travel_mode_id` (`travel_mode_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_travel_date` (`travel_date`),
  CONSTRAINT `travel_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `travel_records_ibfk_2` FOREIGN KEY (`travel_mode_id`) REFERENCES `travel_modes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `travel_records`
--

LOCK TABLES `travel_records` WRITE;
/*!40000 ALTER TABLE `travel_records` DISABLE KEYS */;
INSERT INTO `travel_records` VALUES (1,2,2,'公司','家',15.50,3.26,155.00,1,NULL,NULL,NULL,'2026-03-18','2026-03-18 07:54:47',NULL),(2,2,2,'家','公司',20.00,4.20,200.00,1,NULL,NULL,NULL,'2026-03-16','2026-03-16 07:54:47',NULL),(3,2,3,'小区门口','购物中心',8.50,0.68,68.00,1,NULL,NULL,NULL,'2026-03-14','2026-03-14 07:54:47',NULL),(4,3,1,'家','地铁站',5.00,0.00,25.00,1,NULL,NULL,NULL,'2026-03-18','2026-03-18 07:54:47',NULL),(5,3,4,'地铁A站','地铁B站',12.00,0.48,84.00,1,NULL,NULL,NULL,'2026-03-17','2026-03-17 07:54:47',NULL),(6,3,2,'公园','单位',18.00,3.78,180.00,1,NULL,NULL,NULL,'2026-03-15','2026-03-15 07:54:47',NULL),(7,3,2,'家','郊外',25.00,5.25,250.00,0,NULL,NULL,NULL,'2026-03-19','2026-03-19 07:54:47',NULL),(8,4,2,'出发点','终点',30.00,6.30,300.00,1,NULL,NULL,NULL,'2026-03-18','2026-03-18 07:54:47',NULL),(9,4,3,'社区','医院',10.00,0.80,80.00,1,NULL,NULL,NULL,'2026-03-13','2026-03-13 07:54:47',NULL),(10,4,5,'学校','商场',12.00,0.60,72.00,1,NULL,NULL,NULL,'2026-03-11','2026-03-11 07:54:47',NULL),(11,2,2,'home','office',12.00,2.52,0.00,0,NULL,NULL,NULL,'2026-03-19','2026-03-19 10:40:40',NULL),(12,2,2,'Home','Office',5.00,1.05,0.00,2,'Distance seems unreasonable',1,'2026-03-20 02:49:54','2026-03-19','2026-03-19 11:49:31',NULL),(13,2,2,'A','B',3.00,0.63,30.00,1,'Approved',1,'2026-03-20 02:49:48','2026-03-19','2026-03-19 14:15:20',NULL),(14,8,2,'West Station','Tiananmen',5.50,1.16,55.00,1,'Approved',1,'2026-03-19 14:25:45','2026-03-19','2026-03-19 14:25:30',NULL),(15,8,2,'Home','Work',8.00,1.68,0.00,0,NULL,NULL,NULL,'2026-03-20','2026-03-20 02:54:46',NULL),(16,9,2,'Home','Office',5.00,1.05,50.00,1,'Approved',1,'2026-03-20 03:09:48','2026-03-20','2026-03-20 03:08:43',NULL),(17,9,1,'Station','Home',3.00,0.00,0.00,2,'Rejected',1,'2026-03-20 03:17:42','2026-03-20','2026-03-20 03:08:50',NULL),(18,9,3,'Downtown','Suburb',10.00,0.80,0.00,0,NULL,NULL,NULL,'2026-03-20','2026-03-20 03:09:06',NULL),(19,9,4,'StationA','StationB',15.00,0.60,0.00,0,NULL,NULL,NULL,'2026-03-20','2026-03-20 03:09:14',NULL),(20,9,1,'Home','Park',10.00,0.00,100.00,1,'审核通过',1,'2026-03-20 12:47:01','2026-03-20','2026-03-20 03:16:54',NULL),(21,9,3,'Station1','Station2',20.00,1.60,160.00,1,'Good',1,'2026-03-20 03:34:02','2026-03-20','2026-03-20 03:17:04',NULL),(22,9,2,'Office','Gym',8.00,1.68,0.00,2,'Invalid data',1,'2026-03-20 03:33:41','2026-03-20','2026-03-20 03:17:17',NULL),(23,9,2,'A','B',-5.00,-1.05,-50.00,1,'审核通过',1,'2026-03-20 09:38:51','2026-03-20','2026-03-20 03:46:44',NULL),(24,9,2,'A','B',0.00,0.00,0.00,1,'审核通过',1,'2026-03-20 09:38:49','2026-03-20','2026-03-20 03:47:02',NULL),(25,9,2,'A','B',99999.00,20999.79,999990.00,1,'审核通过',1,'2026-03-20 09:38:47','2026-03-20','2026-03-20 03:47:09',NULL),(26,2,2,'39.9078, 116.4066','39.9082, 116.4187',1.03,0.22,10.30,1,'审核通过',1,'2026-03-20 09:38:41','2026-03-20','2026-03-20 06:22:08',NULL),(27,8,2,'Home','Office',5.00,1.05,50.00,1,'Approved',1,'2026-03-20 07:40:26','2026-03-20','2026-03-20 07:38:57',NULL),(30,8,2,'Home','Office',5.00,1.05,50.00,1,'审核通过',1,'2026-03-20 11:22:43','2026-03-20','2026-03-20 10:17:56',NULL),(31,9,2,'39.9010, 116.4141','39.8931, 116.4123',0.90,0.19,9.00,1,'审核通过',1,'2026-03-20 11:22:40','2026-03-20','2026-03-20 11:22:29',NULL),(32,9,2,'39.9076, 116.3969','39.9078, 116.4001',0.27,0.06,2.70,1,'审核通过',1,'2026-03-20 12:46:55','2026-03-20','2026-03-20 12:43:49',NULL),(33,13,2,'40.2514, 115.9187','39.5100, 117.4359',153.47,32.23,1227.76,1,'审核通过',1,'2026-03-23 09:14:07','2026-03-23','2026-03-23 09:13:53',NULL);
/*!40000 ALTER TABLE `travel_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_follows`
--

DROP TABLE IF EXISTS `user_follows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follows` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `follower_id` bigint NOT NULL,
  `following_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_follow` (`follower_id`,`following_id`),
  KEY `following_id` (`following_id`),
  KEY `idx_follower_id` (`follower_id`),
  CONSTRAINT `user_follows_ibfk_1` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`),
  CONSTRAINT `user_follows_ibfk_2` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_follows`
--

LOCK TABLES `user_follows` WRITE;
/*!40000 ALTER TABLE `user_follows` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_follows` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `real_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
  `id_card` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '身份证号',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1正常 0禁用',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'user' COMMENT '角色: user普通用户 admin管理员',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`),
  UNIQUE KEY `phone` (`phone`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'13800138000','$2a$10$EYqrCfxvdDo/9XG1tc68rOmfhkAuyYRXT4DXIgiP9a8dtVJTJSN4.','UpdatedAdmin',NULL,NULL,NULL,1,'admin','2026-03-19 07:54:46','2026-03-19 07:54:46',NULL),(2,'13900000001','$2a$10$o6gZNpu6SI.Pbz6IxXezJuLNpAw9rAaiUAkNPbtAJSArIctUW5JKK','测试','/uploads/avatars/avatar_1.jpg',NULL,NULL,1,'user','2026-03-19 07:54:46','2026-03-20 13:55:15',NULL),(3,'13900000002','$2a$10$HVCkJUTrtA50crL6gzmX3uSakpGot7R9bkdIHQomPJENRg0r66AV.','绿色行者户外','/uploads/avatars/avatar_2.jpg',NULL,NULL,1,'user','2026-03-19 07:54:46','2026-03-20 13:55:15',NULL),(4,'13900000003','$2a$10$fR6lx.U/mnRZ/vhXAcqDVu7sofXJ1wMxw.MdtqzoJudI8Qae/xx8q','低碳先锋','/uploads/avatars/avatar_3.jpg',NULL,NULL,1,'user','2026-03-19 07:54:47','2026-03-20 13:55:15',NULL),(5,'13900000004','$2a$10$6tP/yuGtx2mu0nHXAiXZeO7q1EDX6yOmlYBO8AlgQ4MpjiHF7IW5m','骑行爱好者','/uploads/avatars/avatar_4.jpg',NULL,NULL,1,'user','2026-03-19 07:54:47','2026-03-20 13:55:15',NULL),(6,'13900000005','$2a$10$cBCmTDopeoAH2/LXMubwy.roFpiCJl4M8xOcFBNyPdHSbTQSINlvy','城市通勤族','/uploads/avatars/avatar_5.jpg',NULL,NULL,1,'user','2026-03-19 07:54:47','2026-03-20 13:55:15',NULL),(7,'18777777777','$2a$10$5vRwA0kXNwAI3qSRl/QAuOX6pfeIaYzbwjB69k3LlSNT19uv.VjTy','用户7777',NULL,NULL,NULL,1,'user','2026-03-19 14:17:50','2026-03-19 14:17:50',NULL),(8,'13900139001','$2a$10$g4derli.z/trbWUpDwQ/OuQJ7WpBDAf8ko19Vxiu9y1gGUnXSnHSm','Updated Nickname',NULL,'Test Real',NULL,1,'user','2026-03-19 14:24:35','2026-03-19 14:24:35',NULL),(9,'13800138099','$2a$10$JGQG.btJPsMLCEtCS3zLmOue1PE7YkQTqsXCmsf5z.ViFnq4Y3Auq','积分富翁','/uploads/2026/03/23/b5d411da074b4e37bd1181ac077e98db.png','',NULL,1,'user','2026-03-20 03:07:08','2026-03-20 03:07:08',NULL),(10,'13900139100','$2a$10$AkQJXArHoZ9sv4FsJX7UQuSWo6ensS8EsRlvbdAzV7lJa5nVu3qpm','NewUser',NULL,NULL,NULL,1,'user','2026-03-20 03:20:12','2026-03-20 03:20:12',NULL),(11,'13900139200','$2a$10$q5LH8HMJcfSSZ/N1ddNU2OPn9Rv89iGies8oFtRSe6oZbPtdWNtMa','AnotherUser',NULL,NULL,NULL,1,'user','2026-03-20 03:36:04','2026-03-20 03:36:04',NULL),(12,'13800138001','$2a$10$1mpR8CeaGAX2loffjPGgguOYg4W9rnnCx98lZFC0v8xgzrZA1vJSS','用户8001',NULL,NULL,NULL,1,'user','2026-03-21 09:48:05','2026-03-21 09:48:05',NULL),(13,'13000000000','$2a$10$qOxUlcVWJeoVh7HlsD6v3OMzlXEkH9y5H7jF8IAsKmMFoSvJKgxs.','用户0000',NULL,NULL,NULL,1,'user','2026-03-23 09:07:46','2026-03-23 09:07:46',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-23 18:25:18
