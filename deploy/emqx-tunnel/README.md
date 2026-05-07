# EMQX Webhook 公网入口（一年期域名 + 隧道）

后端回调路径固定为：`POST {公网HTTPS基址}/agri/envSensor/ingest/emqx`。

## 1. 推荐：自购 1 年域名 + HTTPS 反代

1. 购买任意 **1 年期** 域名（各云厂商「域名注册」即可）。
2. 准备一台有 **公网 IP** 的机器，或云 **CLB / API 网关**，对外提供 **443**。
3. 在该机器上配置 Nginx（示例）：

```nginx
server {
    listen 443 ssl;
    server_name emqx-hook.你的域名.com;
    ssl_certificate     /path/to/fullchain.pem;
    ssl_certificate_key /path/to/privkey.pem;
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

4. 在运行后端的机器上设置环境变量：

```text
AGRI_EMQX_PUBLIC_WEBHOOK_BASE_URL=https://emqx-hook.你的域名.com
AGRI_EMQX_WEBHOOK_VALID_UNTIL=2027-05-06
AGRI_SENSOR_INGEST_TOKEN=<强随机串>
```

5. EMQX Webhook URL 填：`https://emqx-hook.你的域名.com/agri/envSensor/ingest/emqx`，Header：`X-Agri-Token: <同令牌>`。

## 2. 备选：Cloudflare Tunnel（无公网 IP）

1. 安装 [cloudflared](https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/)，按官方文档创建 **Named Tunnel**，并将 **自定义主机名**（你购买的 1 年域名下的子域）绑定到 Tunnel。
2. Tunnel 内网目标指向 `http://127.0.0.1:8080`。
3. 同样设置 `AGRI_EMQX_PUBLIC_WEBHOOK_BASE_URL` 为该 HTTPS 主机名（无路径）。

## 3. 验收

- 管理端登录后调用：`GET /agri/envSensor/integration/emqx-webhook` 核对 `webhookUrl`。
- 本地直连：`iot/scripts/emqx-ingest-curl.example.ps1`（需先改 Token）。
