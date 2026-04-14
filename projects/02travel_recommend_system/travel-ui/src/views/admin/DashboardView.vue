<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">工作台</div>
      </template>

      <el-row :gutter="12">
        <el-col :span="6"><el-statistic title="用户总数" :value="overview.totalUsers" /></el-col>
        <el-col :span="6">
          <el-statistic title="景点总数" :value="overview.totalScenicSpots" />
        </el-col>
        <el-col :span="6"><el-statistic title="评论总数" :value="overview.totalReviews" /></el-col>
        <el-col :span="6"><el-statistic title="旅行计划数" :value="overview.totalPlans" /></el-col>
      </el-row>
      <el-row :gutter="12" style="margin-top: 12px">
        <el-col :span="6">
          <el-statistic title="推荐请求数" :value="overview.totalRecommendRequests" />
        </el-col>
        <el-col :span="6"><el-statistic title="LLM 调用数" :value="overview.totalLlmCalls" /></el-col>
        <el-col :span="6"><el-statistic title="浏览总数" :value="overview.totalBrowseCount" /></el-col>
        <el-col :span="6">
          <el-statistic title="收藏总数" :value="overview.totalFavoriteCount" />
        </el-col>
      </el-row>

      <el-divider content-position="left">景点热度排行榜</el-divider>
      <el-table v-loading="hotLoading" :data="hotRanking.slice(0, 20)">
        <el-table-column label="排名" width="70">
          <template #default="scope">{{ scope.$index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="scenicName" label="景点名称" min-width="160" />
        <el-table-column prop="pvCount" label="PV" width="90" />
        <el-table-column prop="uvCount" label="UV" width="90" />
        <el-table-column prop="favoriteCount" label="收藏数" width="100" />
        <el-table-column prop="reviewCount" label="评论数" width="100" />
        <el-table-column prop="recommendShowCount" label="推荐展示数" width="120" />
        <el-table-column prop="recommendClickCount" label="推荐点击数" width="120" />
        <el-table-column label="平均评分" width="100">
          <template #default="scope">{{ formatOneDecimal(scope.row.avgRating) }}</template>
        </el-table-column>
      </el-table>

      <el-divider content-position="left">推荐分析摘要</el-divider>
      <el-descriptions :column="3" border v-loading="recommendLoading">
        <el-descriptions-item label="总推荐请求数">{{ safeInt(recommend.totalRecommendRequests) }}</el-descriptions-item>
        <el-descriptions-item label="总推荐点击数">{{ safeInt(recommend.totalRecommendClicks) }}</el-descriptions-item>
        <el-descriptions-item label="总推荐收藏数">{{ safeInt(recommend.totalRecommendFavorites) }}</el-descriptions-item>
        <el-descriptions-item label="点击率">{{ formatPercent(recommend.clickRate) }}</el-descriptions-item>
        <el-descriptions-item label="收藏率">{{ formatPercent(recommend.favoriteRate) }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">LLM 分析摘要</el-divider>
      <el-descriptions :column="3" border v-loading="llmLoading">
        <el-descriptions-item label="总调用次数">{{ safeInt(llm.totalCallCount) }}</el-descriptions-item>
        <el-descriptions-item label="成功次数">{{ safeInt(llm.successCallCount) }}</el-descriptions-item>
        <el-descriptions-item label="失败次数">{{ safeInt(llm.failCallCount) }}</el-descriptions-item>
        <el-descriptions-item label="总 Token 数">{{ safeInt(llm.totalTokens) }}</el-descriptions-item>
        <el-descriptions-item label="总费用">{{ formatCost(llm.totalCostAmount) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  getDashboardLlmAnalysis,
  getDashboardOverview,
  getDashboardRecommendAnalysis,
  getDashboardScenicHotRanking,
  type DashboardOverview,
  type LlmAnalysisSummary,
  type RecommendAnalysisSummary,
  type ScenicHotRankingItem,
} from "@/api/dashboard";

const hotLoading = ref(false);
const recommendLoading = ref(false);
const llmLoading = ref(false);

const overview = reactive<DashboardOverview>({
  totalUsers: 0,
  totalScenicSpots: 0,
  totalReviews: 0,
  totalPlans: 0,
  totalRecommendRequests: 0,
  totalLlmCalls: 0,
  totalBrowseCount: 0,
  totalFavoriteCount: 0,
});

const hotRanking = ref<ScenicHotRankingItem[]>([]);
const recommend = reactive<RecommendAnalysisSummary>({});
const llm = reactive<LlmAnalysisSummary>({});

function safeInt(value?: number): number {
  if (value == null || Number.isNaN(Number(value))) {
    return 0;
  }
  return Math.trunc(Number(value));
}

function formatOneDecimal(value?: number): string {
  if (value == null || Number.isNaN(Number(value))) {
    return "-";
  }
  return Number(value).toFixed(1);
}

function formatPercent(value?: number): string {
  if (value == null || Number.isNaN(Number(value))) {
    return "0.00%";
  }
  return `${(Number(value) * 100).toFixed(2)}%`;
}

function formatCost(value?: number): string {
  if (value == null || Number.isNaN(Number(value))) {
    return "0.0000 元";
  }
  return `${Number(value).toFixed(4)} 元`;
}

async function loadOverview(): Promise<void> {
  try {
    const data = await getDashboardOverview();
    Object.assign(overview, data);
  } catch {
    ElMessage.warning("概览数据加载失败");
  }
}

async function loadHotRanking(): Promise<void> {
  hotLoading.value = true;
  try {
    hotRanking.value = await getDashboardScenicHotRanking();
  } catch {
    ElMessage.warning("热度排行加载失败");
  } finally {
    hotLoading.value = false;
  }
}

async function loadRecommend(): Promise<void> {
  recommendLoading.value = true;
  try {
    Object.assign(recommend, await getDashboardRecommendAnalysis());
  } catch {
    ElMessage.warning("推荐分析加载失败");
  } finally {
    recommendLoading.value = false;
  }
}

async function loadLlm(): Promise<void> {
  llmLoading.value = true;
  try {
    Object.assign(llm, await getDashboardLlmAnalysis());
  } catch {
    ElMessage.warning("LLM 分析加载失败");
  } finally {
    llmLoading.value = false;
  }
}

onMounted(() => {
  void Promise.all([loadOverview(), loadHotRanking(), loadRecommend(), loadLlm()]);
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
