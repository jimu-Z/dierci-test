-- Unified ledger reset + seed script
-- Scope: all agri ledger pages except pest identify and quality inspect
-- Excluded tables: agri_pest_identify_task, agri_quality_inspect_task

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_seed_nums;
CREATE TEMPORARY TABLE tmp_seed_nums (
  n INT PRIMARY KEY
);
INSERT INTO tmp_seed_nums(n) VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10);

-- 1) Base rule
DELETE FROM agri_base_rule;
INSERT INTO agri_base_rule(rule_name, rule_type, rule_code, rule_content, status, remark, create_by)
SELECT
  CONCAT('Init rule ', LPAD(n, 2, '0')),
  ELT((n % 6) + 1, 'ENV_MONITOR', 'FARM_OP_STANDARD', 'QUALITY_GRADE', 'LOGISTICS_PATH', 'MARKET_MODEL', 'SMART_CONTRACT'),
  CONCAT('INIT_RULE_', LPAD(n, 2, '0')),
  CONCAT('{"threshold":', 40 + n, ',"version":"v1.', n, '"}'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 2) Brand trace page
DELETE FROM agri_brand_trace_page;
INSERT INTO agri_brand_trace_page(trace_code, brand_name, product_name, product_code, origin_place, planting_batch_no, process_batch_no, logistics_trace_code, cover_image_url, page_url, qr_code_url, brand_story, publish_status, status, remark, create_by)
SELECT
  CONCAT('TRC-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(n, 3, '0')),
  CONCAT('QY Brand ', n),
  CONCAT('Agri Product ', n),
  CONCAT('P', LPAD(n, 4, '0')),
  ELT((n % 4) + 1, 'Shandong', 'Henan', 'Sichuan', 'Hunan'),
  CONCAT('PB', DATE_FORMAT(CURDATE(), '%y%m'), LPAD(n, 3, '0')),
  CONCAT('PR', DATE_FORMAT(CURDATE(), '%y%m'), LPAD(n, 3, '0')),
  CONCAT('LG', DATE_FORMAT(CURDATE(), '%y%m%d'), LPAD(n, 3, '0')),
  CONCAT('https://img.example.com/brand/', n, '.jpg'),
  CONCAT('https://trace.example.com/page/', n),
  CONCAT('https://trace.example.com/qr/', n),
  CONCAT('Seed story ', n),
  IF(n <= 6, '1', '0'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 3) Carbon footprint model
DELETE FROM agri_carbon_footprint_model;
INSERT INTO agri_carbon_footprint_model(model_code, model_name, product_type, emission_factor, calc_status, status, remark, create_by)
SELECT
  CONCAT('CFM-', LPAD(n, 3, '0')),
  CONCAT('Carbon model ', n),
  ELT((n % 4) + 1, 'VEGETABLE', 'FRUIT', 'GRAIN', 'TEA'),
  ROUND(0.8 + n * 0.12, 4),
  ELT((n % 3) + 1, '0', '1', '2'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 4) Consumer scan query
DELETE FROM agri_consumer_scan_query;
INSERT INTO agri_consumer_scan_query(trace_code, consumer_name, consumer_phone, scan_channel, scan_address, scan_ip, scan_result, query_time, status, remark, create_by)
SELECT
  CONCAT('TRC-SCAN-', LPAD(n, 4, '0')),
  CONCAT('Consumer', n),
  CONCAT('1380000', LPAD(n, 4, '0')),
  ELT((n % 3) + 1, 'APP', 'MINI_PROGRAM', 'H5'),
  ELT((n % 4) + 1, 'Jinan', 'Zhengzhou', 'Changsha', 'Chengdu'),
  CONCAT('10.0.0.', n),
  ELT((n % 3) + 1, '1', '2', '0'),
  DATE_SUB(NOW(), INTERVAL n HOUR),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 5) Dashboard overview
DELETE FROM agri_dashboard_overview;
INSERT INTO agri_dashboard_overview(stat_date, total_output, total_sales, trace_query_count, warning_count, online_device_count, pending_task_count, status, remark, create_by)
SELECT
  DATE_SUB(CURDATE(), INTERVAL (10 - n) DAY),
  120 + n * 8,
  80 + n * 6,
  300 + n * 25,
  5 + (n % 4),
  60 + n,
  8 + (n % 5),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 6) Data attestation verify
DELETE FROM agri_data_attestation_verify;
INSERT INTO agri_data_attestation_verify(attestation_no, batch_no, data_type, origin_hash, verify_status, status, remark, create_by)
SELECT
  CONCAT('AT-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(n, 3, '0')),
  CONCAT('BATCH-', DATE_FORMAT(CURDATE(), '%y%m'), '-', LPAD(n, 3, '0')),
  ELT((n % 4) + 1, 'ENV', 'FARM_OP', 'QUALITY', 'LOGISTICS'),
  CONCAT('hash_', LPAD(n, 32, '0')),
  ELT((n % 3) + 1, '0', '1', '2'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 7) Data uplink task
DELETE FROM agri_data_uplink_task;
INSERT INTO agri_data_uplink_task(batch_no, data_type, data_hash, chain_platform, contract_address, tx_hash, uplink_status, uplink_time, status, remark, create_by)
SELECT
  CONCAT('UPB-', DATE_FORMAT(CURDATE(), '%y%m'), '-', LPAD(n, 3, '0')),
  ELT((n % 4) + 1, 'ENV', 'TRACE', 'QUALITY', 'USER'),
  CONCAT('uplink_hash_', LPAD(n, 16, '0')),
  ELT((n % 3) + 1, 'FISCO_BCOS', 'ETHEREUM', 'FABRIC'),
  CONCAT('0x', LPAD(CONV(n * 12345, 10, 16), 40, '0')),
  CONCAT('0x', LPAD(CONV(n * 54321, 10, 16), 64, '0')),
  ELT((n % 3) + 1, '0', '1', '2'),
  DATE_SUB(NOW(), INTERVAL n DAY),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 8) Device access node
DELETE FROM agri_device_access_node;
INSERT INTO agri_device_access_node(device_code, device_name, device_type, protocol_type, firmware_version, bind_area, access_status, status, remark, create_by)
SELECT
  CONCAT('DEV-', LPAD(n, 4, '0')),
  CONCAT('Device ', n),
  ELT((n % 4) + 1, 'SENSOR', 'GATEWAY', 'CAMERA', 'CONTROLLER'),
  ELT((n % 3) + 1, 'MQTT', 'HTTP', 'MODBUS'),
  CONCAT('v1.', n),
  CONCAT('PLOT-', LPAD((n % 6) + 1, 3, '0')),
  ELT((n % 3) + 1, '0', '1', '2'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 9) Device status monitor
DELETE FROM agri_device_status_monitor;
INSERT INTO agri_device_status_monitor(device_code, device_name, online_status, battery_level, warning_level, status, remark, create_by)
SELECT
  CONCAT('DEV-', LPAD(n, 4, '0')),
  CONCAT('Monitor Device ', n),
  IF(n % 4 = 0, '0', '1'),
  ROUND(30 + n * 6.5, 2),
  ELT((n % 4) + 1, '0', '1', '2', '3'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 10) Env sensor record
DELETE FROM agri_env_sensor_record;
INSERT INTO agri_env_sensor_record(device_code, plot_code, temperature, humidity, co2_value, status, data_source, collect_time, remark, create_by)
SELECT
  CONCAT('ENV-', LPAD(n, 4, '0')),
  CONCAT('PLOT-', LPAD((n % 6) + 1, 3, '0')),
  ROUND(18 + n * 0.8, 2),
  ROUND(45 + n * 1.2, 2),
  ROUND(380 + n * 12, 2),
  '0',
  ELT((n % 3) + 1, 'IOT', 'MANUAL', 'GATEWAY'),
  DATE_SUB(NOW(), INTERVAL n HOUR),
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 11) Farm operation record
DELETE FROM agri_farm_operation_record;
INSERT INTO agri_farm_operation_record(plot_code, operation_type, operation_content, operator_name, input_name, input_amount, input_unit, operation_time, status, remark, create_by)
SELECT
  CONCAT('PLOT-', LPAD((n % 6) + 1, 3, '0')),
  ELT((n % 5) + 1, 'SOWING', 'FERTILIZATION', 'IRRIGATION', 'PEST_CONTROL', 'HARVEST'),
  CONCAT('Operation task ', n),
  ELT((n % 4) + 1, 'ZhangSan', 'LiSi', 'WangWu', 'ZhaoLiu'),
  ELT((n % 4) + 1, 'Fertilizer', 'Pesticide', 'Water', 'BioAgent'),
  ROUND(1 + n * 0.6, 2),
  ELT((n % 3) + 1, 'kg', 'L', 'bag'),
  DATE_SUB(NOW(), INTERVAL n DAY),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 12) Finance risk metric
DELETE FROM agri_finance_risk_metric;
INSERT INTO agri_finance_risk_metric(indicator_code, indicator_name, risk_dimension, risk_score, risk_level, evaluate_status, status, remark, create_by)
SELECT
  CONCAT('RISK-', LPAD(n, 3, '0')),
  CONCAT('Risk indicator ', n),
  ELT((n % 4) + 1, 'CREDIT', 'ASSET', 'OPERATION', 'COMPLIANCE'),
  ROUND(40 + n * 4.5, 2),
  ELT((n % 4) + 1, 'L', 'M', 'H', 'C'),
  ELT((n % 3) + 1, '0', '1', '2'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 13) Logistics temp/humidity
DELETE FROM agri_logistics_temp_humidity;
INSERT INTO agri_logistics_temp_humidity(trace_code, order_no, device_code, collect_location, temperature, humidity, temp_upper_limit, temp_lower_limit, humidity_upper_limit, humidity_lower_limit, alert_flag, alert_message, collect_time, status, remark, create_by)
SELECT
  CONCAT('TR-LTH-', LPAD(n, 4, '0')),
  CONCAT('ORD-', DATE_FORMAT(CURDATE(), '%y%m'), '-', LPAD(n, 4, '0')),
  CONCAT('TMP-', LPAD(n, 4, '0')),
  ELT((n % 4) + 1, 'ColdStorage-A', 'Truck-01', 'Transit-Station', 'Warehouse-B'),
  ROUND(2 + n * 0.5, 2),
  ROUND(55 + n * 1.1, 2),
  8,
  0,
  80,
  40,
  IF(n % 5 = 0, '1', '0'),
  IF(n % 5 = 0, 'Threshold warning', 'Normal'),
  DATE_SUB(NOW(), INTERVAL n HOUR),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 14) Logistics track
DELETE FROM agri_logistics_track;
INSERT INTO agri_logistics_track(trace_code, order_no, product_batch_no, vehicle_no, driver_name, driver_phone, start_location, current_location, target_location, route_path, track_status, event_desc, event_time, temperature, humidity, longitude, latitude, status, remark, create_by)
SELECT
  CONCAT('TR-LOG-', LPAD(n, 4, '0')),
  CONCAT('ORD-', DATE_FORMAT(CURDATE(), '%y%m'), '-', LPAD(n, 4, '0')),
  CONCAT('PB-', DATE_FORMAT(CURDATE(), '%y%m'), '-', LPAD(n, 3, '0')),
  CONCAT('CAR-', LPAD(n, 3, '0')),
  ELT((n % 4) + 1, 'DriverA', 'DriverB', 'DriverC', 'DriverD'),
  CONCAT('1390000', LPAD(n, 4, '0')),
  'Farm Center',
  ELT((n % 4) + 1, 'Hub-1', 'Hub-2', 'Highway', 'City Gate'),
  'Retail DC',
  'Farm Center -> Hub -> Retail DC',
  ELT((n % 4) + 1, '0', '1', '2', '3'),
  CONCAT('Track event ', n),
  DATE_SUB(NOW(), INTERVAL n HOUR),
  ROUND(4 + n * 0.4, 2),
  ROUND(60 + n * 0.8, 2),
  116.30 + n * 0.01,
  39.90 + n * 0.01,
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 15) Logistics warning
DELETE FROM agri_logistics_warning;
INSERT INTO agri_logistics_warning(trace_code, order_no, warning_type, warning_level, warning_status, source_record_id, warning_title, warning_content, warning_time, handler, handle_time, handle_remark, status, remark, create_by)
SELECT
  CONCAT('TR-W-', LPAD(n, 4, '0')),
  CONCAT('ORD-', DATE_FORMAT(CURDATE(), '%y%m'), '-', LPAD(n, 4, '0')),
  ELT((n % 4) + 1, 'T', 'H', 'D', 'R'),
  ELT((n % 3) + 1, '1', '2', '3'),
  ELT((n % 3) + 1, '0', '1', '2'),
  n,
  CONCAT('Warning ', n),
  CONCAT('Warning detail ', n),
  DATE_SUB(NOW(), INTERVAL n HOUR),
  IF(n % 2 = 0, 'ops_user', NULL),
  IF(n % 2 = 0, DATE_SUB(NOW(), INTERVAL (n - 1) HOUR), NULL),
  IF(n % 2 = 0, 'handled', NULL),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 16) Market forecast
DELETE FROM agri_market_forecast;
INSERT INTO agri_market_forecast(market_area, product_code, product_name, period_start, period_end, historical_sales_kg, forecast_sales_kg, forecast_price, confidence_rate, model_version, forecast_status, forecast_time, status, remark, create_by)
SELECT
  ELT((n % 4) + 1, 'North', 'East', 'South', 'West'),
  CONCAT('SKU-', LPAD(n, 4, '0')),
  CONCAT('Product ', n),
  DATE_SUB(CURDATE(), INTERVAL (30 + n) DAY),
  DATE_ADD(CURDATE(), INTERVAL n DAY),
  ROUND(1000 + n * 80, 2),
  ROUND(1100 + n * 95, 2),
  ROUND(8 + n * 0.3, 2),
  ROUND(0.70 + n * 0.02, 2),
  CONCAT('market-model-v', 1 + (n % 3)),
  ELT((n % 3) + 1, '0', '1', '2'),
  DATE_SUB(NOW(), INTERVAL n DAY),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 17) Output sales trend
DELETE FROM agri_output_sales_trend;
INSERT INTO agri_output_sales_trend(stat_date, output_value, sales_value, target_output, target_sales, output_mom_rate, sales_mom_rate, status, remark, create_by)
SELECT
  DATE_SUB(CURDATE(), INTERVAL (10 - n) DAY),
  ROUND(90 + n * 6, 2),
  ROUND(65 + n * 5, 2),
  120,
  90,
  ROUND((n - 5) * 1.8, 2),
  ROUND((n - 4) * 1.6, 2),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 18) Process batch link
DELETE FROM agri_process_batch_link;
INSERT INTO agri_process_batch_link(planting_batch_no, process_batch_no, product_code, process_weight_kg, process_status, process_time, status, remark, create_by)
SELECT
  CONCAT('PLB-', DATE_FORMAT(CURDATE(), '%y%m'), '-', LPAD(n, 3, '0')),
  CONCAT('PRB-', DATE_FORMAT(CURDATE(), '%y%m'), '-', LPAD(n, 3, '0')),
  CONCAT('SKU-', LPAD(n, 4, '0')),
  ROUND(500 + n * 35, 2),
  ELT((n % 3) + 1, '0', '1', '2'),
  DATE_SUB(NOW(), INTERVAL n DAY),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 19) Quality report
DELETE FROM agri_quality_report;
INSERT INTO agri_quality_report(report_no, inspect_id, process_batch_no, quality_grade, defect_rate, report_summary, report_status, report_time, status, remark, create_by)
SELECT
  CONCAT('QR-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(n, 3, '0')),
  1000 + n,
  CONCAT('PRB-', DATE_FORMAT(CURDATE(), '%y%m'), '-', LPAD(n, 3, '0')),
  ELT((n % 4) + 1, 'A', 'B', 'C', 'A+'),
  ROUND(0.5 + n * 0.3, 2),
  CONCAT('Quality summary ', n),
  ELT((n % 3) + 1, '0', '1', '2'),
  DATE_SUB(NOW(), INTERVAL n DAY),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 20) Smart contract deploy
DELETE FROM agri_smart_contract_deploy;
INSERT INTO agri_smart_contract_deploy(contract_name, contract_version, chain_platform, source_hash, abi_json, contract_address, deploy_tx_hash, deploy_status, deploy_time, status, remark, create_by)
SELECT
  CONCAT('Contract ', n),
  CONCAT('v', 1 + (n % 3), '.', n),
  ELT((n % 3) + 1, 'FISCO_BCOS', 'ETHEREUM', 'FABRIC'),
  CONCAT('src_hash_', LPAD(n, 16, '0')),
  CONCAT('{"name":"c', n, '"}'),
  CONCAT('0x', LPAD(CONV(n * 22222, 10, 16), 40, '0')),
  CONCAT('0x', LPAD(CONV(n * 33333, 10, 16), 64, '0')),
  ELT((n % 3) + 1, '0', '1', '2'),
  DATE_SUB(NOW(), INTERVAL n DAY),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 21) Supply chain contract
DELETE FROM agri_supply_chain_contract;
INSERT INTO agri_supply_chain_contract(contract_no, contract_name, finance_subject, finance_amount, contract_status, risk_level, status, remark, create_by)
SELECT
  CONCAT('SC-', DATE_FORMAT(CURDATE(), '%y%m'), '-', LPAD(n, 4, '0')),
  CONCAT('Supply contract ', n),
  CONCAT('Subject ', n),
  ROUND(100000 + n * 12000, 2),
  ELT((n % 4) + 1, '0', '1', '2', '3'),
  ELT((n % 4) + 1, 'L', 'M', 'H', 'C'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 22) Third party API access
DELETE FROM agri_third_party_api_access;
INSERT INTO agri_third_party_api_access(access_code, access_name, api_type, endpoint_url, timeout_sec, call_status, status, remark, create_by)
SELECT
  CONCAT('API-', LPAD(n, 4, '0')),
  CONCAT('Third API ', n),
  ELT((n % 4) + 1, 'WEATHER', 'PRICE', 'MAP', 'IOT'),
  CONCAT('https://api.example.com/v1/endpoint/', n),
  30 + n,
  ELT((n % 3) + 1, '0', '1', '2'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 23) Todo task reminder
DELETE FROM agri_todo_task_reminder;
INSERT INTO agri_todo_task_reminder(task_code, task_title, priority_level, deadline_time, reminder_status, status, remark, create_by)
SELECT
  CONCAT('TODO-', DATE_FORMAT(CURDATE(), '%y%m%d'), '-', LPAD(n, 3, '0')),
  CONCAT('Reminder task ', n),
  ELT((n % 4) + 1, '1', '2', '3', '4'),
  DATE_ADD(NOW(), INTERVAL n DAY),
  ELT((n % 3) + 1, '0', '1', '2'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 24) Trace audit log
DELETE FROM agri_trace_audit_log;
INSERT INTO agri_trace_audit_log(biz_no, module_name, action_type, operate_result, status, remark, create_by)
SELECT
  CONCAT('BIZ-', DATE_FORMAT(CURDATE(), '%Y%m%d'), '-', LPAD(n, 4, '0')),
  ELT((n % 5) + 1, 'TRACE', 'QUALITY', 'LOGISTICS', 'CONTRACT', 'USER'),
  ELT((n % 4) + 1, 'CREATE', 'UPDATE', 'VERIFY', 'EXPORT'),
  IF(n % 5 = 0, '0', '1'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 25) Trace query stats
DELETE FROM agri_trace_query_stats;
INSERT INTO agri_trace_query_stats(stat_date, total_query_count, unique_user_count, avg_duration_ms, success_count, fail_count, peak_qps, status, remark, create_by)
SELECT
  DATE_SUB(CURDATE(), INTERVAL (10 - n) DAY),
  500 + n * 40,
  120 + n * 8,
  180 + n * 6,
  480 + n * 36,
  20 + n * 4,
  ROUND(10 + n * 0.8, 2),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 26) User access grant
DELETE FROM agri_user_access_grant;
INSERT INTO agri_user_access_grant(user_name, nick_name, role_key, data_scope, menu_scope, grant_status, status, remark, create_by)
SELECT
  CONCAT('agri_user_', LPAD(n, 2, '0')),
  CONCAT('AgriUser', n),
  ELT((n % 5) + 1, 'agri_admin', 'planter', 'seller', 'logistics', 'quality'),
  ELT((n % 4) + 1, 'ALL', 'DEPT', 'CUSTOM', 'SELF'),
  ELT((n % 4) + 1, 'ALL', 'CORE', 'OPERATE', 'READ'),
  ELT((n % 3) + 1, '0', '1', '2'),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 27) Warning summary
DELETE FROM agri_warning_summary;
INSERT INTO agri_warning_summary(summary_date, total_warning_count, level1_count, level2_count, level3_count, handled_count, pending_count, avg_handle_minutes, status, remark, create_by)
SELECT
  DATE_SUB(CURDATE(), INTERVAL (10 - n) DAY),
  30 + n * 3,
  10 + n,
  8 + n,
  5 + (n % 4),
  20 + n * 2,
  10 + n,
  35 + n * 3,
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

-- 28) Yield forecast task
DELETE FROM agri_yield_forecast_task;
INSERT INTO agri_yield_forecast_task(plot_code, crop_name, season, sow_date, area_mu, forecast_yield_kg, model_version, forecast_status, forecast_time, status, remark, create_by)
SELECT
  CONCAT('PLOT-', LPAD((n % 6) + 1, 3, '0')),
  ELT((n % 4) + 1, 'Tomato', 'Cucumber', 'Corn', 'Rice'),
  ELT((n % 4) + 1, 'SPRING', 'SUMMER', 'AUTUMN', 'WINTER'),
  DATE_SUB(CURDATE(), INTERVAL (35 + n) DAY),
  ROUND(15 + n * 2.5, 2),
  ROUND(2200 + n * 120, 2),
  CONCAT('yield-model-v', 1 + (n % 3)),
  ELT((n % 3) + 1, '0', '1', '2'),
  DATE_SUB(NOW(), INTERVAL n DAY),
  '0',
  CONCAT('seed-', n),
  'seed'
FROM tmp_seed_nums;

DROP TEMPORARY TABLE IF EXISTS tmp_seed_nums;
SET FOREIGN_KEY_CHECKS = 1;
