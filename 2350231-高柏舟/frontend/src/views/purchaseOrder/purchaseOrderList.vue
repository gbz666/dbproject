<template>
  <div class="app-container" style="padding: 20px">
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="purchaseStore.queryParams">
        <el-form-item label="采购单号">
          <el-input v-model="purchaseStore.queryParams.purchaseOrderCode" placeholder="cg..." clearable @keyup.enter="purchaseStore.fetchPageAction" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="purchaseStore.queryParams.supplierName" placeholder="供应商名" style="width: 130px" />
          <el-input v-model="purchaseStore.queryParams.supplierCode" placeholder="编号" style="width: 90px; margin-left: 5px" />
        </el-form-item>
        <el-form-item label="产品">
          <el-input v-model="purchaseStore.queryParams.productName" placeholder="产品名" style="width: 130px" />
          <el-input v-model="purchaseStore.queryParams.productCode" placeholder="编号" style="width: 90px; margin-left: 5px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="purchaseStore.fetchPageAction">查询</el-button>
          <el-button @click="handleResetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleOpenDialog()">新增采购单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="purchaseStore.loading" :data="purchaseStore.orderList" border stripe style="width: 100%">
      <el-table-column type="expand">
        <template #default="{ row }">
          <div style="padding: 10px 40px">
            <h4 style="margin-bottom: 10px">采购清单明细</h4>
            <el-table :data="row.items" size="small" border>
              <el-table-column prop="productCode" label="产品编号" width="120" />
              <el-table-column prop="productName" label="产品名称" min-width="150" />
              <el-table-column prop="quantity" label="数量" width="100" />
              <el-table-column prop="unitPrice" label="进价" width="100">
                <template #default="s">¥{{ s.row.unitPrice.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="小计" width="120">
                <template #default="s">
                  <b>¥{{ (s.row.quantity * s.row.unitPrice).toFixed(2) }}</b>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="purchaseCode" label="单号" width="150" align="center" fixed />
      <el-table-column prop="orderDate" label="日期" width="120" sortable />
      <el-table-column prop="supplierCode" label="供应商编号" width="100" />
      <el-table-column prop="supplierName" label="供应商名称" min-width="180" show-overflow-tooltip />
      <el-table-column label="总金额" width="120" align="right">
        <template #default="{ row }">
          <span style="font-weight: bold; color: #f56c6c">¥{{ row.totalAmount.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="note" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleOpenDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除吗？" @confirm="handleConfirmDelete(row)">
            <template #reference><el-button link type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="purchaseStore.queryParams.pageNum"
        v-model:page-size="purchaseStore.queryParams.pageSize"
        :total="purchaseStore.total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="purchaseStore.fetchPageAction"
        @size-change="purchaseStore.fetchPageAction"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '修改采购单' : '新增采购单 (草稿自动保存)'" width="900px" destroy-on-close>
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierCode">
              <el-select
                v-model="form.supplierCode"
                filterable
                remote
                reserve-keyword
                placeholder="输入供应商名称搜索"
                :remote-method="utilStore.searchSupplierAction"
                :loading="utilStore.SupplierLoading"
                style="width: 100%"
              >
                <el-option
                  v-for="item in utilStore.SupplierList"
                  :key="item.code"
                  :label="item.name"
                  :value="item.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单日期" prop="orderDate">
              <el-date-picker v-model="form.orderDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" :rows="2" />
        </el-form-item>

        <el-divider>产品明细</el-divider>
        <el-table :data="form.items" border size="small">
          <el-table-column label="产品" width="300">
            <template #default="{ row }">
              <el-select
                v-model="row.productCode"
                filterable
                remote
                reserve-keyword
                placeholder="输入产品名称搜索"
                :remote-method="utilStore.searchProductsAction"
                :loading="utilStore.productLoading"
                style="width: 100%"
              >
                <el-option
                  v-for="item in utilStore.productList"
                  :key="item.code"
                  :label="`${item.name} (${item.code})`"
                  :value="item.code"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="160">
            <template #default="{ row }"><el-input-number v-model="row.quantity" :min="1" /></template>
          </el-table-column>
          <el-table-column label="单价" width="180">
            <template #default="{ row }"><el-input-number v-model="row.unitPrice" :precision="2" /></template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button type="danger" :icon="Delete" circle @click="removeItemRow($index)" />
            </template>
          </el-table-column>
        </el-table>
        <el-button class="btn-dashed" style="width: 100%; margin-top: 10px" @click="addItemRow">+ 添加产品</el-button>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: space-between">
          <el-button v-if="!isEdit" type="info" plain @click="handleClearDraft">清空草稿</el-button>
          <div style="flex: 1; text-align: right">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="submitLoading" @click="handleSave">提交</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue';
import { usePurchaseStore } from '@/stores/purchaseOrderStore';
import { useUtilStore } from '@/stores/utilStore';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Delete } from '@element-plus/icons-vue';
import type { FormInstance, FormRules } from 'element-plus';
import type { PurchaseOrderDto } from '@/types/dto';
import type { PurchaseOrderVO } from '@/types/vo';

const purchaseStore = usePurchaseStore();
const utilStore = useUtilStore();
const CURRENT_USER_ID = 2;
const STORAGE_KEY = 'purchase_order_draft'; 

const dialogVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();

const getEmptyForm = (): PurchaseOrderDto => ({
  supplierCode: '',
  orderDate: new Date().toISOString().split('T')[0] as string,
  note: '',
  items: []
});

const initFormWithStorage = (): PurchaseOrderDto => {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (saved) {
    try {
      return JSON.parse(saved);
    } catch (e) {
      console.error('解析草稿失败', e);
    }
  }
  return getEmptyForm();
};

const form = reactive<PurchaseOrderDto>(initFormWithStorage());

watch(
  form,
  (newVal) => {
    if (!isEdit.value) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(newVal));
    }
  },
  { deep: true }
);

const formRules = reactive<FormRules>({
  supplierCode: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  orderDate: [{ required: true, message: '必填', trigger: 'change' }]
});

const handleResetQuery = () => {
  purchaseStore.queryParams.supplierCode = "";
  purchaseStore.queryParams.supplierName = "";
  purchaseStore.queryParams.productCode = "";
  purchaseStore.queryParams.productName = "";
  purchaseStore.queryParams.purchaseOrderCode = "";
  purchaseStore.fetchPageAction();
};

const handleOpenDialog = (row?: PurchaseOrderVO) => {
  isEdit.value = !!row;
  if (row) {
    form.id = row.id;
    form.supplierCode = row.supplierCode ?? '';
    form.orderDate = row.orderDate ?? '';
    form.note = row.note ?? '';
    form.items = row.items ? row.items.map(i => ({
      productCode: i.productCode,
      quantity: i.quantity,
      unitPrice: i.unitPrice
    })) : [];
  } else {
    const draft = initFormWithStorage();
    Object.assign(form, draft);
    if (form.items.length === 0) addItemRow();
  }
  dialogVisible.value = true;
};

const handleClearDraft = () => {
  ElMessageBox.confirm('确定要清空已填写的草稿内容吗？', '提示', { type: 'warning' })
    .then(() => {
      localStorage.removeItem(STORAGE_KEY);
      Object.assign(form, getEmptyForm());
      addItemRow();
      ElMessage.success('草稿已清空');
    });
};

const addItemRow = () => form.items.push({ productCode: '', quantity: 1, unitPrice: 0 });
const removeItemRow = (index: number) => form.items.splice(index, 1);

const handleSave = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    submitLoading.value = true;
    try {
      const success = await purchaseStore.submitOrderAction(form, CURRENT_USER_ID);
      if (success) {
        localStorage.removeItem(STORAGE_KEY);
        dialogVisible.value = false;
      }
    } finally {
      submitLoading.value = false;
    }
  });
};

const handleConfirmDelete = (row: PurchaseOrderVO) => {
  purchaseStore.deleteOrderAction(row.id, CURRENT_USER_ID);
};

onMounted(() => purchaseStore.fetchPageAction());
</script>

<style scoped>
/* 修复 dashed 按钮报错问题并模拟虚线样式 */
.btn-dashed {
  border-style: dashed !important;
  border-color: var(--el-border-color);
  color: var(--el-text-color-regular);
  background-color: transparent;
}
.btn-dashed:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
}
</style>