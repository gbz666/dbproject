// frontend/src/service/supplierService.ts

import { supplierApi } from '@/api/supplierApi';
import type { Supplier } from '@/types/models';
// 引入 ApiError 类型，以便在 Service 层处理结构化错误
// 假设 ApiError 定义在 /src/utils/httpClient.ts 中
import type { ApiError } from '@/utils/httpClient'; 

/**
 * 供应商服务 (SupplierService)
 * 封装了所有与供应商相关的业务逻辑和 API 调用。
 * 目标：保留 ApiError 结构，不丢弃后端的错误信息。
 */
export class SupplierService {
  /**
   * 辅助函数：统一处理并记录 API 错误
   * @param operationName 发生错误的操作名称（用于日志）
   * @param error 捕获到的错误对象
   * @returns 抛出 ApiError
   */
  private handleError(operationName: string, error: unknown): never {
    // 假设上游 (supplierApi) 抛出的就是 ApiError 类型
    const apiError = error as ApiError;
    
    // 记录错误日志，包含更多细节
    console.error(`${operationName} 失败。状态码: ${apiError.status || 'N/A'}, 详情:`, apiError.data || apiError.message);
    
    // 重新抛出结构化的 ApiError，确保调用者能获取到状态码和数据体
    throw apiError; 
  }

  async getSuppliers(): Promise<Supplier[]> {
    try {
      return await supplierApi.getAll();
    } catch (error) {
      // ⚠️ 将通用 Error 替换为结构化错误处理
      this.handleError('获取供应商列表', error);
    }
  }

  async getSupplierById(id: number): Promise<Supplier> {
    try {
      return await supplierApi.getById(id);
    } catch (error) {
      this.handleError(`获取供应商信息 (ID: ${id})`, error);
    }
  }

  async createSupplier(supplier: Omit<Supplier, 'supplier_id'>): Promise<Supplier> {
    try {
      return await supplierApi.create(supplier);
    } catch (error) {
      this.handleError('创建供应商', error);
    }
  }

  async updateSupplier(id: number, supplier: Partial<Supplier>): Promise<Supplier> {
    try {
      return await supplierApi.update(id, supplier);
    } catch (error) {
      this.handleError(`更新供应商 (ID: ${id})`, error);
    }
  }

  async deleteSupplier(id: number): Promise<void> {
    try {
      await supplierApi.delete(id);
    } catch (error) {
      this.handleError(`删除供应商 (ID: ${id})`, error);
    }
  }
}

export const supplierService = new SupplierService();