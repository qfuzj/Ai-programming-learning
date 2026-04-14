<template>
  <div class="page-container">
    <el-card class="page-card" v-loading="loading">
      <template #header>
        <div class="card-header">推荐分析</div>
      </template>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="总推荐请求数">{{ safeInt(summary.totalRecommendRequests) }}</el-descriptions-item>
        <el-descriptions-item label="总推荐点击数">{{ safeInt(summary.totalRecommendClicks) }}</el-descriptions-item>
        <el-descriptions-item label="总推荐收藏数">{{ safeInt(summary.totalRecommendFavorites) }}</el-descriptions-item>
        <el-descriptions-item label="点击率">{{ formatPercent(summary.clickRate) }}</el-descriptions-item>
        <el-descriptions-item label="收藏率">{{ formatPercent(summary.favoriteRate) }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">趋势数据</el-divider>
      <el-table :data="trendRows">
        <el-table-column prop="date" label="日期" min-width="120" />
        <el-table-column prop="requestCount" label="请求数" min-width="100" />
        <el-table-column prop="clickCount" label="点击数" min-width="100" />
        <el-table-column prop="favoriteCount" label="收藏数" min-width="100" />
      </el-table>

      <el-divider content-position="left">景点推荐热度排行</el-divider>
      <el-table :data="hotRanking">
        <el-table-column label="排名" width="70">
          <template #default="scope">{{ scope.$index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="scenicName" label="景点名称" min-width="160" />
        <el-table-column prop="recommendShowCount" label="推荐展示数" min-width="120" />
        <el-table-column prop="recommendClickCount" label="推荐点击数" min-width="120" />
        <el-table-column label="点击率" min-width="120">
          <template #default="scope">
            {{ calcClickRate(scope.row.recommendClickCount, scope.row.recommendShowCount) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  getDashboardRecommendAnalysis,
  getDashboardScenicHotRanking,
  type RecommendAnalysisSummary,
  type ScenicHotRankingItem,
} from "@/api/dashboard";

interface RecommendTrendRow {
  date: string;
  requestCount: number;
  clickCount: number;
  favoriteCount: number;
}

const loading = ref(false);
const summary = reactive<RecommendAnalysisSummary>({});
const hotRanking = ref<ScenicHotRankingItem[]>([]);

const trendRows = computed<RecommendTrendRow[]>(() => {
  const dates = summary.dates ?? [];
  const requestCounts = summary.requestCounts ?? [];
  const clickCounts = summary.clickCounts ?? [];
  const favoriteCounts = summary.favoriteCounts ?? [];
  return dates.map((date, index) => ({
    date,
    requestCount: safeInt(requestCounts[index]),
    clickCount: safeInt(clickCounts[index]),
    favoriteCount: safeInt(favoriteCounts[index]),
  }));
});

function safeInt(value?: number): number {
  if (value == null || Number.isNaN(Number(value))) {
    return 0;
  }
  return Math.trunc(Number(value));
}

function formatPercent(value?: number): string {
  if (value == null || Number.isNaN(Number(value))) {
    return "0.00%";
  }
  return `${(Number(value) * 100).toFixed(2)}%`;
}

function calcClickRate(click?: number, show?: number): string {
  const clickValue = Number(click ?? 0);
  const showValue = Number(show ?? 0);
  if (showValue <= 0) {
    return "0.00%";
  }
  return `${((clickValue / showValue) * 100).toFixed(2)}%`;
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const [recommendSummary, ranking] = await Promise.all([
      getDashboardRecommendAnalysis(),
      getDashboardScenicHotRanking(),
    ]);
    Object.assign(summary, recommendSummary);
    hotRanking.value = ranking;
  } catch {
    ElMessage.error("推荐分析数据加载失败");
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
