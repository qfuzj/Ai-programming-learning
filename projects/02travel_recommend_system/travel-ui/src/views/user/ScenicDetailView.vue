<template>
  <div v-if="detail" class="page">
    <section class="hero">
      <div class="container">
        <div class="breadcrumb-line">
          <span>景点</span>
          <span v-if="detail.regionName">{{ detail.regionName }}</span>
          <span>{{ detail.name }}</span>
        </div>

        <div class="hero-header">
          <div class="title-block">
            <div class="eyebrow">
              <span v-if="detail.category">{{ detail.category }}</span>
              <span v-if="detail.level">{{ detail.level }}</span>
              <span v-if="detail.isRecommended">推荐景点</span>
            </div>
            <h1>{{ detail.name }}</h1>
            <div class="rating-row">
              <span class="score">{{ formattedScore }}</span>
              <span class="rating-dots" :aria-label="`${formattedScore} 分`">
                <i
                  v-for="i in 5"
                  :key="i"
                  :class="{ filled: i <= Math.round(detail.score || 0) }"
                ></i>
              </span>
              <button v-if="reviewCountText" class="text-link" @click="scrollToReviews">
                {{ reviewCountText }}
              </button>
            </div>
          </div>

          <div class="hero-side">
            <div class="hero-actions">
              <button
                class="outline-btn"
                :class="{ active: detail.isFavorite }"
                @click="toggleFavorite"
              >
                <el-icon><Star /></el-icon>
                {{ detail.isFavorite ? "已收藏" : "收藏" }}
              </button>
              <button class="outline-btn" @click="openReviewForm">
                <el-icon><EditPen /></el-icon>
                点评
              </button>
            </div>
            <dl v-if="statItems.length > 0" class="stat-strip">
              <div v-for="item in statItems" :key="item.label">
                <dt>{{ item.label }}</dt>
                <dd>{{ item.value }}</dd>
              </div>
            </dl>
          </div>
        </div>

        <div class="photo-grid" :class="{ empty: galleryImages.length === 0 }">
          <template v-if="galleryImages.length > 0">
            <div
              v-for="(image, index) in galleryImages"
              :key="`${image}-${index}`"
              class="photo-cell"
              :class="{ primary: index === 0 }"
            >
              <img :src="image" :alt="`${detail.name} 图片 ${index + 1}`" />
              <div v-if="index === 0 && imageCount > 0" class="photo-count">
                <el-icon><Picture /></el-icon>
                {{ imageCount }}
              </div>
            </div>
          </template>
          <div v-else class="photo-placeholder">
            <el-icon><Picture /></el-icon>
            <span>暂无图片</span>
          </div>
        </div>

        <nav class="section-nav" aria-label="景点详情导航">
          <button @click="scrollToSection('intro-section')">概览</button>
          <button v-if="infoItems.length > 0" @click="scrollToSection('info-section')">详情</button>
          <button @click="scrollToSection('reviews-section')">点评</button>
          <button v-if="similarList.length > 0" @click="scrollToSection('similar-section')">
            相似推荐
          </button>
        </nav>
      </div>
    </section>

    <main class="detail-layout container">
      <div class="main-column">
        <section id="intro-section" class="section intro-section">
          <h2>相关信息</h2>
          <p v-if="detail.description" class="description">{{ detail.description }}</p>
          <div
            v-if="detail.detailContent"
            class="detail-content"
            v-html="detail.detailContent"
          ></div>
          <p v-if="!detail.description && !detail.detailContent" class="muted">暂无景点介绍。</p>
        </section>

        <section v-if="infoItems.length > 0" id="info-section" class="section">
          <h2>实用信息</h2>
          <div class="info-grid">
            <div v-for="item in infoItems" :key="item.label" class="info-item">
              <el-icon><component :is="item.icon" /></el-icon>
              <div>
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
          </div>
        </section>

        <section v-if="detail.tagList && detail.tagList.length > 0" class="section">
          <h2>标签</h2>
          <div class="tag-list">
            <span v-for="tag in detail.tagList" :key="tag" class="tag">{{ tag }}</span>
          </div>
        </section>

        <section id="reviews-section" class="section reviews-section">
          <div class="section-heading">
            <h2>点评 {{ reviewsTotal ? `(${reviewsTotal})` : "" }}</h2>
            <button v-if="!showReviewForm" class="pill-btn small" @click="openReviewForm">
              写点评
            </button>
          </div>

          <div v-if="showReviewForm" class="review-form">
            <ReviewWriteForm
              :form="reviewForm"
              :loading="reviewSubmitLoading"
              @submit="submitReview"
            />
            <button class="ghost-btn review-cancel" @click="showReviewForm = false">取消</button>
          </div>

          <div v-if="reviewsLoading" class="state-block">加载中...</div>
          <div v-else-if="reviews.length === 0" class="state-block">暂无点评</div>
          <div v-else class="review-list">
            <ReviewItemCard
              v-for="(review, index) in reviews"
              :key="review.id"
              :review="review"
              @update-review="(updated) => updateReviewItem(index, updated)"
            />
          </div>

          <div v-if="reviewsTotal > 0" class="pagination">
            <button
              class="page-btn"
              :disabled="reviewQuery.pageNum <= 1"
              @click="changeReviewPage(reviewQuery.pageNum - 1)"
            >
              上一页
            </button>
            <span>{{ reviewQuery.pageNum }} / {{ totalReviewPages }}</span>
            <button
              class="page-btn"
              :disabled="reviewQuery.pageNum >= totalReviewPages"
              @click="changeReviewPage(reviewQuery.pageNum + 1)"
            >
              下一页
            </button>
          </div>
        </section>
      </div>

      <aside class="sidebar">
        <div class="side-card summary-card">
          <div v-if="detail.ticketPrice !== undefined" class="summary-price">
            <span>门票参考</span>
            <strong>¥{{ detail.ticketPrice }}</strong>
          </div>
          <div v-else class="summary-price">
            <span>门票参考</span>
            <strong>暂无信息</strong>
          </div>
          <p>页面信息来自景点资料、游客点评和平台统计。</p>
          <button class="pill-btn full" @click="scrollToInfo">查看实用信息</button>
        </div>

        <div class="side-card hours-card">
          <div class="side-card-header">
            <h2>开放信息</h2>
            <el-icon><Clock /></el-icon>
          </div>
          <div class="hours-line">
            <span>开放时间</span>
            <span>{{ detail.openTime || "暂无信息" }}</span>
          </div>
          <div v-if="detail.bestSeason" class="hours-line">
            <span>最佳季节</span>
            <span>{{ detail.bestSeason }}</span>
          </div>
          <div v-if="detail.suggestedHours" class="hours-line">
            <span>建议时长</span>
            <span>{{ detail.suggestedHours }}小时</span>
          </div>
        </div>

        <div v-if="detail.tips" class="side-card">
          <h2>温馨提示</h2>
          <p class="side-text">{{ detail.tips }}</p>
        </div>
      </aside>
    </main>

    <section v-if="similarList.length > 0" id="similar-section" class="similar-section">
      <div class="container">
        <div class="section-heading">
          <h2>相似推荐</h2>
          <button class="circle-next" aria-label="查看更多景点" @click="router.push('/scenic')">
            <span>→</span>
          </button>
        </div>
        <div class="similar-list">
          <article
            v-for="item in similarList"
            :key="item.scenicId"
            class="similar-card"
            @click="goDetail(item.scenicId)"
          >
            <img :src="item.coverImage || ''" :alt="item.scenicName" />
            <div class="similar-body">
              <h3>{{ item.scenicName }}</h3>
              <div class="similar-score">
                <span class="mini-dots">
                  <i
                    v-for="i in 5"
                    :key="i"
                    :class="{ filled: i <= Math.round(item.score || 0) }"
                  ></i>
                </span>
                <span v-if="item.score">({{ Number(item.score).toFixed(1) }})</span>
              </div>
              <p v-if="item.reason">{{ item.reason }}</p>
              <button class="pill-btn card-btn">查看详情</button>
            </div>
          </article>
        </div>
      </div>
    </section>
  </div>

  <div v-else-if="loading" class="page">
    <section class="hero">
      <div class="container">
        <div class="skeleton-line back"></div>
        <div class="skeleton-hero">
          <div>
            <div class="skeleton-tags">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <div class="skeleton-title"></div>
            <div class="skeleton-line medium"></div>
            <div class="skeleton-line short"></div>
          </div>
          <div class="skeleton-stats">
            <span v-for="n in 4" :key="n"></span>
          </div>
        </div>
        <div class="skeleton-gallery">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </section>
  </div>

  <div v-else class="empty-page">暂无景点数据</div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  Clock,
  CollectionTag,
  EditPen,
  Picture,
  Star,
  Sunny,
  Tickets,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { getScenicDetail } from "@/api/scenic";
import { addFavorite, removeFavorite } from "@/api/favorite";
import { getScenicReviews, submitReview as submitReviewApi } from "@/api/audit";
import { fetchSimilarRecommendations as getSimilarRecommend } from "@/api/recommend";
import ReviewWriteForm from "@/views/user/components/ReviewWriteForm.vue";
import ReviewItemCard from "@/views/user/components/ReviewItemCard.vue";
import type {
  ScenicDetailModel,
  ScenicRecommendItem,
  ScenicReviewItem,
} from "@/types/scenic-detail";
import type { MyReviewForm } from "@/types/my-reviews";

const route = useRoute();
const router = useRouter();
const loading = ref(true);
const detail = ref<ScenicDetailModel | null>(null);
const reviews = ref<ScenicReviewItem[]>([]);
const reviewsTotal = ref(0);
const reviewsLoading = ref(false);
const similarList = ref<ScenicRecommendItem[]>([]);
const showReviewForm = ref(false);
const reviewSubmitLoading = ref(false);
let similarTimer: number | undefined;

const reviewQuery = reactive({
  pageNum: 1,
  pageSize: 10,
});

const reviewForm = reactive<MyReviewForm>({
  scenicId: 0,
  score: 5,
  content: "",
  visitDate: "",
  travelType: "",
  isAnonymousBool: false,
  imageIds: [],
});

const scenicId = computed(() => Number(route.params.id));
const formattedScore = computed(() => Number(detail.value?.score || 0).toFixed(1));
const totalReviewPages = computed(() =>
  Math.max(1, Math.ceil(reviewsTotal.value / reviewQuery.pageSize))
);
const reviewCountText = computed(() =>
  detail.value?.reviewCount ? `(${detail.value.reviewCount} 条点评)` : ""
);
const statItems = computed(() => {
  if (!detail.value) return [];
  return [
    detail.value.viewCount !== undefined && {
      label: "浏览",
      value: `${detail.value.viewCount} 次`,
    },
    detail.value.favoriteCount !== undefined && {
      label: "收藏",
      value: `${detail.value.favoriteCount} 人`,
    },
  ].filter(Boolean) as Array<{ label: string; value: string }>;
});
const galleryImages = computed(() => {
  if (!detail.value) return [];
  const images = [
    detail.value.coverImage,
    ...(detail.value.images?.map((item) => item.imageUrl).filter(Boolean) || []),
  ].filter(Boolean) as string[];
  return Array.from(new Set(images)).slice(0, 3);
});
const imageCount = computed(() => {
  if (!detail.value) return 0;
  const extra = detail.value.images?.filter((item) => item.imageUrl).length || 0;
  return (detail.value.coverImage ? 1 : 0) + extra;
});
const infoItems = computed(() => {
  if (!detail.value) return [];
  return [
    detail.value.openTime && { label: "开放时间", value: detail.value.openTime, icon: Clock },
    detail.value.ticketInfo && { label: "门票信息", value: detail.value.ticketInfo, icon: Tickets },
    detail.value.ticketPrice !== undefined && {
      label: "门票价格",
      value: `¥${detail.value.ticketPrice}`,
      icon: Tickets,
    },
    detail.value.bestSeason && { label: "最佳季节", value: detail.value.bestSeason, icon: Sunny },
    detail.value.suggestedHours && {
      label: "建议时长",
      value: `${detail.value.suggestedHours}小时`,
      icon: Clock,
    },
    detail.value.category && {
      label: "景点分类",
      value: detail.value.category,
      icon: CollectionTag,
    },
  ].filter(Boolean) as Array<{ label: string; value: string; icon: unknown }>;
});

async function loadDetail(): Promise<void> {
  loading.value = true;
  similarList.value = [];
  if (similarTimer) {
    window.clearTimeout(similarTimer);
    similarTimer = undefined;
  }
  try {
    detail.value = await getScenicDetail(scenicId.value);
    reviewForm.scenicId = scenicId.value;
    loadReviews();
    scheduleSimilarLoad();
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

async function loadReviews(): Promise<void> {
  reviewsLoading.value = true;
  try {
    const res = await getScenicReviews(scenicId.value, {
      pageNum: reviewQuery.pageNum,
      pageSize: reviewQuery.pageSize,
    });
    reviews.value = res.records;
    reviewsTotal.value = res.total;
  } catch {
    // ignore
  } finally {
    reviewsLoading.value = false;
  }
}

async function loadSimilar(): Promise<void> {
  const targetScenicId = scenicId.value;
  try {
    const res = await getSimilarRecommend(targetScenicId, { pageNum: 1, pageSize: 3 });
    if (targetScenicId === scenicId.value) {
      similarList.value = res.records || [];
    }
  } catch {
    // ignore
  }
}

function scheduleSimilarLoad(): void {
  similarTimer = window.setTimeout(() => {
    void loadSimilar();
  }, 800);
}

function changeReviewPage(page: number): void {
  reviewQuery.pageNum = page;
  loadReviews();
}

async function toggleFavorite(): Promise<void> {
  if (!detail.value) return;
  const targetScenicId = scenicId.value;
  try {
    if (detail.value.isFavorite) {
      await removeFavorite(targetScenicId);
      detail.value.isFavorite = false;
      detail.value.favoriteCount = Math.max((detail.value.favoriteCount || 0) - 1, 0);
      ElMessage.success("已取消收藏");
    } else {
      await addFavorite(targetScenicId);
      detail.value.isFavorite = true;
      detail.value.favoriteCount = (detail.value.favoriteCount || 0) + 1;
      ElMessage.success("收藏成功");
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : "";
    if (message.includes("已收藏")) {
      detail.value.isFavorite = true;
      ElMessage.info("已收藏");
      return;
    }
    if (message.includes("不存在")) {
      detail.value.isFavorite = false;
      ElMessage.info("当前未收藏");
      return;
    }
    ElMessage.error(message || "收藏操作失败");
  }
}

async function submitReview(): Promise<void> {
  if (!reviewForm.content.trim()) {
    ElMessage.warning("请输入点评内容");
    return;
  }
  reviewSubmitLoading.value = true;
  try {
    await submitReviewApi({
      scenicId: scenicId.value,
      score: Math.round(reviewForm.score),
      content: reviewForm.content,
      imageIds: reviewForm.imageIds,
      visitDate: reviewForm.visitDate || undefined,
      travelType: reviewForm.travelType || undefined,
      isAnonymous: reviewForm.isAnonymousBool ? 1 : 0,
    });
    showReviewForm.value = false;
    resetReviewForm();
    reviewQuery.pageNum = 1;
    loadReviews();
    ElMessage.success("点评已提交");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "提交失败");
  } finally {
    reviewSubmitLoading.value = false;
  }
}

function resetReviewForm(): void {
  reviewForm.scenicId = scenicId.value;
  reviewForm.score = 5;
  reviewForm.content = "";
  reviewForm.visitDate = "";
  reviewForm.travelType = "";
  reviewForm.isAnonymousBool = false;
  reviewForm.imageIds = [];
}

function updateReviewItem(index: number, updated: ScenicReviewItem): void {
  reviews.value.splice(index, 1, updated);
}

function goDetail(id: number): void {
  router.push(`/scenic/${id}`);
}

function openReviewForm(): void {
  showReviewForm.value = true;
  scrollToReviews();
}

function scrollToReviews(): void {
  scrollToSection("reviews-section");
}

function scrollToInfo(): void {
  scrollToSection("info-section");
}

function scrollToSection(id: string): void {
  const el = document.querySelector(`#${id}`);
  if (!el) return;
  const headerOffset = 150;
  const top = el.getBoundingClientRect().top + window.scrollY - headerOffset;
  window.scrollTo({ top, behavior: "smooth" });
}

watch(
  scenicId,
  (id) => {
    if (id) loadDetail();
  },
  { immediate: true }
);
</script>

<style scoped>
.page {
  --paper: #ffffff;
  --paper-deep: #f7f7f7;
  --ink: #000000;
  --muted: #777777;
  --line: #eeeeee;
  --line-strong: #d8d8d8;
  --pine: #00c665;
  --pine-deep: #003b1b;
  --moss: #00a65a;
  --clay: #00c665;
  --white: #ffffff;
  min-height: 100vh;
  color: var(--ink);
  background: #ffffff;
}

.container {
  width: min(1280px, calc(100% - 72px));
  margin: 0 auto;
}

.hero {
  padding: 24px 0 0;
  background: #ffffff;
  border-bottom: 0;
}

.breadcrumb-line {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 22px;
  font-size: 13px;
  font-weight: 700;
  color: var(--pine-deep);
}

.breadcrumb-line span:not(:last-child)::after {
  margin-left: 8px;
  color: var(--muted);
  content: "›";
}

.breadcrumb-line span:last-child {
  max-width: 360px;
  overflow: hidden;
  color: var(--muted);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.region-crumb {
  font-size: 14px;
  color: var(--muted);
}

.hero-header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 430px);
  gap: 36px;
  align-items: start;
  margin-bottom: 32px;
}

.title-block {
  min-width: 0;
}

.eyebrow {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.eyebrow span,
.tag {
  padding: 6px 11px 5px;
  font-size: 12px;
  font-weight: 700;
  color: var(--pine-deep);
  background: #f8fbf8;
  border: 1px solid #dfe9df;
  border-radius: 4px;
}

h1 {
  max-width: 920px;
  margin: 0 0 15px;
  font-size: clamp(34px, 4vw, 48px);
  font-weight: 800;
  line-height: 1.15;
  color: var(--pine-deep);
}

.rating-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  font-size: 14px;
  color: var(--muted);
}

.score {
  font-size: 18px;
  font-weight: 800;
  color: var(--ink);
}

.rating-dots,
.mini-dots {
  display: inline-flex;
  gap: 4px;
  align-items: center;
}

.rating-dots i {
  width: 14px;
  height: 14px;
}

.mini-dots i {
  width: 9px;
  height: 9px;
}

.rating-dots i,
.mini-dots i {
  display: block;
  background: transparent;
  border: 1px solid var(--moss);
  border-radius: 50%;
}

.rating-dots i.filled,
.mini-dots i.filled {
  background: var(--moss);
}

.text-link {
  padding: 0;
  font: inherit;
  color: var(--pine);
  text-decoration: underline;
  text-underline-offset: 4px;
  cursor: pointer;
  background: transparent;
  border: 0;
}

.rank-text {
  color: var(--muted);
}

.address {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  max-width: 760px;
  margin: 14px 0 0;
  font-size: 15px;
  line-height: 1.65;
  color: #596153;
}

.hero-side {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  flex-direction: column;
}

.stat-strip {
  display: grid;
  width: auto;
  padding: 0;
  margin: 0;
  overflow: hidden;
  background: #ffffff;
  border: 0;
  border-radius: 0;
  grid-template-columns: repeat(2, minmax(96px, 1fr));
}

.stat-strip div {
  min-width: 0;
  padding: 0 22px;
  border-right: 0;
}

.stat-strip div + div {
  border-left: 1px solid var(--line);
}

.stat-strip dt {
  margin-bottom: 5px;
  font-size: 12px;
  color: var(--muted);
}

.stat-strip dd {
  margin: 0;
  overflow: hidden;
  font-size: 17px;
  font-weight: 800;
  color: var(--pine-deep);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hero-actions {
  display: flex;
  flex-shrink: 0;
  gap: 12px;
  padding-top: 0;
}

.outline-btn {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  height: 42px;
  padding: 0 20px;
  font-size: 14px;
  font-weight: 700;
  color: var(--pine-deep);
  cursor: pointer;
  background: #ffffff;
  border: 1px solid var(--pine-deep);
  border-radius: 999px;
  transition:
    background 0.2s,
    border-color 0.2s,
    color 0.2s;
}

.outline-btn:hover,
.outline-btn.active {
  color: var(--white);
  background: var(--pine);
  border-color: var(--pine);
}

.photo-grid {
  display: grid;
  grid-template-columns: 1.95fr 1fr;
  grid-template-rows: repeat(2, 150px);
  gap: 4px;
  padding: 0;
  overflow: hidden;
  background: #ffffff;
  border: 0;
  border-radius: 12px;
}

.photo-grid.empty {
  display: block;
  min-height: 300px;
}

.photo-cell {
  position: relative;
  min-height: 150px;
  overflow: hidden;
  background: var(--paper-deep);
}

.photo-cell.primary {
  grid-row: span 2;
}

.photo-cell img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: none;
  transition:
    transform 0.45s,
    filter 0.45s;
}

.photo-cell:hover img {
  transform: scale(1.025);
}

.photo-count {
  position: absolute;
  right: 18px;
  bottom: 18px;
  display: inline-flex;
  gap: 8px;
  align-items: center;
  padding: 8px 13px;
  font-size: 14px;
  font-weight: 900;
  color: var(--white);
  background: rgba(23, 35, 27, 0.88);
  border-radius: 999px;
}

.photo-placeholder {
  display: grid;
  gap: 12px;
  place-items: center;
  min-height: 300px;
  font-size: 15px;
  color: var(--muted);
  background: var(--paper-deep);
}

.photo-placeholder .el-icon {
  font-size: 42px;
}

.section-nav {
  position: sticky;
  top: 64px;
  z-index: 20;
  display: flex;
  gap: 34px;
  padding: 24px 0 0;
  margin-top: 24px;
  overflow-x: auto;
  background: #ffffff;
  border-bottom: 1px solid var(--line);
}

.section-nav button {
  position: relative;
  flex: 0 0 auto;
  padding: 0 0 15px;
  font-size: 17px;
  font-weight: 800;
  color: var(--pine-deep);
  cursor: pointer;
  background: transparent;
  border: 0;
}

.section-nav button:first-child::after,
.section-nav button:hover::after {
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  height: 3px;
  content: "";
  background: var(--pine-deep);
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 56px;
  align-items: start;
  padding: 40px 0 58px;
}

.main-column {
  min-width: 0;
}

.section {
  position: relative;
  scroll-margin-top: 150px;
  padding: 0 0 42px;
  margin-bottom: 38px;
  border-bottom: 1px solid var(--line);
}

.section::before {
  display: none;
}

.section:last-child {
  border-bottom: 0;
}

.section h2,
.side-card h2 {
  margin: 0 0 22px;
  font-size: 22px;
  font-weight: 800;
  color: var(--pine-deep);
}

.description,
.detail-content,
.side-text {
  max-width: 820px;
  margin: 0;
  font-size: 17px;
  line-height: 1.95;
  color: #3f443a;
}

.detail-content {
  margin-top: 15px;
}

.detail-content :deep(p) {
  margin: 0 0 12px;
}

.muted {
  color: var(--muted);
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  border: 0;
}

.info-item {
  display: flex;
  gap: 12px;
  align-items: center;
  min-height: auto;
  padding: 0;
  background: #ffffff;
  border: 0;
}

.info-item .el-icon {
  flex-shrink: 0;
  margin-top: 2px;
  font-size: 19px;
  color: var(--clay);
}

.info-item span {
  display: block;
  margin-bottom: 2px;
  font-size: 13px;
  color: var(--muted);
}

.info-item strong {
  font-size: 16px;
  line-height: 1.45;
  color: var(--ink);
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 9px;
}

.sidebar {
  position: sticky;
  top: 118px;
}

.side-card {
  padding: 28px;
  margin-bottom: 18px;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 14px;
  box-shadow: 0 14px 36px rgba(0, 0, 0, 0.06);
}

.summary-card {
  background: #ffffff;
}

.summary-card p {
  margin: 12px 0 22px;
  font-size: 14px;
  line-height: 1.75;
  color: var(--muted);
}

.summary-price {
  display: grid;
  gap: 6px;
  margin-bottom: 12px;
}

.summary-price span {
  font-size: 13px;
  font-weight: 700;
  color: var(--muted);
}

.summary-price strong {
  font-size: 28px;
  line-height: 1.2;
  color: var(--pine-deep);
}

.side-card-header,
.section-heading {
  display: flex;
  gap: 18px;
  align-items: center;
  justify-content: space-between;
}

.side-card-header h2,
.section-heading h2 {
  margin: 0;
}

.status {
  display: block;
  margin-bottom: 14px;
  font-size: 15px;
  font-weight: 900;
  color: var(--moss);
}

.hours-line {
  display: flex;
  gap: 16px;
  justify-content: space-between;
  padding: 12px 0;
  font-size: 14px;
  line-height: 1.7;
  color: #3f443a;
  border-bottom: 1px solid var(--line);
}

.hours-line:last-child {
  border-bottom: 0;
}

.pill-btn,
.ghost-btn,
.page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  padding: 0 22px;
  font-size: 14px;
  font-weight: 900;
  color: var(--white);
  cursor: pointer;
  background: var(--pine);
  border: 1px solid var(--pine);
  border-radius: 8px;
  transition:
    background 0.2s,
    border-color 0.2s,
    transform 0.2s;
}

.pill-btn:hover,
.page-btn:hover:not(:disabled) {
  background: var(--pine-deep);
  border-color: var(--pine-deep);
  transform: translateY(-1px);
}

.pill-btn.full {
  width: 100%;
}

.pill-btn.small {
  height: 36px;
  padding: 0 16px;
  font-size: 13px;
}

.ghost-btn,
.page-btn {
  color: var(--pine-deep);
  background: transparent;
  border-color: var(--line-strong);
}

.ghost-btn:hover,
.page-btn:hover:not(:disabled) {
  color: var(--white);
}

.review-form {
  padding: 22px;
  margin-top: 22px;
  margin-bottom: 28px;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 10px;
}

.review-form :deep(.write-review-section) {
  padding: 0;
}

.review-cancel {
  margin-top: 12px;
}

.rating-select {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 900;
  color: var(--ink);
}

.select,
.textarea {
  font: inherit;
  color: var(--ink);
  outline: none;
  background: #ffffff;
  border: 1px solid var(--line);
  border-radius: 8px;
}

.select {
  height: 38px;
  padding: 0 12px;
}

.textarea {
  width: 100%;
  padding: 13px 14px;
  resize: vertical;
}

.select:focus,
.textarea:focus {
  border-color: var(--pine);
  box-shadow: 0 0 0 3px rgba(36, 70, 52, 0.11);
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.state-block {
  padding: 34px;
  font-size: 14px;
  color: var(--muted);
  text-align: center;
  background: #ffffff;
  border: 1px dashed var(--line);
}

.review-list {
  display: grid;
  gap: 0;
  margin-top: 22px;
  border-top: 1px solid var(--line);
}

.review-item {
  padding: 22px 0;
  border-bottom: 1px solid var(--line);
}

.review-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}

.review-avatar {
  display: grid;
  flex-shrink: 0;
  place-items: center;
  width: 40px;
  height: 40px;
  font-weight: 800;
  color: var(--white);
  background: var(--pine);
  border-radius: 50%;
}

.review-header h3 {
  margin: 0 0 3px;
  font-size: 15px;
  color: var(--ink);
}

.review-header span,
.review-header time {
  font-size: 13px;
  color: var(--muted);
}

.review-header time {
  margin-left: auto;
}

.review-item p {
  max-width: 760px;
  margin: 0;
  font-size: 15px;
  line-height: 1.8;
  color: #3f443a;
}

.pagination {
  display: flex;
  gap: 14px;
  align-items: center;
  justify-content: center;
  margin-top: 28px;
  color: var(--muted);
}

.page-btn {
  height: 36px;
  padding: 0 15px;
  font-size: 13px;
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.45;
  transform: none;
}

.similar-section {
  scroll-margin-top: 150px;
  padding: 44px 0 64px;
  background: #ffffff;
  border-top: 1px solid var(--line);
}

.similar-section .section-heading {
  margin-bottom: 24px;
}

.similar-section h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: var(--pine-deep);
}

.circle-next {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  font-size: 24px;
  color: var(--pine-deep);
  cursor: pointer;
  background: transparent;
  border: 1px solid var(--line-strong);
  border-radius: 50%;
}

.circle-next:hover {
  color: var(--white);
  background: var(--pine);
  border-color: var(--pine);
}

.similar-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.similar-card {
  display: flex;
  min-height: 100%;
  overflow: hidden;
  cursor: pointer;
  background: #fffefa;
  border: 1px solid var(--line);
  border-radius: 10px;
  flex-direction: column;
  transition:
    transform 0.2s,
    border-color 0.2s,
    box-shadow 0.2s;
}

.similar-card:hover {
  border-color: var(--line-strong);
  box-shadow: 0 18px 32px rgba(38, 35, 27, 0.08);
  transform: translateY(-3px);
}

.similar-card img {
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
  background: var(--paper-deep);
  filter: none;
}

.similar-body {
  display: flex;
  flex: 1;
  padding: 17px;
  flex-direction: column;
}

.similar-body h3 {
  margin: 0 0 10px;
  font-size: 19px;
  line-height: 1.35;
  color: var(--ink);
}

.similar-score {
  display: flex;
  gap: 7px;
  align-items: center;
  min-height: 18px;
  margin-bottom: 13px;
  font-size: 13px;
  color: var(--muted);
}

.similar-body p {
  display: -webkit-box;
  margin: 0 0 18px;
  overflow: hidden;
  font-size: 14px;
  line-height: 1.6;
  color: var(--muted);
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.card-btn {
  width: 100%;
  height: 40px;
  margin-top: auto;
}

.empty-page {
  padding: 90px 20px;
  font-size: 15px;
  color: var(--muted);
  text-align: center;
  background: #ffffff;
}

.skeleton-line,
.skeleton-title,
.skeleton-tags span,
.skeleton-stats span,
.skeleton-gallery span {
  overflow: hidden;
  background: linear-gradient(90deg, #f3f3f3 0%, #e9e9e9 48%, #f3f3f3 100%);
  background-size: 220% 100%;
  border-radius: 8px;
  animation: skeleton-pulse 1.35s ease-in-out infinite;
}

.skeleton-line.back {
  width: 160px;
  height: 18px;
  margin-bottom: 34px;
}

.skeleton-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 36px;
  align-items: start;
  margin-bottom: 26px;
}

.skeleton-tags {
  display: flex;
  gap: 10px;
  margin-bottom: 22px;
}

.skeleton-tags span {
  width: 86px;
  height: 36px;
}

.skeleton-title {
  width: min(520px, 100%);
  height: 68px;
  margin-bottom: 22px;
}

.skeleton-line.medium {
  width: min(460px, 82%);
  height: 18px;
  margin-bottom: 18px;
}

.skeleton-line.short {
  width: min(320px, 58%);
  height: 18px;
}

.skeleton-stats {
  display: grid;
  gap: 1px;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: 10px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.skeleton-stats span {
  height: 72px;
  border-radius: 0;
}

.skeleton-gallery {
  display: grid;
  grid-template-columns: 1.72fr 1fr;
  grid-template-rows: repeat(2, 180px);
  gap: 10px;
  padding: 10px;
  border: 1px solid var(--line);
  border-radius: 10px;
}

.skeleton-gallery span:first-child {
  grid-row: span 2;
}

@keyframes skeleton-pulse {
  0% {
    background-position: 120% 0;
  }

  100% {
    background-position: -120% 0;
  }
}

@media (max-width: 1080px) {
  .hero-header,
  .detail-layout {
    grid-template-columns: 1fr;
    gap: 28px;
  }

  .hero-actions {
    padding-top: 0;
  }

  .hero-side {
    align-items: stretch;
  }

  .skeleton-hero {
    grid-template-columns: 1fr;
  }

  .sidebar {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
  }

  .side-card {
    margin-bottom: 0;
  }

  .similar-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .container {
    width: min(100% - 28px, 1180px);
  }

  .hero-actions,
  .form-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .stat-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    row-gap: 16px;
  }

  .stat-strip div:nth-child(2) {
    border-right: 0;
  }

  .photo-grid {
    grid-template-columns: 1fr;
    grid-template-rows: none;
    padding: 8px;
  }

  .skeleton-gallery {
    grid-template-columns: 1fr;
    grid-template-rows: repeat(3, 220px);
    padding: 8px;
  }

  .skeleton-gallery span:first-child {
    grid-row: auto;
  }

  .photo-cell,
  .photo-cell.primary {
    grid-row: auto;
    height: 230px;
  }

  .section {
    padding-left: 0;
  }

  .section::before {
    display: none;
  }

  .info-grid,
  .sidebar,
  .similar-list {
    grid-template-columns: 1fr;
  }

  .review-header {
    align-items: flex-start;
  }

  .review-header time {
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  h1 {
    font-size: 36px;
  }

  .section h2,
  .side-card h2,
  .similar-section h2 {
    font-size: 22px;
  }

  .section-heading {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
