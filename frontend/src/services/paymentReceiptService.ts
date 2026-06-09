import { paymentReceiptApi } from "@/api/paymentReceiptApi";
import type { PaymentReceiptDTO, PaymentReceiptQuery } from "@/types/dto";

export const paymentReceiptService = {
  async getPage(params: PaymentReceiptQuery) {
    return await paymentReceiptApi.fetchPage(params);
  },

  async save(data: PaymentReceiptDTO) {
    if (data.id) {
      return await paymentReceiptApi.update(data.id, data);
    }
    return await paymentReceiptApi.create(data);
  },

  async remove(id: number) {
    return await paymentReceiptApi.remove(id);
  },
};
