-- 启元农链第四阶段：设备接入管理（P4-M1-F02）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 支持设备节点接入记录管理与一键激活。

create table if not exists agri_device_access_node (
  node_id              bigint(20)      not null auto_increment comment '主键',
  device_code          varchar(64)     not null comment '设备编码',
  device_name          varchar(64)     not null comment '设备名称',
  device_type          varchar(32)     not null comment '设备类型',
  protocol_type        varchar(32)     not null comment '接入协议',
  firmware_version     varchar(32)     default null comment '固件版本',
  bind_area            varchar(128)    default null comment '绑定区域',
  access_status        char(1)         default '0' comment '接入状态（0待接入 1在线 2离线 3异常）',
  last_online_time     datetime        default null comment '最近在线时间',
  status               char(1)         default '0' comment '状态（0正常 1停用）',
  remark               varchar(500)    default null comment '备注',
  create_by            varchar(64)     default '' comment '创建者',
  create_time          datetime        default current_timestamp comment '创建时间',
  update_by            varchar(64)     default '' comment '更新者',
  update_time          datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (node_id),
  unique key uk_device_code (device_code),
  key idx_device_type (device_type),
  key idx_access_status (access_status)
) engine=innodb comment='启元农链设备接入管理表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备接入管理', @parentId, 16, 'deviceAccess', 'agri/deviceAccess/index', '', 'AgriDeviceAccess', 1, 0, 'C', '0', '0', 'agri:deviceAccess:list', 'monitor', 'admin', sysdate(), '设备接入管理'
where not exists (select 1 from sys_menu where menu_name = '设备接入管理' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '设备接入管理' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备接入管理查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:deviceAccess:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:deviceAccess:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备接入管理新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:deviceAccess:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:deviceAccess:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备接入管理修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:deviceAccess:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:deviceAccess:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备接入管理删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:deviceAccess:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:deviceAccess:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备接入管理导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:deviceAccess:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:deviceAccess:export');

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

insert into agri_device_access_node(device_code, device_name, device_type, protocol_type, firmware_version, bind_area, access_status, status, remark, create_by)
select 'DEV-TEMP-001', '冷链温湿度传感器-1', 'TEMP_HUMIDITY', 'MQTT', 'v1.2.0', '山东烟台基地A区', '1', '0', '初始化样例：在线设备', 'admin'
where not exists (select 1 from agri_device_access_node where device_code = 'DEV-TEMP-001');

insert into agri_device_access_node(device_code, device_name, device_type, protocol_type, firmware_version, bind_area, access_status, status, remark, create_by)
select 'DEV-GW-002', '边缘网关-2', 'GATEWAY', 'HTTP', 'v2.0.1', '黑龙江五常加工厂', '0', '0', '初始化样例：待接入设备', 'admin'
where not exists (select 1 from agri_device_access_node where device_code = 'DEV-GW-002');
