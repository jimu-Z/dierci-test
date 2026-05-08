# 管理端前端（阶段 B）

若依 Vue2 管理端位于 **`frontend/admin-web`**（由原 `ruoyi-ui` 迁入）。

```powershell
cd frontend\admin-web
npm install
npm run dev
npm run build:prod   # 产出 dist，供 Nginx root 指向
```

与后端、Nginx、隧道的衔接见根目录 [README.md](../README.md)、[CLAUDE.md](../CLAUDE.md) 及 [deploy/jimuyu.me/nginx-app-local-http.conf.example](../deploy/jimuyu.me/nginx-app-local-http.conf.example)。
