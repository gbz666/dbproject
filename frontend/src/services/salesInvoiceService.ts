import { salesInvoiceApi } from "@/api/salesInvoiceApi";
import type { SalesInvoiceDTO, SalesInvoiceQuery } from "@/types/dto";
import type { SalesInvoiceVO } from "@/types/vo";

export const salesInvoiceService = {
  // 分页查询
  async getPage(params: SalesInvoiceQuery) {
    return await salesInvoiceApi.fetchPage(params);
  },

  // 新增/修改
  async save(data: SalesInvoiceDTO): Promise<SalesInvoiceVO> {
    if (data.id) {
      return await salesInvoiceApi.update(data.id, data);
    }
    return await salesInvoiceApi.create(data);
  },

  // 删除
  async remove(id: number) {
    return await salesInvoiceApi.remove(id);
  },
};

