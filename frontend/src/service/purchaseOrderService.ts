// frontend/src/service/purchaseOrderService.ts

import { purchaseOrderApi } from '@/api/purchaseOrderApi';
import type { PurchaseOrder } from '@/types/models';
// 引入 ApiError 类型，以便在 Service 层处理结构化错误
// 假设 ApiError 定义在 /src/utils/httpClient.ts 中
import type { ApiError } from '@/utils/httpClient'; 

/**
 * 采购订单服务 (PurchaseOrderService)
 * 封装了所有与采购订单相关的业务逻辑和 API 调用。
 * 目标：保留 ApiError 结构，不丢弃后端的错误信息。
 */
export class PurchaseOrderService {
  /**
   * 辅助函数：统一处理并记录 API 错误
   * @param operationName 发生错误的操作名称（用于日志）
   * @param error 捕获到的错误对象
   * @returns 抛出 ApiError
   */
  private handleError(operationName: string, error: unknown): never {
    // 假设上游 (purchaseOrderApi) 抛出的就是 ApiError 类型
    const apiError = error as ApiError;
    
    // 记录错误日志，包含更多细节
    console.error(`${operationName} 失败。状态码: ${apiError.status || 'N/A'}, 详情:`, apiError.data || apiError.message);
    
    // 重新抛出结构化的 ApiError，确保调用者能获取到状态码和数据体
    throw apiError; 
  }

  async getPurchaseOrders(): Promise<PurchaseOrder[]> {
    try {
      return await purchaseOrderApi.getAll();
    } catch (error) {
      this.handleError('获取采购订单列表', error);
    }
  }

  async getPurchaseOrderById(id: number): Promise<PurchaseOrder> {
    try {
      return await purchaseOrderApi.getById(id);
    } catch (error) {
      this.handleError(`获取采购订单信息 (ID: ${id})`, error);
    }
  }

  async createPurchaseOrder(order: Omit<PurchaseOrder, 'order_id' | 'created_at'>): Promise<PurchaseOrder> {
    try {
      return await purchaseOrderApi.create(order);
    } catch (error) {
      this.handleError('创建采购订单', error);
    }
  }

  async updatePurchaseOrder(id: number, order: Partial<PurchaseOrder>): Promise<PurchaseOrder> {
    try {
      return await purchaseOrderApi.update(id, order);
    } catch (error) {
      this.handleError(`更新采购订单 (ID: ${id})`, error);
    }
  }

  async deletePurchaseOrder(id: number): Promise<void> {
    try {
      await purchaseOrderApi.delete(id);
    } catch (error) {
      this.handleError(`删除采购订单 (ID: ${id})`, error);
    }
  }

  async getPurchaseOrdersBySupplier(supplierId: number): Promise<PurchaseOrder[]> {
    try {
      return await purchaseOrderApi.getBySupplier(supplierId);
    } catch (error) {
      this.handleError(`获取供应商采购订单 (Supplier ID: ${supplierId})`, error);
    }
  }
}

export const purchaseOrderService = new PurchaseOrderService();