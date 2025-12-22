<template>
  <div class="app-container" style="padding: 20px; background-color: #f5f7fa; min-height: 100vh;">
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="store.queryParams">
        <el-form-item label="产品名称">
          <el-input v-model="store.queryParams.productName" placeholder="模糊搜索" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="产品编码">
          <el-input v-model="store.queryParams.productCode" placeholder="输入编码" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
          <el-button icon="Refresh" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table 
      v-loading="store.loading" 
      :data="store.inventoryList" 
      border 
      stripe 
      style="width: 100%"
      header-cell-class-name="table-header"
    >
      <el-table-column prop="productCode" label="产品编码" width="120" fixed="left" />
      <el-table-column prop="productName" label="产品名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="specification" label="规格型号" width="150" />
      <el-table-column prop="unit" label="单位" width="80" align="center" />
      
      <el-table-column label="库存分布 (实时)" align="center">
        <el-table-column prop="shInventory" label="上海仓" width="110" align="right">
          <template #default="{ row }">
            <span :class="getStockClass(row.shInventory)">{{ row.shInventory }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tjInventory" label="天津仓" width="110" align="right">
          <template #default="{ row }">
            <span :class="getStockClass(row.tjInventory)">{{ row.tjInventory }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="szInventory" label="深圳仓" width="110" align="right">
          <template #default="{ row }">
            <span :class="getStockClass(row.szInventory)">{{ row.szInventory }}</span>
          </template>
        </el-table-column>
      </el-table-column>

      <el-table-column prop="totalInventory" label="总库存" width="120" align="right" fixed="right">
        <template #default="{ row }">
          <b style="color: #409EFF">{{ row.totalInventory }}</b>
        </template>
      </el-table-column>
      
      <el-table-column prop="costPrice" label="参考成本" width="110" align="right" fixed="right" />
    </el-table>

    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="store.queryParams.pageNum"
        v-model:page-size="store.queryParams.pageSize"
        :total="store.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="store.loadInventoryData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useInventoryStore } from '@/stores/inventoryStore';

const store = useInventoryStore();

onMounted(() => {
  store.loadInventoryData();
});

const handleSearch = () => {
  store.queryParams.pageNum = 1;
  store.loadInventoryData();
};

const resetFilters = () => {
  store.resetParams();
};

// 根据库存量返回样式类（如库存为0显示灰色，充足显示绿色）
const getStockClass = (val: number) => {
  if (val <= 0) return 'stock-empty';
  if (val < 5) return 'stock-warning';
  return 'stock-normal';
};
</script>

<style scoped>
:deep(.table-header) {
  background-color: #f8f9fa !important;
  color: #333;
}

.stock-empty {
  color: #C0C4CC;
}

.stock-warning {
  color: #E6A23C;
  font-weight: bold;
}

.stock-normal {
  color: #67C23A;
}
</style>