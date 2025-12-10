<!-- frontend/src/components/Layout.vue -->
<template>
  <el-container class="layout-container">
    <el-aside width="250px">
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <div class="logo">
          <h3>账目库存系统</h3>
        </div>
        
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        
        <el-sub-menu index="sales">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>销售管理</span>
          </template>
          <el-menu-item index="/customers">客户管理</el-menu-item>
          <el-menu-item index="/sales-orders">销售订单</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="purchase">
          <template #title>
            <el-icon><ShoppingBag /></el-icon>
            <span>采购管理</span>
          </template>
          <el-menu-item index="/suppliers">供应商管理</el-menu-item>
          <el-menu-item index="/purchase-orders">采购订单</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="warehouse">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>库存管理</span>
          </template>
          <el-menu-item index="/stock-in">入库管理</el-menu-item>
          <el-menu-item index="/stock-out">出库管理</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="finance">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>财务管理</span>
          </template>
          <el-menu-item index="/sales-invoices">销项发票</el-menu-item>
          <el-menu-item index="/purchase-invoices">进项发票</el-menu-item>
          <el-menu-item index="/receipts">收款管理</el-menu-item>
          <el-menu-item index="/payments">付款管理</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="reports">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>报表监控</span>
          </template>
          <el-menu-item index="/sales-monitoring">销售监控</el-menu-item>
          <el-menu-item index="/purchase-monitoring">采购监控</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/admin" v-if="authStore.user?.role === 'admin'">
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <span class="user-info">欢迎，{{ authStore.user?.username }}</span>
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-icon><User /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activeMenu = computed(() => route.path);
const currentRouteName = computed(() => route.meta.title as string || '');

const handleCommand = (command: string) => {
  if (command === 'logout') {
    authStore.logout();
    router.push('/login');
  } else if (command === 'profile') {
    // 处理个人信息
  }
};
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background-color: #2b2f3a;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  background-color: #fff;
}

.user-info {
  margin-right: 10px;
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>