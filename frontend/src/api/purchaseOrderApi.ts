// frontend/src/api/purchaseOrderApi.ts
import { httpClient } from '@/utils/httpClient';
import type { PurchaseOrder } from '@/types/models';

export const purchaseOrderApi = {
  getAll: () => httpClient<PurchaseOrder[]>('/purchase-orders'),
  getById: (id: number) => httpClient<PurchaseOrder>(`/purchase-orders/${id}`),
  create: (order: Omit<PurchaseOrder, 'order_id' | 'created_at'>) => 
    httpClient<PurchaseOrder>('/purchase-orders', {
      method: 'POST',
      body: order
    }),
  update: (id: number, order: Partial<PurchaseOrder>) => 
    httpClient<PurchaseOrder>(`/purchase-orders/${id}`, {
      method: 'PUT',
      body: order
    }),
  delete: (id: number) => 
    httpClient<void>(`/purchase-orders/${id}`, {
      method: 'DELETE'
    }),
  getBySupplier: (supplierId: number) => 
    httpClient<PurchaseOrder[]>(`/purchase-orders/supplier/${supplierId}`),
};