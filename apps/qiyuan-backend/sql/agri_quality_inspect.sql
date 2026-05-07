-- 启元农链第二阶段：视觉品质检测（P2-M2-F02）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 当前为模型接入占位版本，支持检测结果回写。

create table if not exists agri_quality_inspect_task (
  inspect_id         bigint(20)      not null auto_increment comment '主键',
  process_batch_no   varchar(64)     not null comment '加工批次号',
  sample_code        varchar(64)     not null comment '样品编码',
  image_url          varchar(500)    not null comment '图片URL',
  inspect_status     char(1)         default '0' comment '检测状态（0待检测 1已检测 2失败）',
  quality_grade      varchar(20)     default null comment '品质等级',
  defect_rate        decimal(6,4)    default null comment '缺陷率',
  inspect_result     varchar(500)    default null comment '检测结果',
  inspect_time       datetime        default null comment '检测时间',
  model_version      varchar(50)     default null comment '模型版本',
  status             char(1)         default '0' comment '状态（0正常 1停用）',
  remark             varchar(500)    default null comment '备注',
  create_by          varchar(64)     default '' comment '创建者',
  create_time        datetime        default current_timestamp comment '创建时间',
  update_by          varchar(64)     default '' comment '更新者',
  update_time        datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (inspect_id),
  key idx_process_status (process_batch_no, inspect_status),
  key idx_sample_code (sample_code)
) engine=innodb comment='启元农链视觉品质检测任务表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '视觉品质检测', @parentId, 7, 'qualityInspect', 'agri/qualityInspect/index', '', 'AgriQualityInspect', 1, 0, 'C', '0', '0', 'agri:qualityInspect:list', 'camera', 'admin', sysdate(), '视觉品质检测'
where not exists (select 1 from sys_menu where menu_name = '视觉品质检测' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '视觉品质检测' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '视觉品质检测查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityInspect:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:qualityInspect:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '视觉品质检测新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityInspect:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:qualityInspect:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '视觉品质检测修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityInspect:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:qualityInspect:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '视觉品质检测删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityInspect:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:qualityInspect:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '视觉品质检测导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityInspect:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:qualityInspect:export');

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

insert into agri_quality_inspect_task(process_batch_no, sample_code, image_url, inspect_status, quality_grade, defect_rate, inspect_result, inspect_time, model_version, status, remark, create_by)
select 'PROC-202604-A01', 'SAMPLE-A01-01', 'https://example.com/inspect/a01-01.jpg', '1', 'A', 0.0150, '外观完整，色泽良好', now(), 'vision-model-v1', '0', '初始化样例：已检测', 'admin'
where not exists (select 1 from agri_quality_inspect_task where sample_code = 'SAMPLE-A01-01');

insert into agri_quality_inspect_task(process_batch_no, sample_code, image_url, inspect_status, model_version, status, remark, create_by)
select 'PROC-202604-A02', 'SAMPLE-A02-01', 'https://example.com/inspect/a02-01.jpg', '0', 'vision-model-v1', '0', '初始化样例：待检测', 'admin'
where not exists (select 1 from agri_quality_inspect_task where sample_code = 'SAMPLE-A02-01');
