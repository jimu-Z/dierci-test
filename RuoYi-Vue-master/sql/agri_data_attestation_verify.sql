-- 数据存证与校验
create table if not exists agri_data_attestation_verify (
  verify_id bigint(20) not null auto_increment comment '主键',
  attestation_no varchar(64) not null comment '存证编号',
  batch_no varchar(64) not null comment '业务批次号',
  data_type varchar(32) not null comment '数据类型',
  origin_hash varchar(128) not null comment '原始哈希',
  chain_hash varchar(128) default null comment '链上哈希',
  verify_status char(1) not null default '0' comment '校验状态（0待校验 1一致 2不一致）',
  verify_time datetime default null comment '校验时间',
  verify_by_user varchar(64) default null comment '校验人',
  status char(1) not null default '0' comment '状态（0正常 1停用）',
  remark varchar(500) default null comment '备注',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
  primary key (verify_id),
  unique key uk_attestation_no (attestation_no),
  key idx_batch_no (batch_no),
  key idx_verify_status (verify_status)
) engine=innodb default charset=utf8mb4 comment='数据存证与校验表';

-- 字典：数据类型
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '农业数据类型', 'agri_data_type', '0', 'admin', now(), '农业场景数据类型'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_data_type');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '生产数据', 'production', 'agri_data_type', '', 'primary', 'Y', '0', 'admin', now(), '生产记录'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_data_type' and dict_value = 'production'
);

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '加工数据', 'processing', 'agri_data_type', '', 'success', 'N', '0', 'admin', now(), '加工记录'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_data_type' and dict_value = 'processing'
);

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '流通数据', 'logistics', 'agri_data_type', '', 'warning', 'N', '0', 'admin', now(), '流通记录'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_data_type' and dict_value = 'logistics'
);

-- 字典：校验状态
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
select '存证校验状态', 'agri_verify_status', '0', 'admin', now(), '数据存证校验结果状态'
where not exists (select 1 from sys_dict_type where dict_type = 'agri_verify_status');

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 1, '待校验', '0', 'agri_verify_status', '', 'info', 'Y', '0', 'admin', now(), '待比对'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_verify_status' and dict_value = '0'
);

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 2, '一致', '1', 'agri_verify_status', '', 'success', 'N', '0', 'admin', now(), '校验一致'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_verify_status' and dict_value = '1'
);

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
select 3, '不一致', '2', 'agri_verify_status', '', 'danger', 'N', '0', 'admin', now(), '校验不一致'
where not exists (
  select 1 from sys_dict_data where dict_type = 'agri_verify_status' and dict_value = '2'
);

-- 菜单
set @parent_id = (select menu_id from sys_menu where menu_name = '农业全链路追溯平台' and parent_id = 0 limit 1);

-- 清理历史重复菜单（优先保留挂在平台目录下的记录）
delete m1 from sys_menu m1
inner join sys_menu m2
  on m1.path = m2.path
 and ifnull(m1.component, '') = ifnull(m2.component, '')
 and m1.menu_type = m2.menu_type
 and (
      (ifnull(m1.parent_id, 0) = 0 and ifnull(m2.parent_id, 0) <> 0)
      or (ifnull(m1.parent_id, 0) = ifnull(m2.parent_id, 0) and m1.menu_id > m2.menu_id)
 )
where m1.path = 'attestationVerify'
  and ifnull(m1.component, '') = 'agri/attestationVerify/index'
  and m1.menu_type = 'C';

insert into sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据存证与校验', @parent_id, 26, 'attestationVerify', 'agri/attestationVerify/index', '', 'AttestationVerify', 1, 0, 'C', '0', '0', 'agri:attestationVerify:list', 'documentation', 'admin', now(), '数据存证与校验菜单'
where @parent_id is not null and not exists (
  select 1 from sys_menu where parent_id = @parent_id and path = 'attestationVerify'
);

set @menu_id = (select menu_id from sys_menu where parent_id = @parent_id and path = 'attestationVerify' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据存证与校验查询', @menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'agri:attestationVerify:query', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:attestationVerify:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据存证与校验新增', @menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'agri:attestationVerify:add', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:attestationVerify:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据存证与校验修改', @menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'agri:attestationVerify:edit', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:attestationVerify:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据存证与校验删除', @menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'agri:attestationVerify:remove', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:attestationVerify:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '数据存证与校验导出', @menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'agri:attestationVerify:export', '#', 'admin', now(), ''
where @menu_id is not null and not exists (select 1 from sys_menu where parent_id = @menu_id and perms = 'agri:attestationVerify:export');
