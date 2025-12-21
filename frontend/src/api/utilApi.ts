import { httpClient } from "@/utils/httpClient"; // 假设你的封装在这个路径
import type { PageInfo } from "@/types/api"; // 建议定义一个通用的 PageInfo 类型
import type { BaseSelectVO } from "@/types/vo";
// 客户搜索分页接口
export const getCustomerPageApi = (pageNum: number, pageSize: number, keyword?: string) => {
  return httpClient<PageInfo<BaseSelectVO>>("/api/util/search/customer", {
    method: "GET",
    params: { pageNum, pageSize, keyword }
  });
};

// 产品搜索分页接口
export const getProductPageApi = (pageNum: number, pageSize: number, keyword?: string) => {
  return httpClient<PageInfo<BaseSelectVO>>("/api/util/search/product", {
    method: "GET",
    params: { pageNum, pageSize, keyword }
  });
};