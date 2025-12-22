import { httpClient } from "@/utils/httpClient";
import type { OutboundOrderDTO } from "@/types/dto";
import type { OutboundDetailVO, PageInfo } from "@/types/vo";

const BASE_URL = "/api/outbound";

export const outboundApi = {
  // 分页查询：GET /api/outbound/page
  fetchPage: (params: any) => 
    httpClient<PageInfo<OutboundDetailVO>>(`${BASE_URL}/page`, { method: "GET", params }),

  // 创建出库单：POST /api/outbound/create?operatorId=xxx
  create: (data: OutboundOrderDTO, operatorId: number) =>
    httpClient<string>(`${BASE_URL}/create`, { 
      method: "POST", 
      body: data, 
      params: { operatorId } 
    }),

  // 修改出库单：PUT /api/outbound/update?operatorId=xxx
  update: (data: OutboundOrderDTO, operatorId: number) =>
    httpClient<string>(`${BASE_URL}/update`, { 
      method: "PUT", 
      body: data, 
      params: { operatorId } 
    }),

  // 删除出库单：DELETE /api/outbound/{id}?operatorId=xxx
  remove: (id: number, operatorId: number) =>
    httpClient<string>(`${BASE_URL}/${id}`, { 
      method: "DELETE", 
      params: { operatorId } 
    }),
};