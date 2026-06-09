// src/router/index.ts
import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/authStore";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "Layout",
      component: () => import("@/views/Layout.vue"),
      redirect: "/basic/customer",
      children: [
        {
          path: "/basic/customer",
          name: "CustomerList",
          component: () => import("@/views/customer/CustomerList.vue"),
        },
        {
          path: "/basic/supplier",
          name: "SupplierList",
          component: () => import("@/views/supplier/SupplierList.vue"),
        },
        {
          path: "/basic/product",
          name: "productList",
          component: () => import("@/views/product/ProductList.vue"),
        },
        {
          path: "/basic/salesOrder",
          name: "salesOrderList",
          component: () => import("@/views/salesOrder/salesOrderList.vue"),
        },
        {
          path: "/basic/purchaseOrder",
          name: "purchaseOrderList",
          component: () => import("@/views/purchaseOrder/purchaseOrderList.vue"),
        },
        {
          path: "/basic/purchaseInvoice",
          name: "purchaseInvoiceList",
          component: () => import("@/views/purchaseInvoice/purchaseInvoiceList.vue"),
        },
        {
          path: "/basic/salesInvoice",
          name: "salesInvoiceList",
          component: () => import("@/views/salesInvoice/salesInvoiceList.vue"),
        },
        {
          path: "/basic/outBound",
          name: "outBoundList",
          component: () => import("@/views/outBound/outBoundList.vue"),
        },
        {
          path: "/basic/stockIn",
          name: "stockInList",
          component: () => import("@/views/StockIn/stockInList.vue"),
        },
        {
          path: "/basic/inventory",
          name: "inventoryList",
          component: () => import("@/views/Inventory/InventoryList.vue"),
        },
        {
          path: "/finance/payment-receipt",
          name: "PaymentReceiptList",
          component: () => import("@/views/paymentReceipt/PaymentReceiptList.vue"),
          meta: { title: "收款管理" },
        },
        {
          path: "/finance/payment-expense",
          name: "PaymentExpenseList",
          component: () => import("@/views/paymentExpense/PaymentExpenseList.vue"),
          meta: { title: "付款管理" },
        },
        {
          path: "/profile",
          name: "Profile",
          component: () => import("@/views/profile/ProfileView.vue"),
          meta: { title: "个人中心" },
        },
        {
          path: "/ai-query",
          name: "AiQuery",
          component: () => import("@/views/ai/AiQueryView.vue"),
          meta: { title: "AI 智能查询" },
        },
        {
          path: "/system/staff-manage",
          name: "StaffManage",
          component: () => import("@/views/staffManage/StaffManageView.vue"),
          meta: { title: "员工管理", requireRoles: ["admin", "manager"] },
        },
      ],
    },
    {
      path: '/auth/login',
      name: 'Login',
      component: () => import('@/views/auth/login.vue')
    }
  ],
});

// 路由守卫：未登录用户只能访问登录页
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  if (to.name !== 'Login' && !authStore.isLoggedIn) {
    next({ name: 'Login' });
  } else {
    next();
  }
});

export default router;
