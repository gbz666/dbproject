## Java & Spring Boot 编码规范（项目内约定）

> 本文档总结本项目当前 Controller / Service / Mapper 等常见代码风格，便于后续开发保持一致。优先以已有代码为准，如有冲突以实际实现为主。

### 1. Controller 层

- **类注解与路径**
  - 统一使用 `@RestController` + `@RequestMapping("/api/xxx")`，前缀全部挂在 `/api` 之下。
  - 按资源划分 Controller，例如 `ProductsController`、`SalesInvoiceController`，不要做“超级 Controller”。

- **依赖注入**
  - Controller 使用**构造器注入 + `final` 字段**：
    ```java
    @RestController
    @RequestMapping("/api/products")
    public class ProductsController {

        private final ProductsService productsService;

        @Autowired
        public ProductsController(ProductsService productsService) {
            this.productsService = productsService;
        }
    }
    ```

- **方法签名与返回值**
  - 统一返回 `ResponseEntity<Result<...>>`，不直接返回业务对象：
    ```java
    public ResponseEntity<Result<ProductVO>> getProduct(@PathVariable String productCode) {
        ProductVO product = productsService.getProductDetail(productCode);
        return ResponseEntity.ok(Result.success(product));
    }
    ```
  - 创建成功的接口使用 `201 Created`：
    ```java
    ProductVO newProduct = productsService.createProduct(request, currentStaffId);
    Result<ProductVO> result = Result.createsuccess(newProduct);
    result.setMessage("产品创建成功");
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
    ```
  - 删除接口使用 `204 No Content`，响应体通常为 `null`：
    ```java
    productsService.deleteProduct(productCode);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    ```

- **统一返回包装 `Result` 使用规范**
  - 成功：
    - `Result.success(data)`：200 + 默认消息“操作成功”
    - `Result.createsuccess(data)`：201 + 默认消息“创建成功”
    - 如需自定义成功消息：先调用工厂方法，再显式 `setMessage`。
  - 失败：
    - 业务失败或资源不存在：使用 `Result.fail(code, message)` 或 `Result.fail(code, message, data)`，同时配合合适的 HTTP 状态码，例如：
      ```java
      if (vo == null) {
          Result<SalesInvoiceVO> result = Result.fail(404, "销项发票不存在或已删除", null);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
      }
      ```

- **注释风格**
  - 使用 Javadoc，在方法上标明 **HTTP 动作 + 路径 + 关键参数 + 返回值**：
    ```java
    /**
     * GET /api/products: 分页查询产品列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param productName 产品名称（可选）
     * @return 200 OK
     */
    ```
  - 不写与代码完全重复的行级注释，注释主要解释“为什么”而不是“做什么”。

### 2. Service 层

- **职责**
  - Service 负责业务逻辑与事务控制：组装 DTO/PO/VO、计算金额、控制一票多明细等。
  - Controller 不直接操作 Mapper。

- **注解与事务**
  - 类级别使用 `@Service`。
  - 只读查询用 `@Transactional(readOnly = true)`，修改/创建用 `@Transactional(rollbackFor = Exception.class)`。

- **命名**
  - 增删改查方法常用命名：
    - `findPage(...)`：分页查询
    - `getById(...)`：按主键查询详情
    - `createXxx(...)`、`updateXxx(...)`、`deleteXxx(...)`

### 3. Mapper 与 XML

- Mapper 接口命名为 ` XxxMapper`，位于 `com.database.mapper` 包下，与 `resources/com/database/mapper/*.xml` 一一对应。
- XML 中：
  - 使用 `<resultMap>` + `<sql id="Base_Column_List">` 提取公共列。
  - 复杂查询（分页 + 聚合）优先在 SQL 中完成，必要时在 Service 层用 Java 再聚合。

### 4. DTO / VO

- DTO（`com.database.dto`）
  - 表示**请求体或查询条件**，字段对齐接口入参。
  - 命名以 `Request`、`DTO`、`Query` 结尾。

- VO（`com.database.vo`）
  - 表示**接口返回结果**，可以在 DTO 基础上扩展统计字段。
  - 示例：`SalesInvoiceVO extends SalesInvoiceDTO`，新增 `avgInvoiceDays`、`pendingInvoiceAmount` 等。

### 5. 前端（简要约定）

- TypeScript 类型放在 `frontend/src/types` 中：
  - DTO 类型与后端 DTO 一一对应（命名一致）。
  - VO 类型与后端 VO 对应，并在需要时增加前端专用字段（如 `details`、`items`）。
- 统一通过 `src/api/*Api.ts` + `src/services/*Service.ts` 间接调用后端，视图组件只依赖 Service/Store。

---

## 如何让 Agent 启动时先看 README 与本规范

在项目根目录下创建 `.cursor/rules/` 目录，并新增一个始终生效的规则文件，例如：

```markdown
---
description: Always read README and CODE_STYLE before coding
alwaysApply: true
---

- 在处理本仓库任务前，优先快速浏览根目录的 `README.md`，了解项目结构与约定。
- 紧接着阅读 `docs/CODE_STYLE.md`，按照其中的 Controller / Service / Mapper / 前端编码规范进行实现与修改。
```

> 说明：Cursor 会在每次会话开始时加载 `alwaysApply: true` 的规则，从而保证 Agent 先看到 README 与本规范，再开始编码。

