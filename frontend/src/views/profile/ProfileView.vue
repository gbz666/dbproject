<template>
  <div class="profile-page">
    <el-card class="profile-card">
      <template #header>
        <span>个人中心</span>
      </template>

      <!-- 当前用户信息 -->
      <div class="user-info-block">
        <div class="info-row">
          <span class="label">姓名</span>
          <span class="value">{{ authStore.user?.staffName ?? '-' }}</span>
        </div>
        <div class="info-row" v-if="authStore.user?.userId">
          <span class="label">用户 ID</span>
          <span class="value">{{ authStore.user.userId }}</span>
        </div>
      </div>

      <el-divider />

      <!-- 修改密码 -->
      <div class="section-title">修改密码</div>
      <el-form
        ref="formRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
        class="password-form"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="passwordForm.currentPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="6～20 位"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="再次输入新密码"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleChangePassword">
            确认修改
          </el-button>
          <el-button @click="resetPasswordForm">重置</el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <!-- 退出登录 -->
      <div class="section-title">账号操作</div>
      <div class="logout-row">
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const authStore = useAuthStore();

onMounted(() => {
  if (!authStore.isLoggedIn) {
    router.replace('/auth/login');
  }
});

const formRef = ref<FormInstance>();
const submitLoading = ref(false);

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const validateConfirm = (_rule: unknown, value: string, callback: (e?: Error) => void) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'));
  } else {
    callback();
  }
};

const passwordRules: FormRules = {
  currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度为 6～20 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
};

const handleChangePassword = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    submitLoading.value = true;
    try {
      await authStore.changePassword(passwordForm.currentPassword, passwordForm.newPassword);
      ElMessage.success('密码修改成功');
      resetPasswordForm();
    } catch (err: any) {
      ElMessage.error(err?.message || '修改失败');
    } finally {
      submitLoading.value = false;
    }
  });
};

const resetPasswordForm = () => {
  passwordForm.currentPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  formRef.value?.resetFields();
};

const handleLogout = async () => {
  try {
    await authStore.logout();
    ElMessage.success('已退出登录');
    router.replace('/auth/login');
  } catch {
    router.replace('/auth/login');
  }
};
</script>

<style scoped>
.profile-page {
  max-width: 520px;
  margin: 0 auto;
}

.profile-card {
  margin-top: 20px;
}

.user-info-block {
  margin-bottom: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.info-row .label {
  width: 80px;
  color: #606266;
}

.info-row .value {
  flex: 1;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.password-form {
  max-width: 400px;
}

.logout-row {
  margin-top: 8px;
}
</style>
