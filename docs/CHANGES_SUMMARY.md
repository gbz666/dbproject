# Redis 登录认证系统集成 - 文件修改清单

## 修改的文件列表

### 1. 依赖和配置相关

#### `backend/pom.xml`
- **修改内容**: 添加 Redis、JWT、BCrypt 相关依赖
- **新增依赖**:
  - `spring-boot-starter-data-redis`
  - `commons-pool2`
  - `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
  - `spring-security-crypto`

#### `backend/src/main/resources/application.yaml`
- **修改内容**: 添加 Redis 连接配置和 JWT 配置
- **新增配置**:
  - Redis 连接信息（host, port, password, database）
  - Redis 连接池配置
  - JWT secret 和 expiration 配置

### 2. 数据库相关

#### `backend/src/main/java/com/database/pojo/Staffs.java`
- **修改内容**: 添加认证相关字段
- **新增字段**:
  - `username` (登录用户名)
  - `password` (加密密码)
  - `status` (账户状态)
  - `lastLoginAt` (最后登录时间)

#### `backend/src/main/java/com/database/mapper/StaffsMapper.java`
- **修改内容**: 添加认证相关查询方法
- **新增方法**:
  - `selectByUsername()` - 根据用户名查询员工
  - `updateLastLoginTime()` - 更新最后登录时间

#### `backend/src/main/resources/com/database/mapper/StaffsMapper.xml`
- **修改内容**: 添加认证字段映射和 SQL 查询
- **修改内容**:
  - 更新 `BaseResultMap`，添加认证字段映射
  - 更新 `Base_Column_List`，包含认证字段
  - 新增 `selectByUsername` SQL 查询
  - 新增 `updateLastLoginTime` SQL 更新

### 3. 服务层

#### `backend/src/main/java/com/database/service/StaffService.java`
- **修改内容**: 添加认证相关业务方法
- **新增方法**:
  - `getStaffByUsername()` - 根据用户名获取员工
  - `updateLastLoginTime()` - 更新最后登录时间

#### `backend/src/main/java/com/database/service/TokenService.java` (新建)
- **文件说明**: Token 管理服务类
- **主要功能**:
  - 生成 JWT Token
  - 验证 Token 有效性
  - 刷新 Token
  - 删除 Token
  - 检查 Token 是否即将过期
  - 使用 Redis 管理 Token 过期

#### `backend/src/main/java/com/database/service/AuthService.java` (新建)
- **文件说明**: 认证服务类
- **主要功能**:
  - 用户登录（验证用户名密码）
  - 用户登出
  - Token 刷新

### 4. 控制器层

#### `backend/src/main/java/com/database/controller/AuthController.java` (新建)
- **文件说明**: 认证控制器
- **接口**:
  - `POST /api/auth/login` - 用户登录
  - `POST /api/auth/logout` - 用户登出
  - `POST /api/auth/refresh` - 刷新 Token（可选，已实现自动刷新）

### 5. DTO 类

#### `backend/src/main/java/com/database/dto/LoginRequest.java` (新建)
- **文件说明**: 登录请求 DTO
- **字段**: username, password

#### `backend/src/main/java/com/database/dto/LoginResponse.java` (新建)
- **文件说明**: 登录响应 DTO
- **字段**: token, userId, username, staffName, expiration

### 6. 配置类

#### `backend/src/main/java/com/database/config/RedisConfig.java` (新建)
- **文件说明**: Redis 配置类
- **功能**: 配置 RedisTemplate，使用 GenericJackson2JsonRedisSerializer 序列化

#### `backend/src/main/java/com/database/config/WebConfig.java` (新建)
- **文件说明**: Web 配置类
- **功能**: 注册认证拦截器

### 7. 拦截器

#### `backend/src/main/java/com/database/interceptor/AuthInterceptor.java` (新建)
- **文件说明**: 认证拦截器
- **功能**:
  - 拦截需要认证的请求
  - 验证 Token 有效性
  - 自动刷新即将过期的 Token（剩余时间少于30分钟）
  - 将用户信息存储到 request 属性中

### 8. 工具类

#### `backend/src/main/java/com/database/vo/Result.java`
- **修改内容**: 添加支持泛型的失败方法和带消息的成功方法
- **新增方法**:
  - `success(String message)` - 成功，带自定义消息，无返回数据
  - `fail(int code, String message, T data)` - 失败，支持泛型

### 9. 数据库脚本

#### `scripts/add_auth_fields_to_staffs.sql` (新建)
- **文件说明**: 数据库迁移脚本
- **功能**: 为 staffs 表添加认证相关字段

#### `scripts/init_test_user.sql` (新建)
- **文件说明**: 初始化测试用户脚本
- **功能**: 创建测试账号（用户名：admin，密码：123456）

### 10. 文档

#### `README_REDIS_AUTH.md` (新建)
- **文件说明**: Redis 认证系统使用文档
- **内容**: 包含使用说明、API 文档、配置说明等

#### `CHANGES_SUMMARY.md` (新建)
- **文件说明**: 本次修改的文件清单（本文件）

---

## 总结

- **新建文件**: 11 个
- **修改文件**: 6 个
- **总计**: 17 个文件

## Token 刷新机制说明

### 当前实现（已优化）

1. **自动刷新**（推荐）:
   - 拦截器在每次请求时检查 Token 是否即将过期（剩余时间少于30分钟）
   - 如果即将过期，自动刷新 Token
   - 新 Token 通过响应头 `X-New-Token` 返回给前端
   - 前端需要监听响应头，更新本地存储的 Token

2. **手动刷新**（备用）:
   - 前端可以主动调用 `POST /api/auth/refresh` 接口
   - 适用于前端检测到 Token 即将过期时主动刷新

### 前端集成建议

```javascript
// 在 axios 响应拦截器中处理自动刷新的 Token
axiosInstance.interceptors.response.use(
  (response) => {
    // 检查是否有新 Token
    const newToken = response.headers['x-new-token'];
    if (newToken) {
      localStorage.setItem('access_token', newToken);
      console.log('Token已自动刷新');
    }
    return response;
  },
  (error) => {
    // 错误处理...
  }
);
```
