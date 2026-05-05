<template>
  <div class="chat-msg" :class="msg.role">
    <div v-if="msg.role === 'user'" class="bubble user-bubble">
      {{ msg.content }}
    </div>

    <div v-else class="bubble ai-bubble">
      <div v-if="msg.loading" class="loading-row">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在生成</span>
      </div>

      <el-alert
        v-if="msg.error"
        :title="msg.error"
        type="error"
        show-icon
        :closable="false"
        class="msg-alert"
      />

      <template v-if="msg.sqlResult">
        <p class="ai-reason">{{ msg.sqlResult.reason }}</p>
        <el-alert
          v-for="(w, i) in msg.sqlResult.warnings"
          :key="i"
          :title="w"
          type="warning"
          show-icon
          :closable="false"
          class="msg-alert"
        />
        <pre class="sql-preview"><code>{{ msg.sqlResult.sqlTemplate }}</code></pre>
        <button class="btn-use" @click="$emit('use', msg)">使用此 SQL</button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Loading } from "@element-plus/icons-vue";
import type { ChatItem } from "@/stores/aiStore";

defineProps<{
  msg: ChatItem;
}>();

defineEmits<{
  use: [msg: ChatItem];
}>();
</script>

<style scoped>
.chat-msg {
  margin-bottom: 16px;
  display: flex;
}
.chat-msg.user {
  justify-content: flex-end;
}
.chat-msg.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: 85%;
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 17px;
  line-height: 1.47;
  letter-spacing: -0.374px;
}

.user-bubble {
  background: #0066cc;
  color: #ffffff;
  border-bottom-right-radius: 4px;
}

.ai-bubble {
  background: #fafafc;
  border: 1px solid #e0e0e0;
  border-bottom-left-radius: 4px;
}

.ai-reason {
  color: #333333;
  margin: 0 0 10px;
  font-size: 14px;
  line-height: 1.43;
}

.msg-alert {
  margin-bottom: 8px;
}

.loading-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #7a7a7a;
  font-size: 14px;
}

.sql-preview {
  background: #1d1d1f;
  color: #f5f5f7;
  padding: 14px 16px;
  border-radius: 8px;
  overflow-x: auto;
  font-family: "SF Mono", SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
  line-height: 1.5;
  margin: 10px 0 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.btn-use {
  margin-top: 10px;
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 400;
  color: #ffffff;
  background: #0066cc;
  border: none;
  border-radius: 9999px;
  cursor: pointer;
  transition: background 0.15s;
  letter-spacing: -0.224px;
}
.btn-use:hover {
  background: #0071e3;
}
.btn-use:active {
  transform: scale(0.95);
}
</style>
