-- 设备状态监控
create table if not exists agri_device_status_monitor (
  monitor_id bigint(20) not null auto_increment comment '主键',
  device_code varchar(64) not null comment '设备编码',
  device_name varchar(100) not null comment '设备名称',
  device_type varchar(64) default null comment '设备类型',
  online_status char(1) not null default '1' comment '在线状态（0离线 1在线）',
  battery_level decimal(6,2) not null default 100 comment '电量(%)',
  signal_strength decimal(6,2) default 0 comment '信号强度',
  temperature decimal(6,2) default 0 comment '温度(℃)',
  humidity decimal(6,2) default 0 comment '湿度(%)',
  last_report_time datetime default null comment '最后上报时间',
  warning_level char(1) not null default '0' comment '预警等级（0正常 1提示 2预警 3严重）',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (monitor_id),
  unique key uk_device_code (device_code)
) engine=innodb default charset=utf8mb4 comment='设备状态监控表';

-- 菜单
set @parent_id = (select menu_id from sys_menu where menu_name = '农业全链路追溯平台' and parent_id = 0 limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备状态监控', @parent_id, 33, 'deviceStatusMonitor', 'agri/deviceStatusMonitor/index', '', 'DeviceStatusMonitor', 1, 0, 'C', '0', '0', 'agri:deviceStatusMonitor:list', 'monitor', 'admin', now(), '设备状态监控菜单'
where not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'deviceStatusMonitor'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'deviceStatusMonitor' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备状态监控查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:deviceStatusMonitor:query', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:deviceStatusMonitor:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备状态监控新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:deviceStatusMonitor:add', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:deviceStatusMonitor:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备状态监控修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:deviceStatusMonitor:edit', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:deviceStatusMonitor:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备状态监控删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:deviceStatusMonitor:remove', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:deviceStatusMonitor:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '设备状态监控导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:deviceStatusMonitor:export', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:deviceStatusMonitor:export');
