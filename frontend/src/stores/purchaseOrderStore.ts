// frontend/src/stores/purchaseOrderStore.ts
import { defineStore } from 'pinia';
import type { PurchaseOrder } from '@/types/models';
import { purchaseOrderService } from '@/service/purchaseOrderService';

export const usePurchaseOrderStore = defineStore('purchaseOrder', {
  state: () => ({
    purchaseOrders: [] as PurchaseOrder[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    getPurchaseOrderById: (state) => (id: number) => {
      return state.purchaseOrders.find(order => order.order_id === id);
    },
  },

  actions: {
    async fetchPurchaseOrders() {
      this.loading = true;
      this.error = null;
      try {
        this.purchaseOrders = await purchaseOrderService.getPurchaseOrders();
      } catch (error) {
        this.error = error instanceof Error ? error.message : '获取采购订单列表失败';
      } finally {
        this.loading = false;
      }
    },

    async createPurchaseOrder(purchaseOrder: Omit<PurchaseOrder, 'order_id'>) {
      try {
        const newPurchaseOrder = await purchaseOrderService.createPurchaseOrder(purchaseOrder);
        this.purchaseOrders.push(newPurchaseOrder);
        return newPurchaseOrder;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '创建采购订单失败';
        throw error;
      }
    },

    async updatePurchaseOrder(id: number, purchaseOrder: Partial<PurchaseOrder>) {
      try {
        const updatedPurchaseOrder = await purchaseOrderService.updatePurchaseOrder(id, purchaseOrder);
        const index = this.purchaseOrders.findIndex(order => order.order_id === id);
        if (index !== -1) {
          this.purchaseOrders[index] = { ...this.purchaseOrders[index], ...updatedPurchaseOrder };
        }
        return updatedPurchaseOrder;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '更新采购订单失败';
        throw error;
      }
    },

    async deletePurchaseOrder(id: number) {
      try {
        await purchaseOrderService.deletePurchaseOrder(id);
        this.purchaseOrders = this.purchaseOrders.filter(order => order.order_id !== id);
      } catch (error) {
        this.error = error instanceof Error ? error.message : '删除采购订单失败';
        throw error;
      }
    },
  },
});