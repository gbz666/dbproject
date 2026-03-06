import { httpClient } from "@/utils/httpClient";
import type { PurchaseOrderDto } from "@/types/dto";
import type { PurchaseOrderVO, PageInfo } from "@/types/vo";

export const purchaseOrderApi = {
  /** 分页查询采购订单 */
  fetchPage(params: {
    pageNum: number;
    pageSize: number;
    supplierCode?: string;
    supplierName?: string;
    productCode?: string;
    productName?: string;
    purchaseOrderCode?: string;
  }) {
    return httpClient<PageInfo<PurchaseOrderVO>>("/api/purchaseOrder/page", {
      method: "GET",
      params,
    });
  },

  /** 新增采购订单（后端参数名：currentStaffId） */
  create(dto: PurchaseOrderDto, currentStaffId: number) {
    return httpClient<string>("/api/purchaseOrder", {
      method: "POST",
      body: dto,
      params: { currentStaffId },
    });
  },

  /** 更新采购订单（后端参数名：currentStaffId） */
  update(id: number, dto: PurchaseOrderDto, currentStaffId: number) {
    return httpClient<string>(`/api/purchaseOrder/${id}`, {
      method: "PUT",
      body: dto,
      params: { currentStaffId },
    });
  },

  /** 删除采购订单（后端参数名：currentStaffId） */
  remove(id: number, currentStaffId: number) {
    return httpClient<string>(`/api/purchaseOrder/${id}`, {
      method: "DELETE",
      params: { currentStaffId },
    });
  },
};