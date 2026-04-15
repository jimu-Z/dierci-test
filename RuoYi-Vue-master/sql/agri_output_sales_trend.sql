-- 产量与销量趋势图
create table if not exists agri_output_sales_trend (
  trend_id bigint(20) not null auto_increment comment '主键',
  stat_date date not null comment '统计日期',
  output_value decimal(16,2) not null default 0 comment '产量(吨)',
  sales_value decimal(16,2) not null default 0 comment '销量(万元)',
  target_output decimal(16,2) not null default 0 comment '目标产量(吨)',
  target_sales decimal(16,2) not null default 0 comment '目标销量(万元)',
  output_mom_rate decimal(8,2) not null default 0 comment '产量环比增幅(%)',
  sales_mom_rate decimal(8,2) not null default 0 comment '销量环比增幅(%)',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (trend_id),
  unique key uk_stat_date (stat_date)
) engine=innodb default charset=utf8mb4 comment='产量与销量趋势图表';

-- 菜单
set @parent_id = (select menu_id from sys_menu where menu_name = '农业全链路追溯平台' and parent_id = 0 limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量与销量趋势图', @parent_id, 35, 'outputSalesTrend', 'agri/outputSalesTrend/index', '', 'OutputSalesTrend', 1, 0, 'C', '0', '0', 'agri:outputSalesTrend:list', 'trend-charts', 'admin', now(), '产量与销量趋势图菜单'
where not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'outputSalesTrend'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'outputSalesTrend' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量与销量趋势图查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:outputSalesTrend:query', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:outputSalesTrend:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量与销量趋势图新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:outputSalesTrend:add', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:outputSalesTrend:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量与销量趋势图修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:outputSalesTrend:edit', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:outputSalesTrend:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量与销量趋势图删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:outputSalesTrend:remove', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:outputSalesTrend:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '产量与销量趋势图导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:outputSalesTrend:export', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:outputSalesTrend:export');
