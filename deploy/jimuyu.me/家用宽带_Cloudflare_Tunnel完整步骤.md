# 家用宽带：jimuyu.me + 本机若依 + EMQX Webhook 完整实现指南

> 适用：**没有公网 IP / 运营商封 80·443 / 无法做路由器端口映射** 的家庭宽带。  
> 思路：域名 DNS 托管在 **Cloudflare**，用 **Cloudflare Tunnel（cloudflared）** 把 `https://子域.jimuyu.me` 安全转发到你电脑上的 `http://127.0.0.1:8080`。  
> 对外永远是 **HTTPS**（由 Cloudflare 边缘证书处理），**不必**在你家路由器上开端口。

---

## 你需要准备的东西


| 项目             | 说明                                                              |
| -------------- | --------------------------------------------------------------- |
| 域名 `jimuyu.me` | 已在注册商购买                                                         |
| Cloudflare 账号  | 免费即可：[https://dash.cloudflare.com](https://dash.cloudflare.com) |
| 本机已跑通若依后端      | `http://127.0.0.1:8080` 能访问；MySQL、Redis 已启动                     |
| EMQX           | 可用 EMQX Cloud 免费试用或自建；本指南以 **能配置 Webhook** 为准                   |
| 子域建议           | 例如 `**hook.jimuyu.me`** 专门给 Webhook，避免和以后网站冲突                   |


---

## 第一阶段：把 jimuyu.me 交给 Cloudflare 解析

1. 登录 [Cloudflare Dashboard](https://dash.cloudflare.com) → **Add a Site** → 输入 `jimuyu.me`。
2. 选择 **Free** 计划 → 继续直到 Cloudflare 给出 **两条 NS（域名服务器）**，例如 `xxx.ns.cloudflare.com`。
3. 打开你买域名的注册商（阿里云 / 腾讯云 / GoDaddy 等）→ 找到 **DNS / 域名服务器** → 把原有 NS **改成 Cloudflare 提供的两条**。
4. 等待生效（常见 **几小时～48 小时**）。在 Cloudflare 站点页看到 **Active** 即表示解析已接管。

> 生效前不要着急配 Tunnel；可先继续安装 cloudflared。

---

## 第二阶段：在本机安装 cloudflared（Windows）

### 2.1 下载

1. 打开官方发布页：
  [https://github.com/cloudflare/cloudflared/releases](https://github.com/cloudflare/cloudflared/releases)
2. 下载 **Windows** 的 `cloudflared-windows-amd64.exe`（若是 ARM 本机选对应架构）。
3. 改名为 `cloudflared.exe`，放到固定目录，例如：
  `C:\Tools\cloudflared\cloudflared.exe`

### 2.2 加入 PATH（可选但推荐）

- **设置** → **系统** → **关于** → **高级系统设置** → **环境变量** → 在 **Path** 里增加 `C:\Tools\cloudflared`
- 新开 **PowerShell** 执行：

```powershell
cloudflared --version
```

能输出版本号即成功。

---

## 第三阶段：登录 Cloudflare 并创建 Tunnel

以下命令在 **PowerShell** 中执行。若 `cloudflared` 未加入 PATH，请写全路径，例如：

```powershell
$cf = "F:\gongju\cloudflared\cloudflared.exe"
& $cf --version
& $cf tunnel login
```

下文为简洁仍写 `cloudflared`，你可将每条命令里的 `cloudflared` 换成 `& "F:\gongju\cloudflared\cloudflared.exe"`。

### 要不要等 Cloudflare / 解析生效？


| 步骤                                           | 是否必须等 NS 全球生效                                                                                                                                                 |
| -------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **3.1 登录**、**3.2 创建隧道**、**3.3 写 config.yml** | **不必等**。只要本机能装 cloudflared、浏览器能登录 Cloudflare 即可先做。                                                                                                            |
| **3.4 `tunnel route dns`**                   | 域名 **已添加在 Cloudflare 站点里** 即可执行；会在 Cloudflare 面板里**自动写入** `hook` 的 CNAME。若站点仍显示「待激活」，请先在注册商把 **NS 改成 Cloudflare** 并等到变为 **Active** 再执行更稳妥。                    |
| **3.5 浏览器访问 `https://hook.jimuyu.me`**       | **建议等**：注册商 NS 已指向 Cloudflare 且站点 **Active** 后，公网解析才会稳定指向 Cloudflare；否则部分地区可能仍解析到旧 DNS，打不开。可用 `nslookup hook.jimuyu.me 1.1.1.1` 看是否已是 Cloudflare/Tunnel 相关记录。 |


总结：**可以先做完 3.1～3.3（甚至装好 config）**；**3.4 在 Cloudflare 站点正常后做**；**用浏览器验收公网 HTTPS 时**再确认 NS 已生效更省事。

### 3.1 登录（浏览器授权）

```powershell
cloudflared tunnel login
```

- 会弹出浏览器 → 选择 **jimuyu.me** 站点 → 授权。  
- 成功后本机会有证书凭据（默认在用户目录 `.cloudflared` 下）。

### 3.2 创建一条隧道（名字自定）

```powershell
cloudflared tunnel create qiyuan-nonglian
```

记下命令输出里的 **Tunnel UUID**（一串十六进制），后面配置会用到。

### 3.3 编写隧道配置文件

在用户目录创建配置文件（**把 UUID 换成上一步的**）：

路径：`C:\Users\你的用户名\.cloudflared\config.yml`

内容示例：

```yaml
tunnel: <你的Tunnel-UUID>
credentials-file: C:\Users\你的用户名\.cloudflared\<UUID>.json

ingress:
  - hostname: hook.jimuyu.me
    service: http://127.0.0.1:8080
  # （可选）管理前端——见本节下方「同一隧道暴露前端」
  # - hostname: app.jimuyu.me
  #   service: http://127.0.0.1:8890
  - service: http_status:404
```

说明：

- `**hook.jimuyu.me**`：给 EMQX、以及对外后端 API/Webhook；可改成 `api.jimuyu.me` 等，但要与后面 DNS 与环境变量一致。
- `**http://127.0.0.1:8080**`：若依后端本机地址；若你改端口，这里一起改。

### 3.4 把子域绑定到隧道（DNS）

在 PowerShell：

```powershell
cloudflared tunnel route dns qiyuan-nonglian hook.jimuyu.me
# （可选）同时为前端子域写入 DNS，与 config.yml 中 hostname 一致
# cloudflared tunnel route dns qiyuan-nonglian app.jimuyu.me
```

- `qiyuan-nonglian` 是 `**tunnel create` 时起的名字**。  
- 成功后在 Cloudflare **DNS** 里会出现一条 **CNAME**，指向 `xxx.cfargotunnel.com`，**不要手动删**。

### 3.5 启动隧道（先前台试跑）

```powershell
cloudflared tunnel run qiyuan-nonglian
```

- 窗口**不要关**。  
- 用浏览器访问：`**https://hook.jimuyu.me`**  
  - 若若依根路径无首页，可能显示 404 或跳转登录页，**属正常**；可再试：  
  `https://hook.jimuyu.me/captchaImage`（若依验证码接口，常用来测通）。

确认 **HTTPS 能打开** 后，再进入下一阶段。

### 3.6 设为开机自启（可选）

可用 **任务计划程序** 在用户登录时执行：

```powershell
cloudflared tunnel run qiyuan-nonglian
```

或使用 NSSM 等把 `cloudflared` 注册为 Windows 服务（进阶，略）。

### 3.7 （可选）用域名访问管理前端：点击 `https://app.jimuyu.me` 就能用？

**不能直接“点域名就开电脑”。** Tunnel 只是把 **你已在本机运行的服务**映射到公网：电脑要开机且 **后端（8080）+ Tunnel + 前端或 Nginx** 都在运行，域名才能打开整套系统。

推荐拆成两条子域（清晰、也少改后端）：

| 用途 | 子域示例 | 转发到本机 |
| ----- | --------- | ----------- |
| API / EMQX Webhook | `hook.jimuyu.me` | `http://127.0.0.1:8080` |
| 浏览器管理前端 | `app.jimuyu.me` | 见下文 A 或 B |

**不要用根域 `jimuyu.me` 同时兼做 GitHub Pages 与 Tunnel**，容易与现有 **A/CNAME** 冲突；专门为应用增加 `app` 或 `www` 子域最省事。

---

#### A. 开发联调：`npm run dev` 直通 Tunnel（省事，但不适合长期使用）

1. Windows 监听 **80 端口经常要管理员权限**，建议使用其它端口启动前端，例如 **8890**：  
   在 `frontend/admin-web` 目录执行：`npm run dev -- --port 8890`
2. 在用户目录 **`config.yml`** 的 `ingress` 里追加一条（写在 `hook` 与 `catch-all` 之间）：

```yaml
  - hostname: app.jimuyu.me
    service: http://127.0.0.1:8890
```

3. DNS：

```powershell
cloudflared tunnel route dns qiyuan-nonglian app.jimuyu.me
```

4. 重启 `tunnel run`。浏览器访问：**`https://app.jimuyu.me`**。  
Vue 仍会按 `vue.config.js` 把 **`/dev-api`** 转到本机 `8080`，一般 **无需改环境变量**。若遇 HMR/校验主机名问题，请改用下面的 **方案 B（推荐）**。

---

#### B. 推荐：**打包 + 本机 Nginx + Tunnel（稳定、可当日常入口）**

1. 在 **`frontend/admin-web`** 执行生产构建：`npm run build:prod`，得到 **`dist`** 目录。
2. 在本机安装 Nginx（Windows 可用官方 zip），把仓库里的 **`deploy/jimuyu.me/nginx-app-local-http.conf.example`** 拷贝为实际配置，`root` 改成你的 **`dist` 路径**，监听端口与 Tunnel 一致（示例 **8890**）。
3. `config.yml` 与 **`tunnel route dns`** 同方案 A，`service` 指向 `http://127.0.0.1:8890`。
4. 后端需已启用 **`application.yml`** 中与转发头相关的配置（如 `server.forward-headers-strategy: framework`），以便登录、重定向等在 HTTPS 下正常。
5. 日常访问：**`https://app.jimuyu.me`** 即管理前端；接口走前端里的 **`/prod-api`** → Nginx 反代 → `8080`。

**提醒**：第四阶段里的 **`AGRI_EMQX_PUBLIC_WEBHOOK_BASE_URL`** 仍可保持 **`https://hook.jimuyu.me`**（与 EMQX Webhook **专用子域一致**），不必改成 `app` 域名。

##### 3.7.B.1 Windows 点击式实施清单（按顺序做）

以下默认：前端子域 **`app.jimuyu.me`**，本机 Nginx 监听 **`8890`**，后端 **`8080`**，隧道名与文档一致为 **`qiyuan-nonglian`**（请换成你 `tunnel create` 时起的名字）。

**0. 前置（本机须已就绪）**

- 若依后端能访问：`http://127.0.0.1:8080`（或你实际端口）。
- `application.yml` 中已有 `server.forward-headers-strategy: framework`（本仓库已配置，一般不用改）。
- Cloudflare 站点 **`jimuyu.me` 已为 Active**；**`hook.jimuyu.me`** 隧道已通（可先完成 3.1～3.5）。

**1. 安装 Nginx（Windows，图形化）**

1. 浏览器打开 [nginx 官方下载页](http://nginx.org/en/download.html)。
2. 在 **Stable version** 下点 **nginx/Windows-x.x.x** 的 **zip** 下载。
3. 解压到固定目录，例如 **`C:\nginx`**（路径里尽量**不要中文、不要空格**）。
4. 进入 **`C:\nginx\conf`**，用记事本或 VS Code 打开 **`nginx.conf`**（先备份一份副本）。

**2. 接入本仓库里的站点配置**

1. 将仓库文件 **`deploy/jimuyu.me/nginx-app-local-http.conf.example`** 复制到 **`C:\nginx\conf\app-jimuyu.conf`**（文件名自定）。
2. 用编辑器打开 **`app-jimuyu.conf`**，核对两行：
   - **`listen 8890;`**（若 8890 被占用，改成例如 **8891**，后面 Tunnel 里端口要一致）。
   - **`root`**：改成你机器上 **`frontend\admin-web\dist`** 的绝对路径，Windows 可用正斜杠，例如  
     `F:/chuangye/frontend/admin-web/dist;`  
     （**先执行下面第 3 步生成 `dist`，或先写路径，构建后再启动 Nginx**。）
3. 编辑 **`C:\nginx\conf\nginx.conf`**：在 **`http { ... }`** 块**末尾**（最后一个 `}` 之前）增加一行：

```nginx
    include app-jimuyu.conf;
```

4. 保存。打开 **PowerShell**，进入 **`C:\nginx`**，测试配置：

```powershell
.\nginx.exe -t
```

5. 显示 **syntax is ok** / **test is successful** 后启动（同一目录）：

```powershell
.\nginx.exe
```

6. 浏览器访问 **`http://127.0.0.1:8890`**，应出现若依登录页（若 404，多半是 **`root` 路径不对或尚未 `build`**）。停止 Nginx（改配置后重载前）：`.\nginx.exe -s stop`。

**3. 打包前端（得到 `dist`）**

1. 打开 **PowerShell**：

```powershell
cd F:\chuangye\frontend\admin-web
npm install
npm run build
```

2. 确认目录 **`...\frontend\admin-web\dist`** 下已有 **`index.html`**。若上一步 Nginx 的 **`root`** 已指向该目录，重新 **`nginx.exe -t`** 后 **`.\nginx.exe`**（若已在跑则 **`.\nginx.exe -s reload`**）。

**4. 修改 Cloudflare Tunnel 配置（两条子域）**

1. 打开 **`C:\Users\你的用户名\.cloudflared\config.yml`**。
2. **`ingress`** 写成类似下面（**顺序重要**：具体 `hostname` 在前，最后一行 **`http_status:404`** 兜底）：

```yaml
tunnel: <你的Tunnel-UUID>
credentials-file: C:\Users\你的用户名\.cloudflared\<UUID>.json

ingress:
  - hostname: hook.jimuyu.me
    service: http://127.0.0.1:8080
  - hostname: app.jimuyu.me
    service: http://127.0.0.1:8890
  - service: http_status:404
```

3. 若 Nginx 改了端口，把 **`127.0.0.1:8890`** 改成一致。

**5. DNS：为 `app` 子域绑定隧道**

1. 打开 **PowerShell**（`cloudflared` 在 PATH 或写全路径）：

```powershell
cloudflared tunnel route dns qiyuan-nonglian app.jimuyu.me
```

2. 浏览器登录 **Cloudflare** → **`jimuyu.me`** → **DNS** → 确认新增 **`app`** 的 **CNAME**（指向 `xxx.cfargotunnel.com`），**不要手删**。

**6. 启动隧道并验收**

1. **先保证**：后端在跑、Nginx 在跑（`http://127.0.0.1:8890` 本地能打开）。
2. 启动隧道（窗口保持不关）：

```powershell
cloudflared tunnel run qiyuan-nonglian
```

3. 浏览器访问 **`https://app.jimuyu.me`** → 应出现与管理端一致的登录页；登录后功能正常即 **`/prod-api`** 反代成功。
4. **`https://hook.jimuyu.me`** 仍用于 **EMQX Webhook** 与后端直连验证；环境变量 **`AGRI_EMQX_PUBLIC_WEBHOOK_BASE_URL`** 保持 **`https://hook.jimuyu.me`** 即可。

**常见故障**

- **`https://app` 502 / 空白**：本机 Nginx 未启动、端口与 `config.yml` 不一致、或 `dist` 路径错误。
- **能打开页面但登录失败 / 接口 404**：检查 Nginx 里 **`location /prod-api/`** 是否生效、后端是否在 **8080**。
- **改完 `nginx.conf` 不生效**：在 **`C:\nginx`** 执行 **`.\nginx.exe -s reload`**。

---

## 第四阶段：配置若依后端（令牌 + 公网基址）

### 4.1 生成强随机令牌

PowerShell 示例：

```powershell
[guid]::NewGuid().ToString("N")
```

得到类似 `a1b2c3d4e5f6...`，复制保存。

### 4.2 设置环境变量后启动后端

**每次**启动 `run-dev.ps1` 或 `java -jar` 之前，在同一终端先执行：

```powershell
$env:AGRI_SENSOR_INGEST_TOKEN = "这里填你的GUID或更长随机串"
$env:AGRI_EMQX_PUBLIC_WEBHOOK_BASE_URL = "https://hook.jimuyu.me"
$env:AGRI_EMQX_WEBHOOK_VALID_UNTIL = "2027-05-06"
```

然后进入后端目录启动（示例）：

```powershell
cd F:\chuangye\apps\qiyuan-backend\bin
.\run-dev.ps1
```

**推荐（避免嵌套 PowerShell 时 `$env:` 被提前展开）**：使用仓库里的包装脚本，把 **4.1 的令牌** 传给 `-IngestToken`：

```powershell
cd F:\chuangye\apps\qiyuan-backend\bin
.\run-dev-emqx.ps1 -IngestToken "这里填4.1生成的令牌"
```

子域或有效期不同可再加：`-PublicWebhookBaseUrl`、`-WebhookValidUntil`。

> 若用 IDE 启动：在运行配置里加上述三个 **环境变量**。

### 4.3 管理端核对 Webhook 信息

1. 浏览器打开你平时的 **前端地址**（本机 `npm run dev` 一般是 `http://localhost:80`）。
2. 登录若依。
3. 用浏览器或 Postman 调（需带登录 Cookie 或 Token，与若依一致）：
  `**GET https://hook.jimuyu.me/agri/envSensor/integration/emqx-webhook`**  
   若登录态在 `localhost` 而接口在 `hook.jimuyu.me`，**跨域可能失败**，可改用：

- **Swagger**（若已开）：`https://hook.jimuyu.me/swagger-ui.html` 里找该 GET；或  
- 先 **Authorize**，再请求；或  
- 直接按下面 **第五阶段** 的 URL 填 EMQX（与 GET 返回的 `webhookUrl` 一致即可）。

返回 JSON 里应有：

- `webhookUrl`：`https://hook.jimuyu.me/agri/envSensor/ingest/emqx`
- `authHeaderName`：`X-Agri-Token`
- `ingestTokenConfigured`：`true`

---

## 第五阶段：配置 EMQX（规则 + Webhook）

以下以 **EMQX 5.x 控制台** 为例，不同托管商菜单名称可能略有差异。

### 5.1 创建规则（Rule）

1. 进入 **集成 / 规则引擎 / Rules** → **Create**。
2. **SQL** 示例（按你实际 Topic 改）：

```sql
SELECT
  topic,
  clientid,
  timestamp,
  payload
FROM
  "agri/+/+/telemetry"
```

1. **动作（Action）** 选择 **HTTP 服务**（或 Webhook）。
2. **URL**：
  `https://hook.jimuyu.me/agri/envSensor/ingest/emqx`
3. **方法**：`POST`
4. **Headers** 增加：
  - `Content-Type`：`application/json`  
  - `X-Agri-Token`：`与 AGRI_SENSOR_INGEST_TOKEN 完全一致`
5. **Body** 模板（与仓库联调手册一致），示例：

```json
{
  "topic": "${topic}",
  "clientid": "${clientid}",
  "timestamp": "${timestamp}",
  "payload": ${payload}
}
```

1. 保存并启用规则。

### 5.2 设备侧 Topic 与 payload

- Topic 建议：`agri/{plotCode}/{deviceCode}/telemetry`  
例：`agri/PLOT-A01/DEV-TEMP-001/telemetry`
- Payload JSON 示例：

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

### 5.3 若依里必须先有设备

在 **设备接入** 中已存在 `**deviceCode`**（如 `DEV-TEMP-001`），否则接口会返回 **「设备未注册，拒绝接入」**。

---

## 第六阶段：联调自测

### 6.1 本机直连（不经过域名）

PowerShell（先设置 `$env:AGRI_SENSOR_INGEST_TOKEN`）：

```powershell
cd F:\chuangye
.\iot\scripts\emqx-ingest-curl.example.ps1
```

### 6.2 走公网域名（模拟 EMQX）

```powershell
$token = $env:AGRI_SENSOR_INGEST_TOKEN
$body = '{"topic":"agri/PLOT-A01/DEV-TEMP-001/telemetry","payload":{"temperature":26.3,"humidity":63.8,"co2":512,"timestamp":1776300000}}'
Invoke-RestMethod -Method Post -Uri "https://hook.jimuyu.me/agri/envSensor/ingest/emqx" -Headers @{"X-Agri-Token"=$token;"Content-Type"="application/json"} -Body $body
```

成功应返回若依统一 JSON 里带 **「接收成功」** 类文案；数据库 `agri_env_sensor_record` 出现新行，`data_source` 为 `**emqx-webhook`**。

---

## 常见问题

### Cloudflare DNS 里根域名 `jimuyu.me` 指向 185.199.108.x，不是我的家里 IP？

**这是正常现象，也不是你家宽带的公网 IP。**

- `185.199.108.153`、`185.199.109.153`、`185.199.110.153`、`185.199.111.153` 是 **GitHub Pages** 使用的地址，表示 `**jimuyu.me` 根域** 曾被配置为访问 **GitHub 上的静态站**，与若依后端、家用 IP 无关。
- **Cloudflare Tunnel 方案**只要求你为 `**hook.jimuyu.me`（子域）** 生成一条 **CNAME**（执行 `cloudflared tunnel route dns` 后自动出现），**不需要**把任何记录指到你家 IP。
- 若你**不再**用 GitHub Pages 托管根域，可在 DNS 里**删除**那几条指向 `185.199.x.x` 的 **A** 记录；若**还要**保留 GitHub 上的网站，就**保留**它们，与 `hook` 子域不冲突。
- **橙色云「已代理」** 表示经 Cloudflare 转发，对外也不会直接暴露你家路由器 IP。
- 若仍有 **「继续前往激活」**，请到**注册商**把域名 NS 改成 Cloudflare 提供的两条，等站点 **Active** 后再做 Tunnel。


### 本机 `127.0.0.1:8890` 正常，但浏览器访问 `https://hook/app.jimuyu.me` 一直「响应时间太长」`ERR_CONNECTION_TIMED_OUT`

说明 **问题多半不在 Nginx/若依**，而是 **公网入口（浏览器 → Cloudflare 边缘）** 或 **隧道未连上**，请按序排查：

1. **只运行一个 `cloudflared`**  
   任务管理器里结束重复的 `cloudflared.exe`，再只开脚本 `3-启动Cloudflare隧道.bat` 或计划任务之一，避免多实例抢连。

2. **看隧道窗口日志**  
   应出现类似 `Registered tunnel connection`；若反复报错或立刻退出，把整段日志复制下来排查。

3. **命令行自检（本机 PowerShell）**  
   - `cloudflared tunnel info <你的隧道UUID>`：应有 **CONNECTOR** 行（表示边缘已看到你的连接器）。  
   - `curl -4 -I --max-time 10 https://hook.jimuyu.me`：若这里也超时，而第 2 步显示已连接，常见是 **当前网络访问 Cloudflare 边缘 443 不畅**（例如部分宽带/地区策略）。可换 **手机 4G/5G** 访问同一域名对比：手机能开、电脑不能 → 重点查电脑代理、防火墙、安全软件；手机也不能 → 查 Cloudflare DNS 是否 **橙色云**、隧道是否在跑。

4. **Cloudflare 控制台**  
   **DNS**：`hook`、`app` 应为 **CNAME** 指向 `*.cfargotunnel.com`，且为 **已代理**（橙云）。  
   **Zero Trust → Networks → Tunnels**：选中隧道，确认 **Public hostnames**（或本地 `config.yml` 的 `ingress`）与域名一致。

5. **仍不通**  
   启动脚本已改为**不强制** `--protocol http2`（由 cloudflared 自选传输）。若仍超时，在本机开 **VPN** 后再访问同一域名，用于判断是否为运营商对 Cloudflare 边缘路径的限制。

---

| 现象                           | 处理                                                                     |
| ---------------------------- | ---------------------------------------------------------------------- |
| `https://hook.jimuyu.me` 打不开 | 检查 Tunnel 是否运行、`config.yml` 里 UUID、hostname；Cloudflare DNS 是否已有 CNAME。 |
| 502 / Bad Gateway            | 本机后端未启动或端口不是 8080；改 `config.yml` 里 `service`。                          |
| 401 / 令牌无效                   | `X-Agri-Token` 与 `AGRI_SENSOR_INGEST_TOKEN` 不一致；或后端未带环境变量启动。           |
| 设备未注册                        | 在若依里先新增该 `deviceCode` 的设备接入节点。                                         |
| Topic 有数据但 Webhook 没触发       | 检查规则 SQL 的 Topic 过滤是否与设备发布 Topic 一致。                                   |


---

## 安全提醒

- **不要把** `AGRI_SENSOR_INGEST_TOKEN` 提交到 Git 或发聊天群。  
- 家用电脑长期暴露服务时，务必保持 **Windows 更新**、**强密码**，并只开放必要进程。  
- 正式环境建议单独 **VPS** 部署，与家用 PC 隔离。

---

## 文档索引

- EMQX 字段与规则模板：`docs/runbooks/EMQX_阶段A_联调配置模板.md`  
- 隧道与 Webhook 索引：`deploy/jimuyu.me/README.md`（`deploy/emqx-tunnel/README.md` 为短链占位，指向同一套说明）  
- 环境变量示例：`deploy/jimuyu.me/env.example`

