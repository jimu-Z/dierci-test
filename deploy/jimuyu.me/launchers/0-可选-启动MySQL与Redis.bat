@echo off
chcp 65001 >nul
title 可选：启动 MySQL / Redis 服务
echo.
echo 【可选】若 MySQL、Redis 已设为「自动」启动，可跳过本步骤。
echo 下面服务名需与你电脑「服务」里名称一致，错误时请右键本 bat → 编辑修改。
echo.
pause

REM --- 按本机实际服务名修改（常见：MySQL80、MYSQL57、Redis）---
net start MySQL80 2>nul
if errorlevel 1 echo 提示：MySQL80 未启动或名称不对，请打开 services.msc 查看实际名称。

net start Redis 2>nul
if errorlevel 1 echo 提示：Redis 服务名可能不是 Redis，请自行修改本 bat 或手动启动 Redis。

echo.
echo 完成。可按任意键关闭。
pause >nul
