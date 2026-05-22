<!-- frontend/src/views/LoginView.vue -->
<template>
  <div class="login-container">
    <div class="login-bg-decor decor-1"></div>
    <div class="login-bg-decor decor-2"></div>

    <div class="login-card">
      <div class="login-brand">
        <div class="brand-icon">企</div>
        <h2 class="brand-title">企业管理系统</h2>
        <p class="brand-subtitle">进销存 · 财务对账 · AI 智能查询</p>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="staffName">
          <el-input
            v-model="loginForm.staffName"
            placeholder="员工姓名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const authStore = useAuthStore();

const loginFormRef = ref();
const loading = ref(false);

const loginForm = reactive({
  staffName: '',
  password: '',
});

const rules = {
  staffName: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  try {
    const valid = await loginFormRef.value.validate();
    if (valid) {
      loading.value = true;
      await authStore.login(loginForm.staffName, loginForm.password);
      ElMessage.success('登录成功');
      router.push('/');
    }
  } catch (error) {
    ElMessage.error('登录失败，请检查员工姓名和密码');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  position: relative;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e7f0fb 0%, #f5f5f7 60%, #ffffff 100%);
  overflow: hidden;
}

/* 主色系装饰球 */
.login-bg-decor {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.45;
  pointer-events: none;
}

.decor-1 {
  width: 420px;
  height: 420px;
  background: var(--color-primary);
  top: -100px;
  right: -100px;
}

.decor-2 {
  width: 380px;
  height: 380px;
  background: #5eb3ff;
  bottom: -120px;
  left: -120px;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 400px;
  background: var(--color-bg);
  border-radius: var(--radius-lg);
  padding: var(--space-8) var(--space-8) var(--space-6);
  box-shadow: var(--shadow-lg);
}

.login-brand {
  text-align: center;
  margin-bottom: var(--space-8);
}

.brand-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto var(--space-4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  color: #ffffff;
  background: var(--color-primary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
}

.brand-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: var(--letter-spacing-tight);
  margin-bottom: var(--space-2);
}

.brand-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  letter-spacing: var(--letter-spacing-normal);
}

.login-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  padding: 6px 12px;
}

.login-button {
  width: 100%;
  border-radius: var(--radius-sm);
  font-weight: 500;
  letter-spacing: 4px;
}
</style>