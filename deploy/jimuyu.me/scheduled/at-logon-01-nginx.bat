@echo off
REM 计划任务「登录时」调用：稍等几秒再启 Nginx，避免桌面未就绪
chcp 65001 >nul
timeout /t 8 /nobreak >nul

set "SCRIPT_DIR=%~dp0"
call "%SCRIPT_DIR%..\launchers\paths.bat" 2>nul
if not defined NGINX_HOME (
  echo [Qiyuan-01-Nginx] 缺少 launchers\paths.bat，请复制 paths.example.bat 为 paths.bat 并填写 NGINX_HOME。
  exit /b 1
)

cd /d "%NGINX_HOME%"
"%NGINX_HOME%\nginx.exe" -t
if errorlevel 1 exit /b 1
"%NGINX_HOME%\nginx.exe"
exit /b 0
