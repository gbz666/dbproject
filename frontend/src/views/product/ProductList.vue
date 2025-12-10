<template>
  <div class="product-management">
    <h2>产品管理</h2>

    <div class="header-actions">
      <el-button @click="productStore.fetchProducts" :loading="productStore.loading" icon="Refresh">
        刷新数据
      </el-button>
      <el-button type="primary" @click="openCreateDialog" icon="Plus">
        创建新产品
      </el-button>
    </div>

    <el-alert 
      v-if="productStore.error" 
      title="数据加载/操作错误" 
      type="error" 
      :description="productStore.error" 
      show-icon 
      closable 
      style="margin-bottom: 15px;"
    />

    <el-table 
      v-loading="productStore.loading"
      :data="productStore.products" 
      style="width: 100%" 
      border
      stripe
    >
      <el-table-column prop="product_id" label="产品ID" width="100" />
      <el-table-column prop="product_name" label="产品名称" width="180" show-overflow-tooltip />
      <el-table-column prop="category" label="类别" width="120" />
      <el-table-column prop="product_description" label="描述" min-width="250" show-overflow-tooltip />
      <el-table-column prop="created_at" label="创建日期" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.created_at) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row.product_id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog 
      :title="isEditMode ? '编辑产品信息' : '创建新产品'" 
      v-model="dialogVisible" 
      width="50%"
      destroy-on-close
    >
      <el-form 
        :model="currentProduct" 
        :rules="formRules" 
        ref="productFormRef" 
        label-width="100px"
      >
        <el-form-item label="产品名称" prop="product_name">
          <el-input v-model="currentProduct.product_name" placeholder="请输入产品名称" />
        </el-form-item>

        <el-form-item label="产品类别" prop="category">
          <el-input v-model="currentProduct.category" placeholder="请输入产品类别" />
        </el-form-item>
        
        <el-form-item label="产品描述" prop="product_description">
          <el-input 
            v-model="currentProduct.product_description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入详细描述"
          />
        </el-form-item>
        
        <el-form-item label="产品ID" v-if="isEditMode">
          <el-input v-model="currentProduct.product_id" readonly />
        </el-form-item>
        <el-form-item label="创建时间" v-if="isEditMode">
          <el-input :model-value="formatDateTime(currentProduct.created_at)" readonly />
        </el-form-item>

      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useProductStore } from '@/stores/productStore';
import type { Product } from '@/types/models'; // 假设 Product 接口已包含
// 导入 Element Plus 组件类型和消息提示
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import 'element-plus/es/components/message/style/css';
import 'element-plus/es/components/message-box/style/css';

// --- Pinia Store ---
const productStore = useProductStore();

// --- 响应式状态 ---
const dialogVisible = ref(false);
const isEditMode = ref(false);
const productFormRef = ref<FormInstance>();

// 默认产品结构，用于表单重置
const createDefaultProduct = (): Omit<Product, 'product_id' | 'created_at'> => ({
  product_name: '',
  product_description: '',
  category: '',
});

// 当前正在编辑/创建的产品数据 (使用 reactive 包装，包含所有字段)
const currentProduct = reactive<Partial<Product>>({ 
    ...createDefaultProduct(),
    product_id: undefined, // 保持可选
    created_at: undefined, // 保持可选
});


// --- 表单校验规则 ---
const formRules: FormRules = {
  product_name: [
    { required: true, message: '请输入产品名称', trigger: 'blur' },
    { max: 100, message: '产品名称不能超过100个字符', trigger: 'blur' },
  ],
  category: [
    { required: true, message: '请输入产品类别', trigger: 'blur' },
  ],
  product_description: [
    { required: true, message: '请输入产品描述', trigger: 'blur' },
  ],
};


// --- 辅助方法 ---

/** 格式化日期时间显示 */
const formatDateTime = (dateTimeStr: string | undefined) => {
  if (!dateTimeStr) return '';
  try {
    // 格式化为本地易读格式
    return new Date(dateTimeStr).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
    });
  } catch {
    return dateTimeStr;
  }
};

/**
 * 重置表单数据到默认状态
 */
const resetCurrentProduct = () => {
  // 使用 Object.assign 确保 currentProduct 仍然是同一个响应式对象
  Object.assign(currentProduct, createDefaultProduct());
  currentProduct.product_id = undefined;
  currentProduct.created_at = undefined;
};

// --- CRUD 操作 ---

/**
 * 打开创建产品对话框
 */
const openCreateDialog = () => {
  isEditMode.value = false;
  resetCurrentProduct();
  // 延迟重置表单校验
  setTimeout(() => {
    productFormRef.value?.resetFields();
  }, 0);
  dialogVisible.value = true;
};

/**
 * 打开编辑产品对话框
 */
const openEditDialog = (product: Product) => {
  isEditMode.value = true;
  // 复制产品数据到 currentProduct
  Object.assign(currentProduct, { ...product }); 
  dialogVisible.value = true;
};

/**
 * 处理表单提交 (创建或更新)
 */
const handleSubmit = async () => {
  if (!productFormRef.value) return;

  // 1. 校验表单
  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEditMode.value) {
          // 2. 更新模式
          const id = currentProduct.product_id;
          if (typeof id !== 'number') {
            throw new Error("更新失败：产品ID缺失或无效");
          }
          // 排除不需要提交给后端的字段 (如 created_at, product_id)
          const updateData: Partial<Product> = {
            product_name: currentProduct.product_name,
            product_description: currentProduct.product_description,
            category: currentProduct.category,
            // 可以在这里添加其他需要更新的字段
          };

          await productStore.updateProduct(id, updateData);
          ElMessage.success('产品信息更新成功！');

        } else {
          // 3. 创建模式
          // Omit<Product, 'product_id'> 保证只有可创建字段
          const createData: Omit<Product, 'product_id'> = {
            product_name: currentProduct.product_name!,
            product_description: currentProduct.product_description!,
            category: currentProduct.category!,
            // 关键修正：自动填充当前时间
            created_at: new Date().toISOString(), 
          };
          await productStore.createProduct(createData);
          ElMessage.success('新产品创建成功！');
        }

        dialogVisible.value = false; // 关闭对话框
        resetCurrentProduct(); // 重置表单
      } catch (e) {
        // 错误已在 store 中处理，这里只做额外提示
        ElMessage.error(`操作失败: ${productStore.error || '未知错误'}`);
      }
    } else {
      ElMessage.warning('请检查表单中的必填项！');
    }
  });
};

/**
 * 处理删除产品
 */
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除产品ID为 ${id} 的产品吗？此操作不可逆！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    // 调用 store 的删除 action
    await productStore.deleteProduct(id);
    ElMessage.success(`产品 ID: ${id} 删除成功！`);
  } catch (error) {
    // 捕获用户点击取消时的错误（Promise.reject('cancel')）
    if (error !== 'cancel') {
      console.error('删除操作失败:', error);
      ElMessage.error(`删除失败: ${productStore.error || '未知错误'}`);
    }
  }
};

// --- 生命周期钩子 ---
onMounted(() => {
  // 组件挂载时自动加载数据
  productStore.fetchProducts();
});
</script>

<style scoped>
.product-management {
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