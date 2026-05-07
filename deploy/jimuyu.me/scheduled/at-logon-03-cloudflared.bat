@echo off
REM 计划任务「登录时」调用：晚于 Nginx/后端，给 8890/8080 留启动时间
chcp 65001 >nul
timeout /t 50 /nobreak >nul

set "SCRIPT_DIR=%~dp0"
call "%SCRIPT_DIR%..\launchers\paths.bat" 2>nul
if not defined CLOUDFLARED_EXE (
  echo [Qiyuan-03-Cloudflared] 缺少 launchers\paths.bat 或未设置 CLOUDFLARED_EXE
  exit /b 1
)
if not exist "%CLOUDFLARED_EXE%" (
  echo [Qiyuan-03-Cloudflared] 未找到: %CLOUDFLARED_EXE%
  exit /b 1
)
if not exist "%CF_CONFIG%" (
  echo [Qiyuan-03-Cloudflared] 未找到 config: %CF_CONFIG%
  exit /b 1
)

start "Cloudflared-Tunnel" /min powershell -NoExit -NoProfile -Command "& '%CLOUDFLARED_EXE%' tunnel --config '%CF_CONFIG%' run '%CF_TUNNEL_NAME%'"
exit /b 0
