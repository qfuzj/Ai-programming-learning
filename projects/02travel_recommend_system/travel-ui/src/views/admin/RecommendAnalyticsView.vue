<!-- 推荐分析仪表盘：KPI + 趋势 + 漏斗 + 转化结构 -->
<template>
  <div class="admin-page">
    <div class="page-head">
      <div>
        <p class="eyebrow">RECOMMEND ANALYTICS</p>
        <h1>推荐效果分析</h1>
      </div>
    </div>

    <!-- KPI 概览 -->
    <section class="kpi-grid">
      <StatCard label="总推荐请求" :value="formatInt(data.totalRecommendRequests)" tone="default" />
      <StatCard label="总推荐点击" :value="formatInt(data.totalRecommendClicks)" tone="accent" />
      <StatCard
        label="总推荐收藏"
        :value="formatInt(data.totalRecommendFavorites)"
        tone="primary"
      />
      <StatCard label="点击率" :value="formatPercent(data.clickRate)" tone="warning" />
      <StatCard label="收藏率" :value="formatPercent(data.favoriteRate)" tone="primary" />
    </section>

    <!-- 每日趋势 -->
    <section class="section-block">
      <ChartCard
        title="近 N 日推荐趋势"
        :subtitle="trendSubtitle"
        :loading="loading"
        :empty="!loading && !hasTrend"
        empty-text="暂无趋势数据"
        body-height="320px"
      >
        <div ref="trendRef" class="chart-host" />
      </ChartCard>
    </section>

    <!-- 漏斗 + 仪表盘 -->
    <section class="section-block grid-two">
      <ChartCard
        title="推荐转化漏斗"
        subtitle="请求 → 点击 → 收藏"
        :loading="loading"
        :empty="!loading && !hasFunnel"
        empty-text="暂无转化数据"
        body-height="320px"
      >
        <div ref="funnelRef" class="chart-host" />
      </ChartCard>

      <ChartCard
        title="转化率仪表盘"
        subtitle="点击率 / 收藏率"
        :loading="loading"
        body-height="320px"
      >
        <div ref="gaugeRef" class="chart-host" />
      </ChartCard>
    </section>

    <!-- 累计达成 -->
    <section class="section-block">
      <ChartCard
        title="累计转化结构"
        subtitle="请求 / 点击 / 收藏 三者占比"
        :loading="loading"
        :empty="!loading && !hasFunnel"
        empty-text="暂无累计数据"
        body-height="300px"
      >
        <div ref="pieRef" class="chart-host" />
      </ChartCard>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, type Ref } from "vue";
import { getDashboardRecommendAnalysis } from "@/api/dashboard";
import StatCard from "@/components/analytics/StatCard.vue";
import ChartCard from "@/components/analytics/ChartCard.vue";
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
const data = reactive({
  totalRecommendRequests: 0,
  totalRecommendClicks: 0,
  totalRecommendFavorites: 0,
  clickRate: 0,
  favoriteRate: 0,
  dates: [] as string[],
  requestCounts: [] as number[],
  clickCounts: [] as number[],
  favoriteCounts: [] as number[],
});

function safeInt(v?: number): number {
  return typeof v === "number" && !isNaN(v) ? Math.trunc(v) : 0;
}
function formatInt(v?: number): string {
  return safeInt(v).toLocaleString("en-US");
}
function formatPercent(v?: number): string {
  return typeof v === "number" && !isNaN(v) ? `${(v * 100).toFixed(2)}%` : "0.00%";
}

const hasTrend = computed(() => (data.dates?.length ?? 0) > 0);
const hasFunnel = computed(
  () =>
    safeInt(data.totalRecommendRequests) +
      safeInt(data.totalRecommendClicks) +
      safeInt(data.totalRecommendFavorites) >
    0
);

const trendSubtitle = computed(() => {
  if (!hasTrend.value) return "等待数据生成";
  const dates = data.dates;
  return `${dates[0]} ~ ${dates[dates.length - 1]} 共 ${dates.length} 天`;
});

const trendOption: Ref<EChartsOption | null> = ref(null);
const funnelOption: Ref<EChartsOption | null> = ref(null);
const gaugeOption: Ref<EChartsOption | null> = ref(null);
const pieOption: Ref<EChartsOption | null> = ref(null);

const { containerRef: trendRef } = useChart(trendOption);
const { containerRef: funnelRef } = useChart(funnelOption);
const { containerRef: gaugeRef } = useChart(gaugeOption);
const { containerRef: pieRef } = useChart(pieOption);

function buildTrendOption(): EChartsOption {
  return {
    color: [chartSeries[0], chartSeries[1], chartSeries[2]],
    textStyle: chartTextStyle,
    tooltip: {
      trigger: "axis",
      ...chartTooltipStyle,
    },
    legend: {
      top: 4,
      icon: "roundRect",
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: chartPalette.text, fontSize: 12 },
      data: ["请求数", "点击数", "收藏数"],
    },
    grid: { ...chartGrid, top: 48 },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: data.dates,
      ...chartAxisLine,
    },
    yAxis: {
      type: "value",
      ...chartAxisLine,
    },
    series: [
      {
        name: "请求数",
        type: "line",
        smooth: true,
        symbol: "circle",
        symbolSize: 6,
        lineStyle: { width: 2 },
        data: data.requestCounts,
      },
      {
        name: "点击数",
        type: "line",
        smooth: true,
        symbol: "circle",
        symbolSize: 6,
        lineStyle: { width: 2 },
        data: data.clickCounts,
      },
      {
        name: "收藏数",
        type: "line",
        smooth: true,
        symbol: "circle",
        symbolSize: 6,
        lineStyle: { width: 2 },
        data: data.favoriteCounts,
      },
    ],
  };
}

function buildFunnelOption(): EChartsOption {
  const requests = safeInt(data.totalRecommendRequests);
  const clicks = safeInt(data.totalRecommendClicks);
  const favorites = safeInt(data.totalRecommendFavorites);
  const max = Math.max(requests, clicks, favorites, 1);
  return {
    textStyle: chartTextStyle,
    tooltip: {
      trigger: "item",
      formatter: ((params: { name: string; value: number }) => {
        const ratio = requests > 0 ? ((params.value / requests) * 100).toFixed(2) : "0.00";
        return `${params.name}<br/>数量：${params.value.toLocaleString("en-US")}<br/>占请求比：${ratio}%`;
      }) as never,
      ...chartTooltipStyle,
    },
    series: [
      {
        type: "funnel" as const,
        left: "10%",
        right: "10%",
        top: 16,
        bottom: 16,
        min: 0,
        max,
        minSize: "20%",
        maxSize: "100%",
        sort: "descending",
        gap: 4,
        label: {
          show: true,
          position: "inside",
          color: "#fff",
          fontSize: 12,
          formatter: ((params: { name: string; value: number }) => {
            const ratio = requests > 0 ? ((params.value / requests) * 100).toFixed(1) : "0.0";
            return `${params.name} ${params.value.toLocaleString("en-US")} (${ratio}%)`;
          }) as never,
        },
        labelLine: { show: false },
        itemStyle: {
          borderColor: "#fff",
          borderWidth: 2,
        },
        data: [
          { value: requests, name: "推荐请求", itemStyle: { color: chartPalette.primary } },
          { value: clicks, name: "推荐点击", itemStyle: { color: chartPalette.accent } },
          { value: favorites, name: "推荐收藏", itemStyle: { color: chartPalette.warning } },
        ],
      },
    ],
  };
}

function buildGaugeOption(): EChartsOption {
  const clickPct = Math.max(0, Math.min(1, data.clickRate ?? 0)) * 100;
  const favPct = Math.max(0, Math.min(1, data.favoriteRate ?? 0)) * 100;
  const baseGauge = {
    type: "gauge" as const,
    startAngle: 200,
    endAngle: -20,
    min: 0,
    max: 100,
    radius: "80%",
    progress: {
      show: true,
      width: 14,
      roundCap: true,
    },
    axisLine: {
      lineStyle: {
        width: 14,
        color: [[1, chartPalette.borderSoft]] as Array<[number, string]>,
      },
    },
    pointer: { show: false },
    axisTick: { show: false },
    splitLine: { show: false },
    axisLabel: { show: false },
    title: {
      show: true,
      offsetCenter: [0, "70%"],
      color: chartPalette.text,
      fontSize: 12,
    },
    detail: {
      valueAnimation: true,
      offsetCenter: [0, "0%"],
      fontSize: 22,
      fontWeight: 700,
      color: chartPalette.ink,
      formatter: (v: number) => `${v.toFixed(2)}%`,
    },
  };
  return {
    textStyle: chartTextStyle,
    tooltip: {
      formatter: ((p: { seriesName: string; value: number }) =>
        `${p.seriesName}：${p.value.toFixed(2)}%`) as never,
      ...chartTooltipStyle,
    },
    series: [
      {
        ...baseGauge,
        name: "点击率",
        center: ["27%", "55%"],
        progress: { ...baseGauge.progress, itemStyle: { color: chartPalette.accent } },
        data: [{ value: Number(clickPct.toFixed(2)), name: "点击率" }],
      },
      {
        ...baseGauge,
        name: "收藏率",
        center: ["73%", "55%"],
        progress: { ...baseGauge.progress, itemStyle: { color: chartPalette.primary } },
        data: [{ value: Number(favPct.toFixed(2)), name: "收藏率" }],
      },
    ],
  };
}

function buildPieOption(): EChartsOption {
  const requests = safeInt(data.totalRecommendRequests);
  const clicks = safeInt(data.totalRecommendClicks);
  const favorites = safeInt(data.totalRecommendFavorites);
  return {
    color: [chartSeries[0], chartSeries[1], chartSeries[2]],
    textStyle: chartTextStyle,
    tooltip: {
      trigger: "item",
      formatter: ((p: { name: string; value: number; percent: number }) =>
        `${p.name}<br/>数量：${p.value.toLocaleString("en-US")}<br/>占比：${p.percent.toFixed(2)}%`) as never,
      ...chartTooltipStyle,
    },
    legend: {
      orient: "vertical",
      right: 24,
      top: "center",
      icon: "roundRect",
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: chartPalette.text, fontSize: 12 },
    },
    series: [
      {
        type: "pie",
        radius: ["45%", "70%"],
        center: ["38%", "50%"],
        avoidLabelOverlap: true,
        itemStyle: {
          borderColor: "#fff",
          borderWidth: 2,
          borderRadius: 6,
        },
        label: {
          show: true,
          position: "outside",
          color: chartPalette.text,
          fontSize: 12,
          formatter: "{b}\n{d}%",
        },
        labelLine: { length: 12, length2: 8 },
        data: [
          { name: "推荐请求", value: requests },
          { name: "推荐点击", value: clicks },
          { name: "推荐收藏", value: favorites },
        ],
      },
      {
        type: "pie",
        radius: ["0%", "0%"],
        center: ["38%", "50%"],
        silent: true,
        label: {
          show: true,
          position: "center",
          formatter: () => `{a|累计请求}\n{b|${requests.toLocaleString("en-US")}}`,
          rich: {
            a: { color: chartPalette.textMuted, fontSize: 12, lineHeight: 20 },
            b: { color: chartPalette.ink, fontSize: 22, fontWeight: 700, lineHeight: 28 },
          },
        },
        data: [{ value: 1, name: "center" }],
        tooltip: { show: false },
      },
    ],
  };
}

function refreshOptions(): void {
  trendOption.value = hasTrend.value ? buildTrendOption() : null;
  funnelOption.value = hasFunnel.value ? buildFunnelOption() : null;
  gaugeOption.value = buildGaugeOption();
  pieOption.value = hasFunnel.value ? buildPieOption() : null;
}

onMounted(async () => {
  loading.value = true;
  try {
    const res = await getDashboardRecommendAnalysis();
    data.totalRecommendRequests = safeInt(res.totalRecommendRequests);
    data.totalRecommendClicks = safeInt(res.totalRecommendClicks);
    data.totalRecommendFavorites = safeInt(res.totalRecommendFavorites);
    data.clickRate = typeof res.clickRate === "number" ? res.clickRate : 0;
    data.favoriteRate = typeof res.favoriteRate === "number" ? res.favoriteRate : 0;
    data.dates = Array.isArray(res.dates) ? res.dates : [];
    data.requestCounts = Array.isArray(res.requestCounts) ? res.requestCounts : [];
    data.clickCounts = Array.isArray(res.clickCounts) ? res.clickCounts : [];
    data.favoriteCounts = Array.isArray(res.favoriteCounts) ? res.favoriteCounts : [];
  } catch {
    /* empty */
  } finally {
    loading.value = false;
    refreshOptions();
  }
});
</script>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.section-block {
  margin-bottom: 32px;
}

.section-block:last-child {
  margin-bottom: 0;
}

.grid-two {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-host {
  width: 100%;
  height: 100%;
}

@media (max-width: 1024px) {
  .kpi-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .grid-two {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
