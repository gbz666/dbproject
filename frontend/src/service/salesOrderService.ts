// frontend/src/service/salesOrderService.ts

import { salesOrderApi } from '@/api/salesOrderApi';
import type { SalesOrder } from '@/types/models';
// 引入 ApiError 类型，以便在 Service 层处理结构化错误
// 假设 ApiError 定义在 /src/utils/httpClient.ts 中
import type { ApiError } from '@/utils/httpClient'; 

/**
 * 销售订单服务 (SalesOrderService)
 * 封装了所有与销售订单相关的业务逻辑和 API 调用。
 * 目标：保留 ApiError 结构，不丢弃后端的错误信息。
 */
export class SalesOrderService {
  /**
   * 辅助函数：统一处理并记录 API 错误
   * @param operationName 发生错误的操作名称（用于日志）
   * @param error 捕获到的错误对象
   * @returns 抛出 ApiError
   */
  private handleError(operationName: string, error: unknown): never {
    // 假设上游 (salesOrderApi) 抛出的就是 ApiError 类型
    const apiError = error as ApiError;
    
    // 记录错误日志，包含更多细节
    console.error(`${operationName} 失败。状态码: ${apiError.status || 'N/A'}, 详情:`, apiError.data || apiError.message);
    
    // 重新抛出结构化的 ApiError，确保调用者能获取到状态码和数据体
    throw apiError; 
  }

  async getSalesOrders(): Promise<SalesOrder[]> {
    try {
      return await salesOrderApi.getAll();
    } catch (error) {
      // ⚠️ 将通用 Error 替换为结构化错误处理
      this.handleError('获取销售订单列表', error);
    }
  }

  async getSalesOrderById(id: number): Promise<SalesOrder> {
    try {
      return await salesOrderApi.getById(id);
    } catch (error) {
      this.handleError(`获取销售订单信息 (ID: ${id})`, error);
    }
  }

  async createSalesOrder(order: Omit<SalesOrder, 'order_id' | 'created_at'>): Promise<SalesOrder> {
    try {
      return await salesOrderApi.create(order);
    } catch (error) {
      this.handleError('创建销售订单', error);
    }
  }

  async updateSalesOrder(id: number, order: Partial<SalesOrder>): Promise<SalesOrder> {
    try {
      return await salesOrderApi.update(id, order);
    } catch (error) {
      this.handleError(`更新销售订单 (ID: ${id})`, error);
    }
  }

  async deleteSalesOrder(id: number): Promise<void> {
    try {
      await salesOrderApi.delete(id);
    } catch (error) {
      this.handleError(`删除销售订单 (ID: ${id})`, error);
    }
  }
}

export const salesOrderService = new SalesOrderService();