<template>
  <div class="exec-panel">
    <div class="panel-header">
      <span class="header-title">SQL 执行</span>
      <button
        v-if="store.panelActive"
        class="btn-text"
        @click="store.clearPanel()"
      >
        清空
      </button>
    </div>

    <div v-if="!store.panelActive" class="empty-state">
      <div class="empty-icon">⌘</div>
      <p class="empty-title">等待生成 SQL</p>
      <p class="empty-sub">在左侧对话中生成 SQL 后，点击「使用此 SQL」即可在此编辑和执行</p>
    </div>

    <div v-else class="exec-body">
      <div class="sql-section">
        <label class="field-label">SQL 查询</label>
        <textarea
          v-model="store.panelSql"
          class="sql-editor"
          spellcheck="false"
        />
      </div>

      <button
        class="btn-execute"
        :disabled="store.executing"
        @click="handleExecute"
      >
        <span v-if="store.executing" class="spinner" />
        <span v-else>执行查询</span>
      </button>

      <el-alert
        v-if="store.execError"
        :title="store.execError"
        type="error"
        show-icon
        :closable="false"
        class="exec-alert"
      />

      <ResultTable
        v-if="store.execResult"
        :columns="store.execResult.columns"
        :rows="store.execResult.rows"
        :chart-url="store.chartFullUrl || undefined"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from "element-plus";
import { useAiStore } from "@/stores/aiStore";
import ResultTable from "./ResultTable.vue";

const store = useAiStore();

async function handleExecute() {
  await store.executePanel();
  if (store.execError) {
    ElMessage.error(store.execError);
  } else if (store.execResult && !store.execResult.rows.length) {
    ElMessage.info("查询无结果");
  }
}
</script>

<style scoped>
.exec-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid #e0e0e0;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.header-title {
  font-family: "SF Pro Display", system-ui, -apple-system, sans-serif;
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.374px;
}

.btn-text {
  background: none;
  border: none;
  color: #0066cc;
  font-size: 14px;
  cursor: pointer;
  padding: 4px 8px;
  letter-spacing: -0.224px;
}
.btn-text:hover {
  color: #0071e3;
}

/* ── empty ── */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 32px;
}

.empty-icon {
  font-size: 32px;
  color: #c0c4cc;
  margin-bottom: 12px;
}

.empty-title {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 4px;
  letter-spacing: -0.374px;
}

.empty-sub {
  font-size: 14px;
  color: #7a7a7a;
  margin: 0;
  max-width: 260px;
  letter-spacing: -0.224px;
}

/* ── body ── */
.exec-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.sql-section {
  margin-bottom: 12px;
}

.field-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 8px;
  letter-spacing: -0.224px;
}

.sql-editor {
  width: 100%;
  min-height: 120px;
  max-height: 280px;
  padding: 14px 16px;
  font-family: "SF Mono", SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #f5f5f7;
  background: #1d1d1f;
  border: none;
  border-radius: 8px;
  resize: vertical;
  outline: none;
  box-sizing: border-box;
}

.btn-execute {
  width: 100%;
  padding: 12px 24px;
  font-size: 17px;
  font-weight: 400;
  color: #ffffff;
  background: #0066cc;
  border: none;
  border-radius: 9999px;
  cursor: pointer;
  transition: background 0.15s;
  letter-spacing: -0.374px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-execute:hover:not(:disabled) {
  background: #0071e3;
}
.btn-execute:active:not(:disabled) {
  transform: scale(0.95);
}
.btn-execute:disabled {
  opacity: 0.4;
  cursor: default;
}

.exec-alert {
  margin-top: 12px;
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
