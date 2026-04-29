<!-- 极简风格LLM分析页 -->
<template>
  <div class="page">
    <h1 class="title">LLM 分析</h1>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else class="stat-grid">
      <div class="stat-card">
        <div class="stat-value">{{ safeInt(data.totalCallCount) }}</div>
        <div class="stat-label">总调用次数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ safeInt(data.successCallCount) }}</div>
        <div class="stat-label">成功次数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ safeInt(data.failCallCount) }}</div>
        <div class="stat-label">失败次数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ safeInt(data.totalTokens) }}</div>
        <div class="stat-label">总 Token</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ formatCost(data.totalCostAmount) }}</div>
        <div class="stat-label">总费用</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import { getDashboardLlmAnalysis } from "@/api/dashboard";

const loading = ref(false);
const data = reactive({
  totalCallCount: 0,
  successCallCount: 0,
  failCallCount: 0,
  totalTokens: 0,
  totalCostAmount: 0,
});

function safeInt(v?: number): number {
  return typeof v === "number" && !isNaN(v) ? Math.trunc(v) : 0;
}
function formatCost(v?: number): string {
  return typeof v === "number" && !isNaN(v) ? `${v.toFixed(4)} 元` : "0.0000 元";
}

onMounted(async () => {
  loading.value = true;
  try {
    Object.assign(data, await getDashboardLlmAnalysis());
  } catch {
    /* empty */
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}
.title {
  font-size: 28px;
  font-weight: 700;
  color: #000;
  margin: 0 0 32px 0;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.stat-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  transition: all 0.2s;
}
.stat-card:hover {
  border-color: #00e676;
  box-shadow: 0 2px 8px rgba(0, 230, 118, 0.1);
}
.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #000;
  margin-bottom: 8px;
}
.stat-label {
  font-size: 14px;
  color: #999;
}
.loading {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}
@media (max-width: 768px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 480px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
