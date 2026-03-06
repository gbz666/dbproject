<template>
  <div class="app-container" style="padding: 20px">
    <el-card shadow="never" style="margin-bottom: 20px">
      <el-form :inline="true" :model="store.queryParams">
        <el-form-item label="采购单号">
          <el-input
            v-model="store.queryParams.purchaseCode"
            placeholder="cg..."
            clearable
            @keyup.enter="store.fetchPageAction"
          />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input
            v-model="store.queryParams.supplierName"
            placeholder="供应商名称"
            clearable
            style="width: 160px"
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
        <el-form-item>
          <el-button type="primary" @click="store.fetchPageAction">查询</el-button>
          <el-button @click="handleResetQuery">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" @click="handleOpenDialog()">新增进项发票</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table
      v-loading="store.loading"
      :data="store.list"
      border
      stripe
      style="width: 100%"
      @expand-change="handleExpandChange"
    >
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="expand-details">
            <template v-if="expandedDetails[row.id] === undefined && expandLoadingId === row.id">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>加载明细中...</span>
            </template>
            <template v-else-if="(expandedDetails[row.id] ?? []).length">
              <div class="detail-table-label">发票明细（共 {{ (expandedDetails[row.id] ?? []).length }} 项）</div>
              <el-table :data="expandedDetails[row.id] ?? []" border size="small" max-height="280">
                <el-table-column type="index" label="#" width="48" align="center" />
                <el-table-column prop="itemName" label="货物/服务名称" min-width="140" show-overflow-tooltip />
                <el-table-column prop="specification" label="规格型号" width="120" show-overflow-tooltip />
                <el-table-column prop="unit" label="单位" width="72" />
                <el-table-column prop="quantity" label="数量" width="90" align="right">
                  <template #default="{ row: d }">{{ Number(d.quantity).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="unitPrice" label="单价" width="100" align="right">
                  <template #default="{ row: d }">¥{{ Number(d.unitPrice).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="amountExclusiveTax" label="未税金额" width="100" align="right">
                  <template #default="{ row: d }">¥{{ Number(d.amountExclusiveTax).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="taxRate" label="税率(%)" width="80" align="right">
                  <template #default="{ row: d }">{{ (Number(d.taxRate) * 100).toFixed(0) }}</template>
                </el-table-column>
                <el-table-column prop="taxAmount" label="税额" width="90" align="right">
                  <template #default="{ row: d }">¥{{ Number(d.taxAmount).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="amountInclusiveTax" label="含税金额" width="110" align="right">
                  <template #default="{ row: d }">¥{{ Number(d.amountInclusiveTax).toFixed(2) }}</template>
                </el-table-column>
              </el-table>
            </template>
            <template v-else>
              <span class="detail-empty">暂无明细数据，请点击「编辑」补充并保存。</span>
            </template>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="invoiceNo" label="发票号" width="140" fixed />
      <el-table-column prop="purchaseCode" label="采购单号" width="130" />
      <el-table-column prop="invoiceDate" label="开票日期" width="120">
        <template #default="{ row }">{{ row.invoiceDate?.toString().substring(0, 10) }}</template>
      </el-table-column>
      <el-table-column prop="supplierName" label="供应商" min-width="160" show-overflow-tooltip />
      <el-table-column label="明细数" width="100" align="center">
        <template #default="{ row }">共 {{ row.detailCount ?? 0 }} 项</template>
      </el-table-column>
      <el-table-column label="发票金额(含税)" width="140" align="right">
        <template #default="{ row }">¥{{ (row.totalAmount ?? 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="orderDate" label="订单日期" width="120">
        <template #default="{ row }">{{ row.orderDate?.toString().substring(0, 10) }}</template>
      </el-table-column>
      <el-table-column prop="orderNote" label="订单备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleOpenDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除该进项发票吗？" @confirm="handleConfirmDelete(row)">
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
      :title="isEdit ? '修改进项发票' : '新增进项发票'"
      width="960px"
      destroy-on-close
    >
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="采购订单号" prop="purchaseCode">
              <el-input
                v-model="form.purchaseCode"
                placeholder="输入完整采购订单号后失焦自动带出"
                :loading="orderLoading"
                @blur="handlePurchaseCodeBlur"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="公司名称" prop="supplierName">
              <el-input v-model="form.supplierName" placeholder="公司名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票号" prop="invoiceNo">
              <el-input v-model="form.invoiceNo" placeholder="发票编号" />
            </el-form-item>
          </el-col>
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
          <el-col :span="24">
            <el-form-item label="发票备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="1" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="发票明细" required>
          <div style="width: 100%">
            <el-table :data="form.items" border size="small" max-height="320">
              <el-table-column type="index" label="#" width="48" align="center" />
              <el-table-column label="货物/服务名称" min-width="140">
                <template #default="{ row, $index }">
                  <el-input v-model="row.itemName" size="small" placeholder="名称" @blur="() => handleRowCalc($index)" />
                </template>
              </el-table-column>
              <el-table-column label="规格型号" width="120">
                <template #default="{ row, $index }">
                  <el-input v-model="row.specification" size="small" placeholder="规格" />
                </template>
              </el-table-column>
              <el-table-column label="单位" width="72">
                <template #default="{ row }">
                  <el-input v-model="row.unit" size="small" placeholder="单位" />
                </template>
              </el-table-column>
              <el-table-column label="数量" width="100">
                <template #default="{ row, $index }">
                  <el-input-number
                    v-model="row.quantity"
                    :precision="4"
                    :min="0"
                    size="small"
                    controls-position="right"
                    style="width: 100%"
                    @change="() => handleRowCalc($index)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="单价" width="100">
                <template #default="{ row, $index }">
                  <el-input-number
                    v-model="row.unitPrice"
                    :precision="4"
                    :min="0"
                    size="small"
                    controls-position="right"
                    style="width: 100%"
                    @change="() => handleRowCalc($index)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="未税金额" width="100">
                <template #default="{ row, $index }">
                  <el-input-number
                    v-model="row.amountExclusiveTax"
                    :precision="4"
                    :min="0"
                    size="small"
                    controls-position="right"
                    style="width: 100%"
                    @change="() => handleRowCalc($index)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="税率(%)" width="88">
                <template #default="{ $index }">
                  <el-input-number
                    :model-value="taxRateDisplayByIndex($index)"
                    @update:model-value="(v: number) => setTaxRateByIndex($index, v)"
                    :precision="2"
                    :min="0"
                    size="small"
                    controls-position="right"
                    style="width: 100%"
                  />
                </template>
              </el-table-column>
              <el-table-column label="税额" width="90">
                <template #default="{ row, $index }">
                  <el-input-number
                    v-model="row.taxAmount"
                    :precision="4"
                    :min="0"
                    size="small"
                    controls-position="right"
                    style="width: 100%"
                    @change="() => handleRowCalc($index)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="含税金额" width="100">
                <template #default="{ row, $index }">
                  <el-input-number
                    v-model="row.amountInclusiveTax"
                    :precision="4"
                    :min="0"
                    size="small"
                    controls-position="right"
                    style="width: 100%"
                    @change="() => handleRowCalc($index)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" fixed="right" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" size="small" :disabled="form.items.length <= 1" @click="removeItem($index)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain size="small" style="margin-top: 8px" @click="addItem">添加一行</el-button>
          </div>
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
import { Loading } from "@element-plus/icons-vue";
import type { FormInstance, FormRules } from "element-plus";
import { usePurchaseInvoiceStore } from "@/stores/purchaseInvoiceStore";
import type { PurchaseInvoiceDTO, PurchaseInvoiceDetailItemDTO, PurchaseInvoiceQuery } from "@/types/dto";
import type { PurchaseInvoiceVO, PurchaseInvoiceDetailVO } from "@/types/vo";
import { purchaseOrderApi } from "@/api/purchaseOrderApi";
import { purchaseInvoiceApi } from "@/api/purchaseInvoiceApi";

const store = usePurchaseInvoiceStore();

const dialogVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();
const orderLoading = ref(false);
/** 展开行时缓存的明细：id -> details */
const expandedDetails = ref<Record<number, PurchaseInvoiceDetailVO[]>>({});
const expandLoadingId = ref<number | null>(null);

const round4 = (value: number) => {
  if (!Number.isFinite(value)) return 0;
  return Math.round(value * 1e4) / 1e4;
};

function createEmptyItem(): PurchaseInvoiceDetailItemDTO {
  return {
    itemName: "",
    specification: "",
    unit: "",
    quantity: 1,
    unitPrice: 0,
    amountExclusiveTax: 0,
    taxRate: 0,
    taxAmount: 0,
    amountInclusiveTax: 0,
    remark: "",
  };
}

const createEmptyForm = (): PurchaseInvoiceDTO & { supplierName?: string } => ({
  purchaseCode: "",
  invoiceNo: "",
  invoiceDate: new Date().toISOString().split("T")[0] as string,
  supplierName: "",
  supplierId: undefined,
  remark: "",
  items: [createEmptyItem()],
});

const form = reactive<PurchaseInvoiceDTO & { supplierName?: string }>(createEmptyForm());

function taxRateDisplayByIndex(index: number) {
  const row = form.items[index];
  return row ? round4((row.taxRate ?? 0) * 100) : 0;
}
function setTaxRateByIndex(index: number, percent: number) {
  const row = form.items[index];
  if (row) row.taxRate = round4(percent / 100);
  handleRowCalc(index);
}

/** 单行计算：数量/单价/未税/税率/税额/含税联动 */
function handleRowCalc(index: number) {
  const row = form.items[index];
  if (!row) return;
  const qty = Number(row.quantity) || 0;
  const price = Number(row.unitPrice) || 0;
  const rate = Number(row.taxRate) || 0;
  row.amountExclusiveTax = round4(qty * price);
  row.taxAmount = round4(row.amountExclusiveTax * rate);
  row.amountInclusiveTax = round4(row.amountExclusiveTax + (row.taxAmount ?? 0));
}

function addItem() {
  form.items.push(createEmptyItem());
}
function removeItem(index: number) {
  if (form.items.length <= 1) return;
  form.items.splice(index, 1);
}

const formRules: FormRules = {
  purchaseCode: [{ required: true, message: "请输入采购订单号", trigger: "blur" }],
  supplierName: [{ required: true, message: "请输入公司名称", trigger: "blur" }],
  invoiceNo: [{ required: true, message: "请输入发票号", trigger: "blur" }],
  invoiceDate: [{ required: true, message: "请选择开票日期", trigger: "change" }],
  items: [
    {
      required: true,
      validator: (_rule: unknown, _value: unknown, callback: (e?: Error) => void) => {
        if (!form.items.length) {
          callback(new Error("至少保留一行明细"));
        } else if (form.items.some((r) => !r.itemName?.trim())) {
          callback(new Error("请填写每行的货物/服务名称"));
        } else if (form.items.some((r) => (Number(r.quantity) || 0) <= 0)) {
          callback(new Error("请填写有效的数量"));
        } else if (form.items.some((r) => (Number(r.amountInclusiveTax) || 0) <= 0)) {
          callback(new Error("请填写有效的含税金额"));
        } else {
          callback();
        }
      },
      trigger: "change",
    },
  ],
};

const handleResetQuery = () => {
  const qp = store.queryParams as PurchaseInvoiceQuery;
  qp.pageNum = 1;
  qp.pageSize = 10;
  qp.supplierName = "";
  qp.productModel = "";
  qp.purchaseCode = "";
  qp.itemName = "";
  store.fetchPageAction();
};

/** 展开行时拉取该发票的明细（用于在列表中展示「明细在哪」） */
const handleExpandChange = async (_row: PurchaseInvoiceVO, expandedRows: PurchaseInvoiceVO[]) => {
  for (const row of expandedRows) {
    const id = Number(row.id);
    if (!Number.isFinite(id) || expandedDetails.value[id] !== undefined) continue;
    expandLoadingId.value = id;
    try {
      const vo = await purchaseInvoiceApi.getById(id);
      expandedDetails.value[id] = vo?.details ?? [];
    } catch (e: any) {
      expandedDetails.value[id] = [];
      if (e?.status !== 404) ElMessage.error(e?.message ?? "加载明细失败");
    } finally {
      expandLoadingId.value = null;
    }
  }
};

/** 默认税率 13%，用于从订单带出时的计算 */
const DEFAULT_TAX_RATE = 0.13;

const handlePurchaseCodeBlur = async () => {
  const code = form.purchaseCode?.trim();
  if (!code) return;
  orderLoading.value = true;
  try {
    const page = await purchaseOrderApi.fetchPage({
      pageNum: 1,
      pageSize: 10,
      purchaseOrderCode: code,
    });
    const list = page?.list ?? [];
    const order = list.find((o: { purchaseCode: string }) => o.purchaseCode === code) ?? list[0];
    if (!order) return;
    if (order.purchaseCode !== code) {
      ElMessage.warning("未找到完全匹配的采购订单，请核对采购订单号");
      return;
    }
    form.supplierName = order.supplierName ?? "";
    form.supplierId = order.supplierId;
    const orderItems = order.items ?? [];
    if (orderItems.length > 0) {
      form.items = orderItems.map((oi: { productName?: string; quantity?: number; unitPrice?: number }) => {
        const qty = Number(oi.quantity ?? 0) || 0;
        const price = Number(oi.unitPrice ?? 0) || 0;
        const amountExclusiveTax = round4(qty * price);
        const taxAmount = round4(amountExclusiveTax * DEFAULT_TAX_RATE);
        const amountInclusiveTax = round4(amountExclusiveTax + taxAmount);
        return {
          itemName: oi.productName ?? "",
          specification: "",
          unit: "",
          quantity: qty,
          unitPrice: price,
          amountExclusiveTax,
          taxRate: DEFAULT_TAX_RATE,
          taxAmount,
          amountInclusiveTax,
          remark: "",
        };
      });
    } else {
      form.items = [createEmptyItem()];
    }
  } catch (e: any) {
    if (e?.message) ElMessage.error(e.message);
  } finally {
    orderLoading.value = false;
  }
};

const handleOpenDialog = async (row?: PurchaseInvoiceVO) => {
  isEdit.value = !!row;
  if (row) {
    const id = Number(row.id);
    if (!Number.isFinite(id)) {
      ElMessage.error("发票 ID 无效");
      return;
    }
    try {
      const vo = await purchaseInvoiceApi.getById(id);
      if (!vo?.id) {
        ElMessage.error("未找到该进项发票");
        return;
      }
      form.id = vo.id;
      form.purchaseCode = vo.purchaseCode ?? "";
      form.invoiceNo = vo.invoiceNo ?? "";
      form.invoiceDate = vo.invoiceDate?.toString().substring(0, 10) ?? "";
      form.supplierName = vo.supplierName ?? "";
      form.supplierId = vo.supplierId;
      form.remark = vo.remark ?? "";
      const details = vo.details ?? [];
      form.items =
        details.length > 0
          ? details.map((d) => ({
              itemName: d.itemName ?? "",
              specification: d.specification ?? "",
              unit: d.unit ?? "",
              quantity: Number(d.quantity) ?? 0,
              unitPrice: Number(d.unitPrice) ?? 0,
              amountExclusiveTax: Number(d.amountExclusiveTax) ?? 0,
              taxRate: Number(d.taxRate) ?? 0,
              taxAmount: Number(d.taxAmount) ?? 0,
              amountInclusiveTax: Number(d.amountInclusiveTax) ?? 0,
              remark: d.remark ?? "",
            }))
          : [createEmptyItem()];
    } catch (e: any) {
      const msg = e?.status === 404 ? "未找到该进项发票（可能已被删除），请刷新列表" : (e?.message ?? "加载进项发票失败");
      ElMessage.error(msg);
      return;
    }
  } else {
    form.id = undefined;
    Object.assign(form, createEmptyForm());
  }
  dialogVisible.value = true;
};

const handleSave = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    submitLoading.value = true;
    const dto: PurchaseInvoiceDTO = {
      purchaseCode: form.purchaseCode,
      invoiceNo: form.invoiceNo,
      invoiceDate: form.invoiceDate,
      supplierId: form.supplierId,
      remark: form.remark,
      items: form.items.map((r) => ({
        itemName: r.itemName,
        specification: r.specification,
        unit: r.unit,
        quantity: r.quantity,
        unitPrice: r.unitPrice,
        amountExclusiveTax: r.amountExclusiveTax,
        taxRate: r.taxRate,
        taxAmount: r.taxAmount,
        amountInclusiveTax: r.amountInclusiveTax,
        remark: r.remark,
      })),
    };
    if (isEdit.value && form.id != null) {
      await store.submitInvoiceAction({ ...dto, id: form.id });
    } else {
      await store.submitInvoiceAction(dto);
    }
    ElMessage.success(isEdit.value ? "修改成功" : "新增成功");
    if (form.id != null) delete expandedDetails.value[form.id];
    dialogVisible.value = false;
  } catch (e: any) {
    if (e?.message) ElMessage.error(e.message);
  } finally {
    submitLoading.value = false;
  }
};

const handleConfirmDelete = (row: PurchaseInvoiceVO) => {
  ElMessageBox.confirm("确定删除该进项发票吗？", "提示", { type: "warning" })
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
.expand-details {
  padding: 12px 24px;
  background: var(--el-fill-color-light);
}
.detail-table-label {
  margin-bottom: 8px;
  font-weight: 500;
  color: var(--el-text-color-regular);
}
.detail-empty {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
