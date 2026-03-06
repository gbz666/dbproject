import { httpClient } from "@/utils/httpClient";
import type { CustomerDetailVO, PageInfo } from "@/types/vo";
import type { CustomerCreateRequest, CustomerUpdateRequest } from "@/types/dto";

const BASE_PATH = "/api/customers";

export const customerApi = {
  // 分页获取客户
  fetchPage(params: { pageNum: number; pageSize: number; customerName?: string; customerCode?: string }) {
    return httpClient<PageInfo<CustomerDetailVO>>(BASE_PATH, {
      method: "GET",
      params
    });
  },

  // 创建客户 (currentStaffId 作为 query 参数)
  create(data: CustomerCreateRequest, currentStaffId: number) {
    return httpClient<CustomerDetailVO>(BASE_PATH, {
      method: "POST",
      body: data,
      params: { currentStaffId } // httpClient 会拼接到 URL
    });
  },

  // 更新客户
  update(customerCode: string, data: CustomerUpdateRequest, currentStaffId: number) {
    return httpClient<CustomerDetailVO>(`${BASE_PATH}/${customerCode}`, {
      method: "PUT",
      body: data,
      params: { currentStaffId }
    });
  },

  // 删除客户
  remove(customerCode: string, currentStaffId: number) {
    return httpClient<void>(`${BASE_PATH}/${customerCode}`, {
      method: "DELETE",
      params: { currentStaffId }
    });
  }
};