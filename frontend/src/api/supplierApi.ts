// frontend/src/api/supplierApi.ts
import { httpClient } from '@/utils/httpClient';
import type { Supplier } from '@/types/models';

export const supplierApi = {
  getAll: () => httpClient<Supplier[]>('/suppliers'),
  getById: (id: number) => httpClient<Supplier>(`/suppliers/${id}`),
  create: (supplier: Omit<Supplier, 'supplier_id'>) => 
    httpClient<Supplier>('/suppliers', {
      method: 'POST',
      body: supplier
    }),
  update: (id: number, supplier: Partial<Supplier>) => 
    httpClient<Supplier>(`/suppliers/${id}`, {
      method: 'PUT',
      body: supplier
    }),
  delete: (id: number) => 
    httpClient<void>(`/suppliers/${id}`, {
      method: 'DELETE'
    }),
};