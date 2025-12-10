// frontend/src/service/salesInvoiceService.ts
import { salesInvoiceApi } from "@/api/salesInvoiceApi";
import type { SalesInvoice } from "@/types/models";

export class SalesInvoiceService {
  async getSalesInvoices(): Promise<SalesInvoice[]> {
    try {
      return await salesInvoiceApi.getAll();
    } catch (error) {
      throw new Error("获取销项发票列表失败");
    }
  }

  async getSalesInvoiceById(id: number): Promise<SalesInvoice> {
    try {
      return await salesInvoiceApi.getById(id);
    } catch (error) {
      throw new Error("获取销项发票信息失败");
    }
  }

  async getSalesInvoicesByOrder(orderId: number): Promise<SalesInvoice[]> {
    try {
      return await salesInvoiceApi.getBySalesOrder(orderId);
    } catch (error) {
      throw new Error("获取订单销项发票失败");
    }
  }

  async createSalesInvoice(
    invoice: Omit<SalesInvoice, "invoice_id" | "created_at">
  ): Promise<SalesInvoice> {
    try {
      return await salesInvoiceApi.create(invoice);
    } catch (error) {
      throw new Error("创建销项发票失败");
    }
  }

  async updateSalesInvoice(
    id: number,
    invoice: Partial<SalesInvoice>
  ): Promise<SalesInvoice> {
    try {
      return await salesInvoiceApi.update(id, invoice);
    } catch (error) {
      throw new Error("更新销项发票失败");
    }
  }

  async updateInvoiceStatus(
    id: number,
    status: SalesInvoice["status"]
  ): Promise<SalesInvoice> {
    try {
      return await salesInvoiceApi.updateStatus(id, status);
    } catch (error) {
      throw new Error("更新发票状态失败");
    }
  }

  // 发票统计
  async getInvoiceSummary(params?: { startDate?: string; endDate?: string }) {
    try {
      const invoices = await this.getSalesInvoices();
      const filteredInvoices = invoices.filter((invoice) => {
        if (!params?.startDate && !params?.endDate) return true;
        const invoiceDate = new Date(invoice.invoice_date);
        if (params.startDate && invoiceDate < new Date(params.startDate))
          return false;
        if (params.endDate && invoiceDate > new Date(params.endDate))
          return false;
        return true;
      });

      const totalAmount = filteredInvoices.reduce(
        (sum, invoice) => sum + invoice.total_amount_tax_included,
        0
      );
      const totalTax = filteredInvoices.reduce(
        (sum, invoice) => sum + invoice.tax_amount,
        0
      );

      return {
        totalInvoices: filteredInvoices.length,
        totalAmount,
        totalTax,
        averageAmount: totalAmount / filteredInvoices.length || 0,
      };
    } catch (error) {
      throw new Error("获取发票统计失败");
    }
  }
  async deleteSalesInvoice(id: number): Promise<void> {
    try {
      await salesInvoiceApi.delete(id);
    } catch (error) {
      throw new Error("删除销项发票失败");
    }
  }
}

export const salesInvoiceService = new SalesInvoiceService();
