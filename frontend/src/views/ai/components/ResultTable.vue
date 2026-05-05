<template>
  <div class="result-section">
    <div class="result-header">
      <span class="result-label">查询结果</span>
      <span class="row-count">{{ rows.length }} 行</span>
    </div>

    <div v-if="chartUrl" class="chart-wrap">
      <img :src="chartUrl" alt="图表" class="chart-img" />
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th v-for="col in columns" :key="col">{{ col }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, ri) in tableRows" :key="ri">
            <td v-for="col in columns" :key="col">{{ row[col] }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  columns: string[];
  rows: unknown[][];
  chartUrl?: string;
}>();

const tableRows = computed(() =>
  props.rows.map((row) => {
    const obj: Record<string, unknown> = {};
    props.columns.forEach((col, i) => {
      obj[col] = row[i];
    });
    return obj;
  }),
);
</script>

<style scoped>
.result-section {
  margin-top: 24px;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.result-label {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.224px;
}

.row-count {
  font-size: 12px;
  color: #7a7a7a;
  background: #f5f5f7;
  padding: 2px 10px;
  border-radius: 9999px;
  letter-spacing: -0.12px;
}

/* ── chart ── */
.chart-wrap {
  text-align: center;
  margin-bottom: 16px;
}

.chart-img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

/* ── table ── */
.table-wrap {
  overflow-x: auto;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  line-height: 1.43;
  letter-spacing: -0.224px;
}

.data-table th {
  background: #f5f5f7;
  color: #1d1d1f;
  font-weight: 600;
  text-align: left;
  padding: 10px 14px;
  white-space: nowrap;
  border-bottom: 1px solid #e0e0e0;
}

.data-table td {
  padding: 10px 14px;
  color: #1d1d1f;
  border-bottom: 1px solid #f0f0f0;
  white-space: nowrap;
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.data-table tbody tr:hover {
  background: #fafafc;
}
</style>
