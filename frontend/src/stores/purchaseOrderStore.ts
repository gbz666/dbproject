import { defineStore } from "pinia";
import { reactive, ref } from "vue";
import type { PurchaseOrderVO } from "@/types/vo";
import { purchaseOrderService } from "@/services/purchaseOrderService";

export const usePurchaseStore = defineStore("purchaseOrder", () => {
  const orderList = ref<PurchaseOrderVO[]>([]);
  const total = ref(0);
  const loading = ref(false);

  // 严格对应 API 定义的参数名
  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    supplierCode: "",
    supplierName: "",
    productCode: "",
    productName: "",
    purchaseOrderCode: "", // 注意这里是对接 API 的字段名
  });

  const fetchPageAction = async () => {
    loading.value = true;
    try {
      const res = await purchaseOrderService.getPage({ ...queryParams });
      // 假设 PageInfo 的结构包含 list 和 total
      orderList.value = res.list; 
      total.value = res.total;
    } finally {
      loading.value = false;
    }
  };

  const submitOrderAction = async (dto: any, userId: number) => {
    const success = await purchaseOrderService.saveOrder(dto, userId);
    if (success) {
      await fetchPageAction();
    }
    return success;
  };

  const deleteOrderAction = async (id: number, userId: number) => {
    const success = await purchaseOrderService.deleteOrder(id, userId);
    if (success) {
      await fetchPageAction();
    }
    return success;
  };

  return {
    orderList,
    total,
    loading,
    queryParams,
    fetchPageAction,
    submitOrderAction,
    deleteOrderAction,
  };
});