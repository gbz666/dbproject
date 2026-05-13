<template>
  <div v-if="paramsSpec.length" class="param-form">
    <div class="form-header">
      <span class="form-title">参数设置</span>
    </div>
    <div class="form-body">
      <div v-for="p in paramsSpec" :key="p.name" class="form-item">
        <label class="form-label">
          {{ p.label || p.name }}
          <span v-if="p.required" class="required">*</span>
        </label>

        <!-- 日期 -->
        <el-date-picker
          v-if="p.type === 'date'"
          :model-value="modelValue[p.name] as string"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          size="default"
          @update:model-value="(v: string) => updateParam(p.name, v)"
        />

        <!-- 数值 -->
        <el-input-number
          v-else-if="isNumericType(p.type)"
          :model-value="modelValue[p.name] as number"
          :controls="false"
          size="default"
          @update:model-value="(v: number) => updateParam(p.name, v)"
        />

        <!-- 字符串（默认） -->
        <el-input
          v-else
          :model-value="modelValue[p.name] as string"
          :placeholder="p.label || p.name"
          size="default"
          @update:model-value="(v: string) => updateParam(p.name, v)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ParamSpec } from "@/api/aiApi";

const props = defineProps<{
  paramsSpec: ParamSpec[];
  modelValue: Record<string, unknown>;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: Record<string, unknown>): void;
}>();

function isNumericType(type: string): boolean {
  return type === "int" || type === "float" || type === "number";
}

function updateParam(name: string, value: unknown) {
  emit("update:modelValue", { ...props.modelValue, [name]: value });
}
</script>

<style scoped>
.param-form {
  margin-bottom: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.form-header {
  padding: 10px 16px;
  background: #f8f8f8;
  border-bottom: 1px solid #e0e0e0;
}

.form-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.224px;
}

.form-body {
  padding: 12px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 180px;
  flex: 1;
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.required {
  color: #f56c6c;
  margin-left: 2px;
}
</style>
