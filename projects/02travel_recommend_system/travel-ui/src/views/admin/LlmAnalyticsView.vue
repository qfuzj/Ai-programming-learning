<!-- LLM 对话分析仪表盘 -->
<template>
  <div class="admin-page">
    <div class="page-head">
      <div>
        <p class="eyebrow">LLM INSIGHTS</p>
        <h1>大模型对话分析</h1>
      </div>
    </div>

    <!-- KPI 概览 -->
    <section class="kpi-grid">
      <StatCard label="总调用次数" :value="formatInt(data.totalCallCount)" tone="default" />
      <StatCard label="成功次数" :value="formatInt(data.successCallCount)" tone="primary" />
      <StatCard label="失败次数" :value="formatInt(data.failCallCount)" tone="danger" />
      <StatCard label="总 Token" :value="formatInt(data.totalTokens)" tone="accent" />
      <StatCard label="总费用" :value="formatCost(data.totalCostAmount)" tone="warning" />
    </section>

    <!-- 调用结果分布 -->
    <section class="chart-grid two-col">
      <ChartCard
        title="调用成功率"
        :subtitle="successRateSubtitle"
        :loading="loading"
        :empty="!loading && totalCalls === 0"
        empty-text="暂无调用数据"
        body-height="320px"
      >
        <div ref="successPieRef" class="chart-canvas"></div>
      </ChartCard>

      <ChartCard
        title="性能仪表盘"
        subtitle="调用成功率分段评估"
        :loading="loading"
        :empty="!loading && totalCalls === 0"
        empty-text="暂无调用数据"
        body-height="320px"
      >
        <div ref="gaugeRef" class="chart-canvas"></div>
      </ChartCard>
    </section>

    <!-- 每日调用趋势 -->
    <section class="chart-grid">
      <ChartCard
        title="每日调用次数"
        :subtitle="dateRangeSubtitle"
        :loading="loading"
        :empty="!loading && !hasTrend"
        empty-text="暂无趋势数据"
        body-height="340px"
      >
        <div ref="callTrendRef" class="chart-canvas"></div>
      </ChartCard>
    </section>

    <!-- 每日费用趋势 -->
    <section class="chart-grid">
      <ChartCard
        title="每日 Token 与费用"
        :subtitle="costSubtitle"
        :loading="loading"
        :empty="!loading && !hasCost"
        empty-text="暂无费用数据"
        body-height="320px"
      >
        <div ref="costTrendRef" class="chart-canvas"></div>
      </ChartCard>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, shallowRef } from "vue";
import { getDashboardLlmAnalysis, type LlmAnalysisSummary } from "@/api/dashboard";
import ChartCard from "@/components/analytics/ChartCard.vue";
import StatCard from "@/components/analytics/StatCard.vue";
import { useChart, type EChartsOption } from "@/composables/useChart";
import {
  chartAxisLine,
  chartGrid,
  chartPalette,
  chartSeries,
  chartTextStyle,
  chartTooltipStyle,
} from "@/styles/charts";

const loading = ref(false);

const data = reactive<LlmAnalysisSummary>({
  totalCallCount: 0,
  successCallCount: 0,
  failCallCount: 0,
  totalTokens: 0,
  totalCostAmount: 0,
  dates: [],
  callCounts: [],
  costAmounts: [],
});

// ================== 工具函数 ==================
function safeInt(v?: number): number {
  return typeof v === "number" && !isNaN(v) ? Math.trunc(v) : 0;
}
function safeNum(v?: number): number {
  return typeof v === "number" && !isNaN(v) ? v : 0;
}
function formatInt(v?: number): string {
  return safeInt(v).toLocaleString("en-US");
}
function formatCost(v?: number): string {
  return `${safeNum(v).toFixed(4)} 元`;
}
function formatPercent(v: number): string {
  return `${(v * 100).toFixed(2)}%`;
}

// ================== 派生数据 ==================
const totalCalls = computed(() => safeInt(data.successCallCount) + safeInt(data.failCallCount));
const successRate = computed(() =>
  totalCalls.value === 0 ? 0 : safeInt(data.successCallCount) / totalCalls.value
);
const successRateSubtitle = computed(() =>
  totalCalls.value === 0 ? "暂无数据" : `成功率 ${formatPercent(successRate.value)}`
);

const hasTrend = computed(
  () => Array.isArray(data.dates) && data.dates.length > 0 && Array.isArray(data.callCounts)
);
const hasCost = computed(
  () => Array.isArray(data.dates) && data.dates.length > 0 && Array.isArray(data.costAmounts)
);

const dateRangeSubtitle = computed(() => {
  const ds = data.dates ?? [];
  if (ds.length === 0) return "";
  if (ds.length === 1) return ds[0];
  return `${ds[0]} ~ ${ds[ds.length - 1]}`;
});

const costSubtitle = computed(() => {
  const cs = data.costAmounts ?? [];
  if (cs.length === 0) return "";
  const sum = cs.reduce((a, b) => a + safeNum(b), 0);
  return `期间总费用 ${sum.toFixed(4)} 元`;
});

// ================== 图表 option ==================
const successPieOption = shallowRef<EChartsOption | null>(null);
const gaugeOption = shallowRef<EChartsOption | null>(null);
const callTrendOption = shallowRef<EChartsOption | null>(null);
const costTrendOption = shallowRef<EChartsOption | null>(null);

const { containerRef: successPieRef } = useChart(successPieOption);
const { containerRef: gaugeRef } = useChart(gaugeOption);
const { containerRef: callTrendRef } = useChart(callTrendOption);
const { containerRef: costTrendRef } = useChart(costTrendOption);

function buildSuccessPieOption(): EChartsOption {
  const success = safeInt(data.successCallCount);
  const fail = safeInt(data.failCallCount);
  const rate = totalCalls.value === 0 ? 0 : success / totalCalls.value;
  return {
    color: [chartPalette.primary, chartPalette.danger],
    textStyle: chartTextStyle,
    tooltip: {
      trigger: "item",
      formatter: "{b}: {c} ({d}%)",
      ...chartTooltipStyle,
    },
    legend: {
      orient: "vertical",
      right: 16,
      top: "center",
      icon: "circle",
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { color: chartPalette.text, fontSize: 12 },
    },
    graphic: [
      {
        type: "text",
        left: "30%",
        top: "42%",
        style: {
          text: formatPercent(rate),
          textAlign: "center" as const,
          fill: chartPalette.ink,
          fontSize: 24,
          fontWeight: 700,
        },
      } as never,
      {
        type: "text",
        left: "30%",
        top: "58%",
        style: {
          text: "成功率",
          textAlign: "center" as const,
          fill: chartPalette.textMuted,
          fontSize: 12,
        },
      } as never,
    ],
    series: [
      {
        name: "调用结果",
        type: "pie",
        center: ["35%", "50%"],
        radius: ["50%", "72%"],
        avoidLabelOverlap: true,
        label: { show: false },
        labelLine: { show: false },
        data: [
          { name: "成功", value: success },
          { name: "失败", value: fail },
        ],
      },
    ],
  };
}

function buildGaugeOption(): EChartsOption {
  const rate = successRate.value;
  return {
    textStyle: chartTextStyle,
    tooltip: { ...chartTooltipStyle, formatter: () => `成功率 ${formatPercent(rate)}` },
    series: [
      {
        type: "gauge",
        startAngle: 210,
        endAngle: -30,
        min: 0,
        max: 1,
        radius: "82%",
        center: ["50%", "58%"],
        progress: {
          show: true,
          width: 14,
          itemStyle: { color: chartPalette.primary },
        },
        axisLine: {
          lineStyle: {
            width: 14,
            color: [
              [0.5, chartPalette.danger],
              [0.85, chartPalette.warning],
              [1, chartPalette.primary],
            ],
          },
        },
        pointer: {
          length: "60%",
          width: 4,
          itemStyle: { color: chartPalette.ink },
        },
        axisTick: {
          length: 4,
          lineStyle: { color: chartPalette.border, width: 1 },
        },
        splitLine: {
          length: 10,
          lineStyle: { color: chartPalette.textMuted, width: 2 },
        },
        axisLabel: {
          distance: 18,
          color: chartPalette.textMuted,
          fontSize: 11,
          formatter: (v: number) => `${Math.round(v * 100)}`,
        },
        anchor: { show: true, size: 10, itemStyle: { color: chartPalette.ink } },
        title: {
          offsetCenter: [0, "70%"],
          color: chartPalette.textMuted,
          fontSize: 12,
        },
        detail: {
          valueAnimation: true,
          offsetCenter: [0, "30%"],
          fontSize: 26,
          fontWeight: 700,
          color: chartPalette.ink,
          formatter: (v: number) => `${(v * 100).toFixed(2)}%`,
        },
        data: [{ value: rate, name: "成功率" }],
      },
    ],
  };
}

function buildCallTrendOption(): EChartsOption {
  const dates = data.dates ?? [];
  const calls = (data.callCounts ?? []).map(safeInt);
  const cumulative: number[] = [];
  calls.reduce((acc, v, i) => {
    cumulative[i] = acc + v;
    return cumulative[i];
  }, 0);
  return {
    color: [chartSeries[0], chartSeries[1]],
    textStyle: chartTextStyle,
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" },
      ...chartTooltipStyle,
    },
    legend: {
      top: 0,
      right: 0,
      icon: "roundRect",
      itemWidth: 12,
      itemHeight: 4,
      textStyle: { color: chartPalette.text, fontSize: 12 },
    },
    grid: { ...chartGrid, top: 40, right: 56 },
    xAxis: {
      type: "category",
      data: dates,
      ...chartAxisLine,
    },
    yAxis: [
      {
        type: "value",
        name: "调用次数",
        nameTextStyle: { color: chartPalette.textMuted, fontSize: 11 },
        ...chartAxisLine,
      },
      {
        type: "value",
        name: "累计调用",
        nameTextStyle: { color: chartPalette.textMuted, fontSize: 11 },
        ...chartAxisLine,
        splitLine: { show: false },
      },
    ],
    series: [
      {
        name: "调用次数",
        type: "bar",
        data: calls,
        barMaxWidth: 28,
        itemStyle: {
          color: chartPalette.primary,
          borderRadius: [4, 4, 0, 0],
        },
        emphasis: { itemStyle: { color: chartPalette.primary } },
      },
      {
        name: "累计调用",
        type: "line",
        yAxisIndex: 1,
        data: cumulative,
        smooth: true,
        symbol: "circle",
        symbolSize: 6,
        lineStyle: { width: 2, color: chartPalette.accent },
        itemStyle: { color: chartPalette.accent },
      },
    ],
  };
}

function buildCostTrendOption(): EChartsOption {
  const dates = data.dates ?? [];
  const costs = (data.costAmounts ?? []).map(safeNum);
  const calls = (data.callCounts ?? []).map(safeInt);
  return {
    color: [chartPalette.warning],
    textStyle: chartTextStyle,
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "line" },
      ...chartTooltipStyle,
      formatter: (params: unknown) => {
        const arr = params as Array<{ axisValue: string; dataIndex: number; data: number }>;
        if (!arr || arr.length === 0) return "";
        const idx = arr[0].dataIndex;
        const date = arr[0].axisValue;
        const cost = safeNum(costs[idx]).toFixed(4);
        const call = safeInt(calls[idx]).toLocaleString("en-US");
        return `<div style="font-weight:600;color:${chartPalette.ink};margin-bottom:4px">${date}</div>
          <div style="display:flex;justify-content:space-between;gap:24px">
            <span style="color:${chartPalette.text}">费用</span>
            <span style="color:${chartPalette.ink};font-weight:600">${cost} 元</span>
          </div>
          <div style="display:flex;justify-content:space-between;gap:24px">
            <span style="color:${chartPalette.text}">调用次数</span>
            <span style="color:${chartPalette.ink};font-weight:600">${call}</span>
          </div>`;
      },
    },
    grid: { ...chartGrid, top: 24 },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: dates,
      ...chartAxisLine,
    },
    yAxis: {
      type: "value",
      name: "费用 (元)",
      nameTextStyle: { color: chartPalette.textMuted, fontSize: 11 },
      ...chartAxisLine,
    },
    series: [
      {
        name: "费用",
        type: "line",
        data: costs,
        smooth: true,
        symbol: "circle",
        symbolSize: 5,
        lineStyle: { width: 2, color: chartPalette.warning },
        itemStyle: { color: chartPalette.warning },
        areaStyle: { color: chartPalette.warningSoft },
      },
    ],
  };
}

function rebuildAll(): void {
  successPieOption.value = buildSuccessPieOption();
  gaugeOption.value = buildGaugeOption();
  callTrendOption.value = buildCallTrendOption();
  costTrendOption.value = buildCostTrendOption();
}

onMounted(async () => {
  loading.value = true;
  try {
    const res = await getDashboardLlmAnalysis();
    Object.assign(data, res);
  } catch {
    /* 忽略错误，保持空数据降级 */
  } finally {
    loading.value = false;
    rebuildAll();
  }
});
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 32px;
  padding: 32px;
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 0;
}

.page-head h1 {
  margin: 6px 0 0;
  font-size: 24px;
  font-weight: 700;
  color: #101828;
}

.eyebrow {
  margin: 0;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.16em;
  color: #00c46a;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.chart-grid.two-col {
  grid-template-columns: 1fr 1fr;
}

.chart-canvas {
  width: 100%;
  height: 100%;
}

@media (max-width: 1024px) {
  .kpi-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .chart-grid.two-col {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .admin-page {
    gap: 24px;
    padding: 20px 16px;
  }

  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
