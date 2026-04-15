-- 启元农链第三阶段：温湿度监控（P3-M1-F02）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 支持在途温湿度记录管理，并自动判定是否超阈值告警。

create table if not exists agri_logistics_temp_humidity (
  record_id            bigint(20)      not null auto_increment comment '主键',
  trace_code           varchar(64)     not null comment '运单号',
  order_no             varchar(64)     not null comment '订单号',
  device_code          varchar(64)     not null comment '设备编码',
  collect_location     varchar(128)    default null comment '采集位置',
  temperature          decimal(6,2)    not null comment '温度(°C)',
  humidity             decimal(6,2)    not null comment '湿度(%)',
  temp_upper_limit     decimal(6,2)    default 8.00 comment '温度上限',
  temp_lower_limit     decimal(6,2)    default 2.00 comment '温度下限',
  humidity_upper_limit decimal(6,2)    default 75.00 comment '湿度上限',
  humidity_lower_limit decimal(6,2)    default 45.00 comment '湿度下限',
  alert_flag           char(1)         default '0' comment '告警标记（0正常 1超阈值）',
  alert_message        varchar(500)    default null comment '告警信息',
  collect_time         datetime        not null comment '采集时间',
  status               char(1)         default '0' comment '状态（0正常 1停用）',
  remark               varchar(500)    default null comment '备注',
  create_by            varchar(64)     default '' comment '创建者',
  create_time          datetime        default current_timestamp comment '创建时间',
  update_by            varchar(64)     default '' comment '更新者',
  update_time          datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (record_id),
  key idx_trace_collect_time (trace_code, collect_time),
  key idx_alert_flag (alert_flag)
) engine=innodb comment='启元农链物流温湿度监控表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '温湿度监控', @parentId, 10, 'logisticsTemp', 'agri/logisticsTemp/index', '', 'AgriLogisticsTemp', 1, 0, 'C', '0', '0', 'agri:logisticsTemp:list', 'dashboard', 'admin', sysdate(), '物流温湿度监控'
where not exists (select 1 from sys_menu where menu_name = '温湿度监控' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '温湿度监控' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '温湿度监控查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTemp:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsTemp:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '温湿度监控新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTemp:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsTemp:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '温湿度监控修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTemp:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsTemp:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '温湿度监控删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTemp:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsTemp:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '温湿度监控导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTemp:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsTemp:export');

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

insert into agri_logistics_temp_humidity(trace_code, order_no, device_code, collect_location, temperature, humidity, temp_upper_limit, temp_lower_limit, humidity_upper_limit, humidity_lower_limit, alert_flag, alert_message, collect_time, status, remark, create_by)
select 'TRACE-202604150001', 'SO-20260415001', 'IOT-TEMP-001', '青岛仓储中心', 5.20, 62.50, 8.00, 2.00, 75.00, 45.00, '0', '正常', now(), '0', '初始化样例：正常记录', 'admin'
where not exists (select 1 from agri_logistics_temp_humidity where trace_code = 'TRACE-202604150001' and device_code = 'IOT-TEMP-001' and alert_flag = '0');

insert into agri_logistics_temp_humidity(trace_code, order_no, device_code, collect_location, temperature, humidity, temp_upper_limit, temp_lower_limit, humidity_upper_limit, humidity_lower_limit, alert_flag, alert_message, collect_time, status, remark, create_by)
select 'TRACE-202604150001', 'SO-20260415001', 'IOT-TEMP-001', '上海门店A', 10.60, 80.30, 8.00, 2.00, 75.00, 45.00, '1', '温度超过上限;湿度超过上限', date_add(now(), interval 1 hour), '0', '初始化样例：超阈值告警', 'admin'
where not exists (select 1 from agri_logistics_temp_humidity where trace_code = 'TRACE-202604150001' and device_code = 'IOT-TEMP-001' and alert_flag = '1');
