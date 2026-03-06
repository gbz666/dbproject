import { defineStore } from "pinia";
import { ref, reactive } from "vue";
import { productService } from "@/services/productService";
import type { ProductVO } from "@/types/vo";
import type { ProductQuery, ProductRequest } from "@/types/dto";

export const useProductStore = defineStore("product", () => {
  // --- 状态 (State) ---
  const products = ref<ProductVO[]>([]);
  const total = ref(0);
  const loading = ref(false);
  
  const queryParams = reactive<ProductQuery>({
    pageNum: 1,
    pageSize: 10,
    productName: "",
    categoryName: ""
  });

  // --- 动作 (Actions) ---

  /**
   * 分页加载产品数据
   */
  const loadPage = async () => {
    loading.value = true;
    try {
      const res = await productService.getProductsPage(queryParams);
      products.value = res.list;
      total.value = res.total;
    } catch (error) {
      // 错误通常在 service 或 api 层拦截弹出，此处可根据需要处理
      console.error("Store 加载分页失败:", error);
    } finally {
      loading.value = false;
    }
  };

  /**
   * 新增产品
   * @param data 产品请求体
   * @param staffId 操作人ID
   */
  const addProduct = async (data: ProductRequest, staffId: number) => {
    try {
      await productService.saveProduct(data, staffId);
      await loadPage(); // 操作成功后刷新列表
    } catch (error) {
      throw error; // 抛出异常供组件层捕获并显示 ElMessage
    }
  };

  /**
   * 更新产品信息
   * @param code 业务编码 (productCode)
   * @param data 产品请求体
   * @param staffId 操作人ID
   */
  const updateProduct = async (code: string, data: ProductRequest, staffId: number) => {
    try {
      await productService.updateProduct(code, data, staffId);
      await loadPage(); // 操作成功后刷新列表
    } catch (error) {
      throw error;
    }
  };

  /**
   * 删除产品
   * @param code 业务编码
   */
  const deleteItem = async (code: string) => {
    try {
      await productService.deleteProduct(code);
      // 如果当前页只有一条数据且不是第一页，删除后应跳回前一页
      if (products.value.length === 1 && queryParams.pageNum > 1) {
        queryParams.pageNum--;
      }
      await loadPage();
    } catch (error) {
      throw error;
    }
  };

  return {
    products,
    total,
    loading,
    queryParams,
    loadPage,
    addProduct,
    updateProduct,
    deleteItem
  };
});