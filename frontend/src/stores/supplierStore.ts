// frontend/src/stores/supplierStore.ts
import { defineStore } from 'pinia';
import type { Supplier } from '@/types/models';
import { supplierService } from '@/service/supplierService';

export const useSupplierStore = defineStore('supplier', {
  state: () => ({
    suppliers: [] as Supplier[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    getSupplierById: (state) => (id: number) => {
      return state.suppliers.find(supplier => supplier.supplier_id === id);
    },
  },

  actions: {
    async fetchSuppliers() {
      this.loading = true;
      this.error = null;
      try {
        this.suppliers = await supplierService.getSuppliers();
      } catch (error) {
        this.error = error instanceof Error ? error.message : '获取供应商列表失败';
      } finally {
        this.loading = false;
      }
    },

    async createSupplier(supplier: Omit<Supplier, 'supplier_id'>) {
      try {
        const newSupplier = await supplierService.createSupplier(supplier);
        this.suppliers.push(newSupplier);
        return newSupplier;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '创建供应商失败';
        throw error;
      }
    },

    async updateSupplier(id: number, supplier: Partial<Supplier>) {
      try {
        const updatedSupplier = await supplierService.updateSupplier(id, supplier);
        const index = this.suppliers.findIndex(s => s.supplier_id === id);
        if (index !== -1) {
          this.suppliers[index] = { ...this.suppliers[index], ...updatedSupplier };
        }
        return updatedSupplier;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '更新供应商失败';
        throw error;
      }
    },

    async deleteSupplier(id: number) {
      try {
        await supplierService.deleteSupplier(id);
        this.suppliers = this.suppliers.filter(supplier => supplier.supplier_id !== id);
      } catch (error) {
        this.error = error instanceof Error ? error.message : '删除供应商失败';
        throw error;
      }
    },
  },
});