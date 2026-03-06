<!-- frontend/src/views/LoginView.vue -->
<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>小微企业账目库存管理系统</h2>
      </template>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
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
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
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
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.login-card h2 {
  text-align: center;
  color: #333;
  margin: 0;
}
</style>