-- 启元农链第三阶段：在途异常预警（P3-M1-F03）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 支持预警工单管理，并支持根据温湿度超阈值记录一键生成预警。

create table if not exists agri_logistics_warning (
  warning_id        bigint(20)      not null auto_increment comment '主键',
  trace_code        varchar(64)     not null comment '运单号',
  order_no          varchar(64)     not null comment '订单号',
  warning_type      varchar(50)     not null comment '预警类型',
  warning_level     char(1)         default '1' comment '预警级别（1一般 2严重 3紧急）',
  warning_status    char(1)         default '0' comment '预警状态（0待处理 1处理中 2已关闭）',
  source_record_id  bigint(20)      default null comment '来源记录ID（温湿度记录ID）',
  warning_title     varchar(100)    not null comment '预警标题',
  warning_content   varchar(1000)   not null comment '预警内容',
  warning_time      datetime        not null comment '预警时间',
  handler           varchar(64)     default null comment '处理人',
  handle_time       datetime        default null comment '处理时间',
  handle_remark     varchar(500)    default null comment '处理备注',
  status            char(1)         default '0' comment '状态（0正常 1停用）',
  remark            varchar(500)    default null comment '备注',
  create_by         varchar(64)     default '' comment '创建者',
  create_time       datetime        default current_timestamp comment '创建时间',
  update_by         varchar(64)     default '' comment '更新者',
  update_time       datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (warning_id),
  unique key uk_source_record_id (source_record_id),
  key idx_trace_warning_time (trace_code, warning_time),
  key idx_warning_status (warning_status)
) engine=innodb comment='启元农链在途异常预警表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '在途异常预警', @parentId, 11, 'logisticsWarning', 'agri/logisticsWarning/index', '', 'AgriLogisticsWarning', 1, 0, 'C', '0', '0', 'agri:logisticsWarning:list', 'warning', 'admin', sysdate(), '在途异常预警'
where not exists (select 1 from sys_menu where menu_name = '在途异常预警' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '在途异常预警' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '在途异常预警查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsWarning:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsWarning:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '在途异常预警新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsWarning:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsWarning:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '在途异常预警修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsWarning:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsWarning:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '在途异常预警删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsWarning:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsWarning:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '在途异常预警导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsWarning:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsWarning:export');

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

insert into agri_logistics_warning(trace_code, order_no, warning_type, warning_level, warning_status, source_record_id, warning_title, warning_content, warning_time, handler, handle_time, handle_remark, status, remark, create_by)
select 'TRACE-202604150001', 'SO-20260415001', 'TEMP_HUMIDITY', '2', '0', 2, '温湿度在途异常', '温度超过上限;湿度超过上限', now(), null, null, null, '0', '初始化样例：待处理预警', 'admin'
where not exists (select 1 from agri_logistics_warning where source_record_id = 2);

insert into agri_logistics_warning(trace_code, order_no, warning_type, warning_level, warning_status, source_record_id, warning_title, warning_content, warning_time, handler, handle_time, handle_remark, status, remark, create_by)
select 'TRACE-202604150001', 'SO-20260415001', 'TEMP_HUMIDITY', '1', '2', 1, '在途温控提示', '温湿度已恢复至正常区间。', date_sub(now(), interval 2 hour), 'admin', date_sub(now(), interval 1 hour), '已确认恢复正常，关闭预警。', '0', '初始化样例：已关闭预警', 'admin'
where not exists (select 1 from agri_logistics_warning where source_record_id = 1);
