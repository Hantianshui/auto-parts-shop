# 汽配库存销售管理系统

这是一个基于汽配店真实业务场景改造的前后端分离练手项目。项目从原来的静态库存页面升级为 Spring Boot 3 + Vue 3 的完整管理系统，覆盖登录认证、角色权限、库存管理、销售出库、分类供货商管理、图片上传、ECharts 数据统计和 MySQL 持久化。

## 技术栈

后端：

- Spring Boot 3
- Maven
- MyBatis-Plus
- JWT
- MySQL 8
- BCrypt 密码加密
- Spring MVC 拦截器权限控制

前端：

- Vue 3
- Vite
- Element Plus
- Axios
- ECharts

部署/调试：

- Docker Compose 启动 MySQL
- 前后端本地分离启动

## 项目结构

```text
auto-parts-shop
├── backend                 # Spring Boot 后端
│   ├── src/main/java/com/autoparts/shop
│   │   ├── common          # 统一响应和异常处理
│   │   ├── config          # Web、权限、初始化配置
│   │   ├── controller      # REST API
│   │   ├── entity          # 数据库实体
│   │   ├── mapper          # MyBatis-Plus Mapper
│   │   ├── security        # JWT、登录用户上下文、角色注解
│   │   └── service         # 业务逻辑
│   └── src/main/resources
│       ├── application.yml
│       └── db/schema.sql
├── frontend                # Vue 3 前端管理台
│   ├── src/api             # Axios 请求封装
│   ├── src/views           # 页面组件
│   └── src/assets          # 全局样式
├── docker-compose.yml      # MySQL 容器
└── README.md
```

根目录下保留了旧版 `index.html`、`app.js`、`styles.css`、`server.js`，可以作为原型参考。

## 核心功能

- JWT 登录认证
- 三级账号权限：管理员、店长、销售员
- 配件库存新增、编辑、删除、搜索、低库存筛选
- 销售出库并自动扣减库存
- 分类管理
- 供货商管理
- 图片上传接口
- 数据看板和 ECharts 销售图表
- MySQL 数据持久化
- Docker Compose 一键启动数据库

## 角色权限

| 角色 | 账号 | 密码 | 权限 |
| --- | --- | --- | --- |
| 管理员 | admin | 123456 | 全部功能，包含删除配件 |
| 店长 | manager | 123456 | 新增和编辑配件、分类、供货商、销售出库 |
| 销售员 | staff | 123456 | 查看库存、销售出库、查看统计 |

默认账号会在后端首次启动时自动创建。

## 启动方式

### 1. 启动 MySQL

```bash
docker compose up -d
```

默认数据库配置：

- 数据库：`auto_parts_shop`
- 用户名：`root`
- 密码：`root123456`
- 端口：`3306`

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端地址：

```text
http://localhost:8080
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：

```text
http://localhost:5174
```

## 常用接口

| 方法 | 地址 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | 登录 | 公开 |
| GET | `/api/auth/me` | 当前用户 | 登录 |
| GET | `/api/dashboard` | 数据看板 | 登录 |
| GET | `/api/parts` | 配件列表 | 登录 |
| POST | `/api/parts` | 新增或编辑配件 | ADMIN / MANAGER |
| DELETE | `/api/parts/{id}` | 删除配件 | ADMIN |
| GET | `/api/sales` | 销售记录 | 登录 |
| POST | `/api/sales` | 销售出库 | 登录 |
| GET | `/api/categories` | 分类列表 | 登录 |
| POST | `/api/categories` | 保存分类 | ADMIN / MANAGER |
| GET | `/api/suppliers` | 供货商列表 | 登录 |
| POST | `/api/suppliers` | 保存供货商 | ADMIN / MANAGER |
| POST | `/api/upload/image` | 上传图片 | ADMIN / MANAGER |

## 学习路线建议

1. 先跑通登录、库存列表、销售出库。
2. 学 MyBatis-Plus 的实体、Mapper、条件查询。
3. 看 JWT 从登录生成 token 到请求拦截校验的完整流程。
4. 改角色权限，例如让销售员不能查看数据看板。
5. 增加真实业务功能，例如客户管理、采购入库、退货、库存流水、欠款记录。
6. 增加图片上传到配件编辑页面。
7. 增加后端分页、前端表格分页和复杂搜索。

## GitHub 推送

如果本地已经绑定远程仓库：

```bash
git add .
git commit -m "Initialize auto parts shop project"
git push -u origin main
```

如果还没有远程仓库，需要先在 GitHub 创建一个空仓库，然后执行：

```bash
git init
git branch -M main
git remote add origin https://github.com/你的用户名/auto-parts-shop.git
git add .
git commit -m "Initialize auto parts shop project"
git push -u origin main
```
