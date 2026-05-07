-- 溯源审计日志
create table if not exists agri_trace_audit_log (
  audit_id bigint(20) not null auto_increment comment '主键',
  biz_no varchar(64) not null comment '业务编号',
  module_name varchar(64) not null comment '模块名称',
  action_type varchar(32) not null comment '操作类型',
  operator_name varchar(64) default null comment '操作人',
  operate_time datetime default null comment '操作时间',
  operate_result char(1) not null default '1' comment '操作结果（0失败 1成功）',
  tx_hash varchar(128) default null comment '交易哈希',
  ip_address varchar(64) default null comment 'IP地址',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (audit_id),
  key idx_biz_no (biz_no),
  key idx_action_type (action_type),
  key idx_operate_result (operate_result)
) engine=innodb default charset=utf8mb4 comment='溯源审计日志表';

-- 字典：操作类型
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '审计操作类型', 'agri_audit_action_type', '0', 'admin', now(), '溯源审计操作类型'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_audit_action_type');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '创建', 'create', 'agri_audit_action_type', '', 'primary', 'Y', '0', 'admin', now(), '新增记录'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_audit_action_type' and dict_value = 'create'
);

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '更新', 'update', 'agri_audit_action_type', '', 'warning', 'N', '0', 'admin', now(), '修改记录'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_audit_action_type' and dict_value = 'update'
);

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '校验', 'verify', 'agri_audit_action_type', '', 'success', 'N', '0', 'admin', now(), '校验记录'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_audit_action_type' and dict_value = 'verify'
);

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 4, '删除', 'delete', 'agri_audit_action_type', '', 'danger', 'N', '0', 'admin', now(), '删除记录'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_audit_action_type' and dict_value = 'delete'
);

-- 字典：操作结果
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '审计操作结果', 'agri_audit_result', '0', 'admin', now(), '溯源审计操作结果'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_audit_result');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '成功', '1', 'agri_audit_result', '', 'success', 'Y', '0', 'admin', now(), '操作成功'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_audit_result' and dict_value = '1'
);

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '失败', '0', 'agri_audit_result', '', 'danger', 'N', '0', 'admin', now(), '操作失败'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_audit_result' and dict_value = '0'
);

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
where m1.path = 'auditLog'
  and ifnull(m1.component, '') = 'agri/auditLog/index'
  and m1.menu_type = 'C';

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源审计日志', @parent_id, 27, 'auditLog', 'agri/auditLog/index', '', 'AuditLog', 1, 0, 'C', '0', '0', 'agri:auditLog:list', 'clipboard', 'admin', now(), '溯源审计日志菜单'
where @parent_id is not null and not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'auditLog'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'auditLog' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源审计日志查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:auditLog:query', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:auditLog:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源审计日志新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:auditLog:add', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:auditLog:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源审计日志修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:auditLog:edit', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:auditLog:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源审计日志删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:auditLog:remove', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:auditLog:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '溯源审计日志导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:auditLog:export', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:auditLog:export');
