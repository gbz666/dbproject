import { httpClient } from "@/utils/httpClient";
import type { PaymentReceiptDTO, PaymentReceiptQuery } from "@/types/dto";
import type { PaymentReceiptVO, PageInfo } from "@/types/vo";

const BASE_URL = "/api/payment-receipts";

export const paymentReceiptApi = {
  fetchPage: (params: PaymentReceiptQuery) =>
    httpClient<PageInfo<PaymentReceiptVO>>(`${BASE_URL}/page`, {
      method: "GET",
      params,
    }),

  create: (data: PaymentReceiptDTO) =>
    httpClient<void>(BASE_URL, {
      method: "POST",
      body: data,
    }),

  update: (id: number, data: PaymentReceiptDTO) =>
    httpClient<void>(`${BASE_URL}/${id}`, {
      method: "PUT",
      body: data,
    }),

  remove: (id: number) =>
    httpClient<void>(`${BASE_URL}/${id}`, {
      method: "DELETE",
    }),
};
