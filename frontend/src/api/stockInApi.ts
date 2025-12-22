import { httpClient } from "@/utils/httpClient";
import type { StockInDTO } from "@/types/dto";
import type { StockInVO } from "@/types/vo";
import type { PageInfo } from "@/types/api";

export const stockInApi = {
  create: (dto: StockInDTO, operatorId: number) => 
    httpClient<string>("/api/stock-in/create", { 
      method: "POST", body: dto, params: { operatorId } 
    }),

  update: (dto: StockInDTO, operatorId: number) => 
    httpClient<string>("/api/stock-in/update", { 
      method: "PUT", body: dto, params: { operatorId } 
    }),

  getPage: (params: Record<string, any>) => 
    httpClient<PageInfo<StockInVO>>("/api/stock-in/page", { 
      method: "GET", params 
    }),

  delete: (id: number, operatorId: number) => 
    httpClient<string>(`/api/stock-in/${id}`, { 
      method: "DELETE", params: { operatorId } 
    }),
};