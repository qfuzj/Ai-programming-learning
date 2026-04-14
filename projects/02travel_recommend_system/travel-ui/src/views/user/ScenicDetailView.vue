<template>
  <div class="page-container" v-if="detail">
    <div class="breadcrumb-container">
      <el-breadcrumb separator=">">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/scenic' }">景点玩乐</el-breadcrumb-item>
        <el-breadcrumb-item>{{ detail.regionName || '地区' }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ detail.name }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- Header Section -->
    <div class="header-section">
      <div class="header-left">
        <h1 class="scenic-title">{{ detail.name }}</h1>
        <div class="scenic-rating">
          <div class="rating-dots">
            <span v-for="i in 5" :key="i" class="dot" :class="{ filled: i <= Math.round(detail.score || 0) }"></span>
          </div>
          <span class="rating-score">{{ Number(detail.score || 0).toFixed(1) }} 分</span>
          <span class="review-count" v-if="detail.reviewCount" @click="scrollToReviews">({{ detail.reviewCount }} 条点评)</span>
          <span class="rank-text" v-if="detail.regionName">在 {{ detail.regionName }} 的相关分类中排名前列</span>
          <span class="metric-item" title="浏览次数"><el-icon><View /></el-icon> {{ detail.viewCount || 0 }}</span>
          <span class="metric-item" title="收藏次数"><el-icon><Star /></el-icon> {{ detail.favoriteCount || 0 }}</span>
        </div>
      </div>
      <div class="header-right">
        <el-button round class="action-btn" @click="toggleFavorite">
          <el-icon :class="{ 'red-star': detail.isFavorite }"><Star /></el-icon>
          {{ detail.isFavorite ? '已保存' : '保存' }}
        </el-button>
        <el-button round class="action-btn" @click="goWriteReview">
          <el-icon><EditPen /></el-icon>
          点评
        </el-button>
      </div>
    </div>

    <!-- Photo Gallery -->
    <div class="photo-gallery">
      <div class="main-photo" :style="{ backgroundImage: `url(${galleryImages[0]})` }"></div>
      <div class="side-photos">
        <div class="side-photo" :style="{ backgroundImage: `url(${galleryImages[1]})` }"></div>
        <div class="side-photo" :style="{ backgroundImage: `url(${galleryImages[2]})` }">
          <div class="photo-overlay">
            <el-icon><Picture /></el-icon>
            <span>80+</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="content-grid">
      <!-- Left Column -->
      <div class="left-column">
        <section class="info-section">
          <h2>相关信息</h2>
          <p class="scenic-intro" :class="{ 'expanded': introExpanded }">
            {{ detail.intro || '暂无详细简介。请查看体验和门票信息了解详情。' }}
            <template v-if="detail.detailContent">
              <br/><br/>
              {{ detail.detailContent }}
            </template>
          </p>
          <div class="read-more-link" @click="introExpanded = !introExpanded" v-if="detail.intro && detail.intro.length > 100">
            {{ introExpanded ? '收起' : '阅读更多' }} <el-icon><ArrowDown v-if="!introExpanded"/><ArrowUp v-else/></el-icon>
          </div>
          
          <div style="margin-top: 16px; font-size: 15px; color: #333;">
            <p><strong>地理位置：</strong>{{ detail.address || '暂无位置信息' }}</p>
            <p v-if="detail.level"><strong>景区等级：</strong>{{ detail.level }} 景区</p>
          </div>
        </section>

        <!-- Similar Experiences Section -->
        <section class="recommend-experiences" v-if="relatedExperiences && relatedExperiences.length > 0">
          <h2>{{ detail.name }} 的相似推荐</h2>
          <div class="experience-grid">
            <div class="exp-card" v-for="(exp, idx) in relatedExperiences" :key="idx" @click="goToSimilar(exp.scenicId)">
              <div class="exp-image" :style="{ backgroundImage: `url(${exp.coverImage || 'https://via.placeholder.com/400x300?text=No+Image'})` }"></div>
              <div class="exp-content">
                <h4>{{ exp.scenicName }}</h4>
                <div class="exp-meta">
                  <span v-if="exp.score">评分：{{ Number(exp.score).toFixed(1) }} 分</span>
                  <span v-else>暂无评分</span>
                  
                  <span v-if="exp.reason" class="exp-reason" :title="exp.reason">{{ exp.reason }}</span>
                </div>
                <el-button type="success" round class="book-btn">查看详情</el-button>
              </div>
            </div>
          </div>
        </section>

        <!-- User Reviews Section -->
        <section class="reviews-section" id="reviews-section">
          <div class="reviews-header">
            <h2>访客点评</h2>
            <el-button round class="write-review-btn" @click="goWriteReview">写点评</el-button>
          </div>
          
          <div class="reviews-list" v-loading="reviewsLoading">
            <template v-if="reviews.length > 0">
              <div class="review-item" v-for="review in reviews" :key="review.id">
                <div class="reviewer-info">
                  <el-avatar :size="40">{{ (review.username || '匿名用户').charAt(0).toUpperCase() }}</el-avatar>
                  <div class="reviewer-meta">
                    <span class="reviewer-name">{{ review.username || '匿名用户' }}</span>
                    <span class="review-date" v-if="review.createTime">{{ new Date(review.createTime).toLocaleDateString() }}发布</span>
                  </div>
                </div>
                <div class="review-rating">
                  <div class="rating-dots">
                    <span v-for="i in 5" :key="i" class="dot" :class="{ filled: i <= Math.round(review.score || review.rating || 0) }"></span>
                  </div>
                  <span class="visit-date" v-if="review.visitDate">游玩时间：{{ review.visitDate }}</span>
                </div>
                <div class="review-content">{{ review.content }}</div>
                <div class="review-images" v-if="review.images && review.images.length > 0">
                  <el-image
                    v-for="(img, idx) in review.images"
                    :key="idx"
                    :src="img"
                    :preview-src-list="review.images"
                    class="review-img"
                    fit="cover"
                  />
                </div>
              </div>
              
              <!-- Review Pagination -->
              <div class="pagination-container" v-if="reviewsTotal > 0">
                <el-pagination
                  v-model:current-page="reviewQuery.pageNum"
                  v-model:page-size="reviewQuery.pageSize"
                  :total="reviewsTotal"
                  layout="prev, pager, next"
                  @current-change="loadReviews"
                  background
                />
              </div>
            </template>
            <el-empty v-else description="暂无点评，快来发布第一条！" />
          </div>
        </section>
      </div>

      <!-- Right Column -->
      <div class="right-column">
        <div class="sticky-block">
          <el-card class="ticket-card" shadow="never">
            <h3>游览和体验</h3>
            <p>探索体验此地点的不同方式。</p>
            <el-button type="primary" class="full-btn">
              查看选项 / 门票 ¥{{ detail.ticketPrice ?? '咨询' }}
            </el-button>
          </el-card>

          <el-card class="hours-card" shadow="never">
            <div class="hours-header">
              <h3>营业时间</h3>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <div class="hours-status">
              <span class="open-status">正在营业</span>
            </div>
            <div class="hours-detail">
              <span>今日</span>
              <span>{{ detail.openTime || '全天开放' }}</span>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
  <el-empty v-else-if="!loading" description="暂无景点数据"></el-empty>
</template>

<script setup lang="ts">
import { computed, ref, watch, reactive } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getScenicDetail, type ScenicDetail } from "@/api/scenic";
import { reportBrowseHistory } from "@/api/history";
import { addFavorite, removeFavorite } from "@/api/favorite";
import { getScenicReviews, type ReviewItem, type ReviewQuery } from "@/api/audit";
import { fetchSimilarRecommendations, type AiRecommendItem } from "@/api/ai";
import { Star, Picture, ArrowDown, ArrowUp, EditPen, View } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

const route = useRoute();
const router = useRouter();
const scenicId = computed(() => Number(route.params.id));
const loading = ref(false);
const detail = ref<ScenicDetail | null>(null);
const introExpanded = ref(false);

const reviewsLoading = ref(false);
const reviews = ref<ReviewItem[]>([]);
const reviewsTotal = ref(0);
const reviewQuery = reactive<ReviewQuery>({ pageNum: 1, pageSize: 10 });

async function loadReviews() {
  if (!detail.value) return;
  reviewsLoading.value = true;
  try {
    const res = await getScenicReviews(detail.value.id, reviewQuery);
    reviews.value = res.records;
    reviewsTotal.value = res.total;
  } catch (error) {
    console.error("Failed to load reviews", error);
  } finally {
    reviewsLoading.value = false;
  }
}

function scrollToReviews() {
  const el = document.getElementById("reviews-section");
  if (el) {
    el.scrollIntoView({ behavior: "smooth" });
  }
}

const galleryImages = computed(() => {
  if (!detail.value) return [];
  const list: string[] = [];
  const fallback = detail.value.coverImage || 'https://via.placeholder.com/800x600?text=No+Image';
  
  // 主封面图最好固定作为左侧大图，保证视觉统一
  list.push(fallback);
  
  if (detail.value.images && detail.value.images.length > 0) {
    // 过滤掉早期测试用的假图片地址（如包含 example.com 的失效图片）
    const validImages = detail.value.images
      .map((img: any) => img.imageUrl)
      .filter((url: string) => url && !url.includes('example.com'));
      
    list.push(...validImages);
  }
  
  // 补齐至少 3 张图片以适应 UI 的网格布局结构
  while (list.length < 3) {
    list.push(fallback);
  }
  return list;
});

const relatedExperiences = ref<AiRecommendItem[]>([]);

function goToSimilar(id: number) {
  router.push(`/scenic/${id}`);
}

async function loadSimilar() {
  if (!detail.value) return;
  try {
    const res = await fetchSimilarRecommendations(detail.value.id, { pageNum: 1, pageSize: 3 });
    relatedExperiences.value = res.records;
  } catch (error) {
    console.error("Failed to load similar recommendations", error);
  }
}

async function loadDetail(id: number): Promise<void> {
  if (!id) {
    detail.value = null;
    return;
  }
  loading.value = true;
  try {
    const scenicDetail = await getScenicDetail(id);
    detail.value = scenicDetail;
    void reportBrowseHistory({ scenicId: scenicDetail.id, source: "scenic_detail" });
    // Reset review list when switching to a new scenic spot
    reviewQuery.pageNum = 1;
    void loadReviews();
    void loadSimilar();
  } finally {
    loading.value = false;
  }
}

async function toggleFavorite() {
  if (!detail.value) return;
  try {
    if (detail.value.isFavorite) {
      await removeFavorite(detail.value.id);
      detail.value.isFavorite = false;
      detail.value.favoriteCount = Math.max(0, (detail.value.favoriteCount || 0) - 1);
      ElMessage.success("已取消收藏！");
    } else {
      await addFavorite(detail.value.id);
      detail.value.isFavorite = true;
      detail.value.favoriteCount = (detail.value.favoriteCount || 0) + 1;
      ElMessage.success("已保存至我的收藏！");
    }
  } catch (error) {
    console.error("操作失败", error);
  }
}

function goWriteReview() {
  if (detail.value) {
    router.push(`/my-reviews?action=write&scenicId=${detail.value.id}`);
  }
}

watch(
  scenicId,
  (id) => {
    void loadDetail(id);
  },
  { immediate: true }
);
</script>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.breadcrumb-container {
  margin-bottom: 24px;
  font-size: 13px;
}

.breadcrumb-container :deep(.el-breadcrumb__inner) {
  color: #666;
  font-weight: 400;
}
.breadcrumb-container :deep(.el-breadcrumb__inner:hover) {
  color: #000;
  text-decoration: underline;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left {
  flex: 1;
}

.scenic-title {
  font-size: 36px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  line-height: 1.2;
}

.scenic-rating {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 14px;
  color: #333;
}

.rating-dots {
  display: flex;
  gap: 4px;
}

.dot {
  width: 14px; height: 14px; border-radius: 50%;
  border: 1px solid #34e0a1; background: #fff;
}
.dot.filled { background: #34e0a1; border-color: #34e0a1;}

.rating-score {
  font-weight: 700;
}

.review-count {
  text-decoration: underline;
  cursor: pointer;
  color: #1a1a1a;
}
.review-count:hover {
  color: #000;
}

.rank-text {
  color: #333;
}

.metric-item {
  display: flex;
  align-items: center;
  color: #666;
  gap: 4px;
}
.metric-item .el-icon {
  font-size: 15px;
}

.header-right {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}

.action-btn {
  border: 1px solid #1a1a1a;
  color: #1a1a1a;
  font-weight: 700;
  height: 40px;
  padding: 0 20px;
}
.action-btn:hover {
  background-color: #f5f5f5;
  border-color: #000;
  color: #000;
}

.red-star {
  color: #ff4d4f;
}

.photo-gallery {
  display: flex;
  gap: 4px;
  height: 440px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 40px;
}

.main-photo {
  flex: 2;
  background-size: cover;
  background-position: center;
  position: relative;
  cursor: pointer;
  transition: opacity 0.2s;
}
.main-photo:hover { opacity: 0.95; }

.side-photos {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.side-photo {
  flex: 1;
  background-size: cover;
  background-position: center;
  position: relative;
  cursor: pointer;
  transition: opacity 0.2s;
}
.side-photo:hover { opacity: 0.95; }

.photo-overlay {
  position: absolute;
  right: 16px; bottom: 16px;
  background: rgba(0,0,0,0.7);
  color: #fff;
  border-radius: 4px;
  padding: 6px 12px;
  display: flex; align-items: center; gap: 8px;
  font-weight: 600; font-size: 14px;
}

.content-grid {
  display: flex;
  gap: 40px;
}

.left-column {
  flex: 2;
  min-width: 0;
}

.right-column {
  flex: 1;
  min-width: 0;
}

.sticky-block {
  position: sticky;
  top: 80px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-section h2 {
  font-size: 24px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0 0 16px 0;
}

.scenic-intro {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
  white-space: pre-wrap;
}

.scenic-intro.expanded {
  -webkit-line-clamp: unset;
  display: block;
}

.read-more-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #1a1a1a;
  font-weight: 700;
  text-decoration: underline;
  margin-top: 10px;
  cursor: pointer;
  text-underline-offset: 2px;
}
.read-more-link:hover { color: #000; }

.info-section {
  padding-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
}
.recommend-experiences {
  margin-top: 40px;
}
.recommend-experiences h2 {
  font-size: 22px;
  font-weight: 800;
  margin: 0 0 24px 0;
}

.experience-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.exp-card {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.exp-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.exp-image {
  height: 140px;
  background-size: cover;
  background-position: center;
}

.exp-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.exp-content h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.4;
  flex: 1;
}

.exp-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #666;
  margin-bottom: 16px;
}

.exp-reason {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #888;
}

.book-btn {
  width: 100%;
  font-weight: 700;
  background-color: #34e0a1;
  border-color: #34e0a1;
  color: #fff;
}
.book-btn:hover {
  background-color: #2ecf93;
  border-color: #2ecf93;
}

.ticket-card {
  border-radius: 12px;
  border: 1px solid #eaeaea;
}
.ticket-card h3 { font-size: 18px; font-weight: 800; margin: 0 0 8px 0; }
.ticket-card p { font-size: 14px; color: #666; margin: 0 0 16px 0; }

.full-btn {
  width: 100%;
  height: 48px;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 700;
  background-color: #1a1a1a;
  border-color: #1a1a1a;
  color: #fff;
}
.full-btn:hover { background-color: #000; color: #fff; }

.hours-card {
  border-radius: 12px;
  border: 1px solid #eaeaea;
}
.hours-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
}
.hours-header h3 { font-size: 18px; font-weight: 800; margin: 0; }
.hours-header .el-icon {
  cursor: pointer; font-size: 18px; color: #1a1a1a;
}

.hours-status { margin-bottom: 8px; }
.open-status {
  color: #2eac6d; font-weight: 700; font-size: 15px;
}

.hours-detail {
  display: flex; justify-content: space-between;
  font-size: 15px; color: #333; margin-top: 8px;
}

@media (max-width: 992px) {
  .content-grid { flex-direction: column; }
  .photo-gallery { height: 300px; }
  .experience-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .photo-gallery { flex-direction: column; height: auto; }
  .main-photo { height: 240px; }
  .side-photos { flex-direction: row; height: 120px; }
  .experience-grid { grid-template-columns: 1fr; }
}

/* User Reviews Section Styles */
.reviews-section {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid #eaeaea;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.reviews-header h2 {
  font-size: 24px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0;
}

.write-review-btn {
  border: 1px solid #1a1a1a;
  color: #1a1a1a;
}
.write-review-btn:hover {
  background: #1a1a1a;
  color: #fff;
}

.review-item {
  padding: 24px 0;
  border-bottom: 1px solid #f0f0f0;
}

.reviewer-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.reviewer-meta {
  display: flex;
  flex-direction: column;
}

.reviewer-name {
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.review-date {
  font-size: 13px;
  color: #888;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.visit-date {
  font-size: 14px;
  color: #666;
}

.review-content {
  font-size: 15px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 16px;
  white-space: pre-wrap;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.review-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  border: 1px solid #eaeaea;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
