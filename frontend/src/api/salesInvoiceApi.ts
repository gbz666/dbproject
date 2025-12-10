// frontend/src/api/salesInvoiceApi.ts
import { httpClient } from '@/utils/httpClient';
import type { SalesInvoice } from '@/types/models';

export const salesInvoiceApi = {
  getAll: () => httpClient<SalesInvoice[]>('/sales-invoices'),
  getById: (id: number) => httpClient<SalesInvoice>(`/sales-invoices/${id}`),
  getBySalesOrder: (orderId: number) => 
    httpClient<SalesInvoice[]>(`/sales-invoices/order/${orderId}`),
  create: (invoice: Omit<SalesInvoice, 'invoice_id' | 'created_at'>) => 
    httpClient<SalesInvoice>('/sales-invoices', {
      method: 'POST',
      body: invoice
    }),
  update: (id: number, invoice: Partial<SalesInvoice>) => 
    httpClient<SalesInvoice>(`/sales-invoices/${id}`, {
      method: 'PUT',
      body: invoice
    }),
  delete: (id: number) => 
    httpClient<void>(`/sales-invoices/${id}`, {
      method: 'DELETE'
    }),
  updateStatus: (id: number, status: SalesInvoice['status']) => 
    httpClient<SalesInvoice>(`/sales-invoices/${id}/status`, {
      method: 'PATCH',
      body: { status }
    }),
};