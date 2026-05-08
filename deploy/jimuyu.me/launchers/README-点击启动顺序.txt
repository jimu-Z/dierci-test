本目录提供「双击 .bat」启动各进程（不是开机自启；每次开机后按顺序点）。

【一次性准备】
1) 复制 paths.example.bat → 改名为 paths.bat，用记事本打开，按你本机修改：
   - CHUANGYE_ROOT、NGINX_HOME、CLOUDFLARED_EXE、CF_TUNNEL_NAME 等
2) 复制 ingest-token.txt.example 的说明，在同目录新建 ingest-token.txt，
   第一行只写你的 AGRI_SENSOR_INGEST_TOKEN（与 EMQX X-Agri-Token 一致）。

【每次开机后的点击顺序】
1. 0-可选-启动MySQL与Redis.bat     （若库已自动运行可跳过；或按提示改服务名）
2. 1-启动Nginx-8890.bat
3. 2-启动后端-8080.bat             （等待新窗口里出现 Started RuoYiApplication）
4. 3-启动Cloudflare隧道.bat        （窗口勿关）

【一键关闭】
- 4-一键关闭项目.bat：依次尝试 Nginx quit、结束监听 8080 的进程、结束 cloudflared；不停止 MySQL/Redis。

【本机自检】
- http://127.0.0.1:8890  前端
- http://127.0.0.1:8080  后端

【Nginx root】生产静态页来自 frontend\admin-web\dist（npm run build:prod）。若你从旧路径升级，请把站点配置里的 root 从 ruoyi-ui\dist 改为 frontend\admin-web\dist 后 nginx -s reload。

【公网】
- https://app.jimuyu.me
- https://hook.jimuyu.me

【停止】
- 推荐：双击 4-一键关闭项目.bat
- 或手动关闭各命令行窗口；Nginx 也可在 Nginx 目录执行 nginx.exe -s quit

说明：未生成独立 .exe，.bat 双击即可；若需 .exe 可用第三方「bat 转 exe」工具自行封装（注意杀毒误报）。
