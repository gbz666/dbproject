## 项目简介

这是一个面向中小企业的**进销存 + 客户/供应商管理 + 基础财务对账**系统，用于课程设计与实战练习。项目采用前后端分离架构，包含完整的员工/角色体系、客户与供应商管理、商品与库存管理、销售/采购订单、出入库单、发票及收付记录等模块，并配有较完善的数据库设计与并发/Redis 相关文档。

## 技术栈总览

- **前端**
  - 框架：Vue 3 + TypeScript
  - 构建：Vite
  - 路由与状态：Vue Router、Pinia
  - UI 组件：Element Plus
  - 网络：Axios
- **后端**
  - 框架：Spring Boot 3（Java 17）
  - ORM / 持久化：MyBatis、PageHelper
  - 数据源：Druid 连接池
  - 认证与安全：JWT（jjwt）、Spring Security Crypto（密码加密）
  - 校验与切面：Spring Validation、AOP
  - 缓存 / 中间件：Spring Data Redis（封装在多份 Redis 说明文档中）
  - 工具：EasyExcel（Excel 导入）、Lombok
- **数据层**
  - 数据库：MySQL 8，完整建表脚本见 `db.sql`
  - 说明：大部分业务表带有 `created_at/updated_at`、`created_by_id/updated_by_id`、软删除标记 `is_deleted` 等审计字段

## 目录结构（核心部分）

- `backend/`：Spring Boot 后端工程
  - `pom.xml`：后端 Maven 配置，声明 Spring Boot、MyBatis、Druid、Redis、JWT 等依赖
  - `src/main/java/com/database/`：
    - `controller/`：REST 接口层（如 `AuthController`、`CustomerController`、`ProductsController`、`InventoryController`、`SalesOrderController`、`PurchaseOrderController`、`StockInController`、`OutboundOrdersController`、`PurchaseInvoiceController`、`SalesInvoiceController`、`FinanceReconciliationController` 等）
    - `service/`：业务服务层（如 `AuthService`、`StaffService`、`PurchaseOrderService`、`OutBoundOrdersService`、`PurchaseInvoiceService`、`SalesInvoiceService`、`ExcelImportService`、`FinanceReconciliationService` 等）
    - `mapper/`：MyBatis Mapper 接口，与资源目录下的 XML 映射文件一一对应
    - `pojo/`、`dto/`、`vo/`：实体对象、请求/查询 DTO、返回视图对象
    - `config/`：如 `RedisConfig` 等配置类
  - `src/main/resources/com/database/mapper/`：MyBatis XML 映射文件（客户、供应商、产品、库存、订单、出入库、发票、财务对账等）
- `frontend/`：Vue 3 + Vite 前端工程
  - `package.json`：前端依赖与脚本（`pnpm dev`、`pnpm build` 等）
  - `src/router/`：前端路由配置
  - `src/views/`：页面视图（首页、个人信息、销售/采购订单列表、出入库、进销项发票、财务对账等）
  - `src/stores/`：Pinia 状态（如 `authStore`、`purchaseInvoiceStore`、`salesInvoiceStore` 等）
  - `src/api/`、`src/services/`：对接后端的 API 封装与业务服务
  - `src/types/`：前后端共享的 DTO/VO TypeScript 类型定义
- `docs/`：项目说明与进阶技术文档
  - `OVERVIEW.md`：系统整体架构、前后端接口约定（统一返回 `{ code, message, data }`，认证使用 JWT，接口前缀 `/api` 等）
  - `CONCURRENCY.md`：并发与锁设计（库存原子扣减、防止超卖、悲观锁/乐观锁、Redis 分布式锁等）
  - `ADVANCED_TECH_OVERVIEW.md`：Elasticsearch、锁方案等进阶技术与简历写法建议
  - 其他文档：`RESUME_OPTIMIZATION.md` 等，侧重如何把本项目经验写进简历
- `scripts/`：数据库辅助脚本
  - `db.sql`：主库建表脚本，定义员工、角色、客户/供应商、商品分类/商品、仓库/库存、销售/采购订单、出入库、发票和收付记录等表结构以及索引
  - `reset_database.sql`：按依赖顺序清空/重建数据库用的重置脚本
  - `init_test_user.sql`：初始化测试员工/角色等数据（便于本地快速体验）
  - 其他：按需要补充回填价格、扩展字段等脚本
- 根目录其他文档：
  - `README_REDIS_AUTH.md`、`REDIS_MULTI_PURPOSE.md`、`REDIS_TEMPLATE_EXPLAINED.md`、`REDIS_OPS_VALUE_EXPLAINED.md`、`REDIS_WRAPPER_SERVICE.md`：Redis 相关的认证、多用途封装及使用说明
  - `SERVICE_LAYER_ABSTRACTION.md`：服务层抽象设计说明
  - `CHANGES_SUMMARY.md`：重要变更记录

## 业务模块概览

- **员工与权限**
  - 表：`staffs`、`roles`、`staff_roles`
  - 支持员工基本信息、角色分配与审计字段
  - 认证：`/api/auth/login` 使用员工姓名 + 密码登录，返回 JWT，前端存储于 Cookie（默认键 `access_token`）并在请求头附带 `Authorization: Bearer <token>`
- **客户与供应商**
  - 表：`customers`、`suppliers`、`contacts`
  - 支持多联系人、销售/跟进/归属员工、付款条款、软删除与审计字段
  - 接口前缀 `/api/customers`、`/api/suppliers`
- **商品与库存**
  - 表：`product_categories`、`products`、`warehouses`、`inventory`
  - 支持商品分类、成本价/参考售价、复合主键库存（按产品 + 仓库）
  - 控制库存增减、记录最近出入库时间，避免负库存（详见并发文档）
- **销售与出库**
  - 表：`sales_orders`、`sales_order_items`、`outbound_orders`、`outbound_order_items`、`sales_invoices`、`sales_invoice_details`、`payment_receipts`
  - 完整链路：销售订单 → 出库单 → 销项发票 → 收款记录
- **采购与入库**
  - 表：`purchase_orders`、`purchase_order_items`、`stock_ins`、`stock_in_items`、`purchase_invoices`、`purchase_invoice_details`、`payment_expenses`
  - 完整链路：采购订单 → 入库单 → 进项发票 → 付款记录
- **财务对账**
  - 通过 `FinanceReconciliationController` 及相关 Service/Mapper，将发票与收付记录进行对账，支持按客户/供应商、订单等维度查询。

## 运行与开发（概览）

> **提示**：具体启动脚本、端口、数据库配置请以 `backend` 工程中的 `application.yml`/`application.properties` 和前端 `.env` 为准，这里给出通用流程。

### 1. 准备环境

- 安装 **MySQL 8**，创建数据库（如 `company_manage`），执行根目录下 `db.sql` 建表。
- 可选：执行 `scripts/init_test_user.sql` 和其他初始化脚本以导入测试数据。
- 安装 **Redis**（如果要启用 Token 存储、记住登录、多用途缓存等功能，详见各 Redis 文档）。
- 安装 **JDK 17** 与 **Maven 3+**。
- 安装 **Node.js（建议 20+）** 与 **pnpm**。

### 2. 启动后端（Spring Boot）

在 `backend/` 目录：

1. 配置 `application.yml` 中的 `spring.datasource`、`spring.redis`、JWT 密钥等。
2. 运行：
   - 使用 IDE（IntelliJ IDEA）直接运行 Spring Boot 主类，或
   - 命令行执行 `mvn spring-boot:run`。
3. 默认服务端口示例：`http://localhost:8080`，REST 接口统一前缀 `/api`。

### 3. 启动前端（Vue 3 + Vite）

在 `frontend/` 目录：

```sh
pnpm install
pnpm dev
```

- 默认开发地址通常为 `http://localhost:5173/`（以 Vite 配置为准）。
- 前端通过环境变量（如 `VITE_API_BASE_URL`）配置后端地址，一般指向 `http://localhost:8080`。

## 前后端接口约定（简要）

- **统一返回格式**：`{ code, message, data }`
  - `code`：业务状态码（0 或 200 表示成功，其他为错误）
  - `message`：提示信息
  - `data`：实际返回数据
- **认证**
  - 登录：`POST /api/auth/login`，请求体常见字段为 `staffName` 与 `password`
  - 成功后返回 JWT，由前端写入 Cookie（`access_token`），后续请求在 Header 附带 `Authorization: Bearer <token>`
  - 除登录/注册等公开接口外，其余接口默认需要携带 Token
- **分页**
  - 请求参数统一使用：`pageNum`、`pageSize`（从 1 开始计数）
- **操作人**
  - 某些接口通过 Query 参数显式传入（如 `currentStaffId`、`operatorId`）
  - 某些接口（如进项发票）使用拦截器从 Token 中解析当前员工 ID 并注入，无需前端传递

## 文档阅读建议

- **想快速理解整体架构**：先看 `docs/OVERVIEW.md`
- **想优化并发与库存设计**：看 `docs/CONCURRENCY.md`
- **想在简历中充分利用本项目**：看 `docs/ADVANCED_TECH_OVERVIEW.md` 与 `docs/RESUME_OPTIMIZATION.md`
- **想深入 Redis 使用方式**：阅读根目录的 Redis 系列文档

本 `README.md` 旨在作为**课程项目总入口说明**，帮助你或阅卷老师快速了解项目结构、技术栈以及如何启动/扩展。如需针对某一模块（如库存、发票、Redis 鉴权等）做更深入改动，可结合对应的文档与代码目录一起查看。

