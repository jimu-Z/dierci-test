-- 启元农链第三阶段：品牌溯源页面（P3-M2-F02）
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表、菜单、权限与初始化数据。
-- 2) 支持品牌溯源页面配置、发布和按溯源码预览。

create table if not exists agri_brand_trace_page (
  page_id               bigint(20)      not null auto_increment comment '主键',
  trace_code            varchar(64)     not null comment '溯源码',
  brand_name            varchar(64)     not null comment '品牌名称',
  product_name          varchar(64)     not null comment '产品名称',
  product_code          varchar(64)     default null comment '产品编码',
  origin_place          varchar(128)    default null comment '产地',
  planting_batch_no     varchar(64)     default null comment '种植批次号',
  process_batch_no      varchar(64)     default null comment '加工批次号',
  logistics_trace_code  varchar(64)     default null comment '物流运单号',
  cover_image_url       varchar(500)    default null comment '封面图地址',
  page_url              varchar(500)    default null comment '详情页地址',
  qr_code_url           varchar(500)    default null comment '二维码地址',
  brand_story           varchar(2000)   default null comment '品牌故事',
  publish_status        char(1)         default '0' comment '发布状态（0草稿 1已发布）',
  status                char(1)         default '0' comment '状态（0正常 1停用）',
  remark                varchar(500)    default null comment '备注',
  create_by             varchar(64)     default '' comment '创建者',
  create_time           datetime        default current_timestamp comment '创建时间',
  update_by             varchar(64)     default '' comment '更新者',
  update_time           datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (page_id),
  unique key uk_trace_code (trace_code),
  key idx_publish_status (publish_status)
) engine=innodb comment='启元农链品牌溯源页面表';

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录'
where not exists (select 1 from sys_menu where menu_name = '启元农链' and parent_id = 0);

set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '品牌溯源页面', @parentId, 13, 'brandTrace', 'agri/brandTrace/index', '', 'AgriBrandTrace', 1, 0, 'C', '0', '0', 'agri:brandTrace:list', 'example', 'admin', sysdate(), '品牌溯源页面'
where not exists (select 1 from sys_menu where menu_name = '品牌溯源页面' and parent_id = @parentId);

set @menuId = (select menu_id from sys_menu where menu_name = '品牌溯源页面' and parent_id = @parentId order by menu_id desc limit 1);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '品牌溯源页面查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:brandTrace:query', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:brandTrace:query');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '品牌溯源页面新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:brandTrace:add', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:brandTrace:add');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '品牌溯源页面修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:brandTrace:edit', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:brandTrace:edit');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '品牌溯源页面删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:brandTrace:remove', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:brandTrace:remove');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '品牌溯源页面导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:brandTrace:export', '#', 'admin', sysdate(), ''
where not exists (select 1 from sys_menu where parent_id = @menuId and perms = 'agri:brandTrace:export');

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

insert into agri_brand_trace_page(trace_code, brand_name, product_name, product_code, origin_place, planting_batch_no, process_batch_no, logistics_trace_code, cover_image_url, page_url, qr_code_url, brand_story, publish_status, status, remark, create_by)
select 'TRACE-202607-APPLE-001', '启元优选', '富士苹果', 'PRD-APPLE-001', '山东烟台', 'PLANT-202603-A01', 'PROC-202604-A01', 'TRACE-202604150001', 'https://example.com/brand/apple-cover.jpg', 'https://example.com/trace/apple-001', 'https://example.com/qrcode/apple-001.png', '启元优选坚持从源头把控品质，构建种植、加工、物流全链路透明体系。', '1', '0', '初始化样例：已发布页面', 'admin'
where not exists (select 1 from agri_brand_trace_page where trace_code = 'TRACE-202607-APPLE-001');

insert into agri_brand_trace_page(trace_code, brand_name, product_name, product_code, origin_place, planting_batch_no, process_batch_no, logistics_trace_code, page_url, publish_status, status, remark, create_by)
select 'TRACE-202607-RICE-002', '启元优选', '优选大米', 'PRD-RICE-002', '黑龙江五常', 'PLANT-202604-B02', 'PROC-202605-B02', 'TRACE-202604150002', 'https://example.com/trace/rice-002', '0', '0', '初始化样例：草稿页面', 'admin'
where not exists (select 1 from agri_brand_trace_page where trace_code = 'TRACE-202607-RICE-002');
