# "记住我"功能实现说明

## 功能概述

"记住我"功能允许用户选择是否在关闭浏览器后保持登录状态：
- **不勾选"记住我"**：Token 2小时过期（默认）
- **勾选"记住我"**：Token 7天过期

---

## 实现原理

### 后端实现

1. **登录请求增加字段**：
   ```java
   {
     "username": "admin",
     "password": "123456",
     "rememberMe": true  // 新增字段
   }
   ```

2. **Token 生成逻辑**：
   - `rememberMe = false`：使用默认过期时间（2小时）
   - `rememberMe = true`：使用长过期时间（7天）

3. **Token 存储**：
   - Redis 中存储 Token，TTL 根据 `rememberMe` 设置
   - JWT Token 的过期时间也相应调整

### 前端配合

**需要前端配合**，但实现很简单：

#### 1. 登录表单添加"记住我"复选框

```vue
<template>
  <el-form>
    <el-form-item label="用户名">
      <el-input v-model="loginForm.username" />
    </el-form-item>
    <el-form-item label="密码">
      <el-input v-model="loginForm.password" type="password" />
    </el-form-item>
    <!-- 记住我复选框 -->
    <el-form-item>
      <el-checkbox v-model="loginForm.rememberMe">记住我（7天免登录）</el-checkbox>
    </el-form-item>
    <el-button @click="handleLogin">登录</el-button>
  </el-form>
</template>

<script setup>
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false  // 默认不记住
});

const handleLogin = async () => {
  const response = await loginApi.login(loginForm);
  // 登录成功后的处理...
};
</script>
```

#### 2. Token 存储方式（可选优化）

根据是否"记住我"，选择不同的存储方式：

```javascript
// 登录成功后
if (loginForm.rememberMe) {
  // 记住我：使用 localStorage（持久化存储，关闭浏览器后仍存在）
  localStorage.setItem('access_token', response.data.token);
} else {
  // 不记住我：使用 sessionStorage（关闭浏览器后清除）
  sessionStorage.setItem('access_token', response.data.token);
}
```

**注意**：即使使用 `localStorage`，Token 仍然会在 7 天后过期（由后端控制）。

---

## 完整实现方案

### 方案一：仅后端控制（推荐，已实现）

**优点**：
- ✅ 前端无需特殊处理
- ✅ 安全性更高（过期时间由后端控制）
- ✅ 实现简单

**实现**：
1. 前端在登录时传递 `rememberMe` 字段
2. 后端根据 `rememberMe` 生成不同过期时间的 Token
3. 前端正常存储 Token（localStorage 或 sessionStorage 都可以）

**代码示例**：

```java
// AuthService.java
public LoginResponse login(LoginRequest request) {
    // ... 验证用户名密码 ...
    
    // 根据 rememberMe 决定过期时间
    Long tokenExpiration = request.getRememberMe() 
        ? rememberMeExpiration  // 7天
        : expiration;            // 2小时
    
    // 生成 Token（使用自定义过期时间）
    String token = tokenService.generateToken(
        staff.getId(), 
        staff.getUsername(), 
        tokenExpiration
    );
    
    return new LoginResponse(token, ...);
}
```

### 方案二：前端 + 后端双重控制（更灵活）

**优点**：
- ✅ 用户体验更好
- ✅ 可以区分"记住我"和"临时登录"的存储方式

**实现**：
1. 前端根据 `rememberMe` 选择存储方式
2. 后端根据 `rememberMe` 设置 Token 过期时间

---

## 前端代码示例

### Vue 3 + Element Plus 示例

```vue
<template>
  <div class="login-container">
    <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
      <el-form-item prop="username">
        <el-input 
          v-model="loginForm.username" 
          placeholder="用户名"
          prefix-icon="User"
        />
      </el-form-item>
      
      <el-form-item prop="password">
        <el-input 
          v-model="loginForm.password" 
          type="password"
          placeholder="密码"
          prefix-icon="Lock"
          @keyup.enter="handleLogin"
        />
      </el-form-item>
      
      <el-form-item>
        <el-checkbox v-model="loginForm.rememberMe">
          记住我（7天免登录）
        </el-checkbox>
      </el-form-item>
      
      <el-form-item>
        <el-button 
          type="primary" 
          :loading="loading"
          @click="handleLogin"
          style="width: 100%"
        >
          登录
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { loginApi } from '@/api/authApi';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loginFormRef = ref();
const loading = ref(false);

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    loading.value = true;
    try {
      const response = await loginApi.login(loginForm);
      
      // 根据 rememberMe 选择存储方式
      if (loginForm.rememberMe) {
        localStorage.setItem('access_token', response.data.token);
        console.log('已记住登录状态（7天有效）');
      } else {
        sessionStorage.setItem('access_token', response.data.token);
        console.log('临时登录（关闭浏览器后失效）');
      }
      
      ElMessage.success('登录成功');
      router.push('/');
    } catch (error) {
      ElMessage.error(error.message || '登录失败');
    } finally {
      loading.value = false;
    }
  });
};
</script>
```

---

## 配置说明

### application.yaml

```yaml
jwt:
  secret: your-secret-key-should-be-at-least-256-bits-long-for-security-purposes-change-this-in-production
  expiration: 7200          # 默认过期时间：2小时（不记住我）
  remember-me-expiration: 604800  # 记住我过期时间：7天（604800秒）
```

---

## 安全性说明

### Token 过期时间对比

| 场景 | Token 过期时间 | 存储方式建议 | 安全性 |
|------|---------------|------------|--------|
| **不记住我** | 2小时 | sessionStorage | ⭐⭐⭐⭐⭐ 最高 |
| **记住我** | 7天 | localStorage | ⭐⭐⭐ 中等 |

### 安全建议

1. **生产环境建议**：
   - "记住我"功能建议限制为 7-30 天
   - 敏感操作（如支付、修改密码）需要重新验证密码
   - 定期提醒用户检查登录设备

2. **增强安全性（可选）**：
   - 记录登录 IP 和设备信息
   - 异常登录时发送通知
   - 支持"退出所有设备"功能

---

## 总结

### 前端需要做什么？

1. ✅ **添加"记住我"复选框**（必须）
   - 在登录表单中添加复选框
   - 绑定到 `loginForm.rememberMe`

2. ✅ **传递 rememberMe 字段**（必须）
   - 登录时在请求体中包含 `rememberMe` 字段

3. ⚠️ **选择存储方式**（可选，但推荐）
   - `rememberMe = true`：使用 `localStorage`
   - `rememberMe = false`：使用 `sessionStorage`

### 后端已经做了什么？

- ✅ 支持 `rememberMe` 字段接收
- ✅ 根据 `rememberMe` 生成不同过期时间的 Token
- ✅ Token 过期时间由后端完全控制（更安全）

---

## 测试建议

1. **测试"不记住我"**：
   - 登录时不勾选"记住我"
   - 等待 2 小时后，Token 应该失效

2. **测试"记住我"**：
   - 登录时勾选"记住我"
   - 关闭浏览器后重新打开，应该仍然登录
   - 等待 7 天后，Token 应该失效

3. **测试切换**：
   - 先"记住我"登录，然后登出
   - 再"不记住我"登录，验证过期时间正确
