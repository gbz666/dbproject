import { defineStore } from "pinia";
import { reactive, ref } from "vue";
import { paymentExpenseService } from "@/services/paymentExpenseService";
import type { PaymentExpenseDTO, PaymentExpenseQuery } from "@/types/dto";
import type { PaymentExpenseVO } from "@/types/vo";

export const usePaymentExpenseStore = defineStore("paymentExpense", () => {
  const list = ref<PaymentExpenseVO[]>([]);
  const total = ref(0);
  const loading = ref(false);

  const queryParams = reactive<PaymentExpenseQuery>({
    pageNum: 1,
    pageSize: 10,
    supplierName: "",
    paymentNo: "",
    method: "",
  });

  const fetchPageAction = async () => {
    loading.value = true;
    try {
      const page = await paymentExpenseService.getPage({ ...queryParams });
      list.value = page.list;
      total.value = page.total;
    } finally {
      loading.value = false;
    }
  };

  const submitAction = async (dto: PaymentExpenseDTO) => {
    await paymentExpenseService.save(dto);
    await fetchPageAction();
  };

  const deleteAction = async (id: number) => {
    await paymentExpenseService.remove(id);
    await fetchPageAction();
  };

  return { list, total, loading, queryParams, fetchPageAction, submitAction, deleteAction };
});
