// frontend/src/api/receiptApi.ts
import { httpClient } from '@/utils/httpClient';
import type { Receipt } from '@/types/models';

export const receiptApi = {
  getAll: () => httpClient<Receipt[]>('/receipts'),
  getById: (id: number) => httpClient<Receipt>(`/receipts/${id}`),
  getBySalesOrder: (orderId: number) => 
    httpClient<Receipt[]>(`/receipts/order/${orderId}`),
  create: (receipt: Omit<Receipt, 'receipt_id' | 'created_at'>) => 
    httpClient<Receipt>('/receipts', {
      method: 'POST',
      body: receipt
    }),
  update: (id: number, receipt: Partial<Receipt>) => 
    httpClient<Receipt>(`/receipts/${id}`, {
      method: 'PUT',
      body: receipt
    }),
  delete: (id: number) => 
    httpClient<void>(`/receipts/${id}`, {
      method: 'DELETE'
    }),
  getTotalReceived: (orderId: number) => 
    httpClient<{ total_received: number }>(`/receipts/order/${orderId}/total`),
};