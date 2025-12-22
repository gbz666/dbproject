import { stockInApi } from "@/api/stockInApi";
import type { StockInDTO } from "@/types/dto";
import type { StockInVO } from "@/types/vo";
import type { PageInfo } from "@/types/api";

export const stockInService = {
  /**
   * 格式化并提交入库单
   */
  async submitStockIn(data: StockInDTO, operatorId: number): Promise<string> {
    const payload: StockInDTO = {
      ...data,
      // 确保日期格式符合后端 @JsonFormat 要求
      stockInDate: data.stockInDate.toString().split('T')[0] as any
    };

    return payload.id 
      ? await stockInApi.update(payload, operatorId)
      : await stockInApi.create(payload, operatorId);
  },

  async getStockInPage(params: any): Promise<PageInfo<StockInVO>> {
    return await stockInApi.getPage(params);
  },

  async deleteStockIn(id: number, operatorId: number): Promise<string> {
    return await stockInApi.delete(id, operatorId);
  }
};