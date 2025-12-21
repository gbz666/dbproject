import { defineStore } from 'pinia';
import { ref, reactive } from 'vue';
import { customerService } from '@/services/customerService';
import type { CustomerDetailVO } from '@/types/vo';
import type { CustomerCreateRequest, CustomerUpdateRequest } from '@/types/dto';

export const useCustomerStore = defineStore('customer', () => {
  const customers = ref<CustomerDetailVO[]>([]);
  const total = ref(0);
  const loading = ref(false);

  // 查询参数
  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    customerName: '',
    customerCode: ''
  });

  // --- Actions ---

  // 1. 查询列表
  const fetchList = async () => {
    loading.value = true;
    try {
      const data = await customerService.getCustomerList(queryParams);
      customers.value = data.list;
      total.value = data.total;
    } finally {
      loading.value = false;
    }
  };

  // 2. 新增客户
  const addCustomer = async (data: CustomerCreateRequest, staffId: number) => {
    await customerService.createCustomer(data, staffId);
    await fetchList(); // 操作成功后自动刷新列表
  };

  // 3. 修改客户
  const editCustomer = async (code: string, data: CustomerUpdateRequest, staffId: number) => {
    await customerService.updateCustomer(code, data, staffId);
    await fetchList(); // 自动刷新
  };

  // 4. 删除客户
  const removeCustomer = async (code: string, staffId: number) => {
    await customerService.deleteCustomer(code, staffId);
    await fetchList(); // 自动刷新
  };

  // 翻页
  const setPage = (page: number) => {
    queryParams.pageNum = page;
    fetchList();
  };

  return { 
    customers, total, loading, queryParams, 
    fetchList, addCustomer, editCustomer, removeCustomer, setPage 
  };
});