<template>
  <div class="result-panel">
    <el-alert
      v-if="errorMessage"
      type="warning"
      show-icon
      :closable="false"
      :title="errorMessage"
      class="result-alert"
    />

    <el-empty v-else-if="!loading && !result" description="生成后将在此处展示 AI 行程草案" />

    <el-skeleton v-else-if="loading" animated :rows="6" />

    <el-card v-else shadow="never" class="result-card">
      <template #header>
        <div class="result-header">AI 草案结果</div>
      </template>
      <pre class="result-json">{{ formattedResult }}</pre>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

interface Props {
  loading: boolean;
  errorMessage: string;
  result: unknown;
}

const props = defineProps<Props>();

const formattedResult = computed(() => {
  if (!props.result) {
    return "";
  }
  return JSON.stringify(props.result, null, 2);
});
</script>

<style scoped>
.result-panel {
  margin-top: 20px;
}

.result-alert {
  margin-bottom: 12px;
}

.result-card {
  border: 1px solid #ebeef5;
}

.result-header {
  font-weight: 700;
}

.result-json {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  max-height: 420px;
  overflow: auto;
  background: #f8fafc;
  padding: 12px;
  border-radius: 8px;
}
</style>
