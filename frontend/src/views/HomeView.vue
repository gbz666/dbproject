<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="200px" class="main-sidebar">
        <div class="logo-container">数据库课设系统</div>
        <el-menu router :default-active="activeMenu" class="el-menu-vertical-demo" :collapse="isCollapse"
          background-color="#545c64" text-color="#fff" active-text-color="#ffd04b">
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
<!-- 
          <el-sub-menu index="2">
            <template #title>
              <el-icon>
                <Sell />
              </el-icon>
              <span>销售管理 (待开发)</span>
            </template>
          </el-sub-menu> -->

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
    transition: width 0.3s;
    /* 添加折叠过渡 */
  }

  .logo-container {
    height: 60px;
    line-height: 60px;
    color: white;
    text-align: center;
    font-size: 18px;
    font-weight: bold;
    background-color: #545c64;
    white-space: nowrap;
    /* 防止 logo 文本折叠 */
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

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .el-dropdown-link {
    cursor: pointer;
    display: flex;
    align-items: center;
  }
</style>