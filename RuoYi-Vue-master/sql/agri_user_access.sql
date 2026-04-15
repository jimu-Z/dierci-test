-- 启元农链第四阶段：用户权限管理（P4-M1-F01）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 支持用户权限申请记录管理与一键授权。

create table if not exists agri_user_access_grant (
  grant_id            bigint(20)      not null auto_increment comment '主键',
  user_name           varchar(64)     not null comment '用户账号',
  nick_name           varchar(64)     default null comment '用户昵称',
  role_key            varchar(100)    not null comment '角色权限标识',
  data_scope          varchar(32)     not null comment '数据权限范围',
  menu_scope          varchar(100)    default null comment '菜单可见范围',
  grant_status        char(1)         default '0' comment '授权状态（0待审核 1已授权 2已驳回）',
  status              char(1)         default '0' comment '状态（0正常 1停用）',
  remark              varchar(500)    default null comment '备注',
  create_by           varchar(64)     default '' comment '创建者',
  create_time         datetime        default current_timestamp comment '创建时间',
  update_by           varchar(64)     default '' comment '更新者',
  update_time         datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (grant_id),
  key idx_user_name (user_name),
  key idx_role_key (role_key),
  key idx_grant_status (grant_status)
) engine=innodb comment='启元农链用户权限管理表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '用户权限管理', @parentId, 15, 'userAccess', 'agri/userAccess/index', '', 'AgriUserAccess', 1, 0, 'C', '0', '0', 'agri:userAccess:list', 'peoples', 'admin', sysdate(), '用户权限管理'
where not exists (select 1 from sys_menu where menu_name = '用户权限管理' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '用户权限管理' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '用户权限管理查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:userAccess:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:userAccess:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '用户权限管理新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:userAccess:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:userAccess:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '用户权限管理修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:userAccess:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:userAccess:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '用户权限管理删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:userAccess:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:userAccess:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '用户权限管理导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:userAccess:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:userAccess:export');

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

insert into agri_user_access_grant(user_name, nick_name, role_key, data_scope, menu_scope, grant_status, status, remark, create_by)
select 'admin', '管理员', 'agri_admin', 'ALL', 'agri:*', '1', '0', '初始化样例：超级管理员已授权', 'admin'
where not exists (select 1 from agri_user_access_grant where user_name = 'admin' and role_key = 'agri_admin');

insert into agri_user_access_grant(user_name, nick_name, role_key, data_scope, menu_scope, grant_status, status, remark, create_by)
select 'agri_demo', '农业演示账号', 'agri_operator', 'DEPT', 'agri:baseRule,agri:marketForecast,agri:consumerScan', '0', '0', '初始化样例：待审核', 'admin'
where not exists (select 1 from agri_user_access_grant where user_name = 'agri_demo' and role_key = 'agri_operator');
