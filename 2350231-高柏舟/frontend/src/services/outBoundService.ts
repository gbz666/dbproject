import { outboundApi } from "@/api/outboundApi";
import type { OutboundOrderDTO } from "@/types/dto";

export const outboundService = {
  async getOutboundPage(query: any) {
    return await outboundApi.fetchPage(query);
  },

  async saveOutbound(data: OutboundOrderDTO, operatorId: number) {
    // 业务逻辑检查：确保出库数量大于 0
    data.items.forEach(item => {
      const totalQty = item.warehouseDetails.reduce((sum, w) => sum + (w.quantity || 0), 0);
      if (totalQty <= 0) throw new Error(`产品 ${item.productCode} 的出库总数必须大于0`);
    });

    if (data.id) {
      return await outboundApi.update(data, operatorId);
    }
    return await outboundApi.create(data, operatorId);
  },

  async deleteOutbound(id: number, operatorId: number) {
    return await outboundApi.remove(id, operatorId);
  }
};