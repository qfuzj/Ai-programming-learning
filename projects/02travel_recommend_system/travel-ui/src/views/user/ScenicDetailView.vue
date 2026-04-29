<!-- 极简风格景点详情页 -->
<template>
  <div v-if="detail" class="page">
    <!-- 头部信息 -->
    <div class="header">
      <div class="header-content">
        <div class="cover">
          <img :src="detail.coverImage || ''" :alt="detail.name" />
        </div>

        <div class="info">
          <div class="tags">
            <span v-if="detail.category" class="tag">{{ detail.category }}</span>
            <span v-if="detail.level" class="tag">{{ detail.level }}</span>
            <span v-if="detail.isRecommended" class="tag" style="background: #00e676">推荐</span>
          </div>

          <h1 class="title">{{ detail.name }}</h1>

          <p class="location">
            {{ detail.regionName || "未知" }}
            <span v-if="detail.address">· {{ detail.address }}</span>
          </p>

          <div class="meta">
            <span v-if="detail.score" class="score">{{ detail.score.toFixed(1) }}分</span>
            <span v-if="detail.reviewCount" class="count">({{ detail.reviewCount }}条点评)</span>
            <span v-if="detail.viewCount" class="count">· {{ detail.viewCount }}次浏览</span>
          </div>

          <div class="action-bar">
            <button class="btn-fav" :class="{ active: detail.isFavorite }" @click="toggleFavorite">
              {{ detail.isFavorite ? "已收藏" : "收藏" }}
            </button>
            <button class="btn-review" @click="showReviewForm = true">写点评</button>
          </div>
        </div>
      </div>
    </div>

    <div class="content">
      <!-- 左侧：详细介绍、图片、点评 -->
      <div class="main">
        <!-- 图片画廊 -->
        <div v-if="detail.images && detail.images.length > 0" class="section">
          <h2 class="section-title">图片</h2>
          <div class="gallery">
            <img
              v-for="img in detail.images"
              :key="img.id"
              :src="img.imageUrl"
              :alt="detail.name"
              class="gallery-img"
            />
          </div>
        </div>

        <!-- 详细介绍 -->
        <div class="section">
          <h2 class="section-title">景点介绍</h2>
          <p v-if="detail.description" class="description">{{ detail.description }}</p>
          <div
            v-if="detail.detailContent"
            class="detail-content"
            v-html="detail.detailContent"
          ></div>
        </div>

        <!-- 实用信息 -->
        <div class="section">
          <h2 class="section-title">实用信息</h2>
          <div class="info-grid">
            <div v-if="detail.openTime" class="info-item">
              <span class="info-label">开放时间</span>
              <span class="info-value">{{ detail.openTime }}</span>
            </div>
            <div v-if="detail.ticketInfo" class="info-item">
              <span class="info-label">门票信息</span>
              <span class="info-value">{{ detail.ticketInfo }}</span>
            </div>
            <div v-if="detail.bestSeason" class="info-item">
              <span class="info-label">最佳季节</span>
              <span class="info-value">{{ detail.bestSeason }}</span>
            </div>
            <div v-if="detail.suggestedHours" class="info-item">
              <span class="info-label">建议时长</span>
              <span class="info-value">{{ detail.suggestedHours }}小时</span>
            </div>
            <div v-if="detail.ticketPrice" class="info-item">
              <span class="info-label">门票价格</span>
              <span class="info-value price">¥{{ detail.ticketPrice }}</span>
            </div>
          </div>
        </div>

        <!-- 标签 -->
        <div v-if="detail.tagList && detail.tagList.length > 0" class="section">
          <h2 class="section-title">标签</h2>
          <div class="tag-list">
            <span v-for="tag in detail.tagList" :key="tag" class="tag">{{ tag }}</span>
          </div>
        </div>

        <!-- 点评列表 -->
        <div id="reviews-section" class="section">
          <h2 class="section-title">点评 ({{ reviewsTotal }})</h2>

          <button v-if="!showReviewForm" class="btn-write" @click="showReviewForm = true">
            写点评
          </button>

          <div v-if="showReviewForm" class="review-form">
            <div class="rating-select">
              <span class="label">评分：</span>
              <select v-model="reviewForm.score" class="select">
                <option :value="5">5分 - 很棒</option>
                <option :value="4">4分 - 不错</option>
                <option :value="3">3分 - 还行</option>
                <option :value="2">2分 - 较差</option>
                <option :value="1">1分 - 很差</option>
              </select>
            </div>
            <textarea
              v-model="reviewForm.content"
              class="textarea"
              placeholder="分享您的旅行体验..."
              rows="4"
            ></textarea>
            <div class="form-actions">
              <button class="btn-submit" @click="submitReview">提交点评</button>
              <button class="btn-cancel" @click="showReviewForm = false">取消</button>
            </div>
          </div>

          <div v-if="reviewsLoading" class="loading">加载中...</div>
          <div v-else-if="reviews.length === 0" class="empty">暂无点评</div>

          <div v-else class="review-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <div class="review-avatar">{{ (review.username || "U").charAt(0) }}</div>
                <div>
                  <div class="review-user">{{ review.username || "匿名用户" }}</div>
                  <div class="review-score">{{ review.score }}分</div>
                </div>
                <span class="review-time">{{ review.createdAt }}</span>
              </div>
              <p class="review-content">{{ review.content }}</p>
            </div>
          </div>

          <div v-if="reviewsTotal > 0" class="pagination">
            <button
              class="page-btn"
              :disabled="reviewQuery.pageNum <= 1"
              @click="changeReviewPage(reviewQuery.pageNum - 1)"
            >
              上一页
            </button>
            <span class="page-info">
              {{ reviewQuery.pageNum }} / {{ Math.ceil(reviewsTotal / reviewQuery.pageSize) }}
            </span>
            <button
              class="page-btn"
              :disabled="reviewQuery.pageNum >= Math.ceil(reviewsTotal / reviewQuery.pageSize)"
              @click="changeReviewPage(reviewQuery.pageNum + 1)"
            >
              下一页
            </button>
          </div>
        </div>

        <!-- 相似推荐 -->
        <div v-if="similarList.length > 0" class="section">
          <h2 class="section-title">相似推荐</h2>
          <div class="similar-list">
            <div
              v-for="item in similarList"
              :key="item.scenicId"
              class="similar-card"
              @click="goDetail(item.scenicId)"
            >
              <img :src="item.coverImage || ''" :alt="item.scenicName" />
              <div class="similar-body">
                <h3>{{ item.scenicName }}</h3>
                <p v-if="item.reason" class="reason">{{ item.reason }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：价格、时间等 -->
      <div class="sidebar">
        <div class="side-card">
          <div v-if="detail.ticketPrice !== undefined" class="price">
            <span class="label">门票</span>
            <span class="value">¥{{ detail.ticketPrice }}</span>
          </div>
          <div v-if="detail.openTime" class="open-time">
            <span class="label">开放时间</span>
            <span class="value">{{ detail.openTime }}</span>
          </div>
          <div v-if="detail.tips" class="tips">
            <span class="label">温馨提示</span>
            <span class="value">{{ detail.tips }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-else-if="!loading" class="empty-page">暂无景点数据</div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getScenicDetail, type ScenicDetail } from "@/api/scenic";
import { addFavorite, removeFavorite } from "@/api/favorite";
import { getScenicReviews, type ReviewItem, submitReview as submitReviewApi } from "@/api/audit";
import { fetchSimilarRecommendations as getSimilarRecommend } from "@/api/recommend";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const detail = ref<ScenicDetail | null>(null);
const reviews = ref<ReviewItem[]>([]);
const reviewsTotal = ref(0);
const reviewsLoading = ref(false);
const similarList = ref<any[]>([]);
const showReviewForm = ref(false);

const reviewQuery = reactive({
  pageNum: 1,
  pageSize: 10,
});

const reviewForm = reactive({
  score: 5,
  content: "",
});

const scenicId = computed(() => Number(route.params.id));

async function loadDetail(): Promise<void> {
  loading.value = true;
  try {
    detail.value = await getScenicDetail(scenicId.value);
    loadReviews();
    loadSimilar();
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
  try {
    const res = await getSimilarRecommend(scenicId.value, { pageNum: 1, pageSize: 6 });
    similarList.value = res.records || [];
  } catch {
    // ignore
  }
}

function changeReviewPage(page: number): void {
  reviewQuery.pageNum = page;
  loadReviews();
}

async function toggleFavorite(): Promise<void> {
  if (!detail.value) return;
  try {
    if (detail.value.isFavorite) {
      await removeFavorite(detail.value.id);
      detail.value.isFavorite = false;
    } else {
      await addFavorite(detail.value.id);
      detail.value.isFavorite = true;
    }
  } catch {
    alert("操作失败");
  }
}

async function submitReview(): Promise<void> {
  if (!reviewForm.content.trim()) {
    alert("请输入点评内容");
    return;
  }
  try {
    await submitReviewApi({
      scenicId: scenicId.value,
      score: reviewForm.score,
      content: reviewForm.content,
    });
    showReviewForm.value = false;
    reviewForm.content = "";
    reviewQuery.pageNum = 1;
    loadReviews();
  } catch {
    alert("提交失败");
  }
}

function goDetail(id: number): void {
  router.push(`/scenic/${id}`);
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
  background: #ffffff;
}

/* Header */
.header {
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  padding: 40px 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  gap: 40px;
}

.cover {
  width: 480px;
  height: 320px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info {
  flex: 1;
}

.tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.tag {
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
  color: #000000;
  background: #f0f0f0;
  border-radius: 999px;
}

.title {
  font-size: 32px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 12px 0;
}

.location {
  font-size: 15px;
  color: #999999;
  margin: 0 0 16px 0;
}

.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.score {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
}

.count {
  font-size: 14px;
  color: #999999;
}

.action-bar {
  display: flex;
  gap: 12px;
}

.btn-fav,
.btn-review {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-fav {
  color: #000000;
  background: #00e676;
  border: none;
}

.btn-fav.active {
  background: #f0f0f0;
}

.btn-fav:hover {
  background: #00c665;
}

.btn-review {
  color: #000000;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
}

.btn-review:hover {
  background: #f0f0f0;
}

/* Content */
.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
  display: flex;
  gap: 40px;
}

.main {
  flex: 1;
  min-width: 0;
}

.sidebar {
  width: 320px;
  flex-shrink: 0;
}

.side-card {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 24px;
}

.side-card .label {
  display: block;
  font-size: 13px;
  color: #999999;
  margin-bottom: 4px;
}

.side-card .value {
  display: block;
  font-size: 15px;
  color: #000000;
  margin-bottom: 20px;
}

.side-card .price .value {
  font-size: 24px;
  font-weight: 700;
  color: #000000;
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

/* Gallery */
.gallery {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.gallery-img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.gallery-img:hover {
  transform: scale(1.02);
}

/* Description */
.description {
  font-size: 15px;
  color: #333333;
  line-height: 1.8;
  margin: 0 0 16px 0;
}

.detail-content {
  font-size: 15px;
  color: #333333;
  line-height: 1.8;
}

/* Info grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 13px;
  color: #999999;
}

.info-value {
  font-size: 15px;
  color: #000000;
}

.info-value.price {
  font-size: 20px;
  font-weight: 700;
}

/* Tag list */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* Review */
.btn-write {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 20px;
}

.btn-write:hover {
  background: #00c665;
}

.review-form {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.rating-select {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.rating-select .label {
  font-size: 14px;
  color: #333333;
}

.select {
  padding: 8px 12px;
  font-size: 14px;
  color: #000000;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
}

.textarea {
  width: 100%;
  padding: 12px;
  font-size: 14px;
  color: #000000;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
  resize: vertical;
  font-family: inherit;
}

.textarea:focus {
  border-color: #00e676;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.btn-submit {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.btn-cancel {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #666666;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.review-avatar {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  background: #00e676;
  color: #000000;
  font-weight: 600;
  font-size: 14px;
  border-radius: 50%;
  flex-shrink: 0;
}

.review-user {
  font-size: 14px;
  font-weight: 500;
  color: #000000;
}

.review-score {
  font-size: 13px;
  color: #999999;
}

.review-time {
  margin-left: auto;
  font-size: 13px;
  color: #999999;
}

.review-content {
  font-size: 14px;
  color: #333333;
  line-height: 1.6;
  margin: 0;
}

/* Similar */
.similar-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.similar-card {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.similar-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.similar-card img {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.similar-body {
  padding: 12px;
}

.similar-body h3 {
  font-size: 15px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 6px 0;
}

.reason {
  font-size: 13px;
  color: #999999;
  margin: 0;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

.page-btn {
  padding: 8px 16px;
  font-size: 14px;
  color: #000000;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
}

.page-btn:hover:not(:disabled) {
  border-color: #00e676;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #999999;
}

/* Loading & Empty */
.loading,
.empty {
  padding: 40px;
  text-align: center;
  color: #999999;
  font-size: 14px;
}

.empty-page {
  padding: 80px 20px;
  text-align: center;
  color: #999999;
  font-size: 15px;
}

@media (max-width: 992px) {
  .header-content {
    flex-direction: column;
  }
  .cover {
    width: 100%;
    height: 240px;
  }
  .content {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
  }
  .gallery {
    grid-template-columns: repeat(2, 1fr);
  }
  .similar-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .gallery,
  .similar-list {
    grid-template-columns: 1fr;
  }
}
</style>
