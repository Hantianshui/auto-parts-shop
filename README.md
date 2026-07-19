# 汽配库存销售管理系统

这是一个面向学习者的 Spring Boot 3 + Vue 3 前后端分离练手项目。项目场景是“汽配店库存与销售管理”：店里维护配件库存，销售时自动扣库存，管理员或店长可以维护基础资料，销售员只能做日常查询和出库。

这个项目的目标不是一次性做成商业系统，而是用一个真实业务场景串起后端、前端、数据库、JWT 认证、权限拦截、Docker 调试、图表统计等常见开发知识点。

## 1. 项目学什么

通过这个项目，你可以练到这些内容：

- Spring Boot 3 项目结构和启动流程
- REST API 的设计方式
- MyBatis-Plus 的实体、Mapper、条件查询和自定义 SQL 扩展
- MySQL 表设计和 Docker 本地数据库调试
- JWT 登录认证流程
- 基于角色的接口权限控制
- Vue 3 组件化页面开发
- Axios 请求封装和 token 自动携带
- Element Plus 后台管理界面开发
- ECharts 数据统计图表
- 前后端跨域和 ngrok 公网调试
- Git/GitHub 项目版本管理

## 2. 技术栈说明

### 后端

| 技术 | 作用 |
| --- | --- |
| Spring Boot 3 | 后端应用框架，负责启动 Web 服务、加载配置、管理 Bean |
| Spring MVC | 编写 REST 接口，例如 `/api/parts`、`/api/sales` |
| MyBatis-Plus | 简化数据库 CRUD，Mapper 继承 `BaseMapper` 后即可拥有常用增删改查 |
| MySQL 8 | 保存用户、配件、销售、分类、供货商等业务数据 |
| JWT | 登录后生成 token，前端后续请求携带 token 访问接口 |
| BCrypt | 对用户密码加密存储，避免明文密码落库 |
| HandlerInterceptor | Spring MVC 拦截器，用来校验 token 和角色权限 |
| Docker Compose | 启动本地 MySQL 容器，避免手动安装和配置数据库 |

### 前端

| 技术 | 作用 |
| --- | --- |
| Vue 3 | 前端页面框架，负责组件化开发和响应式数据绑定 |
| Vite | 前端开发服务器和打包工具，启动快，适合 Vue 3 项目 |
| Element Plus | 后台管理常用 UI 组件库，例如表格、表单、弹窗、菜单 |
| Axios | 封装 HTTP 请求，统一处理 token 和接口响应 |
| ECharts | 绘制销售统计图表 |

## 3. 项目整体架构

项目采用前后端分离架构：

```text
浏览器 / 手机
   |
   | 访问 Vue 前端页面
   v
Vue 3 + Vite 前端  http://localhost:5174
   |
   | Axios 请求 /api/**
   v
Spring Boot 后端   http://localhost:8080
   |
   | MyBatis-Plus 操作数据库
   v
MySQL 数据库       localhost:3307
```

开发环境里，前端通过 Vite 代理访问后端：

```js
proxy: {
  "/api": "http://localhost:8080",
  "/uploads": "http://localhost:8080"
}
```

这样前端代码里只需要请求 `/api/auth/login`，不用写死后端完整地址。

## 4. 业务模块设计

### 4.1 用户与权限模块

相关文件：

- `backend/src/main/java/com/autoparts/shop/entity/User.java`
- `backend/src/main/java/com/autoparts/shop/controller/AuthController.java`
- `backend/src/main/java/com/autoparts/shop/service/AuthService.java`
- `backend/src/main/java/com/autoparts/shop/security/JwtService.java`
- `backend/src/main/java/com/autoparts/shop/security/AuthInterceptor.java`
- `backend/src/main/java/com/autoparts/shop/security/RequireRole.java`

系统内置三种角色：

| 角色 | 账号 | 密码 | 说明 |
| --- | --- | --- | --- |
| ADMIN | admin | 123456 | 管理员，拥有全部权限 |
| MANAGER | manager | 123456 | 店长，可以维护配件、分类、供货商 |
| STAFF | staff | 123456 | 销售员，可以查看库存、销售出库 |

默认账号由 `DataInitializer` 在后端启动时自动创建。

登录流程：

```text
1. 前端提交 username/password
2. AuthController 接收登录请求
3. AuthService 查询 users 表
4. 使用 BCrypt 校验密码
5. JwtService 生成 JWT token
6. 前端把 token 保存到 localStorage
7. 后续请求由 Axios 自动放到 Authorization 请求头
```

请求头格式：

```text
Authorization: Bearer xxx.yyy.zzz
```

权限控制流程：

```text
1. 请求进入后端 `/api/**`
2. AuthInterceptor 拦截请求
3. 放行 `/api/auth/login`
4. 读取 Authorization token
5. JwtService 解析 token 得到当前用户
6. 如果接口上有 @RequireRole，则判断当前用户角色是否允许访问
7. 允许则进入 Controller，不允许返回 403
```

示例：只有管理员可以删除配件。

```java
@DeleteMapping("/{id}")
@RequireRole("ADMIN")
public ApiResult<Void> delete(@PathVariable Long id) {
    partMapper.deleteById(id);
    return ApiResult.ok();
}
```

### 4.2 配件库存模块

相关文件：

- `backend/src/main/java/com/autoparts/shop/entity/Part.java`
- `backend/src/main/java/com/autoparts/shop/mapper/PartMapper.java`
- `backend/src/main/java/com/autoparts/shop/controller/PartController.java`
- `frontend/src/views/PartsView.vue`

配件表保存汽配商品的核心信息：

- 配件名称
- 配件编码
- 所属分类
- 供货商
- 适用车型
- 当前库存
- 低库存预警值
- 进货价
- 售价
- 图片地址

后端提供的能力：

- 查询配件列表
- 按关键词搜索名称、编码、车型
- 查询低库存和缺货配件
- 新增配件
- 编辑配件
- 删除配件

前端页面实现：

- 使用 `el-table` 展示库存列表
- 使用 `el-input` 做关键词搜索
- 使用 `el-select` 做库存状态筛选
- 使用 `el-dialog` 弹窗编辑配件
- 根据 `canManage` 判断是否显示新增、编辑、删除按钮

权限设计：

| 操作 | ADMIN | MANAGER | STAFF |
| --- | --- | --- | --- |
| 查看库存 | 可以 | 可以 | 可以 |
| 新增配件 | 可以 | 可以 | 不可以 |
| 编辑配件 | 可以 | 可以 | 不可以 |
| 删除配件 | 可以 | 不可以 | 不可以 |

### 4.3 销售出库模块

相关文件：

- `backend/src/main/java/com/autoparts/shop/entity/Sale.java`
- `backend/src/main/java/com/autoparts/shop/mapper/SaleMapper.java`
- `backend/src/main/java/com/autoparts/shop/service/SaleService.java`
- `backend/src/main/java/com/autoparts/shop/controller/SaleController.java`
- `frontend/src/views/SalesView.vue`

销售出库是这个项目的核心业务。它不是简单新增一条销售记录，还需要同步修改库存。

后端设计在 `SaleService` 里完成事务逻辑：

```text
1. 根据 partId 查询配件
2. 判断配件是否存在
3. 判断库存是否足够
4. 扣减库存
5. 新增销售记录
6. 返回销售记录
```

这里使用 `@Transactional`，保证库存扣减和销售记录插入要么一起成功，要么一起失败。如果库存不足，后端会抛出异常，前端显示错误信息。

为什么不能只在前端扣库存？

因为前端数据可以被篡改，而且多人同时销售时会有并发问题。库存扣减必须放在后端数据库事务里处理。

### 4.4 分类和供货商模块

相关文件：

- `backend/src/main/java/com/autoparts/shop/entity/Category.java`
- `backend/src/main/java/com/autoparts/shop/entity/Supplier.java`
- `backend/src/main/java/com/autoparts/shop/controller/SimpleDataController.java`
- `frontend/src/views/BaseDataView.vue`

分类用于管理配件类型，例如：

- 保养件
- 制动系统
- 电器件
- 轮胎轮毂

供货商用于记录采购来源，例如：

- 供货商名称
- 电话
- 地址
- 备注

当前项目里分类和供货商是基础版，只实现了新增和查询。后续可以扩展编辑、删除、详情、分页、关联配件数量等功能。

### 4.5 数据看板模块

相关文件：

- `backend/src/main/java/com/autoparts/shop/controller/DashboardController.java`
- `frontend/src/views/DashboardView.vue`

数据看板负责把业务数据转成管理者容易理解的指标。

当前后端统计：

- 配件种类数
- 低库存数量
- 库存总金额
- 本月销售额
- 按配件统计销售额

前端展示：

- 四个指标卡片
- 一个 ECharts 柱状图

这里的学习重点是：后端负责算数据，前端负责展示数据。不要把复杂统计都放在前端算，否则后面数据量大了会不好维护。

### 4.6 图片上传模块

相关文件：

- `backend/src/main/java/com/autoparts/shop/controller/UploadController.java`
- `backend/src/main/java/com/autoparts/shop/config/WebConfig.java`

图片上传接口：

```text
POST /api/upload/image
```

当前实现逻辑：

```text
1. 前端上传 MultipartFile
2. 后端生成 UUID 文件名
3. 保存到 uploads 目录
4. 返回 /uploads/xxx.jpg
5. WebConfig 把 /uploads/** 映射到本地文件目录
```

目前前端配件编辑页还没有接入图片上传组件，这是一个很适合你下一步练手的小功能。

## 5. 后端分层怎么理解

后端代码按职责拆成几层：

```text
Controller：接收前端请求，返回接口响应
Service：写业务逻辑，例如销售出库、登录校验
Mapper：操作数据库
Entity：和数据库表对应的数据对象
Security：认证和权限相关代码
Config：项目配置、跨域、初始化数据
Common：统一返回结构和异常处理
```

一次典型请求的调用链：

```text
前端点击“记录销售”
   |
Axios POST /api/sales
   |
AuthInterceptor 校验 JWT
   |
SaleController.create()
   |
SaleService.createSale()
   |
PartMapper.selectById()
SaleMapper.insert()
   |
MySQL
```

## 6. 数据库设计

建表脚本在：

```text
backend/src/main/resources/db/schema.sql
```

当前核心表：

| 表名 | 作用 |
| --- | --- |
| users | 用户和角色 |
| categories | 配件分类 |
| suppliers | 供货商 |
| parts | 配件库存 |
| sales | 销售记录 |

主要关系：

```text
parts.category_id  -> categories.id
parts.supplier_id  -> suppliers.id
sales.part_id      -> parts.id
sales.operator_id  -> users.id
```

当前 SQL 没有显式加外键，这是为了练手阶段方便反复修改表结构。项目成熟后可以加外键约束，或者在业务代码里严格校验关联数据。

## 7. 前端设计怎么理解

前端入口：

- `frontend/src/main.js`：创建 Vue 应用，加载 Element Plus 和全局样式
- `frontend/src/App.vue`：管理整体布局、登录状态、菜单切换、角色判断
- `frontend/src/api/http.js`：统一封装 Axios

页面组件：

| 文件 | 页面 | 功能 |
| --- | --- | --- |
| `LoginView.vue` | 登录页 | 输入账号密码，保存 token |
| `DashboardView.vue` | 数据看板 | 展示指标和图表 |
| `PartsView.vue` | 配件库存 | 查询、新增、编辑、删除配件 |
| `SalesView.vue` | 销售出库 | 选择配件，记录销售，扣减库存 |
| `BaseDataView.vue` | 分类供货商 | 管理基础资料 |

前端登录状态保存在浏览器：

```text
localStorage.auto-parts-token
localStorage.auto-parts-user
```

Axios 请求拦截器会自动读取 token：

```js
const token = localStorage.getItem("auto-parts-token");
if (token) config.headers.Authorization = `Bearer ${token}`;
```

## 8. 启动项目

### 8.1 启动 MySQL

```bash
docker compose up -d
```

数据库连接信息：

```text
host: localhost
port: 3307
database: auto_parts_shop
username: root
password: root123456
```

注意：项目使用 `3307` 是为了避开你本机已有的 MySQL `3306`。

### 8.2 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端地址：

```text
http://localhost:8080
```

### 8.3 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：

```text
http://localhost:5174
```

## 9. 常用接口

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

## 10. 如何新增一个后端接口

以“新增客户管理”为例。

### 第一步：新增数据库表

在 `schema.sql` 里加：

```sql
CREATE TABLE IF NOT EXISTS customers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  phone VARCHAR(32),
  car_no VARCHAR(32),
  note VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 第二步：新增 Entity

创建 `Customer.java`：

```java
@TableName("customers")
public class Customer {
    private Long id;
    private String name;
    private String phone;
    private String carNo;
    private String note;
    private LocalDateTime createdAt;
}
```

学习时可以先手写 getter/setter，后面再引入 Lombok 简化代码。

### 第三步：新增 Mapper

```java
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
```

### 第四步：新增 Controller

```java
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerMapper customerMapper;

    public CustomerController(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    @GetMapping
    public ApiResult<List<Customer>> list() {
        return ApiResult.ok(customerMapper.selectList(null));
    }

    @PostMapping
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResult<Customer> save(@RequestBody Customer customer) {
        if (customer.getId() == null) customerMapper.insert(customer);
        else customerMapper.updateById(customer);
        return ApiResult.ok(customer);
    }
}
```

### 第五步：前端新增页面

1. 在 `frontend/src/views` 下创建 `CustomersView.vue`
2. 使用 `el-table` 展示客户
3. 使用 `el-dialog` 新增和编辑客户
4. 在 `App.vue` 菜单里加一个入口

## 11. 如何新增 Mapper 自定义 SQL

简单 SQL 可以直接写注解：

```java
@Mapper
public interface PartMapper extends BaseMapper<Part> {
    @Select("""
        SELECT *
        FROM parts
        WHERE stock <= warning_stock
        ORDER BY stock ASC
    """)
    List<Part> selectLowStockParts();
}
```

复杂 SQL 建议使用 XML：

1. 在 `application.yml` 中配置：

```yml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

2. 创建：

```text
backend/src/main/resources/mapper/PartMapper.xml
```

3. 写 XML SQL：

```xml
<mapper namespace="com.autoparts.shop.mapper.PartMapper">
  <select id="selectLowStockParts" resultType="com.autoparts.shop.entity.Part">
    SELECT *
    FROM parts
    WHERE stock <= warning_stock
    ORDER BY stock ASC
  </select>
</mapper>
```

4. 在 Java Mapper 里声明方法：

```java
List<Part> selectLowStockParts();
```

## 12. 如何扩展功能

### 12.1 接入配件图片上传

当前后端已经有上传接口，但前端还没接入。

扩展思路：

1. 在 `PartsView.vue` 的编辑弹窗中加入 `el-upload`
2. 上传地址使用 `/api/upload/image`
3. 上传成功后把返回的 `url` 保存到 `form.imageUrl`
4. 表格中增加图片列

### 12.2 新增采购入库

现在只有销售出库，缺少采购入库。

建议新增表：

```text
purchase_orders
purchase_items
stock_records
```

核心设计：

```text
采购入库 -> 增加库存 -> 写库存流水
销售出库 -> 扣减库存 -> 写库存流水
```

这样以后能追踪每一次库存变化。

### 12.3 新增库存流水

库存流水可以记录：

- 入库
- 出库
- 退货
- 盘点调整

建议字段：

```text
part_id
change_type
quantity_change
stock_before
stock_after
related_id
operator_id
created_at
```

### 12.4 新增分页查询

当前列表是一次性查全部，数据多了以后不合适。

后端可以用 MyBatis-Plus `Page`：

```java
Page<Part> page = partMapper.selectPage(new Page<>(pageNo, pageSize), query);
```

前端使用 Element Plus：

```vue
<el-pagination />
```

### 12.5 新增更细角色权限

现在权限是简单角色判断。后续可以升级为：

```text
users 用户表
roles 角色表
permissions 权限表
role_permissions 角色权限关联表
user_roles 用户角色关联表
```

这样权限可以从“角色写死在代码里”升级为“后台可配置”。

### 12.6 增加登录过期处理

当前 token 过期后，前端会收到错误。可以在 Axios 响应拦截器里判断 401：

```text
1. 清除 localStorage token
2. 回到登录页
3. 提示“登录已过期，请重新登录”
```

## 13. 推荐学习顺序

1. 先理解数据库表：`users`、`parts`、`sales`
2. 看登录流程：`AuthController` -> `AuthService` -> `JwtService`
3. 看请求拦截：`AuthInterceptor` 如何判断 token 和角色
4. 看库存查询：`PartController` 如何用 MyBatis-Plus 查数据
5. 看销售出库：`SaleService` 如何用事务扣库存
6. 看前端请求：`http.js` 如何自动带 token
7. 看页面组件：`PartsView.vue`、`SalesView.vue`
8. 自己扩展一个客户管理模块
9. 再扩展采购入库和库存流水
10. 最后再做分页、搜索、权限配置、部署

## 14. 常见问题

### 登录 403

如果用 ngrok 手机访问，后端需要允许 ngrok 域名跨域。当前项目已经在 `WebConfig` 中允许：

```text
https://*.ngrok-free.dev
```

前端 Vite 也需要允许当前 ngrok host，否则会提示 `Blocked request`。

### MySQL 连接失败

确认数据库端口是 `3307`：

```text
jdbc:mysql://localhost:3307/auto_parts_shop
```

如果 Docker 容器没启动，执行：

```bash
docker compose up -d
```

### 前端请求不到后端

确认后端在 `8080`，前端在 `5174`：

```text
http://localhost:8080
http://localhost:5174
```

如果前端通过 Vite 代理访问后端，检查 `frontend/vite.config.js`。

## 15. GitHub

远程仓库：

```text
git@github.com:Hantianshui/auto-parts-shop.git
```

常用提交命令：

```bash
git status
git add .
git commit -m "描述你的修改"
git push
```
