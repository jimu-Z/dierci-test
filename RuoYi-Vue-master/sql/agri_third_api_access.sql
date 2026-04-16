-- 第三方API接入
create table if not exists agri_third_party_api_access (
  access_id bigint(20) not null auto_increment comment '主键',
  access_code varchar(64) not null comment '接入编码',
  access_name varchar(100) not null comment '接入名称',
  api_type varchar(32) not null comment 'API类型',
  provider varchar(64) default null comment '供应商',
  endpoint_url varchar(255) not null comment '请求地址',
  timeout_sec int not null default 30 comment '超时(秒)',
  success_rate decimal(5,2) default 0 comment '成功率(%)',
  call_status char(1) not null default '0' comment '调用状态（0未调用 1成功 2失败）',
  last_call_time datetime default null comment '最近调用时间',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (access_id),
  unique key uk_access_code (access_code),
  key idx_api_type (api_type),
  key idx_call_status (call_status)
) engine=innodb default charset=utf8mb4 comment='第三方API接入表';

-- 字典：API类型
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '第三方API类型', 'agri_api_type', '0', 'admin', now(), '第三方API接入类型'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_api_type');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '天气服务', 'weather', 'agri_api_type', '', 'primary', 'Y', '0', 'admin', now(), '天气数据API'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_api_type' and dict_value = 'weather');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '市场行情', 'market', 'agri_api_type', '', 'success', 'N', '0', 'admin', now(), '市场行情API'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_api_type' and dict_value = 'market');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '物流查询', 'logistics', 'agri_api_type', '', 'warning', 'N', '0', 'admin', now(), '物流跟踪API'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_api_type' and dict_value = 'logistics');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 4, '地图服务', 'map', 'agri_api_type', '', 'info', 'N', '0', 'admin', now(), '高德地图API'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_api_type' and dict_value = 'map');

-- 字典：调用状态
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select 'API调用状态', 'agri_api_call_status', '0', 'admin', now(), '第三方API调用状态'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_api_call_status');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '未调用', '0', 'agri_api_call_status', '', 'info', 'Y', '0', 'admin', now(), '未调用'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_api_call_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '成功', '1', 'agri_api_call_status', '', 'success', 'N', '0', 'admin', now(), '调用成功'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_api_call_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '失败', '2', 'agri_api_call_status', '', 'danger', 'N', '0', 'admin', now(), '调用失败'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_api_call_status' and dict_value = '2');

-- 菜单
set @parent_id = (select menu_id from sys_menu where menu_name = '农业全链路追溯平台' and parent_id = 0 limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '第三方API接入', @parent_id, 31, 'thirdApi', 'agri/thirdApi/index', '', 'ThirdApi', 1, 0, 'C', '0', '0', 'agri:thirdApi:list', 'link', 'admin', now(), '第三方API接入菜单'
where not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'thirdApi'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'thirdApi' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '第三方API接入查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:thirdApi:query', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:thirdApi:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '第三方API接入新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:thirdApi:add', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:thirdApi:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '第三方API接入修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:thirdApi:edit', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:thirdApi:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '第三方API接入删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:thirdApi:remove', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:thirdApi:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '第三方API接入导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:thirdApi:export', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:thirdApi:export');
