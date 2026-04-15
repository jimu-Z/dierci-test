-- 启元农链第四阶段：数据上链接口（P4-M2-F01）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 支持数据上链任务管理与一键执行上链。

create table if not exists agri_data_uplink_task (
  uplink_id            bigint(20)      not null auto_increment comment '主键',
  batch_no             varchar(64)     not null comment '业务批次号',
  data_type            varchar(32)     not null comment '数据类型',
  data_hash            varchar(128)    not null comment '数据哈希',
  chain_platform       varchar(32)     not null comment '链平台',
  contract_address     varchar(128)    default null comment '合约地址',
  tx_hash              varchar(128)    default null comment '交易哈希',
  uplink_status        char(1)         default '0' comment '上链状态（0待上链 1已上链 2失败）',
  uplink_time          datetime        default null comment '上链时间',
  status               char(1)         default '0' comment '状态（0正常 1停用）',
  remark               varchar(500)    default null comment '备注',
  create_by            varchar(64)     default '' comment '创建者',
  create_time          datetime        default current_timestamp comment '创建时间',
  update_by            varchar(64)     default '' comment '更新者',
  update_time          datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (uplink_id),
  key idx_batch_no (batch_no),
  key idx_data_type (data_type),
  key idx_uplink_status (uplink_status)
) engine=innodb comment='启元农链数据上链任务表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据上链接口', @parentId, 17, 'dataUplink', 'agri/dataUplink/index', '', 'AgriDataUplink', 1, 0, 'C', '0', '0', 'agri:dataUplink:list', 'guide', 'admin', sysdate(), '数据上链接口'
where not exists (select 1 from sys_menu where menu_name = '数据上链接口' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '数据上链接口' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据上链接口查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:dataUplink:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:dataUplink:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据上链接口新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:dataUplink:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:dataUplink:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据上链接口修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:dataUplink:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:dataUplink:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据上链接口删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:dataUplink:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:dataUplink:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据上链接口导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:dataUplink:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:dataUplink:export');

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

insert into agri_data_uplink_task(batch_no, data_type, data_hash, chain_platform, contract_address, tx_hash, uplink_status, uplink_time, status, remark, create_by)
select 'BATCH-LOGI-20260415-01', 'LOGISTICS_TRACE', 'e56f6f725f72756f79695f74726163655f686173683031', 'FISCO_BCOS', '0xabcdeffedcba1234567890abcdef1234567890ab', '0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff', '1', now(), '0', '初始化样例：已上链', 'admin'
where not exists (select 1 from agri_data_uplink_task where batch_no = 'BATCH-LOGI-20260415-01' and data_type = 'LOGISTICS_TRACE');

insert into agri_data_uplink_task(batch_no, data_type, data_hash, chain_platform, contract_address, uplink_status, status, remark, create_by)
select 'BATCH-QUALITY-20260415-02', 'QUALITY_REPORT', '715f7265706f72745f686173685f3230323630343135', 'HYPERCHAIN', '0x0987654321abcdef0987654321abcdef09876543', '0', '0', '初始化样例：待上链', 'admin'
where not exists (select 1 from agri_data_uplink_task where batch_no = 'BATCH-QUALITY-20260415-02' and data_type = 'QUALITY_REPORT');
