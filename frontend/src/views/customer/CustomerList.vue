<template>
  <div class="customer-list">
    <div class="page-header">
      <h1>客户管理</h1>
      <button @click="showCreateDialog = true" class="btn-primary">新增客户</button>
    </div>
    
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>客户ID</th>
            <th>客户名称</th>
            <th>销售人员</th>
            <th>跟进人员</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="customer in customerStore.customers" :key="customer.customer_id">
            <td>{{ customer.customer_id }}</td>
            <td>{{ customer.customer_name }}</td>
            <td>{{ customer.sales_person }}</td>
            <td>{{ customer.follow_up_person }}</td>
            <td>
              <button @click="editCustomer(customer)" class="btn-edit">编辑</button>
              <button @click="deleteCustomer(customer.customer_id)" class="btn-delete">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 创建/编辑对话框 -->
    <CustomerDialog 
      v-model="showCreateDialog"
      :customer="editingCustomer"
      @save="handleSaveCustomer"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useCustomerStore } from '@/stores/customerStore'
import type { Customer } from '@/types/models'
// import CustomerDialog from './components/CustomerDialog.vue'

const customerStore = useCustomerStore()
const showCreateDialog = ref(false)
const editingCustomer = ref<Customer | null>(null)

onMounted(() => {
  customerStore.fetchCustomers()
})

const editCustomer = (customer: Customer) => {
  editingCustomer.value = { ...customer }
  showCreateDialog.value = true
}

const deleteCustomer = async (id: number) => {
  if (confirm('确定要删除这个客户吗？')) {
    await customerStore.deleteCustomer(id)
  }
}

const handleSaveCustomer = async (customerData: Omit<Customer, 'customer_id'>) => {
  if (editingCustomer.value) {
    await customerStore.updateCustomer(editingCustomer.value.customer_id, customerData)
  } else {
    const newCustomerData = {
      ...customerData,
      create_at: new Date().toISOString() 
    }
    await customerStore.createCustomer(newCustomerData)
  }
  showCreateDialog.value = false
  editingCustomer.value = null
}
</script>