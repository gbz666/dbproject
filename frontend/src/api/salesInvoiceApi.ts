import { httpClient } from "@/utils/httpClient";
import type { SalesInvoiceDTO, SalesInvoiceQuery } from "@/types/dto";
import type { SalesInvoiceVO, PageInfo } from "@/types/vo";

const BASE_URL = "/api/sales-invoices";

export const salesInvoiceApi = {
  // 分页查询销项发票
  fetchPage: (params: SalesInvoiceQuery) =>
    httpClient<PageInfo<SalesInvoiceVO>>(`${BASE_URL}/page`, {
      method: "GET",
      params,
    }),

  // 新增销项发票
  create: (data: SalesInvoiceDTO) =>
    httpClient<SalesInvoiceVO>(BASE_URL, {
      method: "POST",
      body: data,
    }),

  // 修改销项发票
  update: (id: number, data: SalesInvoiceDTO) =>
    httpClient<SalesInvoiceVO>(`${BASE_URL}/${id}`, {
      method: "PUT",
      body: data,
    }),

  // 根据 ID 查询单个销项发票（含一票多明细）
  getById: (id: number) =>
    httpClient<SalesInvoiceVO>(`${BASE_URL}/${id}`, {
      method: "GET",
    }),

  // 删除销项发票
  remove: (id: number) =>
    httpClient<void>(`${BASE_URL}/${id}`, {
      method: "DELETE",
    }),
};

