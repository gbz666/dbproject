// frontend/src/service/purchaseInvoiceService.ts

import { purchaseInvoiceApi } from "@/api/purchaseInvoiceApi";
import type { PurchaseInvoice } from "@/types/models";
import type { ApiError } from "@/utils/httpClient";

export class PurchaseInvoiceService {
  /**
   * 获取所有采购发票列表
   */
  async getPurchaseInvoices(): Promise<PurchaseInvoice[]> {
    try {
      const invoices = await purchaseInvoiceApi.getAll();
      return invoices;
    } catch (error) {
      const apiError = error as ApiError;
      console.error(
        "Service 错误 (获取采购发票列表):",
        apiError.message,
        apiError.status
      );
      throw error;
    }
  }

  /**
   * 根据ID获取采购发票
   */
  async getPurchaseInvoiceById(id: number): Promise<PurchaseInvoice> {
    try {
      const invoice = await purchaseInvoiceApi.getById(id);
      return invoice;
    } catch (error) {
      const apiError = error as ApiError;
      console.error(
        `Service 错误 (获取采购发票 ${id}):`,
        apiError.message,
        apiError.status
      );
      if (apiError.status === 404) {
        throw new Error("采购发票不存在或已被删除");
      }
      throw error;
    }
  }

  /**
   * 根据采购订单ID获取采购发票
   */
  async getPurchaseInvoicesByOrder(orderId: number): Promise<PurchaseInvoice[]> {
    try {
      const invoices = await purchaseInvoiceApi.getByPurchaseOrder(orderId);
      return invoices;
    } catch (error) {
      const apiError = error as ApiError;
      console.error(
        `Service 错误 (获取采购订单 ${orderId} 的发票):`,
        apiError.message,
        apiError.status
      );
      throw error;
    }
  }

  /**
   * 创建采购发票
   */
  async createPurchaseInvoice(
    invoice: Omit<PurchaseInvoice, "invoice_id" | "created_at">
  ): Promise<PurchaseInvoice> {
    try {
      return await purchaseInvoiceApi.create(invoice);
    } catch (error) {
      console.error(
        "Service 错误 (创建采购发票):",
        (error as ApiError).message
      );
      throw error;
    }
  }

  /**
   * 更新采购发票
   */
  async updatePurchaseInvoice(
    id: number,
    invoice: Partial<PurchaseInvoice>
  ): Promise<PurchaseInvoice> {
    try {
      return await purchaseInvoiceApi.update(id, invoice);
    } catch (error) {
      console.error(
        "Service 错误 (更新采购发票):",
        (error as ApiError).message
      );
      throw error;
    }
  }

  /**
   * 删除采购发票
   * 注意：API中没有删除方法，这里提供一个空实现
   */
  async deletePurchaseInvoice(id: number): Promise<void> {
    try {
      await purchaseInvoiceApi.delete(id);
    } catch (error) {
      console.error(
        "Service 错误 (删除采购发票):",
        (error as ApiError).message
      );
      throw error;
    }
  }
}

export const purchaseInvoiceService = new PurchaseInvoiceService();