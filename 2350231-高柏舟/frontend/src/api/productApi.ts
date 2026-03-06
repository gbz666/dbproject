import { httpClient } from "@/utils/httpClient";
import type { ProductVO, PageInfo } from "@/types/vo";
import type { ProductRequest, ProductQuery } from "@/types/dto";

export const productApi = {
  // 分页查询 [R]
  fetchProducts: (params: ProductQuery) => 
    httpClient<PageInfo<ProductVO>>("/api/products", { method: "GET", params }),

  // 获取详情 [R]
  getDetail: (productCode: string) => 
    httpClient<ProductVO>(`/api/products/${productCode}`, { method: "GET" }),

  // 创建产品 [C]
  create: (data: ProductRequest, staffId: number) => 
    httpClient<ProductVO>("/api/products", { 
      method: "POST", 
      body: data, 
      params: { currentStaffId: staffId } 
    }),

  // 更新产品 [U]
  update: (productCode: string, data: ProductRequest, staffId: number) => 
    httpClient<ProductVO>(`/api/products/${productCode}`, { 
      method: "PUT", 
      body: data, 
      params: { currentStaffId: staffId } 
    }),

  // 删除产品 [D]
  remove: (productCode: string) => 
    httpClient<void>(`/api/products/${productCode}`, { method: "DELETE" })
};