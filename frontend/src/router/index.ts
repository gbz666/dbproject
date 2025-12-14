// src/router/index.ts (完善后)
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue' 
import CustomerListView from '@/views/customer/CustomerList.vue'
import SupplierListView from '@/views/supplier/SupplierList.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Layout',
      component: HomeView, // HomeView 现在是布局组件
      redirect: '/basic/customer', // 默认重定向到客户列表
      children: [
        {
          path: '/basic/customer',
          name: 'CustomerList',
          component: CustomerListView // 直接使用您需要的视图
        },
        {
          path: '/basic/supplier',
          name: 'SupplierList',
          component: SupplierListView // 直接使用您需要的视图
        },
        // 其他模块的路由...
      ]
    },
    // 如果有登录页等非布局页面，可以放在这里
    // {
    //   path: '/login',
    //   name: 'Login',
    //   component: () => import('@/views/LoginView.vue')
    // }
  ]
})

export default router