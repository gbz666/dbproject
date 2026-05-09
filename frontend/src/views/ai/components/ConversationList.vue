<template>
  <div class="conv-panel">
    <div class="conv-header">
      <span class="conv-title">历史对话</span>
      <button class="btn-new" @click="handleNew">+ 新建</button>
    </div>
    <div class="conv-body">
      <div
        v-for="conv in store.conversations"
        :key="conv.id"
        class="conv-item"
        :class="{ active: conv.id === store.currentConversationId }"
        @click="store.switchConversation(conv.id)"
      >
        <div class="conv-item-title">{{ conv.title }}</div>
        <div class="conv-item-meta">
          <span class="conv-item-time">{{ formatTime(conv.updatedAt) }}</span>
          <span class="conv-item-count">{{ conv.messages.length }} 条</span>
        </div>
        <button
          class="btn-delete"
          @click.stop="handleDelete(conv.id)"
          title="删除对话"
        >
          &times;
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAiStore } from "@/stores/aiStore";

const store = useAiStore();

function handleNew() {
  store.createConversation();
}

function handleDelete(id: number) {
  if (store.conversations.length <= 1) {
    store.deleteConversation(id);
    return;
  }
  store.deleteConversation(id);
}

function formatTime(ts: number): string {
  const now = Date.now();
  const diff = now - ts;
  const min = 60 * 1000;
  const hour = 60 * min;
  const day = 24 * hour;

  if (diff < min) return "刚刚";
  if (diff < hour) return `${Math.floor(diff / min)} 分钟前`;
  if (diff < day) return `${Math.floor(diff / hour)} 小时前`;
  if (diff < 2 * day) return "昨天";
  const d = new Date(ts);
  return `${d.getMonth() + 1}/${d.getDate()}`;
}
</script>

<style scoped>
.conv-panel {
  width: 240px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.conv-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.conv-title {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.02em;
}

.btn-new {
  padding: 6px 14px;
  background: #0066cc;
  color: #fff;
  border: none;
  border-radius: 9999px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-new:hover {
  background: #005bb5;
}

.conv-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conv-body::-webkit-scrollbar {
  width: 4px;
}

.conv-body::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 4px;
}

.conv-item {
  position: relative;
  padding: 12px 12px 12px 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.12s;
  border-left: 3px solid transparent;
}

.conv-item:hover {
  background: #fafafc;
}

.conv-item:hover .btn-delete {
  opacity: 1;
}

.conv-item.active {
  background: #f0f4ff;
  border-left-color: #0066cc;
}

.conv-item-title {
  font-size: 14px;
  font-weight: 500;
  color: #1d1d1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 20px;
}

.conv-item-meta {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
  color: #86868b;
}

.btn-delete {
  position: absolute;
  top: 10px;
  right: 8px;
  width: 22px;
  height: 22px;
  background: none;
  border: none;
  border-radius: 50%;
  font-size: 16px;
  line-height: 1;
  color: #86868b;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.12s, background 0.12s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-delete:hover {
  background: #ffeaea;
  color: #d00;
}
</style>
