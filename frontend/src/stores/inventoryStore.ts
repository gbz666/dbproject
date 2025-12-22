// @/stores/inventoryStore.ts
import { defineStore } from "pinia";
import { inventoryService } from "@/services/inventoryService";
import type { InventoryVO } from "@/types/vo";

export const useInventoryStore = defineStore("inventory", {
  state: () => ({
    inventoryList: [] as InventoryVO[],
    total: 0,
    loading: false,
    queryParams: {
      pageNum: 1,
      pageSize: 10,
      productName: "",
      productCode: ""
    }
  }),
  actions: {
    async loadInventoryData() {
      this.loading = true;
      try {
        const data = await inventoryService.fetchInventoryPage(this.queryParams);
        if (data) {
          this.inventoryList = data.list;
          this.total = data.total;
        }
      } finally {
        this.loading = false;
      }
    },
    resetParams() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        productName: "",
        productCode: ""
      };
      this.loadInventoryData();
    }
  }
});