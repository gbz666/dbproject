<template>
  <div class="ai-page">
    <ChatPanel />
    <ExecPanel />
  </div>
</template>

<script setup lang="ts">
import ChatPanel from "./components/ChatPanel.vue";
import ExecPanel from "./components/ExecPanel.vue";
</script>

<style scoped>
.ai-page {
  display: flex;
  gap: 16px;
  height: calc(100vh - 100px);
  padding: 0;
}
</style>
