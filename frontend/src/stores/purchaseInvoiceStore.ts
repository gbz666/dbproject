// frontend/src/stores/purchaseInvoiceStore.ts
import { defineStore } from 'pinia';
import type { PurchaseInvoice } from '@/types/models';
import { purchaseInvoiceService } from '@/service/purchaseInvoiceService';

export const usePurchaseInvoiceStore = defineStore('purchaseInvoice', {
  state: () => ({
    purchaseInvoices: [] as PurchaseInvoice[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    getPurchaseInvoiceById: (state) => (id: number) => {
      return state.purchaseInvoices.find(invoice => invoice.invoice_id === id);
    },
  },

  actions: {
    async fetchPurchaseInvoices() {
      this.loading = true;
      this.error = null;
      try {
        this.purchaseInvoices = await purchaseInvoiceService.getPurchaseInvoices();
      } catch (error) {
        this.error = error instanceof Error ? error.message : '获取采购发票列表失败';
      } finally {
        this.loading = false;
      }
    },

    async createPurchaseInvoice(purchaseInvoice: Omit<PurchaseInvoice, 'invoice_id'>) {
      try {
        const newPurchaseInvoice = await purchaseInvoiceService.createPurchaseInvoice(purchaseInvoice);
        this.purchaseInvoices.push(newPurchaseInvoice);
        return newPurchaseInvoice;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '创建采购发票失败';
        throw error;
      }
    },

    async updatePurchaseInvoice(id: number, purchaseInvoice: Partial<PurchaseInvoice>) {
      try {
        const updatedPurchaseInvoice = await purchaseInvoiceService.updatePurchaseInvoice(id, purchaseInvoice);
        const index = this.purchaseInvoices.findIndex(invoice => invoice.invoice_id === id);
        if (index !== -1) {
          this.purchaseInvoices[index] = { ...this.purchaseInvoices[index], ...updatedPurchaseInvoice };
        }
        return updatedPurchaseInvoice;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '更新采购发票失败';
        throw error;
      }
    },

    async deletePurchaseInvoice(id: number) {
      try {
        await purchaseInvoiceService.deletePurchaseInvoice(id);
        this.purchaseInvoices = this.purchaseInvoices.filter(invoice => invoice.invoice_id !== id);
      } catch (error) {
        this.error = error instanceof Error ? error.message : '删除采购发票失败';
        throw error;
      }
    },
  },
});