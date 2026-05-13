# AI Agent 缺失功能实现计划

> 执行顺序：Phase 1 → Phase 2 → Phase 3，每阶段完成后测试并等待用户确认。

---

## Phase 1：审计日志写入（ai_sql_exec_log）✅ 已完成

**目标**：每次通过 AI 接口执行 SQL 后，自动写入审计日志。

### 1.1 新建实体 `AiSqlExecLog`

- **文件**：`backend/src/main/java/com/database/pojo/AiSqlExecLog.java`
- **字段**（对应 `ai_sql_exec_log` 表）：
  - `Long id`
  - `Long memoryId`（可为空，Phase 2 关联）
  - `Long userId`
  - `String sqlHash`（SHA-256）
  - `String sqlText`
  - `String params`（JSON 字符串）
  - `Integer rowCount`
  - `Integer latencyMs`
  - `String status`（success/explain_fail/security_reject/timeout/error）
  - `String errorMsg`
  - `Date createdAt`
- **风格**：`@Data`，手写 `equals/hashCode/toString`，与 `Customers.java` 保持一致

### 1.2 新建 Mapper 接口 + XML

- **接口**：`backend/src/main/java/com/database/mapper/AiSqlExecLogMapper.java`
  - `void insert(AiSqlExecLog log)` — 插入一条日志
  - `List<AiSqlExecLog> selectByUserId(Long userId)` — 按用户查询（可选，用于统计）
  - `List<AiSqlExecLog> selectRecent(int limit)` — 最近 N 条（可选）
- **XML**：`backend/src/main/resources/com/database/mapper/AiSqlExecLogMapper.xml`
  - `BaseResultMap` 映射 snake_case → camelCase
  - `insert` 语句使用 `useGeneratedKeys="true" keyProperty="id"`

### 1.3 修改 `AiAgentService.executeSql()`

- 注入 `AiSqlExecLogMapper`
- 在 `executeSql()` 方法中用 try-finally 包装，记录：
  - 成功时：status=success, rowCount, latencyMs, sqlHash(SHA-256 of finalSql), sqlText=finalSql, params=JSON
  - 异常时：status 对应异常类型（explain_fail/security_reject/error），errorMsg=异常信息
- SHA-256 工具方法使用 `java.security.MessageDigest`
- 日志写入失败不应影响主流程（catch + log.warn）

### 1.4 测试

- 编写 Spring Boot 测试类 `AiSqlExecLogTest`
- 测试 insert + select 基本 CRUD
- 验证 executeSql 成功/失败时日志是否正确写入

---

## Phase 2：前端参数化表单

**目标**：根据 `paramsSpec` 动态渲染参数表单，用户可修改参数后执行 SQL，不再直接编辑原始 SQL 模板。

### 2.1 修改 aiStore

- 新增 `panelParamsSpec: ref<ParamSpec[]>([])` 存储参数规格
- 新增 `panelParams: ref<Record<string, unknown>>({})` 存储当前参数值
- 修改 `loadToPanel()`：
  - 保存原始 `sqlTemplate` 到 `panelSql`（不再调用 `renderDefaults`）
  - 保存 `paramsSpec` 到 `panelParamsSpec`
  - 初始化 `panelParams` 为默认值
- 新增 `renderedSql: computed`：根据 `panelSql` + `panelParams` 动态生成最终 SQL
- 修改 `executePanel()`：使用 `renderedSql.value` 替代 `panelSql.value`
- 修改 `clearPanel()`：清空 `panelParamsSpec` 和 `panelParams`

### 2.2 新建 `ParamForm.vue` 组件

- **文件**：`frontend/src/views/ai/components/ParamForm.vue`
- **Props**：`paramsSpec: ParamSpec[]`，`modelValue: Record<string, unknown>`
- **Emits**：`update:modelValue`
- **功能**：
  - 根据 `type` 渲染不同输入控件：
    - `date` → `<el-date-picker>`（value-format="YYYY-MM-DD"）
    - `number`/`int`/`float` → `<el-input-number>`
    - `string`（默认）→ `<el-input>`
  - 每个参数显示 `label`，必填标 `*`
  - 值变化时 emit `update:modelValue`

### 2.3 修改 `ExecPanel.vue`

- 在 SQL textarea 上方渲染 `ParamForm`（仅当 `panelParamsSpec` 非空时）
- SQL textarea 绑定 `renderedSql`（只读展示，或允许手动覆盖）
- 新增"显示原始模板"折叠开关，展开时可编辑模板

### 2.4 测试

- 前端 `vue-tsc --noEmit` 类型检查
- `vite build` 构建验证
- 手动验证：生成 SQL → 参数表单正确渲染 → 修改参数 → SQL 自动更新 → 执行

---

## Phase 3：SQL-RAG 记忆与反馈（Phase 2 业务逻辑）

**目标**：启用 `ai_sql_memory` + `ai_sql_feedback`，实现历史 SQL 检索复用和用户反馈。

### 3.1 新建实体

- `AiSqlMemory`（对应 `ai_sql_memory` 表，全字段）
- `AiSqlFeedback`（对应 `ai_sql_feedback` 表，全字段）

### 3.2 新建 Mapper

- `AiSqlMemoryMapper`：
  - `void insert(AiSqlMemory memory)`
  - `void updateByPrimaryKeySelective(AiSqlMemory memory)`
  - `List<AiSqlMemory> selectByNormalizedQuestion(String question, int limit)` — 关键词检索
  - `List<AiSqlMemory> selectByTablesUsed(String tables, int limit)` — 表重叠度检索
  - `void incrementUseCount(Long id)`
  - `void incrementSuccessCount(Long id)`
- `AiSqlFeedbackMapper`：
  - `void insertOrUpdate(AiSqlFeedback feedback)` — INSERT ON DUPLICATE KEY UPDATE
  - `int selectSumVote(Long memoryId)` — 获取某模板的净赞数

### 3.3 新建 `AiMemoryService`

- `findRelevantMemories(String question)` — 检索排序算法：
  1. 归一化问题（小写、去停用词）
  2. 按 `normalized_question` LIKE 检索候选
  3. 综合评分：questionSimilarity(0.4) + useCount(0.2) + successRate(0.2) + feedbackScore(0.2)
  4. 返回 Top N 候选
- `saveMemory(AiSqlMemory memory)` — 保存新模板
- `recordFeedback(Long memoryId, Long userId, int vote)` — 记录反馈
- `recordSuccess(Long memoryId)` — 执行成功后更新统计

### 3.4 新增 `POST /api/ai/feedback` 接口

- **Controller**：`AiAgentController` 新增方法
- **Request DTO**：`AiFeedbackRequest`（memoryId, vote）
- **Response**：`Result<Void>`
- 需要 `@RequireRole`

### 3.5 集成到现有流程

- `AiAgentService.generateSql()` 返回后，自动保存到 `ai_sql_memory`
- `AiAgentService.executeSql()` 成功后，更新 memory 的 success_count
- 未来可将检索到的历史模板作为上下文传给 Python AI 服务

### 3.6 测试

- 测试 memory CRUD
- 测试检索排序算法
- 测试 feedback INSERT ON DUPLICATE KEY UPDATE
- 测试 generateSql 后自动保存 memory
- 测试 executeSql 后自动更新 success_count

---

## 文件清单

### Phase 1（审计日志）
| 操作 | 文件 |
|------|------|
| 新建 | `backend/src/main/java/com/database/pojo/AiSqlExecLog.java` |
| 新建 | `backend/src/main/java/com/database/mapper/AiSqlExecLogMapper.java` |
| 新建 | `backend/src/main/resources/com/database/mapper/AiSqlExecLogMapper.xml` |
| 修改 | `backend/src/main/java/com/database/service/AiAgentService.java` |
| 新建 | `backend/src/test/java/com/database/service/AiSqlExecLogTest.java` |

### Phase 2（参数化表单）
| 操作 | 文件 |
|------|------|
| 新建 | `frontend/src/views/ai/components/ParamForm.vue` |
| 修改 | `frontend/src/views/ai/components/ExecPanel.vue` |
| 修改 | `frontend/src/stores/aiStore.ts` |

### Phase 3（记忆与反馈）
| 操作 | 文件 |
|------|------|
| 新建 | `backend/src/main/java/com/database/pojo/AiSqlMemory.java` |
| 新建 | `backend/src/main/java/com/database/pojo/AiSqlFeedback.java` |
| 新建 | `backend/src/main/java/com/database/mapper/AiSqlMemoryMapper.java` |
| 新建 | `backend/src/main/java/com/database/mapper/AiSqlFeedbackMapper.java` |
| 新建 | `backend/src/main/resources/com/database/mapper/AiSqlMemoryMapper.xml` |
| 新建 | `backend/src/main/resources/com/database/mapper/AiSqlFeedbackMapper.xml` |
| 新建 | `backend/src/main/java/com/database/service/AiMemoryService.java` |
| 新建 | `backend/src/main/java/com/database/dto/AiFeedbackRequest.java` |
| 修改 | `backend/src/main/java/com/database/controller/AiAgentController.java` |
| 修改 | `backend/src/main/java/com/database/service/AiAgentService.java` |
| 新建 | `backend/src/test/java/com/database/service/AiMemoryServiceTest.java` |
