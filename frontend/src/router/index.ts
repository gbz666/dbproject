// src/router/index.ts (完善后)
import { createRouter, createWebHistory } from "vue-router";
import HomeView from "@/views/HomeView.vue";
import CustomerListView from "@/views/customer/CustomerList.vue";
import SupplierListView from "@/views/supplier/SupplierList.vue";
import ProductList from "@/views/product/ProductList.vue";
import SalesOrderList from "@/views/salesOrder/salesOrderList.vue";
import PurchaseOrderList from "@/views/purchaseOrder/purchaseOrderList.vue";
import outBoundList from "@/views/outBound/outBoundList.vue";
import StockInList from "@/views/StockIn/stockInList.vue";
import InventoryList from "@/views/Inventory/InventoryList.vue";
import PurchaseInvoiceList from "@/views/purchaseInvoice/purchaseInvoiceList.vue";
import SalesInvoiceList from "@/views/salesInvoice/salesInvoiceList.vue";
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "Layout",
      component: HomeView, // HomeView 现在是布局组件
      redirect: "/basic/customer", // 默认重定向到客户列表
      children: [
        {
          path: "/basic/customer",
          name: "CustomerList",
          component: CustomerListView, // 直接使用您需要的视图
        },
        {
          path: "/basic/supplier",
          name: "SupplierList",
          component: SupplierListView, // 直接使用您需要的视图
        },
        {
          path: "/basic/product",
          name: "productList",
          component: ProductList, // 直接使用您需要的视图
        },
        {
          path: "/basic/salesOrder",
          name: "salesOrderList",
          component: SalesOrderList, // 直接使用您需要的视图
        },
        {
          path: "/basic/purchaseOrder",
          name: "purchaseOrderList",
          component: PurchaseOrderList, // 直接使用您需要的视图
        },
        {
          path: "/basic/purchaseInvoice",
          name: "purchaseInvoiceList",
          component: PurchaseInvoiceList,
        },
        {
          path: "/basic/salesInvoice",
          name: "salesInvoiceList",
          component: SalesInvoiceList,
        },
        {
          path: "/basic/outBound",
          name: "outBoundList",
          component: outBoundList, // 直接使用您需要的视图
        },
        {
          path: "/basic/stockIn",
          name: "stockInList",
          component: StockInList, // 直接使用您需要的视图
        },
        {
          path: "/basic/inventory",
          name: "inventoryList",
          component: InventoryList, // 直接使用您需要的视图
        },
        {
          path: "/profile",
          name: "Profile",
          component: () => import("@/views/profile/ProfileView.vue"),
          meta: { title: "个人中心" },
        },
      ],
    },
    // 如果有登录页等非布局页面，可以放在这里
    {
      path: '/auth/login',
      name: 'Login',
      component: () => import('@/views/auth/login.vue')
    }
  ],
});

export default router;
