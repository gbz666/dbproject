// frontend/src/stores/customerStore.ts
import { defineStore } from 'pinia';
import type { Customer } from '@/types/models';
import { customerService } from '@/service/customerService';

export const useCustomerStore = defineStore('customer', {
  state: () => ({
    customers: [] as Customer[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    getCustomerById: (state) => (id: number) => {
      return state.customers.find(customer => customer.customer_id === id);
    },
  },

  actions: {
    async fetchCustomers() {
      this.loading = true;
      this.error = null;
      try {
        this.customers = await customerService.getCustomers();
      } catch (error) {
        this.error = error instanceof Error ? error.message : '获取客户列表失败';
      } finally {
        this.loading = false;
      }
    },

    async createCustomer(customer: Omit<Customer, 'customer_id'>) {
      try {
        const newCustomer = await customerService.createCustomer(customer);
        this.customers.push(newCustomer);
        return newCustomer;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '创建客户失败';
        throw error;
      }
    },

    async updateCustomer(id: number, customer: Partial<Customer>) {
      try {
        const updatedCustomer = await customerService.updateCustomer(id, customer);
        const index = this.customers.findIndex(c => c.customer_id === id);
        if (index !== -1) {
          this.customers[index] = { ...this.customers[index], ...updatedCustomer };
        }
        return updatedCustomer;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '更新客户失败';
        throw error;
      }
    },

    async deleteCustomer(id: number) {
      try {
        await customerService.deleteCustomer(id);
        this.customers = this.customers.filter(customer => customer.customer_id !== id);
      } catch (error) {
        this.error = error instanceof Error ? error.message : '删除客户失败';
        throw error;
      }
    },
  },
});