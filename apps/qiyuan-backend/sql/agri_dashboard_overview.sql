-- 数据总览看板
create table if not exists agri_dashboard_overview (
  overview_id bigint(20) not null auto_increment comment '主键',
  stat_date date not null comment '统计日期',
  total_output decimal(16,2) not null default 0 comment '总产量(吨)',
  total_sales decimal(16,2) not null default 0 comment '总销量(万元)',
  trace_query_count bigint(20) not null default 0 comment '溯源查询次数',
  warning_count bigint(20) not null default 0 comment '预警数量',
  online_device_count bigint(20) not null default 0 comment '在线设备数',
  pending_task_count bigint(20) not null default 0 comment '待办数量',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (overview_id),
  unique key uk_stat_date (stat_date)
) engine=innodb default charset=utf8mb4 comment='数据总览看板表';

-- 菜单
set @parent_id = (select menu_id from sys_menu where menu_name = '农业全链路追溯平台' and parent_id = 0 limit 1);

-- 清理历史重复菜单（优先保留挂在平台目录下的记录）
delete m1 from sys_menu m1
inner join sys_menu m2
  on m1.path = m2.path
 and ifnull(m1.component, '') = ifnull(m2.component, '')
 and m1.menu_type = m2.menu_type
 and (
      (ifnull(m1.parent_id, 0) = 0 and ifnull(m2.parent_id, 0) <> 0)
      or (ifnull(m1.parent_id, 0) = ifnull(m2.parent_id, 0) and m1.menu_id > m2.menu_id)
 )
where m1.path = 'dashboardOverview'
  and ifnull(m1.component, '') = 'agri/dashboardOverview/index'
  and m1.menu_type = 'C';

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据总览看板', @parent_id, 32, 'dashboardOverview', 'agri/dashboardOverview/index', '', 'DashboardOverview', 1, 0, 'C', '0', '0', 'agri:dashboardOverview:list', 'dashboard', 'admin', now(), '数据总览看板菜单'
where @parent_id is not null and not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'dashboardOverview'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'dashboardOverview' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据总览看板查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:dashboardOverview:query', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:dashboardOverview:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据总览看板新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:dashboardOverview:add', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:dashboardOverview:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据总览看板修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:dashboardOverview:edit', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:dashboardOverview:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据总览看板删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:dashboardOverview:remove', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:dashboardOverview:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据总览看板导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:dashboardOverview:export', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:dashboardOverview:export');
