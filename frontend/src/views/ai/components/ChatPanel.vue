<template>
  <div class="chat-panel">
    <div class="panel-header">
      <span class="header-title">{{ store.currentConversation?.title || "AI 对话" }}</span>
      <button
        v-if="store.messages.length"
        class="btn-text"
        @click="store.clearConversation()"
      >
        清空
      </button>
    </div>

    <div ref="chatBox" class="chat-body">
      <div v-if="!store.messages.length" class="empty-state">
        <div class="empty-icon">✦</div>
        <p class="empty-title">输入问题开始对话</p>
        <p class="empty-sub">AI 将为你生成可执行的 SQL 查询</p>
        <div class="example-chips">
          <button
            v-for="ex in examples"
            :key="ex"
            class="chip"
            @click="store.inputText = ex"
          >
            {{ ex }}
          </button>
        </div>
      </div>

      <ChatMessage
        v-for="msg in store.messages"
        :key="msg.id"
        :msg="msg"
        @use="handleUse"
      />
    </div>

    <div class="chat-input">
      <div class="input-wrap">
        <input
          v-model="store.inputText"
          type="text"
          :placeholder="
            store.messages.length
              ? '继续追问…'
              : '查询本季度销售额前10的产品'
          "
          :disabled="store.sending"
          @keydown.enter.exact="handleSend"
        />
        <button
          class="btn-send"
          :disabled="!store.hasInput || store.sending"
          @click="handleSend"
        >
          <span v-if="store.sending" class="spinner" />
          <span v-else>发送</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from "vue";
import { useAiStore, type ChatItem } from "@/stores/aiStore";
import ChatMessage from "./ChatMessage.vue";

const store = useAiStore();
const chatBox = ref<HTMLElement>();

const examples = [
  "查询本季度销售额前10的产品",
  "查询2025年各月份纯利润情况",
  "各产品分类的销售占比",
];

function scrollChat() {
  nextTick(() => {
    if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight;
  });
}

watch(() => store.messages.length, scrollChat);
watch(
  () => store.messages[store.messages.length - 1]?.loading,
  scrollChat,
);

async function handleSend() {
  if (!store.hasInput || store.sending) return;
  await store.sendMessage();
  scrollChat();
}

function handleUse(msg: ChatItem) {
  if (!msg.sqlResult) return;
  const firstQ =
    store.messages.find((m) => m.role === "user")?.content ?? "";
  store.loadToPanel(msg.sqlResult, firstQ);
}
</script>

<style scoped>
.chat-panel {
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

/* ── body ── */
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f5f7;
}

/* ── empty state ── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.empty-icon {
  font-size: 36px;
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
  margin: 0 0 20px;
  letter-spacing: -0.224px;
}

.example-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  max-width: 360px;
}

.chip {
  padding: 8px 16px;
  font-size: 14px;
  color: #0066cc;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 9999px;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;
  letter-spacing: -0.224px;
}
.chip:hover {
  border-color: #0066cc;
  color: #0071e3;
}

/* ── input ── */
.chat-input {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
  background: #ffffff;
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fafafc;
  border: 1px solid #e0e0e0;
  border-radius: 9999px;
  padding: 4px 4px 4px 18px;
  transition: border-color 0.15s;
}
.input-wrap:focus-within {
  border-color: #0066cc;
}

.input-wrap input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 17px;
  color: #1d1d1f;
  letter-spacing: -0.374px;
  line-height: 1.47;
}
.input-wrap input::placeholder {
  color: #7a7a7a;
}

.btn-send {
  flex-shrink: 0;
  padding: 10px 22px;
  font-size: 17px;
  font-weight: 400;
  color: #ffffff;
  background: #0066cc;
  border: none;
  border-radius: 9999px;
  cursor: pointer;
  transition: background 0.15s;
  letter-spacing: -0.374px;
  min-width: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-send:hover:not(:disabled) {
  background: #0071e3;
}
.btn-send:active:not(:disabled) {
  transform: scale(0.95);
}
.btn-send:disabled {
  opacity: 0.4;
  cursor: default;
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
