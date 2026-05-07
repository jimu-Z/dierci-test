-- 农业金融风控指标
create table if not exists agri_finance_risk_metric (
  risk_id bigint(20) not null auto_increment comment '主键',
  indicator_code varchar(64) not null comment '指标编码',
  indicator_name varchar(100) not null comment '指标名称',
  risk_dimension varchar(32) not null comment '风险维度',
  risk_score decimal(10,2) not null default 0 comment '风险分值',
  threshold_value decimal(10,2) default 0 comment '阈值',
  risk_level char(1) not null default 'L' comment '风险等级（L低 M中 H高 C严重）',
  evaluate_status char(1) not null default '0' comment '评估状态（0待评估 1已评估 2已预警）',
  evaluate_time datetime default null comment '评估时间',
  evaluator varchar(64) default null comment '评估人',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (risk_id),
  unique key uk_indicator_code (indicator_code),
  key idx_risk_dimension (risk_dimension),
  key idx_risk_level (risk_level),
  key idx_evaluate_status (evaluate_status)
) engine=innodb default charset=utf8mb4 comment='农业金融风控指标表';

-- 字典：风险维度
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '风险维度', 'agri_risk_dimension', '0', 'admin', now(), '农业金融风控风险维度'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_risk_dimension');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '信用风险', 'credit', 'agri_risk_dimension', '', 'warning', 'Y', '0', 'admin', now(), '主体信用风险'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_risk_dimension' and dict_value = 'credit');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '经营风险', 'operation', 'agri_risk_dimension', '', 'primary', 'N', '0', 'admin', now(), '经营稳定性风险'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_risk_dimension' and dict_value = 'operation');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '合规风险', 'compliance', 'agri_risk_dimension', '', 'danger', 'N', '0', 'admin', now(), '政策与合规风险'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_risk_dimension' and dict_value = 'compliance');

-- 字典：风险等级
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '风险等级', 'agri_risk_level', '0', 'admin', now(), '农业金融风控风险等级'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_risk_level');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '低', 'L', 'agri_risk_level', '', 'info', 'Y', '0', 'admin', now(), '低风险'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_risk_level' and dict_value = 'L');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '中', 'M', 'agri_risk_level', '', 'primary', 'N', '0', 'admin', now(), '中风险'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_risk_level' and dict_value = 'M');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '高', 'H', 'agri_risk_level', '', 'warning', 'N', '0', 'admin', now(), '高风险'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_risk_level' and dict_value = 'H');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 4, '严重', 'C', 'agri_risk_level', '', 'danger', 'N', '0', 'admin', now(), '严重风险'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_risk_level' and dict_value = 'C');

-- 字典：评估状态
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '风险评估状态', 'agri_evaluate_status', '0', 'admin', now(), '农业金融风控评估状态'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_evaluate_status');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '待评估', '0', 'agri_evaluate_status', '', 'info', 'Y', '0', 'admin', now(), '待评估'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_evaluate_status' and dict_value = '0');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '已评估', '1', 'agri_evaluate_status', '', 'success', 'N', '0', 'admin', now(), '已评估'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_evaluate_status' and dict_value = '1');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '已预警', '2', 'agri_evaluate_status', '', 'danger', 'N', '0', 'admin', now(), '已预警'
where not exists (select 1 from sys_dict_data where dict_type = 'agri_evaluate_status' and dict_value = '2');

-- 菜单
set @parent_id = (select menu_id from sys_menu where menu_name = '农业全链路追溯平台' and parent_id = 0 limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农业金融风控指标', @parent_id, 29, 'financeRisk', 'agri/financeRisk/index', '', 'FinanceRisk', 1, 0, 'C', '0', '0', 'agri:financeRisk:list', 'money', 'admin', now(), '农业金融风控指标菜单'
where not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'financeRisk'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'financeRisk' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农业金融风控指标查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:financeRisk:query', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:financeRisk:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农业金融风控指标新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:financeRisk:add', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:financeRisk:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农业金融风控指标修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:financeRisk:edit', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:financeRisk:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农业金融风控指标删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:financeRisk:remove', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:financeRisk:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '农业金融风控指标导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:financeRisk:export', '#', 'admin', now(), ''
where not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:financeRisk:export');
