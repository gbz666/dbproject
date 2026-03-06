import { defineStore } from 'pinia';
import { ref, reactive } from 'vue';
import { supplierService } from '@/services/supplierService';
import type { SupplierDetailVO, PageInfo } from '@/types/vo';
import type { SupplierRequest } from '@/types/dto';

export const useSupplierStore = defineStore('supplier', () => {
  // 1. State
  const list = ref<SupplierDetailVO[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const pagination = reactive({
    pageNum: 1,
    pageSize: 10
  });
  const searchFilters = reactive({
    supplierCode: '',
    supplierName: ''
  });

  // 2. Actions (业务动作)
  
  /** 加载数据 */
  const loadData = async () => {
    loading.value = true;
    try {
      const res = await supplierService.fetchPage(
        pagination.pageNum, 
        pagination.pageSize, 
        searchFilters
      );
      if (res) {
        list.value = res.list;
        total.value = res.total;
      }
    } finally {
      loading.value = false;
    }
  };

  /** 处理新增或更新 */
  const submitForm = async (formData: SupplierRequest, staffId: number, code?: string) => {
    await supplierService.saveSupplier(formData, staffId, code);
    await loadData(); // 刷新列表
  };

  /** 处理删除 */
  const deleteByCode = async (code: string, staffId: number) => {
    await supplierService.removeSupplier(code, staffId);
    await loadData(); // 刷新列表
  };

  /** 分页切换回调 */
  const onPageChange = (page: number) => {
    pagination.pageNum = page;
    loadData();
  };

  return {
    list, total, loading, pagination, searchFilters,
    loadData, submitForm, deleteByCode, onPageChange
  };
});