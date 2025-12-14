<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="200px" class="main-sidebar">
        <div class="logo-container">数据库课设系统</div>
        <el-menu
          router
          default-active="/basic/customer"
          class="el-menu-vertical-demo"
          :collapse="isCollapse"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <el-sub-menu index="1">
            <template #title>
              <el-icon><Setting /></el-icon> 
              <span>基础数据</span>
            </template>
            <el-menu-item index="/basic/customer">
                <el-icon><User /></el-icon>
                客户列表
            </el-menu-item>
            <el-menu-item index="/basic/supplier">
                <el-icon><Box /></el-icon>
                供应商列表
            </el-menu-item>
            <el-menu-item index="/basic/product">
                <el-icon><Goods /></el-icon>
                产品管理 (待开发)
            </el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="2">
            <template #title>
              <el-icon><Sell /></el-icon>
              <span>销售管理 (待开发)</span>
            </template>
          </el-sub-menu>
          
          </el-menu>
      </el-aside>

      <el-container>
        <el-header class="main-header">
          <div class="header-left">
            <el-icon @click="isCollapse = !isCollapse" class="collapse-icon">
                <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
          </div>
          <div class="header-right">
            <el-dropdown>
              <span class="el-dropdown-link">
                欢迎，管理员 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>个人中心</el-dropdown-item>
                  <el-dropdown-item divided>退出登录</el-dropdown-item>
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
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
// 组合式 API 中不需要在这里 import 图标，因为已经在 main.ts 中全局注册
const isCollapse = ref(false);
</script>

<style scoped>
/* 样式保持不变 */
.common-layout {
  height: 100vh;
}

.el-container {
  height: 100%;
}

/* 侧边栏样式 */
.main-sidebar {
  background-color: #545c64;
  height: 100%;
  overflow-y: auto;
  transition: width 0.3s; /* 添加折叠过渡 */
}

.logo-container {
  height: 60px;
  line-height: 60px;
  color: white;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  background-color: #545c64;
  white-space: nowrap; /* 防止 logo 文本折叠 */
  overflow: hidden;
}

.el-menu {
  border-right: none;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 200px;
}

/* 顶部 Header 样式 */
.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  padding: 0 20px;
}

.header-left .collapse-icon {
    font-size: 24px;
    cursor: pointer;
    margin-right: 20px;
}

/* 主内容区样式 */
.main-content {
  background-color: #f0f2f5;
  padding: 10px;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}
</style>