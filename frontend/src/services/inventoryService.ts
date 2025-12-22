// @/services/inventoryService.ts
import * as inventoryApi from "@/api/inventoryApi";

export const inventoryService = {
  async fetchInventoryPage(query: any) {
    try {
      // 可以在此处对查询参数进行格式化
      return await inventoryApi.getInventoryPageApi(query);
    } catch (error) {
      throw error;
    }
  }
};