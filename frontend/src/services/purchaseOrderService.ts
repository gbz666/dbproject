import { purchaseOrderApi } from "@/api/purchaseOrderApi";
import type { PurchaseOrderDto } from "@/types/dto";
import { ElMessage } from "element-plus";

export const purchaseOrderService = {
  /** * 获取分页数据
   * 对接 api.fetchPage
   */
  async getPage(params: any) {
    // httpClient 返回的是 Promise<PageInfo<PurchaseOrderVO>>
    return await purchaseOrderApi.fetchPage(params);
  },

  /** * 保存订单
   * 自动识别是调用 create 还是 update
   */
  async saveOrder(dto: PurchaseOrderDto, currentUserId: number) {
    try {
      if (dto.id) {
        // 对接 api.update(id, dto, userId)
        await purchaseOrderApi.update(dto.id, dto, currentUserId);
        ElMessage.success("修改成功");
      } else {
        // 对接 api.create(dto, userId)
        await purchaseOrderApi.create(dto, currentUserId);
        ElMessage.success("创建成功");
      }
      return true;
    } catch (error: any) {
      // 错误已经在 httpClient 或此处捕获
      return false;
    }
  },

  /** * 删除订单
   * 对接 api.remove
   */
  async deleteOrder(id: number, currentUserId: number) {
    try {
      await purchaseOrderApi.remove(id, currentUserId);
      ElMessage.success("删除成功");
      return true;
    } catch (error: any) {
      return false;
    }
  }
};