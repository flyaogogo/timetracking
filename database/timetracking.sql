/*
SQLyog Community v13.1.6 (64 bit)
MySQL - 8.0.22 : Database - timetracking
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`timetracking` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `timetracking`;

/*Table structure for table `milestone_tasks` */

DROP TABLE IF EXISTS `milestone_tasks`;

CREATE TABLE `milestone_tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `milestone_id` bigint NOT NULL COMMENT '里程碑ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `weight` decimal(5,2) DEFAULT '1.00' COMMENT '权重',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_milestone_task` (`milestone_id`,`task_id`),
  KEY `idx_milestone_id` (`milestone_id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='里程碑任务关联表';

/*Data for the table `milestone_tasks` */

/*Table structure for table `organizations` */

DROP TABLE IF EXISTS `organizations`;

CREATE TABLE `organizations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `org_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `org_type` enum('COMPANY','DEPARTMENT','TEAM','PROJECT_GROUP') COLLATE utf8mb4_unicode_ci DEFAULT 'DEPARTMENT',
  `org_level` int DEFAULT '1',
  `manager_id` bigint DEFAULT NULL,
  `cost_center` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `budget_amount` decimal(15,2) DEFAULT '0.00',
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `org_code` (`org_code`),
  KEY `parent_id` (`parent_id`),
  KEY `manager_id` (`manager_id`),
  CONSTRAINT `organizations_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `organizations` (`id`),
  CONSTRAINT `organizations_ibfk_2` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `organizations` */

insert  into `organizations`(`id`,`org_name`,`org_code`,`parent_id`,`org_type`,`org_level`,`manager_id`,`cost_center`,`budget_amount`,`status`,`create_time`) values 
(1,'Tech Company','TECH_COMPANY',NULL,'COMPANY',1,1,'COMPANY',10000000.00,'ACTIVE','2025-12-27 10:59:52'),
(2,'R&D Department','RD_DEPT',1,'DEPARTMENT',2,2,'RD_CENTER',5000000.00,'ACTIVE','2025-12-27 10:59:52'),
(3,'QA Department','QA_DEPT',1,'DEPARTMENT',2,3,'QA_CENTER',2000000.00,'ACTIVE','2025-12-27 10:59:52'),
(4,'Backend Team','BACKEND_TEAM',2,'TEAM',3,3,'RD_CENTER',2000000.00,'ACTIVE','2025-12-27 10:59:52'),
(5,'Frontend Team','FRONTEND_TEAM',2,'TEAM',3,4,'RD_CENTER',1500000.00,'ACTIVE','2025-12-27 10:59:52'),
(6,'研发部','R&DCode',1,'DEPARTMENT',2,3,'研发',6000000.00,'ACTIVE','2025-12-29 14:24:48');

/*Table structure for table `project_costs` */

DROP TABLE IF EXISTS `project_costs`;

CREATE TABLE `project_costs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL COMMENT 'Project ID',
  `cost_type` enum('LABOR','EQUIPMENT','TRAVEL','OUTSOURCING','SOFTWARE','HARDWARE','OTHER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `cost_category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Cost category',
  `amount` decimal(15,2) NOT NULL COMMENT 'Amount',
  `currency` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'CNY' COMMENT 'Currency',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'Cost description',
  `incurred_date` date NOT NULL COMMENT 'Incurred date',
  `approved_by` bigint DEFAULT NULL COMMENT 'Approved by',
  `approval_date` date DEFAULT NULL COMMENT 'Approval date',
  `status` enum('PENDING','APPROVED','REJECTED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT 'Approval status',
  `invoice_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Invoice number',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `cost_variance` decimal(15,2) DEFAULT '0.00',
  `budget_impact` enum('WITHIN_BUDGET','BUDGET_REALLOCATION','BUDGET_INCREASE') COLLATE utf8mb4_unicode_ci DEFAULT 'WITHIN_BUDGET',
  PRIMARY KEY (`id`),
  KEY `project_id` (`project_id`),
  KEY `approved_by` (`approved_by`),
  CONSTRAINT `project_costs_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`),
  CONSTRAINT `project_costs_ibfk_2` FOREIGN KEY (`approved_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Project costs table';

/*Data for the table `project_costs` */

insert  into `project_costs`(`id`,`project_id`,`cost_type`,`cost_category`,`amount`,`currency`,`description`,`incurred_date`,`approved_by`,`approval_date`,`status`,`invoice_number`,`create_time`,`update_time`,`cost_variance`,`budget_impact`) values 
(4,1,'LABOR','开发人员',150000.00,'CNY','1月份开发人员工资','2024-01-31',NULL,NULL,'APPROVED',NULL,'2025-12-29 17:56:40','2025-12-29 17:56:40',0.00,'WITHIN_BUDGET'),
(5,1,'EQUIPMENT','开发设备',25000.00,'CNY','购买开发用笔记本电脑','2024-01-15',NULL,NULL,'APPROVED',NULL,'2025-12-29 17:57:01','2025-12-29 17:57:01',0.00,'WITHIN_BUDGET'),
(6,1,'SOFTWARE',NULL,8000.00,'CNY','Visual Studio License','2024-02-01',NULL,NULL,'APPROVED',NULL,'2025-12-30 10:58:46','2025-12-30 10:58:46',0.00,'WITHIN_BUDGET'),
(7,1,'TRAVEL',NULL,3500.00,'CNY','Business Travel','2024-02-15',NULL,NULL,'APPROVED',NULL,'2025-12-30 10:59:00','2025-12-30 10:59:00',0.00,'WITHIN_BUDGET'),
(8,1,'OUTSOURCING',NULL,45000.00,'CNY','UI Design Service','2024-03-01',NULL,NULL,'PENDING',NULL,'2025-12-30 10:59:00','2025-12-30 10:59:00',0.00,'WITHIN_BUDGET'),
(9,2,'HARDWARE',NULL,15000.00,'CNY','Test Server','2024-01-20',NULL,NULL,'APPROVED',NULL,'2025-12-30 10:59:00','2025-12-30 10:59:00',0.00,'WITHIN_BUDGET'),
(10,1,'EQUIPMENT',NULL,1000.00,'CNY','测试成本记录','2025-12-30',NULL,NULL,'PENDING',NULL,'2025-12-30 11:04:24','2025-12-30 11:04:24',0.00,'WITHIN_BUDGET'),
(11,1,'SOFTWARE',NULL,1500.00,'CNY','API?????????','2025-12-30',NULL,NULL,'PENDING',NULL,'2025-12-30 11:04:33','2025-12-30 11:04:33',0.00,'WITHIN_BUDGET'),
(12,1,'SOFTWARE',NULL,1500.00,'CNY','API?????????','2025-12-30',NULL,NULL,'PENDING',NULL,'2025-12-30 11:05:04','2025-12-30 11:05:04',0.00,'WITHIN_BUDGET'),
(14,3,'OTHER',NULL,13000.00,'CNY','笔记本电脑2台','2025-12-22',NULL,NULL,'PENDING','','2025-12-30 11:10:30','2025-12-30 11:10:30',0.00,'WITHIN_BUDGET');

/*Table structure for table `project_members` */

DROP TABLE IF EXISTS `project_members`;

CREATE TABLE `project_members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` enum('MANAGER','DEVELOPER','TESTER','DESIGNER') DEFAULT 'DEVELOPER' COMMENT '项目角色',
  `join_date` date DEFAULT (curdate()) COMMENT '加入日期',
  `allocation_percentage` decimal(5,2) DEFAULT '100.00' COMMENT '分配百分比(%)',
  `hourly_rate` decimal(10,2) DEFAULT '600.00' COMMENT '小时费率',
  `skill_level` enum('JUNIOR','INTERMEDIATE','SENIOR','EXPERT') DEFAULT 'INTERMEDIATE' COMMENT '技能等级',
  `performance_rating` decimal(3,1) DEFAULT '3.5' COMMENT '绩效评分(1-5)',
  `productivity_index` decimal(5,2) DEFAULT '1.10' COMMENT '生产力指数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_project_manager` tinyint(1) DEFAULT '0' COMMENT 'Is project manager',
  `is_tech_leader` tinyint(1) DEFAULT '0' COMMENT 'Is tech leader',
  `can_approve_timesheet` tinyint(1) DEFAULT '0' COMMENT 'Can approve timesheet',
  `can_manage_tasks` tinyint(1) DEFAULT '0' COMMENT 'Can manage tasks',
  `can_view_reports` tinyint(1) DEFAULT '0' COMMENT 'Can view reports',
  `effective_date` date DEFAULT (curdate()) COMMENT 'Effective date',
  `expiry_date` date DEFAULT NULL COMMENT 'Expiry date',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_project_members_manager` (`is_project_manager`),
  KEY `idx_project_members_tech_leader` (`is_tech_leader`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目成员表';

/*Data for the table `project_members` */

insert  into `project_members`(`id`,`project_id`,`user_id`,`role`,`join_date`,`allocation_percentage`,`hourly_rate`,`skill_level`,`performance_rating`,`productivity_index`,`create_time`,`update_time`,`is_project_manager`,`is_tech_leader`,`can_approve_timesheet`,`can_manage_tasks`,`can_view_reports`,`effective_date`,`expiry_date`) values 
(1,1,1,'MANAGER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 08:28:24','2025-12-30 23:20:13',0,0,0,0,0,'2025-12-30',NULL),
(2,1,2,'MANAGER','2025-12-30',100.00,150.00,'SENIOR',4.0,1.20,'2025-12-30 08:28:24','2025-12-30 22:23:18',1,0,1,1,1,'2025-12-30',NULL),
(3,1,3,'DEVELOPER','2025-12-30',80.00,120.00,'INTERMEDIATE',3.5,1.10,'2025-12-30 08:28:24','2025-12-30 22:23:18',0,1,1,1,1,'2025-12-30',NULL),
(4,1,4,'TESTER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 08:28:24','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(5,2,1,'MANAGER','2025-12-30',100.00,150.00,'SENIOR',4.0,1.20,'2025-12-30 08:28:24','2025-12-30 22:23:18',1,0,1,1,1,'2025-12-30',NULL),
(6,2,3,'DEVELOPER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 08:28:24','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(7,2,5,'DESIGNER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 08:28:24','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(8,3,2,'MANAGER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 08:28:24','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(9,3,3,'DEVELOPER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 08:28:24','2025-12-31 08:20:46',0,1,0,1,1,'2025-12-30',NULL),
(10,3,4,'TESTER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 08:28:24','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(18,2,6,'DEVELOPER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 10:33:57','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(19,4,1,'MANAGER','2025-12-30',100.00,150.00,'SENIOR',4.0,1.20,'2025-12-30 08:36:37','2025-12-30 22:23:18',1,0,1,1,1,'2025-12-30',NULL),
(21,5,3,'MANAGER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 08:36:37','2025-12-31 13:39:46',1,0,1,1,1,'2025-12-30',NULL),
(22,4,2,'MANAGER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 08:36:37','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(23,5,2,'MANAGER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 08:36:37','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(25,5,5,'TESTER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 08:36:37','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(26,5,6,'DEVELOPER','2025-12-30',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 14:05:53','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(27,3,6,'MANAGER','2025-12-30',100.00,150.00,'SENIOR',4.0,1.20,'2025-12-30 15:48:35','2025-12-31 13:39:33',1,0,1,1,1,'2025-12-30',NULL),
(28,1,5,'TESTER','2025-01-03',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 22:23:18','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(29,2,4,'DEVELOPER','2025-01-02',100.00,100.00,'INTERMEDIATE',3.0,1.00,'2025-12-30 22:23:18','2025-12-30 22:23:18',0,0,0,0,1,'2025-12-30',NULL),
(36,3,1,'MANAGER','2025-12-31',100.00,800.00,'EXPERT',4.5,1.20,'2025-12-31 11:36:56','2025-12-31 15:00:39',0,0,0,0,0,'2025-12-31',NULL),
(37,4,9,'MANAGER','2025-12-31',100.00,600.00,'INTERMEDIATE',3.5,1.10,'2025-12-31 15:09:35','2025-12-31 15:13:28',0,0,0,0,0,'2025-12-31',NULL),
(38,7,7,'MANAGER','2025-12-31',100.00,600.00,'INTERMEDIATE',3.0,1.10,'2025-12-31 20:12:18','2025-12-31 20:12:17',0,0,0,0,0,'2025-12-31',NULL);

/*Table structure for table `project_members_backup` */

DROP TABLE IF EXISTS `project_members_backup`;

CREATE TABLE `project_members_backup` (
  `id` bigint NOT NULL DEFAULT '0',
  `project_id` bigint NOT NULL COMMENT 'Project ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `role` enum('MANAGER','DEVELOPER','TESTER','DESIGNER') COLLATE utf8mb4_unicode_ci DEFAULT 'DEVELOPER' COMMENT 'Project role',
  `join_date` date DEFAULT (curdate()) COMMENT 'Join date',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `allocation_percentage` decimal(5,2) DEFAULT '100.00',
  `hourly_rate` decimal(10,2) DEFAULT '0.00',
  `skill_level` enum('JUNIOR','INTERMEDIATE','SENIOR','EXPERT') COLLATE utf8mb4_unicode_ci DEFAULT 'INTERMEDIATE',
  `performance_rating` decimal(3,1) DEFAULT '0.0',
  `productivity_index` decimal(5,2) DEFAULT '1.00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `project_members_backup` */

insert  into `project_members_backup`(`id`,`project_id`,`user_id`,`role`,`join_date`,`create_time`,`allocation_percentage`,`hourly_rate`,`skill_level`,`performance_rating`,`productivity_index`) values 
(7,1,1,'MANAGER','2024-01-01','2025-12-29 18:31:14',100.00,0.00,'EXPERT',4.5,1.30),
(8,1,2,'DEVELOPER','2024-01-15','2025-12-29 18:31:14',100.00,0.00,'SENIOR',4.2,1.20),
(9,1,3,'DEVELOPER','2024-02-01','2025-12-29 18:31:14',80.00,0.00,'INTERMEDIATE',3.8,1.10),
(10,1,5,'TESTER','2024-02-15','2025-12-29 18:31:14',100.00,0.00,'INTERMEDIATE',4.0,1.00);

/*Table structure for table `project_milestones` */

DROP TABLE IF EXISTS `project_milestones`;

CREATE TABLE `project_milestones` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL,
  `milestone_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `milestone_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'DEVELOPMENT',
  `planned_date` date NOT NULL,
  `actual_date` date DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `completion_percentage` int DEFAULT '0',
  `health_status` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'GREEN',
  `responsible_person_id` bigint DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `deliverables` text COLLATE utf8mb4_unicode_ci,
  `acceptance_criteria` text COLLATE utf8mb4_unicode_ci,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `project_milestones` */

insert  into `project_milestones`(`id`,`project_id`,`milestone_name`,`milestone_type`,`planned_date`,`actual_date`,`status`,`completion_percentage`,`health_status`,`responsible_person_id`,`description`,`deliverables`,`acceptance_criteria`,`create_time`,`update_time`) values 
(1,1,'需求分析完成','PLANNING','2024-02-15','2024-02-14','COMPLETED',100,'GREEN',2,'完成项目需求分析和设计',NULL,NULL,'2025-12-30 11:18:45','2025-12-30 11:18:45'),
(2,1,'核心功能开发','DEVELOPMENT','2024-04-15',NULL,'IN_PROGRESS',75,'GREEN',4,'完成核心功能模块开发',NULL,NULL,'2025-12-30 11:18:45','2025-12-30 11:18:45'),
(3,1,'系统测试完成','TESTING','2024-05-01',NULL,'PENDING',0,'GREEN',5,'完成系统功能测试',NULL,NULL,'2025-12-30 11:18:45','2025-12-30 11:18:45'),
(4,1,'API测试里程碑','TESTING','2025-01-30',NULL,'DELAYED',0,'GREEN',2,'通过API创建的测试里程碑',NULL,NULL,'2025-12-30 11:28:14','2025-12-30 11:28:14'),
(6,1,'东想西想木ss','DEVELOPMENT','2025-12-16',NULL,'DELAYED',0,'GREEN',6,'sDSAD','sadSD','sdSD','2025-12-30 11:29:11','2025-12-30 11:29:11'),
(7,3,'完成需求设计','PLANNING','2025-12-27',NULL,'DELAYED',0,'GREEN',6,'完成需求设计相关的工作内容','需求文档','文档可执行。','2025-12-30 11:30:31','2025-12-30 11:30:31'),
(8,1,'API测试里程碑','TESTING','2025-01-30',NULL,'DELAYED',0,'GREEN',2,'通过API创建的测试里程碑',NULL,NULL,'2025-12-30 12:07:35','2025-12-30 12:07:35');

/*Table structure for table `project_permission_logs` */

DROP TABLE IF EXISTS `project_permission_logs`;

CREATE TABLE `project_permission_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL COMMENT 'Project ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `permission_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Permission type',
  `action` enum('GRANT','REVOKE','MODIFY') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Action type',
  `old_value` text COLLATE utf8mb4_unicode_ci COMMENT 'Old value',
  `new_value` text COLLATE utf8mb4_unicode_ci COMMENT 'New value',
  `operated_by` bigint NOT NULL COMMENT 'Operated by user ID',
  `operation_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Operation reason',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `project_id` (`project_id`),
  KEY `user_id` (`user_id`),
  KEY `operated_by` (`operated_by`),
  CONSTRAINT `project_permission_logs_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`),
  CONSTRAINT `project_permission_logs_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `project_permission_logs_ibfk_3` FOREIGN KEY (`operated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Project permission change logs';

/*Data for the table `project_permission_logs` */

/*Table structure for table `project_role_permissions` */

DROP TABLE IF EXISTS `project_role_permissions`;

CREATE TABLE `project_role_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL COMMENT 'Project ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `permission_type` enum('PROJECT_MANAGEMENT','TASK_MANAGEMENT','TIMESHEET_APPROVAL','REPORT_VIEW','COST_MANAGEMENT','MILESTONE_MANAGEMENT','TEAM_MANAGEMENT') COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Permission type',
  `granted_by` bigint DEFAULT NULL COMMENT 'Granted by user ID',
  `granted_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Granted date',
  `effective_date` date DEFAULT (curdate()) COMMENT 'Effective date',
  `expiry_date` date DEFAULT NULL COMMENT 'Expiry date',
  `is_active` tinyint(1) DEFAULT '1' COMMENT 'Is active',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user_permission` (`project_id`,`user_id`,`permission_type`),
  KEY `granted_by` (`granted_by`),
  KEY `idx_project_permissions_project_user` (`project_id`,`user_id`),
  KEY `idx_project_permissions_user` (`user_id`),
  KEY `idx_project_permissions_type` (`permission_type`),
  KEY `idx_project_permissions_active` (`is_active`,`effective_date`,`expiry_date`),
  CONSTRAINT `project_role_permissions_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `project_role_permissions_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `project_role_permissions_ibfk_3` FOREIGN KEY (`granted_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Project role permissions table';

/*Data for the table `project_role_permissions` */

insert  into `project_role_permissions`(`id`,`project_id`,`user_id`,`permission_type`,`granted_by`,`granted_date`,`effective_date`,`expiry_date`,`is_active`,`create_time`,`update_time`) values 
(1,1,2,'PROJECT_MANAGEMENT',1,'2025-12-30 23:28:46','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(2,2,1,'PROJECT_MANAGEMENT',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(3,4,1,'PROJECT_MANAGEMENT',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(4,3,6,'PROJECT_MANAGEMENT',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(5,1,2,'TASK_MANAGEMENT',1,'2025-12-30 23:28:46','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(6,2,1,'TASK_MANAGEMENT',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(7,4,1,'TASK_MANAGEMENT',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(8,3,6,'TASK_MANAGEMENT',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(9,1,2,'TIMESHEET_APPROVAL',1,'2025-12-30 23:28:46','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(10,2,1,'TIMESHEET_APPROVAL',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(11,4,1,'TIMESHEET_APPROVAL',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(12,3,6,'TIMESHEET_APPROVAL',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(13,1,2,'REPORT_VIEW',1,'2025-12-30 23:28:46','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(14,2,1,'REPORT_VIEW',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(15,4,1,'REPORT_VIEW',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(16,3,6,'REPORT_VIEW',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(17,1,2,'TEAM_MANAGEMENT',1,'2025-12-30 23:28:46','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(18,2,1,'TEAM_MANAGEMENT',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(19,4,1,'TEAM_MANAGEMENT',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(20,3,6,'TEAM_MANAGEMENT',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(32,1,3,'TASK_MANAGEMENT',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(33,1,3,'REPORT_VIEW',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(34,1,3,'MILESTONE_MANAGEMENT',1,'2025-12-30 17:46:50','2025-12-30',NULL,1,'2025-12-30 17:46:50','2025-12-30 17:46:50'),
(35,1,1,'PROJECT_MANAGEMENT',1,'2025-12-30 23:20:13','2025-12-30',NULL,0,'2025-12-30 23:20:13','2025-12-30 23:20:13'),
(36,1,1,'TASK_MANAGEMENT',1,'2025-12-30 23:20:13','2025-12-30',NULL,0,'2025-12-30 23:20:13','2025-12-30 23:20:13'),
(37,1,1,'TIMESHEET_APPROVAL',1,'2025-12-30 23:20:13','2025-12-30',NULL,0,'2025-12-30 23:20:13','2025-12-30 23:20:13'),
(38,1,1,'REPORT_VIEW',1,'2025-12-30 23:20:13','2025-12-30',NULL,0,'2025-12-30 23:20:13','2025-12-30 23:20:13'),
(39,1,1,'TEAM_MANAGEMENT',1,'2025-12-30 23:20:13','2025-12-30',NULL,0,'2025-12-30 23:20:13','2025-12-30 23:20:13'),
(40,5,3,'PROJECT_MANAGEMENT',1,'2025-12-31 10:26:52','2025-12-30',NULL,1,'2025-12-30 23:30:43','2025-12-30 23:30:43'),
(41,5,3,'TASK_MANAGEMENT',1,'2025-12-31 10:26:52','2025-12-30',NULL,1,'2025-12-30 23:30:43','2025-12-30 23:30:43'),
(42,5,3,'TIMESHEET_APPROVAL',1,'2025-12-31 10:26:52','2025-12-30',NULL,1,'2025-12-30 23:30:43','2025-12-30 23:30:43'),
(43,5,3,'REPORT_VIEW',1,'2025-12-31 10:26:52','2025-12-30',NULL,1,'2025-12-30 23:30:43','2025-12-30 23:30:43'),
(44,5,3,'TEAM_MANAGEMENT',1,'2025-12-31 10:26:52','2025-12-30',NULL,1,'2025-12-30 23:30:43','2025-12-30 23:30:43'),
(45,3,3,'TASK_MANAGEMENT',6,'2025-12-31 08:20:46','2025-12-31',NULL,1,'2025-12-31 08:20:46','2025-12-31 08:20:46'),
(46,3,3,'REPORT_VIEW',6,'2025-12-31 08:20:47','2025-12-31',NULL,1,'2025-12-31 08:20:47','2025-12-31 08:20:47'),
(47,3,3,'MILESTONE_MANAGEMENT',6,'2025-12-31 08:20:47','2025-12-31',NULL,1,'2025-12-31 08:20:47','2025-12-31 08:20:47'),
(48,5,3,'COST_MANAGEMENT',1,'2025-12-31 10:26:52','2025-12-31',NULL,1,'2025-12-31 10:26:52','2025-12-31 10:26:52'),
(49,5,3,'MILESTONE_MANAGEMENT',1,'2025-12-31 10:26:52','2025-12-31',NULL,1,'2025-12-31 10:26:52','2025-12-31 10:26:52'),
(50,4,9,'PROJECT_MANAGEMENT',1,'2025-12-31 15:12:52','2025-12-31',NULL,0,'2025-12-31 15:12:52','2025-12-31 15:12:58'),
(51,4,9,'TASK_MANAGEMENT',1,'2025-12-31 15:13:24','2025-12-31',NULL,1,'2025-12-31 15:12:52','2025-12-31 15:12:58'),
(52,4,9,'TIMESHEET_APPROVAL',1,'2025-12-31 15:12:52','2025-12-31',NULL,0,'2025-12-31 15:12:52','2025-12-31 15:12:58'),
(53,4,9,'REPORT_VIEW',1,'2025-12-31 15:13:24','2025-12-31',NULL,1,'2025-12-31 15:12:52','2025-12-31 15:12:58'),
(54,4,9,'TEAM_MANAGEMENT',1,'2025-12-31 15:12:52','2025-12-31',NULL,0,'2025-12-31 15:12:52','2025-12-31 15:12:58'),
(55,4,9,'MILESTONE_MANAGEMENT',1,'2025-12-31 15:13:24','2025-12-31',NULL,0,'2025-12-31 15:13:24','2025-12-31 15:13:28');

/*Table structure for table `projects` */

DROP TABLE IF EXISTS `projects`;

CREATE TABLE `projects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Project name',
  `project_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Project code',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'Project description',
  `project_type` enum('DEVELOPMENT','MAINTENANCE','RESEARCH') COLLATE utf8mb4_unicode_ci DEFAULT 'DEVELOPMENT' COMMENT 'Project type',
  `priority` enum('HIGH','MEDIUM','LOW') COLLATE utf8mb4_unicode_ci DEFAULT 'MEDIUM' COMMENT 'Priority',
  `estimated_hours` decimal(10,2) DEFAULT '0.00' COMMENT 'Estimated hours',
  `actual_hours` decimal(10,2) DEFAULT '0.00' COMMENT 'Actual hours',
  `contract_amount` decimal(15,2) DEFAULT '0.00' COMMENT 'Contract amount',
  `budget_amount` decimal(15,2) DEFAULT '0.00' COMMENT 'Budget amount',
  `labor_budget` decimal(15,2) DEFAULT '0.00' COMMENT 'Labor budget',
  `other_budget` decimal(15,2) DEFAULT '0.00' COMMENT 'Other budget',
  `actual_labor_cost` decimal(15,2) DEFAULT '0.00' COMMENT 'Actual labor cost',
  `actual_other_cost` decimal(15,2) DEFAULT '0.00' COMMENT 'Actual other cost',
  `planned_start_date` date DEFAULT NULL COMMENT 'Planned start date',
  `planned_end_date` date DEFAULT NULL COMMENT 'Planned end date',
  `actual_start_date` date DEFAULT NULL COMMENT 'Actual start date',
  `actual_end_date` date DEFAULT NULL COMMENT 'Actual end date',
  `start_date` date DEFAULT NULL COMMENT 'Start date',
  `end_date` date DEFAULT NULL COMMENT 'End date',
  `client_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Client name',
  `client_contact` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Client contact',
  `contract_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Contract number',
  `status` enum('PLANNING','IN_PROGRESS','COMPLETED','PAUSED','CANCELLED') COLLATE utf8mb4_unicode_ci DEFAULT 'PLANNING' COMMENT 'Project status',
  `manager_id` bigint DEFAULT NULL COMMENT 'Manager ID',
  `tech_leader_id` bigint DEFAULT NULL COMMENT 'Tech leader ID',
  `risk_level` enum('LOW','MEDIUM','HIGH','CRITICAL') COLLATE utf8mb4_unicode_ci DEFAULT 'LOW' COMMENT 'Risk level',
  `quality_score` decimal(5,2) DEFAULT '0.00' COMMENT 'Quality score',
  `client_satisfaction` decimal(5,2) DEFAULT '0.00' COMMENT 'Client satisfaction',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `baseline_budget` decimal(15,2) DEFAULT '0.00',
  `baseline_hours` decimal(10,2) DEFAULT '0.00',
  `earned_value` decimal(15,2) DEFAULT '0.00',
  `planned_value` decimal(15,2) DEFAULT '0.00',
  `cost_performance_index` decimal(5,4) DEFAULT '1.0000',
  `schedule_performance_index` decimal(5,4) DEFAULT '1.0000',
  `project_health_status` enum('GREEN','YELLOW','RED') COLLATE utf8mb4_unicode_ci DEFAULT 'GREEN',
  `completion_percentage` decimal(5,2) DEFAULT '0.00',
  `resource_utilization` decimal(5,2) DEFAULT '0.00',
  `team_velocity` decimal(8,2) DEFAULT '0.00',
  `burn_rate` decimal(15,2) DEFAULT '0.00',
  `roi_percentage` decimal(5,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `project_code` (`project_code`),
  KEY `manager_id` (`manager_id`),
  KEY `tech_leader_id` (`tech_leader_id`),
  CONSTRAINT `projects_ibfk_1` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`),
  CONSTRAINT `projects_ibfk_2` FOREIGN KEY (`tech_leader_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Projects table';

/*Data for the table `projects` */

insert  into `projects`(`id`,`project_name`,`project_code`,`description`,`project_type`,`priority`,`estimated_hours`,`actual_hours`,`contract_amount`,`budget_amount`,`labor_budget`,`other_budget`,`actual_labor_cost`,`actual_other_cost`,`planned_start_date`,`planned_end_date`,`actual_start_date`,`actual_end_date`,`start_date`,`end_date`,`client_name`,`client_contact`,`contract_number`,`status`,`manager_id`,`tech_leader_id`,`risk_level`,`quality_score`,`client_satisfaction`,`create_time`,`update_time`,`baseline_budget`,`baseline_hours`,`earned_value`,`planned_value`,`cost_performance_index`,`schedule_performance_index`,`project_health_status`,`completion_percentage`,`resource_utilization`,`team_velocity`,`burn_rate`,`roi_percentage`) values 
(1,'Time Tracking System','TMS001','Enterprise time tracking system development','DEVELOPMENT','HIGH',800.00,0.00,500000.00,450000.00,350000.00,100000.00,50000.00,10000.00,'2025-01-01','2025-06-30','2025-01-01',NULL,'2025-01-01','2025-06-30','中航信息',NULL,'CT-2024-001','IN_PROGRESS',2,3,'LOW',85.00,90.00,'2025-12-26 07:51:13','2026-01-04 09:35:09',450000.00,800.00,0.00,0.00,1.0000,1.0000,'YELLOW',0.00,0.00,0.00,0.00,0.00),
(2,'示例项目2','PROJ002','这是另一个示例项目','MAINTENANCE','MEDIUM',80.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,NULL,NULL,NULL,'2025-12-30','2026-02-13',NULL,NULL,NULL,'PLANNING',1,NULL,'LOW',0.00,0.00,'2025-12-30 08:28:24','2026-01-04 09:35:09',0.00,0.00,0.00,0.00,1.0000,1.0000,'GREEN',0.00,0.00,0.00,0.00,0.00),
(3,'国药APM','YZ0001','','DEVELOPMENT','MEDIUM',60.00,8.90,1200000.00,500000.00,32000.00,15000.00,0.00,0.00,NULL,NULL,NULL,NULL,'2025-12-21','2026-02-16',NULL,NULL,NULL,'PLANNING',6,NULL,'LOW',60.00,5.00,'2025-12-26 08:07:06','2026-01-04 09:35:09',400000.00,100.00,0.00,0.00,1.0000,1.0000,'GREEN',74.00,5.00,0.00,0.00,0.00),
(4,'Sample Project 1','PROJ001','This is a sample project','DEVELOPMENT','HIGH',100.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,NULL,NULL,NULL,'2025-12-30','2026-01-29',NULL,NULL,NULL,'IN_PROGRESS',1,NULL,'LOW',0.00,0.00,'2025-12-30 08:34:20','2026-01-04 09:35:09',0.00,0.00,0.00,0.00,1.0000,1.0000,'GREEN',0.00,0.00,0.00,0.00,0.00),
(5,'Sample Project 3','PROJ003','Third sample project','RESEARCH','LOW',120.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,NULL,NULL,NULL,'2025-12-30','2026-02-28',NULL,NULL,NULL,'IN_PROGRESS',1,NULL,'LOW',0.00,0.00,'2025-12-30 08:34:20','2026-01-04 09:35:09',0.00,0.00,0.00,0.00,1.0000,1.0000,'GREEN',0.00,0.00,0.00,0.00,0.00),
(7,'古井IAM','IAM001','古井IAM统一身份认证系统','DEVELOPMENT','MEDIUM',300.00,11.00,1250000.00,250000.00,80.00,15000.00,0.00,0.00,NULL,NULL,NULL,NULL,'2025-12-30','2026-03-25','古井集团',NULL,'','PLANNING',7,NULL,'MEDIUM',85.00,85.00,'2025-12-31 19:53:27','2026-01-04 09:35:09',0.00,0.00,0.00,0.00,1.0000,1.0000,'GREEN',5.00,80.00,0.00,0.00,0.00);

/*Table structure for table `tasks` */

DROP TABLE IF EXISTS `tasks`;

CREATE TABLE `tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Task name',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'Task description',
  `project_id` bigint NOT NULL COMMENT 'Project ID',
  `parent_id` bigint DEFAULT NULL COMMENT 'Parent task ID',
  `task_type` enum('DEVELOPMENT','TESTING','DESIGN','DOCUMENT') COLLATE utf8mb4_unicode_ci DEFAULT 'DEVELOPMENT' COMMENT 'Task type',
  `priority` int DEFAULT '3' COMMENT 'Priority 1-5',
  `difficulty` int DEFAULT '3' COMMENT 'Difficulty 1-5',
  `estimated_hours` decimal(8,2) DEFAULT '0.00' COMMENT 'Estimated hours',
  `actual_hours` decimal(8,2) DEFAULT '0.00' COMMENT 'Actual hours',
  `progress` int DEFAULT '0' COMMENT 'Progress 0-100',
  `status` enum('TODO','IN_PROGRESS','REVIEW','COMPLETED','PAUSED','CANCELLED') COLLATE utf8mb4_unicode_ci DEFAULT 'TODO' COMMENT 'Task status',
  `assignee_id` bigint DEFAULT NULL COMMENT 'Assignee ID',
  `reviewer_id` bigint DEFAULT NULL COMMENT 'Reviewer ID',
  `start_date` date DEFAULT NULL COMMENT 'Start date',
  `end_date` date DEFAULT NULL COMMENT 'End date',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `baseline_hours` decimal(8,2) DEFAULT '0.00',
  `actual_start_date` date DEFAULT NULL,
  `actual_end_date` date DEFAULT NULL,
  `completion_percentage` decimal(5,2) DEFAULT '0.00',
  `effort_variance` decimal(8,2) DEFAULT '0.00',
  `task_complexity` enum('LOW','MEDIUM','HIGH','VERY_HIGH') COLLATE utf8mb4_unicode_ci DEFAULT 'MEDIUM',
  `business_value` int DEFAULT '3',
  `quality_score` decimal(5,2) DEFAULT '0.00',
  `story_points` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `project_id` (`project_id`),
  KEY `parent_id` (`parent_id`),
  KEY `assignee_id` (`assignee_id`),
  KEY `reviewer_id` (`reviewer_id`),
  CONSTRAINT `tasks_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`),
  CONSTRAINT `tasks_ibfk_2` FOREIGN KEY (`parent_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `tasks_ibfk_3` FOREIGN KEY (`assignee_id`) REFERENCES `users` (`id`),
  CONSTRAINT `tasks_ibfk_4` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tasks table';

/*Data for the table `tasks` */

insert  into `tasks`(`id`,`task_name`,`description`,`project_id`,`parent_id`,`task_type`,`priority`,`difficulty`,`estimated_hours`,`actual_hours`,`progress`,`status`,`assignee_id`,`reviewer_id`,`start_date`,`end_date`,`create_time`,`update_time`,`baseline_hours`,`actual_start_date`,`actual_end_date`,`completion_percentage`,`effort_variance`,`task_complexity`,`business_value`,`quality_score`,`story_points`) values 
(1,'需求分析','xxx',3,NULL,'DESIGN',4,3,2.00,4.00,100,'COMPLETED',6,NULL,'2025-12-25','2025-12-26','2025-12-26 09:15:44','2026-01-04 09:35:09',0.00,NULL,NULL,0.00,0.00,'MEDIUM',3,0.00,0),
(2,'拓扑报表展示','',3,NULL,'DEVELOPMENT',3,3,5.00,0.00,0,'TODO',3,NULL,'2025-12-28','2025-12-30','2025-12-26 09:17:37','2026-01-04 09:35:09',0.00,NULL,NULL,0.00,0.00,'MEDIUM',3,0.00,0),
(3,'模块一开发','',3,NULL,'DEVELOPMENT',3,3,2.00,4.90,100,'COMPLETED',3,NULL,'2025-12-24','2025-12-25','2025-12-26 09:57:07','2026-01-04 09:35:09',0.00,NULL,NULL,0.00,0.00,'MEDIUM',3,0.00,0),
(4,'需求任务2','需求任务2',3,NULL,'DESIGN',3,3,3.00,0.00,0,'TODO',6,NULL,'2025-12-29','2025-12-29','2025-12-30 15:13:56','2026-01-04 09:35:09',0.00,NULL,NULL,0.00,0.00,'MEDIUM',3,85.00,1),
(5,'系统架构设计','设计时间跟踪系统的整体架构',1,NULL,'DEVELOPMENT',1,3,16.00,0.00,0,'TODO',1,NULL,NULL,NULL,'2025-12-30 15:18:09','2026-01-04 09:35:09',0.00,NULL,NULL,0.00,0.00,'MEDIUM',3,0.00,0),
(6,'数据库设计','设计系统数据库表结构',1,NULL,'DEVELOPMENT',2,3,8.00,0.00,100,'COMPLETED',1,NULL,NULL,NULL,'2025-12-30 15:18:09','2026-01-04 09:35:09',0.00,NULL,NULL,0.00,0.00,'MEDIUM',3,0.00,0),
(7,'前端开发','开发Vue3前端界面',1,NULL,'DEVELOPMENT',2,3,24.00,0.00,0,'TODO',1,NULL,NULL,NULL,'2025-12-30 15:18:09','2026-01-04 09:35:09',0.00,NULL,NULL,0.00,0.00,'MEDIUM',3,0.00,0),
(8,'API接口开发','开发后端REST API接口',1,NULL,'DEVELOPMENT',1,3,20.00,0.00,0,'TODO',1,NULL,NULL,NULL,'2025-12-30 15:18:09','2026-01-04 09:35:09',0.00,NULL,NULL,0.00,0.00,'MEDIUM',3,0.00,0),
(9,'测试用例编写','编写系统测试用例',2,NULL,'DEVELOPMENT',3,3,12.00,0.00,0,'TODO',1,NULL,NULL,NULL,'2025-12-30 15:18:09','2026-01-04 09:35:09',0.00,NULL,NULL,0.00,0.00,'MEDIUM',3,0.00,0),
(10,'IAM项目调研输出蓝图','IAM项目调研，整理蓝图',7,NULL,'DESIGN',3,3,40.00,11.00,27,'IN_PROGRESS',7,1,'2025-12-30',NULL,'2025-12-31 19:55:12','2026-01-04 09:35:09',0.00,NULL,NULL,0.00,0.00,'MEDIUM',3,85.00,1),
(11,'项目接入','外部13系统接入IAM',7,NULL,'DEVELOPMENT',3,3,160.00,0.00,0,'TODO',10,NULL,'2025-12-30','2026-01-29','2025-12-31 20:16:56','2026-01-04 09:35:09',2.00,NULL,NULL,0.00,0.00,'MEDIUM',3,85.00,5);

/*Table structure for table `time_entries` */

DROP TABLE IF EXISTS `time_entries`;

CREATE TABLE `time_entries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `project_id` bigint NOT NULL COMMENT 'Project ID',
  `task_id` bigint DEFAULT NULL COMMENT 'Task ID',
  `work_date` date NOT NULL COMMENT 'Work date',
  `start_time` time DEFAULT NULL COMMENT 'Start time',
  `end_time` time DEFAULT NULL COMMENT 'End time',
  `duration` decimal(4,2) NOT NULL COMMENT 'Duration in hours',
  `work_type` enum('NORMAL','OVERTIME','HOLIDAY') COLLATE utf8mb4_unicode_ci DEFAULT 'NORMAL' COMMENT 'Work type',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'Work description',
  `status` enum('PENDING','APPROVED','REJECTED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT 'Approval status',
  `approver_id` bigint DEFAULT NULL COMMENT 'Approver ID',
  `approve_time` datetime DEFAULT NULL COMMENT 'Approval time',
  `approve_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Approval comment',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `activity_type` enum('CODING','TESTING','DEBUGGING','MEETING','DOCUMENTATION','RESEARCH','REVIEW','ANALYSIS','LEARNING') COLLATE utf8mb4_unicode_ci DEFAULT 'CODING',
  `productivity_score` decimal(5,2) DEFAULT '0.00',
  `billable_flag` tinyint(1) DEFAULT '1',
  `billing_rate` decimal(10,2) DEFAULT '0.00',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `project_id` (`project_id`),
  KEY `task_id` (`task_id`),
  KEY `approver_id` (`approver_id`),
  CONSTRAINT `time_entries_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `time_entries_ibfk_2` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`),
  CONSTRAINT `time_entries_ibfk_3` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `time_entries_ibfk_4` FOREIGN KEY (`approver_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Time entries table';

/*Data for the table `time_entries` */

insert  into `time_entries`(`id`,`user_id`,`project_id`,`task_id`,`work_date`,`start_time`,`end_time`,`duration`,`work_type`,`description`,`status`,`approver_id`,`approve_time`,`approve_comment`,`create_time`,`update_time`,`activity_type`,`productivity_score`,`billable_flag`,`billing_rate`) values 
(7,6,3,1,'2025-12-26','09:18:00','11:18:00',2.00,'NORMAL','初版内容','APPROVED',6,'2025-12-26 09:22:07','审核通过','2025-12-26 09:18:46','2025-12-26 09:18:46','CODING',0.00,1,0.00),
(8,6,3,1,'2025-12-26','09:22:11','11:22:11',2.00,'NORMAL','编写模块一','APPROVED',6,'2025-12-29 12:28:46','审核通过','2025-12-26 09:22:43','2025-12-26 09:22:43','CODING',0.00,1,0.00),
(9,3,3,3,'2025-12-27','06:47:57','11:43:57',4.90,'NORMAL','adfadf','APPROVED',7,'2025-12-31 20:12:25','审核通过','2025-12-27 11:48:40','2025-12-27 11:48:40','CODING',0.00,1,0.00),
(10,1,3,NULL,'2025-12-29','15:47:07','19:50:07',4.10,'NORMAL','调试接口，模块报错','APPROVED',6,'2025-12-31 12:40:47','审核通过','2025-12-29 18:48:05','2025-12-29 18:48:05','CODING',0.00,1,0.00),
(11,6,2,4,'2025-01-01','10:00:00','11:00:00',1.00,'NORMAL','测试TESTING活动类型','PENDING',NULL,NULL,NULL,'2025-12-31 20:05:10','2025-12-31 20:05:10','TESTING',0.00,1,0.00),
(12,6,2,4,'2025-01-01','11:00:00','12:00:00',1.00,'NORMAL','测试DEBUGGING活动类型','PENDING',NULL,NULL,NULL,'2025-12-31 20:05:10','2025-12-31 20:05:10','DEBUGGING',0.00,1,0.00),
(13,6,2,4,'2025-01-01','12:00:00','13:00:00',1.00,'NORMAL','测试MEETING活动类型','PENDING',NULL,NULL,NULL,'2025-12-31 20:05:10','2025-12-31 20:05:10','MEETING',0.00,1,0.00),
(14,6,2,4,'2025-01-01','13:00:00','14:00:00',1.00,'NORMAL','测试DOCUMENTATION活动类型','PENDING',NULL,NULL,NULL,'2025-12-31 20:05:10','2025-12-31 20:05:10','DOCUMENTATION',0.00,1,0.00),
(15,6,2,4,'2025-01-01','14:00:00','15:00:00',1.00,'NORMAL','测试RESEARCH活动类型','PENDING',NULL,NULL,NULL,'2025-12-31 20:05:10','2025-12-31 20:05:10','RESEARCH',0.00,1,0.00),
(16,6,2,4,'2025-01-01','15:00:00','16:00:00',1.00,'NORMAL','测试REVIEW活动类型','PENDING',NULL,NULL,NULL,'2025-12-31 20:05:10','2025-12-31 20:05:10','REVIEW',0.00,1,0.00),
(17,6,2,4,'2025-01-01','09:00:00','12:00:00',3.00,'NORMAL','????????','PENDING',NULL,NULL,NULL,'2025-12-31 20:09:48','2025-12-31 20:09:48','ANALYSIS',0.00,1,0.00),
(18,6,3,5,'2025-01-01','14:00:00','16:00:00',2.00,'NORMAL','?????','PENDING',NULL,NULL,NULL,'2025-12-31 20:10:07','2025-12-31 20:10:07','LEARNING',0.00,1,0.00),
(19,7,7,10,'2025-12-31','09:54:54','17:55:54',8.00,'NORMAL','与客户沟通，输出调研模板','APPROVED',1,'2025-12-31 20:19:50','审核通过','2025-12-31 20:10:13','2025-12-31 20:10:13','ANALYSIS',0.00,1,0.00),
(20,6,2,4,'2025-01-01','16:00:00','17:00:00',1.00,'NORMAL','????','PENDING',NULL,NULL,NULL,'2025-12-31 20:11:02','2025-12-31 20:11:02','CODING',0.00,1,0.00),
(21,7,7,10,'2025-12-31','11:36:15','14:36:15',3.00,'NORMAL','编写模板，供客户下发厂商填写。','APPROVED',1,'2025-12-31 20:39:30','审核通过','2025-12-31 20:38:38','2025-12-31 20:38:38','DOCUMENTATION',0.00,1,0.00);

/*Table structure for table `user_organizations` */

DROP TABLE IF EXISTS `user_organizations`;

CREATE TABLE `user_organizations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `organization_id` bigint NOT NULL,
  `is_primary` tinyint(1) DEFAULT '0',
  `position` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `join_date` date DEFAULT (curdate()),
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `organization_id` (`organization_id`),
  CONSTRAINT `user_organizations_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `user_organizations_ibfk_2` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `user_organizations` */

insert  into `user_organizations`(`id`,`user_id`,`organization_id`,`is_primary`,`position`,`join_date`,`create_time`) values 
(1,1,1,1,'CEO','2025-12-27','2025-12-27 10:59:52'),
(2,2,2,1,'R&D Director','2025-12-27','2025-12-27 10:59:52'),
(3,3,4,1,'Backend Lead','2025-12-27','2025-12-27 10:59:52'),
(4,4,5,1,'Frontend Lead','2025-12-27','2025-12-27 10:59:52'),
(5,5,3,1,'QA Lead','2025-12-27','2025-12-27 10:59:52'),
(7,6,4,1,'产品经理','2025-12-29','2025-12-29 16:02:56'),
(8,7,6,1,'研发经理','2025-12-29','2025-12-29 16:05:18'),
(9,9,6,1,'后端工程师','2025-12-31','2025-12-31 15:07:48'),
(10,10,6,1,'Java开发','2025-12-31','2025-12-31 15:08:53');

/*Table structure for table `user_preferences` */

DROP TABLE IF EXISTS `user_preferences`;

CREATE TABLE `user_preferences` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `default_project_id` bigint DEFAULT NULL,
  `default_billing_rate` decimal(10,2) DEFAULT NULL,
  `default_work_location` enum('OFFICE','HOME','CLIENT_SITE') COLLATE utf8mb4_unicode_ci DEFAULT 'OFFICE',
  `auto_fill_yesterday_project` tinyint(1) DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_preferences` (`user_id`),
  KEY `default_project_id` (`default_project_id`),
  CONSTRAINT `user_preferences_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `user_preferences_ibfk_2` FOREIGN KEY (`default_project_id`) REFERENCES `projects` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `user_preferences` */

insert  into `user_preferences`(`id`,`user_id`,`default_project_id`,`default_billing_rate`,`default_work_location`,`auto_fill_yesterday_project`,`create_time`) values 
(1,1,NULL,1000.00,'OFFICE',1,'2025-12-27 10:59:52'),
(2,2,1,800.00,'OFFICE',1,'2025-12-27 10:59:52'),
(3,3,1,600.00,'OFFICE',1,'2025-12-27 10:59:52'),
(4,4,1,600.00,'HOME',1,'2025-12-27 10:59:52'),
(5,5,1,450.00,'OFFICE',1,'2025-12-27 10:59:52');

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Username',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Password',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Real name',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Email',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Phone',
  `department` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Department',
  `position` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Position',
  `role` enum('ADMIN','PROJECT_MANAGER','PRODUCT_MANAGER','DEPARTMENT_MANAGER','DEVELOPER','TESTER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `monthly_salary` decimal(10,2) DEFAULT '0.00' COMMENT '月薪（元/月）',
  `salary_level` enum('JUNIOR','MIDDLE','SENIOR','EXPERT','ARCHITECT') COLLATE utf8mb4_unicode_ci DEFAULT 'MIDDLE' COMMENT '??????',
  `cost_center` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '??????',
  `status` tinyint DEFAULT '1' COMMENT 'Status: 1-Active, 0-Disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Users table';

/*Data for the table `users` */

insert  into `users`(`id`,`username`,`password`,`real_name`,`email`,`phone`,`department`,`position`,`role`,`monthly_salary`,`salary_level`,`cost_center`,`status`,`create_time`,`update_time`) values 
(1,'admin','$2a$10$J6i9A2c65jwJhvdcXvyRMOjom5VtdWdusM0BjtjvOckKaSmijd3EO','Administrator','admin@company.com',NULL,'IT','System Admin','ADMIN',25000.00,'MIDDLE',NULL,1,'2025-12-26 07:46:38','2025-12-29 18:19:36'),
(2,'pm001','$2a$10$J6i9A2c65jwJhvdcXvyRMOjom5VtdWdusM0BjtjvOckKaSmijd3EO','Project Manager','pm001@company.com',NULL,'Development','Project Manager','PROJECT_MANAGER',20000.00,'MIDDLE',NULL,1,'2025-12-26 07:46:38','2025-12-29 18:20:26'),
(3,'dev001','$2a$10$J6i9A2c65jwJhvdcXvyRMOjom5VtdWdusM0BjtjvOckKaSmijd3EO','Java Developer','dev001@company.com',NULL,'Development','Java Developer','DEVELOPER',15000.00,'MIDDLE',NULL,1,'2025-12-26 07:46:38','2025-12-29 18:20:59'),
(4,'dev002','$2a$10$J6i9A2c65jwJhvdcXvyRMOjom5VtdWdusM0BjtjvOckKaSmijd3EO','Frontend Developer','dev002@company.com',NULL,'Development','Frontend Developer','DEVELOPER',14000.00,'MIDDLE',NULL,1,'2025-12-26 07:46:38','2025-12-29 18:21:18'),
(5,'test001','$2a$10$J6i9A2c65jwJhvdcXvyRMOjom5VtdWdusM0BjtjvOckKaSmijd3EO','Test Engineer','test001@company.com',NULL,'QA','Test Engineer','TESTER',12000.00,'MIDDLE',NULL,1,'2025-12-26 07:46:38','2025-12-29 18:21:36'),
(6,'wangwu','$2a$10$SfYt71YEGGSUWoyflAblqeKFbES2BLlfO1ENCiUkmFyLkltD4ofIu','王五','wangwu@travelsky.com.cn','13456256321','','产品经理','PROJECT_MANAGER',0.00,'MIDDLE',NULL,1,'2025-12-26 08:06:04','2025-12-26 08:06:04'),
(7,'wfluo','$2a$10$2KN2ewHP/kCfM9qOQxWXQOwubA/L8.Ezpr3.HVdAg97c1brX/Xwv6','罗文飞','wfluo@travelsky.com.cn','13756523215','','研发经理','ADMIN',0.00,'MIDDLE',NULL,1,'2025-12-26 08:06:04','2025-12-26 08:06:04'),
(8,'design001','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi','Designer Zhao','design001@example.com','13800138000','IT','UI Designer','DEVELOPER',9000.00,'MIDDLE','IT001',0,'2025-12-30 08:34:20','2025-12-30 08:34:20'),
(9,'dev003','$2a$10$fPCDdvUfge.EUVj5jHcJwOEbyMT1aMhxk.wv8m8pMZcNTCSR7xTi2','研发003','dev@d.com','13256485987','','后端工程师','DEVELOPER',10000.00,'MIDDLE',NULL,1,'2025-12-31 15:07:48','2025-12-31 15:07:48'),
(10,'dev004','$2a$10$1bbZ584mpSJoanDhbD6AT.ouwLktA.tjGtkbBhdZReiWcPKNsecby','研发004','t@t.com','13695452514','','Java开发','DEVELOPER',0.00,'MIDDLE',NULL,1,'2025-12-31 15:08:53','2025-12-31 15:08:53');




/*创建项目状态变更历史表*/
CREATE TABLE IF NOT EXISTS project_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    old_status VARCHAR(20) NOT NULL COMMENT '变更前状态',
    new_status VARCHAR(20) NOT NULL COMMENT '变更后状态',
    change_reason VARCHAR(255) DEFAULT NULL COMMENT '变更原因',
    changed_by VARCHAR(50) NOT NULL COMMENT '变更人',
    changed_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_project_id (project_id),
    INDEX idx_changed_time (changed_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目状态变更历史表';



/*Table structure for table `v_user_project_permissions` */

DROP TABLE IF EXISTS `v_user_project_permissions`;

/*!50001 DROP VIEW IF EXISTS `v_user_project_permissions` */;
/*!50001 DROP TABLE IF EXISTS `v_user_project_permissions` */;

/*!50001 CREATE TABLE  `v_user_project_permissions`(
 `project_id` bigint ,
 `user_id` bigint ,
 `project_name` varchar(100) ,
 `user_name` varchar(50) ,
 `global_role` enum('ADMIN','PROJECT_MANAGER','PRODUCT_MANAGER','DEPARTMENT_MANAGER','DEVELOPER','TESTER') ,
 `project_role` enum('MANAGER','DEVELOPER','TESTER','DESIGNER') ,
 `is_project_manager` tinyint(1) ,
 `is_tech_leader` tinyint(1) ,
 `can_approve_timesheet` tinyint(1) ,
 `can_manage_tasks` tinyint(1) ,
 `can_view_reports` tinyint(1) ,
 `permissions` text ,
 `effective_date` date ,
 `expiry_date` date 
)*/;

/*View structure for view v_user_project_permissions */

/*!50001 DROP TABLE IF EXISTS `v_user_project_permissions` */;
/*!50001 DROP VIEW IF EXISTS `v_user_project_permissions` */;

/*!50001 CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_user_project_permissions` AS select `pm`.`project_id` AS `project_id`,`pm`.`user_id` AS `user_id`,`p`.`project_name` AS `project_name`,`u`.`real_name` AS `user_name`,`u`.`role` AS `global_role`,`pm`.`role` AS `project_role`,`pm`.`is_project_manager` AS `is_project_manager`,`pm`.`is_tech_leader` AS `is_tech_leader`,`pm`.`can_approve_timesheet` AS `can_approve_timesheet`,`pm`.`can_manage_tasks` AS `can_manage_tasks`,`pm`.`can_view_reports` AS `can_view_reports`,group_concat(`prp`.`permission_type` separator ',') AS `permissions`,`pm`.`effective_date` AS `effective_date`,`pm`.`expiry_date` AS `expiry_date` from (((`project_members` `pm` left join `projects` `p` on((`pm`.`project_id` = `p`.`id`))) left join `users` `u` on((`pm`.`user_id` = `u`.`id`))) left join `project_role_permissions` `prp` on(((`pm`.`project_id` = `prp`.`project_id`) and (`pm`.`user_id` = `prp`.`user_id`) and (`prp`.`is_active` = true) and ((`prp`.`effective_date` is null) or (`prp`.`effective_date` <= curdate())) and ((`prp`.`expiry_date` is null) or (`prp`.`expiry_date` >= curdate()))))) group by `pm`.`project_id`,`pm`.`user_id` */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
