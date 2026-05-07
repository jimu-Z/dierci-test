-- 碳足迹核算模型
create table if not exists agri_carbon_footprint_model (
  model_id bigint(20) not null auto_increment comment '主键',
  model_code varchar(64) not null comment '模型编码',
  model_name varchar(100) not null comment '模型名称',
  product_type varchar(32) not null comment '产品类型',
  boundary_scope varchar(255) default null comment '核算边界',
  emission_factor decimal(16,4) not null default 0 comment '排放因子',
  carbon_emission decimal(16,4) default 0 comment '碳排放量(kgCO2e)',
  calc_status char(1) not null default '0' comment '核算状态（0待计算 1已计算 2已复核）',
  calc_time datetime default null comment '核算时间',
  verifier varchar(64) default null comment '复核人',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (model_id),
  unique key uk_model_code (model_code),
  key idx_product_type (product_type),
  key idx_calc_status (calc_status)
) engine=innodb default charset=utf8mb4 comment='碳足迹核算模型表';

-- 字典：产品类型
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '农产品类型', 'agri_product_type', '0', 'admin', now(), '碳足迹核算产品类型'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_product_type');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '蔬菜', 'vegetable', 'agri_product_type', '', 'success', 'Y', '0', 'admin', now(), '蔬菜类'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_product_type' and dict_value = 'vegetable');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '水果', 'fruit', 'agri_product_type', '', 'primary', 'N', '0', 'admin', now(), '水果类'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_product_type' and dict_value = 'fruit');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '粮食', 'grain', 'agri_product_type', '', 'warning', 'N', '0', 'admin', now(), '粮食类'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_product_type' and dict_value = 'grain');

-- 字典：核算状态
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '碳足迹核算状态', 'agri_calc_status', '0', 'admin', now(), '碳足迹核算状态'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_calc_status');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '待计算', '0', 'agri_calc_status', '', 'info', 'Y', '0', 'admin', now(), '模型待计算'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_calc_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '已计算', '1', 'agri_calc_status', '', 'success', 'N', '0', 'admin', now(), '模型已计算'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_calc_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '已复核', '2', 'agri_calc_status', '', 'primary', 'N', '0', 'admin', now(), '模型已复核'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_calc_status' and dict_value = '2');

-- 菜单
set @parent_id = (select menu_id from sys_menu where menu_name = '农业全链路追溯平台' and parent_id = 0 limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '碳足迹核算模型', @parent_id, 28, 'carbonFootprint', 'agri/carbonFootprint/index', '', 'CarbonFootprint', 1, 0, 'C', '0', '0', 'agri:carbonFootprint:list', 'guide', 'admin', now(), '碳足迹核算模型菜单'
where not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'carbonFootprint'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'carbonFootprint' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '碳足迹核算模型查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:carbonFootprint:query', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:carbonFootprint:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '碳足迹核算模型新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:carbonFootprint:add', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:carbonFootprint:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '碳足迹核算模型修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:carbonFootprint:edit', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:carbonFootprint:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '碳足迹核算模型删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:carbonFootprint:remove', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:carbonFootprint:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '碳足迹核算模型导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:carbonFootprint:export', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:carbonFootprint:export');
