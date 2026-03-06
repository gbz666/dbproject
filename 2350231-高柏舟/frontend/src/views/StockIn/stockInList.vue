<template>
  <div class="app-container" style="padding: 20px; background-color: #f5f7fa; min-height: 100vh;">
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="store.queryParams">
        <el-form-item label="采购单号">
          <el-input v-model="store.queryParams.purchaseOrderCode" placeholder="输入采购单号" clearable
            @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="store.queryParams.supplierName" placeholder="供应商名称" clearable @clear="handleSearch" />
        </el-form-item>
        <!-- <el-form-item label="序列号">
          <el-input v-model="store.queryParams.serialNumber" placeholder="输入序列号" clearable @clear="handleSearch" />
        </el-form-item> -->
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" icon="Plus" @click="openDialog()">新增入库单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="store.loading" :data="store.stockIns" border stripe style="width: 100%">
      <el-table-column prop="purchaseOrderCode" label="关联采购单" width="150" align="center" />
      <el-table-column prop="supplierName" label="供应商" min-width="150" show-overflow-tooltip />
      <el-table-column prop="productName" label="产品名称" min-width="150" />
      <el-table-column prop="purchaseQuantity" label="采购数量" width="100" align="right" />
      <el-table-column prop="subTotal" label="入库数量" width="100" align="right" />
      <el-table-column prop="pendingQuantity" label="未到货数量" width="100" align="right">
        <template #default="{ row }">
          <span :style="getPendingQuantityStyle(row.pendingQuantity)">
            {{ row.pendingQuantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="stockInDate" label="入库日期" width="120" align="center" />
      <el-table-column prop="serialNumbers" label="序列号" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">查看/编辑</el-button>
          <el-popconfirm title="确定删除该入库记录？此操作将影响库存。" @confirm="handleDelete(row.id)">
            <template #reference><el-button link type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination v-model:current-page="store.queryParams.pageNum" v-model:page-size="store.queryParams.pageSize"
        :total="store.total" layout="total, prev, pager, next, jumper" @current-change="store.loadPage" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑入库单' : '新增入库单'" width="1000px" top="5vh" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="采购单号" prop="purchaseOrderCode">
              <el-input v-model="form.purchaseOrderCode" placeholder="关联采购订单号(选填)" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入库日期" prop="stockInDate">
              <el-date-picker v-model="form.stockInDate" type="date" style="width: 100%" placeholder="选择日期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="填写如物流信息、验收情况等" />
        </el-form-item>

        <el-divider content-position="left">产品入库明细</el-divider>

        <div v-for="(item, pIndex) in form.items" :key="pIndex" class="product-item-card">
          <div class="product-header">
            <div style="display: flex; align-items: center; gap: 10px; flex: 1">
              <span class="index-badge">{{ pIndex + 1 }}</span>
              <el-select v-model="item.productCode" filterable remote reserve-keyword placeholder="搜索产品型号"
                :remote-method="handleProductSearch" :loading="utilStore.productLoading" style="width: 250px">
                <el-option v-for="p in utilStore.productList" :key="p.code" :label="p.code" :value="p.code">
                  <span style="float: left">{{ p.code }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ p.name }}</span>
                </el-option>
              </el-select>
              <el-input v-model="item.serialNumbers" placeholder="录入序列号 (多个用英文逗号分隔)" style="flex: 1" />
            </div>
            <el-button type="danger" link icon="Delete" @click="removeItem(pIndex)">删除产品</el-button>
          </div>

          <el-table :data="item.warehouseDetails" size="small" border style="margin-top: 10px">
            <el-table-column label="入库仓库" width="300">
              <template #default="scope">
                <el-select v-model="scope.row.warehouseId" placeholder="选择仓库" style="width: 100%">
                  <el-option v-for="w in warehouseOptions" :key="w.id" :label="w.name" :value="w.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="入库数量" width="180">
              <template #default="scope">
                <el-input-number v-model="scope.row.quantity" :min="1" :precision="0" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center">
              <template #default="scope">
                <el-button type="danger" link @click="item.warehouseDetails.splice(scope.$index, 1)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" link icon="Plus" @click="addWarehouse(item)" style="margin-top: 5px">
            拆分仓库入库
          </el-button>
        </div>

        <el-button type="primary" plain icon="Plus" @click="addProduct" style="margin-top: 10px">添加产品</el-button>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="store.loading" @click="submitForm">确认入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { ElMessage, type FormInstance } from 'element-plus';
  import { useStockInStore } from '@/stores/stockInStore';
  import { useUtilStore } from '@/stores/utilStore';
  import type { StockInDTO } from '@/types/dto';

  const store = useStockInStore();
  const utilStore = useUtilStore();
  const formRef = ref<FormInstance>();
  const dialogVisible = ref(false);
  const isEdit = ref(false);

  // 模拟仓库数据，实际应从后端获取
  const warehouseOptions = [
    { id: 1, name: '上海' },
    { id: 2, name: '天津' },
    { id: 3, name: '深圳' }
  ];

  // 表单响应式数据
  const form = reactive({
    id: null as number | null,
    purchaseOrderCode: '',
    stockInDate: new Date(),
    remark: '',
    items: [] as any[]
  });

  const formRules = {
    stockInDate: [{ required: true, message: '请选择入库日期', trigger: 'change' }]
  };

  onMounted(() => store.loadPage());

  const handleSearch = () => { store.queryParams.pageNum = 1; store.loadPage(); };
  const resetFilters = () => {
    store.queryParams.purchaseOrderCode = "";
    store.queryParams.supplierName = "";
    store.queryParams.productName = "";
    store.queryParams.serialNumber = "";
    handleSearch();
  };

  const handleProductSearch = (query: string) => {
    if (query) utilStore.searchProductsAction(query);
  };

  const openDialog = (row?: any) => {
    isEdit.value = !!row;
    if (row) {
      // 这里的数据映射需要根据你后端 StockInVO 的实际结构调整
      form.id = row.id;
      form.purchaseOrderCode = row.purchaseOrderCode || '';
      form.stockInDate = row.stockInDate ? new Date(row.stockInDate) : new Date();
      form.remark = row.note || '';

      // 远程搜索回显处理
      utilStore.productList = [{ id: 0, code: row.productCode, name: row.productName } as any];

      form.items = [{
        productCode: row.productCode,
        serialNumbers: row.serialNumbers,
        warehouseDetails: row.warehouseDetails ? [...row.warehouseDetails] : [{ warehouseId: 1, quantity: row.quantity || 1 }]
      }];
    } else {
      Object.assign(form, { id: null, purchaseOrderCode: '', stockInDate: new Date(), remark: '', items: [] });
      addProduct();
    }
    dialogVisible.value = true;
  };

  const addProduct = () => {
    form.items.push({
      productCode: '',
      serialNumbers: '',
      warehouseDetails: [{ warehouseId: 1, quantity: 1 }]
    });
  };

  const removeItem = (index: number) => form.items.splice(index, 1);

  const addWarehouse = (item: any) => {
    item.warehouseDetails.push({ warehouseId: 1, quantity: 1 });
  };

  const submitForm = async () => {
    if (!formRef.value) return;
    await formRef.value.validate(async (valid) => {
      if (!valid) return;

      try {
        // 转换 DTO
        const submitData: StockInDTO = {
          id: form.id || undefined,
          purchaseOrderCode: form.purchaseOrderCode,
          stockInDate: form.stockInDate.toISOString().split('T')[0] as any,
          note: form.remark,
          items: form.items.map(item => ({
            productCode: item.productCode,
            serialNumbers: item.serialNumbers,
            warehouseDetails: item.warehouseDetails
          }))
        };

        // 调用 store 的保存方法，operatorId 暂时写死(后续可从 userStore 获取)
        await store.saveStockInAction(submitData, 2);
        ElMessage.success('入库成功');
        dialogVisible.value = false;
      } catch (e: any) {
        ElMessage.error(e.message || '操作失败');
      }
    });
  };
  const getPendingQuantityStyle = (value: number) => {
    if (value > 0) {
      return { color: '#F56C6C', fontWeight: 'bold' }; // 红色 (Danger)
    } else if (value < 0) {
      return { color: '#409EFF' }; // 蓝色 (Primary)，表示超额入库或其他异常
    } else {
      return { color: '#67C23A' }; // 绿色 (Success)
    }
  };
  const handleDelete = (id: number) => {
    store.deleteItemAction(id, 2)
      .then(() => ElMessage.success('删除成功'))
      .catch(err => ElMessage.error(err.message || '删除失败'));
  };
</script>

<style scoped>
  .product-item-card {
    border: 1px solid #ebeef5;
    border-radius: 8px;
    padding: 18px;
    margin-bottom: 20px;
    background-color: #ffffff;
    transition: all 0.3s;
  }

  .product-item-card:hover {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  .product-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
  }

  .index-badge {
    background: #67c23a;
    /* 入库建议用绿色调 */
    color: white;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: bold;
  }
</style>