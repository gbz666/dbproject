import { getCustomerPageApi ,getProductPageApi} from "@/api/utilApi";

export const utilService = {
  // 获取客户分页数据
  async getCustomerOptions(keyword: string, pageNum = 1) {
    return await getCustomerPageApi(pageNum, 15, keyword);
  },

  // 获取产品分页数据
  async getProductOptions(keyword: string, pageNum = 1) {
    return await getProductPageApi(pageNum, 15, keyword);
  }
};