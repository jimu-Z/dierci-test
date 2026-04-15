-- 农业模块菜单重复诊断与清理脚本（加强版）
-- 说明：按“诊断 -> 清理 -> 复查”顺序执行。

-- A1) 诊断：目录/页面菜单重复（M/C）
select
  menu_type,
  path,
  ifnull(component, '') as component,
  count(1) as duplicate_count,
  group_concat(menu_id order by menu_id) as menu_ids
from sys_menu
where menu_type in ('M', 'C')
  and (
    ifnull(component, '') like 'agri/%'
    or path in ('agri', 'dashboardOverview', 'warningSummary', 'attestationVerify', 'auditLog')
  )
group by menu_type, path, ifnull(component, '')
having count(1) > 1
order by duplicate_count desc, path;

-- A2) 诊断：按钮权限重复（F）
select
  perms,
  count(1) as duplicate_count,
  group_concat(menu_id order by menu_id) as menu_ids
from sys_menu
where menu_type = 'F'
  and perms like 'agri:%'
group by perms
having count(1) > 1
order by duplicate_count desc, perms;

-- A3) 诊断：业务页面菜单挂在根节点（parent_id=0）
select
  menu_id,
  menu_name,
  parent_id,
  path,
  ifnull(component, '') as component,
  menu_type,
  perms
from sys_menu
where menu_type = 'C'
  and ifnull(component, '') like 'agri/%'
  and ifnull(parent_id, 0) = 0
order by menu_id;

-- B1) 清理：目录/页面菜单去重（M/C）
-- 规则：优先保留非根节点；同层级保留最小menu_id
delete m1 from sys_menu m1
inner join sys_menu m2
  on m1.menu_type = m2.menu_type
 and m1.path = m2.path
 and ifnull(m1.component, '') = ifnull(m2.component, '')
 and m1.menu_type in ('M', 'C')
 and (
    ifnull(m1.component, '') like 'agri/%'
    or m1.path in ('agri', 'dashboardOverview', 'warningSummary', 'attestationVerify', 'auditLog')
 )
 and (
      (ifnull(m1.parent_id, 0) = 0 and ifnull(m2.parent_id, 0) <> 0)
      or (ifnull(m1.parent_id, 0) = ifnull(m2.parent_id, 0) and m1.menu_id > m2.menu_id)
 );

-- B2) 清理：按钮权限去重（F）
-- 规则：同一perms仅保留一条，优先保留挂在非根节点的按钮，次选menu_id更小
delete m1 from sys_menu m1
inner join sys_menu m2
  on m1.menu_type = 'F'
 and m2.menu_type = 'F'
 and ifnull(m1.perms, '') = ifnull(m2.perms, '')
 and ifnull(m1.perms, '') like 'agri:%'
 and (
      (ifnull(m1.parent_id, 0) = 0 and ifnull(m2.parent_id, 0) <> 0)
      or (ifnull(m1.parent_id, 0) = ifnull(m2.parent_id, 0) and m1.menu_id > m2.menu_id)
 );

-- C1) 复查：目录/页面菜单重复（M/C）
select
  menu_type,
  path,
  ifnull(component, '') as component,
  count(1) as duplicate_count,
  group_concat(menu_id order by menu_id) as menu_ids
from sys_menu
where menu_type in ('M', 'C')
  and (
    ifnull(component, '') like 'agri/%'
    or path in ('agri', 'dashboardOverview', 'warningSummary', 'attestationVerify', 'auditLog')
  )
group by menu_type, path, ifnull(component, '')
having count(1) > 1
order by duplicate_count desc, path;

-- C2) 复查：按钮权限重复（F）
select
  perms,
  count(1) as duplicate_count,
  group_concat(menu_id order by menu_id) as menu_ids
from sys_menu
where menu_type = 'F'
  and perms like 'agri:%'
group by perms
having count(1) > 1
order by duplicate_count desc, perms;
