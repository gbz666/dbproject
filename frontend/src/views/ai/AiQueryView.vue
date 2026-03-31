<template>
  <div class="ai-page">
    <!-- ====== 左侧：对话区 ====== -->
    <div class="chat-side">
      <div class="side-header">
        <span>AI 对话</span>
        <el-button v-if="store.messages.length" link size="small" @click="store.clearConversation()">清空</el-button>
      </div>

      <div ref="chatBox" class="chat-box">
        <!-- 空状态 -->
        <div v-if="!store.messages.length" class="empty-hint">
          <el-icon :size="40" color="#c0c4cc"><ChatDotRound /></el-icon>
          <p>输入问题开始对话</p>
          <div class="hint-examples">
            <el-tag v-for="ex in examples" :key="ex" class="hint-tag" effect="plain" @click="store.inputText = ex">{{ ex }}</el-tag>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="msg in store.messages" :key="msg.id" class="chat-msg" :class="msg.role">
          <!-- 用户 -->
          <div v-if="msg.role === 'user'" class="bubble user-bubble">{{ msg.content }}</div>

          <!-- AI -->
          <div v-else class="bubble ai-bubble">
            <div v-if="msg.loading" class="loading-row">
              <el-icon class="is-loading"><Loading /></el-icon> 正在生成...
            </div>
            <el-alert v-if="msg.error" :title="msg.error" type="error" show-icon :closable="false" style="margin-bottom: 6px" />

            <template v-if="msg.sqlResult">
              <div class="ai-reason">{{ msg.sqlResult.reason }}</div>
              <el-alert
                v-for="(w, i) in msg.sqlResult.warnings"
                :key="i" :title="w" type="warning" show-icon :closable="false" style="margin-bottom: 6px"
              />
              <pre class="sql-readonly"><code>{{ msg.sqlResult.sqlTemplate }}</code></pre>
              <el-button type="primary" size="small" style="margin-top: 6px" @click="handleUse(msg)">
                使用此 SQL
              </el-button>
            </template>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="chat-input">
        <el-input
          v-model="store.inputText"
          :placeholder="store.messages.length ? '继续追问，如：改成前5名 / 加按月分组...' : '输入问题，如：查询本季度销售额前10的产品'"
          :disabled="store.sending"
          @keydown.enter.exact="handleSend"
        >
          <template #append>
            <el-button type="primary" :loading="store.sending" :disabled="!store.hasInput" @click="handleSend">发送</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- ====== 右侧：SQL 执行面板 ====== -->
    <div class="exec-side">
      <div class="side-header">
        <span>SQL 执行</span>
        <el-button v-if="store.panelActive" link size="small" @click="store.clearPanel()">清空</el-button>
      </div>

      <div v-if="!store.panelActive" class="exec-empty">
        <el-icon :size="36" color="#c0c4cc"><Monitor /></el-icon>
        <p>在左侧对话中生成 SQL 后，点击「使用此 SQL」即可在此编辑和执行</p>
      </div>

      <div v-else class="exec-content">
        <!-- SQL 编辑器 -->
        <div class="section-label">SQL（可直接编辑）</div>
        <el-input
          v-model="store.panelSql"
          type="textarea"
          :autosize="{ minRows: 4, maxRows: 14 }"
          class="sql-editor"
        />

        <div style="margin-top: 10px">
          <el-button type="success" :loading="store.executing" @click="handleExecute">执行查询</el-button>
        </div>

        <!-- 执行错误 -->
        <el-alert v-if="store.execError" :title="store.execError" type="error" show-icon :closable="false" style="margin-top: 10px" />

        <!-- 结果 -->
        <template v-if="store.execResult">
          <div style="display: flex; align-items: center; justify-content: space-between; margin-top: 16px">
            <div class="section-label" style="margin-bottom: 0">查询结果</div>
            <el-tag size="small" type="info">{{ store.execResult.rows.length }} 行</el-tag>
          </div>

          <!-- 图表 -->
          <div v-if="store.chartFullUrl" style="text-align: center; margin: 10px 0">
            <img :src="store.chartFullUrl" alt="图表" class="chart-img" />
          </div>

          <!-- 表格 -->
          <el-table :data="store.tableRows" border stripe max-height="360" size="small" style="width: 100%; margin-top: 8px">
            <el-table-column
              v-for="col in store.execResult.columns"
              :key="col" :prop="col" :label="col"
              min-width="100" show-overflow-tooltip
            />
          </el-table>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from "vue";
import { ElMessage } from "element-plus";
import { Loading, ChatDotRound, Monitor } from "@element-plus/icons-vue";
import { useAiStore, type ChatItem } from "@/stores/aiStore";

const store = useAiStore();
const chatBox = ref<HTMLElement>();

const examples = [
  "查询本季度销售额前10的产品",
  "近6个月各月销售总额趋势",
  "各产品分类的销售占比",
];

function scrollChat() {
  nextTick(() => { if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight; });
}
watch(() => store.messages.length, scrollChat);
watch(() => store.messages[store.messages.length - 1]?.loading, scrollChat);

async function handleSend() {
  if (!store.hasInput || store.sending) return;
  await store.sendMessage();
  scrollChat();
}

function handleUse(msg: ChatItem) {
  if (!msg.sqlResult) return;
  const firstQ = store.messages.find((m) => m.role === "user")?.content ?? "";
  store.loadToPanel(msg.sqlResult, firstQ);
}

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
.ai-page {
  display: flex;
  gap: 16px;
  height: calc(100vh - 100px);
  padding: 12px;
}

/* ── 左侧对话 ── */
.chat-side {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}

.side-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  font-weight: 600;
  font-size: 14px;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.chat-box {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #f5f7fa;
}

.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  font-size: 13px;
}
.empty-hint p { margin: 10px 0 8px; }
.hint-examples { display: flex; flex-wrap: wrap; gap: 6px; justify-content: center; }
.hint-tag { cursor: pointer; }
.hint-tag:hover { color: var(--el-color-primary); border-color: var(--el-color-primary); }

.chat-msg { margin-bottom: 12px; display: flex; }
.chat-msg.user { justify-content: flex-end; }
.chat-msg.assistant { justify-content: flex-start; }

.bubble { max-width: 88%; padding: 10px 14px; border-radius: 8px; font-size: 13px; line-height: 1.6; }
.user-bubble { background: #409eff; color: #fff; border-bottom-right-radius: 2px; }
.ai-bubble { background: #fff; border: 1px solid #e4e7ed; border-bottom-left-radius: 2px; }

.ai-reason { color: #606266; margin-bottom: 8px; }

.loading-row { display: flex; align-items: center; gap: 6px; color: #909399; font-size: 13px; }

.sql-readonly {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 10px 12px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: Consolas, "Courier New", monospace;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.chat-input { padding: 10px; border-top: 1px solid #e4e7ed; flex-shrink: 0; }

/* ── 右侧执行面板 ── */
.exec-side {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}

.exec-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 13px;
  padding: 20px;
  text-align: center;
}
.exec-empty p { margin-top: 10px; }

.exec-content {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
}

.section-label {
  font-weight: 600;
  margin-bottom: 6px;
  font-size: 12px;
  color: #303133;
}

.sql-editor :deep(.el-textarea__inner) {
  font-family: Consolas, "Courier New", monospace;
  font-size: 13px;
  line-height: 1.5;
  background: #1e1e1e;
  color: #d4d4d4;
  border-radius: 4px;
  padding: 12px 14px;
}

.chart-img {
  max-width: 100%;
  max-height: 320px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}
</style>
