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
        <div class="msg-actions">
          <button class="btn-use" @click="$emit('use', msg)">使用此 SQL</button>
          <div v-if="msg.sqlResult.memoryId" class="vote-group">
            <button
              class="btn-vote"
              :class="{ active: msg.feedbackVote === 1 }"
              :disabled="voting"
              title="这条 SQL 有用"
              @click="onVote(1)"
            >
              <el-icon><CaretTop /></el-icon>
              <span>有用</span>
            </button>
            <button
              class="btn-vote"
              :class="{ active: msg.feedbackVote === -1 }"
              :disabled="voting"
              title="这条 SQL 有问题"
              @click="onVote(-1)"
            >
              <el-icon><CaretBottom /></el-icon>
              <span>有问题</span>
            </button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { ElMessage } from "element-plus";
import { Loading, CaretTop, CaretBottom } from "@element-plus/icons-vue";
import { useAiStore } from "@/stores/aiStore";
import type { ChatItem } from "@/stores/aiStore";

const props = defineProps<{
  msg: ChatItem;
}>();

defineEmits<{
  use: [msg: ChatItem];
}>();

const aiStore = useAiStore();
const voting = ref(false);

async function onVote(vote: 1 | -1) {
  if (voting.value) return;
  voting.value = true;
  try {
    await aiStore.submitFeedback(props.msg, vote);
  } catch {
    ElMessage.error("反馈提交失败，请稍后重试");
  } finally {
    voting.value = false;
  }
}
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

.msg-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.btn-use {
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

.vote-group {
  display: flex;
  gap: 6px;
}

.btn-vote {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 12px;
  color: #555;
  background: #ffffff;
  border: 1px solid #d0d0d0;
  border-radius: 9999px;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-vote:hover:not(:disabled) {
  background: #f5f5f7;
  border-color: #b0b0b0;
}
.btn-vote:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-vote.active {
  color: #ffffff;
  background: #0066cc;
  border-color: #0066cc;
}
</style>
