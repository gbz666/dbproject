import { defineStore } from "pinia";
import { ref, reactive } from "vue";
import { outboundService } from "@/services/outBoundService";
import type { OutboundDetailVO } from "@/types/vo";
import type { OutboundOrderDTO } from "@/types/dto";

export const useOutboundStore = defineStore("outbound", () => {
  const loading = ref(false);
  const outbounds = ref<OutboundDetailVO[]>([]);
  const total = ref(0);
  
  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10,
    salesOrderCode: "",
    customerName: "",
    productName: "",
    serialNumber: ""
  });

  const loadPage = async () => {
    loading.value = true;
    try {
      const res = await outboundService.getOutboundPage(queryParams);
      outbounds.value = res.list;
      total.value = res.total;
    } finally {
      loading.value = false;
    }
  };

  const saveOutboundAction = async (dto: OutboundOrderDTO, operatorId: number) => {
    loading.value = true;
    try {
      await outboundService.saveOutbound(dto, operatorId);
      await loadPage();
    } finally {
      loading.value = false;
    }
  };

  const deleteItem = async (id: number, operatorId: number) => {
    await outboundService.deleteOutbound(id, operatorId);
    await loadPage();
  };

  return { loading, outbounds, total, queryParams, loadPage, saveOutboundAction, deleteItem };
});