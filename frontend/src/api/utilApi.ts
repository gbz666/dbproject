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
export const getProductTypeOptionsApi = (pageNum: number, pageSize: number, keyword?: string) => {
  return httpClient<PageInfo<BaseSelectVO>>('/api/util/search/productType', {
    method: "GET",
    params: { pageNum, pageSize, keyword }
  });
};
export const getSupplierOptionsApi = (pageNum: number, pageSize: number, keyword?: string) => {
  return httpClient<PageInfo<BaseSelectVO>>('/api/util/search/supplier', {
    method: "GET",
    params: { pageNum, pageSize, keyword }
  });
};

/** Excel 导入结果 */
export interface ExcelImportResult {
  products: number;
  suppliers: number;
  customers: number;
  purchases: number;
  purchaseInvoices?: number;
  sales: number;
  stockIns: number;
  outbounds: number;
  errors: string[];
  rawErrors?: string[];
  errorFile?: string | null;
  message: string;
}

/** 上传 Excel 文件并一键导入（产品、供应商、客户、采购、销售、入库、出库） */
export const importExcelApi = (file: File) => {
  const formData = new FormData();
  formData.append("file", file);
  return httpClient<ExcelImportResult>("/api/excel/import", {
    method: "POST",
    body: formData as any,
  });
};
