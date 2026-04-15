-- 启元农链第二阶段：质检报告生成（P2-M2-F03）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 报告可由检测任务自动生成，也支持人工补录维护。

create table if not exists agri_quality_report (
  report_id          bigint(20)      not null auto_increment comment '主键',
  report_no          varchar(64)     not null comment '报告编号',
  inspect_id         bigint(20)      not null comment '检测任务ID',
  process_batch_no   varchar(64)     not null comment '加工批次号',
  quality_grade      varchar(20)     default null comment '品质等级',
  defect_rate        decimal(6,4)    default null comment '缺陷率',
  report_summary     varchar(1000)   default null comment '报告摘要',
  report_status      char(1)         default '0' comment '报告状态（0草稿 1已生成）',
  report_time        datetime        default null comment '报告时间',
  status             char(1)         default '0' comment '状态（0正常 1停用）',
  remark             varchar(500)    default null comment '备注',
  create_by          varchar(64)     default '' comment '创建者',
  create_time        datetime        default current_timestamp comment '创建时间',
  update_by          varchar(64)     default '' comment '更新者',
  update_time        datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (report_id),
  unique key uk_report_no (report_no),
  unique key uk_inspect_id (inspect_id),
  key idx_process_batch_no (process_batch_no)
) engine=innodb comment='启元农链质检报告表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '质检报告', @parentId, 8, 'qualityReport', 'agri/qualityReport/index', '', 'AgriQualityReport', 1, 0, 'C', '0', '0', 'agri:qualityReport:list', 'documentation', 'admin', sysdate(), '质检报告生成'
where not exists (select 1 from sys_menu where menu_name = '质检报告' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '质检报告' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '质检报告查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityReport:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:qualityReport:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '质检报告新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityReport:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:qualityReport:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '质检报告修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityReport:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:qualityReport:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '质检报告删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityReport:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:qualityReport:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '质检报告导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:qualityReport:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:qualityReport:export');

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

insert into agri_quality_report(report_no, inspect_id, process_batch_no, quality_grade, defect_rate, report_summary, report_status, report_time, status, remark, create_by)
select 'QCR-20260414-0001', 1, 'PROC-202604-A01', 'A', 0.0150, '批次样品整体品质良好，缺陷率处于可控范围。', '1', now(), '0', '初始化样例：已生成', 'admin'
where not exists (select 1 from agri_quality_report where report_no = 'QCR-20260414-0001');
