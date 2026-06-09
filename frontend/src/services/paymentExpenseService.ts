import { paymentExpenseApi } from "@/api/paymentExpenseApi";
import type { PaymentExpenseDTO, PaymentExpenseQuery } from "@/types/dto";

export const paymentExpenseService = {
  async getPage(params: PaymentExpenseQuery) {
    return await paymentExpenseApi.fetchPage(params);
  },

  async save(data: PaymentExpenseDTO) {
    if (data.id) {
      return await paymentExpenseApi.update(data.id, data);
    }
    return await paymentExpenseApi.create(data);
  },

  async remove(id: number) {
    return await paymentExpenseApi.remove(id);
  },
};
