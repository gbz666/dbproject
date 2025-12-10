// frontend/src/stores/productStore.ts
import { defineStore } from 'pinia';
import type { Product } from '@/types/models';
import { productService } from '@/service/productService';

export const useProductStore = defineStore('product', {
  state: () => ({
    products: [] as Product[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    getProductById: (state) => (id: number) => {
      return state.products.find(product => product.product_id === id);
    },
  },

  actions: {
    async fetchProducts() {
      this.loading = true;
      this.error = null;
      try {
        this.products = await productService.getProducts();
      } catch (error) {
        this.error = error instanceof Error ? error.message : '获取产品列表失败';
      } finally {
        this.loading = false;
      }
    },

    async createProduct(product: Omit<Product, 'product_id'>) {
      try {
        const newProduct = await productService.createProduct(product);
        this.products.push(newProduct);
        return newProduct;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '创建产品失败';
        throw error;
      }
    },

    async updateProduct(id: number, product: Partial<Product>) {
      try {
        const updatedProduct = await productService.updateProduct(id, product);
        const index = this.products.findIndex(p => p.product_id === id);
        if (index !== -1) {
          this.products[index] = { ...this.products[index], ...updatedProduct };
        }
        return updatedProduct;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '更新产品失败';
        throw error;
      }
    },

    async deleteProduct(id: number) {
      try {
        await productService.deleteProduct(id);
        this.products = this.products.filter(product => product.product_id !== id);
      } catch (error) {
        this.error = error instanceof Error ? error.message : '删除产品失败';
        throw error;
      }
    },
  },
});