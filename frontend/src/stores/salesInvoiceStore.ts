// frontend/src/stores/salesInvoiceStore.ts
import { defineStore } from 'pinia';
import type { SalesInvoice } from '@/types/models';
import { salesInvoiceService } from '@/service/salesInvoiceService';

export const useSalesInvoiceStore = defineStore('salesInvoice', {
  state: () => ({
    salesInvoices: [] as SalesInvoice[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    getSalesInvoiceById: (state) => (id: number) => {
      return state.salesInvoices.find(invoice => invoice.invoice_id === id);
    },
  },

  actions: {
    async fetchSalesInvoices() {
      this.loading = true;
      this.error = null;
      try {
        this.salesInvoices = await salesInvoiceService.getSalesInvoices();
      } catch (error) {
        this.error = error instanceof Error ? error.message : '获取销售发票列表失败';
      } finally {
        this.loading = false;
      }
    },

    async createSalesInvoice(salesInvoice: Omit<SalesInvoice, 'invoice_id'>) {
      try {
        const newSalesInvoice = await salesInvoiceService.createSalesInvoice(salesInvoice);
        this.salesInvoices.push(newSalesInvoice);
        return newSalesInvoice;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '创建销售发票失败';
        throw error;
      }
    },

    async updateSalesInvoice(id: number, salesInvoice: Partial<SalesInvoice>) {
      try {
        const updatedSalesInvoice = await salesInvoiceService.updateSalesInvoice(id, salesInvoice);
        const index = this.salesInvoices.findIndex(invoice => invoice.invoice_id === id);
        if (index !== -1) {
          this.salesInvoices[index] = { ...this.salesInvoices[index], ...updatedSalesInvoice };
        }
        return updatedSalesInvoice;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '更新销售发票失败';
        throw error;
      }
    },

    // async deleteSalesInvoice(id: number) {
    //   try {
    //     await salesInvoiceService.deleteSalesInvoice(id);
    //     this.salesInvoices = this.salesInvoices.filter(invoice => invoice.invoice_id !== id);
    //   } catch (error) {
    //     this.error = error instanceof Error ? error.message : '删除销售发票失败';
    //     throw error;
    //   }
    // },
  },
});