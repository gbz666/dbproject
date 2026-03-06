import { httpClient } from "@/utils/httpClient";
import type { SalesOrderDTO } from "@/types/dto";
import type { SalesOrderVO, PageInfo } from "@/types/vo";

const BASE_URL = "/api/salesOrder";

export const salesOrderApi = {
  // 分页查询
  fetchPage: (params: any) => 
    httpClient<PageInfo<SalesOrderVO>>(BASE_URL, { method: "GET", params }),

  // 创建订单（后端参数名：currentStaffId）
  create: (data: SalesOrderDTO, currentStaffId: number) =>
    httpClient<string>(BASE_URL, { 
      method: "POST", 
      body: data, 
      params: { currentStaffId } 
    }),

  // 修改订单（后端参数名：currentStaffId）
  update: (id: number, data: SalesOrderDTO, currentStaffId: number) =>
    httpClient<string>(`${BASE_URL}/${id}`, { 
      method: "PUT", 
      body: data, 
      params: { currentStaffId } 
    }),

  // 删除订单
  remove: (id: number) =>
    httpClient<string>(`${BASE_URL}/${id}`, { method: "DELETE" }),
};