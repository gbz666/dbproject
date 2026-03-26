# Redis 登录认证系统集成说明

## 概述

本项目已集成基于 Redis 的用户登录认证系统，主要用于：
- 用户登录认证
- Token 生成与验证
- Token 过期管理（通过 Redis 自动过期）
- 单点登录支持（一个用户只能有一个有效 Token）

## 技术栈

- **Redis**: 用于存储 Token 和用户会话信息
- **JWT**: 用于生成和验证 Token
- **BCrypt**: 用于密码加密存储
- **Spring Interceptor**: 用于请求拦截和 Token 验证

## 数据库变更

### 1. 执行数据库迁移脚本

首先执行 `scripts/add_auth_fields_to_staffs.sql`，为 `staffs` 表添加认证相关字段：

```sql
-- 添加的字段：
- username: 登录用户名（唯一）
- password: 登录密码（BCrypt加密）
- status: 账户状态（0=禁用，1=启用）
- last_login_at: 最后登录时间
```

### 2. 初始化测试账号

执行 `scripts/init_test_user.sql` 创建测试账号：
- 用户名：`admin`
- 密码：`123456`
- 账户状态：启用

## 配置文件

### application.yaml

已添加以下配置：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: # 如果Redis设置了密码，在这里填写
      database: 0

jwt:
  secret: your-secret-key-should-be-at-least-256-bits-long-for-security-purposes-change-this-in-production
  expiration: 7200 # Token过期时间（秒），默认2小时
```

**重要**：生产环境请修改 `jwt.secret` 为安全的随机字符串！

## 核心组件说明

### 1. TokenService
- **位置**: `com.database.service.TokenService`
- **功能**:
  - 生成 JWT Token
  - 验证 Token 有效性
  - 刷新 Token
  - 删除 Token（登出）
  - Token 过期管理（通过 Redis TTL）

### 2. AuthService
- **位置**: `com.database.service.AuthService`
- **功能**:
  - 用户登录（验证用户名密码）
  - 用户登出
  - Token 刷新

### 3. AuthController
- **位置**: `com.database.controller.AuthController`
- **接口**:
  - `POST /api/auth/login` - 用户登录
  - `POST /api/auth/logout` - 用户登出
  - `POST /api/auth/refresh` - 刷新 Token

### 4. AuthInterceptor
- **位置**: `com.database.interceptor.AuthInterceptor`
- **功能**:
  - 拦截需要认证的请求
  - 验证 Token 有效性
  - 将用户信息存储到 request 属性中

## API 使用示例

### 1. 用户登录

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "admin",
    "staffName": "管理员",
    "expiration": 7200
  }
}
```

### 2. 访问需要认证的接口

在请求头中添加 Token：

```bash
GET /api/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. 用户登出

```bash
POST /api/auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4. 刷新 Token

```bash
POST /api/auth/refresh
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Redis 数据结构

### Token 存储

- **Key**: `token:{token}`
- **Value**: `userId`
- **TTL**: 与 JWT 过期时间一致（默认 7200 秒）

### 用户 Token 映射

- **Key**: `user_token:{userId}`
- **Value**: `token`
- **TTL**: 与 JWT 过期时间一致

**作用**: 实现单点登录，一个用户只能有一个有效 Token。新登录会自动删除旧 Token。

## 安全特性

1. **密码加密**: 使用 BCrypt 加密存储密码
2. **Token 过期**: Token 在 Redis 中设置过期时间，自动清理
3. **单点登录**: 同一用户只能有一个有效 Token
4. **Token 验证**: 双重验证（JWT 签名 + Redis 存在性检查）
5. **账户状态**: 支持禁用/启用账户

## 部署前检查清单

- [ ] 执行数据库迁移脚本 `add_auth_fields_to_staffs.sql`
- [ ] 修改 `application.yaml` 中的 `jwt.secret` 为安全的随机字符串
- [ ] 确保 Redis 服务已启动并可访问
- [ ] 测试登录接口是否正常工作
- [ ] 测试 Token 验证是否正常工作
- [ ] 测试 Token 过期是否正常工作

## 常见问题

### 1. Redis 连接失败

**错误**: `Unable to connect to Redis`

**解决**: 
- 检查 Redis 服务是否启动
- 检查 `application.yaml` 中的 Redis 配置是否正确
- 检查防火墙设置

### 2. Token 验证失败

**错误**: `Token无效或已过期`

**解决**:
- 检查 Token 是否在请求头中正确传递
- 检查 Token 是否已过期（默认 2 小时）
- 检查 Redis 中是否存在对应的 Token

### 3. 密码验证失败

**错误**: `用户名或密码错误`

**解决**:
- 确认数据库中密码字段已正确存储（BCrypt 加密）
- 确认密码加密方式与验证方式一致

## 后续扩展建议

1. **角色权限**: 可以基于现有的 `roles` 和 `staff_roles` 表实现基于角色的访问控制（RBAC）
2. **刷新 Token**: 实现 Refresh Token 机制，延长用户会话时间
3. **登录日志**: 记录登录历史，便于审计
4. **密码策略**: 添加密码复杂度要求、密码过期策略等
5. **多设备登录**: 如果需要支持多设备同时登录，可以修改 Token 存储策略
