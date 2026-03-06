import { salesOrderApi } from "@/api/salesOrderApi";
import type { ApiResult } from "@/types/api";
import type { SalesOrderDTO } from "@/types/dto";

export const salesOrderService = {
  // 分页查询逻辑，透传查询对象
  async getPage(params: any) {
    return await salesOrderApi.fetchPage(params);
  },

  // 综合保存逻辑
  async save(data: SalesOrderDTO, userId: number) {
    try {
      let result: string; // 根据你的 api 定义，create/update 返回的是 Promise<string>

      if (data.id) {
        // 如果 update 失败，这里会直接 throw，不会赋值给 result
        result = await salesOrderApi.update(data.id, data, userId);
      } else {
        result = await salesOrderApi.create(data, userId);
      }
      return result;
    } catch (error: any) {

      throw error;
    }
  },

  // 删除逻辑
  async remove(id: number) {
    return await salesOrderApi.remove(id);
  },
};
