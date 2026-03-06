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
    productLoading: false,

    productTypeList: [] as BaseSelectVO[],
    productTypeTotal: 0,
    productTypeLoading: false,

    SupplierList: [] as BaseSelectVO[],
    SupplierTotal: 0,
    SupplierLoading: false,
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
    },
    async searchProductTypesAction(query: string) {
      // 注意：这里去掉了 if(!query) return，允许查询全部种类
      this.productTypeLoading = true;
      try {
        const data = await utilService.getProductTypeOptions(query);
        this.productTypeList = data.list;
        this.productTypeTotal = data.total;
      } finally {
        this.productTypeLoading = false;
      }
    },
    async searchSupplierAction(query: string) {
      // 注意：这里去掉了 if(!query) return，允许查询全部种类
      this.SupplierLoading = true;
      try {
        const data = await utilService.getSupplierOptions(query);
        this.SupplierList = data.list;
        this.SupplierTotal = data.total;
      } finally {
        this.SupplierLoading = false;
      }
    }
  }
});