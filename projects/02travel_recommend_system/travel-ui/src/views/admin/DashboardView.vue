<!-- 管理后台工作台首页：基于 ECharts 的数据分析仪表盘 -->
<template>
  <div class="admin-page">
    <div class="page-head">
      <div>
        <p class="eyebrow">DASHBOARD</p>
        <h1>工作台首页</h1>
      </div>
    </div>

    <!-- KPI 概览 -->
    <section class="kpi-grid">
      <StatCard
        v-for="(item, idx) in kpiItems"
        :key="item.label"
        :label="item.label"
        :value="formatInt(item.value)"
        :hint="item.hint"
        :tone="kpiTone(idx)"
      />
    </section>

    <!-- 景点热度 Top 10 -->
    <section class="section">
      <ChartCard
        title="景点热度 Top 10"
        subtitle="按页面浏览量排序"
        :loading="hotLoading"
        :empty="!hotLoading && topHotRanking.length === 0"
        empty-text="暂无景点热度数据"
        :body-height="hotChartHeight"
      >
        <div ref="hotChartRef" class="chart-canvas" />
      </ChartCard>
    </section>

    <!-- 推荐效果分析：漏斗 + 双仪表盘 -->
    <section class="section grid-2">
      <ChartCard
        title="推荐转化漏斗"
        subtitle="请求 → 点击 → 收藏"
        :loading="recLoading"
        :empty="!recLoading && recommendFunnelEmpty"
        empty-text="暂无推荐转化数据"
        body-height="340px"
      >
        <div ref="funnelChartRef" class="chart-canvas" />
      </ChartCard>
      <ChartCard
        title="点击率 / 收藏率"
        subtitle="推荐效果转化指标"
        :loading="recLoading"
        :empty="!recLoading && recommendGaugeEmpty"
        empty-text="暂无转化率数据"
        body-height="340px"
      >
        <div ref="gaugeChartRef" class="chart-canvas" />
      </ChartCard>
    </section>

    <!-- LLM 调用分布：环形图 + 指标卡 -->
    <section class="section grid-llm">
      <ChartCard
        title="调用结果分布"
        subtitle="成功 / 失败 调用占比"
        :loading="llmLoading"
        :empty="!llmLoading && llmPieEmpty"
        empty-text="暂无 LLM 调用数据"
        body-height="340px"
      >
        <div ref="llmPieRef" class="chart-canvas" />
      </ChartCard>
      <div class="llm-side">
        <StatCard
          label="总 Token 消耗"
          :value="formatInt(llm.totalTokens)"
          hint="累计输入 + 输出 token"
          tone="accent"
        />
        <StatCard
          label="估算成本"
          :value="formatCost(llm.totalCostAmount)"
          hint="按当期单价估算"
          tone="warning"
        />
        <StatCard label="成功调用" :value="formatInt(llm.successCallCount)" tone="primary" />
        <StatCard label="失败调用" :value="formatInt(llm.failCallCount)" tone="danger" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, shallowRef } from "vue";
import type { EChartsOption } from "echarts";
import {
  getDashboardOverview,
  getDashboardScenicHotRanking,
  getDashboardRecommendAnalysis,
  getDashboardLlmAnalysis,
  type ScenicHotRankingItem,
} from "@/api/dashboard";
import ChartCard from "@/components/analytics/ChartCard.vue";
import StatCard from "@/components/analytics/StatCard.vue";
import { useChart } from "@/composables/useChart";
import {
  chartAxisLine,
  chartGrid,
  chartPalette,
  chartTextStyle,
  chartTooltipStyle,
} from "@/styles/charts";

// ===== 数据 =====
const overview = reactive({
  totalUsers: 0,
  totalScenicSpots: 0,
  totalReviews: 0,
  totalTravelPlans: 0,
  totalRecommendRequests: 0,
  totalLlmCalls: 0,
  totalBrowseCount: 0,
  totalFavoriteCount: 0,
});

const hotRanking = ref<ScenicHotRankingItem[]>([]);
const hotLoading = ref(false);
const recLoading = ref(false);
const llmLoading = ref(false);

const recommend = reactive({
  totalRecommendRequests: 0,
  totalRecommendClicks: 0,
  totalRecommendFavorites: 0,
  clickRate: 0,
  favoriteRate: 0,
});

const llm = reactive({
  totalCallCount: 0,
  successCallCount: 0,
  failCallCount: 0,
  totalTokens: 0,
  totalCostAmount: 0,
});

// ===== KPI =====
const kpiItems = computed(() => [
  { label: "用户总数", value: overview.totalUsers, hint: "全平台注册用户" },
  { label: "景点总数", value: overview.totalScenicSpots, hint: "上架景点数量" },
  { label: "评论总数", value: overview.totalReviews, hint: "用户点评累计" },
  { label: "行程总数", value: overview.totalTravelPlans, hint: "用户创建行程数" },
  { label: "推荐请求", value: overview.totalRecommendRequests ?? 0, hint: "推荐引擎调用次数" },
  { label: "LLM 调用", value: overview.totalLlmCalls, hint: "大模型对话调用次数" },
  { label: "浏览总数", value: overview.totalBrowseCount ?? 0, hint: "浏览历史累计" },
  { label: "收藏总数", value: overview.totalFavoriteCount ?? 0, hint: "用户收藏累计" },
]);

function kpiTone(idx: number): "primary" | "accent" | "warning" | "default" {
  const seq: Array<"primary" | "accent" | "warning" | "default"> = [
    "primary",
    "accent",
    "warning",
    "default",
  ];
  return seq[idx % seq.length];
}

// ===== 景点热度图 =====
const topHotRanking = computed(() => hotRanking.value.slice(0, 10));

const hotChartHeight = computed(() => {
  const rows = topHotRanking.value.length;
  const min = 320;
  const calc = rows * 38 + 80;
  return `${Math.max(min, calc)}px`;
});

const hotOption = shallowRef<EChartsOption | null>(null);
const { containerRef: hotChartRef } = useChart(hotOption);

function buildHotOption(): void {
  if (topHotRanking.value.length === 0) {
    hotOption.value = null;
    return;
  }
  // 反向：top1 在最上方
  const reversed = [...topHotRanking.value].reverse();
  const names = reversed.map((it) => it.scenicName);
  const values = reversed.map((it) => it.pvCount ?? 0);
  hotOption.value = {
    textStyle: chartTextStyle,
    tooltip: {
      ...chartTooltipStyle,
      trigger: "axis",
      axisPointer: { type: "shadow" },
    },
    grid: { ...chartGrid, left: 12, right: 64, top: 16, bottom: 16 },
    xAxis: {
      type: "value",
      ...chartAxisLine,
      splitLine: { lineStyle: { color: chartPalette.borderSoft, type: "dashed" } },
    },
    yAxis: {
      type: "category",
      data: names,
      ...chartAxisLine,
      splitLine: { show: false },
      axisLabel: {
        ...chartAxisLine.axisLabel,
        color: chartPalette.ink,
        fontWeight: 500,
        width: 140,
        overflow: "truncate",
      },
    },
    series: [
      {
        name: "PV",
        type: "bar",
        data: values,
        barWidth: 18,
        itemStyle: {
          color: chartPalette.primary,
          borderRadius: [0, 4, 4, 0],
        },
        label: {
          show: true,
          position: "right",
          color: chartPalette.ink,
          fontFamily: chartTextStyle.fontFamily,
          fontSize: 12,
          formatter: (params) => formatInt(Number((params as { value?: number }).value ?? 0)),
        },
        emphasis: {
          itemStyle: { color: chartPalette.primary, opacity: 0.85 },
        },
      },
    ],
  };
}

// ===== 推荐转化漏斗 =====
const recommendFunnelEmpty = computed(() => {
  const r = recommend.totalRecommendRequests ?? 0;
  const c = recommend.totalRecommendClicks ?? 0;
  const f = recommend.totalRecommendFavorites ?? 0;
  return r + c + f === 0;
});

const funnelOption = shallowRef<EChartsOption | null>(null);
const { containerRef: funnelChartRef } = useChart(funnelOption);

function buildFunnelOption(): void {
  if (recommendFunnelEmpty.value) {
    funnelOption.value = null;
    return;
  }
  const data = [
    { name: "推荐请求", value: recommend.totalRecommendRequests ?? 0 },
    { name: "推荐点击", value: recommend.totalRecommendClicks ?? 0 },
    { name: "推荐收藏", value: recommend.totalRecommendFavorites ?? 0 },
  ];
  funnelOption.value = {
    textStyle: chartTextStyle,
    tooltip: {
      ...chartTooltipStyle,
      trigger: "item",
      formatter: (params) => {
        const p = params as { name: string; value: number };
        return `${p.name}<br/>数量：${formatInt(p.value)}`;
      },
    },
    color: [chartPalette.primary, chartPalette.accent, chartPalette.warning],
    series: [
      {
        type: "funnel",
        left: 24,
        right: 24,
        top: 16,
        bottom: 16,
        sort: "descending",
        gap: 4,
        minSize: "20%",
        maxSize: "100%",
        label: {
          show: true,
          position: "inside",
          color: "#ffffff",
          fontFamily: chartTextStyle.fontFamily,
          fontWeight: 600,
          fontSize: 13,
          formatter: (params) => {
            const p = params as { name: string; value: number };
            return `${p.name}\n${formatInt(p.value)}`;
          },
        },
        labelLine: { show: false },
        itemStyle: {
          borderColor: chartPalette.background,
          borderWidth: 2,
        },
        emphasis: {
          label: { fontSize: 14 },
        },
        data,
      },
    ],
  };
}

// ===== 双仪表盘 =====
const recommendGaugeEmpty = computed(() => {
  const cr = recommend.clickRate ?? 0;
  const fr = recommend.favoriteRate ?? 0;
  return cr === 0 && fr === 0 && recommendFunnelEmpty.value;
});

const gaugeOption = shallowRef<EChartsOption | null>(null);
const { containerRef: gaugeChartRef } = useChart(gaugeOption);

function buildGaugeOption(): void {
  if (recommendGaugeEmpty.value) {
    gaugeOption.value = null;
    return;
  }
  const clickPct = clampPct((recommend.clickRate ?? 0) * 100);
  const favPct = clampPct((recommend.favoriteRate ?? 0) * 100);
  const baseGauge = {
    type: "gauge" as const,
    startAngle: 220,
    endAngle: -40,
    min: 0,
    max: 100,
    radius: "78%",
    axisLine: {
      lineStyle: {
        width: 12,
        color: [[1, chartPalette.borderSoft]] as [number, string][],
      },
    },
    progress: {
      show: true,
      width: 12,
      roundCap: true,
    },
    pointer: { show: false },
    axisTick: { show: false },
    splitLine: { show: false },
    axisLabel: { show: false },
    anchor: { show: false },
    title: {
      offsetCenter: [0, "68%"],
      fontSize: 12,
      color: chartPalette.text,
      fontFamily: chartTextStyle.fontFamily,
    },
    detail: {
      offsetCenter: [0, 0],
      fontSize: 22,
      fontWeight: 700,
      color: chartPalette.ink,
      fontFamily: chartTextStyle.fontFamily,
      formatter: (val: number) => `${val.toFixed(2)}%`,
    },
  };
  gaugeOption.value = {
    textStyle: chartTextStyle,
    tooltip: { ...chartTooltipStyle, trigger: "item" },
    series: [
      {
        ...baseGauge,
        center: ["28%", "55%"],
        itemStyle: { color: chartPalette.primary },
        progress: { ...baseGauge.progress, itemStyle: { color: chartPalette.primary } },
        data: [{ value: clickPct, name: "点击率" }],
      },
      {
        ...baseGauge,
        center: ["72%", "55%"],
        itemStyle: { color: chartPalette.accent },
        progress: { ...baseGauge.progress, itemStyle: { color: chartPalette.accent } },
        data: [{ value: favPct, name: "收藏率" }],
      },
    ],
  };
}

// ===== LLM 调用分布饼图 =====
const llmPieEmpty = computed(() => (llm.totalCallCount ?? 0) === 0);

const llmPieOption = shallowRef<EChartsOption | null>(null);
const { containerRef: llmPieRef } = useChart(llmPieOption);

function buildLlmPieOption(): void {
  if (llmPieEmpty.value) {
    llmPieOption.value = null;
    return;
  }
  const total = llm.totalCallCount ?? 0;
  llmPieOption.value = {
    textStyle: chartTextStyle,
    tooltip: {
      ...chartTooltipStyle,
      trigger: "item",
      formatter: (params) => {
        const p = params as { name: string; value: number; percent: number };
        return `${p.name}<br/>次数：${formatInt(p.value)}（${p.percent.toFixed(2)}%）`;
      },
    },
    legend: {
      orient: "horizontal",
      bottom: 8,
      left: "center",
      icon: "circle",
      itemWidth: 8,
      itemHeight: 8,
      textStyle: chartTextStyle,
    },
    series: [
      {
        name: "调用结果",
        type: "pie",
        radius: ["55%", "75%"],
        center: ["50%", "45%"],
        avoidLabelOverlap: true,
        itemStyle: {
          borderColor: chartPalette.background,
          borderWidth: 2,
        },
        label: {
          show: true,
          position: "outside",
          color: chartPalette.text,
          fontFamily: chartTextStyle.fontFamily,
          fontSize: 12,
          formatter: (params) => {
            const p = params as { name: string; percent: number };
            return `${p.name} ${p.percent.toFixed(1)}%`;
          },
        },
        labelLine: {
          length: 10,
          length2: 10,
          lineStyle: { color: chartPalette.border },
        },
        data: [
          {
            name: "成功",
            value: llm.successCallCount ?? 0,
            itemStyle: { color: chartPalette.primary },
          },
          {
            name: "失败",
            value: llm.failCallCount ?? 0,
            itemStyle: { color: chartPalette.danger },
          },
        ],
      },
    ],
    graphic: [
      {
        type: "text",
        left: "center",
        top: "38%",
        style: {
          text: "总调用",
          fill: chartPalette.textMuted,
          fontFamily: chartTextStyle.fontFamily,
          fontSize: 12,
        },
      },
      {
        type: "text",
        left: "center",
        top: "46%",
        style: {
          text: formatInt(total),
          fill: chartPalette.ink,
          fontFamily: chartTextStyle.fontFamily,
          fontSize: 22,
          fontWeight: 700,
        },
      },
    ],
  };
}

// ===== 工具函数 =====
const intFormatter = new Intl.NumberFormat("zh-CN");

function formatInt(v?: number | null): string {
  const n = typeof v === "number" && !Number.isNaN(v) ? Math.trunc(v) : 0;
  return intFormatter.format(n);
}

function formatCost(v?: number | null): string {
  const n = typeof v === "number" && !Number.isNaN(v) ? v : 0;
  return n.toFixed(4);
}

function clampPct(v: number): number {
  if (!Number.isFinite(v)) return 0;
  if (v < 0) return 0;
  if (v > 100) return 100;
  return v;
}

// ===== 加载 =====
async function loadOverview(): Promise<void> {
  try {
    Object.assign(overview, await getDashboardOverview());
  } catch {
    /* empty */
  }
}

async function loadHot(): Promise<void> {
  hotLoading.value = true;
  try {
    hotRanking.value = await getDashboardScenicHotRanking();
    buildHotOption();
  } catch {
    hotRanking.value = [];
    hotOption.value = null;
  } finally {
    hotLoading.value = false;
  }
}

async function loadRecommend(): Promise<void> {
  recLoading.value = true;
  try {
    Object.assign(recommend, await getDashboardRecommendAnalysis());
    buildFunnelOption();
    buildGaugeOption();
  } catch {
    /* empty */
  } finally {
    recLoading.value = false;
  }
}

async function loadLlm(): Promise<void> {
  llmLoading.value = true;
  try {
    Object.assign(llm, await getDashboardLlmAnalysis());
    buildLlmPieOption();
  } catch {
    /* empty */
  } finally {
    llmLoading.value = false;
  }
}

onMounted(() => {
  void Promise.all([loadOverview(), loadHot(), loadRecommend(), loadLlm()]);
});
</script>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.section {
  margin-bottom: 32px;
}

.section.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.section.grid-llm {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
}

.llm-side {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  align-content: start;
}

.chart-canvas {
  width: 100%;
  height: 100%;
}

@media (max-width: 1024px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .section.grid-2,
  .section.grid-llm {
    grid-template-columns: 1fr;
  }

  .llm-side {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }

  .llm-side {
    grid-template-columns: 1fr;
  }
}
</style>
