# 开机/登录自动启动（Nginx + 后端 + Cloudflare Tunnel）

## 前置条件

1. **`deploy/jimuyu.me/launchers/paths.bat`**  
   从 `paths.example.bat` 复制并填写（与手动启动相同）。

2. **`deploy/jimuyu.me/launchers/ingest-token.txt`**  
   第一行为 `AGRI_SENSOR_INGEST_TOKEN`。

3. **MySQL / Redis**  
   建议设为 Windows **服务 → 启动类型：自动**，这样会早于「登录」就绪；后端任务内部再 **延迟 30 秒** 启动。

## 注册计划任务（推荐：PowerShell）

1. 打开 **PowerShell**（不必管理员，除非策略限制）。
2. 执行：

```powershell
cd F:\chuangye\deploy\jimuyu.me\scheduled
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned -Force
.\register-scheduled-tasks.ps1
```

将注册 3 个任务（**当前用户登录时**触发）：

| 任务名 | 行为 |
|--------|------|
| `Qiyuan-Auto-01-Nginx` | 登录后约 **8 秒** 启动 Nginx |
| `Qiyuan-Auto-02-Backend` | 登录后 **30 秒** 再启动后端（新窗口最小化） |
| `Qiyuan-Auto-03-Cloudflared` | 登录后 **50 秒** 再启动隧道（新窗口最小化） |

延迟叠在各自 `.bat` 里的 `timeout`，避免 schtasks 的 `/DELAY` 格式差异。

## 用图形界面核对（可选）

1. **Win + R** → `taskschd.msc` → 回车。  
2. **任务计划程序库** 中找到上述 3 个任务。  
3. 双击 → **操作** 选项卡：应指向  
   `cmd.exe /c "F:\chuangye\deploy\jimuyu.me\scheduled\at-logon-0x-....bat"`  
4. **触发器**：「登录时」、用户为你的账户。

## 卸载

```powershell
cd F:\chuangye\deploy\jimuyu.me\scheduled
.\unregister-scheduled-tasks.ps1
```

## 手动验证顺序（不依赖重启）

1. 双击运行 `at-logon-01-nginx.bat`  
2. 等后端编译：`at-logon-02-backend.bat`（会先等 30 秒）  
3. `at-logon-03-cloudflared.bat`（会先等 50 秒）  

或仍使用 `launchers` 下 1/2/3 脚本快速联调。
