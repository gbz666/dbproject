import { defineStore } from 'pinia';
import { utilService } from '@/services/utilService';
import type { BaseSelectVO } from '@/types/vo';

export const useUtilStore = defineStore('util', {
  state: () => ({
    // 客户相关状态
    customerList: [] as BaseSelectVO[],
    customerTotal: 0,
    customerLoading: false,

    // 产品相关状态
    productList: [] as BaseSelectVO[],
    productTotal: 0,
    productLoading: false
  }),
  actions: {
    // 搜索客户
    async searchCustomersAction(query: string) {
      if (!query) return;
      this.customerLoading = true;
      try {
        const data = await utilService.getCustomerOptions(query);
        this.customerList = data.list;
        this.customerTotal = data.total;
      } finally {
        this.customerLoading = false;
      }
    },

    // 搜索产品
    async searchProductsAction(query: string) {
      if (!query) return;
      this.productLoading = true;
      try {
        const data = await utilService.getProductOptions(query);
        this.productList = data.list;
        this.productTotal = data.total;
      } finally {
        this.productLoading = false;
      }
    }
  }
});