-- Verify ledger seed result (minimum rows per ledger table)
-- Excluded by requirement: agri_pest_identify_task, agri_quality_inspect_task

SET @min_rows = 10;

SELECT table_name, row_count,
       CASE WHEN row_count >= @min_rows THEN 'PASS' ELSE 'FAIL' END AS check_result
FROM (
  SELECT 'agri_base_rule' AS table_name, COUNT(*) AS row_count FROM agri_base_rule
  UNION ALL SELECT 'agri_brand_trace_page', COUNT(*) FROM agri_brand_trace_page
  UNION ALL SELECT 'agri_carbon_footprint_model', COUNT(*) FROM agri_carbon_footprint_model
  UNION ALL SELECT 'agri_consumer_scan_query', COUNT(*) FROM agri_consumer_scan_query
  UNION ALL SELECT 'agri_dashboard_overview', COUNT(*) FROM agri_dashboard_overview
  UNION ALL SELECT 'agri_data_attestation_verify', COUNT(*) FROM agri_data_attestation_verify
  UNION ALL SELECT 'agri_data_uplink_task', COUNT(*) FROM agri_data_uplink_task
  UNION ALL SELECT 'agri_device_access_node', COUNT(*) FROM agri_device_access_node
  UNION ALL SELECT 'agri_device_status_monitor', COUNT(*) FROM agri_device_status_monitor
  UNION ALL SELECT 'agri_env_sensor_record', COUNT(*) FROM agri_env_sensor_record
  UNION ALL SELECT 'agri_farm_operation_record', COUNT(*) FROM agri_farm_operation_record
  UNION ALL SELECT 'agri_finance_risk_metric', COUNT(*) FROM agri_finance_risk_metric
  UNION ALL SELECT 'agri_logistics_temp_humidity', COUNT(*) FROM agri_logistics_temp_humidity
  UNION ALL SELECT 'agri_logistics_track', COUNT(*) FROM agri_logistics_track
  UNION ALL SELECT 'agri_logistics_warning', COUNT(*) FROM agri_logistics_warning
  UNION ALL SELECT 'agri_market_forecast', COUNT(*) FROM agri_market_forecast
  UNION ALL SELECT 'agri_output_sales_trend', COUNT(*) FROM agri_output_sales_trend
  UNION ALL SELECT 'agri_process_batch_link', COUNT(*) FROM agri_process_batch_link
  UNION ALL SELECT 'agri_quality_report', COUNT(*) FROM agri_quality_report
  UNION ALL SELECT 'agri_smart_contract_deploy', COUNT(*) FROM agri_smart_contract_deploy
  UNION ALL SELECT 'agri_supply_chain_contract', COUNT(*) FROM agri_supply_chain_contract
  UNION ALL SELECT 'agri_third_party_api_access', COUNT(*) FROM agri_third_party_api_access
  UNION ALL SELECT 'agri_todo_task_reminder', COUNT(*) FROM agri_todo_task_reminder
  UNION ALL SELECT 'agri_trace_audit_log', COUNT(*) FROM agri_trace_audit_log
  UNION ALL SELECT 'agri_trace_query_stats', COUNT(*) FROM agri_trace_query_stats
  UNION ALL SELECT 'agri_user_access_grant', COUNT(*) FROM agri_user_access_grant
  UNION ALL SELECT 'agri_warning_summary', COUNT(*) FROM agri_warning_summary
  UNION ALL SELECT 'agri_yield_forecast_task', COUNT(*) FROM agri_yield_forecast_task
) t
ORDER BY CASE WHEN row_count >= @min_rows THEN 0 ELSE 1 END, table_name;

SELECT
  SUM(CASE WHEN row_count >= @min_rows THEN 1 ELSE 0 END) AS pass_tables,
  SUM(CASE WHEN row_count < @min_rows THEN 1 ELSE 0 END) AS fail_tables,
  COUNT(*) AS total_tables
FROM (
  SELECT COUNT(*) AS row_count FROM agri_base_rule
  UNION ALL SELECT COUNT(*) FROM agri_brand_trace_page
  UNION ALL SELECT COUNT(*) FROM agri_carbon_footprint_model
  UNION ALL SELECT COUNT(*) FROM agri_consumer_scan_query
  UNION ALL SELECT COUNT(*) FROM agri_dashboard_overview
  UNION ALL SELECT COUNT(*) FROM agri_data_attestation_verify
  UNION ALL SELECT COUNT(*) FROM agri_data_uplink_task
  UNION ALL SELECT COUNT(*) FROM agri_device_access_node
  UNION ALL SELECT COUNT(*) FROM agri_device_status_monitor
  UNION ALL SELECT COUNT(*) FROM agri_env_sensor_record
  UNION ALL SELECT COUNT(*) FROM agri_farm_operation_record
  UNION ALL SELECT COUNT(*) FROM agri_finance_risk_metric
  UNION ALL SELECT COUNT(*) FROM agri_logistics_temp_humidity
  UNION ALL SELECT COUNT(*) FROM agri_logistics_track
  UNION ALL SELECT COUNT(*) FROM agri_logistics_warning
  UNION ALL SELECT COUNT(*) FROM agri_market_forecast
  UNION ALL SELECT COUNT(*) FROM agri_output_sales_trend
  UNION ALL SELECT COUNT(*) FROM agri_process_batch_link
  UNION ALL SELECT COUNT(*) FROM agri_quality_report
  UNION ALL SELECT COUNT(*) FROM agri_smart_contract_deploy
  UNION ALL SELECT COUNT(*) FROM agri_supply_chain_contract
  UNION ALL SELECT COUNT(*) FROM agri_third_party_api_access
  UNION ALL SELECT COUNT(*) FROM agri_todo_task_reminder
  UNION ALL SELECT COUNT(*) FROM agri_trace_audit_log
  UNION ALL SELECT COUNT(*) FROM agri_trace_query_stats
  UNION ALL SELECT COUNT(*) FROM agri_user_access_grant
  UNION ALL SELECT COUNT(*) FROM agri_warning_summary
  UNION ALL SELECT COUNT(*) FROM agri_yield_forecast_task
) s;
