# 项目优化建议（简历向 + 技术拓展）

基于当前代码与文档的梳理，下面按「性价比」和「简历可写性」给出可落地的优化方向，便于你既把项目做扎实，又能在简历上体现更多技术点。

---

## 一、优先建议（高性价比、易写进简历）

### 1. 单元测试 + 接口测试

**现状**：项目内暂无测试代码。

**建议**：
- **后端**：引入 **JUnit 5 + Mockito**，为 1～2 个核心 Service（如 `AuthService`、`SalesOrderService`）写单元测试；为 1～2 个关键接口写 **MockMvc** 或 **RestAssured** 的接口测试。
- **前端**：用 **Vitest** 为 1～2 个工具函数或简单页面逻辑写测试（可选）。

**简历可写**：熟悉 JUnit5、Mockito、MockMvc / 接口自动化测试；具备单元测试与接口测试经验。

**落地步骤**：
- 在 `pom.xml` 增加：`spring-boot-starter-test`（一般已带 JUnit5、Mockito、MockMvc）。
- 新建 `src/test/java`，例如：
  - `AuthServiceTest`：mock `StaffsMapper`、`TokenService`，测登录成功/失败、密码错误等。
  - `AuthControllerTest`：用 MockMvc 测 `POST /api/auth/login` 返回 200/401 及 body 结构。

---

### 2. 配置与敏感信息安全

**现状**：`application.yaml` 中数据库密码、JWT secret 等明文写死。

**建议**：
- 使用 **环境变量** 或 **`application-{profile}.yml`** 区分 dev/test/prod，敏感项不提交仓库。
- 在根目录增加 `.env.example`（或文档）说明：`DB_PASSWORD`、`JWT_SECRET`、`REDIS_PASSWORD` 等，生产环境用环境变量注入。
- 若部署到云或容器，可顺带用 **Spring Cloud Config** 或 12-Factor 配置方式（简历可写「按环境管理配置、敏感信息不落库」）。

**简历可写**：按环境管理配置；敏感信息环境变量化；了解 12-Factor 配置实践。

---

### 3. Docker 化部署

**现状**：无 Dockerfile / docker-compose。

**建议**：
- 为 **后端** 写多阶段构建的 **Dockerfile**（基于 Eclipse Temurin 或 openjdk 镜像）。
- 用 **docker-compose.yml** 编排：`app` + `mysql` + `redis`，一键 `docker-compose up` 跑起整套环境。
- 前端可选用 Nginx 镜像构建静态资源，或先保持 `npm run build` 后由 Nginx 托管（compose 里加一层 Nginx 即可）。

**简历可写**：熟悉 Docker 多阶段构建、docker-compose 编排；具备本地/演示环境容器化经验。

---

### 4. API 文档（OpenAPI / Swagger）

**现状**：无 Swagger/OpenAPI。

**建议**：
- 引入 **SpringDoc OpenAPI 3**（适配 Spring Boot 3），在 Controller 上补充 `@Tag`、`@Operation`、`@Parameter`，必要时用 `@Schema` 描述 DTO。
- 配置后访问 `/v3/api-docs`、`/swagger-ui.html` 即可在线看接口文档与调试。

**简历可写**：使用 OpenAPI 3 维护 REST API 文档；与前端协作规范接口契约。

---

### 5. 全局异常与校验统一返回

**现状**：已有 `GlobalExceptionHandler`，但 `MethodArgumentNotValidException`（如 `@NotBlank` 校验失败）未单独处理，日志里曾出现 500。

**建议**：
- 在 `GlobalExceptionHandler` 中增加对 **`MethodArgumentNotValidException`** 的处理：返回 **400**，body 中 `code=400`，`message` 为第一条字段错误或「参数校验失败」，`data` 可为字段级错误列表（便于前端展示）。
- 避免将校验失败当作 500 返回，同时保证与现有 `Result` 结构一致。

**简历可写**：统一异常处理、参数校验与错误码规范；提升接口可维护性。

---

### 6. 前后端统一「成功」标识（Result.success）

**现状**：后端 `Result` 仅有 `code`、`message`、`data`；前端 `ApiResult` 和 `httpClient` 中使用了 `success` 字段判断业务是否成功。

**建议**：
- 在后端 `Result` 中增加 **`success`** 字段（或由现有 `code` 派生）：例如 `code >= 200 && code < 300` 为成功，序列化时带上 `success: true/false`。
- 这样前端类型与真实响应一致，接口契约清晰，也便于后续扩展。

---

## 二、进阶建议（技术深度 + 简历亮点）

### 7. 简单 CI（GitHub Actions / 其他）

**建议**：在仓库中增加 **GitHub Actions**（或 GitLab CI）工作流：
- 触发：push / PR 到 main。
- 步骤：拉取代码 → 后端 `mvn test`（若有测试）→ 前端 `npm ci && npm run type-check`（或 `build`）。
- 可选：构建 Docker 镜像并 push 到镜像仓库。

**简历可写**：使用 GitHub Actions 做 CI；自动化测试与构建流水线。

---

### 8. 接口限流 / 防刷（Redis）

**建议**：对登录、注册等接口用 **Redis** 做简单限流（如固定窗口或滑动窗口），例如：同一 IP 每分钟最多 N 次。  
既体现「除 Token 外 Redis 的用法」，又提升安全性。

**简历可写**：基于 Redis 的接口限流与防刷；与网关/拦截器结合。

---

### 9. 健康检查与可观测（Spring Boot Actuator）

**建议**：
- 引入 **spring-boot-starter-actuator**，开启 `health`（可包含 DB、Redis 健康）、按需开启 `info`。
- 生产环境可关闭敏感端点或通过网关/权限控制访问；配合 Docker/K8s 做存活探针。

**简历可写**：使用 Actuator 做健康检查与基础可观测；为容器化部署提供就绪/存活探针。

---

### 10. 前端工程化与体验

- **ESLint + Prettier**：统一代码风格，减少低级错误。
- **路由懒加载**：对非首屏页面使用 `() => import('...')`，减小首屏体积。
- **请求层**：对 401 响应头中的 `X-New-Token` 做自动替换并持久化（若后端已支持刷新），避免频繁掉线。
- **错误边界 / 全局错误提示**：对未捕获的请求错误做统一提示或错误页，提升体验。

**简历可写**：前端工程化（ESLint/Prettier）、性能优化（懒加载）、统一错误处理与 Token 刷新策略。

---

## 三、可选（时间充裕时）

| 方向           | 内容简述                               | 简历可写点               |
|----------------|----------------------------------------|--------------------------|
| Redis 缓存     | 对 `/api/util` 下拉等读多写少接口做短期缓存 | 缓存策略、缓存失效       |
| 日志与审计     | 关键业务操作写审计日志表或日志文件     | 操作审计、可追溯         |
| 分页与查询     | 统一分页参数、排序、过滤（如 currentStaffId） | 接口规范、可扩展性       |
| 读写分离/多数据源 | 若课设允许，可做 MySQL 只读从库 + 简单读写分离 | 读写分离、高可用（慎用） |

---

## 四、建议实施顺序（兼顾简历与时间）

1. **先做**：配置安全（环境变量 + 不提交密码） + 全局异常补全（校验异常 400）+ Result 增加 `success`。  
   → 投入小，接口更规范，面试时可说「注重安全与接口一致性」。
2. **接着**：单元/接口测试（至少 Auth + 一个业务接口）+ API 文档（SpringDoc）。  
   → 简历上可写测试与文档，面试可现场演示 Swagger。
3. **再上**：Docker + docker-compose 一键运行。  
   → 简历写「容器化部署」，演示时一条命令起整套环境。
4. **有余力**：CI（GitHub Actions）、Actuator 健康检查、Redis 限流、前端懒加载与 Token 刷新。  
   → 进一步体现工程化与多技术栈。

---

## 五、简历项目描述可参考句式（技术关键词）

- 技术栈：Vue3 + TypeScript + Pinia + Element Plus / Spring Boot 3 + MyBatis + MySQL + Redis + JWT。
- 负责/参与：RESTful API 设计与实现、统一异常处理与参数校验、JWT+Redis 认证与 Token 刷新、按环境管理配置与敏感信息、Docker Compose 编排部署、SpringDoc 接口文档、JUnit5/MockMvc 单元与接口测试、基于 Redis 的登录限流。

按上面顺序挑 3～5 项做实，就足够在简历和面试中展开讲清楚、体现技术广度与一定深度。
