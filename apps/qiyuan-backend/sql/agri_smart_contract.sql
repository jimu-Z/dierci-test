-- 启元农链第四阶段：智能合约部署（P4-M2-F02）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 支持智能合约部署任务管理与一键执行部署。

create table if not exists agri_smart_contract_deploy (
  deploy_id             bigint(20)      not null auto_increment comment '主键',
  contract_name         varchar(64)     not null comment '合约名称',
  contract_version      varchar(32)     not null comment '合约版本',
  chain_platform        varchar(32)     not null comment '链平台',
  source_hash           varchar(128)    not null comment '源码哈希',
  abi_json              text            default null comment 'ABI',
  contract_address      varchar(128)    default null comment '合约地址',
  deploy_tx_hash        varchar(128)    default null comment '部署交易哈希',
  deploy_status         char(1)         default '0' comment '部署状态（0待部署 1已部署 2失败）',
  deploy_time           datetime        default null comment '部署时间',
  status                char(1)         default '0' comment '状态（0正常 1停用）',
  remark                varchar(500)    default null comment '备注',
  create_by             varchar(64)     default '' comment '创建者',
  create_time           datetime        default current_timestamp comment '创建时间',
  update_by             varchar(64)     default '' comment '更新者',
  update_time           datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (deploy_id),
  key idx_contract_name (contract_name),
  key idx_chain_platform (chain_platform),
  key idx_deploy_status (deploy_status)
) engine=innodb comment='启元农链智能合约部署表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '智能合约部署', @parentId, 18, 'smartContract', 'agri/smartContract/index', '', 'AgriSmartContract', 1, 0, 'C', '0', '0', 'agri:smartContract:list', 'cpu', 'admin', sysdate(), '智能合约部署'
where not exists (select 1 from sys_menu where menu_name = '智能合约部署' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '智能合约部署' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '智能合约部署查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:smartContract:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:smartContract:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '智能合约部署新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:smartContract:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:smartContract:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '智能合约部署修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:smartContract:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:smartContract:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '智能合约部署删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:smartContract:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:smartContract:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '智能合约部署导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:smartContract:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:smartContract:export');

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

insert into agri_smart_contract_deploy(contract_name, contract_version, chain_platform, source_hash, abi_json, contract_address, deploy_tx_hash, deploy_status, deploy_time, status, remark, create_by)
select 'TraceStore', 'v1.0.0', 'FISCO_BCOS', '74726163655f73746f72655f736f757263655f68617368', '[{"inputs":[{"name":"traceCode","type":"string"}],"name":"storeTrace","outputs":[],"stateMutability":"nonpayable","type":"function"}]', '0x1234567890abcdef1234567890abcdef12345678', '0xabcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890', '1', now(), '0', '初始化样例：已部署', 'admin'
where not exists (select 1 from agri_smart_contract_deploy where contract_name = 'TraceStore' and contract_version = 'v1.0.0');

insert into agri_smart_contract_deploy(contract_name, contract_version, chain_platform, source_hash, abi_json, deploy_status, status, remark, create_by)
select 'QualityAudit', 'v1.1.0', 'HYPERCHAIN', '7175616c6974795f61756469745f736f757263655f68617368', '[{"inputs":[{"name":"reportNo","type":"string"}],"name":"storeAudit","outputs":[],"stateMutability":"nonpayable","type":"function"}]', '0', '0', '初始化样例：待部署', 'admin'
where not exists (select 1 from agri_smart_contract_deploy where contract_name = 'QualityAudit' and contract_version = 'v1.1.0');
