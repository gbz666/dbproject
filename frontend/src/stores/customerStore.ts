// src/stores/customerStore.ts
import { defineStore } from 'pinia';
import type { Customer, CustomerDetailDTO } from '@/types/pojo';
import { customerService } from '@/service/customerService';
import type { PageInfo } from '@/types/api';
import { reactive, ref } from 'vue';

export const useCustomerStore = defineStore('customer', () => {
    // 状态 (State)
    const customers = ref<Customer[]>([]);
    const pageInfo = reactive<Partial<PageInfo<CustomerDetailDTO>>>({
        pageNum: 1,
        pageSize: 10,
        total: 0,
        pages: 1
    });
    const isLoading = ref(false);

    // 操作 (Actions)
    async function getCustomers(pageNum: number = pageInfo.pageNum!, pageSize: number = pageInfo.pageSize!) {
        isLoading.value = true;
        
        const result = await customerService.fetchCustomers(pageNum, pageSize);

        if (result) {
            customers.value = result.list;
            // 更新分页信息
            Object.assign(pageInfo, result);
        } else {
            // 清空列表，保持当前分页参数
            customers.value = [];
            pageInfo.total = 0;
        }
        
        isLoading.value = false;
    }

    // 暴露给外部的属性和方法
    return {
        customers,
        pageInfo,
        isLoading,
        getCustomers,
    };
});