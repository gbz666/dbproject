import { defineStore } from "pinia";
import { reactive, ref } from "vue";
import { purchaseInvoiceService } from "@/services/purchaseInvoiceService";
import type { PurchaseInvoiceDTO, PurchaseInvoiceQuery } from "@/types/dto";
import type { PurchaseInvoiceVO } from "@/types/vo";

export const usePurchaseInvoiceStore = defineStore("purchaseInvoice", () => {
  const list = ref<PurchaseInvoiceVO[]>([]);
  const total = ref(0);
  const loading = ref(false);

  const queryParams = reactive<PurchaseInvoiceQuery>({
    pageNum: 1,
    pageSize: 10,
    supplierName: "",
    productModel: "",
    purchaseCode: "",
    itemName: "",
  });

  const fetchPageAction = async () => {
    loading.value = true;
    try {
      const page = await purchaseInvoiceService.getPage({ ...queryParams });
      list.value = page.list;
      total.value = page.total;
    } finally {
      loading.value = false;
    }
  };

  const submitInvoiceAction = async (dto: PurchaseInvoiceDTO) => {
    await purchaseInvoiceService.save(dto);
    await fetchPageAction();
  };

  const deleteInvoiceAction = async (id: number) => {
    await purchaseInvoiceService.remove(id);
    await fetchPageAction();
  };

  return {
    list,
    total,
    loading,
    queryParams,
    fetchPageAction,
    submitInvoiceAction,
    deleteInvoiceAction,
  };
});

