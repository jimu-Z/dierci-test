@echo off
chcp 65001 >nul
title 启动 Cloudflare 隧道
cd /d "%~dp0"
if not exist "paths.bat" (
  echo 请先复制 paths.example.bat 为 paths.bat 并填写路径。
  pause
  exit /b 1
)
call paths.bat

if not exist "%CLOUDFLARED_EXE%" (
  echo 未找到 cloudflared，请编辑 paths.bat 中的 CLOUDFLARED_EXE
  pause
  exit /b 1
)
if not exist "%CF_CONFIG%" (
  echo 未找到: %CF_CONFIG%
  pause
  exit /b 1
)

REM 隧道名在 config.yml 的 tunnel: 已指定时可省略；勿重复起多个 cloudflared（易异常）
start "Cloudflared-Tunnel" powershell -NoExit -NoProfile -Command "& '%CLOUDFLARED_EXE%' tunnel --config '%CF_CONFIG%' run '%CF_TUNNEL_NAME%'"
echo 已打开新窗口运行隧道，请勿关闭该窗口。
echo 公网: https://app.jimuyu.me 与 https://hook.jimuyu.me
echo 若浏览器超时: 任务管理器里只保留一个 cloudflared；另用手机 4G 试访问以排除本机/宽带拦截。
pause
