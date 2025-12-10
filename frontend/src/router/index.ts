import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/login.vue')
    },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/Dashboard.vue')
        },
        {
          path: 'customers',
          name: 'customers',
          component: () => import('@/views/customer/CustomerList.vue')
        },
        {
          path: 'suppliers',
          name: 'suppliers',
          component: () => import('@/views/supplier/SupplierList.vue')
        },
        {
          path: 'products',
          name: 'products',
          component: () => import('@/views/product/ProductList.vue')
        },
        {
          path: 'sales-orders',
          name: 'sales-orders',
          component: () => import('@/views/sales/SalesOrderList.vue')
        },
        {
          path: 'purchase-orders',
          name: 'purchase-orders',
          component: () => import('@/views/purchase/PurchaseOrderList.vue')
        },
        {
          path: 'stock',
          name: 'stock',
          component: () => import('@/views/stock/StockManagement.vue')
        },
        {
          path: 'receipts',
          name: 'receipts',
          component: () => import('@/views/finance/ReceiptList.vue')
        },
        {
          path: 'payments',
          name: 'payments',
          component: () => import('@/views/finance/PaymentList.vue')
        },
        {
          path: 'sales-invoices',
          name: 'sales-invoices',
          component: () => import('@/views/invoice/SalesInvoiceList.vue')
        },
        {
          path: 'purchase-invoices',
          name: 'purchase-invoices',
          component: () => import('@/views/invoice/PurchaseInvoiceList.vue')
        }
      ]
    }
  ]
})

export default router