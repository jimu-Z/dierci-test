# 启元农链 — 协作与命令速查

面向人类开发者与 AI 助手：路径约定、常用命令、勿把构建日志贴进本文件。

## 仓库路径

| 路径 | 说明 |
|------|------|
| `apps/qiyuan-backend/` | 若依后端 Maven 多模块（入口 `ruoyi-admin`） |
| `frontend/admin-web/` | 管理端前端（原 `ruoyi-ui`，阶段 B 已迁入） |
| `docs/design/`、`docs/runbooks/` | 设计说明、联调手册 |
| `db/baseline/` | 基线 SQL |
| `iot/emqx/` | 本地 EMQX Docker 示例 |
| `deploy/jimuyu.me/` | 家用宽带：Nginx、Cloudflare Tunnel、启动脚本 |

## 环境版本

- **后端**：JDK **21**（见 `apps/qiyuan-backend/pom.xml`）、Maven 3.8+、MySQL、Redis  
- **前端**：Node LTS（建议 18+），工程在 `frontend/admin-web`

## 常用命令（PowerShell）

```powershell
# 后端编译并运行（在 apps/qiyuan-backend）
mvn -pl ruoyi-admin -am spring-boot:run

# 或本地脚本（EMQX 联调环境变量见 bin/run-dev-emqx.ps1）
cd apps\qiyuan-backend\bin
.\run-dev.ps1

# 前端开发
cd frontend\admin-web
npm install
npm run dev

# 前端生产构建（Nginx 的 root 指向本目录下的 dist）
npm run build:prod
```

## 农业业务代码放哪

见 [docs/design/启元农链_目录重构与开发指南.md](docs/design/启元农链_目录重构与开发指南.md) 第 2 节（Controller 在 `ruoyi-admin/.../agri/`，领域在 `ruoyi-system`）。

## EMQX Webhook（联调）

- 本地：`POST http://127.0.0.1:8080/agri/envSensor/ingest/emqx`，Header：`X-Agri-Token`  
- 公网与隧道：见 [deploy/jimuyu.me/README.md](deploy/jimuyu.me/README.md) 与 [docs/runbooks/EMQX_阶段A_联调配置模板.md](docs/runbooks/EMQX_阶段A_联调配置模板.md)

## 说明

- 个人密钥、`paths.bat`、`ingest-token.txt` 勿提交；见 `deploy/jimuyu.me/launchers/.gitignore`。  
- **Nginx** 若曾把 `root` 指到 `apps/qiyuan-backend/ruoyi-ui/dist`，请改为 **`frontend/admin-web/dist`** 后 `nginx -s reload`。  
- 进度类大表以实际交付为准；归档型计划见 `docs/design/启元农链_项目开发计划文档.md` 文首提示。
