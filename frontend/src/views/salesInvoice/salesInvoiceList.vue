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
      @expand-change="handleExpandChange"
    >
      <!-- 明细展开 -->
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
                <el-table-column prop="unitPriceInclusiveTax" label="含税单价(含税)" width="110" align="right">
                  <template #default="{ row: d }">¥{{ Number(d.unitPriceInclusiveTax).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="amountExclusiveTax" label="未税金额" width="110" align="right">
                  <template #default="{ row: d }">¥{{ Number(d.amountExclusiveTax).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="taxAmount" label="税额" width="100" align="right">
                  <template #default="{ row: d }">¥{{ Number(d.taxAmount).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="amountInclusiveTax" label="含税金额" width="120" align="right">
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

      <!-- 总览列：订单号 / 日期 / 公司 / 金额汇总 / 编码 -->
      <el-table-column prop="invoiceNo" label="发票编码" width="140" fixed />
      <el-table-column prop="salesOrderCode" label="销售订单号" width="140" />
      <el-table-column prop="invoiceDate" label="开票日期" width="120">
        <template #default="{ row }">{{ row.invoiceDate?.toString().substring(0, 10) }}</template>
      </el-table-column>
      <el-table-column prop="companyName" label="公司名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="amountInclusiveTax" label="含税总金额" width="150" align="right">
        <template #default="{ row }">¥{{ row.amountInclusiveTax != null ? Number(row.amountInclusiveTax).toFixed(2) : "" }}</template>
      </el-table-column>
      <el-table-column prop="amountExclusiveTax" label="未税销售金额" width="150" align="right">
        <template #default="{ row }">¥{{ row.amountExclusiveTax != null ? Number(row.amountExclusiveTax).toFixed(2) : "" }}</template>
      </el-table-column>
      <el-table-column prop="taxAmount" label="税额" width="130" align="right">
        <template #default="{ row }">¥{{ row.taxAmount != null ? Number(row.taxAmount).toFixed(2) : "" }}</template>
      </el-table-column>
      <el-table-column prop="pendingInvoiceAmount" label="未开金额" width="130" align="right">
        <template #default="{ row }">
          <span :style="{ color: (row.pendingInvoiceAmount ?? 0) < 0 ? '#F56C6C' : undefined }">
            ¥{{ row.pendingInvoiceAmount != null ? Number(row.pendingInvoiceAmount).toFixed(2) : "" }}
          </span>
        </template>
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
      width="960px"
      destroy-on-close
    >
      <el-form :model="form" ref="formRef" :rules="formRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="销售订单号" prop="salesOrderCode">
              <el-input
                v-model="form.salesOrderCode"
                placeholder="输入完整订单号后失焦自动带出"
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
          <el-col :span="12">
            <el-form-item label="公司名称" prop="companyName">
              <el-input v-model="form.companyName" placeholder="根据销售订单号自动带出" readonly />
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
            <el-form-item label="备注">
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
                <template #default="{ row }">
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
              <el-table-column label="含税单价" width="110">
                <template #default="{ row, $index }">
                  <el-input-number
                    v-model="row.unitPriceInclusiveTax"
                    :precision="4"
                    :min="0"
                    size="small"
                    controls-position="right"
                    style="width: 100%"
                    @change="() => handleRowCalc($index)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="未税金额" width="110">
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
              <el-table-column label="税额" width="100">
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
              <el-table-column label="含税金额" width="120">
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
                  <el-button
                    link
                    type="danger"
                    size="small"
                    :disabled="form.items.length <= 1"
                    @click="removeItem($index)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain size="small" style="margin-top: 8px" @click="addItem">
              添加一行
            </el-button>
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
import { useSalesInvoiceStore } from "@/stores/salesInvoiceStore";
import { salesOrderApi } from "@/api/salesOrderApi";
import { salesInvoiceApi } from "@/api/salesInvoiceApi";
import type { SalesInvoiceDTO, SalesInvoiceDetailItemDTO, SalesInvoiceQuery } from "@/types/dto";
import type { SalesInvoiceVO } from "@/types/vo";

const store = useSalesInvoiceStore();
const orderLoading = ref(false);

const dialogVisible = ref(false);
const isEdit = ref(false);
const submitLoading = ref(false);
const formRef = ref<FormInstance>();

// 展开行缓存的明细：id -> items
const expandedDetails = ref<Record<number, SalesInvoiceDetailItemDTO[]>>({});
const expandLoadingId = ref<number | null>(null);

function createEmptyItem(): SalesInvoiceDetailItemDTO {
  return {
    itemName: "",
    specification: "",
    unit: "",
    quantity: 1,
    unitPriceInclusiveTax: 0,
    amountExclusiveTax: 0,
    taxAmount: 0,
    amountInclusiveTax: 0,
    remark: "",
  };
}

const createEmptyForm = (): SalesInvoiceDTO => ({
  salesOrderCode: "",
  invoiceNo: "",
  invoiceDate: new Date().toISOString().slice(0, 10),
  companyName: "",
  remark: "",
  items: [createEmptyItem()],
});

const form = reactive<SalesInvoiceDTO>(createEmptyForm());

const formRules = reactive<FormRules>({
  salesOrderCode: [{ required: true, message: "请输入销售订单号", trigger: "blur" }],
  invoiceNo: [{ required: true, message: "请输入发票编码", trigger: "blur" }],
  invoiceDate: [{ required: true, message: "请选择开票日期", trigger: "change" }],
  items: [
    {
      required: true,
      validator: (_rule: unknown, _value: unknown, callback: (e?: Error) => void) => {
        if (!form.items?.length) {
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
});

const DEFAULT_TAX_RATE = 0.13;

function handleRowCalc(index: number) {
  const row = form.items?.[index];
  if (!row) return;
  const qty = Number(row.quantity) || 0;
  const price = Number(row.unitPriceInclusiveTax) || 0;
  const amountInclusive = qty * price;
  row.amountInclusiveTax = Math.round(amountInclusive * 1e4) / 1e4;
  row.amountExclusiveTax = Math.round((row.amountInclusiveTax / (1 + DEFAULT_TAX_RATE)) * 1e4) / 1e4;
  row.taxAmount = Math.round(row.amountExclusiveTax * DEFAULT_TAX_RATE * 1e4) / 1e4;
}

function addItem() {
  if (!form.items) {
    form.items = [];
  }
  form.items.push(createEmptyItem());
}

function removeItem(index: number) {
  if (!form.items || form.items.length <= 1) return;
  form.items.splice(index, 1);
}

/** 根据销售订单号拉取订单并自动填充：公司名称（只读）、明细列表及默认金额 */
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
    const orderItems = order.items ?? [];
    if (orderItems.length > 0) {
      form.items = orderItems.map((oi: { productName?: string; quantity?: number; unitPrice?: number }) => {
        const qty = Number(oi.quantity ?? 0) || 0;
        const priceInclusive = Number(oi.unitPrice ?? 0) || 0;
        const amountInclusive = qty * priceInclusive;
        const amountExclusive = amountInclusive / (1 + DEFAULT_TAX_RATE);
        const taxAmount = amountExclusive * DEFAULT_TAX_RATE;
        return {
          itemName: oi.productName ?? "",
          specification: "",
          unit: "",
          quantity: qty,
          unitPriceInclusiveTax: priceInclusive,
          amountExclusiveTax: Math.round(amountExclusive * 1e4) / 1e4,
          taxAmount: Math.round(taxAmount * 1e4) / 1e4,
          amountInclusiveTax: Math.round(amountInclusive * 1e4) / 1e4,
          remark: "",
        } as SalesInvoiceDetailItemDTO;
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

/** 展开行时拉取该发票的明细（用于在列表中展示「明细在哪」） */
const handleExpandChange = async (_row: SalesInvoiceVO, expandedRows: SalesInvoiceVO[]) => {
  for (const row of expandedRows) {
    const id = Number(row.id);
    if (!Number.isFinite(id) || expandedDetails.value[id] !== undefined) continue;
    expandLoadingId.value = id;
    try {
      const vo = await salesInvoiceApi.getById(id);
      expandedDetails.value[id] = (vo?.items ?? []) as SalesInvoiceDetailItemDTO[];
    } catch (e: any) {
      expandedDetails.value[id] = [];
      if (e?.status !== 404) ElMessage.error(e?.message ?? "加载明细失败");
    } finally {
      expandLoadingId.value = null;
    }
  }
};

const handleOpenDialog = async (row?: SalesInvoiceVO) => {
  isEdit.value = !!row;
  if (row) {
    const id = Number(row.id);
    if (!Number.isFinite(id)) {
      ElMessage.error("发票 ID 无效");
      return;
    }
    try {
      const vo = await salesInvoiceApi.getById(id);
      if (!vo?.id) {
        ElMessage.error("未找到该销项发票");
        return;
      }
      form.id = vo.id;
      form.salesOrderCode = vo.salesOrderCode ?? "";
      form.invoiceNo = vo.invoiceNo ?? "";
      form.invoiceDate = vo.invoiceDate?.toString().substring(0, 10) ?? "";
      form.companyName = vo.companyName ?? "";
      form.remark = vo.remark ?? "";
      const items = vo.items ?? [];
      form.items =
        items.length > 0
          ? items.map((d) => ({
              itemName: d.itemName ?? "",
              specification: d.specification ?? "",
              unit: d.unit ?? "",
              quantity: Number(d.quantity) ?? 0,
              unitPriceInclusiveTax: Number(d.unitPriceInclusiveTax) ?? 0,
              amountExclusiveTax: Number(d.amountExclusiveTax) ?? 0,
              taxAmount: Number(d.taxAmount) ?? 0,
              amountInclusiveTax: Number(d.amountInclusiveTax) ?? 0,
              remark: d.remark ?? "",
            }))
          : [createEmptyItem()];
    } catch (e: any) {
      const msg = e?.status === 404 ? "未找到该销项发票（可能已被删除），请刷新列表" : e?.message ?? "加载销项发票失败";
      ElMessage.error(msg);
      return;
    }
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

