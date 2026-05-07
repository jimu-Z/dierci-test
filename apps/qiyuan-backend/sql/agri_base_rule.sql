-- 启元农链第一阶段：基础规则配置
-- 执行说明：
-- 1) 在若依数据库执行本脚本创建表与菜单。
-- 2) 给对应角色分配菜单与权限后可见前端页面。

-- ----------------------------
-- 1. 基础规则配置表
-- ----------------------------
drop table if exists agri_base_rule;
create table agri_base_rule (
  rule_id       bigint(20)      not null auto_increment comment '主键',
  rule_name     varchar(100)    not null comment '规则名称',
  rule_type     varchar(50)     not null comment '规则类型',
  rule_code     varchar(100)    not null comment '规则编码',
  rule_content  text            not null comment '规则内容',
  status        char(1)         default '0' comment '状态（0正常 1停用）',
  remark        varchar(500)    default null comment '备注',
  create_by     varchar(64)     default '' comment '创建者',
  create_time   datetime        default current_timestamp comment '创建时间',
  update_by     varchar(64)     default '' comment '更新者',
  update_time   datetime        default current_timestamp on update current_timestamp comment '更新时间',
  primary key (rule_id),
  unique key uk_rule_code (rule_code),
  key idx_rule_type (rule_type),
  key idx_status (status)
) engine=innodb comment='启元农链基础规则配置表';

-- ----------------------------
-- 2. 菜单与权限（若依）
-- ----------------------------
-- 父菜单：启元农链
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('启元农链', 0, 6, 'agri', null, '', '', 1, 0, 'M', '0', '0', '', 'leaf', 'admin', sysdate(), '启元农链目录');

-- 子菜单：基础规则配置
set @parentId = (select menu_id from sys_menu where menu_name = '启元农链' and parent_id = 0 order by menu_id desc limit 1);
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('基础规则配置', @parentId, 1, 'baseRule', 'agri/baseRule/index', '', 'AgriBaseRule', 1, 0, 'C', '0', '0', 'agri:baseRule:list', 'edit', 'admin', sysdate(), '基础规则配置菜单');

set @menuId = (select menu_id from sys_menu where menu_name = '基础规则配置' and parent_id = @parentId order by menu_id desc limit 1);

-- 按钮权限
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('基础规则配置查询', @menuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:baseRule:query', '#', 'admin', sysdate(), ''),
      ('基础规则配置新增', @menuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:baseRule:add', '#', 'admin', sysdate(), ''),
      ('基础规则配置修改', @menuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:baseRule:edit', '#', 'admin', sysdate(), ''),
      ('基础规则配置删除', @menuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:baseRule:remove', '#', 'admin', sysdate(), ''),
      ('基础规则配置导出', @menuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'agri:baseRule:export', '#', 'admin', sysdate(), '');

-- ----------------------------
-- 3. 初始数据（覆盖第一阶段六类配置）
-- ----------------------------
insert into agri_base_rule(rule_name, rule_type, rule_code, rule_content, status, remark, create_by)
values
('环境监测默认阈值', 'ENV_MONITOR', 'ENV_MONITOR_DEFAULT', '{"temperature":{"min":5,"max":35},"humidity":{"min":30,"max":90}}', '0', '第一阶段初始化', 'admin'),
('农事操作标准模板', 'FARM_OP_STANDARD', 'FARM_OP_STANDARD_V1', '{"mustRecord":["操作人","时间","地块","用量"]}', '0', '第一阶段初始化', 'admin'),
('品质分级规则模板', 'QUALITY_GRADE', 'QUALITY_GRADE_V1', '{"grade":["A","B","C"],"dimensions":["大小","色泽","完整度"]}', '0', '第一阶段初始化', 'admin'),
('物流路径模板', 'LOGISTICS_PATH', 'LOGISTICS_PATH_V1', '{"defaultRoute":["基地","加工厂","仓储","销售终端"]}', '0', '第一阶段初始化', 'admin'),
('市场分析模型配置', 'MARKET_MODEL', 'MARKET_MODEL_V1', '{"model":"baseline","windowDays":30}', '0', '第一阶段初始化', 'admin'),
('智能合约规则模板', 'SMART_CONTRACT', 'SMART_CONTRACT_V1', '{"onChainFields":["batchNo","qualityStatus","traceCode"]}', '0', '第一阶段初始化', 'admin');
