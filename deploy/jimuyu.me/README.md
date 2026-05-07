# jimuyu.me 对接本机后端（EMQX Webhook / 若依 API）

**只有家用宽带、没有公网 IP？** 请直接打开分步教程：  
**[家用宽带_Cloudflare_Tunnel完整步骤.md](./家用宽带_Cloudflare_Tunnel完整步骤.md)**（Cloudflare Tunnel + `hook.jimuyu.me` → 本机 `8080`，无需路由器开端口）。

目标：让公网可通过 **`https://hook.jimuyu.me`**（示例子域，可改名）访问你本机 **`http://127.0.0.1:8080`** 上的 Spring Boot。

## 1. 要不要用你手里的 SSL 证书？

- **需要 HTTPS**：EMQX 云端 Webhook、浏览器混合内容策略，都建议走 **https**。
- **二选一即可**：
  1. **你已有的证书**：把 Nginx（或 Caddy）配成 `ssl_certificate` / `ssl_certificate_key` 指向你的 **完整链证书 + 私钥**（常见为 `.pem` + `.key`；若是 `.pfx` 需先导出为 pem/key）。
  2. **Let’s Encrypt 免费证书**：在**有公网 IP 且 80 端口可达**的机器上用 Certbot 自动申请与续期，可不用你买的证书。

本机仅跑 Java、**不直接绑定 443** 时：由 **Nginx/Caddy 监听 443** 终止 TLS，再 **反向代理** 到 `127.0.0.1:8080`。

## 2. 域名「现在是空的」——怎么指到本机？

### 方案 A：你有固定公网 IP（家庭宽带较少见）

1. 在域名 DNS（阿里云/腾讯云/Cloudflare 等）添加：  
   - **`hook.jimuyu.me` → A 记录 → 你的公网 IP**  
2. 路由器上做 **端口转发**：外网 `443` → 运行 Nginx 的那台机器 `443`。  
3. 在该机器安装 Nginx，使用本目录下 `nginx-hook.conf.example`（改证书路径、改 `server_name`）。  
4. 本机启动若依后端（默认 `8080`）。

### 方案 B：无公网 IP / 80、443 被封（更常见）

使用 **Cloudflare Tunnel**（或同类内网穿透）：

1. 把 `jimuyu.me` 的 DNS 托管到 **Cloudflare**（迁移 NS 或在注册商改 NS）。  
2. 安装 `cloudflared`，按 Cloudflare 文档创建 **Named Tunnel**，给隧道绑定 **`hook.jimuyu.me`**。  
3. 隧道内网目标填 **`http://127.0.0.1:8080`**（后端跑在本机时）。  
4. 访客与 EMQX 访问的是 Cloudflare 边缘 **HTTPS**，一般**不必在你本机再配你买的证书**（证书由 Cloudflare 对外呈现；源站可用 http 本地）。

若你希望「端到端都用你自己的证书」，则更适合方案 A（自有 VPS + Nginx + 你的证书）。

## 3. 后端环境变量（与代码一致）

在运行后端的进程环境里设置（PowerShell 示例）：

```powershell
$env:AGRI_EMQX_PUBLIC_WEBHOOK_BASE_URL = "https://hook.jimuyu.me"
$env:AGRI_EMQX_WEBHOOK_VALID_UNTIL = "2027-05-06"
$env:AGRI_SENSOR_INGEST_TOKEN = "<强随机串>"
```

EMQX 控制台 Webhook URL：

`https://hook.jimuyu.me/agri/envSensor/ingest/emqx`  

Header：`X-Agri-Token: <同上令牌>`

登录若依后也可调用：`GET /agri/envSensor/integration/emqx-webhook` 核对拼接后的地址。

## 4. 子域命名建议

- 推荐单独子域 **`hook.jimuyu.me`** 或 **`api.jimuyu.me`** 给后端，避免与以后官网 `www` 冲突。  
- 若坚持用根域 `https://jimuyu.me`，把 Nginx 的 `server_name` 改成 `jimuyu.me www.jimuyu.me` 即可，路径仍为 `/agri/...`。

## 5. 前端（可选）

若希望 **用域名打开整套管理端**（Tunnel 暴露 Vue 页面 + 反代接口），见教程 **§3.7**：[家用宽带_Cloudflare_Tunnel完整步骤.md](./家用宽带_Cloudflare_Tunnel完整步骤.md)。  
生产推荐 Nginx 示例：`nginx-app-local-http.conf.example`（本机 HTTP、Cloudflare 边缘 HTTPS）。

**说明**：域名不会在关机时帮你“自动启动项目”；本机需常驻运行 **后端 + cloudflared（+ 前端或 Nginx）**。
