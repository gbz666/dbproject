<template>
  <div class="app-container" style="padding: 20px">
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="customerStore.queryParams">
        <el-form-item label="客户编号">
          <el-input v-model="customerStore.queryParams.customerCode" placeholder="请输入编号" clearable @clear="handleResetQuery" />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input v-model="customerStore.queryParams.customerName" placeholder="请输入名称" clearable @clear="handleResetQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="customerStore.fetchList">查询</el-button>
          <el-button @click="handleResetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleOpenDialog()">新增客户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="customerStore.loading" :data="customerStore.customers" border stripe style="width: 100%">
      <el-table-column prop="customerCode" label="客户编号" width="120" align="center" />
      <el-table-column prop="customerName" label="客户全称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="salesPersonName" label="销售负责人" width="110" align="center" />
      <el-table-column prop="followUpPersonName" label="跟单负责人" width="110" align="center" />
      <el-table-column prop="ownerName" label="业绩" width="110" align="center" />
      <el-table-column prop="updatedAt" label="最后更新" width="170" align="center">
        <template #default="{ row }">
          {{ formatTime(row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleOpenDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除该客户吗？" @confirm="handleConfirmDelete(row)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="customerStore.queryParams.pageNum"
        v-model:page-size="customerStore.queryParams.pageSize"
        :total="customerStore.total"
        layout="total, prev, pager, next, jumper"
        @current-change="customerStore.fetchList"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑客户信息' : '新增客户 (已开启草稿自动保存)'"
      width="800px"
      destroy-on-close
    >
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="110px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入完整的客户公司名称" />
        </el-form-item>

        <el-row :gutter="30">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="手机或座机" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="form.email" placeholder="example@company.com" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="30">
          <el-col :span="12">
            <el-form-item label="销售负责人" prop="salesPersonName">
              <el-input v-model="form.salesPersonName" placeholder="销售姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="跟单负责人" prop="followUpPersonName">
              <el-input v-model="form.followUpPersonName" placeholder="跟单员姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="30">
          <el-col :span="12">
            <el-form-item label="业绩归属" prop="ownerName">
              <el-input v-model="form.ownerName" placeholder="业绩人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="账期天数" prop="paymentTermsDays">
              <el-input-number v-model="form.paymentTermsDays" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>

        <el-form-item label="账期备注" prop="paymentTermsNotes">
          <el-input v-model="form.paymentTermsNotes" type="textarea" :rows="2" placeholder="如：对账及结算约定" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div style="display: flex; justify-content: space-between">
          <el-button v-if="!isEdit" type="info" plain @click="clearDraft">清空草稿</el-button>
          <div style="flex: 1; text-align: right">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="submitLoading" @click="handleSave">确认保存</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue';
import { useCustomerStore } from '@/stores/customerStore';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import type { CustomerCreateRequest } from '@/types/dto'; 

const customerStore = useCustomerStore();
const CURRENT_STAFF_ID = 1;
const STORAGE_KEY = 'customer_form_draft'; // 缓存键名

const dialogVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const editCode = ref('');

// 1. 定义空的初始结构
const emptyForm: CustomerCreateRequest = {
  customerName: '',
  address: '',
  phone: '',
  email: '',
  salesPersonName: '',
  followUpPersonName: '',
  ownerName: '',
  paymentTermsDays: 0,
  paymentTermsNotes: ''
};

// 2. 初始化表单逻辑：优先读取本地缓存
const getStoredForm = () => {
  const saved = localStorage.getItem(STORAGE_KEY);
  return saved ? JSON.parse(saved) : { ...emptyForm };
};

const form = reactive<CustomerCreateRequest>(getStoredForm());

// 3. 监听表单变化：实现“刷新不丢失”
watch(
  form,
  (newVal) => {
    // 仅在新增模式下保存草稿，避免覆盖编辑数据
    if (!isEdit.value) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(newVal));
    }
  },
  { deep: true }
);

const formRules = reactive<FormRules>({
  customerName: [{ required: true, message: '客户名称是必填项', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
});

onMounted(() => {
  customerStore.fetchList();
});

const handleResetQuery = () => {
  customerStore.queryParams.customerName = '';
  customerStore.queryParams.customerCode = '';
  customerStore.queryParams.pageNum = 1;
  customerStore.fetchList();
};

const handleOpenDialog = (row?: any) => {
  isEdit.value = !!row;
  if (row) {
    editCode.value = row.customerCode;
    // 编辑模式：直接从行数据填充
    Object.keys(emptyForm).forEach(key => {
      (form as any)[key] = row[key] ?? (emptyForm as any)[key];
    });
  } else {
    // 新增模式：保持当前 reactive 中的值（即暂存的值）
    editCode.value = '';
  }
  dialogVisible.value = true;
};

const handleSave = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true;
      try {
        if (isEdit.value) {
          await customerStore.editCustomer(editCode.value, { ...form }, CURRENT_STAFF_ID);
          ElMessage.success('修改成功');
        } else {
          await customerStore.addCustomer({ ...form }, CURRENT_STAFF_ID);
          ElMessage.success('新增成功');
          // 只有保存成功才清除缓存
          clearDraft(false); 
        }
        dialogVisible.value = false;
        customerStore.fetchList();
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败');
      } finally {
        submitLoading.value = false;
      }
    }
  });
};

// 清空草稿方法
const clearDraft = (confirm = true) => {
  const executeClear = () => {
    localStorage.removeItem(STORAGE_KEY);
    Object.assign(form, emptyForm);
  };

  if (confirm) {
    ElMessageBox.confirm('确定要清空当前已填写的所有内容吗？', '提示', { type: 'warning' })
      .then(executeClear);
  } else {
    executeClear();
  }
};

const handleConfirmDelete = async (row: any) => {
  try {
    await customerStore.removeCustomer(row.customerCode, CURRENT_STAFF_ID);
    ElMessage.success('删除成功');
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败');
  }
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '-';
  return timeStr.replace('T', ' ').substring(0, 16);
};
</script>