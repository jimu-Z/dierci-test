-- 启元农链第三阶段：物流路径追踪（P3-M1-F01）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 支持物流事件上报、轨迹回放与状态查询。

create table if not exists agri_logistics_track (
  track_id           bigint(20)      not null auto_increment comment '主键',
  trace_code         varchar(64)     not null comment '运单号',
  order_no           varchar(64)     not null comment '销售订单号',
  product_batch_no   varchar(64)     not null comment '产品批次号',
  vehicle_no         varchar(64)     default null comment '车辆编号',
  driver_name        varchar(64)     default null comment '司机姓名',
  driver_phone       varchar(20)     default null comment '司机电话',
  start_location     varchar(128)    default null comment '起始地',
  current_location   varchar(128)    not null comment '当前位置',
  target_location    varchar(128)    default null comment '目的地',
  route_path         varchar(1000)   default null comment '路线轨迹',
  track_status       char(1)         default '0' comment '运输状态（0待发车 1运输中 2已签收 3异常）',
  event_desc         varchar(500)    default null comment '轨迹描述',
  event_time         datetime        not null comment '事件时间',
  temperature        decimal(6,2)    default null comment '温度(°C)',
  humidity           decimal(6,2)    default null comment '湿度(%)',
  longitude          decimal(10,6)   default null comment '经度',
  latitude           decimal(10,6)   default null comment '纬度',
  status             char(1)         default '0' comment '状态（0正常 1停用）',
  remark             varchar(500)    default null comment '备注',
  create_by          varchar(64)     default '' comment '创建者',
  create_time        datetime        default current_timestamp comment '创建时间',
  update_by          varchar(64)     default '' comment '更新者',
  update_time        datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (track_id),
  key idx_trace_time (trace_code, event_time),
  key idx_order_no (order_no)
) engine=innodb comment='启元农链物流路径追踪表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物流路径追踪', @parentId, 9, 'logisticsTrack', 'agri/logisticsTrack/index', '', 'AgriLogisticsTrack', 1, 0, 'C', '0', '0', 'agri:logisticsTrack:list', 'guide', 'admin', sysdate(), '物流路径追踪'
where not exists (select 1 from sys_menu where menu_name = '物流路径追踪' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '物流路径追踪' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物流路径追踪查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTrack:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsTrack:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物流路径追踪新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTrack:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsTrack:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物流路径追踪修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTrack:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsTrack:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物流路径追踪删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTrack:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsTrack:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物流路径追踪导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:logisticsTrack:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:logisticsTrack:export');

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

insert into agri_logistics_track(trace_code, order_no, product_batch_no, vehicle_no, driver_name, driver_phone, start_location, current_location, target_location, route_path, track_status, event_desc, event_time, temperature, humidity, longitude, latitude, status, remark, create_by)
select 'TRACE-202604150001', 'SO-20260415001', 'PROC-202604-A01', '鲁A9F321', '张强', '13800112233', '青岛基地', '青岛仓储中心', '上海门店A', '青岛基地->青岛仓储中心->上海门店A', '1', '车辆已到达青岛仓储中心，等待转运。', now(), 5.20, 62.50, 120.382640, 36.067108, '0', '初始化样例：运输中', 'admin'
where not exists (select 1 from agri_logistics_track where trace_code = 'TRACE-202604150001' and current_location = '青岛仓储中心');

insert into agri_logistics_track(trace_code, order_no, product_batch_no, vehicle_no, driver_name, driver_phone, start_location, current_location, target_location, route_path, track_status, event_desc, event_time, temperature, humidity, longitude, latitude, status, remark, create_by)
select 'TRACE-202604150001', 'SO-20260415001', 'PROC-202604-A01', '鲁A9F321', '张强', '13800112233', '青岛基地', '上海门店A', '上海门店A', '青岛基地->青岛仓储中心->上海门店A', '2', '货物已签收。', date_add(now(), interval 2 hour), 7.10, 58.40, 121.473700, 31.230400, '0', '初始化样例：已签收', 'admin'
where not exists (select 1 from agri_logistics_track where trace_code = 'TRACE-202604150001' and current_location = '上海门店A');
