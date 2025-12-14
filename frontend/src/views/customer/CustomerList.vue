<template>
  <div class="customer-list-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>客户列表</span>
          <el-button type="primary" @click="handleCreate">新增客户</el-button>
        </div>
      </template>

      <el-table 
        :data="customerStore.customers" 
        v-loading="customerStore.isLoading"
        border 
        stripe 
        style="width: 100%"
        max-height="650"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="customerCode" label="客户编号" width="120" sortable />
        <el-table-column prop="customerName" label="客户名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
        
        <el-table-column prop="salesPersonName" label="负责销售" width="100" />
        <el-table-column prop="followUpPersonName" label="跟进人员" width="100" />
        <el-table-column prop="ownerName" label="业绩" width="100" />
        
        <el-table-column prop="paymentTermsDays" label="账期(天)" width="100" align="center" />
        <el-table-column prop="paymentTermsNotes" label="账期备注" min-width="120" show-overflow-tooltip />
        
        <el-table-column prop="createdByName" label="创建人" width="100" />
        
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ row.createdAt ? new Date(row.createdAt).toLocaleString() : '-' }}
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
          :current-page="customerStore.pageInfo.pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="customerStore.pageInfo.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="customerStore.pageInfo.total"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useCustomerStore } from '@/stores/customerStore';
import { ElMessage } from 'element-plus';

// 使用 Pinia store
const customerStore = useCustomerStore();

// --- 组合式 API 核心逻辑 (保持不变) ---

const handleSizeChange = (val: number) => {
  if (customerStore.pageInfo.pageSize !== val) {
    customerStore.pageInfo.pageSize = val;
    customerStore.getCustomers(1, val);
  }
};

const handleCurrentChange = (val: number) => {
  if (customerStore.pageInfo.pageNum !== val) {
    customerStore.pageInfo.pageNum = val;
    customerStore.getCustomers(val, customerStore.pageInfo.pageSize);
  }
};

const handleCreate = () => {
    ElMessage.info('新增功能待实现');
};

onMounted(() => {
  customerStore.getCustomers(); 
});
</script>

<style scoped>
/* 样式保持不变 */
.customer-list-page {
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