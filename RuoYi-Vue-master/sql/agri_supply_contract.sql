-- 供应链金融合约管理
create table if not exists agri_supply_chain_contract (
  contract_id bigint(20) not null auto_increment comment '主键',
  contract_no varchar(64) not null comment '合约编号',
  contract_name varchar(100) not null comment '合约名称',
  finance_subject varchar(100) not null comment '融资主体',
  finance_amount decimal(16,2) not null default 0 comment '融资金额',
  interest_rate decimal(8,4) default 0 comment '利率(%)',
  start_date date default null comment '起始日期',
  end_date date default null comment '到期日期',
  contract_status char(1) not null default '0' comment '合约状态（0草稿 1生效 2到期 3终止）',
  risk_level char(1) not null default 'L' comment '风控等级（L低 M中 H高 C严重）',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (contract_id),
  unique key uk_contract_no (contract_no),
  key idx_contract_status (contract_status),
  key idx_risk_level (risk_level)
) engine=innodb default charset=utf8mb4 comment='供应链金融合约管理表';

-- 字典：合约状态
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '供应链合约状态', 'agri_contract_status', '0', 'admin', now(), '供应链金融合约状态'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_contract_status');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '草稿', '0', 'agri_contract_status', '', 'info', 'Y', '0', 'admin', now(), '草稿状态'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_contract_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '生效', '1', 'agri_contract_status', '', 'success', 'N', '0', 'admin', now(), '生效状态'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_contract_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '到期', '2', 'agri_contract_status', '', 'warning', 'N', '0', 'admin', now(), '到期状态'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_contract_status' and dict_value = '2');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 4, '终止', '3', 'agri_contract_status', '', 'danger', 'N', '0', 'admin', now(), '终止状态'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_contract_status' and dict_value = '3');

-- 菜单
set @parent_id = (select menu_id from sys_menu where menu_name = '农业全链路追溯平台' and parent_id = 0 limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '供应链金融合约管理', @parent_id, 30, 'supplyContract', 'agri/supplyContract/index', '', 'SupplyContract', 1, 0, 'C', '0', '0', 'agri:supplyContract:list', 'documentation', 'admin', now(), '供应链金融合约管理菜单'
where not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'supplyContract'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'supplyContract' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '供应链金融合约查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:supplyContract:query', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:supplyContract:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '供应链金融合约新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:supplyContract:add', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:supplyContract:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '供应链金融合约修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:supplyContract:edit', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:supplyContract:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '供应链金融合约删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:supplyContract:remove', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:supplyContract:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '供应链金融合约导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:supplyContract:export', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:supplyContract:export');
