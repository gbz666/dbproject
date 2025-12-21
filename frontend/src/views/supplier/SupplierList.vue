<template>
  <div class="app-container" style="padding: 20px">
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="store.searchFilters">
        <el-form-item label="供应商编号">
          <el-input v-model="store.searchFilters.supplierCode" placeholder="请输入编号" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="供应商名称">
          <el-input v-model="store.searchFilters.supplierName" placeholder="请输入名称" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="openDialog()">新增供应商</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table 
      v-loading="store.loading" 
      :data="store.list" 
      border 
      stripe 
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="supplierCode" label="业务编号" width="120" />
      <el-table-column prop="supplierName" label="供应商名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="mainBusiness" label="主营业务" />
      <el-table-column prop="updatedAt" label="最后更新" width="180">
        <template #default="{ row }">
          {{ formatTime(row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除该供应商吗？" @confirm="handleDelete(row.supplierCode)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="store.pagination.pageNum"
        v-model:page-size="store.pagination.pageSize"
        :total="store.total"
        layout="total, prev, pager, next, jumper"
        @current-change="store.loadData"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑供应商' : '新增供应商 (已开启自动暂存)'"
      width="600px"
      destroy-on-close
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="供应商名称" required>
          <el-input v-model="form.supplierName" />
        </el-form-item>
        <el-form-item label="简称">
          <el-input v-model="form.shortName" />
        </el-form-item>
        <el-form-item label="主营业务">
          <el-input v-model="form.mainBusiness" type="textarea" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="税号">
              <el-input v-model="form.taxNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="归属人">
          <el-input v-model="form.ownerName" placeholder="对应后端 Staff 姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: space-between">
          <el-button v-if="!isEdit" type="info" plain @click="handleClearDraft">清空草稿</el-button>
          <div style="flex: 1; text-align: right">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="submitForm">确认保存</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue';
import { useSupplierStore } from '@/stores/supplierStore';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { SupplierRequest } from '@/types/dto';

const store = useSupplierStore();
const CURRENT_STAFF_ID = 2; // 当前用户：高
const STORAGE_KEY = 'supplier_form_draft';

// 状态控制
const dialogVisible = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const currentEditCode = ref('');

// 1. 定义空的初始状态
const initialEmptyForm: SupplierRequest = {
  supplierName: '',
  shortName: '',
  mainBusiness: '',
  taxNo: '',
  address: '',
  phone: '',
  email: '',
  salesPersonName: '高', 
  followUpPersonName: '高',
  ownerName: '高'
};

// 2. 初始化逻辑：优先加载草稿
const getStoredForm = () => {
  const saved = localStorage.getItem(STORAGE_KEY);
  return saved ? JSON.parse(saved) : { ...initialEmptyForm };
};

const form = reactive<SupplierRequest>(getStoredForm());

// 3. 监听：自动保存草稿
watch(
  form,
  (newVal) => {
    // 仅在新增模式下同步到本地，防止编辑数据污染草稿
    if (!isEdit.value) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(newVal));
    }
  },
  { deep: true }
);

onMounted(() => {
  store.loadData();
});

const handleSearch = () => {
  store.pagination.pageNum = 1;
  store.loadData();
};

const resetFilters = () => {
  store.searchFilters.supplierCode = '';
  store.searchFilters.supplierName = '';
  handleSearch();
};

const openDialog = (row?: any) => {
  isEdit.value = !!row;
  if (row) {
    currentEditCode.value = row.supplierCode;
    // 编辑模式：直接加载行数据回显
    Object.assign(form, row);
  } else {
    // 新增模式：保持当前 reactive 中的值（即暂存的值）
    currentEditCode.value = '';
  }
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (!form.supplierName) {
    ElMessage.warning('供应商名称必填');
    return;
  }
  
  submitting.value = true;
  try {
    await store.submitForm(form, CURRENT_STAFF_ID, isEdit.value ? currentEditCode.value : undefined);
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功');
    
    // 保存成功后处理：如果是新增，则清空草稿
    if (!isEdit.value) {
      clearFormState(false);
    }
    
    dialogVisible.value = false;
  } catch (err: any) {
    ElMessage.error(err.message || '操作失败');
  } finally {
    submitting.value = false;
  }
};

// 抽取清理逻辑
const clearFormState = (needConfirm = true) => {
  const doClear = () => {
    localStorage.removeItem(STORAGE_KEY);
    Object.assign(form, initialEmptyForm);
  };

  if (needConfirm) {
    ElMessageBox.confirm('确定要清空当前已填写的供应商信息吗？', '提示', { type: 'warning' })
      .then(doClear);
  } else {
    doClear();
  }
};

const handleClearDraft = () => clearFormState(true);

const handleDelete = async (code: string) => {
  try {
    await store.deleteByCode(code, CURRENT_STAFF_ID);
    ElMessage.success('删除成功');
  } catch (err: any) {
    ElMessage.error(err.message || '删除失败');
  }
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '-';
  return timeStr.replace('T', ' ').substring(0, 16);
};
</script>