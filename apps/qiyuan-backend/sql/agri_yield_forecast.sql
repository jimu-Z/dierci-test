-- 启元农链第二阶段：产量预测模型接入（P2-M1-F04）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 当前为模型接入占位版本，支持任务录入与预测结果回写。

create table if not exists agri_yield_forecast_task (
  forecast_id        bigint(20)      not null auto_increment comment '主键',
  plot_code          varchar(64)     not null comment '地块编码',
  crop_name          varchar(64)     not null comment '作物名称',
  season             varchar(30)     not null comment '季节',
  sow_date           date            not null comment '播种日期',
  area_mu            decimal(10,2)   not null comment '种植面积(亩)',
  forecast_yield_kg  decimal(12,2)   default null comment '预测产量(kg)',
  model_version      varchar(50)     default null comment '模型版本',
  forecast_status    char(1)         default '0' comment '预测状态（0待预测 1已预测 2失败）',
  forecast_time      datetime        default null comment '预测时间',
  status             char(1)         default '0' comment '状态（0正常 1停用）',
  remark             varchar(500)    default null comment '备注',
  create_by          varchar(64)     default '' comment '创建者',
  create_time        datetime        default current_timestamp comment '创建时间',
  update_by          varchar(64)     default '' comment '更新者',
  update_time        datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (forecast_id),
  key idx_plot_status (plot_code, forecast_status),
  key idx_crop_status (crop_name, forecast_status)
) engine=innodb comment='启元农链产量预测任务表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量预测', @parentId, 5, 'yieldForecast', 'agri/yieldForecast/index', '', 'AgriYieldForecast', 1, 0, 'C', '0', '0', 'agri:yieldForecast:list', 'chart', 'admin', sysdate(), '产量预测模型接入'
where not exists (
  select 1 from sys_menu where menu_name = '产量预测' and parent_id = @parentId
);

set @menuId = (select menu_id from sys_menu where menu_name = '产量预测' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量预测查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:yieldForecast:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:yieldForecast:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量预测新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:yieldForecast:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:yieldForecast:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量预测修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:yieldForecast:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:yieldForecast:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量预测删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:yieldForecast:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:yieldForecast:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量预测导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:yieldForecast:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:yieldForecast:export');

insert into sys_role_menu(role_id, menu_id)
select 1, m.menu_id
from sys_menu m
where m.menu_id = @menuId
  and not exists (select 1 from sys_role_menu rm where rm.role_id = 1 and rm.menu_id = m.menu_id);

insert into sys_role_menu(role_id, menu_id)
select 1, m.menu_id
from sys_menu m
where m.parent_id = @menuId and m.menu_type = 'F'
  and not exists (select 1 from sys_role_menu rm where rm.role_id = 1 and rm.menu_id = m.menu_id);

insert into agri_yield_forecast_task(plot_code, crop_name, season, sow_date, area_mu, forecast_yield_kg, model_version, forecast_status, forecast_time, status, remark, create_by)
select 'PLOT-A01', '番茄', '春季', '2026-03-01', 8.50, 3825.00, 'yield-model-v1', '1', now(), '0', '初始化样例：已预测', 'admin'
where not exists (select 1 from agri_yield_forecast_task where plot_code = 'PLOT-A01' and crop_name = '番茄' and season = '春季');

insert into agri_yield_forecast_task(plot_code, crop_name, season, sow_date, area_mu, model_version, forecast_status, status, remark, create_by)
select 'PLOT-A02', '黄瓜', '春季', '2026-03-05', 6.20, 'yield-model-v1', '0', '0', '初始化样例：待预测', 'admin'
where not exists (select 1 from agri_yield_forecast_task where plot_code = 'PLOT-A02' and crop_name = '黄瓜' and season = '春季');
