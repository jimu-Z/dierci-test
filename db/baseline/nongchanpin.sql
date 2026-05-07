/*
 Navicat Premium Data Transfer

 Source Server         : frist
 Source Server Type    : MySQL
 Source Server Version : 80042
 Source Host           : localhost:3306
 Source Schema         : nongchanpin

 Target Server Type    : MySQL
 Target Server Version : 80042
 File Encoding         : 65001

 Date: 15/04/2026 17:38:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for agri_base_rule
-- ----------------------------
DROP TABLE IF EXISTS `agri_base_rule`;
CREATE TABLE `agri_base_rule`  (
  `rule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则名称',
  `rule_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则类型',
  `rule_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则编码',
  `rule_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`rule_id`) USING BTREE,
  UNIQUE INDEX `uk_rule_code`(`rule_code`) USING BTREE,
  INDEX `idx_rule_type`(`rule_type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链基础规则配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_base_rule
-- ----------------------------
INSERT INTO `agri_base_rule` VALUES (1, '环境监测默认阈值', 'ENV_MONITOR', 'ENV_MONITOR_DEFAULT', '{\"temperature\":{\"min\":5,\"max\":35},\"humidity\":{\"min\":30,\"max\":90}}', '0', '第一阶段初始化', 'admin', '2026-04-14 21:16:36', '', '2026-04-14 21:16:36');
INSERT INTO `agri_base_rule` VALUES (2, '农事操作标准模板', 'FARM_OP_STANDARD', 'FARM_OP_STANDARD_V1', '{\"mustRecord\":[\"操作人\",\"时间\",\"地块\",\"用量\"]}', '0', '第一阶段初始化', 'admin', '2026-04-14 21:16:36', '', '2026-04-14 21:16:36');
INSERT INTO `agri_base_rule` VALUES (3, '品质分级规则模板', 'QUALITY_GRADE', 'QUALITY_GRADE_V1', '{\"grade\":[\"A\",\"B\",\"C\"],\"dimensions\":[\"大小\",\"色泽\",\"完整度\"]}', '0', '第一阶段初始化', 'admin', '2026-04-14 21:16:36', '', '2026-04-14 21:16:36');
INSERT INTO `agri_base_rule` VALUES (4, '物流路径模板', 'LOGISTICS_PATH', 'LOGISTICS_PATH_V1', '{\"defaultRoute\":[\"基地\",\"加工厂\",\"仓储\",\"销售终端\"]}', '0', '第一阶段初始化', 'admin', '2026-04-14 21:16:36', '', '2026-04-14 21:16:36');
INSERT INTO `agri_base_rule` VALUES (5, '市场分析模型配置', 'MARKET_MODEL', 'MARKET_MODEL_V1', '{\"model\":\"baseline\",\"windowDays\":30}', '0', '第一阶段初始化', 'admin', '2026-04-14 21:16:36', '', '2026-04-14 21:16:36');
INSERT INTO `agri_base_rule` VALUES (6, '智能合约规则模板', 'SMART_CONTRACT', 'SMART_CONTRACT_V1', '{\"onChainFields\":[\"batchNo\",\"qualityStatus\",\"traceCode\"]}', '0', '第一阶段初始化', 'admin', '2026-04-14 21:16:36', '', '2026-04-14 21:16:36');

-- ----------------------------
-- Table structure for agri_brand_trace_page
-- ----------------------------
DROP TABLE IF EXISTS `agri_brand_trace_page`;
CREATE TABLE `agri_brand_trace_page`  (
  `page_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trace_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '溯源码',
  `brand_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '品牌名称',
  `product_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品名称',
  `product_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '产品编码',
  `origin_place` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '产地',
  `planting_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '种植批次号',
  `process_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '加工批次号',
  `logistics_trace_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '物流运单号',
  `cover_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图地址',
  `page_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '详情页地址',
  `qr_code_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '二维码地址',
  `brand_story` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品牌故事',
  `publish_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '发布状态（0草稿 1已发布）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`page_id`) USING BTREE,
  UNIQUE INDEX `uk_trace_code`(`trace_code`) USING BTREE,
  INDEX `idx_publish_status`(`publish_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链品牌溯源页面表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_brand_trace_page
-- ----------------------------
INSERT INTO `agri_brand_trace_page` VALUES (1, 'TRACE-202607-APPLE-001', '启元优选', '富士苹果', 'PRD-APPLE-001', '山东烟台', 'PLANT-202603-A01', 'PROC-202604-A01', 'TRACE-202604150001', 'https://example.com/brand/apple-cover.jpg', 'https://example.com/trace/apple-001', 'https://example.com/qrcode/apple-001.png', '启元优选坚持从源头把控品质，构建种植、加工、物流全链路透明体系。', '1', '0', '初始化样例：已发布页面', 'admin', '2026-04-15 10:07:18', '', '2026-04-15 10:07:18');
INSERT INTO `agri_brand_trace_page` VALUES (2, 'TRACE-202607-RICE-002', '启元优选', '优选大米', 'PRD-RICE-002', '黑龙江五常', 'PLANT-202604-B02', 'PROC-202605-B02', 'TRACE-202604150002', NULL, 'https://example.com/trace/rice-002', NULL, NULL, '0', '0', '初始化样例：草稿页面', 'admin', '2026-04-15 10:07:18', '', '2026-04-15 10:07:18');

-- ----------------------------
-- Table structure for agri_carbon_footprint_model
-- ----------------------------
DROP TABLE IF EXISTS `agri_carbon_footprint_model`;
CREATE TABLE `agri_carbon_footprint_model`  (
  `model_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `model_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模型编码',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模型名称',
  `product_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品类型',
  `boundary_scope` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '核算边界',
  `emission_factor` decimal(16, 4) NOT NULL DEFAULT 0.0000 COMMENT '排放因子',
  `carbon_emission` decimal(16, 4) NULL DEFAULT 0.0000 COMMENT '碳排放量(kgCO2e)',
  `calc_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '核算状态（0待计算 1已计算 2已复核）',
  `calc_time` datetime(0) NULL DEFAULT NULL COMMENT '核算时间',
  `verifier` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '复核人',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`model_id`) USING BTREE,
  UNIQUE INDEX `uk_model_code`(`model_code`) USING BTREE,
  INDEX `idx_product_type`(`product_type`) USING BTREE,
  INDEX `idx_calc_status`(`calc_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '碳足迹核算模型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_carbon_footprint_model
-- ----------------------------

-- ----------------------------
-- Table structure for agri_consumer_scan_query
-- ----------------------------
DROP TABLE IF EXISTS `agri_consumer_scan_query`;
CREATE TABLE `agri_consumer_scan_query`  (
  `query_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trace_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '溯源码',
  `consumer_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消费者姓名',
  `consumer_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消费者手机号',
  `scan_channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'WECHAT' COMMENT '扫码渠道',
  `scan_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扫码地址',
  `scan_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '扫码IP',
  `scan_result` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '查询结果（0未命中 1命中已发布 2命中未发布）',
  `query_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '查询时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`query_id`) USING BTREE,
  INDEX `idx_trace_code`(`trace_code`) USING BTREE,
  INDEX `idx_query_time`(`query_time`) USING BTREE,
  INDEX `idx_scan_result`(`scan_result`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链消费者扫码查询表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_consumer_scan_query
-- ----------------------------
INSERT INTO `agri_consumer_scan_query` VALUES (1, 'TRACE-202607-APPLE-001', '张三', '13800000001', 'WECHAT', '上海市浦东新区', '10.10.1.21', '1', '2026-04-15 10:12:34', '0', '初始化样例：命中已发布', 'admin', '2026-04-15 10:12:34', '', '2026-04-15 10:12:34');
INSERT INTO `agri_consumer_scan_query` VALUES (2, 'TRACE-202607-RICE-002', '李四', '13900000002', 'ALIPAY', '广东省广州市', '10.10.2.22', '2', '2026-04-15 10:12:34', '0', '初始化样例：命中未发布', 'admin', '2026-04-15 10:12:34', '', '2026-04-15 10:12:34');

-- ----------------------------
-- Table structure for agri_dashboard_overview
-- ----------------------------
DROP TABLE IF EXISTS `agri_dashboard_overview`;
CREATE TABLE `agri_dashboard_overview`  (
  `overview_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `total_output` decimal(16, 2) NOT NULL DEFAULT 0.00 COMMENT '总产量(吨)',
  `total_sales` decimal(16, 2) NOT NULL DEFAULT 0.00 COMMENT '总销量(万元)',
  `trace_query_count` bigint NOT NULL DEFAULT 0 COMMENT '溯源查询次数',
  `warning_count` bigint NOT NULL DEFAULT 0 COMMENT '预警数量',
  `online_device_count` bigint NOT NULL DEFAULT 0 COMMENT '在线设备数',
  `pending_task_count` bigint NOT NULL DEFAULT 0 COMMENT '待办数量',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`overview_id`) USING BTREE,
  UNIQUE INDEX `uk_stat_date`(`stat_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据总览看板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_dashboard_overview
-- ----------------------------

-- ----------------------------
-- Table structure for agri_data_attestation_verify
-- ----------------------------
DROP TABLE IF EXISTS `agri_data_attestation_verify`;
CREATE TABLE `agri_data_attestation_verify`  (
  `verify_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `attestation_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存证编号',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务批次号',
  `data_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据类型',
  `origin_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始哈希',
  `chain_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链上哈希',
  `verify_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '校验状态（0待校验 1一致 2不一致）',
  `verify_time` datetime(0) NULL DEFAULT NULL COMMENT '校验时间',
  `verify_by_user` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '校验人',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`verify_id`) USING BTREE,
  UNIQUE INDEX `uk_attestation_no`(`attestation_no`) USING BTREE,
  INDEX `idx_batch_no`(`batch_no`) USING BTREE,
  INDEX `idx_verify_status`(`verify_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据存证与校验表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_data_attestation_verify
-- ----------------------------

-- ----------------------------
-- Table structure for agri_data_uplink_task
-- ----------------------------
DROP TABLE IF EXISTS `agri_data_uplink_task`;
CREATE TABLE `agri_data_uplink_task`  (
  `uplink_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务批次号',
  `data_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据类型',
  `data_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据哈希',
  `chain_platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '链平台',
  `contract_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '合约地址',
  `tx_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '交易哈希',
  `uplink_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '上链状态（0待上链 1已上链 2失败）',
  `uplink_time` datetime(0) NULL DEFAULT NULL COMMENT '上链时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`uplink_id`) USING BTREE,
  INDEX `idx_batch_no`(`batch_no`) USING BTREE,
  INDEX `idx_data_type`(`data_type`) USING BTREE,
  INDEX `idx_uplink_status`(`uplink_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链数据上链任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_data_uplink_task
-- ----------------------------
INSERT INTO `agri_data_uplink_task` VALUES (1, 'BATCH-LOGI-20260415-01', 'LOGISTICS_TRACE', 'e56f6f725f72756f79695f74726163655f686173683031', 'FISCO_BCOS', '0xabcdeffedcba1234567890abcdef1234567890ab', '0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff', '1', '2026-04-15 10:23:28', '0', '初始化样例：已上链', 'admin', '2026-04-15 10:23:28', '', '2026-04-15 10:23:28');
INSERT INTO `agri_data_uplink_task` VALUES (2, 'BATCH-QUALITY-20260415-02', 'QUALITY_REPORT', '715f7265706f72745f686173685f3230323630343135', 'HYPERCHAIN', '0x0987654321abcdef0987654321abcdef09876543', NULL, '0', NULL, '0', '初始化样例：待上链', 'admin', '2026-04-15 10:23:28', '', '2026-04-15 10:23:28');

-- ----------------------------
-- Table structure for agri_device_access_node
-- ----------------------------
DROP TABLE IF EXISTS `agri_device_access_node`;
CREATE TABLE `agri_device_access_node`  (
  `node_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `device_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备编码',
  `device_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备名称',
  `device_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备类型',
  `protocol_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接入协议',
  `firmware_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '固件版本',
  `bind_area` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '绑定区域',
  `access_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '接入状态（0待接入 1在线 2离线 3异常）',
  `last_online_time` datetime(0) NULL DEFAULT NULL COMMENT '最近在线时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`node_id`) USING BTREE,
  UNIQUE INDEX `uk_device_code`(`device_code`) USING BTREE,
  INDEX `idx_device_type`(`device_type`) USING BTREE,
  INDEX `idx_access_status`(`access_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链设备接入管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_device_access_node
-- ----------------------------
INSERT INTO `agri_device_access_node` VALUES (1, 'DEV-TEMP-001', '冷链温湿度传感器-1', 'TEMP_HUMIDITY', 'MQTT', 'v1.2.0', '山东烟台基地A区', '1', NULL, '0', '初始化样例：在线设备', 'admin', '2026-04-15 10:19:42', '', '2026-04-15 10:19:42');
INSERT INTO `agri_device_access_node` VALUES (2, 'DEV-GW-002', '边缘网关-2', 'GATEWAY', 'HTTP', 'v2.0.1', '黑龙江五常加工厂', '0', NULL, '0', '初始化样例：待接入设备', 'admin', '2026-04-15 10:19:42', '', '2026-04-15 10:19:42');

-- ----------------------------
-- Table structure for agri_device_status_monitor
-- ----------------------------
DROP TABLE IF EXISTS `agri_device_status_monitor`;
CREATE TABLE `agri_device_status_monitor`  (
  `monitor_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `device_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备编码',
  `device_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备名称',
  `device_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备类型',
  `online_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '在线状态（0离线 1在线）',
  `battery_level` decimal(6, 2) NOT NULL DEFAULT 100.00 COMMENT '电量(%)',
  `signal_strength` decimal(6, 2) NULL DEFAULT 0.00 COMMENT '信号强度',
  `temperature` decimal(6, 2) NULL DEFAULT 0.00 COMMENT '温度(℃)',
  `humidity` decimal(6, 2) NULL DEFAULT 0.00 COMMENT '湿度(%)',
  `last_report_time` datetime(0) NULL DEFAULT NULL COMMENT '最后上报时间',
  `warning_level` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '预警等级（0正常 1提示 2预警 3严重）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`monitor_id`) USING BTREE,
  UNIQUE INDEX `uk_device_code`(`device_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备状态监控表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_device_status_monitor
-- ----------------------------

-- ----------------------------
-- Table structure for agri_env_sensor_record
-- ----------------------------
DROP TABLE IF EXISTS `agri_env_sensor_record`;
CREATE TABLE `agri_env_sensor_record`  (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `device_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备编码',
  `plot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地块编码',
  `temperature` decimal(8, 2) NULL DEFAULT NULL COMMENT '温度(℃)',
  `humidity` decimal(8, 2) NULL DEFAULT NULL COMMENT '湿度(%)',
  `co2_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '二氧化碳浓度(ppm)',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1预警）',
  `data_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'manual' COMMENT '数据来源',
  `collect_time` datetime(0) NOT NULL COMMENT '采集时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `idx_device_collect_time`(`device_code`, `collect_time`) USING BTREE,
  INDEX `idx_plot_collect_time`(`plot_code`, `collect_time`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链环境传感器数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_env_sensor_record
-- ----------------------------
INSERT INTO `agri_env_sensor_record` VALUES (1, 'DEV-001', 'PLOT-A01', 24.50, 61.20, 460.00, '0', 'mqtt-gateway-01', '2026-04-14 21:34:19', '初始化样例数据', 'admin', '2026-04-14 21:34:19', '', '2026-04-14 21:34:19');
INSERT INTO `agri_env_sensor_record` VALUES (2, 'DEV-002', 'PLOT-A02', 33.10, 84.60, 720.00, '1', 'mqtt-gateway-01', '2026-04-14 21:34:19', '高温高湿预警样例', 'admin', '2026-04-14 21:34:19', '', '2026-04-14 21:34:19');

-- ----------------------------
-- Table structure for agri_farm_operation_record
-- ----------------------------
DROP TABLE IF EXISTS `agri_farm_operation_record`;
CREATE TABLE `agri_farm_operation_record`  (
  `operation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `plot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地块编码',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '作业类型',
  `operation_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '作业内容',
  `operator_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '作业人',
  `input_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '投入品名称',
  `input_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '投入品用量',
  `input_unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用量单位',
  `operation_time` datetime(0) NOT NULL COMMENT '作业时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`operation_id`) USING BTREE,
  INDEX `idx_plot_time`(`plot_code`, `operation_time`) USING BTREE,
  INDEX `idx_type_time`(`operation_type`, `operation_time`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链农事记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_farm_operation_record
-- ----------------------------
INSERT INTO `agri_farm_operation_record` VALUES (1, 'PLOT-A01', 'FERTILIZATION', '春季追肥', '张三', '有机肥', 12.50, 'kg', '2026-04-14 21:41:07', '0', '初始化样例数据', 'admin', '2026-04-14 21:41:07', '', '2026-04-14 21:41:07');
INSERT INTO `agri_farm_operation_record` VALUES (2, 'PLOT-A02', 'PEST_CONTROL', '病虫害防治喷洒', '李四', '生物农药', 3.20, 'L', '2026-04-14 21:41:07', '0', '初始化样例数据', 'admin', '2026-04-14 21:41:07', '', '2026-04-14 21:41:07');

-- ----------------------------
-- Table structure for agri_finance_risk_metric
-- ----------------------------
DROP TABLE IF EXISTS `agri_finance_risk_metric`;
CREATE TABLE `agri_finance_risk_metric`  (
  `risk_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `indicator_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '指标编码',
  `indicator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '指标名称',
  `risk_dimension` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '风险维度',
  `risk_score` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '风险分值',
  `threshold_value` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '阈值',
  `risk_level` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'L' COMMENT '风险等级（L低 M中 H高 C严重）',
  `evaluate_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '评估状态（0待评估 1已评估 2已预警）',
  `evaluate_time` datetime(0) NULL DEFAULT NULL COMMENT '评估时间',
  `evaluator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评估人',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`risk_id`) USING BTREE,
  UNIQUE INDEX `uk_indicator_code`(`indicator_code`) USING BTREE,
  INDEX `idx_risk_dimension`(`risk_dimension`) USING BTREE,
  INDEX `idx_risk_level`(`risk_level`) USING BTREE,
  INDEX `idx_evaluate_status`(`evaluate_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '农业金融风控指标表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_finance_risk_metric
-- ----------------------------

-- ----------------------------
-- Table structure for agri_logistics_temp_humidity
-- ----------------------------
DROP TABLE IF EXISTS `agri_logistics_temp_humidity`;
CREATE TABLE `agri_logistics_temp_humidity`  (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trace_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '运单号',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `device_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '设备编码',
  `collect_location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '采集位置',
  `temperature` decimal(6, 2) NOT NULL COMMENT '温度(°C)',
  `humidity` decimal(6, 2) NOT NULL COMMENT '湿度(%)',
  `temp_upper_limit` decimal(6, 2) NULL DEFAULT 8.00 COMMENT '温度上限',
  `temp_lower_limit` decimal(6, 2) NULL DEFAULT 2.00 COMMENT '温度下限',
  `humidity_upper_limit` decimal(6, 2) NULL DEFAULT 75.00 COMMENT '湿度上限',
  `humidity_lower_limit` decimal(6, 2) NULL DEFAULT 45.00 COMMENT '湿度下限',
  `alert_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '告警标记（0正常 1超阈值）',
  `alert_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '告警信息',
  `collect_time` datetime(0) NOT NULL COMMENT '采集时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `idx_trace_collect_time`(`trace_code`, `collect_time`) USING BTREE,
  INDEX `idx_alert_flag`(`alert_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链物流温湿度监控表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_logistics_temp_humidity
-- ----------------------------
INSERT INTO `agri_logistics_temp_humidity` VALUES (1, 'TRACE-202604150001', 'SO-20260415001', 'IOT-TEMP-001', '青岛仓储中心', 5.20, 62.50, 8.00, 2.00, 75.00, 45.00, '0', '正常', '2026-04-15 09:51:57', '0', '初始化样例：正常记录', 'admin', '2026-04-15 09:51:57', '', '2026-04-15 09:51:57');
INSERT INTO `agri_logistics_temp_humidity` VALUES (2, 'TRACE-202604150001', 'SO-20260415001', 'IOT-TEMP-001', '上海门店A', 10.60, 80.30, 8.00, 2.00, 75.00, 45.00, '1', '温度超过上限;湿度超过上限', '2026-04-15 10:51:57', '0', '初始化样例：超阈值告警', 'admin', '2026-04-15 09:51:57', '', '2026-04-15 09:51:57');

-- ----------------------------
-- Table structure for agri_logistics_track
-- ----------------------------
DROP TABLE IF EXISTS `agri_logistics_track`;
CREATE TABLE `agri_logistics_track`  (
  `track_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trace_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '运单号',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '销售订单号',
  `product_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品批次号',
  `vehicle_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '车辆编号',
  `driver_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '司机姓名',
  `driver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '司机电话',
  `start_location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '起始地',
  `current_location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '当前位置',
  `target_location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目的地',
  `route_path` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路线轨迹',
  `track_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '运输状态（0待发车 1运输中 2已签收 3异常）',
  `event_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '轨迹描述',
  `event_time` datetime(0) NOT NULL COMMENT '事件时间',
  `temperature` decimal(6, 2) NULL DEFAULT NULL COMMENT '温度(°C)',
  `humidity` decimal(6, 2) NULL DEFAULT NULL COMMENT '湿度(%)',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`track_id`) USING BTREE,
  INDEX `idx_trace_time`(`trace_code`, `event_time`) USING BTREE,
  INDEX `idx_order_no`(`order_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链物流路径追踪表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_logistics_track
-- ----------------------------
INSERT INTO `agri_logistics_track` VALUES (1, 'TRACE-202604150001', 'SO-20260415001', 'PROC-202604-A01', '鲁A9F321', '张强', '13800112233', '青岛基地', '青岛仓储中心', '上海门店A', '青岛基地->青岛仓储中心->上海门店A', '1', '车辆已到达青岛仓储中心，等待转运。', '2026-04-15 09:44:18', 5.20, 62.50, 120.382640, 36.067108, '0', '初始化样例：运输中', 'admin', '2026-04-15 09:44:18', '', '2026-04-15 09:44:18');
INSERT INTO `agri_logistics_track` VALUES (2, 'TRACE-202604150001', 'SO-20260415001', 'PROC-202604-A01', '鲁A9F321', '张强', '13800112233', '青岛基地', '上海门店A', '上海门店A', '青岛基地->青岛仓储中心->上海门店A', '2', '货物已签收。', '2026-04-15 11:44:18', 7.10, 58.40, 121.473700, 31.230400, '0', '初始化样例：已签收', 'admin', '2026-04-15 09:44:18', '', '2026-04-15 09:44:18');

-- ----------------------------
-- Table structure for agri_logistics_warning
-- ----------------------------
DROP TABLE IF EXISTS `agri_logistics_warning`;
CREATE TABLE `agri_logistics_warning`  (
  `warning_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `trace_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '运单号',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
  `warning_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '预警类型',
  `warning_level` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '预警级别（1一般 2严重 3紧急）',
  `warning_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '预警状态（0待处理 1处理中 2已关闭）',
  `source_record_id` bigint NULL DEFAULT NULL COMMENT '来源记录ID（温湿度记录ID）',
  `warning_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '预警标题',
  `warning_content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '预警内容',
  `warning_time` datetime(0) NOT NULL COMMENT '预警时间',
  `handler` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理人',
  `handle_time` datetime(0) NULL DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '处理备注',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`warning_id`) USING BTREE,
  UNIQUE INDEX `uk_source_record_id`(`source_record_id`) USING BTREE,
  INDEX `idx_trace_warning_time`(`trace_code`, `warning_time`) USING BTREE,
  INDEX `idx_warning_status`(`warning_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链在途异常预警表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_logistics_warning
-- ----------------------------
INSERT INTO `agri_logistics_warning` VALUES (1, 'TRACE-202604150001', 'SO-20260415001', 'TEMP_HUMIDITY', '2', '0', 2, '温湿度在途异常', '温度超过上限;湿度超过上限', '2026-04-15 09:56:31', NULL, NULL, NULL, '0', '初始化样例：待处理预警', 'admin', '2026-04-15 09:56:31', '', '2026-04-15 09:56:31');
INSERT INTO `agri_logistics_warning` VALUES (2, 'TRACE-202604150001', 'SO-20260415001', 'TEMP_HUMIDITY', '1', '2', 1, '在途温控提示', '温湿度已恢复至正常区间。', '2026-04-15 07:56:31', 'admin', '2026-04-15 08:56:31', '已确认恢复正常，关闭预警。', '0', '初始化样例：已关闭预警', 'admin', '2026-04-15 09:56:31', '', '2026-04-15 09:56:31');

-- ----------------------------
-- Table structure for agri_market_forecast
-- ----------------------------
DROP TABLE IF EXISTS `agri_market_forecast`;
CREATE TABLE `agri_market_forecast`  (
  `forecast_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `market_area` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '市场区域',
  `product_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品编码',
  `product_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品名称',
  `period_start` date NOT NULL COMMENT '预测周期开始',
  `period_end` date NOT NULL COMMENT '预测周期结束',
  `historical_sales_kg` decimal(12, 2) NULL DEFAULT NULL COMMENT '历史销量(kg)',
  `forecast_sales_kg` decimal(12, 2) NULL DEFAULT NULL COMMENT '预测销量(kg)',
  `forecast_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '预测价格(元/kg)',
  `confidence_rate` decimal(6, 4) NULL DEFAULT NULL COMMENT '置信度',
  `model_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型版本',
  `forecast_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '预测状态（0待预测 1已预测 2失败）',
  `forecast_time` datetime(0) NULL DEFAULT NULL COMMENT '预测时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`forecast_id`) USING BTREE,
  INDEX `idx_market_period`(`market_area`, `period_start`, `period_end`) USING BTREE,
  INDEX `idx_forecast_status`(`forecast_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链市场预测分析表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_market_forecast
-- ----------------------------
INSERT INTO `agri_market_forecast` VALUES (1, '华东大区', 'PRD-APPLE-001', '富士苹果', '2026-07-01', '2026-07-31', 12850.00, 13878.00, 8.80, 0.8200, 'market-model-v1', '1', '2026-04-15 10:00:38', '0', '初始化样例：已预测', 'admin', '2026-04-15 10:00:38', '', '2026-04-15 10:00:38');
INSERT INTO `agri_market_forecast` VALUES (2, '华南大区', 'PRD-RICE-002', '优选大米', '2026-07-01', '2026-07-31', 22500.00, NULL, NULL, NULL, 'market-model-v1', '0', NULL, '0', '初始化样例：待预测', 'admin', '2026-04-15 10:00:38', '', '2026-04-15 10:00:38');

-- ----------------------------
-- Table structure for agri_output_sales_trend
-- ----------------------------
DROP TABLE IF EXISTS `agri_output_sales_trend`;
CREATE TABLE `agri_output_sales_trend`  (
  `trend_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `output_value` decimal(16, 2) NOT NULL DEFAULT 0.00 COMMENT '产量(吨)',
  `sales_value` decimal(16, 2) NOT NULL DEFAULT 0.00 COMMENT '销量(万元)',
  `target_output` decimal(16, 2) NOT NULL DEFAULT 0.00 COMMENT '目标产量(吨)',
  `target_sales` decimal(16, 2) NOT NULL DEFAULT 0.00 COMMENT '目标销量(万元)',
  `output_mom_rate` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '产量环比增幅(%)',
  `sales_mom_rate` decimal(8, 2) NOT NULL DEFAULT 0.00 COMMENT '销量环比增幅(%)',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`trend_id`) USING BTREE,
  UNIQUE INDEX `uk_stat_date`(`stat_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '产量与销量趋势图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_output_sales_trend
-- ----------------------------

-- ----------------------------
-- Table structure for agri_pest_identify_task
-- ----------------------------
DROP TABLE IF EXISTS `agri_pest_identify_task`;
CREATE TABLE `agri_pest_identify_task`  (
  `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `plot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地块编码',
  `crop_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '作物名称',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片URL',
  `identify_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '识别状态（0待识别 1已识别 2失败）',
  `identify_result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '识别结果',
  `confidence` decimal(6, 4) NULL DEFAULT NULL COMMENT '置信度',
  `identify_time` datetime(0) NULL DEFAULT NULL COMMENT '识别时间',
  `model_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型版本',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`task_id`) USING BTREE,
  INDEX `idx_plot_status`(`plot_code`, `identify_status`) USING BTREE,
  INDEX `idx_crop_status`(`crop_name`, `identify_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链病虫害识别任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_pest_identify_task
-- ----------------------------
INSERT INTO `agri_pest_identify_task` VALUES (1, 'PLOT-A01', '番茄', 'https://example.com/image/tomato-1.jpg', '1', '疑似早疫病', 0.9230, '2026-04-14 21:48:06', 'pest-model-v1', '0', '初始化样例：已识别', 'admin', '2026-04-14 21:48:06', '', '2026-04-14 21:48:06');
INSERT INTO `agri_pest_identify_task` VALUES (2, 'PLOT-A02', '黄瓜', 'https://example.com/image/cucumber-1.jpg', '0', NULL, NULL, NULL, NULL, '0', '初始化样例：待识别', 'admin', '2026-04-14 21:48:06', '', '2026-04-14 21:48:06');

-- ----------------------------
-- Table structure for agri_process_batch_link
-- ----------------------------
DROP TABLE IF EXISTS `agri_process_batch_link`;
CREATE TABLE `agri_process_batch_link`  (
  `link_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `planting_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '种植批次号',
  `process_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '加工批次号',
  `product_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '产品编码',
  `process_weight_kg` decimal(12, 2) NOT NULL COMMENT '加工重量(kg)',
  `process_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '加工状态（0待加工 1加工中 2已完成）',
  `process_time` datetime(0) NULL DEFAULT NULL COMMENT '加工时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`link_id`) USING BTREE,
  UNIQUE INDEX `uk_process_batch_no`(`process_batch_no`) USING BTREE,
  INDEX `idx_planting_batch_no`(`planting_batch_no`) USING BTREE,
  INDEX `idx_process_status`(`process_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链加工批次关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_process_batch_link
-- ----------------------------
INSERT INTO `agri_process_batch_link` VALUES (1, 'PLANT-202604-A01', 'PROC-202604-A01', 'TOMATO-PROD-A', 3200.00, '2', '2026-04-14 21:59:12', '0', '初始化样例：已完成', 'admin', '2026-04-14 21:59:12', '', '2026-04-14 21:59:12');
INSERT INTO `agri_process_batch_link` VALUES (2, 'PLANT-202604-A02', 'PROC-202604-A02', 'CUCUMBER-PROD-A', 2100.00, '1', NULL, '0', '初始化样例：加工中', 'admin', '2026-04-14 21:59:12', '', '2026-04-14 21:59:12');

-- ----------------------------
-- Table structure for agri_quality_inspect_task
-- ----------------------------
DROP TABLE IF EXISTS `agri_quality_inspect_task`;
CREATE TABLE `agri_quality_inspect_task`  (
  `inspect_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `process_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '加工批次号',
  `sample_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '样品编码',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片URL',
  `inspect_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '检测状态（0待检测 1已检测 2失败）',
  `quality_grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品质等级',
  `defect_rate` decimal(6, 4) NULL DEFAULT NULL COMMENT '缺陷率',
  `inspect_result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '检测结果',
  `inspect_time` datetime(0) NULL DEFAULT NULL COMMENT '检测时间',
  `model_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型版本',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`inspect_id`) USING BTREE,
  INDEX `idx_process_status`(`process_batch_no`, `inspect_status`) USING BTREE,
  INDEX `idx_sample_code`(`sample_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链视觉品质检测任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_quality_inspect_task
-- ----------------------------
INSERT INTO `agri_quality_inspect_task` VALUES (1, 'PROC-202604-A01', 'SAMPLE-A01-01', 'https://example.com/inspect/a01-01.jpg', '1', 'A', 0.0150, '外观完整，色泽良好', '2026-04-14 22:03:04', 'vision-model-v1', '0', '初始化样例：已检测', 'admin', '2026-04-14 22:03:04', '', '2026-04-14 22:03:04');
INSERT INTO `agri_quality_inspect_task` VALUES (2, 'PROC-202604-A02', 'SAMPLE-A02-01', 'https://example.com/inspect/a02-01.jpg', '0', NULL, NULL, NULL, NULL, 'vision-model-v1', '0', '初始化样例：待检测', 'admin', '2026-04-14 22:03:04', '', '2026-04-14 22:03:04');

-- ----------------------------
-- Table structure for agri_quality_report
-- ----------------------------
DROP TABLE IF EXISTS `agri_quality_report`;
CREATE TABLE `agri_quality_report`  (
  `report_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `report_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '报告编号',
  `inspect_id` bigint NOT NULL COMMENT '检测任务ID',
  `process_batch_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '加工批次号',
  `quality_grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '品质等级',
  `defect_rate` decimal(6, 4) NULL DEFAULT NULL COMMENT '缺陷率',
  `report_summary` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '报告摘要',
  `report_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '报告状态（0草稿 1已生成）',
  `report_time` datetime(0) NULL DEFAULT NULL COMMENT '报告时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`report_id`) USING BTREE,
  UNIQUE INDEX `uk_report_no`(`report_no`) USING BTREE,
  UNIQUE INDEX `uk_inspect_id`(`inspect_id`) USING BTREE,
  INDEX `idx_process_batch_no`(`process_batch_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链质检报告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_quality_report
-- ----------------------------
INSERT INTO `agri_quality_report` VALUES (1, 'QCR-20260414-0001', 1, 'PROC-202604-A01', 'A', 0.0150, '批次样品整体品质良好，缺陷率处于可控范围。', '1', '2026-04-15 07:51:27', '0', '初始化样例：已生成', 'admin', '2026-04-15 07:51:27', '', '2026-04-15 07:51:27');

-- ----------------------------
-- Table structure for agri_smart_contract_deploy
-- ----------------------------
DROP TABLE IF EXISTS `agri_smart_contract_deploy`;
CREATE TABLE `agri_smart_contract_deploy`  (
  `deploy_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `contract_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '合约名称',
  `contract_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '合约版本',
  `chain_platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '链平台',
  `source_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '源码哈希',
  `abi_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'ABI',
  `contract_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '合约地址',
  `deploy_tx_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部署交易哈希',
  `deploy_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '部署状态（0待部署 1已部署 2失败）',
  `deploy_time` datetime(0) NULL DEFAULT NULL COMMENT '部署时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`deploy_id`) USING BTREE,
  INDEX `idx_contract_name`(`contract_name`) USING BTREE,
  INDEX `idx_chain_platform`(`chain_platform`) USING BTREE,
  INDEX `idx_deploy_status`(`deploy_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链智能合约部署表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_smart_contract_deploy
-- ----------------------------
INSERT INTO `agri_smart_contract_deploy` VALUES (1, 'TraceStore', 'v1.0.0', 'FISCO_BCOS', '74726163655f73746f72655f736f757263655f68617368', '[{\"inputs\":[{\"name\":\"traceCode\",\"type\":\"string\"}],\"name\":\"storeTrace\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]', '0x1234567890abcdef1234567890abcdef12345678', '0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890', '1', '2026-04-15 10:27:14', '0', '初始化样例：已部署', 'admin', '2026-04-15 10:27:14', '', '2026-04-15 10:27:14');
INSERT INTO `agri_smart_contract_deploy` VALUES (2, 'QualityAudit', 'v1.1.0', 'HYPERCHAIN', '7175616c6974795f61756469745f736f757263655f68617368', '[{\"inputs\":[{\"name\":\"reportNo\",\"type\":\"string\"}],\"name\":\"storeAudit\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]', NULL, NULL, '0', NULL, '0', '初始化样例：待部署', 'admin', '2026-04-15 10:27:14', '', '2026-04-15 10:27:14');

-- ----------------------------
-- Table structure for agri_supply_chain_contract
-- ----------------------------
DROP TABLE IF EXISTS `agri_supply_chain_contract`;
CREATE TABLE `agri_supply_chain_contract`  (
  `contract_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `contract_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '合约编号',
  `contract_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '合约名称',
  `finance_subject` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '融资主体',
  `finance_amount` decimal(16, 2) NOT NULL DEFAULT 0.00 COMMENT '融资金额',
  `interest_rate` decimal(8, 4) NULL DEFAULT 0.0000 COMMENT '利率(%)',
  `start_date` date NULL DEFAULT NULL COMMENT '起始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '到期日期',
  `contract_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '合约状态（0草稿 1生效 2到期 3终止）',
  `risk_level` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'L' COMMENT '风控等级（L低 M中 H高 C严重）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`contract_id`) USING BTREE,
  UNIQUE INDEX `uk_contract_no`(`contract_no`) USING BTREE,
  INDEX `idx_contract_status`(`contract_status`) USING BTREE,
  INDEX `idx_risk_level`(`risk_level`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应链金融合约管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_supply_chain_contract
-- ----------------------------

-- ----------------------------
-- Table structure for agri_third_party_api_access
-- ----------------------------
DROP TABLE IF EXISTS `agri_third_party_api_access`;
CREATE TABLE `agri_third_party_api_access`  (
  `access_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `access_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接入编码',
  `access_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接入名称',
  `api_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'API类型',
  `provider` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商',
  `endpoint_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求地址',
  `timeout_sec` int NOT NULL DEFAULT 30 COMMENT '超时(秒)',
  `success_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '成功率(%)',
  `call_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '调用状态（0未调用 1成功 2失败）',
  `last_call_time` datetime(0) NULL DEFAULT NULL COMMENT '最近调用时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`access_id`) USING BTREE,
  UNIQUE INDEX `uk_access_code`(`access_code`) USING BTREE,
  INDEX `idx_api_type`(`api_type`) USING BTREE,
  INDEX `idx_call_status`(`call_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '第三方API接入表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_third_party_api_access
-- ----------------------------

-- ----------------------------
-- Table structure for agri_todo_task_reminder
-- ----------------------------
DROP TABLE IF EXISTS `agri_todo_task_reminder`;
CREATE TABLE `agri_todo_task_reminder`  (
  `reminder_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编码',
  `task_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务标题',
  `task_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务类型',
  `priority_level` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '2' COMMENT '优先级（1低 2中 3高 4紧急）',
  `assignee` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理人',
  `deadline_time` datetime(0) NOT NULL COMMENT '截止时间',
  `reminder_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '提醒状态（0待提醒 1已提醒 2已完成）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`reminder_id`) USING BTREE,
  UNIQUE INDEX `uk_task_code`(`task_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务待办提醒表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_todo_task_reminder
-- ----------------------------

-- ----------------------------
-- Table structure for agri_trace_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `agri_trace_audit_log`;
CREATE TABLE `agri_trace_audit_log`  (
  `audit_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务编号',
  `module_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块名称',
  `action_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型',
  `operator_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人',
  `operate_time` datetime(0) NULL DEFAULT NULL COMMENT '操作时间',
  `operate_result` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1' COMMENT '操作结果（0失败 1成功）',
  `tx_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易哈希',
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`audit_id`) USING BTREE,
  INDEX `idx_biz_no`(`biz_no`) USING BTREE,
  INDEX `idx_action_type`(`action_type`) USING BTREE,
  INDEX `idx_operate_result`(`operate_result`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '溯源审计日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_trace_audit_log
-- ----------------------------

-- ----------------------------
-- Table structure for agri_trace_query_stats
-- ----------------------------
DROP TABLE IF EXISTS `agri_trace_query_stats`;
CREATE TABLE `agri_trace_query_stats`  (
  `stats_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `total_query_count` bigint NOT NULL DEFAULT 0 COMMENT '查询总次数',
  `unique_user_count` bigint NOT NULL DEFAULT 0 COMMENT '独立用户数',
  `avg_duration_ms` bigint NOT NULL DEFAULT 0 COMMENT '平均响应时长(ms)',
  `success_count` bigint NOT NULL DEFAULT 0 COMMENT '成功次数',
  `fail_count` bigint NOT NULL DEFAULT 0 COMMENT '失败次数',
  `peak_qps` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '峰值QPS',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`stats_id`) USING BTREE,
  UNIQUE INDEX `uk_stat_date`(`stat_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '溯源查询统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_trace_query_stats
-- ----------------------------

-- ----------------------------
-- Table structure for agri_user_access_grant
-- ----------------------------
DROP TABLE IF EXISTS `agri_user_access_grant`;
CREATE TABLE `agri_user_access_grant`  (
  `grant_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限标识',
  `data_scope` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据权限范围',
  `menu_scope` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单可见范围',
  `grant_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '授权状态（0待审核 1已授权 2已驳回）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`grant_id`) USING BTREE,
  INDEX `idx_user_name`(`user_name`) USING BTREE,
  INDEX `idx_role_key`(`role_key`) USING BTREE,
  INDEX `idx_grant_status`(`grant_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链用户权限管理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_user_access_grant
-- ----------------------------
INSERT INTO `agri_user_access_grant` VALUES (1, 'admin', '管理员', 'agri_admin', 'ALL', 'agri:*', '1', '0', '初始化样例：超级管理员已授权', 'admin', '2026-04-15 10:16:07', '', '2026-04-15 10:16:07');
INSERT INTO `agri_user_access_grant` VALUES (2, 'agri_demo', '农业演示账号', 'agri_operator', 'DEPT', 'agri:baseRule,agri:marketForecast,agri:consumerScan', '0', '0', '初始化样例：待审核', 'admin', '2026-04-15 10:16:07', '', '2026-04-15 10:16:07');

-- ----------------------------
-- Table structure for agri_warning_summary
-- ----------------------------
DROP TABLE IF EXISTS `agri_warning_summary`;
CREATE TABLE `agri_warning_summary`  (
  `summary_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `summary_date` date NOT NULL COMMENT '统计日期',
  `total_warning_count` bigint NOT NULL DEFAULT 0 COMMENT '预警总数',
  `level1_count` bigint NOT NULL DEFAULT 0 COMMENT '提示级数量',
  `level2_count` bigint NOT NULL DEFAULT 0 COMMENT '预警级数量',
  `level3_count` bigint NOT NULL DEFAULT 0 COMMENT '严重级数量',
  `handled_count` bigint NOT NULL DEFAULT 0 COMMENT '已处理数量',
  `pending_count` bigint NOT NULL DEFAULT 0 COMMENT '待处理数量',
  `avg_handle_minutes` bigint NOT NULL DEFAULT 0 COMMENT '平均处理时长(分钟)',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`summary_id`) USING BTREE,
  UNIQUE INDEX `uk_summary_date`(`summary_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预警信息汇总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_warning_summary
-- ----------------------------

-- ----------------------------
-- Table structure for agri_yield_forecast_task
-- ----------------------------
DROP TABLE IF EXISTS `agri_yield_forecast_task`;
CREATE TABLE `agri_yield_forecast_task`  (
  `forecast_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `plot_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地块编码',
  `crop_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '作物名称',
  `season` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '季节',
  `sow_date` date NOT NULL COMMENT '播种日期',
  `area_mu` decimal(10, 2) NOT NULL COMMENT '种植面积(亩)',
  `forecast_yield_kg` decimal(12, 2) NULL DEFAULT NULL COMMENT '预测产量(kg)',
  `model_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型版本',
  `forecast_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '预测状态（0待预测 1已预测 2失败）',
  `forecast_time` datetime(0) NULL DEFAULT NULL COMMENT '预测时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`forecast_id`) USING BTREE,
  INDEX `idx_plot_status`(`plot_code`, `forecast_status`) USING BTREE,
  INDEX `idx_crop_status`(`crop_name`, `forecast_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '启元农链产量预测任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of agri_yield_forecast_task
-- ----------------------------
INSERT INTO `agri_yield_forecast_task` VALUES (1, 'PLOT-A01', '番茄', '春季', '2026-03-01', 8.50, 3825.00, 'yield-model-v1', '1', '2026-04-14 21:52:48', '0', '初始化样例：已预测', 'admin', '2026-04-14 21:52:48', '', '2026-04-14 21:52:48');
INSERT INTO `agri_yield_forecast_task` VALUES (2, 'PLOT-A02', '黄瓜', '春季', '2026-03-05', 6.20, NULL, 'yield-model-v1', '0', NULL, '0', '初始化样例：待预测', 'admin', '2026-04-14 21:52:48', '', '2026-04-14 21:52:48');

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `form_col_num` int NULL DEFAULT 1 COMMENT '表单布局（单列 双列 三列）',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table
-- ----------------------------

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint NULL DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `blob_data` blob NULL COMMENT '存放持久化Trigger对象',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Blob类型的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_blob_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`, `calendar_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '日历信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `cron_expression` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'Cron类型的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint NOT NULL COMMENT '触发的时间',
  `sched_time` bigint NOT NULL COMMENT '定时器制定的时间',
  `priority` int NOT NULL COMMENT '优先级',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`, `entry_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '已触发的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '任务详细信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`, `lock_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '存储的悲观锁信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  PRIMARY KEY (`sched_name`, `trigger_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '暂停的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`, `instance_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '调度器状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `repeat_count` bigint NOT NULL COMMENT '重复的次数统计',
  `repeat_interval` bigint NOT NULL COMMENT '重复的间隔时间',
  `times_triggered` bigint NOT NULL COMMENT '已经触发的次数',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '简单触发器的信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `str_prop_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
  `str_prop_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
  `str_prop_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
  `int_prop_1` int NULL DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
  `int_prop_2` int NULL DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
  `long_prop_1` bigint NULL DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
  `long_prop_2` bigint NULL DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
  `dec_prop_1` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
  `dec_prop_2` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
  `bool_prop_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
  `bool_prop_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '同步机制的行锁表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint NULL DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint NULL DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int NULL DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器的类型',
  `start_time` bigint NOT NULL COMMENT '开始时间',
  `end_time` bigint NULL DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint NULL DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  INDEX `sched_name`(`sched_name`, `job_name`, `job_group`) USING BTREE,
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '触发器详细信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2026-04-14 21:16:22', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2026-04-14 21:16:22', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2026-04-14 21:16:22', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config` VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2026-04-14 21:16:22', '', NULL, '是否开启验证码功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2026-04-14 21:16:22', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2026-04-14 21:16:22', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO `sys_config` VALUES (7, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2026-04-14 21:16:22', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` VALUES (8, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2026-04-14 21:16:22', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 200 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '若依科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL);
INSERT INTO `sys_dept` VALUES (101, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL);
INSERT INTO `sys_dept` VALUES (102, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL);
INSERT INTO `sys_dept` VALUES (103, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL);
INSERT INTO `sys_dept` VALUES (104, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL);
INSERT INTO `sys_dept` VALUES (105, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL);
INSERT INTO `sys_dept` VALUES (106, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL);
INSERT INTO `sys_dept` VALUES (107, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL);
INSERT INTO `sys_dept` VALUES (108, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL);
INSERT INTO `sys_dept` VALUES (109, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL);

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (100, 1, '生产数据', 'production', 'agri_data_type', '', 'primary', 'Y', '0', 'admin', '2026-04-15 10:31:16', '', NULL, '生产记录');
INSERT INTO `sys_dict_data` VALUES (101, 2, '加工数据', 'processing', 'agri_data_type', '', 'success', 'N', '0', 'admin', '2026-04-15 10:31:16', '', NULL, '加工记录');
INSERT INTO `sys_dict_data` VALUES (102, 3, '流通数据', 'logistics', 'agri_data_type', '', 'warning', 'N', '0', 'admin', '2026-04-15 10:31:16', '', NULL, '流通记录');
INSERT INTO `sys_dict_data` VALUES (103, 1, '待校验', '0', 'agri_verify_status', '', 'info', 'Y', '0', 'admin', '2026-04-15 10:31:16', '', NULL, '待比对');
INSERT INTO `sys_dict_data` VALUES (104, 2, '一致', '1', 'agri_verify_status', '', 'success', 'N', '0', 'admin', '2026-04-15 10:31:16', '', NULL, '校验一致');
INSERT INTO `sys_dict_data` VALUES (105, 3, '不一致', '2', 'agri_verify_status', '', 'danger', 'N', '0', 'admin', '2026-04-15 10:31:16', '', NULL, '校验不一致');
INSERT INTO `sys_dict_data` VALUES (106, 1, '创建', 'create', 'agri_audit_action_type', '', 'primary', 'Y', '0', 'admin', '2026-04-15 10:51:53', '', NULL, '新增记录');
INSERT INTO `sys_dict_data` VALUES (107, 2, '更新', 'update', 'agri_audit_action_type', '', 'warning', 'N', '0', 'admin', '2026-04-15 10:51:53', '', NULL, '修改记录');
INSERT INTO `sys_dict_data` VALUES (108, 3, '校验', 'verify', 'agri_audit_action_type', '', 'success', 'N', '0', 'admin', '2026-04-15 10:51:53', '', NULL, '校验记录');
INSERT INTO `sys_dict_data` VALUES (109, 4, '删除', 'delete', 'agri_audit_action_type', '', 'danger', 'N', '0', 'admin', '2026-04-15 10:51:53', '', NULL, '删除记录');
INSERT INTO `sys_dict_data` VALUES (110, 1, '成功', '1', 'agri_audit_result', '', 'success', 'Y', '0', 'admin', '2026-04-15 10:51:53', '', NULL, '操作成功');
INSERT INTO `sys_dict_data` VALUES (111, 2, '失败', '0', 'agri_audit_result', '', 'danger', 'N', '0', 'admin', '2026-04-15 10:51:53', '', NULL, '操作失败');
INSERT INTO `sys_dict_data` VALUES (112, 1, '蔬菜', 'vegetable', 'agri_product_type', '', 'success', 'Y', '0', 'admin', '2026-04-15 10:55:45', '', NULL, '蔬菜类');
INSERT INTO `sys_dict_data` VALUES (113, 2, '水果', 'fruit', 'agri_product_type', '', 'primary', 'N', '0', 'admin', '2026-04-15 10:55:45', '', NULL, '水果类');
INSERT INTO `sys_dict_data` VALUES (114, 3, '粮食', 'grain', 'agri_product_type', '', 'warning', 'N', '0', 'admin', '2026-04-15 10:55:45', '', NULL, '粮食类');
INSERT INTO `sys_dict_data` VALUES (115, 1, '待计算', '0', 'agri_calc_status', '', 'info', 'Y', '0', 'admin', '2026-04-15 10:55:45', '', NULL, '模型待计算');
INSERT INTO `sys_dict_data` VALUES (116, 2, '已计算', '1', 'agri_calc_status', '', 'success', 'N', '0', 'admin', '2026-04-15 10:55:45', '', NULL, '模型已计算');
INSERT INTO `sys_dict_data` VALUES (117, 3, '已复核', '2', 'agri_calc_status', '', 'primary', 'N', '0', 'admin', '2026-04-15 10:55:45', '', NULL, '模型已复核');
INSERT INTO `sys_dict_data` VALUES (118, 1, '信用风险', 'credit', 'agri_risk_dimension', '', 'warning', 'Y', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '主体信用风险');
INSERT INTO `sys_dict_data` VALUES (119, 2, '经营风险', 'operation', 'agri_risk_dimension', '', 'primary', 'N', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '经营稳定性风险');
INSERT INTO `sys_dict_data` VALUES (120, 3, '合规风险', 'compliance', 'agri_risk_dimension', '', 'danger', 'N', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '政策与合规风险');
INSERT INTO `sys_dict_data` VALUES (121, 1, '低', 'L', 'agri_risk_level', '', 'info', 'Y', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '低风险');
INSERT INTO `sys_dict_data` VALUES (122, 2, '中', 'M', 'agri_risk_level', '', 'primary', 'N', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '中风险');
INSERT INTO `sys_dict_data` VALUES (123, 3, '高', 'H', 'agri_risk_level', '', 'warning', 'N', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '高风险');
INSERT INTO `sys_dict_data` VALUES (124, 4, '严重', 'C', 'agri_risk_level', '', 'danger', 'N', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '严重风险');
INSERT INTO `sys_dict_data` VALUES (125, 1, '待评估', '0', 'agri_evaluate_status', '', 'info', 'Y', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '待评估');
INSERT INTO `sys_dict_data` VALUES (126, 2, '已评估', '1', 'agri_evaluate_status', '', 'success', 'N', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '已评估');
INSERT INTO `sys_dict_data` VALUES (127, 3, '已预警', '2', 'agri_evaluate_status', '', 'danger', 'N', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '已预警');
INSERT INTO `sys_dict_data` VALUES (128, 1, '草稿', '0', 'agri_contract_status', '', 'info', 'Y', '0', 'admin', '2026-04-15 11:03:17', '', NULL, '草稿状态');
INSERT INTO `sys_dict_data` VALUES (129, 2, '生效', '1', 'agri_contract_status', '', 'success', 'N', '0', 'admin', '2026-04-15 11:03:17', '', NULL, '生效状态');
INSERT INTO `sys_dict_data` VALUES (130, 3, '到期', '2', 'agri_contract_status', '', 'warning', 'N', '0', 'admin', '2026-04-15 11:03:17', '', NULL, '到期状态');
INSERT INTO `sys_dict_data` VALUES (131, 4, '终止', '3', 'agri_contract_status', '', 'danger', 'N', '0', 'admin', '2026-04-15 11:03:17', '', NULL, '终止状态');
INSERT INTO `sys_dict_data` VALUES (132, 1, '天气服务', 'weather', 'agri_api_type', '', 'primary', 'Y', '0', 'admin', '2026-04-15 11:07:04', '', NULL, '天气数据API');
INSERT INTO `sys_dict_data` VALUES (133, 2, '市场行情', 'market', 'agri_api_type', '', 'success', 'N', '0', 'admin', '2026-04-15 11:07:04', '', NULL, '市场行情API');
INSERT INTO `sys_dict_data` VALUES (134, 3, '物流查询', 'logistics', 'agri_api_type', '', 'warning', 'N', '0', 'admin', '2026-04-15 11:07:04', '', NULL, '物流跟踪API');
INSERT INTO `sys_dict_data` VALUES (135, 1, '未调用', '0', 'agri_api_call_status', '', 'info', 'Y', '0', 'admin', '2026-04-15 11:07:04', '', NULL, '未调用');
INSERT INTO `sys_dict_data` VALUES (136, 2, '成功', '1', 'agri_api_call_status', '', 'success', 'N', '0', 'admin', '2026-04-15 11:07:04', '', NULL, '调用成功');
INSERT INTO `sys_dict_data` VALUES (137, 3, '失败', '2', 'agri_api_call_status', '', 'danger', 'N', '0', 'admin', '2026-04-15 11:07:04', '', NULL, '调用失败');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '登录状态列表');
INSERT INTO `sys_dict_type` VALUES (100, '农业数据类型', 'agri_data_type', '0', 'admin', '2026-04-15 10:31:16', '', NULL, '农业场景数据类型');
INSERT INTO `sys_dict_type` VALUES (101, '存证校验状态', 'agri_verify_status', '0', 'admin', '2026-04-15 10:31:16', '', NULL, '数据存证校验结果状态');
INSERT INTO `sys_dict_type` VALUES (102, '审计操作类型', 'agri_audit_action_type', '0', 'admin', '2026-04-15 10:51:53', '', NULL, '溯源审计操作类型');
INSERT INTO `sys_dict_type` VALUES (103, '审计操作结果', 'agri_audit_result', '0', 'admin', '2026-04-15 10:51:53', '', NULL, '溯源审计操作结果');
INSERT INTO `sys_dict_type` VALUES (104, '农产品类型', 'agri_product_type', '0', 'admin', '2026-04-15 10:55:45', '', NULL, '碳足迹核算产品类型');
INSERT INTO `sys_dict_type` VALUES (105, '碳足迹核算状态', 'agri_calc_status', '0', 'admin', '2026-04-15 10:55:45', '', NULL, '碳足迹核算状态');
INSERT INTO `sys_dict_type` VALUES (106, '风险维度', 'agri_risk_dimension', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '农业金融风控风险维度');
INSERT INTO `sys_dict_type` VALUES (107, '风险等级', 'agri_risk_level', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '农业金融风控风险等级');
INSERT INTO `sys_dict_type` VALUES (108, '风险评估状态', 'agri_evaluate_status', '0', 'admin', '2026-04-15 10:59:31', '', NULL, '农业金融风控评估状态');
INSERT INTO `sys_dict_type` VALUES (109, '供应链合约状态', 'agri_contract_status', '0', 'admin', '2026-04-15 11:03:17', '', NULL, '供应链金融合约状态');
INSERT INTO `sys_dict_type` VALUES (110, '第三方API类型', 'agri_api_type', '0', 'admin', '2026-04-15 11:07:04', '', NULL, '第三方API接入类型');
INSERT INTO `sys_dict_type` VALUES (111, 'API调用状态', 'agri_api_call_status', '0', 'admin', '2026-04-15 11:07:04', '', NULL, '第三方API调用状态');

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务调度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO `sys_job` VALUES (1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_job` VALUES (2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_job` VALUES (3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2026-04-14 21:16:22', '', NULL, '');

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '日志信息',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '异常信息',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '执行开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '执行结束时间',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '定时任务调度日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '提示消息',
  `login_time` datetime(0) NULL DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_logininfor_s`(`status`) USING BTREE,
  INDEX `idx_sys_logininfor_lt`(`login_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统访问记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
INSERT INTO `sys_logininfor` VALUES (100, 'admin', '127.0.0.1', '内网IP', 'Code 1.115.0', 'Windows 10.0', '0', '登录成功', '2026-04-15 11:45:24');
INSERT INTO `sys_logininfor` VALUES (101, 'admin', '127.0.0.1', '内网IP', 'Chrome 146', 'Windows10', '0', '登录成功', '2026-04-15 11:45:49');
INSERT INTO `sys_logininfor` VALUES (102, 'admin', '127.0.0.1', '内网IP', 'Chrome 146', 'Windows10', '0', '退出成功', '2026-04-15 12:08:44');
INSERT INTO `sys_logininfor` VALUES (103, 'admin', '127.0.0.1', '内网IP', 'Chrome 146', 'Windows10', '0', '登录成功', '2026-04-15 12:08:47');
INSERT INTO `sys_logininfor` VALUES (104, 'admin', '127.0.0.1', '内网IP', 'Chrome 146', 'Windows10', '0', '登录成功', '2026-04-15 12:23:07');
INSERT INTO `sys_logininfor` VALUES (105, 'admin', '127.0.0.1', '内网IP', 'Chrome 146', 'Windows10', '0', '登录成功', '2026-04-15 12:32:12');
INSERT INTO `sys_logininfor` VALUES (106, 'admin', '127.0.0.1', '内网IP', 'Chrome 146', 'Windows10', '0', '登录成功', '2026-04-15 12:37:08');
INSERT INTO `sys_logininfor` VALUES (107, 'admin', '127.0.0.1', '内网IP', 'Chrome 146', 'Windows10', '0', '登录成功', '2026-04-15 12:45:18');
INSERT INTO `sys_logininfor` VALUES (108, 'admin', '127.0.0.1', '内网IP', 'Chrome 146', 'Windows10', '0', '退出成功', '2026-04-15 12:49:25');
INSERT INTO `sys_logininfor` VALUES (109, 'admin', '127.0.0.1', '内网IP', 'Chrome 146', 'Windows10', '0', '登录成功', '2026-04-15 12:50:03');
INSERT INTO `sys_logininfor` VALUES (110, 'admin', '127.0.0.1', '内网IP', 'Chrome 146', 'Windows10', '0', '登录成功', '2026-04-15 13:39:01');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '路由名称',
  `is_frame` int NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2007 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, 'system', NULL, '', '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2026-04-14 21:16:22', '', NULL, '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, '系统监控', 0, 2, 'monitor', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2026-04-14 21:16:22', '', NULL, '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, '系统工具', 0, 3, 'tool', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2026-04-14 21:16:22', '', NULL, '系统工具目录');
INSERT INTO `sys_menu` VALUES (4, '若依官网', 0, 4, 'http://ruoyi.vip', NULL, '', '', 0, 0, 'M', '1', '0', '', 'guide', 'admin', '2026-04-14 21:16:22', 'admin', '2026-04-15 13:40:02', '若依官网地址');
INSERT INTO `sys_menu` VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2026-04-14 21:16:22', '', NULL, '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2026-04-14 21:16:22', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2026-04-14 21:16:22', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2026-04-14 21:16:22', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2026-04-14 21:16:22', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2026-04-14 21:16:22', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2026-04-14 21:16:22', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2026-04-14 21:16:22', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` VALUES (108, '日志管理', 1, 9, 'log', '', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2026-04-14 21:16:22', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2026-04-14 21:16:22', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2026-04-14 21:16:22', '', NULL, '定时任务菜单');
INSERT INTO `sys_menu` VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid', 'admin', '2026-04-14 21:16:22', '', NULL, '数据监控菜单');
INSERT INTO `sys_menu` VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', '', 1, 0, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2026-04-14 21:16:22', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis', 'admin', '2026-04-14 21:16:22', '', NULL, '缓存监控菜单');
INSERT INTO `sys_menu` VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis-list', 'admin', '2026-04-14 21:16:22', '', NULL, '缓存列表菜单');
INSERT INTO `sys_menu` VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2026-04-14 21:16:22', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2026-04-14 21:16:22', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2026-04-14 21:16:22', '', NULL, '系统接口菜单');
INSERT INTO `sys_menu` VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2026-04-14 21:16:22', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', '2026-04-14 21:16:22', '', NULL, '登录日志菜单');
INSERT INTO `sys_menu` VALUES (1000, '用户查询', 100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1001, '用户新增', 100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1002, '用户修改', 100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1003, '用户删除', 100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1004, '用户导出', 100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1005, '用户导入', 100, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1006, '重置密码', 100, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1007, '角色查询', 101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1008, '角色新增', 101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1009, '角色修改', 101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1010, '角色删除', 101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1011, '角色导出', 101, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1012, '菜单查询', 102, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1013, '菜单新增', 102, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1014, '菜单修改', 102, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1015, '菜单删除', 102, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1016, '部门查询', 103, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1017, '部门新增', 103, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1018, '部门修改', 103, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1019, '部门删除', 103, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1020, '岗位查询', 104, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1021, '岗位新增', 104, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1022, '岗位修改', 104, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1023, '岗位删除', 104, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1024, '岗位导出', 104, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1025, '字典查询', 105, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1026, '字典新增', 105, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1027, '字典修改', 105, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1028, '字典删除', 105, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1029, '字典导出', 105, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1030, '参数查询', 106, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1031, '参数新增', 106, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1032, '参数修改', 106, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1033, '参数删除', 106, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1034, '参数导出', 106, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1035, '公告查询', 107, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1036, '公告新增', 107, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1037, '公告修改', 107, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1038, '公告删除', 107, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1039, '操作查询', 500, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1040, '操作删除', 500, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1041, '日志导出', 500, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1042, '登录查询', 501, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1043, '登录删除', 501, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1044, '日志导出', 501, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1045, '账户解锁', 501, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1046, '在线查询', 109, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1047, '批量强退', 109, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1048, '单条强退', 109, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1049, '任务查询', 110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1050, '任务新增', 110, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1051, '任务修改', 110, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1052, '任务删除', 110, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1053, '状态修改', 110, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1054, '任务导出', 110, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1055, '生成查询', 116, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1056, '生成修改', 116, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1057, '生成删除', 116, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1058, '导入代码', 116, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1059, '预览代码', 116, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (1060, '生成代码', 116, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2000, '启元农链', 0, 6, 'agri', NULL, '', '', 1, 0, 'M', '0', '0', '', 'drag', 'admin', '2026-04-14 21:16:36', 'admin', '2026-04-15 13:41:47', '启元农链目录');
INSERT INTO `sys_menu` VALUES (2001, '基础规则配置', 2000, 1, 'baseRule', 'agri/baseRule/index', '', 'AgriBaseRule', 1, 0, 'C', '0', '0', 'agri:baseRule:list', 'edit', 'admin', '2026-04-14 21:16:36', '', NULL, '基础规则配置菜单');
INSERT INTO `sys_menu` VALUES (2002, '基础规则配置查询', 2001, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:baseRule:query', '#', 'admin', '2026-04-14 21:16:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2003, '基础规则配置新增', 2001, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:baseRule:add', '#', 'admin', '2026-04-14 21:16:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2004, '基础规则配置修改', 2001, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:baseRule:edit', '#', 'admin', '2026-04-14 21:16:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2005, '基础规则配置删除', 2001, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:baseRule:remove', '#', 'admin', '2026-04-14 21:16:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2006, '基础规则配置导出', 2001, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:baseRule:export', '#', 'admin', '2026-04-14 21:16:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2007, '环境传感数据', 2000, 2, 'envSensor', 'agri/envSensor/index', '', 'AgriEnvSensor', 1, 0, 'C', '0', '0', 'agri:envSensor:list', 'monitor', 'admin', '2026-04-14 21:34:19', '', NULL, '环境传感器数据接入');
INSERT INTO `sys_menu` VALUES (2008, '环境传感数据查询', 2007, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:envSensor:query', '#', 'admin', '2026-04-14 21:34:19', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2009, '环境传感数据新增', 2007, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:envSensor:add', '#', 'admin', '2026-04-14 21:34:19', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2010, '环境传感数据修改', 2007, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:envSensor:edit', '#', 'admin', '2026-04-14 21:34:19', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2011, '环境传感数据删除', 2007, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:envSensor:remove', '#', 'admin', '2026-04-14 21:34:19', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2012, '环境传感数据导出', 2007, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:envSensor:export', '#', 'admin', '2026-04-14 21:34:19', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2013, '农事记录管理', 2000, 3, 'farmOp', 'agri/farmOp/index', '', 'AgriFarmOperation', 1, 0, 'C', '0', '0', 'agri:farmOp:list', 'form', 'admin', '2026-04-14 21:41:07', '', NULL, '农事记录管理');
INSERT INTO `sys_menu` VALUES (2014, '农事记录查询', 2013, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:farmOp:query', '#', 'admin', '2026-04-14 21:41:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2015, '农事记录新增', 2013, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:farmOp:add', '#', 'admin', '2026-04-14 21:41:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2016, '农事记录修改', 2013, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:farmOp:edit', '#', 'admin', '2026-04-14 21:41:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2017, '农事记录删除', 2013, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:farmOp:remove', '#', 'admin', '2026-04-14 21:41:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2018, '农事记录导出', 2013, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:farmOp:export', '#', 'admin', '2026-04-14 21:41:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2019, '病虫害识别', 2000, 4, 'pestIdentify', 'agri/pestIdentify/index', '', 'AgriPestIdentify', 1, 0, 'C', '0', '0', 'agri:pestIdentify:list', 'bug', 'admin', '2026-04-14 21:48:06', '', NULL, '病虫害识别模型接入');
INSERT INTO `sys_menu` VALUES (2020, '病虫害识别查询', 2019, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:pestIdentify:query', '#', 'admin', '2026-04-14 21:48:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2021, '病虫害识别新增', 2019, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:pestIdentify:add', '#', 'admin', '2026-04-14 21:48:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2022, '病虫害识别修改', 2019, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:pestIdentify:edit', '#', 'admin', '2026-04-14 21:48:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2023, '病虫害识别删除', 2019, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:pestIdentify:remove', '#', 'admin', '2026-04-14 21:48:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2024, '病虫害识别导出', 2019, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:pestIdentify:export', '#', 'admin', '2026-04-14 21:48:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2025, '产量预测', 2000, 5, 'yieldForecast', 'agri/yieldForecast/index', '', 'AgriYieldForecast', 1, 0, 'C', '0', '0', 'agri:yieldForecast:list', 'chart', 'admin', '2026-04-14 21:52:48', '', NULL, '产量预测模型接入');
INSERT INTO `sys_menu` VALUES (2026, '产量预测查询', 2025, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:yieldForecast:query', '#', 'admin', '2026-04-14 21:52:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2027, '产量预测新增', 2025, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:yieldForecast:add', '#', 'admin', '2026-04-14 21:52:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2028, '产量预测修改', 2025, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:yieldForecast:edit', '#', 'admin', '2026-04-14 21:52:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2029, '产量预测删除', 2025, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:yieldForecast:remove', '#', 'admin', '2026-04-14 21:52:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2030, '产量预测导出', 2025, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:yieldForecast:export', '#', 'admin', '2026-04-14 21:52:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2031, '批次关联管理', 2000, 6, 'processBatch', 'agri/processBatch/index', '', 'AgriProcessBatch', 1, 0, 'C', '0', '0', 'agri:processBatch:list', 'link', 'admin', '2026-04-14 21:59:12', '', NULL, '批次关联管理');
INSERT INTO `sys_menu` VALUES (2032, '批次关联查询', 2031, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:processBatch:query', '#', 'admin', '2026-04-14 21:59:12', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2033, '批次关联新增', 2031, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:processBatch:add', '#', 'admin', '2026-04-14 21:59:12', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2034, '批次关联修改', 2031, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:processBatch:edit', '#', 'admin', '2026-04-14 21:59:12', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2035, '批次关联删除', 2031, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:processBatch:remove', '#', 'admin', '2026-04-14 21:59:12', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2036, '批次关联导出', 2031, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:processBatch:export', '#', 'admin', '2026-04-14 21:59:12', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2037, '视觉品质检测', 2000, 7, 'qualityInspect', 'agri/qualityInspect/index', '', 'AgriQualityInspect', 1, 0, 'C', '0', '0', 'agri:qualityInspect:list', 'camera', 'admin', '2026-04-14 22:03:04', '', NULL, '视觉品质检测');
INSERT INTO `sys_menu` VALUES (2038, '视觉品质检测查询', 2037, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityInspect:query', '#', 'admin', '2026-04-14 22:03:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2039, '视觉品质检测新增', 2037, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityInspect:add', '#', 'admin', '2026-04-14 22:03:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2040, '视觉品质检测修改', 2037, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityInspect:edit', '#', 'admin', '2026-04-14 22:03:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2041, '视觉品质检测删除', 2037, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityInspect:remove', '#', 'admin', '2026-04-14 22:03:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2042, '视觉品质检测导出', 2037, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityInspect:export', '#', 'admin', '2026-04-14 22:03:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2043, '质检报告', 2000, 8, 'qualityReport', 'agri/qualityReport/index', '', 'AgriQualityReport', 1, 0, 'C', '0', '0', 'agri:qualityReport:list', 'documentation', 'admin', '2026-04-15 07:51:27', '', NULL, '质检报告生成');
INSERT INTO `sys_menu` VALUES (2044, '质检报告查询', 2043, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityReport:query', '#', 'admin', '2026-04-15 07:51:27', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2045, '质检报告新增', 2043, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityReport:add', '#', 'admin', '2026-04-15 07:51:27', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2046, '质检报告修改', 2043, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityReport:edit', '#', 'admin', '2026-04-15 07:51:27', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2047, '质检报告删除', 2043, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityReport:remove', '#', 'admin', '2026-04-15 07:51:27', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2048, '质检报告导出', 2043, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityReport:export', '#', 'admin', '2026-04-15 07:51:27', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2049, '物流路径追踪', 2000, 9, 'logisticsTrack', 'agri/logisticsTrack/index', '', 'AgriLogisticsTrack', 1, 0, 'C', '0', '0', 'agri:logisticsTrack:list', 'guide', 'admin', '2026-04-15 09:44:18', '', NULL, '物流路径追踪');
INSERT INTO `sys_menu` VALUES (2050, '物流路径追踪查询', 2049, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTrack:query', '#', 'admin', '2026-04-15 09:44:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2051, '物流路径追踪新增', 2049, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTrack:add', '#', 'admin', '2026-04-15 09:44:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2052, '物流路径追踪修改', 2049, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTrack:edit', '#', 'admin', '2026-04-15 09:44:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2053, '物流路径追踪删除', 2049, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTrack:remove', '#', 'admin', '2026-04-15 09:44:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2054, '物流路径追踪导出', 2049, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTrack:export', '#', 'admin', '2026-04-15 09:44:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2055, '温湿度监控', 2000, 10, 'logisticsTemp', 'agri/logisticsTemp/index', '', 'AgriLogisticsTemp', 1, 0, 'C', '0', '0', 'agri:logisticsTemp:list', 'dashboard', 'admin', '2026-04-15 09:51:57', '', NULL, '物流温湿度监控');
INSERT INTO `sys_menu` VALUES (2056, '温湿度监控查询', 2055, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTemp:query', '#', 'admin', '2026-04-15 09:51:57', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2057, '温湿度监控新增', 2055, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTemp:add', '#', 'admin', '2026-04-15 09:51:57', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2058, '温湿度监控修改', 2055, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTemp:edit', '#', 'admin', '2026-04-15 09:51:57', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2059, '温湿度监控删除', 2055, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTemp:remove', '#', 'admin', '2026-04-15 09:51:57', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2060, '温湿度监控导出', 2055, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTemp:export', '#', 'admin', '2026-04-15 09:51:57', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2061, '在途异常预警', 2000, 11, 'logisticsWarning', 'agri/logisticsWarning/index', '', 'AgriLogisticsWarning', 1, 0, 'C', '0', '0', 'agri:logisticsWarning:list', 'warning', 'admin', '2026-04-15 09:56:31', '', NULL, '在途异常预警');
INSERT INTO `sys_menu` VALUES (2062, '在途异常预警查询', 2061, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsWarning:query', '#', 'admin', '2026-04-15 09:56:31', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2063, '在途异常预警新增', 2061, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsWarning:add', '#', 'admin', '2026-04-15 09:56:31', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2064, '在途异常预警修改', 2061, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsWarning:edit', '#', 'admin', '2026-04-15 09:56:31', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2065, '在途异常预警删除', 2061, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsWarning:remove', '#', 'admin', '2026-04-15 09:56:31', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2066, '在途异常预警导出', 2061, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsWarning:export', '#', 'admin', '2026-04-15 09:56:31', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2067, '市场预测分析', 2000, 12, 'marketForecast', 'agri/marketForecast/index', '', 'AgriMarketForecast', 1, 0, 'C', '0', '0', 'agri:marketForecast:list', 'trend-charts', 'admin', '2026-04-15 10:00:38', '', NULL, '市场预测分析');
INSERT INTO `sys_menu` VALUES (2068, '市场预测分析查询', 2067, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:marketForecast:query', '#', 'admin', '2026-04-15 10:00:38', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2069, '市场预测分析新增', 2067, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:marketForecast:add', '#', 'admin', '2026-04-15 10:00:38', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2070, '市场预测分析修改', 2067, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:marketForecast:edit', '#', 'admin', '2026-04-15 10:00:38', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2071, '市场预测分析删除', 2067, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:marketForecast:remove', '#', 'admin', '2026-04-15 10:00:38', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2072, '市场预测分析导出', 2067, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:marketForecast:export', '#', 'admin', '2026-04-15 10:00:38', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2073, '品牌溯源页面', 2000, 13, 'brandTrace', 'agri/brandTrace/index', '', 'AgriBrandTrace', 1, 0, 'C', '0', '0', 'agri:brandTrace:list', 'example', 'admin', '2026-04-15 10:07:18', '', NULL, '品牌溯源页面');
INSERT INTO `sys_menu` VALUES (2074, '品牌溯源页面查询', 2073, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:brandTrace:query', '#', 'admin', '2026-04-15 10:07:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2075, '品牌溯源页面新增', 2073, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:brandTrace:add', '#', 'admin', '2026-04-15 10:07:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2076, '品牌溯源页面修改', 2073, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:brandTrace:edit', '#', 'admin', '2026-04-15 10:07:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2077, '品牌溯源页面删除', 2073, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:brandTrace:remove', '#', 'admin', '2026-04-15 10:07:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2078, '品牌溯源页面导出', 2073, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:brandTrace:export', '#', 'admin', '2026-04-15 10:07:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2079, '消费者扫码查询', 2000, 14, 'consumerScan', 'agri/consumerScan/index', '', 'AgriConsumerScan', 1, 0, 'C', '0', '0', 'agri:consumerScan:list', 'search', 'admin', '2026-04-15 10:12:34', '', NULL, '消费者扫码查询');
INSERT INTO `sys_menu` VALUES (2080, '消费者扫码查询查询', 2079, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:consumerScan:query', '#', 'admin', '2026-04-15 10:12:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2081, '消费者扫码查询新增', 2079, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:consumerScan:add', '#', 'admin', '2026-04-15 10:12:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2082, '消费者扫码查询修改', 2079, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:consumerScan:edit', '#', 'admin', '2026-04-15 10:12:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2083, '消费者扫码查询删除', 2079, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:consumerScan:remove', '#', 'admin', '2026-04-15 10:12:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2084, '消费者扫码查询导出', 2079, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:consumerScan:export', '#', 'admin', '2026-04-15 10:12:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2085, '用户权限管理', 2000, 15, 'userAccess', 'agri/userAccess/index', '', 'AgriUserAccess', 1, 0, 'C', '0', '0', 'agri:userAccess:list', 'peoples', 'admin', '2026-04-15 10:16:07', '', NULL, '用户权限管理');
INSERT INTO `sys_menu` VALUES (2086, '用户权限管理查询', 2085, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:userAccess:query', '#', 'admin', '2026-04-15 10:16:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2087, '用户权限管理新增', 2085, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:userAccess:add', '#', 'admin', '2026-04-15 10:16:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2088, '用户权限管理修改', 2085, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:userAccess:edit', '#', 'admin', '2026-04-15 10:16:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2089, '用户权限管理删除', 2085, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:userAccess:remove', '#', 'admin', '2026-04-15 10:16:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2090, '用户权限管理导出', 2085, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:userAccess:export', '#', 'admin', '2026-04-15 10:16:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2091, '设备接入管理', 2000, 16, 'deviceAccess', 'agri/deviceAccess/index', '', 'AgriDeviceAccess', 1, 0, 'C', '0', '0', 'agri:deviceAccess:list', 'monitor', 'admin', '2026-04-15 10:19:42', '', NULL, '设备接入管理');
INSERT INTO `sys_menu` VALUES (2092, '设备接入管理查询', 2091, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:deviceAccess:query', '#', 'admin', '2026-04-15 10:19:42', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2093, '设备接入管理新增', 2091, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:deviceAccess:add', '#', 'admin', '2026-04-15 10:19:42', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2094, '设备接入管理修改', 2091, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:deviceAccess:edit', '#', 'admin', '2026-04-15 10:19:42', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2095, '设备接入管理删除', 2091, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:deviceAccess:remove', '#', 'admin', '2026-04-15 10:19:42', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2096, '设备接入管理导出', 2091, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:deviceAccess:export', '#', 'admin', '2026-04-15 10:19:42', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2097, '数据上链接口', 2000, 17, 'dataUplink', 'agri/dataUplink/index', '', 'AgriDataUplink', 1, 0, 'C', '0', '0', 'agri:dataUplink:list', 'guide', 'admin', '2026-04-15 10:23:28', '', NULL, '数据上链接口');
INSERT INTO `sys_menu` VALUES (2098, '数据上链接口查询', 2097, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:dataUplink:query', '#', 'admin', '2026-04-15 10:23:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2099, '数据上链接口新增', 2097, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:dataUplink:add', '#', 'admin', '2026-04-15 10:23:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2100, '数据上链接口修改', 2097, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:dataUplink:edit', '#', 'admin', '2026-04-15 10:23:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2101, '数据上链接口删除', 2097, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:dataUplink:remove', '#', 'admin', '2026-04-15 10:23:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2102, '数据上链接口导出', 2097, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:dataUplink:export', '#', 'admin', '2026-04-15 10:23:28', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2103, '智能合约部署', 2000, 18, 'smartContract', 'agri/smartContract/index', '', 'AgriSmartContract', 1, 0, 'C', '0', '0', 'agri:smartContract:list', 'cpu', 'admin', '2026-04-15 10:27:14', '', NULL, '智能合约部署');
INSERT INTO `sys_menu` VALUES (2104, '智能合约部署查询', 2103, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:smartContract:query', '#', 'admin', '2026-04-15 10:27:14', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2105, '智能合约部署新增', 2103, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:smartContract:add', '#', 'admin', '2026-04-15 10:27:14', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2106, '智能合约部署修改', 2103, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:smartContract:edit', '#', 'admin', '2026-04-15 10:27:14', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2107, '智能合约部署删除', 2103, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:smartContract:remove', '#', 'admin', '2026-04-15 10:27:14', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2108, '智能合约部署导出', 2103, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:smartContract:export', '#', 'admin', '2026-04-15 10:27:14', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2109, '数据存证与校验', NULL, 26, 'attestationVerify', 'agri/attestationVerify/index', '', 'AttestationVerify', 1, 0, 'C', '0', '0', 'agri:attestationVerify:list', 'documentation', 'admin', '2026-04-15 10:31:16', '', NULL, '数据存证与校验菜单');
INSERT INTO `sys_menu` VALUES (2110, '数据存证与校验查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:attestationVerify:query', '#', 'admin', '2026-04-15 10:31:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2111, '数据存证与校验新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:attestationVerify:add', '#', 'admin', '2026-04-15 10:31:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2112, '数据存证与校验修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:attestationVerify:edit', '#', 'admin', '2026-04-15 10:31:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2113, '数据存证与校验删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:attestationVerify:remove', '#', 'admin', '2026-04-15 10:31:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2114, '数据存证与校验导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:attestationVerify:export', '#', 'admin', '2026-04-15 10:31:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2121, '溯源审计日志', NULL, 27, 'auditLog', 'agri/auditLog/index', '', 'AuditLog', 1, 0, 'C', '0', '0', 'agri:auditLog:list', 'clipboard', 'admin', '2026-04-15 10:51:53', '', NULL, '溯源审计日志菜单');
INSERT INTO `sys_menu` VALUES (2122, '溯源审计日志查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:auditLog:query', '#', 'admin', '2026-04-15 10:51:53', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2123, '溯源审计日志新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:auditLog:add', '#', 'admin', '2026-04-15 10:51:53', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2124, '溯源审计日志修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:auditLog:edit', '#', 'admin', '2026-04-15 10:51:53', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2125, '溯源审计日志删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:auditLog:remove', '#', 'admin', '2026-04-15 10:51:53', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2126, '溯源审计日志导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:auditLog:export', '#', 'admin', '2026-04-15 10:51:53', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2127, '碳足迹核算模型', NULL, 28, 'carbonFootprint', 'agri/carbonFootprint/index', '', 'CarbonFootprint', 1, 0, 'C', '0', '0', 'agri:carbonFootprint:list', 'guide', 'admin', '2026-04-15 10:55:45', '', NULL, '碳足迹核算模型菜单');
INSERT INTO `sys_menu` VALUES (2128, '碳足迹核算模型查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:carbonFootprint:query', '#', 'admin', '2026-04-15 10:55:45', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2129, '碳足迹核算模型新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:carbonFootprint:add', '#', 'admin', '2026-04-15 10:55:45', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2130, '碳足迹核算模型修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:carbonFootprint:edit', '#', 'admin', '2026-04-15 10:55:45', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2131, '碳足迹核算模型删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:carbonFootprint:remove', '#', 'admin', '2026-04-15 10:55:45', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2132, '碳足迹核算模型导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:carbonFootprint:export', '#', 'admin', '2026-04-15 10:55:45', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2133, '农业金融风控指标', NULL, 29, 'financeRisk', 'agri/financeRisk/index', '', 'FinanceRisk', 1, 0, 'C', '0', '0', 'agri:financeRisk:list', 'money', 'admin', '2026-04-15 10:59:31', '', NULL, '农业金融风控指标菜单');
INSERT INTO `sys_menu` VALUES (2134, '农业金融风控指标查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:financeRisk:query', '#', 'admin', '2026-04-15 10:59:31', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2135, '农业金融风控指标新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:financeRisk:add', '#', 'admin', '2026-04-15 10:59:31', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2136, '农业金融风控指标修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:financeRisk:edit', '#', 'admin', '2026-04-15 10:59:31', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2137, '农业金融风控指标删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:financeRisk:remove', '#', 'admin', '2026-04-15 10:59:31', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2138, '农业金融风控指标导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:financeRisk:export', '#', 'admin', '2026-04-15 10:59:31', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2139, '供应链金融合约管理', NULL, 30, 'supplyContract', 'agri/supplyContract/index', '', 'SupplyContract', 1, 0, 'C', '0', '0', 'agri:supplyContract:list', 'documentation', 'admin', '2026-04-15 11:03:17', '', NULL, '供应链金融合约管理菜单');
INSERT INTO `sys_menu` VALUES (2140, '供应链金融合约查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:supplyContract:query', '#', 'admin', '2026-04-15 11:03:17', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2141, '供应链金融合约新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:supplyContract:add', '#', 'admin', '2026-04-15 11:03:17', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2142, '供应链金融合约修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:supplyContract:edit', '#', 'admin', '2026-04-15 11:03:17', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2143, '供应链金融合约删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:supplyContract:remove', '#', 'admin', '2026-04-15 11:03:17', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2144, '供应链金融合约导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:supplyContract:export', '#', 'admin', '2026-04-15 11:03:17', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2145, '第三方API接入', NULL, 31, 'thirdApi', 'agri/thirdApi/index', '', 'ThirdApi', 1, 0, 'C', '0', '0', 'agri:thirdApi:list', 'link', 'admin', '2026-04-15 11:07:04', '', NULL, '第三方API接入菜单');
INSERT INTO `sys_menu` VALUES (2146, '第三方API接入查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:thirdApi:query', '#', 'admin', '2026-04-15 11:07:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2147, '第三方API接入新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:thirdApi:add', '#', 'admin', '2026-04-15 11:07:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2148, '第三方API接入修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:thirdApi:edit', '#', 'admin', '2026-04-15 11:07:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2149, '第三方API接入删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:thirdApi:remove', '#', 'admin', '2026-04-15 11:07:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2150, '第三方API接入导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:thirdApi:export', '#', 'admin', '2026-04-15 11:07:04', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2151, '数据总览看板', NULL, 32, 'dashboardOverview', 'agri/dashboardOverview/index', '', 'DashboardOverview', 1, 0, 'C', '0', '0', 'agri:dashboardOverview:list', 'dashboard', 'admin', '2026-04-15 11:11:42', '', NULL, '数据总览看板菜单');
INSERT INTO `sys_menu` VALUES (2152, '数据总览看板查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:dashboardOverview:query', '#', 'admin', '2026-04-15 11:11:42', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2153, '数据总览看板新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:dashboardOverview:add', '#', 'admin', '2026-04-15 11:11:42', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2154, '数据总览看板修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:dashboardOverview:edit', '#', 'admin', '2026-04-15 11:11:42', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2155, '数据总览看板删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:dashboardOverview:remove', '#', 'admin', '2026-04-15 11:11:42', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2156, '数据总览看板导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:dashboardOverview:export', '#', 'admin', '2026-04-15 11:11:42', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2157, '设备状态监控', NULL, 33, 'deviceStatusMonitor', 'agri/deviceStatusMonitor/index', '', 'DeviceStatusMonitor', 1, 0, 'C', '0', '0', 'agri:deviceStatusMonitor:list', 'monitor', 'admin', '2026-04-15 11:15:24', '', NULL, '设备状态监控菜单');
INSERT INTO `sys_menu` VALUES (2158, '设备状态监控查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:deviceStatusMonitor:query', '#', 'admin', '2026-04-15 11:15:24', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2159, '设备状态监控新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:deviceStatusMonitor:add', '#', 'admin', '2026-04-15 11:15:24', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2160, '设备状态监控修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:deviceStatusMonitor:edit', '#', 'admin', '2026-04-15 11:15:24', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2161, '设备状态监控删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:deviceStatusMonitor:remove', '#', 'admin', '2026-04-15 11:15:24', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2162, '设备状态监控导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:deviceStatusMonitor:export', '#', 'admin', '2026-04-15 11:15:24', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2163, '预警信息汇总', NULL, 34, 'warningSummary', 'agri/warningSummary/index', '', 'WarningSummary', 1, 0, 'C', '0', '0', 'agri:warningSummary:list', 'build', 'admin', '2026-04-15 11:18:30', 'admin', '2026-04-15 13:40:57', '预警信息汇总菜单');
INSERT INTO `sys_menu` VALUES (2164, '预警信息汇总查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:warningSummary:query', '#', 'admin', '2026-04-15 11:18:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2165, '预警信息汇总新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:warningSummary:add', '#', 'admin', '2026-04-15 11:18:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2166, '预警信息汇总修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:warningSummary:edit', '#', 'admin', '2026-04-15 11:18:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2167, '预警信息汇总删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:warningSummary:remove', '#', 'admin', '2026-04-15 11:18:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2168, '预警信息汇总导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:warningSummary:export', '#', 'admin', '2026-04-15 11:18:30', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2169, '产量与销量趋势图', NULL, 35, 'outputSalesTrend', 'agri/outputSalesTrend/index', '', 'OutputSalesTrend', 1, 0, 'C', '0', '0', 'agri:outputSalesTrend:list', 'chart', 'admin', '2026-04-15 11:21:40', 'admin', '2026-04-15 13:41:11', '产量与销量趋势图菜单');
INSERT INTO `sys_menu` VALUES (2170, '产量与销量趋势图查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:outputSalesTrend:query', '#', 'admin', '2026-04-15 11:21:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2171, '产量与销量趋势图新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:outputSalesTrend:add', '#', 'admin', '2026-04-15 11:21:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2172, '产量与销量趋势图修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:outputSalesTrend:edit', '#', 'admin', '2026-04-15 11:21:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2173, '产量与销量趋势图删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:outputSalesTrend:remove', '#', 'admin', '2026-04-15 11:21:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2174, '产量与销量趋势图导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:outputSalesTrend:export', '#', 'admin', '2026-04-15 11:21:40', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2175, '溯源查询统计', NULL, 36, 'traceQueryStats', 'agri/traceQueryStats/index', '', 'TraceQueryStats', 1, 0, 'C', '0', '0', 'agri:traceQueryStats:list', 'search', 'admin', '2026-04-15 11:25:00', '', NULL, '溯源查询统计菜单');
INSERT INTO `sys_menu` VALUES (2176, '溯源查询统计查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:traceQueryStats:query', '#', 'admin', '2026-04-15 11:25:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2177, '溯源查询统计新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:traceQueryStats:add', '#', 'admin', '2026-04-15 11:25:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2178, '溯源查询统计修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:traceQueryStats:edit', '#', 'admin', '2026-04-15 11:25:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2179, '溯源查询统计删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:traceQueryStats:remove', '#', 'admin', '2026-04-15 11:25:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2180, '溯源查询统计导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:traceQueryStats:export', '#', 'admin', '2026-04-15 11:25:00', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2181, '任务待办提醒', NULL, 37, 'todoTaskReminder', 'agri/todoTaskReminder/index', '', 'TodoTaskReminder', 1, 0, 'C', '0', '0', 'agri:todoTaskReminder:list', 'message', 'admin', '2026-04-15 11:28:06', '', NULL, '任务待办提醒菜单');
INSERT INTO `sys_menu` VALUES (2182, '任务待办提醒查询', NULL, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:todoTaskReminder:query', '#', 'admin', '2026-04-15 11:28:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2183, '任务待办提醒新增', NULL, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:todoTaskReminder:add', '#', 'admin', '2026-04-15 11:28:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2184, '任务待办提醒修改', NULL, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:todoTaskReminder:edit', '#', 'admin', '2026-04-15 11:28:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2185, '任务待办提醒删除', NULL, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:todoTaskReminder:remove', '#', 'admin', '2026-04-15 11:28:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2186, '任务待办提醒导出', NULL, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'agri:todoTaskReminder:export', '#', 'admin', '2026-04-15 11:28:06', '', NULL, '');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin', '2026-04-14 21:16:22', '', NULL, '管理员');
INSERT INTO `sys_notice` VALUES (2, '维护通知：2018-07-01 若依系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2026-04-14 21:16:22', '', NULL, '管理员');
INSERT INTO `sys_notice` VALUES (3, '若依开源框架介绍', '1', 0x3C703E3C7370616E207374796C653D22636F6C6F723A20726762283233302C20302C2030293B223EE9A1B9E79BAEE4BB8BE7BB8D3C2F7370616E3E3C2F703E3C703E3C666F6E7420636F6C6F723D2223333333333333223E52756F5969E5BC80E6BA90E9A1B9E79BAEE698AFE4B8BAE4BC81E4B89AE794A8E688B7E5AE9AE588B6E79A84E5908EE58FB0E8849AE6898BE69EB6E6A186E69EB6EFBC8CE4B8BAE4BC81E4B89AE68993E980A0E79A84E4B880E7AB99E5BC8FE8A7A3E586B3E696B9E6A188EFBC8CE9998DE4BD8EE4BC81E4B89AE5BC80E58F91E68890E69CACEFBC8CE68F90E58D87E5BC80E58F91E69588E78E87E38082E4B8BBE8A681E58C85E68BACE794A8E688B7E7AEA1E79086E38081E8A792E889B2E7AEA1E79086E38081E983A8E997A8E7AEA1E79086E38081E88F9CE58D95E7AEA1E79086E38081E58F82E695B0E7AEA1E79086E38081E5AD97E585B8E7AEA1E79086E380813C2F666F6E743E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE5B297E4BD8DE7AEA1E790863C2F7370616E3E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE38081E5AE9AE697B6E4BBBBE58AA13C2F7370616E3E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE380813C2F7370616E3E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE69C8DE58AA1E79B91E68EA7E38081E799BBE5BD95E697A5E5BF97E38081E6938DE4BD9CE697A5E5BF97E38081E4BBA3E7A081E7949FE68890E7AD89E58A9FE883BDE38082E585B6E4B8ADEFBC8CE8BF98E694AFE68C81E5A49AE695B0E68DAEE6BA90E38081E695B0E68DAEE69D83E99990E38081E59BBDE99985E58C96E380815265646973E7BC93E5AD98E38081446F636B6572E983A8E7BDB2E38081E6BB91E58AA8E9AA8CE8AF81E7A081E38081E7ACACE4B889E696B9E8AEA4E8AF81E799BBE5BD95E38081E58886E5B883E5BC8FE4BA8BE58AA1E380813C2F7370616E3E3C666F6E7420636F6C6F723D2223333333333333223EE58886E5B883E5BC8FE69687E4BBB6E5AD98E582A83C2F666F6E743E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE38081E58886E5BA93E58886E8A1A8E5A484E79086E7AD89E68A80E69CAFE789B9E782B9E380823C2F7370616E3E3C2F703E3C703E3C696D67207372633D2268747470733A2F2F666F727564612E67697465652E636F6D2F696D616765732F313737333933313834383334323433393033322F61346432323331335F313831353039352E706E6722207374796C653D2277696474683A20363470783B223E3C62723E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A20726762283233302C20302C2030293B223EE5AE98E7BD91E58F8AE6BC94E7A4BA3C2F7370616E3E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE88BA5E4BE9DE5AE98E7BD91E59CB0E59D80EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F72756F79692E7669703C2F613E3C6120687265663D22687474703A2F2F72756F79692E76697022207461726765743D225F626C616E6B223E3C2F613E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE88BA5E4BE9DE69687E6A1A3E59CB0E59D80EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F646F632E72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F646F632E72756F79692E7669703C2F613E3C62723E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE6BC94E7A4BAE59CB0E59D80E38090E4B88DE58886E7A6BBE78988E38091EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F64656D6F2E72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F64656D6F2E72756F79692E7669703C2F613E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE6BC94E7A4BAE59CB0E59D80E38090E58886E7A6BBE78988E69CACE38091EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F7675652E72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F7675652E72756F79692E7669703C2F613E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE6BC94E7A4BAE59CB0E59D80E38090E5BEAEE69C8DE58AA1E78988E38091EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F636C6F75642E72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F636C6F75642E72756F79692E7669703C2F613E3C2F703E3C703E3C7370616E207374796C653D22636F6C6F723A207267622835312C2035312C203531293B223EE6BC94E7A4BAE59CB0E59D80E38090E7A7BBE58AA8E7ABAFE78988E38091EFBC9A266E6273703B3C2F7370616E3E3C6120687265663D22687474703A2F2F68352E72756F79692E76697022207461726765743D225F626C616E6B223E687474703A2F2F68352E72756F79692E7669703C2F613E3C2F703E3C703E3C6272207374796C653D22636F6C6F723A207267622834382C2034392C203531293B20666F6E742D66616D696C793A202671756F743B48656C766574696361204E6575652671756F743B2C2048656C7665746963612C20417269616C2C2073616E732D73657269663B20666F6E742D73697A653A20313270783B223E3C2F703E, '0', 'admin', '2026-04-14 21:16:22', '', NULL, '管理员');

-- ----------------------------
-- Table structure for sys_notice_read
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice_read`;
CREATE TABLE `sys_notice_read`  (
  `read_id` bigint NOT NULL AUTO_INCREMENT COMMENT '已读主键',
  `notice_id` int NOT NULL COMMENT '公告id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `read_time` datetime(0) NOT NULL COMMENT '阅读时间',
  PRIMARY KEY (`read_id`) USING BTREE,
  UNIQUE INDEX `uk_user_notice`(`user_id`, `notice_id`) USING BTREE COMMENT '同一用户同一公告只记录一次'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公告已读记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice_read
-- ----------------------------

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime(0) NULL DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint NULL DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type`) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status`) USING BTREE,
  INDEX `idx_sys_oper_log_ot`(`oper_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (100, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2026-04-14 21:16:22\",\"icon\":\"guide\",\"isCache\":\"0\",\"isFrame\":\"0\",\"menuId\":4,\"menuName\":\"若依官网\",\"menuType\":\"M\",\"orderNum\":4,\"params\":{},\"parentId\":0,\"path\":\"http://ruoyi.vip\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"1\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-04-15 13:40:02', 70);
INSERT INTO `sys_oper_log` VALUES (101, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"agri/warningSummary/index\",\"createTime\":\"2026-04-15 11:18:30\",\"icon\":\"build\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2163,\"menuName\":\"预警信息汇总\",\"menuType\":\"C\",\"orderNum\":34,\"params\":{},\"path\":\"warningSummary\",\"perms\":\"agri:warningSummary:list\",\"query\":\"\",\"routeName\":\"WarningSummary\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-04-15 13:40:57', 12);
INSERT INTO `sys_oper_log` VALUES (102, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"component\":\"agri/outputSalesTrend/index\",\"createTime\":\"2026-04-15 11:21:40\",\"icon\":\"chart\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2169,\"menuName\":\"产量与销量趋势图\",\"menuType\":\"C\",\"orderNum\":35,\"params\":{},\"path\":\"outputSalesTrend\",\"perms\":\"agri:outputSalesTrend:list\",\"query\":\"\",\"routeName\":\"OutputSalesTrend\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-04-15 13:41:11', 17);
INSERT INTO `sys_oper_log` VALUES (103, '菜单管理', 2, 'com.ruoyi.web.controller.system.SysMenuController.edit()', 'PUT', 1, 'admin', '研发部门', '/system/menu', '127.0.0.1', '内网IP', '{\"children\":[],\"createTime\":\"2026-04-14 21:16:36\",\"icon\":\"drag\",\"isCache\":\"0\",\"isFrame\":\"1\",\"menuId\":2000,\"menuName\":\"启元农链\",\"menuType\":\"M\",\"orderNum\":6,\"params\":{},\"parentId\":0,\"path\":\"agri\",\"perms\":\"\",\"query\":\"\",\"routeName\":\"\",\"status\":\"0\",\"updateBy\":\"admin\",\"visible\":\"0\"} ', '{\"msg\":\"操作成功\",\"code\":200}', 0, NULL, '2026-04-15 13:41:47', 15);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '岗位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_post` VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_post` VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2026-04-14 21:16:22', '', NULL, '');
INSERT INTO `sys_post` VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2026-04-14 21:16:22', '', NULL, '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '超级管理员');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '0', 'admin', '2026-04-14 21:16:22', '', NULL, '普通角色');

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和部门关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept` VALUES (2, 100);
INSERT INTO `sys_role_dept` VALUES (2, 101);
INSERT INTO `sys_role_dept` VALUES (2, 105);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 2007);
INSERT INTO `sys_role_menu` VALUES (1, 2008);
INSERT INTO `sys_role_menu` VALUES (1, 2009);
INSERT INTO `sys_role_menu` VALUES (1, 2010);
INSERT INTO `sys_role_menu` VALUES (1, 2011);
INSERT INTO `sys_role_menu` VALUES (1, 2012);
INSERT INTO `sys_role_menu` VALUES (1, 2013);
INSERT INTO `sys_role_menu` VALUES (1, 2014);
INSERT INTO `sys_role_menu` VALUES (1, 2015);
INSERT INTO `sys_role_menu` VALUES (1, 2016);
INSERT INTO `sys_role_menu` VALUES (1, 2017);
INSERT INTO `sys_role_menu` VALUES (1, 2018);
INSERT INTO `sys_role_menu` VALUES (1, 2019);
INSERT INTO `sys_role_menu` VALUES (1, 2020);
INSERT INTO `sys_role_menu` VALUES (1, 2021);
INSERT INTO `sys_role_menu` VALUES (1, 2022);
INSERT INTO `sys_role_menu` VALUES (1, 2023);
INSERT INTO `sys_role_menu` VALUES (1, 2024);
INSERT INTO `sys_role_menu` VALUES (1, 2025);
INSERT INTO `sys_role_menu` VALUES (1, 2026);
INSERT INTO `sys_role_menu` VALUES (1, 2027);
INSERT INTO `sys_role_menu` VALUES (1, 2028);
INSERT INTO `sys_role_menu` VALUES (1, 2029);
INSERT INTO `sys_role_menu` VALUES (1, 2030);
INSERT INTO `sys_role_menu` VALUES (1, 2031);
INSERT INTO `sys_role_menu` VALUES (1, 2032);
INSERT INTO `sys_role_menu` VALUES (1, 2033);
INSERT INTO `sys_role_menu` VALUES (1, 2034);
INSERT INTO `sys_role_menu` VALUES (1, 2035);
INSERT INTO `sys_role_menu` VALUES (1, 2036);
INSERT INTO `sys_role_menu` VALUES (1, 2037);
INSERT INTO `sys_role_menu` VALUES (1, 2038);
INSERT INTO `sys_role_menu` VALUES (1, 2039);
INSERT INTO `sys_role_menu` VALUES (1, 2040);
INSERT INTO `sys_role_menu` VALUES (1, 2041);
INSERT INTO `sys_role_menu` VALUES (1, 2042);
INSERT INTO `sys_role_menu` VALUES (1, 2043);
INSERT INTO `sys_role_menu` VALUES (1, 2044);
INSERT INTO `sys_role_menu` VALUES (1, 2045);
INSERT INTO `sys_role_menu` VALUES (1, 2046);
INSERT INTO `sys_role_menu` VALUES (1, 2047);
INSERT INTO `sys_role_menu` VALUES (1, 2048);
INSERT INTO `sys_role_menu` VALUES (1, 2049);
INSERT INTO `sys_role_menu` VALUES (1, 2050);
INSERT INTO `sys_role_menu` VALUES (1, 2051);
INSERT INTO `sys_role_menu` VALUES (1, 2052);
INSERT INTO `sys_role_menu` VALUES (1, 2053);
INSERT INTO `sys_role_menu` VALUES (1, 2054);
INSERT INTO `sys_role_menu` VALUES (1, 2055);
INSERT INTO `sys_role_menu` VALUES (1, 2056);
INSERT INTO `sys_role_menu` VALUES (1, 2057);
INSERT INTO `sys_role_menu` VALUES (1, 2058);
INSERT INTO `sys_role_menu` VALUES (1, 2059);
INSERT INTO `sys_role_menu` VALUES (1, 2060);
INSERT INTO `sys_role_menu` VALUES (1, 2061);
INSERT INTO `sys_role_menu` VALUES (1, 2062);
INSERT INTO `sys_role_menu` VALUES (1, 2063);
INSERT INTO `sys_role_menu` VALUES (1, 2064);
INSERT INTO `sys_role_menu` VALUES (1, 2065);
INSERT INTO `sys_role_menu` VALUES (1, 2066);
INSERT INTO `sys_role_menu` VALUES (1, 2067);
INSERT INTO `sys_role_menu` VALUES (1, 2068);
INSERT INTO `sys_role_menu` VALUES (1, 2069);
INSERT INTO `sys_role_menu` VALUES (1, 2070);
INSERT INTO `sys_role_menu` VALUES (1, 2071);
INSERT INTO `sys_role_menu` VALUES (1, 2072);
INSERT INTO `sys_role_menu` VALUES (1, 2073);
INSERT INTO `sys_role_menu` VALUES (1, 2074);
INSERT INTO `sys_role_menu` VALUES (1, 2075);
INSERT INTO `sys_role_menu` VALUES (1, 2076);
INSERT INTO `sys_role_menu` VALUES (1, 2077);
INSERT INTO `sys_role_menu` VALUES (1, 2078);
INSERT INTO `sys_role_menu` VALUES (1, 2079);
INSERT INTO `sys_role_menu` VALUES (1, 2080);
INSERT INTO `sys_role_menu` VALUES (1, 2081);
INSERT INTO `sys_role_menu` VALUES (1, 2082);
INSERT INTO `sys_role_menu` VALUES (1, 2083);
INSERT INTO `sys_role_menu` VALUES (1, 2084);
INSERT INTO `sys_role_menu` VALUES (1, 2085);
INSERT INTO `sys_role_menu` VALUES (1, 2086);
INSERT INTO `sys_role_menu` VALUES (1, 2087);
INSERT INTO `sys_role_menu` VALUES (1, 2088);
INSERT INTO `sys_role_menu` VALUES (1, 2089);
INSERT INTO `sys_role_menu` VALUES (1, 2090);
INSERT INTO `sys_role_menu` VALUES (1, 2091);
INSERT INTO `sys_role_menu` VALUES (1, 2092);
INSERT INTO `sys_role_menu` VALUES (1, 2093);
INSERT INTO `sys_role_menu` VALUES (1, 2094);
INSERT INTO `sys_role_menu` VALUES (1, 2095);
INSERT INTO `sys_role_menu` VALUES (1, 2096);
INSERT INTO `sys_role_menu` VALUES (1, 2097);
INSERT INTO `sys_role_menu` VALUES (1, 2098);
INSERT INTO `sys_role_menu` VALUES (1, 2099);
INSERT INTO `sys_role_menu` VALUES (1, 2100);
INSERT INTO `sys_role_menu` VALUES (1, 2101);
INSERT INTO `sys_role_menu` VALUES (1, 2102);
INSERT INTO `sys_role_menu` VALUES (1, 2103);
INSERT INTO `sys_role_menu` VALUES (1, 2104);
INSERT INTO `sys_role_menu` VALUES (1, 2105);
INSERT INTO `sys_role_menu` VALUES (1, 2106);
INSERT INTO `sys_role_menu` VALUES (1, 2107);
INSERT INTO `sys_role_menu` VALUES (1, 2108);
INSERT INTO `sys_role_menu` VALUES (2, 1);
INSERT INTO `sys_role_menu` VALUES (2, 2);
INSERT INTO `sys_role_menu` VALUES (2, 3);
INSERT INTO `sys_role_menu` VALUES (2, 4);
INSERT INTO `sys_role_menu` VALUES (2, 100);
INSERT INTO `sys_role_menu` VALUES (2, 101);
INSERT INTO `sys_role_menu` VALUES (2, 102);
INSERT INTO `sys_role_menu` VALUES (2, 103);
INSERT INTO `sys_role_menu` VALUES (2, 104);
INSERT INTO `sys_role_menu` VALUES (2, 105);
INSERT INTO `sys_role_menu` VALUES (2, 106);
INSERT INTO `sys_role_menu` VALUES (2, 107);
INSERT INTO `sys_role_menu` VALUES (2, 108);
INSERT INTO `sys_role_menu` VALUES (2, 109);
INSERT INTO `sys_role_menu` VALUES (2, 110);
INSERT INTO `sys_role_menu` VALUES (2, 111);
INSERT INTO `sys_role_menu` VALUES (2, 112);
INSERT INTO `sys_role_menu` VALUES (2, 113);
INSERT INTO `sys_role_menu` VALUES (2, 114);
INSERT INTO `sys_role_menu` VALUES (2, 115);
INSERT INTO `sys_role_menu` VALUES (2, 116);
INSERT INTO `sys_role_menu` VALUES (2, 117);
INSERT INTO `sys_role_menu` VALUES (2, 500);
INSERT INTO `sys_role_menu` VALUES (2, 501);
INSERT INTO `sys_role_menu` VALUES (2, 1000);
INSERT INTO `sys_role_menu` VALUES (2, 1001);
INSERT INTO `sys_role_menu` VALUES (2, 1002);
INSERT INTO `sys_role_menu` VALUES (2, 1003);
INSERT INTO `sys_role_menu` VALUES (2, 1004);
INSERT INTO `sys_role_menu` VALUES (2, 1005);
INSERT INTO `sys_role_menu` VALUES (2, 1006);
INSERT INTO `sys_role_menu` VALUES (2, 1007);
INSERT INTO `sys_role_menu` VALUES (2, 1008);
INSERT INTO `sys_role_menu` VALUES (2, 1009);
INSERT INTO `sys_role_menu` VALUES (2, 1010);
INSERT INTO `sys_role_menu` VALUES (2, 1011);
INSERT INTO `sys_role_menu` VALUES (2, 1012);
INSERT INTO `sys_role_menu` VALUES (2, 1013);
INSERT INTO `sys_role_menu` VALUES (2, 1014);
INSERT INTO `sys_role_menu` VALUES (2, 1015);
INSERT INTO `sys_role_menu` VALUES (2, 1016);
INSERT INTO `sys_role_menu` VALUES (2, 1017);
INSERT INTO `sys_role_menu` VALUES (2, 1018);
INSERT INTO `sys_role_menu` VALUES (2, 1019);
INSERT INTO `sys_role_menu` VALUES (2, 1020);
INSERT INTO `sys_role_menu` VALUES (2, 1021);
INSERT INTO `sys_role_menu` VALUES (2, 1022);
INSERT INTO `sys_role_menu` VALUES (2, 1023);
INSERT INTO `sys_role_menu` VALUES (2, 1024);
INSERT INTO `sys_role_menu` VALUES (2, 1025);
INSERT INTO `sys_role_menu` VALUES (2, 1026);
INSERT INTO `sys_role_menu` VALUES (2, 1027);
INSERT INTO `sys_role_menu` VALUES (2, 1028);
INSERT INTO `sys_role_menu` VALUES (2, 1029);
INSERT INTO `sys_role_menu` VALUES (2, 1030);
INSERT INTO `sys_role_menu` VALUES (2, 1031);
INSERT INTO `sys_role_menu` VALUES (2, 1032);
INSERT INTO `sys_role_menu` VALUES (2, 1033);
INSERT INTO `sys_role_menu` VALUES (2, 1034);
INSERT INTO `sys_role_menu` VALUES (2, 1035);
INSERT INTO `sys_role_menu` VALUES (2, 1036);
INSERT INTO `sys_role_menu` VALUES (2, 1037);
INSERT INTO `sys_role_menu` VALUES (2, 1038);
INSERT INTO `sys_role_menu` VALUES (2, 1039);
INSERT INTO `sys_role_menu` VALUES (2, 1040);
INSERT INTO `sys_role_menu` VALUES (2, 1041);
INSERT INTO `sys_role_menu` VALUES (2, 1042);
INSERT INTO `sys_role_menu` VALUES (2, 1043);
INSERT INTO `sys_role_menu` VALUES (2, 1044);
INSERT INTO `sys_role_menu` VALUES (2, 1045);
INSERT INTO `sys_role_menu` VALUES (2, 1046);
INSERT INTO `sys_role_menu` VALUES (2, 1047);
INSERT INTO `sys_role_menu` VALUES (2, 1048);
INSERT INTO `sys_role_menu` VALUES (2, 1049);
INSERT INTO `sys_role_menu` VALUES (2, 1050);
INSERT INTO `sys_role_menu` VALUES (2, 1051);
INSERT INTO `sys_role_menu` VALUES (2, 1052);
INSERT INTO `sys_role_menu` VALUES (2, 1053);
INSERT INTO `sys_role_menu` VALUES (2, 1054);
INSERT INTO `sys_role_menu` VALUES (2, 1055);
INSERT INTO `sys_role_menu` VALUES (2, 1056);
INSERT INTO `sys_role_menu` VALUES (2, 1057);
INSERT INTO `sys_role_menu` VALUES (2, 1058);
INSERT INTO `sys_role_menu` VALUES (2, 1059);
INSERT INTO `sys_role_menu` VALUES (2, 1060);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime(0) NULL DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2026-04-15 13:39:02', '2026-04-14 21:16:22', 'admin', '2026-04-14 21:16:22', '', NULL, '管理员');
INSERT INTO `sys_user` VALUES (2, 105, 'ry', '若依', '00', 'ry@qq.com', '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2026-04-14 21:16:22', '2026-04-14 21:16:22', 'admin', '2026-04-14 21:16:22', '', NULL, '测试员');

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`user_id`, `post_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (1, 1);
INSERT INTO `sys_user_post` VALUES (2, 2);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户和角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);

SET FOREIGN_KEY_CHECKS = 1;
