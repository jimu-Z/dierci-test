-- 启元农链第二阶段：病虫害识别模型接入（P2-M1-F03）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 当前为模型接入占位版本，回写接口用于模拟AI识别结果回传。

create table if not exists agri_pest_identify_task (
  task_id          bigint(20)      not null auto_increment comment '主键',
  plot_code        varchar(64)     not null comment '地块编码',
  crop_name        varchar(64)     not null comment '作物名称',
  image_url        varchar(500)    not null comment '图片URL',
  identify_status  char(1)         default '0' comment '识别状态（0待识别 1已识别 2失败）',
  identify_result  varchar(500)    default null comment '识别结果',
  confidence       decimal(6,4)    default null comment '置信度',
  identify_time    datetime        default null comment '识别时间',
  model_version    varchar(50)     default null comment '模型版本',
  status           char(1)         default '0' comment '状态（0正常 1停用）',
  remark           varchar(500)    default null comment '备注',
  create_by        varchar(64)     default '' comment '创建者',
  create_time      datetime        default current_timestamp comment '创建时间',
  update_by        varchar(64)     default '' comment '更新者',
  update_time      datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (task_id),
  key idx_plot_status (plot_code, identify_status),
  key idx_crop_status (crop_name, identify_status)
) engine=innodb comment='启元农链病虫害识别任务表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '病虫害识别', @parentId, 4, 'pestIdentify', 'agri/pestIdentify/index', '', 'AgriPestIdentify', 1, 0, 'C', '0', '0', 'agri:pestIdentify:list', 'bug', 'admin', sysdate(), '病虫害识别模型接入'
where not exists (
  select 1 from sys_menu where menu_name = '病虫害识别' and parent_id = @parentId
);

set @menuId = (select menu_id from sys_menu where menu_name = '病虫害识别' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '病虫害识别查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:pestIdentify:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:pestIdentify:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '病虫害识别新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:pestIdentify:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:pestIdentify:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '病虫害识别修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:pestIdentify:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:pestIdentify:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '病虫害识别删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:pestIdentify:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:pestIdentify:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '病虫害识别导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:pestIdentify:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:pestIdentify:export');

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

insert into agri_pest_identify_task(plot_code, crop_name, image_url, identify_status, identify_result, confidence, identify_time, model_version, status, remark, create_by)
select 'PLOT-A01', '番茄', 'https://example.com/image/tomato-1.jpg', '1', '疑似早疫病', 0.9230, now(), 'pest-model-v1', '0', '初始化样例：已识别', 'admin'
where not exists (select 1 from agri_pest_identify_task where plot_code = 'PLOT-A01' and crop_name = '番茄' and image_url = 'https://example.com/image/tomato-1.jpg');

insert into agri_pest_identify_task(plot_code, crop_name, image_url, identify_status, status, remark, create_by)
select 'PLOT-A02', '黄瓜', 'https://example.com/image/cucumber-1.jpg', '0', '0', '初始化样例：待识别', 'admin'
where not exists (select 1 from agri_pest_identify_task where plot_code = 'PLOT-A02' and crop_name = '黄瓜' and image_url = 'https://example.com/image/cucumber-1.jpg');
