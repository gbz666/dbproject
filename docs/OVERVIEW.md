# 项目总览：架构与技术栈

## 1. 架构

- **前端**：单页应用（SPA），通过 REST API 访问后端；开发环境通过 Vite 代理或直连后端地址。
- **后端**：单体 Spring Boot 应用，提供 REST 接口；认证通过 JWT（请求头 `Authorization: Bearer <token>`），需登录的接口由拦截器校验 Token 并注入 `currentStaffId`。
- **数据**：MySQL 持久化；Redis 用于 Token 存储/刷新（可选）。
- **前后端约定**：后端统一返回 `{ code, message, data }`；创建成功 201、删除成功 204、其余成功 200；除登录/注册外，请求需带 Token。

---

## 2. 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3、TypeScript、Vite、Vue Router、Pinia、Element Plus、Axios |
| 后端 | Spring Boot 3、Java 17、MyBatis、PageHelper、Druid、JWT（jjwt）、Spring Validation、AOP、Spring Data Redis |
| AI Agent | FastAPI (Python)、LangGraph (ReAct Agent)、OpenAI SDK、matplotlib |
| 数据库 | MySQL 8、Redis（Token/会话） |
| 其他 | EasyExcel（Excel 导入）、Lombok |

---

## 3. 前后端接口约定

- **Base URL**：前端 `VITE_API_BASE_URL` 或默认 `http://localhost:8080`，接口前缀 `/api`。
- **认证**：登录接口 `POST /api/auth/login` 请求体 `{ "staffName", "password" }`（仅使用员工姓名，无 username），成功后前端将返回的 token 存入 Cookie（key: `access_token`），后续请求在 Header 中携带 `Authorization: Bearer <token>`。
- **操作人**：需要“当前操作员”的接口有两种方式：  
  - **Query 参数**：如 `currentStaffId`（客户、供应商、销售订单、采购订单、产品等）、出库/入库的 `operatorId`。  
  - **请求属性**：进项发票的 `currentStaffId` 由后端拦截器从 Token 解析后注入，前端无需传。
- **分页**：统一使用 `pageNum`、`pageSize`（从 1 开始），与后端 PageHelper 一致。

### 3.1 后端接口一览（与前端对接）

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 认证 | `/api/auth` | login, logout, refresh |
| 客户 | `/api/customers` | GET 分页, POST, PUT /{customerCode}, DELETE /{customerCode}，query: currentStaffId |
| 供应商 | `/api/suppliers` | GET 分页, POST, PUT /{supplierCode}, DELETE /{supplierCode}，query: currentStaffId |
| 产品 | `/api/products` | GET 分页, GET /{productCode}, POST, PUT /{productCode}, DELETE /{productCode}，query: currentStaffId |
| 销售订单 | `/api/salesOrder` | GET 分页, POST, PUT /{id}, DELETE /{id}，query: currentStaffId |
| 采购订单 | `/api/purchaseOrder` | GET /page 分页, POST, PUT /{id}, DELETE /{id}，query: currentStaffId |
| 出库 | `/api/outbound` | GET /page, POST /create, PUT /update, DELETE /{id}，query: operatorId |
| 入库 | `/api/stock-in` | GET /page, POST /create, PUT /update, DELETE /{id}，query: operatorId |
| 库存 | `/api/inventory` | GET 分页 |
| 进项发票 | `/api/purchase-invoices` | GET /page, POST, PUT /{id}, DELETE /{id}；currentStaffId 由拦截器注入 |
| 工具/下拉 | `/api/util` | GET /search/customer, /search/product, /search/supplier, /search/productType |
| 员工 | `/api/staff` | GET?name= 查员工ID |
| 财务对账 | `/api/finance` | GET /purchase-reconciliation |
| Excel | `/api/excel` | POST /init-product-categories, POST /import（file） |
| AI 对话 | `/api/ai` | POST /generate-sql, POST /execute-sql, GET /chart/{id} |
| AI 对话管理 | `/api/ai/conversations` | CRUD + 消息保存/加载 |

前端调用上述路径与参数即可与后端一一对应；若某处命名不一致（如前端用 `currentUserId`、后端用 `currentStaffId`），以本表为准在前后端统一其一。
