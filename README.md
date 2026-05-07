# 启元农链

农业溯源与物联网数据平台（基于 RuoYi-Vue 二次开发）。

## 仓库结构

| 路径 | 说明 |
|------|------|
| [apps/qiyuan-backend](apps/qiyuan-backend/) | 后端 Maven 多模块，入口 `ruoyi-admin`；前端 `ruoyi-ui` |
| [docs/](docs/) | 设计文档、开发计划、ADR |
| [docs/runbooks/](docs/runbooks/) | 联调与运维手册（含 EMQX） |
| [db/baseline/](db/baseline/) | 基线数据库脚本 |
| [iot/emqx/](iot/emqx/) | 本地 EMQX Docker 示例 |

## 快速启动

详见根目录 [CLAUDE.md](CLAUDE.md)（命令、环境、模块说明）。

- 后端：`cd apps/qiyuan-backend` 后执行 `mvn -pl ruoyi-admin -am spring-boot:run` 或使用 `apps/qiyuan-backend/bin/run-dev.ps1`
- 前端：`cd apps/qiyuan-backend/ruoyi-ui` 后 `npm install` / `npm run dev`

## 架构说明

见 [docs/design/启元农链_目录重构与开发指南.md](docs/design/启元农链_目录重构与开发指南.md)。
