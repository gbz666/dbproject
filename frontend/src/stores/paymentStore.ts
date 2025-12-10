// frontend/src/stores/paymentStore.ts
import { defineStore } from 'pinia';
import type { Payment } from '@/types/models';
import { paymentService } from '@/service/paymentService';

export const usePaymentStore = defineStore('payment', {
  state: () => ({
    payments: [] as Payment[],
    loading: false,
    error: null as string | null,
  }),

  getters: {
    getPaymentById: (state) => (id: number) => {
      return state.payments.find(payment => payment.payment_id === id);
    },
  },

  actions: {
    async fetchPayments() {
      this.loading = true;
      this.error = null;
      try {
        this.payments = await paymentService.fetchAllPayments();
      } catch (error) {
        this.error = error instanceof Error ? error.message : '获取付款记录失败';
      } finally {
        this.loading = false;
      }
    },

    async createPayment(payment: Omit<Payment, 'payment_id'>) {
      try {
        const newPayment = await paymentService.createNewPayment(payment);
        this.payments.push(newPayment);
        return newPayment;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '创建付款记录失败';
        throw error;
      }
    },

    async updatePayment(id: number, payment: Partial<Payment>) {
      try {
        const updatedPayment = await paymentService.updatePayment(id, payment);
        const index = this.payments.findIndex(p => p.payment_id === id);
        if (index !== -1) {
          this.payments[index] = { ...this.payments[index], ...updatedPayment };
        }
        return updatedPayment;
      } catch (error) {
        this.error = error instanceof Error ? error.message : '更新付款记录失败';
        throw error;
      }
    },

    // async deletePayment(id: number) {
    //   try {
    //     await paymentService.;
    //     this.payments = this.payments.filter(payment => payment.payment_id !== id);
    //   } catch (error) {
    //     this.error = error instanceof Error ? error.message : '删除付款记录失败';
    //     throw error;
    //   }
    // },
  },
});