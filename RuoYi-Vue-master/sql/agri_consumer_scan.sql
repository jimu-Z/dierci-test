-- 启元农链第三阶段：消费者扫码查询（P3-M2-F03）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 支持消费者扫码查询日志管理与扫码模拟查询能力。

create table if not exists agri_consumer_scan_query (
  query_id           bigint(20)      not null auto_increment comment '主键',
  trace_code         varchar(64)     not null comment '溯源码',
  consumer_name      varchar(64)     default null comment '消费者姓名',
  consumer_phone     varchar(32)     default null comment '消费者手机号',
  scan_channel       varchar(32)     default 'WECHAT' comment '扫码渠道',
  scan_address       varchar(128)    default null comment '扫码地址',
  scan_ip            varchar(64)     default null comment '扫码IP',
  scan_result        char(1)         default '0' comment '查询结果（0未命中 1命中已发布 2命中未发布）',
  query_time         datetime        default current_timestamp comment '查询时间',
  status             char(1)         default '0' comment '状态（0正常 1停用）',
  remark             varchar(500)    default null comment '备注',
  create_by          varchar(64)     default '' comment '创建者',
  create_time        datetime        default current_timestamp comment '创建时间',
  update_by          varchar(64)     default '' comment '更新者',
  update_time        datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (query_id),
  key idx_trace_code (trace_code),
  key idx_query_time (query_time),
  key idx_scan_result (scan_result)
) engine=innodb comment='启元农链消费者扫码查询表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '消费者扫码查询', @parentId, 14, 'consumerScan', 'agri/consumerScan/index', '', 'AgriConsumerScan', 1, 0, 'C', '0', '0', 'agri:consumerScan:list', 'search', 'admin', sysdate(), '消费者扫码查询'
where not exists (select 1 from sys_menu where menu_name = '消费者扫码查询' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '消费者扫码查询' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '消费者扫码查询查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:consumerScan:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:consumerScan:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '消费者扫码查询新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:consumerScan:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:consumerScan:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '消费者扫码查询修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:consumerScan:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:consumerScan:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '消费者扫码查询删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:consumerScan:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:consumerScan:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '消费者扫码查询导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:consumerScan:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:consumerScan:export');

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

insert into agri_consumer_scan_query(trace_code, consumer_name, consumer_phone, scan_channel, scan_address, scan_ip, scan_result, query_time, status, remark, create_by)
select 'TRACE-202607-APPLE-001', '张三', '13800000001', 'WECHAT', '上海市浦东新区', '10.10.1.21', '1', now(), '0', '初始化样例：命中已发布', 'admin'
where not exists (select 1 from agri_consumer_scan_query where trace_code = 'TRACE-202607-APPLE-001' and consumer_phone = '13800000001');

insert into agri_consumer_scan_query(trace_code, consumer_name, consumer_phone, scan_channel, scan_address, scan_ip, scan_result, query_time, status, remark, create_by)
select 'TRACE-202607-RICE-002', '李四', '13900000002', 'ALIPAY', '广东省广州市', '10.10.2.22', '2', now(), '0', '初始化样例：命中未发布', 'admin'
where not exists (select 1 from agri_consumer_scan_query where trace_code = 'TRACE-202607-RICE-002' and consumer_phone = '13900000002');
