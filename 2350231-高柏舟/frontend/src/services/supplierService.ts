import { supplierApi } from "@/api/supplierApi";
import type { SupplierRequest } from "@/types/dto";

export const supplierService = {
  /** 获取分页列表 */
  async fetchPage(pageNum: number, pageSize: number, filters: any) {
    return await supplierApi.list({ pageNum, pageSize, ...filters });
  },

  /** 保存供应商 (根据是否有 code 判断是新增还是更新) */
  async saveSupplier(data: SupplierRequest, currentStaffId: number, code?: string) {
    if (code) {
      return await supplierApi.update(code, data, currentStaffId);
    }
    return await supplierApi.create(data, currentStaffId);
  },

  /** 删除 */
  async removeSupplier(code: string, currentStaffId: number) {
    return await supplierApi.delete(code, currentStaffId);
  }
};