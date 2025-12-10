<template>
  <div class="sales-order-management">
    <h2>销售订单管理</h2>
    
    <div class="header-actions">
      <el-button @click="salesOrderStore.fetchSalesOrders" :loading="salesOrderStore.loading" icon="Refresh">
        刷新数据
      </el-button>
      <el-button type="primary" @click="openCreateDialog" icon="Plus">
        创建销售订单
      </el-button>
    </div>
    
    <el-alert 
      v-if="salesOrderStore.error" 
      title="数据加载/操作错误" 
      type="error" 
      :description="salesOrderStore.error" 
      show-icon 
      closable 
      style="margin-bottom: 15px;"
    />

    <el-table 
      v-loading="salesOrderStore.loading"
      :data="salesOrderStore.salesOrders" 
      style="width: 100%" 
      border
      stripe
    >
      <el-table-column prop="order_id" label="订单ID" width="100" />
      <el-table-column prop="customer_id" label="客户ID" width="100" />
      <el-table-column prop="product_id" label="产品ID" width="100" />
      <el-table-column prop="order_date" label="订单日期" width="120">
        <template #default="{ row }">
          {{ formatDate(row.order_date) }}
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" align="right" />
      <el-table-column prop="unit_price" label="单价" width="100" align="right" />
      <el-table-column prop="total_amount" label="总金额" width="120" align="right">
        <template #default="{ row }">
          ¥ {{ row.total_amount.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="follow_up_person" label="跟进人" width="100" />
      <el-table-column prop="notes" label="备注" min-width="150" show-overflow-tooltip />
      
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row.order_id!)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog 
      :title="isEditMode ? '编辑销售订单' : '创建销售订单'" 
      v-model="dialogVisible" 
      width="70%"
      destroy-on-close
    >
      <el-form 
        :model="currentOrder" 
        :rules="formRules" 
        ref="orderFormRef" 
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="订单日期" prop="order_date">
              <el-date-picker
                v-model="currentOrder.order_date"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DDTHH:mm:ss.sssZ" 
                style="width: 100%"
                :disabled="isEditMode"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="客户ID" prop="customer_id">
              <el-input v-model="currentOrder.customer_id" placeholder="客户ID" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="产品ID" prop="product_id">
              <el-input v-model="currentOrder.product_id" placeholder="产品ID" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number 
                v-model.number="currentOrder.quantity" 
                :min="1" 
                :precision="0"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="单价" prop="unit_price">
              <el-input-number 
                v-model.number="currentOrder.unit_price" 
                :min="0.01" 
                :precision="2" 
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="成本价" prop="cost_price">
              <el-input-number 
                v-model.number="currentOrder.cost_price" 
                :min="0" 
                :precision="2" 
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="总金额">
              <el-input 
                :model-value="`¥ ${calculatedTotalAmount.toFixed(2)}`" 
                readonly 
                placeholder="自动计算"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="跟进人" prop="follow_up_person">
              <el-input v-model="currentOrder.follow_up_person" placeholder="跟进人" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="客户负责人" prop="customer_owner">
              <el-input v-model="currentOrder.customer_owner" placeholder="客户负责人" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="绩效负责人" prop="performance_owner">
              <el-input v-model="currentOrder.performance_owner" placeholder="绩效负责人" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="采购小计" prop="purchase_subtotal">
              <el-input-number 
                v-model.number="currentOrder.purchase_subtotal" 
                :min="0" 
                :precision="2" 
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="数据类别" prop="data_category">
              <el-input v-model="currentOrder.data_category" placeholder="例如：新客户/老客户" />
            </el-form-item>
          </el-col>
          
          <el-col :span="12" v-if="isEditMode">
            <el-form-item label="状态" prop="status">
              <el-select v-model="currentOrder.status" placeholder="选择状态" style="width: 100%">
                <el-option label="待处理" value="pending" />
                <el-option label="已完成" value="completed" />
                <el-option label="已取消" value="cancelled" />
                </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="备注" prop="notes">
              <el-input 
                v-model="currentOrder.notes" 
                type="textarea" 
                :rows="2" 
                placeholder="输入备注信息"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useSalesOrderStore } from '@/stores/salesOrderStore';
import type { SalesOrder } from '@/types/models';
// 导入 Element Plus 组件类型和消息提示
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import 'element-plus/es/components/message/style/css';
import 'element-plus/es/components/message-box/style/css';
// 假设您已在项目中配置了 Element Plus Icons
// import { Refresh, Plus } from '@element-plus/icons-vue'; 

// --- Pinia Store ---
const salesOrderStore = useSalesOrderStore();

// --- 响应式状态 ---
const dialogVisible = ref(false);
const isEditMode = ref(false);
const orderFormRef = ref<FormInstance>();

// 默认订单结构，用于表单重置
const createDefaultOrder = (): SalesOrder => ({
  order_date: new Date().toISOString(), // 默认当天，ISO 格式
  customer_id: 0,
  product_id: 0,
  quantity: 1,
  unit_price: 0.01, // 初始值避免 NaN
  total_amount: 0,
  cost_price: 0,
  follow_up_person: '',
  customer_owner: '',
  performance_owner: '',
  purchase_subtotal: 0,
  data_category: '',
  notes: '',
  status: 'pending',
});

// 当前正在编辑/创建的订单数据 (使用 reactive 包装)
const currentOrder = reactive<SalesOrder>(createDefaultOrder());


// --- 计算属性 ---
// 计算总金额：数量 * 单价
const calculatedTotalAmount = computed(() => {
  // 确保 quantity 和 unit_price 是有效的数字
  const qty = currentOrder.quantity || 0;
  const price = currentOrder.unit_price || 0;
  return qty * price;
});


// --- 表单校验规则 ---
const formRules: FormRules = {
  order_date: [{ required: true, message: '请选择订单日期', trigger: 'change' }],
  customer_id: [{ required: true, message: '请输入客户ID', trigger: 'blur' }],
  product_id: [{ required: true, message: '请输入产品ID', trigger: 'blur' }],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量必须大于或等于1', trigger: 'blur' },
  ],
  unit_price: [
    { required: true, message: '请输入单价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '单价必须大于0', trigger: 'blur' },
  ],
  cost_price: [
    { type: 'number', min: 0, message: '成本价不能小于0', trigger: 'blur' },
  ],
};


// --- 辅助方法 ---

/** 格式化日期显示 */
const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return '';
  try {
    // 仅显示年月日
    return new Date(dateStr).toLocaleDateString('zh-CN');
  } catch {
    return dateStr;
  }
};

/** 根据状态获取 Tag 类型 */
const getStatusTagType = (status: string | undefined) => {
    switch (status) {
        case 'completed': return 'success';
        case 'cancelled': return 'danger';
        case 'pending':
        default: return 'warning';
    }
};

/** 根据状态获取中文文本 */
const getStatusText = (status: string | undefined) => {
    switch (status) {
        case 'completed': return '已完成';
        case 'cancelled': return '已取消';
        case 'pending': return '待处理';
        default: return status || '未知状态';
    }
};


/**
 * 重置表单数据到默认状态
 */
const resetCurrentOrder = () => {
  // 使用 Object.assign 确保 currentOrder 仍然是同一个响应式对象
  Object.assign(currentOrder, createDefaultOrder());
};

// --- CRUD 操作 ---

/**
 * 打开创建订单对话框
 */
const openCreateDialog = () => {
  isEditMode.value = false;
  resetCurrentOrder();
  // 延迟重置表单校验，确保对话框已打开
  setTimeout(() => {
    orderFormRef.value?.resetFields();
  }, 0);
  dialogVisible.value = true;
};

/**
 * 打开编辑订单对话框
 */
const openEditDialog = (order: SalesOrder) => {
  isEditMode.value = true;
  // 复制订单数据到 currentOrder
  Object.assign(currentOrder, { ...order }); 
  dialogVisible.value = true;
};

/**
 * 处理表单提交 (创建或更新)
 */
const handleSubmit = async () => {
  if (!orderFormRef.value) return;

  // 1. 校验表单
  await orderFormRef.value.validate(async (valid) => {
    if (valid) {
      // 2. 准备提交数据，并确保总金额是计算值
      const dataToSubmit: SalesOrder = {
        ...currentOrder,
        total_amount: calculatedTotalAmount.value, 
      };

      // 3. 移除可选且可能为 undefined 的属性，避免提交到后端
      if (!dataToSubmit.order_id) delete dataToSubmit.order_id;
      if (!dataToSubmit.created_at) delete dataToSubmit.created_at;

      try {
        if (isEditMode.value) {
          // 4. 更新模式
          if (!dataToSubmit.order_id) {
            throw new Error("更新失败：订单ID缺失");
          }
          await salesOrderStore.updateSalesOrder(
            dataToSubmit.order_id, 
            dataToSubmit
          );
          ElMessage.success('销售订单更新成功！');
        } else {
          // 5. 创建模式
          await salesOrderStore.createSalesOrder(dataToSubmit);
          ElMessage.success('销售订单创建成功！');
        }

        dialogVisible.value = false; // 关闭对话框
        resetCurrentOrder(); // 重置表单
      } catch (e) {
        // 错误已在 store 中处理，这里只需展示提示
        ElMessage.error(`操作失败: ${salesOrderStore.error || '未知错误'}`);
      }
    } else {
      ElMessage.warning('请检查表单中的必填项和格式！');
    }
  });
};

/**
 * 处理删除订单
 */
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除ID为 ${id} 的销售订单吗？此操作不可逆！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    // 调用 store 的删除 action
    await salesOrderStore.deleteSalesOrder(id);
    ElMessage.success(`订单 ID: ${id} 删除成功！`);
  } catch (error) {
    // 捕获用户点击取消时的错误（Promise.reject('cancel')）
    if (error !== 'cancel') {
      ElMessage.error(`删除失败: ${salesOrderStore.error || '未知错误'}`);
    }
  }
};

// --- 生命周期钩子 ---
onMounted(() => {
  // 组件挂载时自动加载数据
  salesOrderStore.fetchSalesOrders();
});
</script>

<style scoped>
.sales-order-management {
  padding: 20px;
}

.header-actions {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end; /* 按钮靠右对齐 */
  gap: 10px;
}

.el-table {
  margin-top: 20px;
}
</style>