// src/stores/supplierStore.ts
import { defineStore } from 'pinia';
import type { Supplier } from '@/types/pojo';
import { supplierService } from '@/service/supplierService';
import type { PageInfo } from '@/types/api';
import { reactive, ref } from 'vue';

export const useSupplierStore = defineStore('supplier', () => {
    // 状态 (State)
    const suppliers = ref<Supplier[]>([]);
    const pageInfo = reactive<Partial<PageInfo<Supplier>>>({
        pageNum: 1,
        pageSize: 10,
        total: 0,
        pages: 1
    });
    const isLoading = ref(false);

    // 操作 (Actions)
    async function getSuppliers(pageNum: number = pageInfo.pageNum!, pageSize: number = pageInfo.pageSize!) {
        isLoading.value = true;
        
        const result = await supplierService.fetchSuppliers(pageNum, pageSize);

        if (result) {
            suppliers.value = result.list;
            // 更新分页信息
            Object.assign(pageInfo, result);
        } else {
            suppliers.value = [];
            pageInfo.total = 0;
        }
        
        isLoading.value = false;
    }

    // 暴露给外部的属性和方法
    return {
        suppliers,
        pageInfo,
        isLoading,
        getSuppliers,
    };
});