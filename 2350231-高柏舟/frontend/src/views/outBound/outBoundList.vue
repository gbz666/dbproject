<template>
  <div class="app-container" style="padding: 20px; background-color: #f5f7fa; min-height: 100vh;">
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="store.queryParams">
        <el-form-item label="销售单号">
          <el-input v-model="store.queryParams.salesOrderCode" placeholder="输入单号" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="store.queryParams.productName" placeholder="输入名称" clearable @clear="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="openDialog()">创建出库单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="store.loading" :data="store.outbounds" border stripe style="width: 100%">
      <el-table-column prop="salesOrderCode" label="销售单号" width="150" align="center" />
      <el-table-column prop="customerName" label="客户" min-width="120" />
      <el-table-column prop="productCode" label="产品型号" width="120" />
      <el-table-column prop="orderQuantity" label="订货数" width="90" align="right" />
      <el-table-column prop="subTotal" label="出库数" width="90" align="right" />
      <el-table-column prop="pendingQuantity" label="未交数" width="90" align="right">
        <template #default="{ row }">
          <span :style="{ color: row.pendingQuantity > 0 ? '#F56C6C' : '#67C23A', fontWeight: 'bold' }">
            {{ row.pendingQuantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="serialNumbers" label="序列号汇总" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除该记录？" @confirm="handleDelete(row.id)">
            <template #reference><el-button link type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="store.queryParams.pageNum"
        v-model:page-size="store.queryParams.pageSize"
        :total="store.total"
        layout="total, prev, pager, next, jumper"
        @current-change="store.loadPage"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑出库单' : '新增出库单'" width="1000px" top="5vh">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="销售单号" prop="salesOrderCode">
              <el-input v-model="form.salesOrderCode" placeholder="关联销售订单号" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出库日期" prop="outboundDate">
              <el-date-picker v-model="form.outboundDate" type="date" style="width: 100%" placeholder="选择日期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="单据备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>

        <el-divider content-position="left">产品出库明细</el-divider>
        <div v-for="(item, pIndex) in form.items" :key="pIndex" class="product-item-card">
          <div class="product-header">
            <div style="display: flex; align-items: center; gap: 10px; flex: 1">
              <span class="index-badge">{{ pIndex + 1 }}</span>
              <el-select
                v-model="item.productCode"
                filterable
                remote
                reserve-keyword
                placeholder="搜索产品型号"
                :remote-method="handleProductSearch"
                :loading="utilStore.productLoading"
                style="width: 250px"
              >
                <el-option
                  v-for="p in utilStore.productList"
                  :key="p.code"
                  :label="p.code"
                  :value="p.code"
                >
                  <span style="float: left">{{ p.code }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ p.name }}</span>
                </el-option>
              </el-select>
              <el-input v-model="item.serialNumbers" placeholder="录入序列号 (逗号分隔)" style="flex: 1" />
            </div>
            <el-button type="danger" link icon="Delete" @click="removeItem(pIndex)">删除产品</el-button>
          </div>

          <el-table :data="item.warehouseDetails" size="small" border style="margin-top: 10px">
            <el-table-column label="出库仓库" width="300">
              <template #default="scope">
                <el-select v-model="scope.row.warehouseId" placeholder="选择仓库" style="width: 100%">
                  <el-option v-for="w in warehouseOptions" :key="w.id" :label="w.name" :value="w.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="出库数量" width="180">
              <template #default="scope">
                <el-input-number v-model="scope.row.quantity" :min="1" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center">
              <template #default="scope">
                <el-button type="danger" link @click="item.warehouseDetails.splice(scope.$index, 1)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button type="primary" link icon="Plus" @click="addWarehouse(item)" style="margin-top: 5px">
            添加仓库分配
          </el-button>
        </div>
        
        <el-button type="primary" icon="Plus" @click="addProduct" style="margin-top: 10px">添加出库产品</el-button>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="store.loading" @click="submitForm">保存单据</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, type FormInstance } from 'element-plus';
import { useOutboundStore } from '@/stores/outBoundStore';
import { useUtilStore } from '@/stores/utilStore';
import type { OutboundOrderDTO } from '@/types/dto'; // 假设你的 DTO 在这里

const store = useOutboundStore();
const utilStore = useUtilStore();
const formRef = ref<FormInstance>();
const dialogVisible = ref(false);
const isEdit = ref(false);

// 仓库数据建议后续通过 utilStore 获取
const warehouseOptions = [{ id: 1, name: '上海' }, { id: 2, name: '天津' }, { id: 3, name: '深圳' }];

// 定义内部表单状态（UI 交互用，id 允许 null，日期用 Date 对象）
const form = reactive({
  id: null as number | null,
  salesOrderCode: '',
  outboundDate: new Date(),
  remark: '',
  items: [] as any[]
});

const formRules = {
  salesOrderCode: [{ required: true, message: '必须关联销售单号', trigger: 'blur' }],
  outboundDate: [{ required: true, message: '请选择出库日期', trigger: 'change' }]
};

onMounted(() => store.loadPage());

const handleSearch = () => { store.queryParams.pageNum = 1; store.loadPage(); };
const resetFilters = () => {
  store.queryParams.salesOrderCode = "";
  store.queryParams.productName = "";
  handleSearch();
};

const handleProductSearch = (query: string) => {
  if (query) utilStore.searchProductsAction(query);
};

const openDialog = (row?: any) => {
  isEdit.value = !!row;
  if (row) {
    form.id = row.id;
    form.salesOrderCode = row.salesOrderCode;
    form.outboundDate = row.firstOutboundDate ? new Date(row.firstOutboundDate) : new Date();
    form.remark = row.orderRemark || '';
    
    // 编辑回显产品型号下拉
    utilStore.productList = [{ id: 0, code: row.productCode, name: row.productName } as any];
    
    form.items = [{
      productCode: row.productCode,
      serialNumbers: row.serialNumbers,
      warehouseDetails: row.warehouseDetails ? [...row.warehouseDetails] : [{ warehouseId: 1, quantity: 1 }]
    }];
  } else {
    Object.assign(form, { id: null, salesOrderCode: '', outboundDate: new Date(), remark: '', items: [] });
    addProduct();
  }
  dialogVisible.value = true;
};

const addProduct = () => {
  form.items.push({ productCode: '', serialNumbers: '', warehouseDetails: [{ warehouseId: 1, quantity: 1 }] });
};
const removeItem = (index: number) => form.items.splice(index, 1);
const addWarehouse = (item: any) => item.warehouseDetails.push({ warehouseId: 1, quantity: 1 });

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (!valid) return;
    
    try {
      // 核心：将表单数据转换为满足 OutboundOrderDTO 的结构
      const submitData: OutboundOrderDTO = {
        // 使用三元运算符处理 id: 如果是 null 则传 undefined (不传该 key)
        id: form.id === null ? undefined : form.id,
        
        salesOrderCode: form.salesOrderCode,
        // 将日期格式化为字符串 (后端 Date 对应的 DTO 类型)
        outboundDate: form.outboundDate.toISOString().split('T')[0] as any, 
        remark: form.remark,
        items: form.items.map(item => ({
          productCode: item.productCode,
          serialNumbers: item.serialNumbers,
          warehouseDetails: item.warehouseDetails
        }))
      };
      await store.saveOutboundAction(submitData as any, 2); 
      ElMessage.success('保存成功');
      dialogVisible.value = false;
    } catch (e: any) {
      ElMessage.error(e.message || '保存失败');
    }
  });
};

const handleDelete = (id: number) => store.deleteItem(id, 2);
</script>

<style scoped>
.product-item-card {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  background-color: #fff;
}
.product-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.index-badge {
  background: #409eff;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}
</style>