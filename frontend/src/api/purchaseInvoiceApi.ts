// frontend/src/api/purchaseInvoiceApi.ts
import { httpClient } from '@/utils/httpClient';
import type { PurchaseInvoice } from '@/types/models';

export const purchaseInvoiceApi = {
  getAll: () => httpClient<PurchaseInvoice[]>('/purchase-invoices'),
  getById: (id: number) => httpClient<PurchaseInvoice>(`/purchase-invoices/${id}`),
  getByPurchaseOrder: (orderId: number) => 
    httpClient<PurchaseInvoice[]>(`/purchase-invoices/order/${orderId}`),
  create: (invoice: Omit<PurchaseInvoice, 'invoice_id' | 'created_at'>) => 
    httpClient<PurchaseInvoice>('/purchase-invoices', {
      method: 'POST',
      body: invoice
    }),
  update: (id: number, invoice: Partial<PurchaseInvoice>) => 
    httpClient<PurchaseInvoice>(`/purchase-invoices/${id}`, {
      method: 'PUT',
      body: invoice
    }),
  delete: (id: number) => 
    httpClient<void>(`/purchase-invoices/${id}`, {
      method: 'DELETE'
    }),
};