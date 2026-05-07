-- 启元农链第三阶段：市场预测分析（P3-M2-F01）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 支持市场销量与价格预测任务管理，并支持一键执行预测回写。

create table if not exists agri_market_forecast (
  forecast_id         bigint(20)      not null auto_increment comment '主键',
  market_area         varchar(64)     not null comment '市场区域',
  product_code        varchar(64)     not null comment '产品编码',
  product_name        varchar(64)     not null comment '产品名称',
  period_start        date            not null comment '预测周期开始',
  period_end          date            not null comment '预测周期结束',
  historical_sales_kg decimal(12,2)   default null comment '历史销量(kg)',
  forecast_sales_kg   decimal(12,2)   default null comment '预测销量(kg)',
  forecast_price      decimal(10,2)   default null comment '预测价格(元/kg)',
  confidence_rate     decimal(6,4)    default null comment '置信度',
  model_version       varchar(50)     default null comment '模型版本',
  forecast_status     char(1)         default '0' comment '预测状态（0待预测 1已预测 2失败）',
  forecast_time       datetime        default null comment '预测时间',
  status              char(1)         default '0' comment '状态（0正常 1停用）',
  remark              varchar(500)    default null comment '备注',
  create_by           varchar(64)     default '' comment '创建者',
  create_time         datetime        default current_timestamp comment '创建时间',
  update_by           varchar(64)     default '' comment '更新者',
  update_time         datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (forecast_id),
  key idx_market_period (market_area, period_start, period_end),
  key idx_forecast_status (forecast_status)
) engine=innodb comment='启元农链市场预测分析表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '市场预测分析', @parentId, 12, 'marketForecast', 'agri/marketForecast/index', '', 'AgriMarketForecast', 1, 0, 'C', '0', '0', 'agri:marketForecast:list', 'trend-charts', 'admin', sysdate(), '市场预测分析'
where not exists (select 1 from sys_menu where menu_name = '市场预测分析' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '市场预测分析' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '市场预测分析查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:marketForecast:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:marketForecast:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '市场预测分析新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:marketForecast:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:marketForecast:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '市场预测分析修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:marketForecast:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:marketForecast:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '市场预测分析删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:marketForecast:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:marketForecast:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '市场预测分析导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:marketForecast:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:marketForecast:export');

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

insert into agri_market_forecast(market_area, product_code, product_name, period_start, period_end, historical_sales_kg, forecast_sales_kg, forecast_price, confidence_rate, model_version, forecast_status, forecast_time, status, remark, create_by)
select '华东大区', 'PRD-APPLE-001', '富士苹果', '2026-07-01', '2026-07-31', 12850.00, 13878.00, 8.80, 0.8200, 'market-model-v1', '1', now(), '0', '初始化样例：已预测', 'admin'
where not exists (select 1 from agri_market_forecast where market_area = '华东大区' and product_code = 'PRD-APPLE-001' and period_start = '2026-07-01');

insert into agri_market_forecast(market_area, product_code, product_name, period_start, period_end, historical_sales_kg, model_version, forecast_status, status, remark, create_by)
select '华南大区', 'PRD-RICE-002', '优选大米', '2026-07-01', '2026-07-31', 22500.00, 'market-model-v1', '0', '0', '初始化样例：待预测', 'admin'
where not exists (select 1 from agri_market_forecast where market_area = '华南大区' and product_code = 'PRD-RICE-002' and period_start = '2026-07-01');
