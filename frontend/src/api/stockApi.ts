// frontend/src/api/stockApi.ts
import { httpClient } from '@/utils/httpClient'; 
import type { StockIn, StockOut } from '@/types/models';

/**
 * @interface StockSummary
 * 库存汇总数据的类型定义，与 Service 层保持一致。
 */
export interface StockSummary {
  totalIn: number;
  totalOut: number;
  currentStock: number;
  byWarehouse: {
    shanghai: number;
    tianjin: number;
    shenzhen: number;
  };
}

export const stockApi = {
  // --- 入库相关 ---
  stockIn: {
    /**
     * 获取所有入库记录
     */
    getAll: () => httpClient<StockIn[]>('/stock-in'),
    
    /**
     * ⚠️ 新增: 根据 ID 获取单个入库记录
     */
    getById: (id: number) => httpClient<StockIn>(`/stock-in/${id}`),

    /**
     * 创建入库记录
     * @param stockIn 入库数据
     */
    create: (stockIn: Omit<StockIn, 'stock_in_id' | 'created_at'>) => 
      httpClient<StockIn>('/stock-in', {
        method: 'POST',
        body: stockIn
      }),
    
    /**
     * 更新入库记录
     * @param id 记录ID
     * @param stockIn 更新数据
     */
    update: (id: number, stockIn: Partial<StockIn>) => 
      httpClient<StockIn>(`/stock-in/${id}`, {
        method: 'PUT',
        body: stockIn
      }),
    
    /**
     * ⚠️ 新增: 删除入库记录
     */
    delete: (id: number) => httpClient<void>(`/stock-in/${id}`, {
      method: 'DELETE',
    }),
  },
  
  // --- 出库相关 ---
  stockOut: {
    /**
     * 获取所有出库记录
     */
    getAll: () => httpClient<StockOut[]>('/stock-out'),

    /**
     * ⚠️ 新增: 根据 ID 获取单个出库记录
     */
    getById: (id: number) => httpClient<StockOut>(`/stock-out/${id}`),
    
    /**
     * 创建出库记录
     * @param stockOut 出库数据
     */
    create: (stockOut: Omit<StockOut, 'stock_out_id' | 'created_at'>) => 
      httpClient<StockOut>('/stock-out', {
        method: 'POST',
        body: stockOut
      }),
    
    /**
     * 更新出库记录
     * @param id 记录ID
     * @param stockOut 更新数据
     */
    update: (id: number, stockOut: Partial<StockOut>) => 
      httpClient<StockOut>(`/stock-out/${id}`, {
        method: 'PUT',
        body: stockOut
      }),
      
    /**
     * ⚠️ 新增: 删除出库记录
     */
    delete: (id: number) => httpClient<void>(`/stock-out/${id}`, {
      method: 'DELETE',
    }),
  },
  
  // --- 库存查询/汇总 ---
  /**
   * ⚠️ 关键新增: 获取库存汇总数据
   * @param productId 可选，按产品ID查询
   * @returns 结构化的库存汇总数据
   */
  getSummary: (productId?: string) => {
    const endpoint = productId ? `/stock/summary?productId=${productId}` : '/stock/summary';
    return httpClient<StockSummary>(endpoint);
  },
};