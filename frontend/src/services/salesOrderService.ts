import { salesOrderApi } from "@/api/salesOrderApi";
import type { SalesOrderDTO } from "@/types/dto";

export const salesOrderService = {
  // 分页查询逻辑，透传查询对象
  async getPage(params: any) {
    return await salesOrderApi.fetchPage(params);
  },

  // 综合保存逻辑
  async save(data: SalesOrderDTO, userId: number) {
    if (data.id) {
      return await salesOrderApi.update(data.id, data, userId);
    }
    return await salesOrderApi.create(data, userId);
  },

  // 删除逻辑
  async remove(id: number) {
    return await salesOrderApi.remove(id);
  }
};