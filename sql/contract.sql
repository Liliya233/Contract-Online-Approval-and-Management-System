/*
 Navicat Premium Data Transfer

 Source Server Type    : MySQL
 Source Server Version : 50718 (5.7.18-cynos-log)
 Source Schema         : contract

 Target Server Type    : MySQL
 Target Server Version : 50718 (5.7.18-cynos-log)
 File Encoding         : 65001

 Date: 06/05/2023 23:11:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for contract
-- ----------------------------
DROP TABLE IF EXISTS `contract`;
CREATE TABLE `contract`  (
  `contract_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `contract_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `contract_author_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '负责人ID',
  `contract_status` int(11) NOT NULL COMMENT '状态',
  `contract_create_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '合同创建时间',
  `contract_type` int(11) NULL DEFAULT NULL COMMENT '合同类型 (0-15)',
  `contract_start_date` datetime NULL DEFAULT NULL COMMENT '合同起始日期',
  `contract_end_date` datetime NULL DEFAULT NULL COMMENT '合同终止日期',
  `contract_period_start_date` datetime NULL DEFAULT NULL COMMENT '合同期限起始日期',
  `contract_period_expire_date` datetime NULL DEFAULT NULL COMMENT '合同期限终止日期',
  `contract_party` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '合同方',
  `contract_funds` bigint(20) NULL DEFAULT NULL COMMENT '合同经费',
  `contract_seal_author_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '盖章负责人ID',
  `contract_seal_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '盖章备注信息',
  `contract_end_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '终结备注信息',
  PRIMARY KEY (`contract_id`) USING BTREE,
  INDEX `contract_author_id`(`contract_author_id`) USING BTREE,
  INDEX `contract_seal_author_id`(`contract_seal_author_id`) USING BTREE,
  CONSTRAINT `contract_author_id` FOREIGN KEY (`contract_author_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `contract_seal_author_id` FOREIGN KEY (`contract_seal_author_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1021 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for contract_attachment
-- ----------------------------
DROP TABLE IF EXISTS `contract_attachment`;
CREATE TABLE `contract_attachment`  (
  `attachment_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `attachment_contract_id` int(10) UNSIGNED NOT NULL COMMENT '对应的合同ID',
  `attachment_type` int(11) NOT NULL COMMENT '附件类型',
  `attachment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '附件名称',
  `attachment_size` bigint(20) NOT NULL COMMENT '附件大小',
  `attachment_upload_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '上传时间',
  `attachment_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源路径',
  PRIMARY KEY (`attachment_id`) USING BTREE,
  INDEX `contract_id`(`attachment_contract_id`) USING BTREE,
  CONSTRAINT `contract_id` FOREIGN KEY (`attachment_contract_id`) REFERENCES `contract` (`contract_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 142 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for contract_record
-- ----------------------------
DROP TABLE IF EXISTS `contract_record`;
CREATE TABLE `contract_record`  (
  `record_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `record_contract_id` int(10) UNSIGNED NOT NULL COMMENT '对应的合同ID',
  `record_operator_id` int(10) UNSIGNED NOT NULL COMMENT '操作员ID',
  `record_operation_type` int(11) NOT NULL COMMENT '操作类型',
  `record_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '操作时间',
  `record_details` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作详情',
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `record_contract_id`(`record_contract_id`) USING BTREE,
  CONSTRAINT `record_contract_id` FOREIGN KEY (`record_contract_id`) REFERENCES `contract` (`contract_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 10098 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `department_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `department_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `department_permission_admin` tinyint(1) NOT NULL COMMENT '管理权限',
  `department_permission_create` tinyint(1) NOT NULL COMMENT '合同生成权限',
  `department_permission_approval` int(11) NOT NULL COMMENT '合同审批权限 (禁止、部门、公司、最终)',
  `department_permission_seal` tinyint(1) NOT NULL COMMENT '合同盖章权限',
  `department_permission_terminate` tinyint(1) NOT NULL COMMENT '合同终结权限',
  `department_permission_access` int(11) NOT NULL COMMENT '数据访问权限 (个人、部门、公司)',
  PRIMARY KEY (`department_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for persistent_logins
-- ----------------------------
DROP TABLE IF EXISTS `persistent_logins`;
CREATE TABLE `persistent_logins`  (
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `series` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `token` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `last_used` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`series`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for supplement
-- ----------------------------
DROP TABLE IF EXISTS `supplement`;
CREATE TABLE `supplement`  (
  `supplement_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `supplement_contract_id` int(10) UNSIGNED NOT NULL COMMENT '所属合同ID',
  `supplement_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  `supplement_create_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `supplement_status` int(11) NOT NULL COMMENT '状态',
  `supplement_seal_author_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '盖章负责人ID',
  `supplement_seal_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '盖章备注信息',
  PRIMARY KEY (`supplement_id`) USING BTREE,
  INDEX `supplement_seal_author_id`(`supplement_seal_author_id`) USING BTREE,
  INDEX `supplement_contract_id`(`supplement_contract_id`) USING BTREE,
  CONSTRAINT `supplement_contract_id` FOREIGN KEY (`supplement_contract_id`) REFERENCES `contract` (`contract_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `supplement_seal_author_id` FOREIGN KEY (`supplement_seal_author_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1109 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for supplement_attachment
-- ----------------------------
DROP TABLE IF EXISTS `supplement_attachment`;
CREATE TABLE `supplement_attachment`  (
  `attachment_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `attachment_supplement_id` int(10) UNSIGNED NOT NULL COMMENT '对应的协议ID',
  `attachment_type` int(11) NOT NULL COMMENT '附件类型',
  `attachment_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '附件名称',
  `attachment_size` bigint(20) NOT NULL COMMENT '附件大小',
  `attachment_upload_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '上传时间',
  `attachment_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源路径',
  PRIMARY KEY (`attachment_id`) USING BTREE,
  INDEX `supplement_id`(`attachment_supplement_id`) USING BTREE,
  CONSTRAINT `supplement_id` FOREIGN KEY (`attachment_supplement_id`) REFERENCES `supplement` (`supplement_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for supplement_record
-- ----------------------------
DROP TABLE IF EXISTS `supplement_record`;
CREATE TABLE `supplement_record`  (
  `record_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `record_supplement_id` int(10) UNSIGNED NOT NULL COMMENT '对应的协议ID',
  `record_operator_id` int(10) UNSIGNED NOT NULL COMMENT '操作员ID',
  `record_operation_type` int(11) NOT NULL COMMENT '操作类型',
  `record_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '操作时间',
  `record_details` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作详情',
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `record_supplement_id`(`record_supplement_id`) USING BTREE,
  CONSTRAINT `record_supplement_id` FOREIGN KEY (`record_supplement_id`) REFERENCES `supplement` (`supplement_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 10029 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名称',
  `user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户密码',
  `user_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户邮箱',
  `user_department_id` int(10) UNSIGNED NULL DEFAULT NULL COMMENT '用户所属部门ID',
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `user_department_id`(`user_department_id`) USING BTREE,
  CONSTRAINT `user_department_id` FOREIGN KEY (`user_department_id`) REFERENCES `department` (`department_id`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1004 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
