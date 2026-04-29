<!-- 极简风格AI推荐页 -->
<template>
  <div class="page">
    <div class="header">
      <h1 class="title">AI 推荐</h1>
      <p class="subtitle">基于智能算法为您推荐景点</p>
      <button class="btn-refresh" :disabled="loading" @click="loadData">刷新推荐</button>
    </div>

    <div v-if="loading" class="grid">
      <div v-for="n in 6" :key="n" class="skeleton-card">
        <div class="skeleton-img"></div>
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
      </div>
    </div>

    <div v-else-if="list.length === 0" class="empty">暂无推荐结果</div>

    <div v-else class="grid">
      <div v-for="item in list" :key="item.scenicId" class="card" @click="goDetail(item)">
        <div class="card-img">
          <img :src="item.coverImage || ''" :alt="item.scenicName" />
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ item.scenicName }}</h3>
          <p class="card-reason">{{ item.reason || "为您智能推荐" }}</p>
          <div class="card-meta">
            <span v-if="item.score" class="score">{{ item.score.toFixed(1) }}分</span>
            <span v-if="item.sourceType" class="source">{{ item.sourceType }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="total > 0" class="pagination">
      <button
        class="page-btn"
        :disabled="query.pageNum <= 1"
        @click="changePage(query.pageNum - 1)"
      >
        上一页
      </button>
      <span class="page-info">{{ query.pageNum }} / {{ Math.ceil(total / query.pageSize) }}</span>
      <button
        class="page-btn"
        :disabled="query.pageNum >= Math.ceil(total / query.pageSize)"
        @click="changePage(query.pageNum + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { fetchAiRecommendations, sendRecommendClick, type AiRecommendItem } from "@/api/recommend";

const router = useRouter();
const loading = ref(false);
const list = ref<AiRecommendItem[]>([]);
const total = ref(0);

const query = reactive({
  pageNum: 1,
  pageSize: 8,
});

function goDetail(item: AiRecommendItem): void {
  if (item.recommendRecordId && item.resultItemId && item.scenicId) {
    sendRecommendClick({
      recommendRecordId: item.recommendRecordId,
      resultItemId: item.resultItemId,
      scenicId: item.scenicId,
    }).catch(() => {});
  }
  router.push(`/scenic/${item.scenicId}`);
}

function changePage(page: number): void {
  query.pageNum = page;
  loadData();
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await fetchAiRecommendations(query);
    list.value = res.records || [];
    total.value = res.total || 0;
  } catch {
    alert("推荐结果加载失败");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.page {
  max-width: 1200px;
  padding: 40px 24px;
  margin: 0 auto;
}

.header {
  margin-bottom: 40px;
}

.title {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 700;
  color: #000000;
}

.subtitle {
  margin: 0 0 20px 0;
  font-size: 15px;
  color: #999999;
}

.btn-refresh {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
  transition: background 0.2s;
}

.btn-refresh:hover:not(:disabled) {
  background: #00c665;
}

.btn-refresh:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* Grid */
.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

@media (max-width: 1024px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .grid {
    grid-template-columns: 1fr;
  }
}

/* Card */
.card {
  cursor: pointer;
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-4px);
}

.card-img {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
  border-radius: 12px;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-body {
  padding: 16px 4px;
}

.card-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #000000;
}

.card-reason {
  margin: 0 0 12px 0;
  font-size: 14px;
  line-height: 1.6;
  color: #666666;
}

.card-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.score {
  padding: 2px 8px;
  font-size: 14px;
  font-weight: 600;
  color: #000000;
  background: #f0f0f0;
  border-radius: 4px;
}

.source {
  font-size: 12px;
  color: #999999;
}

/* Pagination */
.pagination {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: center;
  margin-top: 40px;
}

.page-btn {
  padding: 10px 20px;
  font-size: 14px;
  color: #000000;
  cursor: pointer;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #f9fff9;
  border-color: #00e676;
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.page-info {
  font-size: 14px;
  color: #999999;
}

/* Empty */
.empty {
  padding: 80px 20px;
  font-size: 15px;
  color: #999999;
  text-align: center;
}

/* Skeleton */
.skeleton-card {
  overflow: hidden;
  border-radius: 12px;
}

.skeleton-img {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
}

.skeleton-line {
  height: 16px;
  margin-top: 12px;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  animation: loading 1.5s infinite;
}

.skeleton-line.short {
  width: 60%;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
