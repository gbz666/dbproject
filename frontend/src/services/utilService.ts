import {
  getCustomerPageApi,
  getProductPageApi,
  getProductTypeOptionsApi,
  getSupplierOptionsApi,
} from "@/api/utilApi";

export const utilService = {
  // 获取客户分页数据
  async getCustomerOptions(keyword: string, pageNum = 1) {
    return await getCustomerPageApi(pageNum, 100, keyword);
  },

  // 获取产品分页数据
  async getProductOptions(keyword: string, pageNum = 1) {
    return await getProductPageApi(pageNum, 100, keyword);
  },
  async getProductTypeOptions(keyword: string, pageNum = 1) {
    return await getProductTypeOptionsApi(pageNum, 100, keyword);
  },


  async getSupplierOptions(keyword: string, pageNum = 1) {
    return await getSupplierOptionsApi(pageNum, 100, keyword);
  },
};
