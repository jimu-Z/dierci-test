# 启元农链（CLAUDE / 协作说明）

> 本文件供开发者与 AI 助手快速对齐：**命令、规范、模块边界、IoT 本地环境**。  
> **请勿**将 Maven/控制台构建日志粘贴覆盖本文件；日志请放 `logs/` 或 CI 产物。

---

## 1. 仓库布局（目标）


| 路径                              | 内容                                     |
| ------------------------------- | -------------------------------------- |
| `apps/qiyuan-backend/`          | Spring Boot 多模块（若依二开），入口 `ruoyi-admin` |
| `apps/qiyuan-backend/ruoyi-ui/` | Vue2 管理端（npm）                          |
| `docs/`                         | 设计、计划、ADR                              |
| `docs/runbooks/`                | 联调手册（含 EMQX）                           |
| `db/baseline/`                  | 基线 SQL（如 `nongchanpin.sql`）            |
| `iot/emqx/`                     | 本地 EMQX docker-compose（可选，团队维护）        |


后端、前端与脚本路径以本节前表为准；业务增量 SQL 位于 `apps/qiyuan-backend/sql/`。

---

## 2. 环境要求

- **JDK**：21（`apps/qiyuan-backend/pom.xml` 与 enforcer 约束）
- **Maven**：3.9+
- **Node.js**：与 `ruoyi-ui/package.json` 生态兼容（建议 LTS）
- **MySQL**：与 `application-druid.yml` 一致
- **Redis**：本地默认 `localhost:6379`

---

## 3. 常用开发命令

**后端（Windows PowerShell 示例）：**

```powershell
cd F:\chuangye\apps\qiyuan-backend

mvn clean install -DskipTests
mvn -pl ruoyi-admin -am spring-boot:run
```

**前端：**

```powershell
cd F:\chuangye\apps\qiyuan-backend\ruoyi-ui
npm install
npm run dev
```

**仅编译校验：**

```powershell
mvn -q -DskipTests compile
```

---

## 4. 代码与分层规范（后端）


| 层级                        | 模块路径                                             | 约定                                              |
| ------------------------- | ------------------------------------------------ | ----------------------------------------------- |
| Controller                | `ruoyi-admin/.../controller/`、`controller/agri/` | REST、权限、分页；薄层                                   |
| Service / Mapper / Entity | `ruoyi-system/`                                  | 业务与持久化；MyBatis XML 在 `resources/mapper/system/` |
| 通用工具                      | `ruoyi-common/`                                  | 与业务无关的可复用代码                                     |
| 安全与基础设施                   | `ruoyi-framework/`                               | 谨慎修改，便于合并若依上游                                   |


**命名：** 农业业务实体/接口已与 `Agri`* 对齐；新增领域优先沿用 `Agri` 前缀或 `com.ruoyi.agri` 包（若团队统一）。

**API：** 遵循若依 `AjaxResult` / `TableDataInfo`；权限使用 `@PreAuthorize("@ss.hasPermi('模块:资源:操作')")`。

---

## 5. 模块依赖关系（Maven）

```text
ruoyi-admin
  → ruoyi-framework → ruoyi-system → ruoyi-common
  → ruoyi-quartz → ruoyi-common
  → ruoyi-generator → ruoyi-common
```

- **不要**让 `ruoyi-common` 依赖 `ruoyi-system` / `ruoyi-framework`（避免循环依赖）。
- 大型农业子域如需隔离，可新增 `ruoyi-agribiz` 模块（仅依赖 `ruoyi-common`），并在 `ruoyi-admin` 引入；需在 `docs/adr/` 记录决策。

---

## 6. EMQX 本地环境（推荐 Docker）

在 `iot/emqx/docker-compose.yml` 维护以下最小示例（若文件不存在则由团队创建）：

```yaml
services:
  emqx:
    image: emqx/emqx:5
    container_name: emqx-local
    ports:
      - "1883:1883"
      - "8083:8083"
      - "18083:18083"
    environment:
      EMQX_NAME: emqx
      EMQX_HOST: 127.0.0.1
```

启动：

```powershell
cd F:\chuangye\iot\emqx
docker compose up -d
```

- **控制台**：`http://127.0.0.1:18083`（默认账号密码以 EMQX 镜像说明为准）。
- **后端 Webhook 示例路径**：`POST http://127.0.0.1:8080/agri/envSensor/ingest/emqx`，Header `X-Agri-Token` 与配置项一致。  
详细 Topic、规则与样例见 `docs/runbooks/EMQX_阶段A_联调配置模板.md`。

---

## 7. 配置与安全提示

- 勿将生产数据库密码、EMQX 密钥、第三方 Key 提交入库；使用环境变量或本地 `application-local.yml`（gitignore）。
- 上传路径、Redis、数据源以 `apps/qiyuan-backend/ruoyi-admin/src/main/resources/application*.yml` 为准。

---

## 8. CI 建议（摘要）

- **backend**：`mvn -B -f apps/qiyuan-backend/pom.xml -DskipTests package`（见 `.github/workflows/backend-ci.yml`）  
- **frontend**：在 `apps/qiyuan-backend/ruoyi-ui` 下 `npm ci && npm run build:prod`  
- **SQL**：可选 `sqlfluff` 或人工评审 `db/migrations/`  
- 缓存：Maven `~/.m2`、npm `node_modules` 缓存加速

---

## 9. 文档入口

- 目录重构与上手路径：`docs/design/启元农链_目录重构与开发指南.md`

