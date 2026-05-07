@echo off
chcp 65001 >nul
title 若依后端 8080（run-dev-emqx）
cd /d "%~dp0"
if not exist "paths.bat" (
  echo 请先复制 paths.example.bat 为 paths.bat 并填写路径。
  pause
  exit /b 1
)
call paths.bat

if not exist "%~dp0ingest-token.txt" (
  echo 请在本目录创建 ingest-token.txt（可参考 ingest-token.txt.example），第一行为接入令牌。
  pause
  exit /b 1
)

set "INGEST_TOKEN="
for /f "usebackq delims=" %%a in ("%~dp0ingest-token.txt") do (
  set "INGEST_TOKEN=%%a"
  goto :tok_done
)
:tok_done
if "%INGEST_TOKEN%"=="" (
  echo ingest-token.txt 为空。
  pause
  exit /b 1
)

REM 勿用 cmd /k 嵌套引号 cd，易触发「文件名、目录名或卷标语法不正确」
start "Qiyuan-Backend-8080" powershell -NoExit -NoProfile -ExecutionPolicy Bypass -Command "Set-Location -LiteralPath '%QIYUAN_BIN%'; & .\run-dev-emqx.ps1 -IngestToken '%INGEST_TOKEN%'"
echo 已在新窗口启动后端（首次会编译，需等待出现 Started RuoYiApplication）。
pause
