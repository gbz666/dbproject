import { defineStore } from "pinia";
import { reactive, ref } from "vue";
import { salesInvoiceService } from "@/services/salesInvoiceService";
import type { SalesInvoiceDTO, SalesInvoiceQuery } from "@/types/dto";
import type { SalesInvoiceVO } from "@/types/vo";

export const useSalesInvoiceStore = defineStore("salesInvoice", () => {
  const list = ref<SalesInvoiceVO[]>([]);
  const total = ref(0);
  const loading = ref(false);

  const queryParams = reactive<SalesInvoiceQuery>({
    pageNum: 1,
    pageSize: 10,
    companyName: "",
    productModel: "",
    salesOrderCode: "",
    itemName: "",
    invoiceNo: "",
  });

  const fetchPageAction = async () => {
    loading.value = true;
    try {
      const page = await salesInvoiceService.getPage({ ...queryParams });
      list.value = page.list;
      total.value = page.total;
    } finally {
      loading.value = false;
    }
  };

  const submitInvoiceAction = async (dto: SalesInvoiceDTO) => {
    await salesInvoiceService.save(dto);
    await fetchPageAction();
  };

  const deleteInvoiceAction = async (id: number) => {
    await salesInvoiceService.remove(id);
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

