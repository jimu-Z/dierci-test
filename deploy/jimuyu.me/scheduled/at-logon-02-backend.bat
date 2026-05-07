@echo off
REM 计划任务「登录时」调用：延迟 30 秒再启后端，给 MySQL/Redis 留启动时间
chcp 65001 >nul
timeout /t 30 /nobreak >nul

set "SCRIPT_DIR=%~dp0"
call "%SCRIPT_DIR%..\launchers\paths.bat" 2>nul
if not defined QIYUAN_BIN (
  echo [Qiyuan-02-Backend] 缺少 launchers\paths.bat
  exit /b 1
)

set "TOKEN_FILE=%SCRIPT_DIR%..\launchers\ingest-token.txt"
if not exist "%TOKEN_FILE%" (
  echo [Qiyuan-02-Backend] 缺少 %TOKEN_FILE%
  exit /b 1
)

set "INGEST_TOKEN="
for /f "usebackq delims=" %%a in ("%TOKEN_FILE%") do (
  set "INGEST_TOKEN=%%a"
  goto :have_tok
)
:have_tok
if "%INGEST_TOKEN%"=="" exit /b 1

REM 最小化 PowerShell，避免 cmd 嵌套引号导致路径语法错误
start "Qiyuan-Backend-8080" /min powershell -NoExit -NoProfile -WindowStyle Minimized -ExecutionPolicy Bypass -Command "Set-Location -LiteralPath '%QIYUAN_BIN%'; & .\run-dev-emqx.ps1 -IngestToken '%INGEST_TOKEN%'"
exit /b 0
