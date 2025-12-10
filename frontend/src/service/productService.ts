// frontend/src/service/productService.ts

import { productApi } from '@/api/productApi';
import type { Product } from '@/types/models';
// ⚠️ 引入 ApiError 类型，以便在 Service 层处理结构化错误
// 假设 ApiError 定义在 /src/utils/httpClient.ts 中
import type { ApiError } from '@/utils/httpClient'; 

/**
 * 产品服务 (ProductService)
 * 封装了所有与产品相关的业务逻辑和 API 调用。
 * 目标：保留 ApiError 结构，不丢弃后端的错误信息。
 */
export class ProductService {
  /**
   * 辅助函数：统一处理并记录 API 错误
   * @param operationName 发生错误的操作名称（用于日志）
   * @param error 捕获到的错误对象
   * @returns 抛出 ApiError
   */
  private handleError(operationName: string, error: unknown): never {
    // 假设上游 (productApi) 抛出的就是 ApiError 类型
    const apiError = error as ApiError;
    
    // 记录错误日志，包含更多细节
    console.error(`${operationName} 失败。状态码: ${apiError.status || 'N/A'}, 详情:`, apiError.data || apiError.message);
    
    // 重新抛出结构化的 ApiError，确保调用者能获取到状态码和数据体
    throw apiError; 
  }

  async getProducts(): Promise<Product[]> {
    try {
      return await productApi.getAll();
    } catch (error) {
      this.handleError('获取产品列表', error);
    }
  }

  async getProductById(id: number): Promise<Product> {
    try {
      return await productApi.getById(id);
    } catch (error) {
      this.handleError(`获取产品信息 (ID: ${id})`, error);
    }
  }

  async createProduct(product: Omit<Product, 'product_id' | 'created_at'>): Promise<Product> {
    try {
      return await productApi.create(product);
    } catch (error) {
      this.handleError('创建产品', error);
    }
  }

  async updateProduct(id: number, product: Partial<Product>): Promise<Product> {
    try {
      return await productApi.update(id, product);
    }
    catch (error) {
      this.handleError(`更新产品 (ID: ${id})`, error);
    }
  }

  async deleteProduct(id: number): Promise<void> {
    try {
      // 假设 delete 方法返回 Promise<void>
      await productApi.delete(id);
    } catch (error) {
      this.handleError(`删除产品 (ID: ${id})`, error);
    }
  }

  async getProductsByCategory(category: string): Promise<Product[]> {
    try {
      return await productApi.getByCategory(category);
    } catch (error) {
      this.handleError(`按分类 (${category}) 获取产品`, error);
    }
  }
}

export const productService = new ProductService();