<template>
  <div class="app-container" style="padding: 20px">
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="store.queryParams">
        <el-form-item label="收款单号">
          <el-input
            v-model="store.queryParams.receiptNo"
            placeholder="收款单号"
            clearable
            @keyup.enter="store.fetchPageAction"
          />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input
            v-model="store.queryParams.customerName"
            placeholder="客户名称"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="收款方式">
          <el-select
            v-model="store.queryParams.method"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <el-option label="银行转账" value="bank" />
            <el-option label="现金" value="cash" />
            <el-option label="支票" value="check" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="store.fetchPageAction">查询</el-button>
          <el-button @click="handleResetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleOpenDialog()">新增收款</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table v-loading="store.loading" :data="store.list" border stripe style="width: 100%">
      <el-table-column prop="receiptNo" label="收款单号" width="160" fixed />
      <el-table-column prop="customerName" label="客户" min-width="160" show-overflow-tooltip />
      <el-table-column prop="salesInvoiceNo" label="关联销项发票" width="160" show-overflow-tooltip />
      <el-table-column label="收款金额" width="140" align="right">
        <template #default="{ row }">¥{{ Number(row.amount ?? 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="receiptDate" label="收款日期" width="120">
        <template #default="{ row }">{{ row.receiptDate?.substring(0, 10) }}</template>
      </el-table-column>
      <el-table-column label="收款方式" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="methodTagType(row.method)" size="small">{{ methodLabel(row.method) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleOpenDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除该收款记录吗？" @confirm="handleDelete(row.id)">
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
      :title="isEdit ? '修改收款' : '新增收款'"
      width="600px"
      destroy-on-close
    >
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="110px">
        <el-form-item label="收款单号" prop="receiptNo">
          <el-input v-model="form.receiptNo" placeholder="收款单编号" />
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select
            v-model="form.customerId"
            filterable
            remote
            reserve-keyword
            placeholder="输入客户名称搜索"
            :remote-method="utilStore.searchCustomersAction"
            :loading="utilStore.customerLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in utilStore.customerList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联销项发票">
          <el-input v-model.number="form.salesInvoiceId" placeholder="销项发票ID（选填）" />
        </el-form-item>
        <el-form-item label="收款金额" prop="amount">
          <el-input-number v-model="form.amount" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="收款日期" prop="receiptDate">
          <el-date-picker v-model="form.receiptDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="收款方式" prop="method">
          <el-select v-model="form.method" placeholder="请选择" style="width: 100%">
            <el-option label="银行转账" value="bank" />
            <el-option label="现金" value="cash" />
            <el-option label="支票" value="check" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSave">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { usePaymentReceiptStore } from "@/stores/paymentReceiptStore";
import { useUtilStore } from "@/stores/utilStore";
import type { PaymentReceiptDTO } from "@/types/dto";
import type { PaymentReceiptVO } from "@/types/vo";

const store = usePaymentReceiptStore();
const utilStore = useUtilStore();

const dialogVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();

const METHOD_MAP: Record<string, string> = { bank: "银行转账", cash: "现金", check: "支票", other: "其他" };
const methodLabel = (m?: string) => (m ? METHOD_MAP[m] ?? m : "-");
const methodTagType = (m?: string): "" | "success" | "warning" | "info" => {
  if (m === "bank") return "";
  if (m === "cash") return "success";
  if (m === "check") return "warning";
  return "info";
};

const createEmptyForm = (): PaymentReceiptDTO => ({
  id: undefined,
  receiptNo: "",
  customerId: undefined,
  salesInvoiceId: undefined,
  amount: 0,
  receiptDate: new Date().toISOString().split("T")[0],
  method: "bank",
  remark: "",
});

const form = reactive<PaymentReceiptDTO>(createEmptyForm());

const formRules: FormRules = {
  receiptNo: [{ required: true, message: "请输入收款单号", trigger: "blur" }],
  customerId: [{ required: true, message: "请输入客户ID", trigger: "blur" }],
  amount: [{ required: true, message: "请输入收款金额", trigger: "blur" }],
  receiptDate: [{ required: true, message: "请选择收款日期", trigger: "change" }],
  method: [{ required: true, message: "请选择收款方式", trigger: "change" }],
};

const handleResetQuery = () => {
  store.queryParams.pageNum = 1;
  store.queryParams.customerName = "";
  store.queryParams.receiptNo = "";
  store.queryParams.method = "";
  store.fetchPageAction();
};

const handleOpenDialog = (row?: PaymentReceiptVO) => {
  isEdit.value = !!row;
  if (row) {
    form.id = row.id;
    form.receiptNo = row.receiptNo ?? "";
    form.customerId = row.customerId;
    form.salesInvoiceId = row.salesInvoiceId;
    form.amount = row.amount ?? 0;
    form.receiptDate = row.receiptDate?.substring(0, 10) ?? "";
    form.method = row.method ?? "bank";
    form.remark = row.remark ?? "";
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
    await store.submitAction({ ...form });
    ElMessage.success(isEdit.value ? "修改成功" : "新增成功");
    dialogVisible.value = false;
  } catch (e: any) {
    if (e instanceof Error && e.message) ElMessage.error(e.message);
  } finally {
    submitLoading.value = false;
  }
};

const handleDelete = async (id: number) => {
  await store.deleteAction(id);
  ElMessage.success("已删除");
};

onMounted(() => {
  store.fetchPageAction();
});
</script>
