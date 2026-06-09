<template>
  <div class="common-layout">
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '220px'" class="main-sidebar">
        <div class="logo-container">
          <span v-if="!isCollapse">企业管理系统</span>
          <span v-else class="logo-mini">企</span>
        </div>
        <el-menu router :default-active="activeMenu" class="sidebar-menu" :collapse="isCollapse">
          <el-sub-menu index="1">
            <template #title>
              <el-icon>
                <Setting />
              </el-icon>
              <span>基础数据</span>
            </template>
            <el-menu-item index="/basic/customer">
              <el-icon>
                <User />
              </el-icon>
              客户列表
            </el-menu-item>
            <el-menu-item index="/basic/supplier">
              <el-icon>
                <Box />
              </el-icon>
              供应商列表
            </el-menu-item>
            <el-menu-item index="/basic/product">
              <el-icon>
                <Goods />
              </el-icon>
              产品管理
            </el-menu-item>
            <el-menu-item index="/basic/salesOrder">
              <el-icon>
                <Goods />
              </el-icon>
              销售订单
            </el-menu-item>
            <el-menu-item index="/basic/purchaseOrder">
              <el-icon>
                <Goods />
              </el-icon>
              采购订单
            </el-menu-item>
            <el-menu-item index="/basic/purchaseInvoice">
              <el-icon>
                <Goods />
              </el-icon>
              进项发票
            </el-menu-item>
            <el-menu-item index="/basic/salesInvoice">
              <el-icon>
                <Goods />
              </el-icon>
              销项发票
            </el-menu-item>
            <el-menu-item index="/basic/outBound">
              <el-icon>
                <Goods />
              </el-icon>
              出库管理
            </el-menu-item>
            <el-menu-item index="/basic/stockIn">
              <el-icon>
                <Goods />
              </el-icon>
              入库管理
            </el-menu-item>
            <el-menu-item index="/basic/inventory">
              <el-icon>
                <Goods />
              </el-icon>
              库存展示
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="finance">
            <template #title>
              <el-icon><Money /></el-icon>
              <span>财务管理</span>
            </template>
            <el-menu-item index="/finance/payment-receipt">
              <el-icon><Download /></el-icon>
              收款管理
            </el-menu-item>
            <el-menu-item index="/finance/payment-expense">
              <el-icon><Upload /></el-icon>
              付款管理
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/ai-query">
            <el-icon><MagicStick /></el-icon>
            <span>AI 智能查询</span>
          </el-menu-item>

          <el-sub-menu v-if="authStore.canManageStaff" index="system">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/staff-manage">
              <el-icon><User /></el-icon>
              员工管理
            </el-menu-item>
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
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              accept=".xlsx,.xls"
              @change="handleExcelImport"
            >
              <el-button type="primary" :loading="importLoading">
                <el-icon><Upload /></el-icon>
                导入 Excel
              </el-button>
            </el-upload>
            <el-dropdown @command="handleUserCommand">
              <span class="el-dropdown-link">
                欢迎，{{ authStore.user?.staffName ?? '管理员' }} <el-icon class="el-icon--right">
                  <ArrowDown />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
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
  </div>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { ElMessage } from 'element-plus';
  import { importExcelApi } from '@/api/utilApi';
  import { useAuthStore } from '@/stores/authStore';

  const isCollapse = ref(false);
  const route = useRoute();
  const router = useRouter();
  const importLoading = ref(false);
  const authStore = useAuthStore();

  const activeMenu = computed(() => route.path);

  const handleUserCommand = (command: string) => {
    if (command === 'profile') {
      router.push('/profile');
    } else if (command === 'logout') {
      authStore.logout();
      router.push('/auth/login');
    }
  };

  const handleExcelImport = async (e: { raw?: File }) => {
    const file = e?.raw;
    if (!file) return;
    importLoading.value = true;
    try {
      const result = await importExcelApi(file);
      const msg = `导入完成：产品 ${result.products}、供应商 ${result.suppliers}、客户 ${result.customers}、采购 ${result.purchases}、销售 ${result.sales}、入库 ${result.stockIns}、出库 ${result.outbounds}`;
      if (result.errors?.length) {
        ElMessage.warning(`${msg}，${result.errors.length} 条行有误`);
      } else {
        ElMessage.success(msg);
      }
    } catch (err: any) {
      ElMessage.error(err?.message || '导入失败');
    } finally {
      importLoading.value = false;
    }
  };
</script>

<style scoped>
  .common-layout {
    height: 100vh;
  }

  .el-container {
    height: 100%;
  }

  /* ── 侧边栏：白底 + 苹果灰 hover + 浅蓝选中 ── */
  .main-sidebar {
    background-color: var(--color-bg);
    height: 100%;
    overflow-y: auto;
    transition: width var(--transition-base);
    border-right: 1px solid var(--color-border-soft);
  }

  .logo-container {
    height: var(--layout-header-height);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--color-text);
    background-color: var(--color-bg);
    border-bottom: 1px solid var(--color-border-soft);
    font-size: var(--font-size-lg);
    font-weight: 600;
    letter-spacing: var(--letter-spacing-tight);
    white-space: nowrap;
    overflow: hidden;
  }

  .logo-mini {
    font-size: var(--font-size-xl);
    color: var(--color-primary);
  }

  .sidebar-menu {
    border-right: none;
    background: var(--color-bg);
    padding: var(--space-2) var(--space-2);
  }

  .sidebar-menu:not(.el-menu--collapse) {
    width: 220px;
  }

  /* 菜单项 Apple 风：白底、轻 hover、浅蓝选中 */
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 40px;
    line-height: 40px;
    color: var(--color-text-secondary);
    font-size: var(--font-size-base);
    border-radius: var(--radius-sm);
    margin-bottom: var(--space-1);
  }

  :deep(.el-menu-item:hover),
  :deep(.el-sub-menu__title:hover) {
    background-color: var(--color-bg-mute);
    color: var(--color-text);
  }

  :deep(.el-menu-item.is-active) {
    background-color: var(--color-primary-bg);
    color: var(--color-primary);
    font-weight: 500;
  }

  :deep(.el-sub-menu .el-menu-item) {
    padding-left: 44px !important;
    min-width: auto;
  }

  /* ── 顶部 Header ── */
  .main-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: var(--color-bg);
    border-bottom: 1px solid var(--color-border-soft);
    padding: 0 var(--space-6);
    height: var(--layout-header-height);
    line-height: var(--layout-header-height);
  }

  .header-left .collapse-icon {
    font-size: var(--font-size-xl);
    cursor: pointer;
    color: var(--color-text-secondary);
    padding: var(--space-2);
    border-radius: var(--radius-sm);
    transition: background var(--transition-fast);
  }

  .header-left .collapse-icon:hover {
    background: var(--color-bg-mute);
    color: var(--color-text);
  }

  /* ── 主内容区 ── */
  .main-content {
    background-color: var(--color-bg-mute);
    padding: var(--space-6);
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: var(--space-3);
  }

  .el-dropdown-link {
    cursor: pointer;
    display: flex;
    align-items: center;
    color: var(--color-text-secondary);
    font-size: var(--font-size-base);
    padding: var(--space-2) var(--space-3);
    border-radius: var(--radius-sm);
    transition: background var(--transition-fast);
  }

  .el-dropdown-link:hover {
    background: var(--color-bg-mute);
    color: var(--color-text);
  }
</style>