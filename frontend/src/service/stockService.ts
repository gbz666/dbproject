// frontend/src/service/stockService.ts

import { stockApi } from "@/api/stockApi";
import type { StockIn, StockOut } from "@/types/models";
// 引入 ApiError 类型，以便在 Service 层处理结构化错误
// 假设 ApiError 定义在 /src/utils/httpClient.ts 中
import type { ApiError } from "@/utils/httpClient";

/**
 * 库存服务 (StockService)
 * 封装了所有与入库、出库、库存查询相关的业务逻辑和 API 调用。
 * 目标：保留 ApiError 结构，不丢弃后端的错误信息。
 */
export class StockService {
  /**
   * 辅助函数：统一处理并记录 API 错误
   * @param operationName 发生错误的操作名称（用于日志）
   * @param error 捕获到的错误对象
   * @returns 抛出 ApiError
   */
  private handleError(operationName: string, error: unknown): never {
    // 假设上游 (stockApi) 抛出的就是 ApiError 类型
    const apiError = error as ApiError;

    // 记录错误日志，包含更多细节
    console.error(
      `${operationName} 失败。状态码: ${apiError.status || "N/A"}, 详情:`,
      apiError.data || apiError.message
    );

    // 重新抛出结构化的 ApiError，确保调用者能获取到状态码和数据体
    throw apiError;
  }

  // --- 入库相关 ---
  async getStockInRecords(): Promise<StockIn[]> {
    try {
      return await stockApi.stockIn.getAll();
    } catch (error) {
      this.handleError("获取入库记录", error);
    }
  }

  async createStockInRecord(
    stockIn: Omit<StockIn, "stock_in_id" | "created_at">
  ): Promise<StockIn> {
    try {
      return await stockApi.stockIn.create(stockIn);
    } catch (error) {
      this.handleError("创建入库记录", error);
    }
  }

  async updateStockInRecord(
    id: number,
    stockIn: Partial<StockIn>
  ): Promise<StockIn> {
    try {
      return await stockApi.stockIn.update(id, stockIn);
    } catch (error) {
      this.handleError(`更新入库记录 (ID: ${id})`, error);
    }
  }

  // --- 出库相关 ---
  async getStockOutRecords(): Promise<StockOut[]> {
    try {
      return await stockApi.stockOut.getAll();
    } catch (error) {
      this.handleError("获取出库记录", error);
    }
  }

  async createStockOutRecord(
    stockOut: Omit<StockOut, "stock_out_id" | "created_at">
  ): Promise<StockOut> {
    try {
      return await stockApi.stockOut.create(stockOut);
    } catch (error) {
      this.handleError("创建出库记录", error);
    }
  }

  async updateStockOutRecord(
    id: number,
    stockOut: Partial<StockOut>
  ): Promise<StockOut> {
    try {
      return await stockApi.stockOut.update(id, stockOut);
    } catch (error) {
      this.handleError(`更新出库记录 (ID: ${id})`, error);
    }
  }

  // --- 库存查询 ---
  async getStockSummary(productId?: string): Promise<{
    totalIn: number;
    totalOut: number;
    currentStock: number;
    byWarehouse: {
      shanghai: number;
      tianjin: number;
      shenzhen: number;
    };
  }> {
    try {
      // ⚠️ 假设这里最终会调用一个 API 或依赖其他 Service
      // 如果调用 API 失败，会将错误抛到这里

      // 替换掉原来的模拟实现
      const summaryResult = await stockApi.getSummary(productId);

      return summaryResult;
    } catch (error) {
      // ⚠️ 假设库存查询 API 返回 ApiError
      if ((error as ApiError).status !== undefined) {
        this.handleError("获取库存汇总", error);
      }

      // 如果是客户端逻辑错误，则抛出通用错误
      console.error("获取库存汇总 (客户端逻辑或未识别错误) 失败:", error);
      throw new Error("获取库存汇总失败");
    }
  }
  async deleteStockInRecord(id: number): Promise<void> {
    try {
      await stockApi.stockIn.delete(id);
    } catch (error) {
      this.handleError(`删除入库记录 (ID: ${id})`, error);
    }
  }

  async deleteStockOutRecord(id: number): Promise<void> {
    try {
      await stockApi.stockOut.delete(id);
    } catch (error) {
      this.handleError(`删除出库记录 (ID: ${id})`, error);
    }
  }
}

export const stockService = new StockService();
