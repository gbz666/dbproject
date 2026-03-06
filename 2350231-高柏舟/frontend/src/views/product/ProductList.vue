<template>
  <div class="app-container" style="padding: 20px">
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="store.queryParams">
        <el-form-item label="产品编码">
          <el-input 
            v-model="store.queryParams.productCode" 
            placeholder="请输入编码" 
            clearable 
            @clear="handleSearch" 
          />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input 
            v-model="store.queryParams.productName" 
            placeholder="请输入名称" 
            clearable 
            @clear="handleSearch" 
          />
        </el-form-item>

        <el-form-item label="产品分类">
          <el-select 
            v-model="store.queryParams.categoryName" 
            placeholder="请选择分类" 
            clearable 
            filterable
            style="width: 200px"
            @change="handleSearch"
            @clear="handleSearch"
          >
            <el-option
              v-for="item in utilStore.productTypeList"
              :key="item.code"
              :label="item.name"
              :value="item.name" 
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
        
        <el-form-item style="float: right">
          <el-button type="success" @click="openDialog()">新增产品</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table 
      v-loading="store.loading" 
      :data="store.products" 
      border 
      stripe 
      style="width: 100%"
    >
      <el-table-column prop="productCode" label="产品编码" width="120" align="center" />
      <el-table-column prop="productName" label="产品名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="120" align="center" />
      <el-table-column prop="sku" label="SKU" width="120" />
      <el-table-column prop="specification" label="规格" show-overflow-tooltip />
      <el-table-column prop="costPrice" label="成本价" width="110" align="right">
        <template #default="{ row }">¥{{ row.costPrice?.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="listPrice" label="建议售价" width="110" align="right">
        <template #default="{ row }">¥{{ row.listPrice?.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm 
            :title="`确定删除产品 ${row.productName} 吗？`" 
            @confirm="handleDelete(row.productCode)"
          >
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="store.queryParams.pageNum"
        v-model:page-size="store.queryParams.pageSize"
        :total="store.total"
        layout="total, prev, pager, next, jumper"
        @current-change="store.loadPage"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑产品' : '新增产品 (已开启自动暂存)'"
      width="800px"
      destroy-on-close
    >
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="100px">
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入产品全称" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属分类" prop="categoryName">
              <el-select
                v-model="form.categoryName"
                filterable
                remote
                reserve-keyword
                placeholder="输入关键字搜索分类"
                :remote-method="utilStore.searchProductTypesAction"
                :loading="utilStore.productTypeLoading"
                style="width: 100%"
              >
                <el-option
                  v-for="item in utilStore.productTypeList"
                  :key="item.code"
                  :label="item.name"
                  :value="item.name"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SKU" prop="sku">
              <el-input v-model="form.sku" placeholder="库存单位" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" placeholder="如：个/箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格" prop="specification">
              <el-input v-model="form.specification" placeholder="规格型号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成本价" prop="costPrice">
              <el-input-number v-model="form.costPrice" :precision="2" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="建议售价" prop="listPrice">
              <el-input-number v-model="form.listPrice" :precision="2" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="产品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div style="display: flex; justify-content: space-between">
          <el-button v-if="!isEdit" type="info" plain @click="clearDraft">清空草稿</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { useProductStore } from '@/stores/productStore';
import { useUtilStore } from '@/stores/utilStore'; // 引入你的工具 Store
import type { ProductRequest } from '@/types/dto';

// 实例化 Store
const store = useProductStore();
const utilStore = useUtilStore();

const formRef = ref<FormInstance>();
const CURRENT_STAFF_ID = 1;
const STORAGE_KEY = 'product_form_draft';

// 状态控制
const dialogVisible = ref(false);
const isEdit = ref(false);
const submitting = ref(false);
const currentProductCode = ref(''); 

// 1. 初始化空表单结构
const initialEmptyForm: ProductRequest = {
  id: null,
  productName: '',
  categoryName: '',
  sku: '',
  unit: '',
  specification: '',
  description: '',
  costPrice: 0,
  listPrice: 0
};

// 2. 从本地缓存恢复数据
const getStoredForm = () => {
  const saved = localStorage.getItem(STORAGE_KEY);
  return saved ? JSON.parse(saved) : { ...initialEmptyForm };
};

const form = reactive<ProductRequest>(getStoredForm());

// 3. 侦听器：自动保存新增模式下的草稿
watch(
  form,
  (newVal) => {
    if (!isEdit.value) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(newVal));
    }
  },
  { deep: true }
);

// 表单验证规则
const formRules = reactive<FormRules>({
  productName: [{ required: true, message: '产品名称必填', trigger: 'blur' }],
  categoryName: [{ required: true, message: '分类必填', trigger: 'change' }], // 注意：Select 触发通常是 change
  sku: [{ required: true, message: 'SKU必填', trigger: 'blur' }]
});

// 生命周期挂载
onMounted(() => {
  // 加载产品分页列表数据
  store.loadPage();
  // 初始加载分类下拉列表（传空字符串获取前15条）
  utilStore.searchProductTypesAction(''); 
});

// 搜索处理
const handleSearch = () => {
  store.queryParams.pageNum = 1;
  store.loadPage();
};

// 重置搜索条件
const resetFilters = () => {
  store.queryParams.productCode = '';
  store.queryParams.productName = '';
  store.queryParams.categoryName = '';
  handleSearch();
};

// 打开对话框逻辑
const openDialog = (row?: any) => {
  isEdit.value = !!row;
  if (row) {
    currentProductCode.value = row.productCode;
    // 编辑模式：回显行数据
    Object.keys(initialEmptyForm).forEach(key => {
      (form as any)[key] = row[key] ?? (initialEmptyForm as any)[key];
    });
  } else {
    // 新增模式：不重置 reactive，保留 watch 恢复的草稿
    currentProductCode.value = '';
    // 确保弹窗打开时下拉列表有数据
    utilStore.searchProductTypesAction('');
  }
  dialogVisible.value = true;
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    submitting.value = true;
    try {
      if (isEdit.value) {
        // 更新逻辑
        await store.updateProduct(currentProductCode.value, { ...form }, CURRENT_STAFF_ID);
        ElMessage.success('更新成功');
      } else {
        // 新增逻辑
        await store.addProduct({ ...form }, CURRENT_STAFF_ID);
        ElMessage.success('新增成功');
        // 保存成功后清空草稿
        clearDraft(false);
      }
      dialogVisible.value = false;
    } catch (err: any) {
      ElMessage.error(err.message || '操作失败');
    } finally {
      submitting.value = false;
    }
  });
};

// 清空草稿方法
const clearDraft = (needConfirm = true) => {
  const doClear = () => {
    localStorage.removeItem(STORAGE_KEY);
    Object.assign(form, initialEmptyForm);
  };

  if (needConfirm) {
    ElMessageBox.confirm('确定要清空已填写的产品信息吗？', '提示', { type: 'warning' })
      .then(doClear)
      .catch(() => {});
  } else {
    doClear();
  }
};

// 删除产品
const handleDelete = async (code: string) => {
  try {
    await store.deleteItem(code);
    ElMessage.success('删除成功');
  } catch (err: any) {
    ElMessage.error(err.message || '删除失败');
  }
};
</script>

<style scoped>
.app-container {
  background-color: #f5f7fa;
  min-height: 100vh;
}
</style>