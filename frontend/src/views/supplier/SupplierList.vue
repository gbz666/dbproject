<template>
  <div class="supplier-list-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>供应商列表</span>
          <el-button type="primary" @click="handleCreate">新增供应商</el-button>
        </div>
      </template>

      <el-table 
        :data="supplierStore.suppliers" 
        v-loading="supplierStore.isLoading"
        border 
        stripe 
        style="width: 100%"
        max-height="650"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="supplierCode" label="供应商编号" width="120" sortable />
        <el-table-column prop="supplierName" label="供应商名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="shortName" label="简称" width="100" />
        <el-table-column prop="mainBusiness" label="主营业务" min-width="150" show-overflow-tooltip />
        <el-table-column prop="taxNo" label="税号" width="150" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default>
            <el-button link type="primary" size="small">编辑</el-button>
            <el-button link type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-footer">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="supplierStore.pageInfo.pageNum"
          :page-sizes="[10, 20, 50, 100,300]"
          :page-size="supplierStore.pageInfo.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="supplierStore.pageInfo.total"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useSupplierStore } from '@/stores/supplierStore';
import { ElMessage } from 'element-plus';

// 使用 Pinia store
const supplierStore = useSupplierStore();

// --- 组合式 API 核心逻辑 ---

// 改变每页显示数量
const handleSizeChange = (val: number) => {
  if (supplierStore.pageInfo.pageSize !== val) {
    supplierStore.pageInfo.pageSize = val;
    supplierStore.getSuppliers(1, val);
  }
};

// 改变当前页码
const handleCurrentChange = (val: number) => {
  if (supplierStore.pageInfo.pageNum !== val) {
    supplierStore.pageInfo.pageNum = val;
    supplierStore.getSuppliers(val, supplierStore.pageInfo.pageSize);
  }
};

// 新增按钮点击事件
const handleCreate = () => {
    ElMessage.info('新增功能待实现');
};

// 页面加载完成后立即获取数据
onMounted(() => {
  supplierStore.getSuppliers(); 
});
</script>

<style scoped>
.supplier-list-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.pagination-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 调整表格行高，使其更像 Excel */
:deep(.el-table .el-table__cell) {
  padding: 8px 0; /* 减小上下内边距 */
}
</style>