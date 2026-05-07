@echo off
REM 复制本文件为 paths.bat 后按你本机路径修改（paths.bat 可加入 .gitignore，勿提交令牌）

set "CHUANGYE_ROOT=F:\chuangye"
set "QIYUAN_BIN=%CHUANGYE_ROOT%\apps\qiyuan-backend\bin"
set "NGINX_HOME=F:\gongju\nginx-1.30.0"
set "CLOUDFLARED_EXE=F:\gongju\cloudflared\cloudflared.exe"
set "CF_CONFIG=%USERPROFILE%\.cloudflared\config.yml"
set "CF_TUNNEL_NAME=qiyuan-nonglian"
