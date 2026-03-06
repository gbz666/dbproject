import { purchaseInvoiceApi } from "@/api/purchaseInvoiceApi";
import type { PurchaseInvoiceDTO, PurchaseInvoiceQuery } from "@/types/dto";
import type { PurchaseInvoiceVO } from "@/types/vo";

export const purchaseInvoiceService = {
  // 分页查询
  async getPage(params: PurchaseInvoiceQuery) {
    return await purchaseInvoiceApi.fetchPage(params);
  },

  // 新增/修改
  async save(data: PurchaseInvoiceDTO): Promise<PurchaseInvoiceVO> {
    if (data.id) {
      return await purchaseInvoiceApi.update(data.id, data);
    }
    return await purchaseInvoiceApi.create(data);
  },

  // 删除
  async remove(id: number) {
    return await purchaseInvoiceApi.remove(id);
  },
};

