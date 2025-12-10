// frontend/src/service/receiptService.ts

import { receiptApi } from "@/api/receiptApi";
import type { Receipt } from "@/types/models";
// 引入 ApiError 类型，以便在 Service 层处理结构化错误
// 假设 ApiError 定义在 /src/utils/httpClient.ts 中
import type { ApiError } from "@/utils/httpClient";

/**
 * 收款服务 (ReceiptService)
 * 封装了所有与收款相关的业务逻辑和 API 调用。
 * 目标：保留 ApiError 结构，不丢弃后端的错误信息。
 */
export class ReceiptService {
  /**
   * 辅助函数：统一处理并记录 API 错误
   * @param operationName 发生错误的操作名称（用于日志）
   * @param error 捕获到的错误对象
   * @returns 抛出 ApiError
   */
  private handleError(operationName: string, error: unknown): never {
    // 假设上游 (receiptApi) 抛出的就是 ApiError 类型
    const apiError = error as ApiError;

    // 记录错误日志，包含更多细节
    console.error(
      `${operationName} 失败。状态码: ${apiError.status || "N/A"}, 详情:`,
      apiError.data || apiError.message
    );

    // 重新抛出结构化的 ApiError，确保调用者能获取到状态码和数据体
    throw apiError;
  }

  async getReceipts(): Promise<Receipt[]> {
    try {
      return await receiptApi.getAll();
    } catch (error) {
      // ⚠️ 将通用 Error 替换为结构化错误处理
      this.handleError("获取收款记录", error);
    }
  }

  async getReceiptById(id: number): Promise<Receipt> {
    try {
      return await receiptApi.getById(id);
    } catch (error) {
      this.handleError(`获取收款信息 (ID: ${id})`, error);
    }
  }

  async getReceiptsByOrder(orderId: number): Promise<Receipt[]> {
    try {
      return await receiptApi.getBySalesOrder(orderId);
    } catch (error) {
      this.handleError(`获取订单 (${orderId}) 收款记录`, error);
    }
  }

  async createReceipt(
    receipt: Omit<Receipt, "receipt_id" | "created_at">
  ): Promise<Receipt> {
    try {
      return await receiptApi.create(receipt);
    } catch (error) {
      this.handleError("创建收款记录", error);
    }
  }

  async updateReceipt(id: number, receipt: Partial<Receipt>): Promise<Receipt> {
    try {
      return await receiptApi.update(id, receipt);
    } catch (error) {
      this.handleError(`更新收款记录 (ID: ${id})`, error);
    }
  }

  async getTotalReceived(orderId: number): Promise<number> {
    try {
      const response = await receiptApi.getTotalReceived(orderId);
      // ⚠️ 注意：这里假设 getTotalReceived API 返回 { total_received: number } 的 ApiResult 结构，
      // httpClient 负责剥离 data，所以 response 已经是 { total_received: number }。
      return response.total_received;
    } catch (error) {
      this.handleError(`获取订单 (${orderId}) 已收款总额`, error);
    }
  }

  /**
   * 收款统计 (此方法主要执行客户端业务逻辑，错误处理也应遵循抛出 ApiError 的原则)
   */
  async getReceiptSummary(params?: { startDate?: string; endDate?: string }) {
    try {
      // 1. 调用依赖的方法 (getReceipts)，如果它失败，会抛出 ApiError
      const receipts = await this.getReceipts();

      // 2. 客户端业务逻辑
      const filteredReceipts = receipts.filter((receipt) => {
        if (!params?.startDate && !params?.endDate) return true;
        const receiptDate = new Date(receipt.receipt_date);

        // 确保日期过滤逻辑正确
        if (params!.startDate && receiptDate < new Date(params!.startDate))
          return false;
        if (params!.endDate && receiptDate > new Date(params!.endDate))
          return false;
        return true;
      });

      const totalAmount = filteredReceipts.reduce(
        (sum, receipt) => sum + receipt.amount,
        0
      );
      const byPaymentMethod = filteredReceipts.reduce((acc, receipt) => {
        acc[receipt.payment_method] =
          (acc[receipt.payment_method] || 0) + receipt.amount;
        return acc;
      }, {} as Record<string, number>);

      return {
        totalReceipts: filteredReceipts.length,
        totalAmount,
        byPaymentMethod,
      };
    } catch (error) {
      // 如果是 ApiError，我们希望它继续向上抛。
      // 如果是 Date 转换等运行时错误，则需要确保其被捕获和记录。
      if ((error as ApiError).status !== undefined) {
        // 如果是 ApiError，直接重新抛出
        throw error;
      }

      // 对于纯客户端逻辑（如 Date 转换）发生的错误，我们记录并使用一个通用的 Error 结构
      console.error("获取收款统计（客户端逻辑）失败:", error);
      throw new Error("获取收款统计失败");
    }
  }
  async deleteReceipt(id: number): Promise<void> {
    try {
      await receiptApi.delete(id);
    } catch (error) {
      this.handleError(`删除收款记录 (ID: ${id})`, error);
    }
  }
}

export const receiptService = new ReceiptService();
