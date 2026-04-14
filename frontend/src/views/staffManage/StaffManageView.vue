<template>
  <div class="app-container" style="padding: 20px">
    <!-- 搜索栏 -->
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" @submit.prevent="fetchList">
        <el-form-item label="关键词">
          <el-input
            v-model="keyword"
            placeholder="姓名 / 编号 / 邮箱"
            clearable
            @clear="handleReset"
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="openCreateDialog">
            <el-icon><Plus /></el-icon> 新增员工
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 员工表格 -->
    <el-table v-loading="loading" :data="staffList" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="staffCode" label="编号" width="110" align="center">
        <template #default="{ row }">{{ row.staffCode || '-' }}</template>
      </el-table-column>
      <el-table-column prop="staffName" label="姓名" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.email || '-' }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" width="130">
        <template #default="{ row }">{{ row.phone || '-' }}</template>
      </el-table-column>
      <el-table-column prop="title" label="职位" width="110">
        <template #default="{ row }">{{ row.title || '-' }}</template>
      </el-table-column>
      <el-table-column label="角色" width="180">
        <template #default="{ row }">
          <el-tag
            v-for="r in row.roleNames"
            :key="r"
            :type="roleTagType(r)"
            size="small"
            style="margin-right: 4px"
          >
            {{ roleLabel(r) }}
          </el-tag>
          <span v-if="!row.roleNames?.length" style="color: #999">无角色</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后登录" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.lastLoginAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="warning" @click="openResetPwdDialog(row)">重置密码</el-button>
          <el-popconfirm
            :title="`确定${row.status === 1 ? '禁用' : '启用'}该员工吗？`"
            @confirm="handleToggleStatus(row)"
          >
            <template #reference>
              <el-button link :type="row.status === 1 ? 'danger' : 'success'">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="fetchList"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑员工' : '新增员工'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="员工姓名" prop="staffName">
          <el-input v-model="form.staffName" placeholder="请输入姓名" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工编号">
              <el-input v-model="form.staffCode" placeholder="可选" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位">
              <el-input v-model="form.title" placeholder="如：销售经理" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="example@co.com" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话">
              <el-input v-model="form.phone" placeholder="手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少6位" />
        </el-form-item>
        <el-form-item label="分配角色">
          <el-checkbox-group v-model="form.roleIds">
            <el-checkbox
              v-for="role in allRoles"
              :key="role.id"
              :value="role.id"
            >
              {{ roleLabel(role.roleName) }}
              <span v-if="role.description" style="color: #999; font-size: 12px; margin-left: 4px">
                ({{ role.description }})
              </span>
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认保存</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPwdVisible" title="重置密码" width="420px" destroy-on-close>
      <p style="margin-bottom: 12px">
        即将重置 <strong>{{ resetTarget?.staffName }}</strong> 的登录密码
      </p>
      <el-form ref="resetPwdFormRef" :model="resetPwdForm" :rules="resetPwdRules" label-width="80px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPwdForm.newPassword" type="password" show-password placeholder="至少6位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="warning" :loading="resetPwdLoading" @click="handleResetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import {
  staffManageApi,
  type StaffListItem,
  type RoleItem,
} from "@/api/staffManageApi";

// ---------- 角色映射 ----------
const ROLE_LABELS: Record<string, string> = {
  admin: "管理员",
  manager: "经理",
  analyst: "分析师",
  sales: "销售",
};
const ROLE_TAG_TYPE: Record<string, string> = {
  admin: "danger",
  manager: "warning",
  analyst: "",
  sales: "success",
};
const roleLabel = (name: string) => ROLE_LABELS[name] ?? name;
const roleTagType = (name: string) => (ROLE_TAG_TYPE[name] ?? "info") as any;

// ---------- 列表 ----------
const loading = ref(false);
const staffList = ref<StaffListItem[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);
const keyword = ref("");
const allRoles = ref<RoleItem[]>([]);

const fetchList = async () => {
  loading.value = true;
  try {
    const data = await staffManageApi.list(pageNum.value, pageSize.value, keyword.value || undefined);
    staffList.value = data.list;
    total.value = data.total;
  } catch (err: any) {
    ElMessage.error(err?.message || "获取员工列表失败");
  } finally {
    loading.value = false;
  }
};

const fetchRoles = async () => {
  try {
    allRoles.value = await staffManageApi.getRoles();
  } catch (err: any) {
    ElMessage.error(err?.message || "获取角色列表失败");
  }
};

const handleReset = () => {
  keyword.value = "";
  pageNum.value = 1;
  fetchList();
};

// ---------- 新增 / 编辑 ----------
const formDialogVisible = ref(false);
const isEdit = ref(false);
const editId = ref<number>(0);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();

const emptyForm = () => ({
  staffName: "",
  staffCode: "",
  email: "",
  phone: "",
  title: "",
  password: "",
  roleIds: [] as number[],
});
const form = reactive(emptyForm());

const formRules = reactive<FormRules>({
  staffName: [{ required: true, message: "姓名不能为空", trigger: "blur" }],
  password: [{ required: true, message: "密码不能为空", trigger: "blur" },
             { min: 6, message: "密码至少6位", trigger: "blur" }],
});

const openCreateDialog = () => {
  isEdit.value = false;
  editId.value = 0;
  Object.assign(form, emptyForm());
  formDialogVisible.value = true;
};

const openEditDialog = (row: StaffListItem) => {
  isEdit.value = true;
  editId.value = row.id;
  Object.assign(form, {
    staffName: row.staffName ?? "",
    staffCode: row.staffCode ?? "",
    email: row.email ?? "",
    phone: row.phone ?? "",
    title: row.title ?? "",
    password: "",
    roleIds: [...(row.roleIds ?? [])],
  });
  formDialogVisible.value = true;
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    submitLoading.value = true;
    try {
      if (isEdit.value) {
        const { password, ...payload } = form;
        await staffManageApi.update(editId.value, payload);
        ElMessage.success("员工信息更新成功");
      } else {
        await staffManageApi.create({ ...form });
        ElMessage.success("员工创建成功");
      }
      formDialogVisible.value = false;
      fetchList();
    } catch (err: any) {
      ElMessage.error(err?.message || "操作失败");
    } finally {
      submitLoading.value = false;
    }
  });
};

// ---------- 重置密码 ----------
const resetPwdVisible = ref(false);
const resetPwdLoading = ref(false);
const resetTarget = ref<StaffListItem | null>(null);
const resetPwdFormRef = ref<FormInstance>();
const resetPwdForm = reactive({ newPassword: "" });
const resetPwdRules = reactive<FormRules>({
  newPassword: [
    { required: true, message: "新密码不能为空", trigger: "blur" },
    { min: 6, message: "密码至少6位", trigger: "blur" },
  ],
});

const openResetPwdDialog = (row: StaffListItem) => {
  resetTarget.value = row;
  resetPwdForm.newPassword = "";
  resetPwdVisible.value = true;
};

const handleResetPassword = async () => {
  if (!resetPwdFormRef.value) return;
  await resetPwdFormRef.value.validate(async (valid) => {
    if (!valid) return;
    resetPwdLoading.value = true;
    try {
      await staffManageApi.resetPassword(resetTarget.value!.id, resetPwdForm.newPassword);
      ElMessage.success("密码重置成功");
      resetPwdVisible.value = false;
    } catch (err: any) {
      ElMessage.error(err?.message || "重置失败");
    } finally {
      resetPwdLoading.value = false;
    }
  });
};

// ---------- 启用 / 禁用 ----------
const handleToggleStatus = async (row: StaffListItem) => {
  try {
    await staffManageApi.toggleStatus(row.id);
    ElMessage.success("状态切换成功");
    fetchList();
  } catch (err: any) {
    ElMessage.error(err?.message || "操作失败");
  }
};

// ---------- 工具 ----------
const formatTime = (timeStr: string | null) => {
  if (!timeStr) return "-";
  return timeStr.replace("T", " ").substring(0, 16);
};

onMounted(() => {
  fetchList();
  fetchRoles();
});
</script>
