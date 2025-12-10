// frontend/src/api/salesOrderApi.ts
import { httpClient } from '@/utils/httpClient'; 
import type { SalesOrder } from '@/types/models';

export const salesOrderApi = {
  /**
   * 获取所有销售订单
   */
  getAll: () => httpClient<SalesOrder[]>('/sales-orders'),
  
  /**
   * 根据ID获取销售订单
   * @param id 订单ID
   */
  getById: (id: number) => httpClient<SalesOrder>(`/sales-orders/${id}`),
  
  /**
   * 创建新销售订单
   * @param order 订单数据
   */
  create: (order: Omit<SalesOrder, 'order_id' | 'created_at'>) => 
    httpClient<SalesOrder>('/sales-orders', {
      method: 'POST',
      body: order
    }),
  
  /**
   * 更新销售订单
   * @param id 订单ID
   * @param order 订单部分更新数据
   */
  update: (id: number, order: Partial<SalesOrder>) => 
    httpClient<SalesOrder>(`/sales-orders/${id}`, {
      method: 'PUT', // 或者 'PATCH'
      body: order
    }),
  
  /**
   * 删除销售订单
   * @param id 订单ID
   */
  delete: (id: number) => 
    httpClient<void>(`/sales-orders/${id}`, {
      method: 'DELETE'
    }),
};