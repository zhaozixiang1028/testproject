/*
 Navicat Premium Data Transfer

 Source Server         : SJT2405
 Source Server Type    : MySQL
 Source Server Version : 50735
 Source Host           : localhost:3306
 Source Schema         : testproject

 Target Server Type    : MySQL
 Target Server Version : 50735
 File Encoding         : 65001

 Date: 11/03/2026 16:45:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for company_review
-- ----------------------------
DROP TABLE IF EXISTS `company_review`;
CREATE TABLE `company_review`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint(20) NOT NULL COMMENT 'User id',
  `company_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Company name',
  `culture_score` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Culture score',
  `team_score` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Team atmosphere score',
  `growth_score` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Growth score',
  `salary_score` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Salary score',
  `balance_score` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'Work balance score',
  `anonymous_mode` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1 anonymous 0 public',
  `public_visible` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1 public visible 0 hidden',
  `review_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'Review content',
  `review_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING APPROVED REJECTED',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_review_company`(`company_name`) USING BTREE,
  INDEX `idx_company_review_user`(`user_id`) USING BTREE,
  CONSTRAINT `fk_company_review_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Company reviews' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of company_review
-- ----------------------------
INSERT INTO `company_review` VALUES (10, 1, 'SeedCorpA_1773214785', 5, 5, 5, 5, 5, 0, 0, 'seed superadmin private', 'APPROVED', '2026-03-11 15:39:45', '2026-03-11 15:39:45');
INSERT INTO `company_review` VALUES (11, 2, 'SeedCorpB_1773214785', 3, 4, 4, 3, 4, 1, 1, 'seed admin public', 'PENDING', '2026-03-11 15:39:45', '2026-03-11 15:39:45');
INSERT INTO `company_review` VALUES (12, 6, '亚信科技', 4, 4, 4, 4, 4, 1, 1, '', 'APPROVED', '2026-03-11 15:55:39', '2026-03-11 15:55:39');

-- ----------------------------
-- Table structure for review_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `review_audit_log`;
CREATE TABLE `review_audit_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `review_id` bigint(20) NOT NULL COMMENT 'Review id',
  `operator_user_id` bigint(20) NOT NULL COMMENT 'Operator user id',
  `operator_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Operator username',
  `old_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Old status',
  `new_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'New status',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Audit remark',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_audit_review`(`review_id`) USING BTREE,
  INDEX `idx_audit_operator`(`operator_user_id`) USING BTREE,
  CONSTRAINT `fk_audit_operator` FOREIGN KEY (`operator_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_audit_review` FOREIGN KEY (`review_id`) REFERENCES `company_review` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Review audit logs' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review_audit_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_company
-- ----------------------------
DROP TABLE IF EXISTS `sys_company`;
CREATE TABLE `sys_company`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Company name',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '0:disabled 1:enabled',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Company catalog' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_company
-- ----------------------------
INSERT INTO `sys_company` VALUES (1, '亚信科技', 1, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_company` VALUES (2, 'TestCompany', 1, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_company` VALUES (3, 'WrongCompanyShouldBeIgnored', 1, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_company` VALUES (4, '迈瑞医疗', 1, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_company` VALUES (8, 'Company_1773214542089', 1, '2026-03-11 15:36:55', '2026-03-11 15:36:55');
INSERT INTO `sys_company` VALUES (9, 'Company_1773214717070', 1, '2026-03-11 15:38:38', '2026-03-11 15:38:38');
INSERT INTO `sys_company` VALUES (10, 'SeedCorpA_1773214785', 1, '2026-03-11 16:04:22', '2026-03-11 16:04:22');
INSERT INTO `sys_company` VALUES (11, 'SeedCorpB_1773214785', 1, '2026-03-11 16:04:22', '2026-03-11 16:04:22');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Role code',
  `label` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Role label',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Role description',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '0:disabled 1:enabled',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'System roles' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'SUPER_ADMIN', '超级管理员', '仅开发人员使用，拥有全量权限', 1, '2026-03-11 14:36:35', '2026-03-11 14:36:35');
INSERT INTO `sys_role` VALUES (2, 'ADMIN', '管理员', '可管理员工账号和业务数据', 1, '2026-03-11 14:36:35', '2026-03-11 14:36:35');
INSERT INTO `sys_role` VALUES (3, 'EMPLOYEE', '员工', '可使用工作日志和评价功能', 1, '2026-03-11 14:36:35', '2026-03-11 14:36:35');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Login account',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'BCrypt encrypted password',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Display name',
  `employed_company` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Current employed company',
  `role` enum('SUPER_ADMIN','ADMIN','EMPLOYEE') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'EMPLOYEE' COMMENT 'Role code',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '0:disabled 1:enabled',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'System users' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'superadmin', '$2a$10$.wflmGt19Mw3B.p/.QggHOhXTIMAU9zV.qZmxQfW0bbVjbR78N5/S', 'Super Admin', NULL, 'SUPER_ADMIN', 1, '2026-03-11 11:05:50', '2026-03-11 11:05:50');
INSERT INTO `sys_user` VALUES (2, 'admin', '$2a$10$x3BmDbGX/nD3lRpld7mpJ.zQWwZH5bjMVAQcy.wUyc2pBuQ35PQJS', 'Admin', NULL, 'ADMIN', 1, '2026-03-11 11:05:50', '2026-03-11 11:05:50');
INSERT INTO `sys_user` VALUES (3, 'employee', '$2a$10$EohcpQTopF9Tv8ElSuD.Kuc5.tQnNImzD/eEpTl5pzKMKM/JKuRI.', 'Employee', 'Company_1773214717070', 'EMPLOYEE', 1, '2026-03-11 11:05:50', '2026-03-11 11:05:50');
INSERT INTO `sys_user` VALUES (4, 'employee02', '$2a$10$M8Wjw4Jr4vMVW8OK/m7vtOqH4RQId/N4.fNmNGl1F4LZdijafw2dG', 'Employee 02', NULL, 'EMPLOYEE', 1, '2026-03-11 11:23:03', '2026-03-11 11:23:03');
INSERT INTO `sys_user` VALUES (5, 'manager01', '$2a$10$5cmGnXAv6qmushUXWiTT3e.Xt3VsN7MPUXOIe7dxgAICes/Bd8uXG', 'Manager 01', NULL, 'ADMIN', 1, '2026-03-11 11:23:03', '2026-03-11 11:23:03');
INSERT INTO `sys_user` VALUES (6, 'zhangsan', '$2a$10$MLNsrxh7sObOI1qRU3zs.e5nlxFjuMTIRrm45Gn4LQu3EFDkcikX.', 'zhangsan', '亚信科技', 'EMPLOYEE', 1, '2026-03-11 11:36:42', '2026-03-11 14:04:18');
INSERT INTO `sys_user` VALUES (7, 'emp_seed_20260311135506', '$2a$10$dWvtBcaavz7nzjlPjS9Rv.c5rTyKrB0.tCEJCDWFIRNsoR0ZJnKwa', 'SeedEmployee', 'TestCompany', 'EMPLOYEE', 1, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `sys_user` VALUES (8, 'emp_nocomp_20260311135506', '$2a$10$HqtxdMHwzXNTVhqh1Hi/de347R9zrYdtjMdzMA40vk6yaNNH/mrSi', 'NoCompany', NULL, 'EMPLOYEE', 1, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `sys_user` VALUES (9, 'emp_dbg_20260311135535', '$2a$10$fiNYspweyfbv3REELa37T.aCdpa5.EMvOsgTN/WEXCsTj6froPLLe', 'Dbg', NULL, 'EMPLOYEE', 1, '2026-03-11 13:55:36', '2026-03-11 13:55:36');
INSERT INTO `sys_user` VALUES (10, 'emp_seed_20260311135608', '$2a$10$3QkArspi959JmX/byEsKeOSwA5i32uxSUPhQvBkkwJSBg9SRA8/Ou', 'SeedEmployee', 'TestCompany', 'EMPLOYEE', 1, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `sys_user` VALUES (11, 'emp_nocomp_20260311135608', '$2a$10$vpjx9ADPuWZsH7TtHAf4f.OzIdpH/wXyGvB1V1EPvVUZpLo5kSjOK', 'NoCompany', NULL, 'EMPLOYEE', 1, '2026-03-11 13:56:09', '2026-03-11 13:56:09');

-- ----------------------------
-- Table structure for sys_user_company
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_company`;
CREATE TABLE `sys_user_company`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint(20) NOT NULL COMMENT 'User id',
  `company_id` bigint(20) NOT NULL DEFAULT 0 COMMENT 'Company id, 0 means none',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_company_user`(`user_id`) USING BTREE,
  INDEX `idx_user_company_company`(`company_id`) USING BTREE,
  CONSTRAINT `fk_user_company_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'User employed company mapping' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_company
-- ----------------------------
INSERT INTO `sys_user_company` VALUES (1, 1, 0, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_user_company` VALUES (2, 2, 0, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_user_company` VALUES (3, 3, 9, '2026-03-11 15:22:01', '2026-03-11 15:38:38');
INSERT INTO `sys_user_company` VALUES (4, 4, 0, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_user_company` VALUES (5, 5, 0, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_user_company` VALUES (6, 6, 1, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_user_company` VALUES (7, 7, 2, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_user_company` VALUES (8, 8, 0, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_user_company` VALUES (9, 9, 0, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_user_company` VALUES (10, 10, 2, '2026-03-11 15:22:01', '2026-03-11 15:22:01');
INSERT INTO `sys_user_company` VALUES (11, 11, 0, '2026-03-11 15:22:01', '2026-03-11 15:22:01');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint(20) NOT NULL COMMENT 'User id',
  `role_id` bigint(20) NOT NULL COMMENT 'Role id',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id`, `role_id`) USING BTREE,
  INDEX `idx_user_role_user`(`user_id`) USING BTREE,
  INDEX `idx_user_role_role`(`role_id`) USING BTREE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'User role mappings' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (4, 4, 3, '2026-03-11 14:36:35');
INSERT INTO `sys_user_role` VALUES (5, 5, 2, '2026-03-11 14:36:35');
INSERT INTO `sys_user_role` VALUES (6, 6, 3, '2026-03-11 14:36:35');
INSERT INTO `sys_user_role` VALUES (7, 7, 3, '2026-03-11 14:36:35');
INSERT INTO `sys_user_role` VALUES (8, 8, 3, '2026-03-11 14:36:35');
INSERT INTO `sys_user_role` VALUES (9, 9, 3, '2026-03-11 14:36:35');
INSERT INTO `sys_user_role` VALUES (10, 10, 3, '2026-03-11 14:36:35');
INSERT INTO `sys_user_role` VALUES (11, 11, 3, '2026-03-11 14:36:35');
INSERT INTO `sys_user_role` VALUES (28, 1, 1, '2026-03-11 16:04:25');
INSERT INTO `sys_user_role` VALUES (29, 2, 2, '2026-03-11 16:04:25');
INSERT INTO `sys_user_role` VALUES (30, 3, 3, '2026-03-11 16:04:25');

-- ----------------------------
-- Table structure for work_log
-- ----------------------------
DROP TABLE IF EXISTS `work_log`;
CREATE TABLE `work_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `user_id` bigint(20) NOT NULL COMMENT 'User id',
  `work_date` date NOT NULL COMMENT 'Work date',
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Log title',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'Rich text content',
  `project_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Project name',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Task type',
  `priority_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Priority level',
  `start_time` datetime NULL DEFAULT NULL COMMENT 'Start time',
  `end_time` datetime NULL DEFAULT NULL COMMENT 'End time',
  `work_hours` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'Calculated work hours',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Tag list',
  `attachment_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'Attachment urls',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_work_log_user_date`(`user_id`, `work_date`) USING BTREE,
  CONSTRAINT `fk_work_log_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 147 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Daily work logs' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of work_log
-- ----------------------------
INSERT INTO `work_log` VALUES (1, 2, '2026-03-11', '????', NULL, 'TestProject', '??', 'HIGH', '2026-03-11 09:00:00', '2026-03-11 11:00:00', 2.00, 'backend,api', NULL, '2026-03-11 11:51:35', '2026-03-11 11:51:35');
INSERT INTO `work_log` VALUES (2, 6, '2026-03-11', '我的第二天上班', '关于我的上班日常系统的初步完成', '关于我的上班日常系统', '开发', 'LOW', '2026-03-11 09:00:00', '2026-03-11 18:00:00', 9.00, '系统开发', '', '2026-03-11 13:14:13', '2026-03-11 13:14:13');
INSERT INTO `work_log` VALUES (3, 3, '2026-03-10', 'seed-log-1', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case1', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (4, 3, '2026-03-09', 'seed-log-2', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case2', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (5, 3, '2026-03-08', 'seed-log-3', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case3', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (6, 3, '2026-03-07', 'seed-log-4', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case4', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (7, 3, '2026-03-06', 'seed-log-5', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case5', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (8, 3, '2026-03-05', 'seed-log-6', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case6', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (9, 3, '2026-03-04', 'seed-log-7', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case7', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (10, 3, '2026-03-03', 'seed-log-8', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case8', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (11, 3, '2026-03-02', 'seed-log-9', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case9', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (12, 3, '2026-03-01', 'seed-log-10', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case10', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (13, 3, '2026-02-28', 'seed-log-11', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case11', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (14, 3, '2026-03-11', 'seed-log-12', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case12', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (15, 3, '2026-03-10', 'seed-log-13', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case13', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (16, 3, '2026-03-09', 'seed-log-14', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case14', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (17, 3, '2026-03-08', 'seed-log-15', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case15', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (18, 3, '2026-03-07', 'seed-log-16', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case16', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (19, 3, '2026-03-06', 'seed-log-17', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case17', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (20, 3, '2026-03-05', 'seed-log-18', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case18', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (21, 3, '2026-03-04', 'seed-log-19', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case19', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (22, 3, '2026-03-03', 'seed-log-20', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case20', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (23, 3, '2026-03-02', 'seed-log-21', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case21', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (24, 3, '2026-03-01', 'seed-log-22', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case22', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (25, 3, '2026-02-28', 'seed-log-23', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case23', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (26, 3, '2026-03-11', 'seed-log-24', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case24', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (27, 3, '2026-03-10', 'seed-log-25', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case25', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (28, 3, '2026-03-09', 'seed-log-26', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case26', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (29, 3, '2026-03-08', 'seed-log-27', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case27', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (30, 3, '2026-03-07', 'seed-log-28', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case28', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (31, 3, '2026-03-06', 'seed-log-29', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case29', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (32, 3, '2026-03-05', 'seed-log-30', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case30', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (33, 3, '2026-03-04', 'seed-log-31', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case31', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (34, 3, '2026-03-03', 'seed-log-32', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case32', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (35, 3, '2026-03-02', 'seed-log-33', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case33', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (36, 3, '2026-03-01', 'seed-log-34', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case34', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (37, 3, '2026-02-28', 'seed-log-35', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case35', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (38, 3, '2026-03-11', 'seed-log-36', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case36', NULL, '2026-03-11 13:53:01', '2026-03-11 13:53:01');
INSERT INTO `work_log` VALUES (39, 3, '2026-03-10', 'seed-log-1', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case1', NULL, '2026-03-11 13:54:39', '2026-03-11 13:54:39');
INSERT INTO `work_log` VALUES (40, 3, '2026-03-09', 'seed-log-2', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case2', NULL, '2026-03-11 13:54:39', '2026-03-11 13:54:39');
INSERT INTO `work_log` VALUES (41, 3, '2026-03-08', 'seed-log-3', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case3', NULL, '2026-03-11 13:54:39', '2026-03-11 13:54:39');
INSERT INTO `work_log` VALUES (42, 3, '2026-03-07', 'seed-log-4', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case4', NULL, '2026-03-11 13:54:39', '2026-03-11 13:54:39');
INSERT INTO `work_log` VALUES (43, 3, '2026-03-06', 'seed-log-5', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case5', NULL, '2026-03-11 13:54:39', '2026-03-11 13:54:39');
INSERT INTO `work_log` VALUES (44, 3, '2026-03-05', 'seed-log-6', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case6', NULL, '2026-03-11 13:54:39', '2026-03-11 13:54:39');
INSERT INTO `work_log` VALUES (45, 3, '2026-03-04', 'seed-log-7', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case7', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (46, 3, '2026-03-03', 'seed-log-8', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case8', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (47, 3, '2026-03-02', 'seed-log-9', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case9', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (48, 3, '2026-03-01', 'seed-log-10', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case10', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (49, 3, '2026-02-28', 'seed-log-11', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case11', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (50, 3, '2026-03-11', 'seed-log-12', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case12', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (51, 3, '2026-03-10', 'seed-log-13', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case13', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (52, 3, '2026-03-09', 'seed-log-14', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case14', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (53, 3, '2026-03-08', 'seed-log-15', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case15', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (54, 3, '2026-03-07', 'seed-log-16', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case16', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (55, 3, '2026-03-06', 'seed-log-17', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case17', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (56, 3, '2026-03-05', 'seed-log-18', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case18', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (57, 3, '2026-03-04', 'seed-log-19', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case19', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (58, 3, '2026-03-03', 'seed-log-20', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case20', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (59, 3, '2026-03-02', 'seed-log-21', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case21', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (60, 3, '2026-03-01', 'seed-log-22', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case22', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (61, 3, '2026-02-28', 'seed-log-23', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case23', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (62, 3, '2026-03-11', 'seed-log-24', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case24', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (63, 3, '2026-03-10', 'seed-log-25', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case25', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (64, 3, '2026-03-09', 'seed-log-26', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case26', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (65, 3, '2026-03-08', 'seed-log-27', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case27', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (66, 3, '2026-03-07', 'seed-log-28', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case28', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (67, 3, '2026-03-06', 'seed-log-29', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case29', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (68, 3, '2026-03-05', 'seed-log-30', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case30', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (69, 3, '2026-03-04', 'seed-log-31', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case31', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (70, 3, '2026-03-03', 'seed-log-32', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case32', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (71, 3, '2026-03-02', 'seed-log-33', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case33', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (72, 3, '2026-03-01', 'seed-log-34', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case34', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (73, 3, '2026-02-28', 'seed-log-35', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case35', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (74, 3, '2026-03-11', 'seed-log-36', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case36', NULL, '2026-03-11 13:54:40', '2026-03-11 13:54:40');
INSERT INTO `work_log` VALUES (75, 7, '2026-03-10', 'seed-log-1', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case1', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (76, 7, '2026-03-09', 'seed-log-2', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case2', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (77, 7, '2026-03-08', 'seed-log-3', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case3', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (78, 7, '2026-03-07', 'seed-log-4', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case4', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (79, 7, '2026-03-06', 'seed-log-5', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case5', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (80, 7, '2026-03-05', 'seed-log-6', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case6', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (81, 7, '2026-03-04', 'seed-log-7', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case7', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (82, 7, '2026-03-03', 'seed-log-8', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case8', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (83, 7, '2026-03-02', 'seed-log-9', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case9', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (84, 7, '2026-03-01', 'seed-log-10', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case10', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (85, 7, '2026-02-28', 'seed-log-11', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case11', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (86, 7, '2026-03-11', 'seed-log-12', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case12', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (87, 7, '2026-03-10', 'seed-log-13', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case13', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (88, 7, '2026-03-09', 'seed-log-14', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case14', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (89, 7, '2026-03-08', 'seed-log-15', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case15', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (90, 7, '2026-03-07', 'seed-log-16', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case16', NULL, '2026-03-11 13:55:06', '2026-03-11 13:55:06');
INSERT INTO `work_log` VALUES (91, 7, '2026-03-06', 'seed-log-17', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case17', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (92, 7, '2026-03-05', 'seed-log-18', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case18', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (93, 7, '2026-03-04', 'seed-log-19', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case19', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (94, 7, '2026-03-03', 'seed-log-20', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case20', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (95, 7, '2026-03-02', 'seed-log-21', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case21', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (96, 7, '2026-03-01', 'seed-log-22', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case22', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (97, 7, '2026-02-28', 'seed-log-23', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case23', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (98, 7, '2026-03-11', 'seed-log-24', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case24', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (99, 7, '2026-03-10', 'seed-log-25', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case25', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (100, 7, '2026-03-09', 'seed-log-26', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case26', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (101, 7, '2026-03-08', 'seed-log-27', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case27', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (102, 7, '2026-03-07', 'seed-log-28', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case28', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (103, 7, '2026-03-06', 'seed-log-29', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case29', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (104, 7, '2026-03-05', 'seed-log-30', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case30', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (105, 7, '2026-03-04', 'seed-log-31', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case31', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (106, 7, '2026-03-03', 'seed-log-32', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case32', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (107, 7, '2026-03-02', 'seed-log-33', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case33', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (108, 7, '2026-03-01', 'seed-log-34', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case34', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (109, 7, '2026-02-28', 'seed-log-35', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case35', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (110, 7, '2026-03-11', 'seed-log-36', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case36', NULL, '2026-03-11 13:55:07', '2026-03-11 13:55:07');
INSERT INTO `work_log` VALUES (111, 10, '2026-03-10', 'seed-log-1', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case1', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (112, 10, '2026-03-09', 'seed-log-2', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case2', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (113, 10, '2026-03-08', 'seed-log-3', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case3', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (114, 10, '2026-03-07', 'seed-log-4', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case4', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (115, 10, '2026-03-06', 'seed-log-5', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case5', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (116, 10, '2026-03-05', 'seed-log-6', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case6', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (117, 10, '2026-03-04', 'seed-log-7', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case7', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (118, 10, '2026-03-03', 'seed-log-8', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case8', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (119, 10, '2026-03-02', 'seed-log-9', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case9', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (120, 10, '2026-03-01', 'seed-log-10', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case10', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (121, 10, '2026-02-28', 'seed-log-11', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case11', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (122, 10, '2026-03-11', 'seed-log-12', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case12', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (123, 10, '2026-03-10', 'seed-log-13', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case13', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (124, 10, '2026-03-09', 'seed-log-14', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case14', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (125, 10, '2026-03-08', 'seed-log-15', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case15', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (126, 10, '2026-03-07', 'seed-log-16', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case16', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (127, 10, '2026-03-06', 'seed-log-17', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case17', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (128, 10, '2026-03-05', 'seed-log-18', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case18', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (129, 10, '2026-03-04', 'seed-log-19', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case19', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (130, 10, '2026-03-03', 'seed-log-20', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case20', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (131, 10, '2026-03-02', 'seed-log-21', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case21', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (132, 10, '2026-03-01', 'seed-log-22', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case22', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (133, 10, '2026-02-28', 'seed-log-23', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case23', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (134, 10, '2026-03-11', 'seed-log-24', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case24', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (135, 10, '2026-03-10', 'seed-log-25', 'seed for pagination filter sort', 'Beta', 'TEST', 'MEDIUM', '2026-03-10 10:00:00', '2026-03-10 12:30:00', 2.50, 'seed,case25', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (136, 10, '2026-03-09', 'seed-log-26', 'seed for pagination filter sort', 'Gamma', 'MEETING', 'HIGH', '2026-03-09 11:00:00', '2026-03-09 13:30:00', 2.50, 'seed,case26', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (137, 10, '2026-03-08', 'seed-log-27', 'seed for pagination filter sort', 'Delta', 'DEV', 'LOW', '2026-03-08 09:00:00', '2026-03-08 11:30:00', 2.50, 'seed,case27', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (138, 10, '2026-03-07', 'seed-log-28', 'seed for pagination filter sort', 'Alpha', 'TEST', 'MEDIUM', '2026-03-07 10:00:00', '2026-03-07 12:30:00', 2.50, 'seed,case28', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (139, 10, '2026-03-06', 'seed-log-29', 'seed for pagination filter sort', 'Beta', 'MEETING', 'HIGH', '2026-03-06 11:00:00', '2026-03-06 13:30:00', 2.50, 'seed,case29', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (140, 10, '2026-03-05', 'seed-log-30', 'seed for pagination filter sort', 'Gamma', 'DEV', 'LOW', '2026-03-05 09:00:00', '2026-03-05 11:30:00', 2.50, 'seed,case30', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (141, 10, '2026-03-04', 'seed-log-31', 'seed for pagination filter sort', 'Delta', 'TEST', 'MEDIUM', '2026-03-04 10:00:00', '2026-03-04 12:30:00', 2.50, 'seed,case31', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (142, 10, '2026-03-03', 'seed-log-32', 'seed for pagination filter sort', 'Alpha', 'MEETING', 'HIGH', '2026-03-03 11:00:00', '2026-03-03 13:30:00', 2.50, 'seed,case32', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (143, 10, '2026-03-02', 'seed-log-33', 'seed for pagination filter sort', 'Beta', 'DEV', 'LOW', '2026-03-02 09:00:00', '2026-03-02 11:30:00', 2.50, 'seed,case33', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (144, 10, '2026-03-01', 'seed-log-34', 'seed for pagination filter sort', 'Gamma', 'TEST', 'MEDIUM', '2026-03-01 10:00:00', '2026-03-01 12:30:00', 2.50, 'seed,case34', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (145, 10, '2026-02-28', 'seed-log-35', 'seed for pagination filter sort', 'Delta', 'MEETING', 'HIGH', '2026-02-28 11:00:00', '2026-02-28 13:30:00', 2.50, 'seed,case35', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');
INSERT INTO `work_log` VALUES (146, 10, '2026-03-11', 'seed-log-36', 'seed for pagination filter sort', 'Alpha', 'DEV', 'LOW', '2026-03-11 09:00:00', '2026-03-11 11:30:00', 2.50, 'seed,case36', NULL, '2026-03-11 13:56:08', '2026-03-11 13:56:08');

SET FOREIGN_KEY_CHECKS = 1;
