-- 启元农链第二阶段：批次关联管理（P2-M2-F01）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 本模块用于建立种植批次与加工批次映射关系。

create table if not exists agri_process_batch_link (
  link_id             bigint(20)      not null auto_increment comment '主键',
  planting_batch_no   varchar(64)     not null comment '种植批次号',
  process_batch_no    varchar(64)     not null comment '加工批次号',
  product_code        varchar(64)     not null comment '产品编码',
  process_weight_kg   decimal(12,2)   not null comment '加工重量(kg)',
  process_status      char(1)         default '0' comment '加工状态（0待加工 1加工中 2已完成）',
  process_time        datetime        default null comment '加工时间',
  status              char(1)         default '0' comment '状态（0正常 1停用）',
  remark              varchar(500)    default null comment '备注',
  create_by           varchar(64)     default '' comment '创建者',
  create_time         datetime        default current_timestamp comment '创建时间',
  update_by           varchar(64)     default '' comment '更新者',
  update_time         datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (link_id),
  unique key uk_process_batch_no (process_batch_no),
  key idx_planting_batch_no (planting_batch_no),
  key idx_process_status (process_status)
) engine=innodb comment='启元农链加工批次关联表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '批次关联管理', @parentId, 6, 'processBatch', 'agri/processBatch/index', '', 'AgriProcessBatch', 1, 0, 'C', '0', '0', 'agri:processBatch:list', 'link', 'admin', sysdate(), '批次关联管理'
where not exists (select 1 from sys_menu where menu_name = '批次关联管理' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '批次关联管理' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '批次关联查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:processBatch:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:processBatch:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '批次关联新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:processBatch:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:processBatch:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '批次关联修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:processBatch:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:processBatch:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '批次关联删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:processBatch:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:processBatch:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '批次关联导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:processBatch:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:processBatch:export');

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

insert into agri_process_batch_link(planting_batch_no, process_batch_no, product_code, process_weight_kg, process_status, process_time, status, remark, create_by)
select 'PLANT-202604-A01', 'PROC-202604-A01', 'TOMATO-PROD-A', 3200.00, '2', now(), '0', '初始化样例：已完成', 'admin'
where not exists (select 1 from agri_process_batch_link where process_batch_no = 'PROC-202604-A01');

insert into agri_process_batch_link(planting_batch_no, process_batch_no, product_code, process_weight_kg, process_status, status, remark, create_by)
select 'PLANT-202604-A02', 'PROC-202604-A02', 'CUCUMBER-PROD-A', 2100.00, '1', '0', '初始化样例：加工中', 'admin'
where not exists (select 1 from agri_process_batch_link where process_batch_no = 'PROC-202604-A02');
