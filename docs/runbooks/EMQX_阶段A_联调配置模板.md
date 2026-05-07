# EMQX 阶段A联调配置模板

## 1. 用途

本模板用于你在 EMQX 控制台完成 A2 后，快速配置 webhook 上行到本系统并完成首轮联调。

## 1.1 使用原则

- 这里只保留联调必需项：回调地址、Header、Topic 规范、消息样例和排查步骤。
- 临时公网地址仅用于短期验证，正式环境必须替换为稳定域名。
- 联调完成后，记得把最终回调结论同步回项目开发计划文档。

## 2. 回调目标

- 方法：POST
- URL：`https://<你的公网基址>/agri/envSensor/ingest/emqx`（公网基址须与后端配置一致，见 2.2）
- Content-Type：application/json
- Header：
  - `X-Agri-Token: <你的AGRI_SENSOR_INGEST_TOKEN>`

### 2.1 管理端查看「当前应配置的 Webhook」

登录系统后（需 `agri:envSensor:query` 权限），请求：

`GET /agri/envSensor/integration/emqx-webhook`

返回 JSON 中含 `webhookUrl`、`authHeaderName`、`ingestTokenConfigured`、`publicWebhookValidUntil` 等字段，可直接对照填入 EMQX 控制台。**勿**将该接口对公网匿名开放。

### 2.2 公网基址与「一年期临时域名」约定

1. **环境变量（推荐）**
  - `AGRI_EMQX_PUBLIC_WEBHOOK_BASE_URL`：公网 **HTTPS 基址**，不含路径。例如 `https://emqx-hook.yourcompany.com`。  
  - `AGRI_EMQX_WEBHOOK_VALID_UNTIL`：计划下线日期，默认 `2027-05-06`（便于联调台账与续约提醒，与 SSL/域名账单无关时需自行维护）。  
  - `AGRI_SENSOR_INGEST_TOKEN`：**必须**设为强随机串；EMQX Webhook 请求头携带 `X-Agri-Token: <同值>`。
2. **一年期临时域名（实施方式，非代码自动生成）**
  代码无法替你注册真实 DNS。推荐流程：在任意注册商购买 **1 年期** 最便宜域名 → 将 `emqx-hook`（或任意子域）**CNAME** 到云厂商 **负载均衡 / API 网关 / Cloudflare Tunnel** 给出的目标 → 网关反代到本机 `http://127.0.0.1:8080`。详细示例见仓库 `deploy/emqx-tunnel/README.md`。
3. **未配置公网基址时**
  `GET .../integration/emqx-webhook` 会用**当前请求的协议+主机+端口**拼出 `webhookUrl`，仅适用于本机或内网验收；EMQX 云端规则必须使用 2.2 中的公网 HTTPS 地址。

### 2.3 极短期隧道（仅小时级验证）

- 本地回环：`http://127.0.0.1:8080/agri/envSensor/ingest/emqx`
- 临时隧道（loca.lt / ngrok 等）：**进程结束即失效**，不适合作为「一年期」方案；与 2.2 正式域名互补使用。

## 3. 推荐 Topic 规范

- 上报Topic：`agri/{plotCode}/{deviceCode}/telemetry`
- 示例：`agri/PLOT-A01/DEV-TEMP-001/telemetry`

## 4. 推荐 EMQX Rule 输出JSON（示例）

```json
{
  "topic": "${topic}",
  "clientid": "${clientid}",
  "timestamp": "${timestamp}",
  "payload": ${payload}
}
```

说明：

- 代码已支持从 `payload` / `data` / 根对象读取常见字段。
- 若规则引擎把 `payload` 以 **JSON 字符串**形式放进 HTTP 体（而非对象），后端会尝试 **parse** 后再读温湿度等字段。
- Topic 为 `agri/{plotCode}/{deviceCode}/telemetry` 时，即使 payload 未带地块/设备编码，也会从 Topic 解析出 `plotCode` 与 `deviceCode`（与手册第 3 节一致）。
- 支持字段别名：
  - 设备：`deviceCode` / `device_code` / `deviceId` / `clientid`
  - 地块：`plotCode` / `plot_code`
  - 温度：`temperature` / `temp`
  - 湿度：`humidity` / `hum`
  - CO2：`co2` / `co2Value` / `co2_value`
  - 时间：`timestamp` / `time` / `ts`
  如果你的设备上报结构更偏向嵌套对象，也可以直接把原始 payload 透传，后端会优先从 `payload`、`data` 或根对象读取字段。

## 5. 设备消息体示例（设备侧发布）

```json
{
  "deviceCode": "DEV-TEMP-001",
  "plotCode": "PLOT-A01",
  "temperature": 26.3,
  "humidity": 63.8,
  "co2": 512,
  "timestamp": 1776300000
}
```

```json
{
  "device_code": "DEV-HUM-002",
  "plot_code": "PLOT-A02",
  "temp": 28.1,
  "hum": 71.4,
  "co2Value": 498,
  "time": 1776300000123
}
```

## 6. 首次联调检查

1. EMQX Rule 命中数增长。
2. Webhook 2xx 成功率 >= 99%。
3. 本系统数据库出现新传感记录，`data_source` 为 `emqx-webhook`。
4. 设备在线状态被回写为在线；若为首次上报的设备，**设备接入管理**中会出现自动建档记录（备注含「EMQX Webhook 自动建档」）。
5. 超阈值时记录状态自动置为预警。
6. 设备列表与轨迹/告警页面能看到最新上报时间。

### 6.1 快速验证命令

- 本地直连回调：`curl -X POST http://127.0.0.1:8080/agri/envSensor/ingest/emqx -H "Content-Type: application/json" -H "X-Agri-Token: <token>" -d '{"deviceCode":"DEV-TEMP-001","plotCode":"PLOT-A01","temperature":26.3,"humidity":63.8,"co2":512,"timestamp":1776300000}'`
- 如果返回 200，说明后端接收链路和 token 校验已通。

## 7. 常见问题排查

- 401/令牌无效：检查 `X-Agri-Token` 是否与服务端环境变量一致。
- 设备未注册：**仅**非 EMQX 的 `POST /agri/envSensor/ingest`（gateway-http）会要求台账已登记；EMQX Webhook `POST .../ingest/emqx` 会对**未知设备编码自动建档**并联调写入在线状态。若仍报错，多半是 `deviceCode`/`plotCode` 仍解析不到（Topic 不规范或 Body 模板未传 `topic`/`payload`）。
- 地块或设备为空：检查 payload 是否包含字段，或 Topic 是否遵循推荐规范。
- 时间异常：优先传 Unix 秒级或毫秒级时间戳。
- 503/隧道不可用：检查 localtunnel 是否仍在运行，或切换到本地回环地址验证。
- 规则命中但未入库：检查 EMQX 输出 JSON 中 `payload` 是否被正确传递，必要时直接透传原始消息体。

## 8. 安全提醒

- 不要在聊天工具发送账号密码或长期密钥。
- 若口令曾外泄，请立即重置并启用多因素认证。

## 9. 点击式验收清单（EMQX + 管理端闭环）

以下按顺序点即可完成验收。

### 9.1 前置确认

1. **后端**：`apps/qiyuan-backend/bin/run-dev-emqx.ps1`，且 `AGRI_SENSOR_INGEST_TOKEN`、`AGRI_EMQX_PUBLIC_WEBHOOK_BASE_URL` 已就绪（与环境变量一节一致）。
2. **Tunnel**：本机运行 `cloudflared tunnel run <你的隧道名>`，且 `hook` 域名能打开后端根路径提示页。
3. **EMQX**：数据集成里规则 **已启用**，动作指向 `https://.../agri/envSensor/ingest/emqx`，Header 含 `X-Agri-Token`。

### 9.2 EMQX 控制台

1. 进入 **项目管理** → 选中部署 → 左侧 **数据集成**。
2. 点开你的规则进入 **运行统计**。
3. 用 **在线调试** 或实体设备向 Topic `agri/{plotCode}/{deviceCode}/telemetry` 发一条 JSON 测试消息。
4. 确认统计里 **「通过」「动作成功」** 计数增加且 **失败为 0**。

### 9.3 管理端——设备台账

1. 浏览器打开（开发环境通常为）`http://localhost` 并登录。
2. **首页快捷入口** → 点 **「设备接入管理」**（系统会跳转到 `/agri/deviceAccess`）。
3. 列表搜索框输入本次测试用的 **设备编码**（须与 Topic 第三段或与 payload 中 `deviceCode` 一致）。
4. **期望**：能看到一条新记录；**设备名称**形如 `EMQX/<设备编码>`；**协议**为 MQTT；**设备类型**为温湿度传感器；**备注**含「首次 EMQX Webhook 上报自动建档」；**接入状态**为在线。

### 9.4 管理端——传感记录

如有菜单 **「环境传感监测」**（路径一般为 `/agri/envSensor`），打开列表按时间倒序查看最新一条。

若无菜单，已在登录态下可用浏览器控制台：

```javascript
fetch('/dev-api/agri/envSensor/list?pageNum=1&pageSize=10', {
  headers: { Authorization: 'Bearer ' + localStorage.getItem('Admin-Token') }
}).then(r => r.json()).then(console.log)
```

**期望**：最新记录 `data_source` 为 **`emqx-webhook`**，温湿度等与测试上报一致。

### 9.5 说明

自动建档仅在 **EMQX Webhook** 路径启用；`/agri/envSensor/ingest` 仍要求在设备接入管理中**预先注册**，以免非 EMQX 通道误建新设备。

