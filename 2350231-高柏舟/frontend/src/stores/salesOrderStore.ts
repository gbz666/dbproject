import { defineStore } from "pinia";
import { ref, reactive } from "vue";
import { salesOrderService } from "@/services/salesOrderService";
import type { SalesOrderVO } from "@/types/vo";
import type { SalesOrderDTO } from "@/types/dto";

export const useSalesStore = defineStore("sales", () => {
  const orderList = ref<SalesOrderVO[]>([]);
  const total = ref(0);
  const loading = ref(false);

  // 严格匹配你要求的 5 个查询参数
  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    customerName: "",
    customerCode: "",
    productName: "",
    productCode: "",
    salesOrderCode: ""
  });

  // Action: 由 Store 发起 Service 请求并更新状态
  const fetchPageAction = async () => {
    loading.value = true;
    try {
      // Store 调用 Service
      const res = await salesOrderService.getPage({ ...queryParams });
      orderList.value = res.list;
      total.value = res.total;
    } finally {
      loading.value = false;
    }
  };

  // Action: 创建或更新 (透传 currentUserId)
  const submitOrderAction = async (dto: SalesOrderDTO, userId: number) => {
    await salesOrderService.save(dto, userId);
    await fetchPageAction(); // 操作完刷新列表
  };

  // Action: 删除
  const deleteOrderAction = async (id: number) => {
    await salesOrderService.remove(id);
    await fetchPageAction();
  };

  return { 
    orderList, total, loading, queryParams, 
    fetchPageAction, submitOrderAction, deleteOrderAction 
  };
});