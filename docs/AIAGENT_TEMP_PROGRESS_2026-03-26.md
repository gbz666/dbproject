# AI Agent 临时进度与问题体检（2026-03-26）

## 1. 当前进度结论

基于 `AIAGENT_ARCHITECTURE.md` 对照代码现状：

- Step 0（环境准备）：已完成
- Step 1（Python `/generate-sql`）：已完成并可运行
- Step 2（Java `/api/ai/generate-sql` 转发）：已完成
- Step 3（Java `/api/ai/execute-sql` 安全执行）：已完成（含基础安全校验）
- Step 4（图表展示链路）：部分完成（Python `/generate-chart` 已有；Java 缺 `GET /api/ai/chart/{chartId}`）
- Step 5（前端 AI 查询页）：未开始
- Step 6~7（权限加固 / 记忆反馈）：未开始

## 2. 本轮检查发现的问题

### P0（必须先补）

1) 缺少图表读取接口  
- 现状：`AiAgentService` 会返回 `chartUrl=/api/ai/chart/{chartId}`，但 Java 端没有 `AiChartController` 实现该 GET 接口。  
- 影响：前端即使拿到 `chartUrl` 也无法展示图片。  

### P1（高优先）

2) SQL 生成效果弱，主要在 prompt 与模型组合  
- 现状：`aiagent/agent/sql_agent.py` 的 system prompt 较简短，缺少 few-shot 示例。  
- 现状：模型为本地 `qwen2.5-coder:7b`，对 NL2SQL 结构化输出稳定性一般。  
- 影响：SQL 可用性与可解释性波动较大。  

3) Schema 上下文过重  
- 现状：`schema_loader.py` 会抽取大量表和字段。  
- 影响：小模型上下文压力大，容易牺牲输出质量。  

4) execute-sql 里 question 为空  
- 现状：`AiAgentController.executeSql()` 调用 `aiAgentService.executeSql(request, null)`。  
- 影响：图表标题退化为默认值“查询结果”，丢失上下文。  

### P2（中优先）

5) SQL 渲染目前是字符串替换  
- 现状：`SqlSecurityService.renderSql()` 用 `{param}` 直接替换。  
- 风险：虽然做了引号转义和关键词黑名单，但仍不如参数化执行稳健。  

6) 前端尚未接入 AI 页面  
- 现状：`frontend/src/router/index.ts` 没有 `/ai-query`，`frontend/src/api/` 没有 `aiApi.ts`。  
- 影响：只能靠 curl 测，无法形成业务闭环演示。  

## 3. 临时执行方案（可分 commit，步骤详细）

> 原则：每完成一个阶段就可暂停并提交一次 commit，降低回滚风险。

### 阶段 A：先提升 SQL 生成质量（只改 Python）

目标：不动 Java/前端，先让 `/generate-sql` 输出更稳定。

步骤：

1. 改 `aiagent/agent/sql_agent.py`
   - 在 `build_system_prompt()` 增加 2~3 条 few-shot（使用真实表名：`sales_orders`、`sales_order_items`、`products`）。
   - 明确 JSON 字段必须包含：`sqlTemplate`, `paramsSpec`, `reason`, `chartHint`, `confidence`, `warnings`。
   - 明确只允许输出 JSON 对象，不允许 markdown 包裹。

2. 改 `aiagent/core/schema_loader.py`
   - 增加“核心表优先”策略（优先销售、采购、商品、库存核心表）。
   - 过滤审计字段（如 `created_at`, `updated_at`, `deleted_at`, `created_by_id`, `updated_by_id`）以减 token。
   - 加 schema 长度保护（超限时截断并告警）。

3. 验证（本地）
   - 启动：`uvicorn main:app --host 0.0.0.0 --port 8001 --reload`
   - 用 3 组问题压测 `/generate-sql`（趋势、排行、占比）
   - 检查：JSON 结构完整率、sqlTemplate 可执行率、chartHint 合理性

建议 commit 名称：`improve aiagent nl2sql prompt and schema context`

---

### 阶段 B：补齐图表访问接口（Java 小改动）

目标：打通 chartUrl 展示最后一公里。

步骤：

1. 新建 `backend/src/main/java/com/database/controller/AiChartController.java`
   - `GET /api/ai/chart/{chartId}`
   - 从 `aiagent.chart-dir` 读取 `{chartId}.png`
   - 返回 `ResponseEntity<byte[]>` + `Content-Type: image/png`
   - 文件不存在返回 404 的 `Result.fail(...)`（按项目统一返回规范）

2. 自测
   - 先调用 `/api/ai/execute-sql` 生成 `chartUrl`
   - 浏览器访问 `http://localhost:8080/api/ai/chart/{chartId}`
   - 确认可见图表

建议 commit 名称：`add ai chart controller for png serving`

---

### 阶段 C：接前端页面（形成演示闭环）

目标：页面可输入自然语言并展示 SQL/结果/图。

步骤：

1. 新建 `frontend/src/api/aiApi.ts`
   - `generateSql(question)`
   - `executeSql(payload)`
   - 复用现有 `httpClient` 规范（`auth: true`）

2. 新建 `frontend/src/views/AiQueryView.vue`
   - 输入区：question
   - 结果区1：SQL 模板展示（只读）
   - 结果区2：参数表单（由 `paramsSpec` 动态渲染）
   - 结果区3：查询表格 + 图表 `<img :src="chartUrl">`

3. 修改 `frontend/src/router/index.ts`
   - 增加 `/ai-query` 路由并挂载到现有布局 children

4. 联调
   - Python、Java、前端同时启动
   - 按“生成 SQL -> 执行 SQL -> 展示图表”全流程走通

建议 commit 名称：`add ai query frontend flow`

## 4. 本轮建议的停止点

如果你希望现在就“可停、可提交”：

- 推荐停在 **阶段 A 完成后**，先提交一次（最小风险且立即改善“效果差”问题）。
- 然后再做 B、C 两次小提交，方便回滚与对比效果。

## 5. 额外注意事项

- `backend/src/main/resources/application.yaml` 当前包含真实数据库连接信息，建议后续做敏感信息治理（环境变量或本地覆盖配置）。
- 若继续使用 7B 本地模型，建议优先把 prompt+schema 做到“短而硬约束”；若机器资源允许，升级到 14B 通常提升明显。
