import { httpClient } from "@/utils/httpClient";
import type { SalesOrderDTO } from "@/types/dto";
import type { SalesOrderVO, PageInfo } from "@/types/vo";

const BASE_URL = "/api/salesOrder";

export const salesOrderApi = {
  // 分页查询
  fetchPage: (params: any) => 
    httpClient<PageInfo<SalesOrderVO>>(BASE_URL, { method: "GET", params }),

  // 创建订单
  create: (data: SalesOrderDTO, currentUserId: number) =>
    httpClient<string>(BASE_URL, { 
      method: "POST", 
      body: data, 
      params: { currentUserId } 
    }),

  // 修改订单
  update: (id: number, data: SalesOrderDTO, currentUserId: number) =>
    httpClient<string>(`${BASE_URL}/${id}`, { 
      method: "PUT", 
      body: data, 
      params: { currentUserId } 
    }),

  // 删除订单
  remove: (id: number) =>
    httpClient<string>(`${BASE_URL}/${id}`, { method: "DELETE" }),
};