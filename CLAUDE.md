# CLAUDE.md

## Project Overview

企业综合管理系统（进销存 + 财务对账 + AI 查询），数据库课程设计项目。

## Tech Stack

- **Frontend**: Vue 3 + TypeScript + Pinia + Element Plus + Vite, pnpm
- **Backend**: Spring Boot 3.0.5 (Java 17) + MyBatis + Druid + PageHelper + JWT + Redis
- **AI Agent**: FastAPI (Python) + LangGraph + OpenAI SDK
- **Database**: MySQL 8 (InnoDB), DDL in `db.sql`

## Project Structure

```
backend/          # Spring Boot, API prefix /api
frontend/         # Vue 3 SPA
aiagent/          # Python FastAPI, port 8001
db.sql            # 全量建表脚本
```

## Run Commands

```sh
# Backend
cd backend && mvn spring-boot:run        # localhost:8080

# Frontend
cd frontend && pnpm install && pnpm dev  # localhost:5173

# AI Agent
python -m uvicorn aiagent.main:app --port 8001 --reload
```

## API Conventions

- Response format: `{ code, message, data }` (code=200 为成功)
- Auth: JWT in Cookie `access_token`, header `Authorization: Bearer <token>`
- Pagination: `pageNum` (from 1) + `pageSize` query params

## Key Notes

- Frontend httpClient 判断成功用 `code === 200`，非 `success` 布尔值
- 所有业务表含审计字段: `created_at`, `updated_at`, `created_by_id`, `updated_by_id`, `is_deleted`
- AI 模块通过后端代理调用 Python AI Agent 服务
- 环境变量配置见 `backend/.env.example`

## Development Workflow

**按可测试的功能点划分任务**：每次修改都应能独立测试，测试通过后再提交 git。

划分原则：
1. 数据库变更（建表/改表） → 执行 SQL 验证 → 提交
2. 后端 POJO + Mapper → 单元测试验证 CRUD → 提交
3. 后端 Service + Controller → curl/Postman 测试 API → 提交
4. 前端 API/Store → 浏览器验证功能 → 提交
5. 前端组件 → UI 交互测试 → 提交

每个步骤完成后：
- 运行测试确认功能正常
- 提交 git（附带清晰的 commit message）
- 再进行下一步
