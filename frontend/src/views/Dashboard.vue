<template>
  <div class="dashboard">
    <h1>业务仪表板</h1>
    <div class="stats-grid">
      <div class="stat-card">
        <h3>总客户数</h3>
        <p class="stat-number">{{ customerCount }}</p>
      </div>
      <div class="stat-card">
        <h3>总供应商数</h3>
        <p class="stat-number">{{ supplierCount }}</p>
      </div>
      <div class="stat-card">
        <h3>总产品数</h3>
        <p class="stat-number">{{ productCount }}</p>
      </div>
      <div class="stat-card">
        <h3>本月销售额</h3>
        <p class="stat-number">¥{{ monthlySales }}</p>
      </div>
    </div>
    
    <div class="recent-activities">
      <h2>最近活动</h2>
      <div class="activity-list">
        <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
          <span class="activity-type">{{ activity.type }}</span>
          <span class="activity-desc">{{ activity.description }}</span>
          <span class="activity-time">{{ activity.time }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useCustomerStore } from '@/stores/customerStore'
import { useSupplierStore } from '@/stores/supplierStore'
import { useProductStore } from '@/stores/productStore'

const customerStore = useCustomerStore()
const supplierStore = useSupplierStore()
const productStore = useProductStore()

const customerCount = ref(0)
const supplierCount = ref(0)
const productCount = ref(0)
const monthlySales = ref(0)
const recentActivities = ref([])

onMounted(async () => {
  await Promise.all([
    customerStore.fetchCustomers(),
    supplierStore.fetchSuppliers(),
    productStore.fetchProducts()
  ])
  
  customerCount.value = customerStore.customers.length
  supplierCount.value = supplierStore.suppliers.length
  productCount.value = productStore.products.length
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  text-align: center;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #2c3e50;
  margin: 10px 0 0 0;
}

.recent-activities {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.activity-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-type {
  background: #e3f2fd;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
}
</style>