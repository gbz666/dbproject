import { customerApi } from "@/api/customerApi";
import { ElMessage } from "element-plus";
import type { CustomerCreateRequest, CustomerUpdateRequest } from "@/types/dto";

export const customerService = {
  async getCustomerList(params: any) {
    return await customerApi.fetchPage(params);
  },

  async createCustomer(data: CustomerCreateRequest, staffId: number) {
    const res = await customerApi.create(data, staffId);
    ElMessage.success("客户创建成功");
    return res;
  },

  async updateCustomer(code: string, data: CustomerUpdateRequest, staffId: number) {
    const res = await customerApi.update(code, data, staffId);
    ElMessage.success("客户更新成功");
    return res;
  },

  async deleteCustomer(code: string, staffId: number) {
    await customerApi.remove(code, staffId);
    ElMessage.success("客户已删除");
  }
};