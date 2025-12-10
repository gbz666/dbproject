// 文件名：frontend/src/services/customerService.ts

import { customerApi } from "@/api/customerApi";
import type { Customer } from "@/types/models";
// 引入我们定义的 ApiError 类型
import type { ApiError } from "@/utils/httpClient";

export class CustomerService {
  /**
   * 获取所有客户列表
   */
  async getCustomers(): Promise<Customer[]> {
    try {
      const customers = await customerApi.getAll();
      return customers;
    } catch (error) {
      // 捕获到 ApiError 结构的对象
      const apiError = error as ApiError;
      console.error(
        "Service 错误 (获取列表):",
        apiError.message,
        apiError.status
      );
      throw error;
    }
  }
  /**
   * 根据ID获取客户
   */

  async getCustomerById(id: number): Promise<Customer> {
    try {
      const customer = await customerApi.getById(id);
      return customer;
    } catch (error) {
      const apiError = error as ApiError;
      console.error(
        `Service 错误 (获取客户 ${id}):`,
        apiError.message,
        apiError.status
      ); // 示例：特定的业务逻辑错误处理
      if (apiError.status === 404) {
        // 抛出新的错误，UI 就可以显示这个新的 message
        throw new Error("客户不存在或已被删除");
      }
      throw error; // 抛出原始错误
    }
  } // ... (其他方法结构类似，只需将 catch 中的逻辑改为 throw error;)

  async createCustomer(
    customer: Omit<Customer, "customer_id">
  ): Promise<Customer> {
    try {
      return await customerApi.create(customer);
    } catch (error) {
      console.error("Service 错误 (创建客户):", (error as ApiError).message);
      throw error;
    }
  }

  async updateCustomer(
    id: number,
    customer: Partial<Customer>
  ): Promise<Customer> {
    try {
      return await customerApi.update(id, customer);
    } catch (error) {
      console.error("Service 错误 (更新客户):", (error as ApiError).message);
      throw error;
    }
  }

  async deleteCustomer(id: number): Promise<void> {
    try {
      await customerApi.delete(id);
    } catch (error) {
      console.error("Service 错误 (删除客户):", (error as ApiError).message);
      throw error;
    }
  }
}

export const customerService = new CustomerService();
