// frontend/src/stores/receiptStore.ts
import { defineStore } from 'pinia';
import type { Receipt } from '@/types/models';
import { receiptService } from '@/service/receiptService';

export const useReceiptStore = defineStore('receipt', {
  state: () => ({
    receipts: [] as Receipt[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    getReceiptById: (state) => (id: number) => {
      return state.receipts.find(receipt => receipt.receipt_id === id);
    },
  },

  actions: {
    async fetchReceipts() {
      this.loading = true;
      this.error = null;
      try {
        this.receipts = await receiptService.getReceipts();
      } catch (error) {
        this.error = error instanceof Error ? error.message : '获取收款记录失败';
      } finally {
        this.loading = false;
      }
    },

    async createReceipt(receipt: Omit<Receipt, 'receipt_id'>) {
      try {
        const newReceipt = await receiptService.createReceipt(receipt);
        this.receipts.push(newReceipt);
        return newReceipt;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '创建收款记录失败';
        throw error;
      }
    },

    async updateReceipt(id: number, receipt: Partial<Receipt>) {
      try {
        const updatedReceipt = await receiptService.updateReceipt(id, receipt);
        const index = this.receipts.findIndex(r => r.receipt_id === id);
        if (index !== -1) {
          this.receipts[index] = { ...this.receipts[index], ...updatedReceipt };
        }
        return updatedReceipt;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '更新收款记录失败';
        throw error;
      }
    },

    // async deleteReceipt(id: number) {
    //   try {
    //     await receiptService.deleteReceipt(id);
    //     this.receipts = this.receipts.filter(receipt => receipt.receipt_id !== id);
    //   } catch (error) {
    //     this.error = error instanceof Error ? error.message : '删除收款记录失败';
    //     throw error;
    //   }
    // },
  },
});