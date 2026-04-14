<template>
  <div class="page-container">
    <el-card class="page-card" v-loading="loading">
      <template #header>
        <div class="card-header">LLM 分析</div>
      </template>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="总调用次数">{{ safeInt(summary.totalCallCount) }}</el-descriptions-item>
        <el-descriptions-item label="成功次数">{{ safeInt(summary.successCallCount) }}</el-descriptions-item>
        <el-descriptions-item label="失败次数">{{ safeInt(summary.failCallCount) }}</el-descriptions-item>
        <el-descriptions-item label="成功率">{{ calcSuccessRate() }}</el-descriptions-item>
        <el-descriptions-item label="总 Token 数">{{ safeInt(summary.totalTokens) }}</el-descriptions-item>
        <el-descriptions-item label="总费用">{{ formatCost(summary.totalCostAmount) }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">趋势数据</el-divider>
      <el-table :data="trendRows">
        <el-table-column prop="date" label="日期" min-width="160" />
        <el-table-column prop="callCount" label="调用次数" min-width="120" />
        <el-table-column prop="costAmount" label="费用（元）" min-width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { getDashboardLlmAnalysis, type LlmAnalysisSummary } from "@/api/dashboard";

interface LlmTrendRow {
  date: string;
  callCount: number;
  costAmount: string;
}

const loading = ref(false);
const summary = reactive<LlmAnalysisSummary>({});

const trendRows = computed<LlmTrendRow[]>(() => {
  const dates = summary.dates ?? [];
  const callCounts = summary.callCounts ?? [];
  const costAmounts = summary.costAmounts ?? [];
  return dates.map((date, index) => ({
    date,
    callCount: safeInt(callCounts[index]),
    costAmount: formatCostValue(costAmounts[index]),
  }));
});

function safeInt(value?: number): number {
  if (value == null || Number.isNaN(Number(value))) {
    return 0;
  }
  return Math.trunc(Number(value));
}

function formatCostValue(value?: number): string {
  if (value == null || Number.isNaN(Number(value))) {
    return "0.0000";
  }
  return Number(value).toFixed(4);
}

function formatCost(value?: number): string {
  return `${formatCostValue(value)} 元`;
}

function calcSuccessRate(): string {
  const total = Number(summary.totalCallCount ?? 0);
  const success = Number(summary.successCallCount ?? 0);
  if (total <= 0) {
    return "0.00%";
  }
  return `${((success / total) * 100).toFixed(2)}%`;
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    Object.assign(summary, await getDashboardLlmAnalysis());
  } catch {
    ElMessage.error("LLM 分析数据加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadData();
});
</script>

<style scoped>
.card-header {
  font-size: 16px;
  font-weight: 700;
}

.filter-form {
  margin-bottom: 12px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.text-muted {
  color: #909399;
}

.tag-gap {
  margin-right: 6px;
}

.page-container {
  padding: 0px;
}
</style>
