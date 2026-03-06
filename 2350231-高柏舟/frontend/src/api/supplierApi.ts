import { httpClient } from "@/utils/httpClient";
import type { SupplierRequest } from "@/types/dto";
import type { SupplierDetailVO, PageInfo } from "@/types/vo";

const BASE_URL = "/api/suppliers";

export const supplierApi = {
  /** 分页获取供应商 */
  list: (params: { pageNum: number; pageSize: number; supplierCode?: string; supplierName?: string }) =>
    httpClient<PageInfo<SupplierDetailVO>>(BASE_URL, { method: "GET", params }),

  /** 新增供应商 */
  create: (data: SupplierRequest, currentStaffId: number) =>
    httpClient<any>(BASE_URL, { 
      method: "POST", 
      body: data, 
      params: { currentStaffId } // 对应后端 @RequestParam
    }),

  /** 更新供应商 */
  update: (code: string, data: SupplierRequest, currentStaffId: number) =>
    httpClient<SupplierDetailVO>(`${BASE_URL}/${code}`, { 
      method: "PUT", 
      body: data, 
      params: { currentStaffId } 
    }),

  /** 删除供应商 (软删除) */
  delete: (code: string, currentStaffId: number) =>
    httpClient<void>(`${BASE_URL}/${code}`, { 
      method: "DELETE", 
      params: { currentStaffId } 
    }),
};