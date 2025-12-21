import { productApi } from "@/api/productApi";
import type { ProductRequest, ProductQuery } from "@/types/dto";

export const productService = {
  async getProductsPage(query: ProductQuery) {
    try {
      return await productApi.fetchProducts(query);
    } catch (error) {
      console.error("加载产品列表失败", error);
      throw error;
    }
  },

  async saveProduct(data: ProductRequest, staffId: number) {
    // 根据是否有 ID 判断是创建还是更新
    if (data.id) {
      // 注意：后端更新接口路径需要 productCode，这里假设 VO 中已带入或从业务逻辑获取
      // 如果只有 ID 没有 code，建议在调用前确保 code 已存在
      throw new Error("更新操作请调用 updateByCode");
    }
    return await productApi.create(data, staffId);
  },

  async updateProduct(code: string, data: ProductRequest, staffId: number) {
    return await productApi.update(code, data, staffId);
  },

  async deleteProduct(code: string) {
    if (confirm(`确定要删除产品 ${code} 吗？`)) {
      return await productApi.remove(code);
    }
  }
};