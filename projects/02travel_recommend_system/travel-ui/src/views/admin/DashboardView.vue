<!-- 极简风格管理后台工作台 -->
<template>
  <div class="page">
    <h1 class="title">工作台</h1>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div v-for="s in stats" :key="s.label" class="stat-card">
        <div class="stat-value">{{ s.value }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>

    <!-- 景点热度排行 -->
    <section class="section">
      <h2 class="section-title">景点热度排行</h2>
      <div v-if="hotLoading" class="loading">加载中...</div>
      <div v-else-if="hotRanking.length === 0" class="empty">暂无数据</div>
      <div v-else class="rank-list">
        <div v-for="(item, idx) in hotRanking.slice(0, 10)" :key="item.scenicId" class="rank-item">
          <span class="rank" :class="{ top: idx < 3 }">{{ idx + 1 }}</span>
          <span class="name">{{ item.scenicName }}</span>
          <span class="count">PV {{ item.pvCount || 0 }}</span>
        </div>
      </div>
    </section>

    <!-- 推荐分析 -->
    <section class="section">
      <h2 class="section-title">推荐分析</h2>
      <div v-if="recLoading" class="loading">加载中...</div>
      <div v-else class="stat-grid small">
        <div class="stat-card">
          <div class="stat-value">{{ safeInt(recommend.totalRecommendRequests) }}</div>
          <div class="stat-label">总推荐请求</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ safeInt(recommend.totalRecommendClicks) }}</div>
          <div class="stat-label">总推荐点击</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ formatPercent(recommend.clickRate) }}</div>
          <div class="stat-label">点击率</div>
        </div>
      </div>
    </section>

    <!-- LLM 分析 -->
    <section class="section">
      <h2 class="section-title">LLM 分析</h2>
      <div v-if="llmLoading" class="loading">加载中...</div>
      <div v-else class="stat-grid small">
        <div class="stat-card">
          <div class="stat-value">{{ safeInt(llm.totalCallCount) }}</div>
          <div class="stat-label">总调用次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ safeInt(llm.successCallCount) }}</div>
          <div class="stat-label">成功次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ safeInt(llm.failCallCount) }}</div>
          <div class="stat-label">失败次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ safeInt(llm.totalTokens) }}</div>
          <div class="stat-label">总 Token</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import {
  getDashboardOverview,
  getDashboardScenicHotRanking,
  getDashboardRecommendAnalysis,
  getDashboardLlmAnalysis,
} from "@/api/dashboard";

const hotLoading = ref(false);
const recLoading = ref(false);
const llmLoading = ref(false);

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

const stats = ref<{ label: string; value: number }[]>([]);

function buildStats(): void {
  stats.value = [
    { label: "用户总数", value: overview.totalUsers },
    { label: "景点总数", value: overview.totalScenicSpots },
    { label: "评论总数", value: overview.totalReviews },
    { label: "行程总数", value: overview.totalTravelPlans },
    { label: "推荐请求", value: overview.totalRecommendRequests },
    { label: "LLM 调用", value: overview.totalLlmCalls },
    { label: "浏览总数", value: overview.totalBrowseCount },
    { label: "收藏总数", value: overview.totalFavoriteCount },
  ];
}

const hotRanking = ref<any[]>([]);

async function loadHot(): Promise<void> {
  hotLoading.value = true;
  try {
    hotRanking.value = await getDashboardScenicHotRanking();
  } finally {
    hotLoading.value = false;
  }
}

const recommend = reactive({
  totalRecommendRequests: 0,
  totalRecommendClicks: 0,
  clickRate: 0,
});

async function loadRecommend(): Promise<void> {
  recLoading.value = true;
  try {
    Object.assign(recommend, await getDashboardRecommendAnalysis());
  } finally {
    recLoading.value = false;
  }
}

const llm = reactive({
  totalCallCount: 0,
  successCallCount: 0,
  failCallCount: 0,
  totalTokens: 0,
});

async function loadLlm(): Promise<void> {
  llmLoading.value = true;
  try {
    Object.assign(llm, await getDashboardLlmAnalysis());
  } finally {
    llmLoading.value = false;
  }
}

function safeInt(v?: number): number {
  return typeof v === "number" && !isNaN(v) ? Math.trunc(v) : 0;
}

function formatPercent(v?: number): string {
  return typeof v === "number" && !isNaN(v) ? `${(v * 100).toFixed(2)}%` : "0.00%";
}

onMounted(async () => {
  try {
    Object.assign(overview, await getDashboardOverview());
    buildStats();
  } catch {
    /* empty */
  }
  loadHot();
  loadRecommend();
  loadLlm();
});
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

.title {
  font-size: 32px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 32px 0;
}

/* Stat grid */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 48px;
}

.stat-grid.small {
  grid-template-columns: repeat(4, 1fr);
  margin-bottom: 0;
}

@media (max-width: 1024px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .stat-grid.small {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
  .stat-grid.small {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  background: #ffffff;
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
  color: #000000;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #999999;
}

/* Section */
.section {
  margin-bottom: 48px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 20px 0;
}

/* Rank list */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  transition: border-color 0.2s;
}

.rank-item:hover {
  border-color: #00e676;
}

.rank {
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 700;
  color: #999999;
  background: #f5f5f5;
  border-radius: 50%;
  flex-shrink: 0;
}

.rank.top {
  background: #00e676;
  color: #000000;
}

.name {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: #000000;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.count {
  font-size: 13px;
  color: #999999;
  flex-shrink: 0;
}

/* Loading / Empty */
.loading,
.empty {
  padding: 40px;
  text-align: center;
  color: #999999;
  font-size: 14px;
}
</style>
