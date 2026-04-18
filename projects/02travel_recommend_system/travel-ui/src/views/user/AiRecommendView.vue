<template>
  <div class="recommend-page">
    <section class="hero">
      <div>
        <p class="hero-eyebrow">AI 推荐</p>
        <h1>基于召回结果生成更自然的推荐理由</h1>
        <p class="hero-desc">
          当前列表保留规则排序分
          <code>rankScore</code>
          ，推荐理由优先由 LLM 生成，失败时自动降级为规则文案。
        </p>
      </div>
    </section>

    <el-card class="page-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>推荐结果</span>
          <el-button :loading="loading" @click="loadRecommendations">刷新推荐</el-button>
        </div>
      </template>

      <el-skeleton v-if="loading" animated :rows="8" />
      <el-empty v-else-if="page.records.length === 0" description="暂无推荐结果" />

      <div v-else class="recommend-list">
        <article
          v-for="item in page.records"
          :key="`${item.recommendRecordId}-${item.resultItemId}-${item.scenicId}`"
          class="recommend-card"
          @click="goToDetail(item)"
        >
          <el-image class="cover" :src="item.coverImage || ''" fit="cover">
            <template #error>
              <div class="cover-fallback">暂无图片</div>
            </template>
          </el-image>
          <div class="recommend-body">
            <div class="recommend-top">
              <h3>{{ item.scenicName }}</h3>
              <div class="score-group">
                <span class="score-label">公共评分</span>
                <strong>{{ formatScore(item.score) }}</strong>
              </div>
            </div>
            <p class="reason">{{ item.reason || "正在生成推荐理由" }}</p>
            <div class="meta">
              <span>来源：{{ item.sourceType || "-" }}</span>
              <span>排序分：{{ formatRankScore(item.rankScore) }}</span>
            </div>
          </div>
        </article>
      </div>

      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="query.pageNum"
          :page-size="query.pageSize"
          :total="page.total"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { fetchAiRecommendations, type AiRecommendItem } from "@/api/recommend";
import type { PageResult } from "@/types/api";

const router = useRouter();

const loading = ref(false);
const query = reactive({
  pageNum: 1,
  pageSize: 8,
});
const page = ref<PageResult<AiRecommendItem>>({
  records: [],
  total: 0,
  pageNum: 1,
  pageSize: 8,
  totalPage: 0,
});

function formatScore(score?: number): string {
  return typeof score === "number" ? score.toFixed(1) : "0.0";
}

function formatRankScore(score?: number): string {
  return typeof score === "number" ? score.toFixed(2) : "0.00";
}

async function loadRecommendations(): Promise<void> {
  loading.value = true;
  try {
    page.value = await fetchAiRecommendations(query);
  } catch (error) {
    console.error(error);
    ElMessage.error("推荐结果加载失败");
  } finally {
    loading.value = false;
  }
}

function onPageChange(pageNum: number): void {
  query.pageNum = pageNum;
  void loadRecommendations();
}

function goToDetail(item: AiRecommendItem): void {
  void router.push(`/scenic/${item.scenicId}`);
}

onMounted(() => {
  void loadRecommendations();
});
</script>

<style scoped>
.recommend-page {
  max-width: 1200px;
  padding: 24px;
  margin: 0 auto;
}

.hero {
  padding: 24px 28px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #f4fbff 0%, #eef8f3 100%);
  border: 1px solid #e6eef0;
  border-radius: 24px;
}

.hero-eyebrow {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 600;
  color: #3b82f6;
}

.hero h1 {
  margin: 0 0 12px;
  font-size: 30px;
  font-weight: 700;
  color: #111827;
}

.hero-desc {
  margin: 0;
  line-height: 1.7;
  color: #4b5563;
}

.page-card {
  border-radius: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.recommend-list {
  display: grid;
  gap: 16px;
}

.recommend-card {
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr);
  gap: 18px;
  padding: 16px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #e9edf2;
  border-radius: 20px;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.recommend-card:hover {
  border-color: #bfd6ff;
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.08);
  transform: translateY(-1px);
}

.cover {
  width: 100%;
  height: 122px;
  overflow: hidden;
  border-radius: 16px;
}

.cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #9ca3af;
  background: #f3f4f6;
}

.recommend-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.recommend-top {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 10px;
}

.recommend-top h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.score-group {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  min-width: 90px;
  color: #6b7280;
}

.score-group strong {
  font-size: 24px;
  color: #0f172a;
}

.score-label {
  font-size: 12px;
}

.reason {
  margin: 0 0 14px;
  font-size: 15px;
  line-height: 1.8;
  color: #374151;
}

.meta {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
  margin-top: auto;
  font-size: 13px;
  color: #6b7280;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .recommend-page {
    padding: 16px;
  }

  .recommend-card {
    grid-template-columns: 1fr;
  }

  .cover {
    height: 180px;
  }

  .recommend-top {
    flex-direction: column;
  }

  .score-group {
    align-items: flex-start;
  }
}
</style>
