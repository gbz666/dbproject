import { httpClient } from "@/utils/httpClient";
import type { PaymentExpenseDTO, PaymentExpenseQuery } from "@/types/dto";
import type { PaymentExpenseVO, PageInfo } from "@/types/vo";

const BASE_URL = "/api/payment-expenses";

export const paymentExpenseApi = {
  fetchPage: (params: PaymentExpenseQuery) =>
    httpClient<PageInfo<PaymentExpenseVO>>(`${BASE_URL}/page`, {
      method: "GET",
      params,
    }),

  create: (data: PaymentExpenseDTO) =>
    httpClient<void>(BASE_URL, {
      method: "POST",
      body: data,
    }),

  update: (id: number, data: PaymentExpenseDTO) =>
    httpClient<void>(`${BASE_URL}/${id}`, {
      method: "PUT",
      body: data,
    }),

  remove: (id: number) =>
    httpClient<void>(`${BASE_URL}/${id}`, {
      method: "DELETE",
    }),
};
