// @/api/inventory.ts
import { httpClient } from "@/utils/httpClient";
import type { InventoryVO } from "@/types/vo";
import type { PageInfo } from "@/types/api"; // 假设你有通用的分页类型定义

export const getInventoryPageApi = (params: {
  pageNum: number;
  pageSize: number;
  productName?: string;
  productCode?: string;
}) => {
  return httpClient<PageInfo<InventoryVO>>("/api/inventory", {
    method: "GET",
    params,
  });
};