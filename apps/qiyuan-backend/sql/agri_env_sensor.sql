-- 启元农链第二阶段：环境传感器数据接入（P2-M1-F01）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 若已存在“启元农链”目录菜单，脚本会复用该目录。

-- ----------------------------
-- 1. 环境传感器数据表
-- ----------------------------
create table if not exists agri_env_sensor_record (
  record_id      bigint(20)      not null auto_increment comment '主键',
  device_code    varchar(64)     not null comment '设备编码',
  plot_code      varchar(64)     not null comment '地块编码',
  temperature    decimal(8,2)    default null comment '温度(℃)',
  humidity       decimal(8,2)    default null comment '湿度(%)',
  co2_value      decimal(10,2)   default null comment '二氧化碳浓度(ppm)',
  status         char(1)         default '0' comment '状态（0正常 1预警）',
  data_source    varchar(32)     default 'manual' comment '数据来源',
  collect_time   datetime        not null comment '采集时间',
  remark         varchar(500)    default null comment '备注',
  create_by      varchar(64)     default '' comment '创建者',
  create_time    datetime        default current_timestamp comment '创建时间',
  update_by      varchar(64)     default '' comment '更新者',
  update_time    datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (record_id),
  key idx_device_collect_time (device_code, collect_time),
  key idx_plot_collect_time (plot_code, collect_time),
  key idx_status (status)
) engine=innodb comment='启元农链环境传感器数据表';

-- ----------------------------
-- 2. 菜单与权限（若依）
-- ----------------------------
set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '环境传感数据', @parentId, 2, 'envSensor', 'agri/envSensor/index', '', 'AgriEnvSensor', 1, 0, 'C', '0', '0', 'agri:envSensor:list', 'monitor', 'admin', sysdate(), '环境传感器数据接入'
where not exists (
  select 1 from sys_menu where menu_name = '环境传感数据' and parent_id = @parentId
);

set @menuId = (select menu_id from sys_menu where menu_name = '环境传感数据' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '环境传感数据查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:envSensor:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:envSensor:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '环境传感数据新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:envSensor:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:envSensor:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '环境传感数据修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:envSensor:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:envSensor:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '环境传感数据删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:envSensor:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:envSensor:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '环境传感数据导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:envSensor:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:envSensor:export');

-- 绑定管理员角色（role_id=1）菜单，避免菜单不可见
insert into sys_role_menu(role_id, menu_id)
select 1, m.menu_id
from sys_menu m
where m.menu_id = @menuId
  and not exists (
    select 1 from sys_role_menu rm where rm.role_id = 1 and rm.menu_id = m.menu_id
  );

insert into sys_role_menu(role_id, menu_id)
select 1, m.menu_id
from sys_menu m
where m.parent_id = @menuId
  and m.menu_type = 'F'
  and not exists (
    select 1 from sys_role_menu rm where rm.role_id = 1 and rm.menu_id = m.menu_id
  );

-- ----------------------------
-- 3. 初始化数据
-- ----------------------------
insert into agri_env_sensor_record(device_code, plot_code, temperature, humidity, co2_value, status, data_source, collect_time, remark, create_by)
select 'DEV-001', 'PLOT-A01', 24.50, 61.20, 460.00, '0', 'mqtt-gateway-01', now(), '初始化样例数据', 'admin'
where not exists (select 1 from agri_env_sensor_record where device_code = 'DEV-001' and plot_code = 'PLOT-A01');

insert into agri_env_sensor_record(device_code, plot_code, temperature, humidity, co2_value, status, data_source, collect_time, remark, create_by)
select 'DEV-002', 'PLOT-A02', 33.10, 84.60, 720.00, '1', 'mqtt-gateway-01', now(), '高温高湿预警样例', 'admin'
where not exists (select 1 from agri_env_sensor_record where device_code = 'DEV-002' and plot_code = 'PLOT-A02');
