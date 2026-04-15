-- 预警信息汇总
create table if not exists agri_warning_summary (
  summary_id bigint(20) not null auto_increment comment '主键',
  summary_date date not null comment '统计日期',
  total_warning_count bigint(20) not null default 0 comment '预警总数',
  level1_count bigint(20) not null default 0 comment '提示级数量',
  level2_count bigint(20) not null default 0 comment '预警级数量',
  level3_count bigint(20) not null default 0 comment '严重级数量',
  handled_count bigint(20) not null default 0 comment '已处理数量',
  pending_count bigint(20) not null default 0 comment '待处理数量',
  avg_handle_minutes bigint(20) not null default 0 comment '平均处理时长(分钟)',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (summary_id),
  unique key uk_summary_date (summary_date)
) engine=innodb default charset=utf8mb4 comment='预警信息汇总表';

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
where m1.path = 'warningSummary'
  and ifnull(m1.component, '') = 'agri/warningSummary/index'
  and m1.menu_type = 'C';

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '预警信息汇总', @parent_id, 34, 'warningSummary', 'agri/warningSummary/index', '', 'WarningSummary', 1, 0, 'C', '0', '0', 'agri:warningSummary:list', 'warning', 'admin', now(), '预警信息汇总菜单'
where @parent_id is not null and not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'warningSummary'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'warningSummary' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '预警信息汇总查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:warningSummary:query', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:warningSummary:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '预警信息汇总新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:warningSummary:add', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:warningSummary:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '预警信息汇总修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:warningSummary:edit', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:warningSummary:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '预警信息汇总删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:warningSummary:remove', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:warningSummary:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '预警信息汇总导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:warningSummary:export', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:warningSummary:export');
