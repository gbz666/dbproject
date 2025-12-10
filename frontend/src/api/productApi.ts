// frontend/src/api/productApi.ts
import { httpClient } from '@/utils/httpClient';
import type { Product } from '@/types/models';

export const productApi = {
  getAll: () => httpClient<Product[]>('/products'),
  getById: (id: number) => httpClient<Product>(`/products/${id}`),
  create: (product: Omit<Product, 'product_id' | 'created_at'>) => 
    httpClient<Product>('/products', {
      method: 'POST',
      body: product
    }),
  update: (id: number, product: Partial<Product>) => 
    httpClient<Product>(`/products/${id}`, {
      method: 'PUT',
      body: product
    }),
  delete: (id: number) => 
    httpClient<void>(`/products/${id}`, {
      method: 'DELETE'
    }),
  getByCategory: (category: string) => 
    httpClient<Product[]>(`/products/category/${category}`),
};