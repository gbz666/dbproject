<template>
  <div class="app-container" style="padding: 20px">
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="store.queryParams">
        <el-form-item label="销售订单号">
          <el-input
            v-model="store.queryParams.salesOrderCode"
            placeholder="xs..."
            clearable
            @keyup.enter="store.fetchPageAction"
          />
        </el-form-item>
        <el-form-item label="公司名称">
          <el-input
            v-model="store.queryParams.companyName"
            placeholder="客户名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="货物名称">
          <el-input
            v-model="store.queryParams.itemName"
            placeholder="货物/服务名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input
            v-model="store.queryParams.productModel"
            placeholder="规格型号"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="发票编码">
          <el-input
            v-model="store.queryParams.invoiceNo"
            placeholder="发票编码"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="store.fetchPageAction">查询</el-button>
          <el-button @click="handleResetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleOpenDialog()">新增销项发票</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table
      v-loading="store.loading"
      :data="store.list"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="invoiceNo" label="发票编码" width="140" fixed />
      <el-table-column prop="salesOrderCode" label="销售订单号" width="140" />
      <el-table-column prop="invoiceDate" label="开票日期" width="120">
        <template #default="{ row }">{{ row.invoiceDate?.toString().substring(0, 10) }}</template>
      </el-table-column>
      <el-table-column prop="companyName" label="公司名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="itemName" label="货物/服务名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="specification" label="规格型号" min-width="140" show-overflow-tooltip />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="quantity" label="数量" width="100" align="right">
        <template #default="{ row }">{{ row.quantity != null ? Number(row.quantity).toFixed(6) : "" }}</template>
      </el-table-column>
      <el-table-column prop="unitPriceInclusiveTax" label="含税单价" width="120" align="right">
        <template #default="{ row }">¥{{ row.unitPriceInclusiveTax != null ? Number(row.unitPriceInclusiveTax).toFixed(6) : "" }}</template>
      </el-table-column>
      <el-table-column prop="amountExclusiveTax" label="未税金额" width="130" align="right">
        <template #default="{ row }">¥{{ row.amountExclusiveTax != null ? Number(row.amountExclusiveTax).toFixed(6) : "" }}</template>
      </el-table-column>
      <el-table-column prop="taxAmount" label="税额" width="110" align="right">
        <template #default="{ row }">¥{{ row.taxAmount != null ? Number(row.taxAmount).toFixed(6) : "" }}</template>
      </el-table-column>
      <el-table-column prop="amountInclusiveTax" label="含税金额" width="130" align="right">
        <template #default="{ row }">¥{{ row.amountInclusiveTax != null ? Number(row.amountInclusiveTax).toFixed(6) : "" }}</template>
      </el-table-column>
      <el-table-column prop="avgInvoiceDays" label="平均开票时间(天)" width="150" align="right" />
      <el-table-column prop="pendingInvoiceAmount" label="未开金额" width="130" align="right">
        <template #default="{ row }">¥{{ row.pendingInvoiceAmount?.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleOpenDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除该销项发票吗？" @confirm="handleConfirmDelete(row)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; display: flex; justify-content: flex-end">
      <el-pagination
        v-model:current-page="store.queryParams.pageNum"
        v-model:page-size="store.queryParams.pageSize"
        :total="store.total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="store.fetchPageAction"
        @size-change="store.fetchPageAction"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改销项发票' : '新增销项发票'"
      width="900px"
      destroy-on-close
    >
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="销售订单号" prop="salesOrderCode">
              <el-input
                v-model="form.salesOrderCode"
                placeholder="输入完整订单号后失焦自动带出公司、数量等"
                :loading="orderLoading"
                @blur="handleSalesOrderCodeBlur"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票编码" prop="invoiceNo">
              <el-input v-model="form.invoiceNo" placeholder="发票编号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="公司名称">
          <el-input v-model="form.companyName" placeholder="根据销售订单号自动带出" readonly />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开票日期" prop="invoiceDate">
              <el-date-picker
                v-model="form.invoiceDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" placeholder="个/件/台..." />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="货物/服务名称" prop="itemName">
          <el-input v-model="form.itemName" />
        </el-form-item>
        <el-form-item label="规格型号" prop="specification">
          <el-input v-model="form.specification" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数量" prop="quantity">
              <el-input-number
                v-model="form.quantity"
                :precision="6"
                :min="0.000001"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="含税单价" prop="unitPriceInclusiveTax">
              <el-input-number
                v-model="form.unitPriceInclusiveTax"
                :precision="6"
                :min="0"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="未税金额" prop="amountExclusiveTax">
              <el-input-number
                v-model="form.amountExclusiveTax"
                :precision="6"
                :min="0"
                controls-position="right"
                class="invoice-amount-input"
              />
              <div class="formula-hint">默认: 含税总金额÷1.13</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="税额" prop="taxAmount">
              <el-input-number
                v-model="form.taxAmount"
                :precision="6"
                :min="0"
                controls-position="right"
                class="invoice-amount-input"
              />
              <div class="formula-hint">默认: 未税金额×0.13</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="含税总金额" prop="amountInclusiveTax">
              <el-input-number
                v-model="form.amountInclusiveTax"
                :precision="6"
                :min="0"
                controls-position="right"
                class="invoice-amount-input"
              />
              <div class="formula-hint">默认: 数量×含税单价</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div style="text-align: right">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSave">
            确认保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { useSalesInvoiceStore } from "@/stores/salesInvoiceStore";
import { salesOrderApi } from "@/api/salesOrderApi";
import type { SalesInvoiceDTO, SalesInvoiceQuery } from "@/types/dto";
import type { SalesInvoiceVO } from "@/types/vo";

const store = useSalesInvoiceStore();
const orderLoading = ref(false);

const dialogVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();

const createEmptyForm = (): SalesInvoiceDTO => ({
  salesOrderCode: "",
  invoiceNo: "",
  invoiceDate: new Date().toISOString().slice(0, 10),
  companyName: "",
  itemName: "",
  specification: "",
  unit: "",
  quantity: 1,
  unitPriceInclusiveTax: 0,
  amountInclusiveTax: 0,
  amountExclusiveTax: 0,
  taxAmount: 0,
  remark: "",
});

const form = reactive<SalesInvoiceDTO>(createEmptyForm());

const formRules = reactive<FormRules>({
  salesOrderCode: [{ required: true, message: "请输入销售订单号", trigger: "blur" }],
  invoiceNo: [{ required: true, message: "请输入发票编码", trigger: "blur" }],
  invoiceDate: [{ required: true, message: "请选择开票日期", trigger: "change" }],
  itemName: [{ required: true, message: "请输入货物/服务名称", trigger: "blur" }],
  quantity: [{ required: true, message: "请输入数量", trigger: "blur" }],
  amountInclusiveTax: [{ required: true, message: "请输入含税总金额", trigger: "blur" }],
});

/** 根据销售订单号拉取订单并自动填充：公司名称（只读）、数量、含税单价、货物名称及税额/含税总金额/未税金额默认值 */
const handleSalesOrderCodeBlur = async () => {
  const code = form.salesOrderCode?.trim();
  if (!code) return;
  orderLoading.value = true;
  try {
    const page = await salesOrderApi.fetchPage({
      pageNum: 1,
      pageSize: 10,
      salesOrderCode: code,
    });
    const list = page?.list ?? [];
    const order = list.find((o) => o.orderCode === code) ?? list[0];
    if (!order) {
      return;
    }
    if (order.orderCode !== code) {
      ElMessage.warning("未找到完全匹配的销售订单，请核对订单号");
      return;
    }
    form.companyName = order.customerName ?? "";
    const item = order.items?.[0];
    if (item) {
      const qty = Number((item as any).quantity ?? 0) || 0;
      const unitPrice = Number((item as any).unitPrice ?? (item as any).unit_price ?? 0) || 0;
      form.quantity = qty > 0 ? qty : form.quantity;
      form.unitPriceInclusiveTax = unitPrice;
      const amountInclusive = form.quantity * form.unitPriceInclusiveTax;
      form.amountInclusiveTax = Math.round(amountInclusive * 1e6) / 1e6;
      form.amountExclusiveTax = Math.round((form.amountInclusiveTax / 1.13) * 1e6) / 1e6;
      form.taxAmount = Math.round(form.amountExclusiveTax * 0.13 * 1e6) / 1e6;
    }
  } catch (e: any) {
    if (e?.message) ElMessage.error(e.message);
  } finally {
    orderLoading.value = false;
  }
};

const handleResetQuery = () => {
  const qp = store.queryParams as SalesInvoiceQuery;
  qp.pageNum = 1;
  qp.pageSize = 10;
  qp.companyName = "";
  qp.productModel = "";
  qp.salesOrderCode = "";
  qp.itemName = "";
  qp.invoiceNo = "";
  store.fetchPageAction();
};

const handleOpenDialog = (row?: SalesInvoiceVO) => {
  isEdit.value = !!row;
  if (row) {
    Object.assign(form, {
      id: row.id,
      salesOrderCode: row.salesOrderCode,
      invoiceNo: row.invoiceNo,
      invoiceDate: row.invoiceDate?.toString().substring(0, 10),
      companyName: row.companyName,
      itemName: row.itemName,
      specification: row.specification,
      unit: row.unit,
      quantity: row.quantity,
      unitPriceInclusiveTax: row.unitPriceInclusiveTax,
      amountInclusiveTax: row.amountInclusiveTax,
      amountExclusiveTax: row.amountExclusiveTax,
      taxAmount: row.taxAmount,
      remark: row.remark,
    });
  } else {
    Object.assign(form, createEmptyForm());
  }
  dialogVisible.value = true;
};

const handleSave = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitLoading.value = true;
    await store.submitInvoiceAction({ ...form });
    ElMessage.success(isEdit.value ? "修改成功" : "新增成功");
    dialogVisible.value = false;
  } catch (e: any) {
    if (e?.message) {
      ElMessage.error(e.message);
    }
  } finally {
    submitLoading.value = false;
  }
};

const handleConfirmDelete = (row: SalesInvoiceVO) => {
  ElMessageBox.confirm("确定删除该销项发票吗？", "提示", { type: "warning" })
    .then(async () => {
      await store.deleteInvoiceAction(row.id);
      ElMessage.success("已删除");
    })
    .catch(() => {});
};

onMounted(() => {
  store.fetchPageAction();
});
</script>

<style scoped>
.formula-hint {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
  margin-top: 4px;
  line-height: 1.2;
}
.invoice-amount-input {
  width: 100%;
  min-width: 200px;
}
.invoice-amount-input :deep(.el-input__wrapper) {
  min-width: 200px;
}
</style>

