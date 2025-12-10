// frontend/src/services/payment.service.ts

import { paymentApi } from "@/api/paymentApi";
import type { Payment } from "@/types/models";
// ⚠️ 关键修改 1: 引入 ApiError 类型，该类型定义在 /src/utils/httpClient.ts 中
import type { ApiError } from "@/utils/httpClient";

/**
 * @interface PaymentCreationData
 * 创建支付记录所需的数据类型，与 API 层保持一致。
 */
export type PaymentCreationData = Omit<Payment, "payment_id" | "created_at">;

/**
 * 支付服务 (PaymentService)
 * 封装了所有与支付相关的业务逻辑和 API 调用。
 * 目标：确保当 API 调用失败时，向上层抛出结构化的 ApiError。
 */
export const paymentService = {
  /**
   * 获取所有支付记录
   * @returns 支付记录数组 Promise<Payment[]>
   */
  async fetchAllPayments(): Promise<Payment[]> {
    try {
      const payments = await paymentApi.getAll();
      return payments;
    } catch (error) {
      // ⚠️ 关键修改 2: 明确断言并记录 error，然后重新抛出
      const apiError = error as ApiError;
      console.error(
        "获取所有支付记录失败:",
        apiError.message,
        apiError.status,
        apiError.data
      );
      // httpClient 已经确保抛出的是 ApiError 结构，因此我们直接重新抛出它
      throw apiError;
    }
  },

  /**
   * 根据 ID 获取单个支付记录详情
   * @param id 支付记录ID
   * @returns 单个支付记录 Promise<Payment>
   */
  async getPaymentDetails(id: number): Promise<Payment> {
    try {
      const payment = await paymentApi.getById(id);
      return payment;
    } catch (error) {
      const apiError = error as ApiError;
      console.error(`获取支付记录 (ID: ${id}) 失败:`, apiError);
      throw apiError;
    }
  },

  /**
   * 根据采购订单 ID 获取所有相关的支付记录
   * @param orderId 采购订单ID (string)
   * @returns 支付记录数组 Promise<Payment[]>
   */
  async getPaymentsByOrderId(orderId: number): Promise<Payment[]> {
    try {
      const payments = await paymentApi.getByPurchaseOrder(orderId);
      return payments;
    } catch (error) {
      const apiError = error as ApiError;
      console.error(`获取订单 (${orderId}) 支付记录失败:`, apiError);
      throw apiError;
    }
  },

  /**
   * 创建新的支付记录
   * @param paymentData 支付创建数据
   * @returns 新创建的支付记录 Promise<Payment>
   */
  async createNewPayment(paymentData: PaymentCreationData): Promise<Payment> {
    try {
      const newPayment = await paymentApi.create(paymentData);
      return newPayment;
    } catch (error) {
      const apiError = error as ApiError;
      console.error("创建支付记录失败:", apiError);
      throw apiError;
    }
  },

  /**
   * 更新现有支付记录（部分更新）
   * @param id 支付记录ID
   * @param updateData 需要更新的数据
   * @returns 更新后的支付记录 Promise<Payment>
   */
  async updatePayment(
    id: number,
    updateData: Partial<Payment>
  ): Promise<Payment> {
    try {
      const updatedPayment = await paymentApi.update(id, updateData);
      return updatedPayment;
    } catch (error) {
      const apiError = error as ApiError;
      console.error(`更新支付记录 (ID: ${id}) 失败:`, apiError);
      throw apiError;
    }
  },

  /**
   * 获取指定订单的已支付总金额
   * @param orderId 采购订单ID
   * @returns 已支付总金额 Promise<number>
   */
  async getOrderTotalPaid(orderId: number): Promise<number> {
    try {
      const result = await paymentApi.getTotalPaid(orderId);
      return result.total_paid;
    } catch (error) {
      const apiError = error as ApiError;
      console.error(`获取订单 (${orderId}) 已支付总额失败:`, apiError);
      throw apiError;
    }
  },
  async deletePayment(id: number): Promise<void> {
    try {
      await paymentApi.delete(id);
    } catch (error) {
      const apiError = error as ApiError;
      console.error(`删除支付记录 (ID: ${id}) 失败:`, apiError);
      throw apiError;
    }
  },
};

// 导出类型，方便其他模块使用
export default paymentService;
