-- 任务待办提醒
create table if not exists agri_todo_task_reminder (
  reminder_id bigint(20) not null auto_increment comment '主键',
  task_code varchar(64) not null comment '任务编码',
  task_title varchar(200) not null comment '任务标题',
  task_type varchar(64) default null comment '任务类型',
  priority_level char(1) not null default '2' comment '优先级（1低 2中 3高 4紧急）',
  assignee varchar(64) default null comment '处理人',
  deadline_time datetime not null comment '截止时间',
  reminder_status char(1) not null default '0' comment '提醒状态（0待提醒 1已提醒 2已完成）',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (reminder_id),
  unique key uk_task_code (task_code)
) engine=innodb default charset=utf8mb4 comment='任务待办提醒表';

-- 菜单
set @parent_id = (select menu_id from sys_menu where menu_name = '农业全链路追溯平台' and parent_id = 0 limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '任务待办提醒', @parent_id, 37, 'todoTaskReminder', 'agri/todoTaskReminder/index', '', 'TodoTaskReminder', 1, 0, 'C', '0', '0', 'agri:todoTaskReminder:list', 'message', 'admin', now(), '任务待办提醒菜单'
where not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'todoTaskReminder'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'todoTaskReminder' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '任务待办提醒查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:todoTaskReminder:query', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:todoTaskReminder:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '任务待办提醒新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:todoTaskReminder:add', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:todoTaskReminder:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '任务待办提醒修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:todoTaskReminder:edit', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:todoTaskReminder:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '任务待办提醒删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:todoTaskReminder:remove', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:todoTaskReminder:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '任务待办提醒导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:todoTaskReminder:export', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:todoTaskReminder:export');
