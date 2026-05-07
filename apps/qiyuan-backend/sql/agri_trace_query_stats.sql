-- 溯源查询统计
create table if not exists agri_trace_query_stats (
  stats_id bigint(20) not null auto_increment comment '主键',
  stat_date date not null comment '统计日期',
  total_query_count bigint(20) not null default 0 comment '查询总次数',
  unique_user_count bigint(20) not null default 0 comment '独立用户数',
  avg_duration_ms bigint(20) not null default 0 comment '平均响应时长(ms)',
  success_count bigint(20) not null default 0 comment '成功次数',
  fail_count bigint(20) not null default 0 comment '失败次数',
  peak_qps decimal(10,2) not null default 0 comment '峰值QPS',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (stats_id),
  unique key uk_stat_date (stat_date)
) engine=innodb default charset=utf8mb4 comment='溯源查询统计表';

-- 菜单
set @parent_id = (select menu_id from sys_menu where menu_name = '农业全链路追溯平台' and parent_id = 0 limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源查询统计', @parent_id, 36, 'traceQueryStats', 'agri/traceQueryStats/index', '', 'TraceQueryStats', 1, 0, 'C', '0', '0', 'agri:traceQueryStats:list', 'search', 'admin', now(), '溯源查询统计菜单'
where not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'traceQueryStats'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'traceQueryStats' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源查询统计查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:traceQueryStats:query', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:traceQueryStats:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源查询统计新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:traceQueryStats:add', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:traceQueryStats:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源查询统计修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:traceQueryStats:edit', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:traceQueryStats:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源查询统计删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:traceQueryStats:remove', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:traceQueryStats:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源查询统计导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:traceQueryStats:export', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:traceQueryStats:export');
