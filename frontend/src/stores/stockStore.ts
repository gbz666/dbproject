// frontend/src/stores/stockStore.ts
import { defineStore } from 'pinia';
import type { StockIn, StockOut } from '@/types/models';
import { stockService } from '@/service/stockService';

export const useStockStore = defineStore('stock', {
  state: () => ({
    stockIns: [] as StockIn[],
    stockOuts: [] as StockOut[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    getStockInById: (state) => (id: number) => {
      return state.stockIns.find(stockIn => stockIn.stock_in_id === id);
    },
    getStockOutById: (state) => (id: number) => {
      return state.stockOuts.find(stockOut => stockOut.stock_out_id === id);
    },
  },

  actions: {
    async fetchStockIns() {
      this.loading = true;
      this.error = null;
      try {
        this.stockIns = await stockService.getStockInRecords();
      } catch (error) {
        this.error = error instanceof Error ? error.message : '获取入库记录失败';
      } finally {
        this.loading = false;
      }
    },

    async fetchStockOuts() {
      this.loading = true;
      this.error = null;
      try {
        this.stockOuts = await stockService.getStockOutRecords();
      } catch (error) {
        this.error = error instanceof Error ? error.message : '获取出库记录失败';
      } finally {
        this.loading = false;
      }
    },

    async createStockIn(stockIn: Omit<StockIn, 'stock_in_id'>) {
      try {
        const newStockIn = await stockService.createStockInRecord(stockIn);
        this.stockIns.push(newStockIn);
        return newStockIn;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '创建入库记录失败';
        throw error;
      }
    },

    async createStockOut(stockOut: Omit<StockOut, 'stock_out_id'>) {
      try {
        const newStockOut = await stockService.createStockOutRecord(stockOut);
        this.stockOuts.push(newStockOut);
        return newStockOut;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '创建出库记录失败';
        throw error;
      }
    },

    async updateStockIn(id: number, stockIn: Partial<StockIn>) {
      try {
        const updatedStockIn = await stockService.updateStockInRecord(id, stockIn);
        const index = this.stockIns.findIndex(s => s.stock_in_id === id);
        if (index !== -1) {
          this.stockIns[index] = { ...this.stockIns[index], ...updatedStockIn };
        }
        return updatedStockIn;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '更新入库记录失败';
        throw error;
      }
    },

    async updateStockOut(id: number, stockOut: Partial<StockOut>) {
      try {
        const updatedStockOut = await stockService.updateStockOutRecord(id, stockOut);
        const index = this.stockOuts.findIndex(s => s.stock_out_id === id);
        if (index !== -1) {
          this.stockOuts[index] = { ...this.stockOuts[index], ...updatedStockOut };
        }
        return updatedStockOut;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '更新出库记录失败';
        throw error;
      }
    },

    // async deleteStockIn(id: number) {
    //   try {
    //     await stockService.deleteStockIn(id);
    //     this.stockIns = this.stockIns.filter(stockIn => stockIn.stock_in_id !== id);
    //   } catch (error) {
    //     this.error = error instanceof Error ? error.message : '删除入库记录失败';
    //     throw error;
    //   }
    // },

    // async deleteStockOut(id: number) {
    //   try {
    //     await stockService.deleteStockOutR(id);
    //     this.stockOuts = this.stockOuts.filter(stockOut => stockOut.stock_out_id !== id);
    //   } catch (error) {
    //     this.error = error instanceof Error ? error.message : '删除出库记录失败';
    //     throw error;
    //   }
    // },
  },
});