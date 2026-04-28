# EMQX 阶段A联调配置模板

## 1. 用途
本模板用于你在 EMQX 控制台完成 A2 后，快速配置 webhook 上行到本系统并完成首轮联调。

## 1.1 使用原则
- 这里只保留联调必需项：回调地址、Header、Topic 规范、消息样例和排查步骤。
- 临时公网地址仅用于短期验证，正式环境必须替换为稳定域名。
- 联调完成后，记得把最终回调结论同步回项目开发计划文档。

## 2. 回调目标
- 方法：POST
- URL：`https://<你的域名>/agri/envSensor/ingest/emqx`
- Content-Type：application/json
- Header：
  - `X-Agri-Token: <你的AGRI_SENSOR_INGEST_TOKEN>`

### 2.1 本次会话可直接使用
- 临时公网地址：`https://curly-shoes-speak.loca.lt/agri/envSensor/ingest/emqx`
- 本地回环地址：`http://127.0.0.1:8080/agri/envSensor/ingest/emqx`
- 说明：临时公网地址仅用于联调，隧道关闭后失效。

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
4. 设备在线状态被回写为在线。
5. 超阈值时记录状态自动置为预警。
6. 设备列表与轨迹/告警页面能看到最新上报时间。

### 6.1 快速验证命令
- 本地直连回调：`curl -X POST http://127.0.0.1:8080/agri/envSensor/ingest/emqx -H "Content-Type: application/json" -H "X-Agri-Token: <token>" -d '{"deviceCode":"DEV-TEMP-001","plotCode":"PLOT-A01","temperature":26.3,"humidity":63.8,"co2":512,"timestamp":1776300000}'`
- 如果返回 200，说明后端接收链路和 token 校验已通。

## 7. 常见问题排查
- 401/令牌无效：检查 `X-Agri-Token` 是否与服务端环境变量一致。
- 设备未注册：先在系统设备接入管理中创建对应 `deviceCode`。
- 地块或设备为空：检查 payload 是否包含字段，或 Topic 是否遵循推荐规范。
- 时间异常：优先传 Unix 秒级或毫秒级时间戳。
- 503/隧道不可用：检查 localtunnel 是否仍在运行，或切换到本地回环地址验证。
- 规则命中但未入库：检查 EMQX 输出 JSON 中 `payload` 是否被正确传递，必要时直接透传原始消息体。

## 8. 安全提醒
- 不要在聊天工具发送账号密码或长期密钥。
- 若口令曾外泄，请立即重置并启用多因素认证。
