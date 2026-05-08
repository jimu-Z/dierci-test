## 在本仓库中的位置

前端工程路径：**`frontend/admin-web`**（与 monorepo 根仓库一起克隆；阶段 B 由原 `ruoyi-ui` 迁入）。

开发服务器端口以 **`vue.config.js`** 中 `devServer.port` 为准（默认常见为 80，若冲突请改端口或加 `--port`）。

---

## 开发

```bash
# 若依官方示例（独立克隆时用）；本仓库请从仓库根目录进入 frontend/admin-web
git clone https://gitee.com/y_project/RuoYi-Vue

# 进入项目目录（官方独立仓为 ruoyi-ui；本仓为 admin-web）
cd ruoyi-ui

# 安装依赖
npm install

# 建议不要直接使用 cnpm 安装依赖，会有各种诡异的 bug。可以通过如下操作解决 npm 下载速度慢的问题
npm install --registry=https://registry.npmmirror.com

# 启动服务
npm run dev
```

浏览器访问 http://localhost:80

## 发布

```bash
# 构建测试环境
npm run build:stage

# 构建生产环境
npm run build:prod
```