-- 角色门户与菜单重编排脚本（2026-04-20）
-- 目标：按角色分配农业模块菜单；超级管理员保留全量权限。

-- 1) 初始化角色（不存在才创建）
set @max_sort := ifnull((select max(role_sort) from sys_role), 2);

insert into sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
select '农业管理员', 'agri_admin', @max_sort + 1, '2', 1, 1, '0', '0', 'admin', sysdate(), '', null, '农业业务管理员（可见全部农业菜单）'
where not exists (select 1 from sys_role where role_key = 'agri_admin');

insert into sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
select '种植员', 'agri_farmer', @max_sort + 2, '3', 1, 1, '0', '0', 'admin', sysdate(), '', null, '种植作业角色（环境、农事、病虫、产量）'
where not exists (select 1 from sys_role where role_key = 'agri_farmer');

insert into sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
select '销售专员', 'agri_sales', @max_sort + 3, '3', 1, 1, '0', '0', 'admin', sysdate(), '', null, '销售运营角色（产销、扫码、品牌）'
where not exists (select 1 from sys_role where role_key = 'agri_sales');

insert into sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
select '物流调度员', 'agri_logistics', @max_sort + 4, '3', 1, 1, '0', '0', 'admin', sysdate(), '', null, '物流运输角色（路径、温湿度、预警）'
where not exists (select 1 from sys_role where role_key = 'agri_logistics');

insert into sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
select '质检员', 'agri_quality', @max_sort + 5, '3', 1, 1, '0', '0', 'admin', sysdate(), '', null, '质检与合规角色（质检、报告、存证）'
where not exists (select 1 from sys_role where role_key = 'agri_quality');

-- 2) 角色ID
set @role_admin := (select role_id from sys_role where role_key = 'agri_admin' limit 1);
set @role_farmer := (select role_id from sys_role where role_key = 'agri_farmer' limit 1);
set @role_sales := (select role_id from sys_role where role_key = 'agri_sales' limit 1);
set @role_logistics := (select role_id from sys_role where role_key = 'agri_logistics' limit 1);
set @role_quality := (select role_id from sys_role where role_key = 'agri_quality' limit 1);

-- 3) 保证超级管理员拥有所有农业菜单（含后续新增）
insert ignore into sys_role_menu (role_id, menu_id)
select 1, m.menu_id
from sys_menu m
where m.path = 'agri'
   or ifnull(m.perms, '') like 'agri:%'
   or m.parent_id in (select menu_id from sys_menu where path = 'agri');

-- 4) 清理已有农业菜单映射，避免重复授权和历史残留
delete rm
from sys_role_menu rm
join sys_menu m on m.menu_id = rm.menu_id
where rm.role_id in (@role_admin, @role_farmer, @role_sales, @role_logistics, @role_quality)
  and (
    m.path = 'agri'
    or ifnull(m.perms, '') like 'agri:%'
    or m.parent_id in (select menu_id from sys_menu where path = 'agri')
  );

-- 5) 农业管理员：全部农业菜单
insert ignore into sys_role_menu (role_id, menu_id)
select @role_admin, m.menu_id
from sys_menu m
where @role_admin is not null
  and (
    m.path = 'agri'
    or ifnull(m.perms, '') like 'agri:%'
    or m.parent_id in (select menu_id from sys_menu where path = 'agri')
  );

-- 6) 种植员：首页 + 种植主线 + 预警 + 待办
insert ignore into sys_role_menu (role_id, menu_id)
select distinct @role_farmer, x.menu_id
from (
  select m.menu_id
  from sys_menu m
  where m.path in ('farmOp', 'envSensor', 'pestIdentify', 'yieldForecast', 'warningSummary', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(farmOp|envSensor|pestIdentify|yieldForecast|warningSummary|todoTaskReminder|dashboardOverview):'
  union
  select m.parent_id
  from sys_menu m
  where (m.path in ('farmOp', 'envSensor', 'pestIdentify', 'yieldForecast', 'warningSummary', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(farmOp|envSensor|pestIdentify|yieldForecast|warningSummary|todoTaskReminder|dashboardOverview):')
    and m.parent_id > 0
  union
  select p.parent_id
  from sys_menu m
  left join sys_menu p on p.menu_id = m.parent_id
  where (m.path in ('farmOp', 'envSensor', 'pestIdentify', 'yieldForecast', 'warningSummary', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(farmOp|envSensor|pestIdentify|yieldForecast|warningSummary|todoTaskReminder|dashboardOverview):')
    and ifnull(p.parent_id, 0) > 0
) x
where @role_farmer is not null and x.menu_id is not null;

-- 7) 销售专员：首页 + 产销 + 扫码 + 品牌 + 市场预测 + 待办
insert ignore into sys_role_menu (role_id, menu_id)
select distinct @role_sales, x.menu_id
from (
  select m.menu_id
  from sys_menu m
  where m.path in ('outputSalesTrend', 'consumerScan', 'brandTrace', 'marketForecast', 'traceQueryStats', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(outputSalesTrend|consumerScan|brandTrace|marketForecast|traceQueryStats|todoTaskReminder|dashboardOverview):'
  union
  select m.parent_id
  from sys_menu m
  where (m.path in ('outputSalesTrend', 'consumerScan', 'brandTrace', 'marketForecast', 'traceQueryStats', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(outputSalesTrend|consumerScan|brandTrace|marketForecast|traceQueryStats|todoTaskReminder|dashboardOverview):')
    and m.parent_id > 0
  union
  select p.parent_id
  from sys_menu m
  left join sys_menu p on p.menu_id = m.parent_id
  where (m.path in ('outputSalesTrend', 'consumerScan', 'brandTrace', 'marketForecast', 'traceQueryStats', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(outputSalesTrend|consumerScan|brandTrace|marketForecast|traceQueryStats|todoTaskReminder|dashboardOverview):')
    and ifnull(p.parent_id, 0) > 0
) x
where @role_sales is not null and x.menu_id is not null;

-- 8) 物流调度员：首页 + 物流主线 + 预警 + 设备 + 待办
insert ignore into sys_role_menu (role_id, menu_id)
select distinct @role_logistics, x.menu_id
from (
  select m.menu_id
  from sys_menu m
  where m.path in ('logisticsTrack', 'logisticsTempHumidity', 'logisticsWarning', 'warningSummary', 'deviceStatusMonitor', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(logisticsTrack|logisticsTempHumidity|logisticsWarning|warningSummary|deviceStatusMonitor|todoTaskReminder|dashboardOverview):'
  union
  select m.parent_id
  from sys_menu m
  where (m.path in ('logisticsTrack', 'logisticsTempHumidity', 'logisticsWarning', 'warningSummary', 'deviceStatusMonitor', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(logisticsTrack|logisticsTempHumidity|logisticsWarning|warningSummary|deviceStatusMonitor|todoTaskReminder|dashboardOverview):')
    and m.parent_id > 0
  union
  select p.parent_id
  from sys_menu m
  left join sys_menu p on p.menu_id = m.parent_id
  where (m.path in ('logisticsTrack', 'logisticsTempHumidity', 'logisticsWarning', 'warningSummary', 'deviceStatusMonitor', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(logisticsTrack|logisticsTempHumidity|logisticsWarning|warningSummary|deviceStatusMonitor|todoTaskReminder|dashboardOverview):')
    and ifnull(p.parent_id, 0) > 0
) x
where @role_logistics is not null and x.menu_id is not null;

-- 9) 质检员：首页 + 质检主线 + 存证校验 + 审计 + 待办
insert ignore into sys_role_menu (role_id, menu_id)
select distinct @role_quality, x.menu_id
from (
  select m.menu_id
  from sys_menu m
  where m.path in ('qualityInspect', 'qualityReport', 'attestationVerify', 'auditLog', 'warningSummary', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(qualityInspect|qualityReport|attestationVerify|auditLog|warningSummary|todoTaskReminder|dashboardOverview):'
  union
  select m.parent_id
  from sys_menu m
  where (m.path in ('qualityInspect', 'qualityReport', 'attestationVerify', 'auditLog', 'warningSummary', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(qualityInspect|qualityReport|attestationVerify|auditLog|warningSummary|todoTaskReminder|dashboardOverview):')
    and m.parent_id > 0
  union
  select p.parent_id
  from sys_menu m
  left join sys_menu p on p.menu_id = m.parent_id
  where (m.path in ('qualityInspect', 'qualityReport', 'attestationVerify', 'auditLog', 'warningSummary', 'todoTaskReminder', 'dashboardOverview')
     or ifnull(m.perms, '') regexp '^agri:(qualityInspect|qualityReport|attestationVerify|auditLog|warningSummary|todoTaskReminder|dashboardOverview):')
    and ifnull(p.parent_id, 0) > 0
) x
where @role_quality is not null and x.menu_id is not null;
