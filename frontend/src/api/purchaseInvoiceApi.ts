import { httpClient } from "@/utils/httpClient";
import type { PurchaseInvoiceDTO, PurchaseInvoiceQuery } from "@/types/dto";
import type { PurchaseInvoiceVO, PageInfo } from "@/types/vo";

const BASE_URL = "/api/purchase-invoices";

export const purchaseInvoiceApi = {
  // 分页查询进项发票
  fetchPage: (params: PurchaseInvoiceQuery) =>
    httpClient<PageInfo<PurchaseInvoiceVO>>(`${BASE_URL}/page`, {
      method: "GET",
      params,
    }),

  // 按 ID 查询进项发票（含完整明细，用于编辑）
  getById: (id: number) =>
    httpClient<PurchaseInvoiceVO>(`${BASE_URL}/${id}`, {
      method: "GET",
    }),

  // 新增进项发票
  create: (data: PurchaseInvoiceDTO) =>
    httpClient<PurchaseInvoiceVO>(BASE_URL, {
      method: "POST",
      body: data,
    }),

  // 修改进项发票
  update: (id: number, data: PurchaseInvoiceDTO) =>
    httpClient<PurchaseInvoiceVO>(`${BASE_URL}/${id}`, {
      method: "PUT",
      body: data,
    }),

  // 删除进项发票
  remove: (id: number) =>
    httpClient<void>(`${BASE_URL}/${id}`, {
      method: "DELETE",
    }),
};

