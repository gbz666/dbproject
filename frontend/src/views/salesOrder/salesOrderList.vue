<template>
  <div class="app-container" style="padding: 20px">
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="salesStore.queryParams">
        <el-form-item label="订单号">
          <el-input v-model="salesStore.queryParams.salesOrderCode" placeholder="xs..." clearable />
        </el-form-item>
        <el-form-item label="客户信息">
          <el-input v-model="salesStore.queryParams.customerName" placeholder="客户名" style="width: 130px" />
          <el-input v-model="salesStore.queryParams.customerCode" placeholder="编号" style="width: 90px; margin-left: 5px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="salesStore.fetchPageAction">查询</el-button>
          <el-button @click="handleResetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleOpenDialog()">新增销售单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="salesStore.loading" :data="salesStore.orderList" border stripe style="width: 100%">
      <el-table-column type="expand">
        <template #default="{ row }">
          <div style="padding: 10px 40px">
            <h4 style="margin-bottom: 10px">订单详细明细</h4>
            <el-table :data="row.items" size="small" border>
              <el-table-column prop="productCode" label="产品编号" width="120" />
              <el-table-column prop="productName" label="产品名称" min-width="150" />
              <el-table-column prop="quantity" label="数量" width="100" />
              <el-table-column prop="unitPrice" label="成交单价" width="100">
                <template #default="s">¥{{ s.row.unitPrice }}</template>
              </el-table-column>
              <el-table-column label="单项小计" width="120">
                <template #default="s">
                  <b style="color: #f56c6c">¥{{ (s.row.quantity * s.row.unitPrice).toFixed(2) }}</b>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="orderCode" label="订单号" width="130" align="center" fixed />
      <el-table-column prop="orderDate" label="订单日期" width="120" sortable />
      <el-table-column prop="customerCode" label="客户编号" width="100" />
      <el-table-column prop="customerName" label="客户全称" min-width="180" show-overflow-tooltip />
      
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleOpenDialog(row)">编辑</el-button>
          <el-popconfirm title="确定彻底删除订单吗？" @confirm="handleConfirmDelete(row)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="salesStore.queryParams.pageNum"
        v-model:page-size="salesStore.queryParams.pageSize"
        :total="salesStore.total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="salesStore.fetchPageAction"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '修改销售订单' : '新增销售订单'" width="950px" destroy-on-close>
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择客户" prop="customerCode">
              <el-select
                v-model="form.customerCode"
                filterable
                remote
                reserve-keyword
                placeholder="请输入客户名称搜索"
                :remote-method="remoteSearchCustomer"
                :loading="utilStore.customerLoading"
                style="width: 100%"
              >
                <el-option
                  v-for="item in utilStore.customerList"
                  :key="item.code"
                  :label="`${item.name} (${item.code})`"
                  :value="item.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="订单日期" prop="orderDate">
              <el-date-picker 
                v-model="form.orderDate" 
                type="date" 
                value-format="YYYY-MM-DD" 
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.note" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-table :data="form.items" border size="small" style="margin-top: 10px">
          <el-table-column label="选择产品" min-width="250">
            <template #default="{ row }">
              <el-select
                v-model="row.productCode"
                filterable
                remote
                reserve-keyword
                placeholder="产品名称/编号"
                :remote-method="remoteSearchProduct"
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
            <template #default="{ row }"><el-input-number v-model="row.quantity" :min="0.0001" :precision="4" /></template>
          </el-table-column>
          <el-table-column label="成交单价" width="160">
            <template #default="{ row }"><el-input-number v-model="row.unitPrice" :precision="2" :min="0" /></template>
          </el-table-column>
          <el-table-column label="操作" width="60" align="center">
            <template #default="{ $index }">
              <el-button type="danger" :icon="Delete" circle @click="removeItemRow($index)" />
            </template>
          </el-table-column>
        </el-table>
        <el-button type="dashed" style="width: 100%; margin-top: 10px" @click="addItemRow">+ 添加产品明细</el-button>
      </el-form>

      <template #footer>
        <div style="display: flex; justify-content: space-between">
          <el-button v-if="!isEdit" type="info" plain @click="handleClearDraft">清空草稿</el-button>
          <div>
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
import { useSalesStore } from '@/stores/salesOrderStore';
import { useUtilStore } from '@/stores/utilStore'; // 引入工具 store
import { ElMessage, ElMessageBox } from 'element-plus';
import { Delete } from '@element-plus/icons-vue';
import type { FormInstance, FormRules } from 'element-plus';
import type { SalesOrderDTO } from '@/types/dto';

// Store 初始化
const salesStore = useSalesStore();
const utilStore = useUtilStore();

const CURRENT_STAFF_ID = 2; 
const STORAGE_KEY = 'sales_order_draft';

const dialogVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const editId = ref<number>();

// 1. 远程搜索逻辑
const remoteSearchCustomer = (query: string) => {
  if (query) {
    utilStore.searchCustomersAction(query);
  }
};

const remoteSearchProduct = (query: string) => {
  if (query) {
    utilStore.searchProductsAction(query);
  }
};

// 2. 日期与表单基础
const getTodayDate = (): string => new Date().toISOString().split('T')[0];

const createEmptyForm = (): SalesOrderDTO => ({
  customerCode: '',
  orderDate: getTodayDate(),
  note: '',
  items: []
});

const getInitialData = (): SalesOrderDTO => {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (saved && !isEdit.value) {
    try {
      return JSON.parse(saved);
    } catch (e) {
      console.error("解析草稿失败", e);
    }
  }
  return { ...createEmptyForm(), items: [{ productCode: '', quantity: 1, unitPrice: 0 }] };
};

const form = reactive<SalesOrderDTO>(getInitialData());

// 3. 草稿观察者
watch(form, (v) => { 
  if (!isEdit.value && dialogVisible.value) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(v));
  }
}, { deep: true });

const formRules = reactive<FormRules>({
  customerCode: [{ required: true, message: '请选择客户', trigger: 'change' }],
  orderDate: [{ required: true, message: '请选择订单日期', trigger: 'change' }]
});

// 4. 操作函数
const handleOpenDialog = (row?: any) => {
  isEdit.value = !!row;
  if (row) {
    editId.value = row.id;
    // 编辑模式回填，同时手动触发一次搜索以显示名称
    form.customerCode = row.customerCode;
    utilStore.searchCustomersAction(row.customerCode); 
    
    form.orderDate = row.orderDate?.substring(0, 10) || getTodayDate();
    form.note = row.note || '';
    form.items = row.items?.map((i: any) => {
      utilStore.searchProductsAction(i.productCode); // 预加载产品信息显示
      return { productCode: i.productCode, quantity: i.quantity, unitPrice: i.unitPrice };
    }) || [];
  } else {
    editId.value = undefined;
    Object.assign(form, getInitialData());
  }
  dialogVisible.value = true;
};

const handleSave = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    if (form.items.some(item => !item.productCode)) {
      ElMessage.warning('明细中存在未选择产品的行');
      return;
    }

    submitLoading.value = true;
    const dto = { ...form, id: editId.value };
    await salesStore.submitOrderAction(dto, CURRENT_STAFF_ID);
    
    ElMessage.success(isEdit.value ? '修改成功' : '创建成功');
    if (!isEdit.value) localStorage.removeItem(STORAGE_KEY);
    dialogVisible.value = false;
    salesStore.fetchPageAction();
  } catch (error) {
    console.error('提交失败:', error);
  } finally {
    submitLoading.value = false;
  }
};

const handleClearDraft = () => {
  ElMessageBox.confirm('确定清空草稿吗？', '提示', { type: 'warning' }).then(() => {
    localStorage.removeItem(STORAGE_KEY);
    Object.assign(form, createEmptyForm());
    form.items = [{ productCode: '', quantity: 1, unitPrice: 0 }];
  });
};

const handleResetQuery = () => {
  salesStore.queryParams = { pageNum: 1, pageSize: 10, customerName: '', customerCode: '', productCode: '', productName: '', salesOrderCode: '' };
  salesStore.fetchPageAction();
};

const addItemRow = () => form.items.push({ productCode: '', quantity: 1, unitPrice: 0 });
const removeItemRow = (i: number) => form.items.splice(i, 1);
const handleConfirmDelete = (row: any) => salesStore.deleteOrderAction(row.id).then(() => {
  ElMessage.success('已删除');
  salesStore.fetchPageAction();
});

onMounted(() => salesStore.fetchPageAction());
</script>

<style scoped>
.app-container :deep(.el-table__expanded-cell) {
  background-color: #fafafa;
}
</style>