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

  /** 新增采购订单 */
  create(dto: PurchaseOrderDto, currentUserId: number) {
    return httpClient<string>("/api/purchaseOrder", {
      method: "POST",
      body: dto,
      params: { currentUserId },
    });
  },

  /** 更新采购订单 */
  update(id: number, dto: PurchaseOrderDto, currentUserId: number) {
    return httpClient<string>(`/api/purchaseOrder/${id}`, {
      method: "PUT",
      body: dto,
      params: { currentUserId },
    });
  },

  /** 删除采购订单 */
  remove(id: number, currentUserId: number) {
    return httpClient<string>(`/api/purchaseOrder/${id}`, {
      method: "DELETE",
      params: { currentUserId },
    });
  },
};