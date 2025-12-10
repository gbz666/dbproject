// frontend/src/stores/salesOrderStore.ts
import { defineStore } from 'pinia';
import type { SalesOrder } from '@/types/models';
import { salesOrderService } from '@/service/salesOrderService';

export const useSalesOrderStore = defineStore('salesOrder', {
  state: () => ({
    salesOrders: [] as SalesOrder[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    getSalesOrderById: (state) => (id: number) => {
      return state.salesOrders.find(order => order.order_id === id);
    },
  },

  actions: {
    async fetchSalesOrders() {
      this.loading = true;
      this.error = null;
      try {
        this.salesOrders = await salesOrderService.getSalesOrders();
      } catch (error) {
        this.error = error instanceof Error ? error.message : '获取销售订单列表失败';
      } finally {
        this.loading = false;
      }
    },

    async createSalesOrder(salesOrder: SalesOrder) {
      try {
        const newSalesOrder = await salesOrderService.createSalesOrder(salesOrder);
        this.salesOrders.push(newSalesOrder);
        return newSalesOrder;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '创建销售订单失败';
        throw error;
      }
    },

    async updateSalesOrder(id: number, salesOrder: Partial<SalesOrder>) {
      try {
        const updatedSalesOrder = await salesOrderService.updateSalesOrder(id, salesOrder);
        const index = this.salesOrders.findIndex(order => order.order_id === id);
        if (index !== -1) {
          this.salesOrders[index] = { ...this.salesOrders[index], ...updatedSalesOrder };
        }
        return updatedSalesOrder;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '更新销售订单失败';
        throw error;
      }
    },

    async deleteSalesOrder(id: number) {
      try {
        await salesOrderService.deleteSalesOrder(id);
        this.salesOrders = this.salesOrders.filter(order => order.order_id !== id);
      } catch (error) {
        this.error = error instanceof Error ? error.message : '删除销售订单失败';
        throw error;
      }
    },
  },
});