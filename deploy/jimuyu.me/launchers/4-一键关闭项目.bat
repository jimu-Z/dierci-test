@echo off
chcp 65001 >nul
title 启元农链 — 一键关闭（Nginx / 后端 8080 / cloudflared）
cd /d "%~dp0"
if not exist "paths.bat" (
  echo 请先复制 paths.example.bat 为 paths.bat 并填写路径。
  pause
  exit /b 1
)
call paths.bat

echo.
echo [1/3] 尝试优雅停止 Nginx（nginx -s quit）...
if exist "%NGINX_HOME%\nginx.exe" (
  "%NGINX_HOME%\nginx.exe" -s quit 2>nul
  if errorlevel 1 (
    echo      （未运行或已退出，可忽略）
  ) else (
    echo      已发送 quit 信号。
  )
) else (
  echo      未找到: %NGINX_HOME%\nginx.exe
)

echo.
echo [2/3] 结束占用本机 8080 端口的进程（若依后端）...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ids = @(Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique); if ($ids.Count -eq 0) { Write-Host '      8080 无监听进程。' } else { foreach ($id in $ids) { try { Stop-Process -Id $id -Force -ErrorAction Stop; Write-Host ('      已结束 PID ' + $id) } catch { Write-Host ('      无法结束 PID ' + $id + '：' + $_.Exception.Message) } } }"

echo.
echo [3/3] 结束 cloudflared 隧道进程...
taskkill /F /IM cloudflared.exe 2>nul
if errorlevel 1 (
  echo      （未运行 cloudflared，可忽略）
) else (
  echo      已结束 cloudflared.exe
)

echo.
echo 完成。MySQL / Redis 未处理（一般为系统服务，请保留运行）。
echo 注意：若本机另有程序占用 8080，也会被一并结束，请自行核对。
pause
