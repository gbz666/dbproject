// 文件名：frontend/src/api/customerApi.ts

import { httpClient } from "@/utils/httpClient";
import type { Customer } from "@/types/models";
// import type { ApiResult } from '@/types/api'; // ApiResult 不再直接用于 httpClient 的返回类型

export const customerApi = {
  /**
   * 获取所有客户 - 直接返回 Customer[]
   */
  getAll: () => httpClient<Customer[]>("/customers")
  /**
   * 根据ID获取客户 - 直接返回 Customer
   * @param id 客户ID
   */,
  getById: (id: number) => httpClient<Customer>(`/customers/${id}`)
  /**
   * 创建新客户 - 直接返回 Customer
   * @param customer 客户数据 (不包含 customer_id)
   */,
  create: (customer: Omit<Customer, "customer_id">) =>
    httpClient<Customer>("/customers", {
      method: "POST",
      body: customer,
    })
  /**
   * 更新客户 - 直接返回 Customer
   * @param id 客户ID
   * @param customer 客户部分更新数据
   */,
  update: (id: number, customer: Partial<Customer>) =>
    httpClient<Customer>(`/customers/${id}`, {
      method: "PUT",
      body: customer,
    })
  /**
   * 删除客户 - 返回 void (httpClient 泛型使用 void)
   * @param id 客户ID
   */,
  delete: (id: number) =>
    httpClient<void>(`/customers/${id}`, {
      method: "DELETE",
    }),
};
