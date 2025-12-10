// frontend/src/api/paymentApi.ts
import { httpClient } from '@/utils/httpClient';
import type{ Payment } from '@/types/models';

export const paymentApi = {
  getAll: () => httpClient<Payment[]>('/payments'),
  getById: (id: number) => httpClient<Payment>(`/payments/${id}`),
  getByPurchaseOrder: (orderId: number) => 
    httpClient<Payment[]>(`/payments/order/${orderId}`),
  create: (payment: Omit<Payment, 'payment_id' | 'created_at'>) => 
    httpClient<Payment>('/payments', {
      method: 'POST',
      body: payment
    }),
  update: (id: number, payment: Partial<Payment>) => 
    httpClient<Payment>(`/payments/${id}`, {
      method: 'PUT',
      body: payment
    }),
  delete: (id: number) => 
    httpClient<void>(`/payments/${id}`, {
      method: 'DELETE'
    }),
  getTotalPaid: (orderId: number) => 
    httpClient<{ total_paid: number }>(`/payments/order/${orderId}/total`),
};