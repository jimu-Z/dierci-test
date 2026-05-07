@echo off
chcp 65001 >nul
title 启动 Nginx 8890
cd /d "%~dp0"
if not exist "paths.bat" (
  echo 请先复制 paths.example.bat 为 paths.bat 并填写路径。
  pause
  exit /b 1
)
call paths.bat

cd /d "%NGINX_HOME%"
"%NGINX_HOME%\nginx.exe" -t
if errorlevel 1 (
  echo nginx 配置检测失败。
  pause
  exit /b 1
)
"%NGINX_HOME%\nginx.exe"
if errorlevel 1 (
  echo 启动失败（可能已在运行或端口占用）。
  pause
  exit /b 1
)
echo.
echo Nginx 已在后台运行。本机打开: http://127.0.0.1:8890
echo 停止请到本目录执行: nginx.exe -s quit
echo.
pause
