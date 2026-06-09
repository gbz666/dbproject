import { defineStore } from "pinia";
import { reactive, ref } from "vue";
import { paymentReceiptService } from "@/services/paymentReceiptService";
import type { PaymentReceiptDTO, PaymentReceiptQuery } from "@/types/dto";
import type { PaymentReceiptVO } from "@/types/vo";

export const usePaymentReceiptStore = defineStore("paymentReceipt", () => {
  const list = ref<PaymentReceiptVO[]>([]);
  const total = ref(0);
  const loading = ref(false);

  const queryParams = reactive<PaymentReceiptQuery>({
    pageNum: 1,
    pageSize: 10,
    customerName: "",
    receiptNo: "",
    method: "",
  });

  const fetchPageAction = async () => {
    loading.value = true;
    try {
      const page = await paymentReceiptService.getPage({ ...queryParams });
      list.value = page.list;
      total.value = page.total;
    } finally {
      loading.value = false;
    }
  };

  const submitAction = async (dto: PaymentReceiptDTO) => {
    await paymentReceiptService.save(dto);
    await fetchPageAction();
  };

  const deleteAction = async (id: number) => {
    await paymentReceiptService.remove(id);
    await fetchPageAction();
  };

  return { list, total, loading, queryParams, fetchPageAction, submitAction, deleteAction };
});
