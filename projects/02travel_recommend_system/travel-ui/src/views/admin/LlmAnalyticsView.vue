<template>
  <div class="page-container">
    <el-card v-loading="loading" class="page-card">
      <template #header>
        <div class="card-header">LLM 调用运营看板</div>
      </template>

      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-label">总调用次数</div>
          <div class="kpi-value">{{ safeInt(summary.totalCallCount).toLocaleString() }}</div>
        </div>
        <div class="kpi-card kpi-success">
          <div class="kpi-label">成功次数</div>
          <div class="kpi-value">{{ safeInt(summary.successCallCount).toLocaleString() }}</div>
        </div>
        <div class="kpi-card kpi-fail">
          <div class="kpi-label">失败次数</div>
          <div class="kpi-value">{{ safeInt(summary.failCallCount).toLocaleString() }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">成功率</div>
          <div class="kpi-value">{{ successRate }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">总 Token 数</div>
          <div class="kpi-value">{{ safeInt(summary.totalTokens).toLocaleString() }}</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">总费用 (元)</div>
          <div class="kpi-value">{{ formatCostValue(summary.totalCostAmount) }}</div>
        </div>
      </div>

      <div class="chart-grid">
        <div class="chart-box chart-box-wide">
          <div class="chart-title">每日调用次数趋势</div>
          <div ref="callChartRef" class="chart-canvas"></div>
        </div>
        <div class="chart-box">
          <div class="chart-title">成功 / 失败占比</div>
          <div ref="ratioChartRef" class="chart-canvas"></div>
        </div>
        <div class="chart-box chart-box-wide">
          <div class="chart-title">每日费用趋势（元）</div>
          <div ref="costChartRef" class="chart-canvas"></div>
        </div>
      </div>

      <el-divider content-position="left">趋势明细</el-divider>
      <el-table :data="trendRows" stripe>
        <el-table-column prop="date" label="日期" min-width="160" />
        <el-table-column prop="callCount" label="调用次数" min-width="120" align="right" />
        <el-table-column prop="costAmount" label="费用（元）" min-width="120" align="right" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import { getDashboardLlmAnalysis, type LlmAnalysisSummary } from "@/api/dashboard";

interface LlmTrendRow {
  date: string;
  callCount: number;
  costAmount: string;
}

const loading = ref(false);
const summary = reactive<LlmAnalysisSummary>({});

const callChartRef = ref<HTMLDivElement>();
const costChartRef = ref<HTMLDivElement>();
const ratioChartRef = ref<HTMLDivElement>();

let callChart: echarts.ECharts | null = null;
let costChart: echarts.ECharts | null = null;
let ratioChart: echarts.ECharts | null = null;

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

const successRate = computed<string>(() => {
  const total = Number(summary.totalCallCount ?? 0);
  const success = Number(summary.successCallCount ?? 0);
  if (total <= 0) return "0.00%";
  return `${((success / total) * 100).toFixed(2)}%`;
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

function initCharts(): void {
  if (callChartRef.value && !callChart) {
    callChart = echarts.init(callChartRef.value);
  }
  if (costChartRef.value && !costChart) {
    costChart = echarts.init(costChartRef.value);
  }
  if (ratioChartRef.value && !ratioChart) {
    ratioChart = echarts.init(ratioChartRef.value);
  }
}

function renderCharts(): void {
  const dates = summary.dates ?? [];
  const callCounts = (summary.callCounts ?? []).map((v) => safeInt(v));
  const costAmounts = (summary.costAmounts ?? []).map((v) => Number(formatCostValue(v)));

  callChart?.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: "category",
      data: dates,
      axisLabel: { color: "#606266" },
      axisLine: { lineStyle: { color: "#dcdfe6" } },
    },
    yAxis: {
      type: "value",
      axisLabel: { color: "#606266" },
      splitLine: { lineStyle: { color: "#f0f0f0" } },
    },
    series: [
      {
        name: "调用次数",
        type: "bar",
        data: callCounts,
        barMaxWidth: 32,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "#79bbff" },
            { offset: 1, color: "#409eff" },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
      },
    ],
  });

  costChart?.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: 50, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: "category",
      data: dates,
      axisLabel: { color: "#606266" },
      axisLine: { lineStyle: { color: "#dcdfe6" } },
    },
    yAxis: {
      type: "value",
      axisLabel: { color: "#606266", formatter: "{value}" },
      splitLine: { lineStyle: { color: "#f0f0f0" } },
    },
    series: [
      {
        name: "费用",
        type: "line",
        data: costAmounts,
        smooth: true,
        symbol: "circle",
        symbolSize: 6,
        itemStyle: { color: "#67c23a" },
        lineStyle: { width: 2, color: "#67c23a" },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(103, 194, 58, 0.25)" },
            { offset: 1, color: "rgba(103, 194, 58, 0.02)" },
          ]),
        },
      },
    ],
  });

  const success = safeInt(summary.successCallCount);
  const fail = safeInt(summary.failCallCount);
  ratioChart?.setOption({
    tooltip: { trigger: "item", formatter: "{b}: {c} ({d}%)" },
    legend: { bottom: 0, icon: "circle", textStyle: { color: "#606266" } },
    series: [
      {
        name: "调用结果",
        type: "pie",
        radius: ["50%", "72%"],
        center: ["50%", "45%"],
        avoidLabelOverlap: true,
        label: { show: false },
        data: [
          { name: "成功", value: success, itemStyle: { color: "#67c23a" } },
          { name: "失败", value: fail, itemStyle: { color: "#f56c6c" } },
        ],
      },
    ],
  });
}

function resizeCharts(): void {
  callChart?.resize();
  costChart?.resize();
  ratioChart?.resize();
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const data = await getDashboardLlmAnalysis();
    Object.keys(summary).forEach((key) => {
      delete (summary as Record<string, unknown>)[key];
    });
    Object.assign(summary, data);
  } catch {
    ElMessage.error("LLM 分析数据加载失败");
  } finally {
    loading.value = false;
  }
}

watch(
  () => summary,
  async () => {
    await nextTick();
    initCharts();
    renderCharts();
  },
  { deep: true }
);

onMounted(async () => {
  await loadData();
  await nextTick();
  initCharts();
  renderCharts();
  window.addEventListener("resize", resizeCharts);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeCharts);
  callChart?.dispose();
  costChart?.dispose();
  ratioChart?.dispose();
  callChart = null;
  costChart = null;
  ratioChart = null;
});
</script>

<style scoped>
.card-header {
  font-size: 16px;
  font-weight: 700;
}

.page-container {
  padding: 0;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.kpi-card {
  padding: 16px 18px;
  background: #fafbfc;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}

.kpi-card.kpi-success {
  border-left: 3px solid #67c23a;
}

.kpi-card.kpi-fail {
  border-left: 3px solid #f56c6c;
}

.kpi-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.kpi-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.chart-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  margin-bottom: 20px;
}

.chart-box {
  padding: 16px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}

.chart-box-wide {
  grid-column: 1 / 2;
}

.chart-box:nth-child(2) {
  grid-column: 2 / 3;
  grid-row: 1 / 3;
}

.chart-title {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.chart-canvas {
  width: 100%;
  height: 260px;
}

.chart-box:nth-child(2) .chart-canvas {
  height: 536px;
}

@media (max-width: 960px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
  .chart-box-wide,
  .chart-box:nth-child(2) {
    grid-column: auto;
    grid-row: auto;
  }
  .chart-box:nth-child(2) .chart-canvas {
    height: 260px;
  }
}
</style>
