-- 启元农链第二阶段：农事记录管理（P2-M1-F02）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 若已存在“启元农链”目录菜单，脚本会复用该目录。

-- ----------------------------
-- 1. 农事记录表
-- ----------------------------
create table if not exists agri_farm_operation_record (
  operation_id      bigint(20)      not null auto_increment comment '主键',
  plot_code         varchar(64)     not null comment '地块编码',
  operation_type    varchar(50)     not null comment '作业类型',
  operation_content varchar(500)    not null comment '作业内容',
  operator_name     varchar(64)     not null comment '作业人',
  input_name        varchar(100)    default null comment '投入品名称',
  input_amount      decimal(10,2)   default null comment '投入品用量',
  input_unit        varchar(20)     default null comment '用量单位',
  operation_time    datetime        not null comment '作业时间',
  status            char(1)         default '0' comment '状态（0正常 1停用）',
  remark            varchar(500)    default null comment '备注',
  create_by         varchar(64)     default '' comment '创建者',
  create_time       datetime        default current_timestamp comment '创建时间',
  update_by         varchar(64)     default '' comment '更新者',
  update_time       datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (operation_id),
  key idx_plot_time (plot_code, operation_time),
  key idx_type_time (operation_type, operation_time),
  key idx_status (status)
) engine=innodb comment='启元农链农事记录表';

-- ----------------------------
-- 2. 菜单与权限（若依）
-- ----------------------------
set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农事记录管理', @parentId, 3, 'farmOp', 'agri/farmOp/index', '', 'AgriFarmOperation', 1, 0, 'C', '0', '0', 'agri:farmOp:list', 'form', 'admin', sysdate(), '农事记录管理'
where not exists (
  select 1 from sys_menu where menu_name = '农事记录管理' and parent_id = @parentId
);

set @menuId = (select menu_id from sys_menu where menu_name = '农事记录管理' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农事记录查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:farmOp:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:farmOp:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农事记录新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:farmOp:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:farmOp:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农事记录修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:farmOp:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:farmOp:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农事记录删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:farmOp:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:farmOp:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农事记录导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:farmOp:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:farmOp:export');

-- 绑定管理员角色（role_id=1）菜单
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
insert into agri_farm_operation_record(plot_code, operation_type, operation_content, operator_name, input_name, input_amount, input_unit, operation_time, status, remark, create_by)
select 'PLOT-A01', 'FERTILIZATION', '春季追肥', '张三', '有机肥', 12.50, 'kg', now(), '0', '初始化样例数据', 'admin'
where not exists (
  select 1 from agri_farm_operation_record
  where plot_code = 'PLOT-A01' and operation_type = 'FERTILIZATION' and operation_content = '春季追肥'
);

insert into agri_farm_operation_record(plot_code, operation_type, operation_content, operator_name, input_name, input_amount, input_unit, operation_time, status, remark, create_by)
select 'PLOT-A02', 'PEST_CONTROL', '病虫害防治喷洒', '李四', '生物农药', 3.20, 'L', now(), '0', '初始化样例数据', 'admin'
where not exists (
  select 1 from agri_farm_operation_record
  where plot_code = 'PLOT-A02' and operation_type = 'PEST_CONTROL' and operation_content = '病虫害防治喷洒'
);
