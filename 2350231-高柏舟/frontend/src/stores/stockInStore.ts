import { defineStore } from "pinia";
import { stockInService } from "@/services/stockInService";
import type { StockInVO } from "@/types/vo";
import type { StockInDTO } from "@/types/dto";

export const useStockInStore = defineStore("stockIn", {
  state: () => ({
    stockIns: [] as StockInVO[],
    total: 0,
    loading: false,
    queryParams: {
      pageNum: 1,
      pageSize: 10,
      purchaseOrderCode: "",
      supplierName: "",
      productName: "",
      serialNumber: ""
    }
  }),
  actions: {
    async loadPage() {
      this.loading = true;
      try {
        const data = await stockInService.getStockInPage(this.queryParams);
        this.stockIns = data.list;
        this.total = data.total;
      } finally {
        this.loading = false;
      }
    },

    async saveStockInAction(dto: StockInDTO, operatorId: number) {
      try {
        await stockInService.submitStockIn(dto, operatorId);
        await this.loadPage();
        return true;
      } catch (error) {
        throw error;
      }
    },

    async deleteItemAction(id: number, operatorId: number) {
      await stockInService.deleteStockIn(id, operatorId);
      await this.loadPage();
    }
  }
});