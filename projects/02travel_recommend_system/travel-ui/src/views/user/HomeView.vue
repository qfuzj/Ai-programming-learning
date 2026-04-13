<!-- 用户首页 -->
<template>
  <div class="home-wrapper">
    <!-- Hero 搜索区 -->
    <div class="home-top-search">
      <div class="hero-inner">
        <h1 class="home-top-title">去哪里？</h1>
        <div class="home-top-tabs">
          <div class="tab-btn is-active">
            <el-icon><House /></el-icon>
            搜索全部
          </div>
          <div class="tab-btn" @click="router.push('/scenic')">
            <el-icon><OfficeBuilding /></el-icon>
            酒店
          </div>
          <div class="tab-btn" @click="router.push('/scenic')">
            <el-icon><Camera /></el-icon>
            景点玩乐
          </div>
          <div class="tab-btn" @click="router.push('/scenic')">
            <el-icon><ForkSpoon /></el-icon>
            美食
          </div>
        </div>
        <div class="home-top-input-bar">
          <div class="search-input-wrapper">
            <el-icon class="search-icon"><Search /></el-icon>
            <el-input
              v-model="keyword"
              class="search-input"
              placeholder="景点玩乐、美食、地区..."
              clearable
              @keyup.enter="goScenicList"
            />
          </div>
          <el-button round class="search-btn" @click="goScenicList">查询</el-button>
        </div>
      </div>
    </div>

    <div class="home-page">
      <!-- 热门推荐部分：您可能会喜欢这些 -->
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">您可能会喜欢这些</h2>
          <p class="section-subtitle">基于热门趋势的景点推荐</p>
        </div>

        <el-skeleton v-if="loading" animated :rows="6" />
        <el-empty v-else-if="hotScenicList.length === 0" description="暂无推荐景点" />

        <!-- 横向滚动列表 -->
        <div v-else class="horizontal-scroll-list">
          <div
            v-for="item in hotScenicList"
            :key="item.id"
            class="scenic-card"
            @click="goScenicDetail(item.id)"
          >
            <div class="heart-icon-wrapper">
              <el-icon><Star /></el-icon>
            </div>
            <el-image class="scenic-cover" :src="item.coverImage || ''" fit="cover">
              <template #error>
                <div class="image-fallback">暂无图片</div>
              </template>
            </el-image>
            <div class="scenic-body">
              <div class="scenic-name">{{ item.name }}</div>
              <div class="scenic-rating">
                <span class="rating-number">
                  {{ Math.min(5, Number(item.score.toFixed(1))) }}
                </span>
                <span class="rating-dots">
                  <span
                    v-for="n in 5"
                    :key="n"
                    class="dot"
                    :class="{ 'is-active': n <= Math.round(item.score) }"
                  ></span>
                </span>
                <span class="review-count">({{ item.regionName || "未知" }})</span>
              </div>
              <div v-if="item.tags && item.tags.length" class="scenic-price">
                {{ item.tags.slice(0, 2).join(" · ") }}
              </div>
            </div>
          </div>
        </div>
      </div>

            <!-- 我的景区标签 -->
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">我的景区标签</h2>
          <p class="section-subtitle">查找您感兴趣的标签，快速发现对应景点</p>
        </div>

        <el-skeleton v-if="loading" animated :rows="6" />
        <el-empty v-else-if="tagList.length === 0" description="暂无标签" />

        <!-- 横向滚动列表 -->
        <div v-else class="horizontal-scroll-list">
          <div
            v-for="item in tagList"
            :key="item.id"
            class="scenic-card tag-card"
            @click="goScenicListByTag(item.name)"
          >
            <div class="tag-cover">
              <el-image v-if="item.icon" :src="item.icon" fit="cover" class="tag-image" />
              <div v-else class="tag-placeholder" :style="{ backgroundColor: item.color || '#00e676' }">
              <span class="tag-name-large">{{ item.name.substring(0, 2) }}</span>
              </div>
            </div>
            <div class="scenic-body">
              <div class="scenic-name">{{ item.name }}</div>
              <div v-if="item.category" class="scenic-price">{{ item.category }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Search, House, OfficeBuilding, Camera, ForkSpoon, Star } from "@element-plus/icons-vue";
import { getScenicHotList, type ScenicItem } from "@/api/scenic";
import { getTagsByType, type CommonTagItem } from "@/api/common";
import { getMyPreferenceTags } from "@/api/profile";

const router = useRouter();

const loading = ref(false);
const keyword = ref("");
const hotScenicList = ref<ScenicItem[]>([]);
const tagList = ref<CommonTagItem[]>([]);

function goScenicDetail(id: number): void {
  void router.push(`/scenic/${id}`);
}

function goScenicList(): void {
  const query = keyword.value.trim();
  void router.push({
    path: "/scenic",
    query: query ? { keyword: query } : undefined,
  });
}

function goScenicListByTag(tag: string): void {
  void router.push({
    path: "/scenic",
    query: { keyword: tag },
  });
}

async function loadHomeData(): Promise<void> {
  loading.value = true;
  try {
    hotScenicList.value = await getScenicHotList();
    try {
      tagList.value = await getMyPreferenceTags();
    } catch (err) {
      console.error(err);
    }
  } catch {
    ElMessage.warning("景点加载失败，请稍后重试");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadHomeData();
});
</script>

<style scoped>
.home-wrapper {
  width: 100%;
  min-width: 0;
  padding-top: 0;
  margin-top: 0;
}

/* 基本样式和容器 */
.home-page {
  max-width: 1200px;
  padding: 40px 20px;
  margin: 0 auto;
}

/* Hero Section */
.home-top-search {
  display: block !important;
  width: 100%;
  padding: 30px 20px 40px 20px;
  text-align: center;
  background-color: #ffffff;
  /* border-bottom: 1px solid #ebebfc; */
}

.hero-inner {
  max-width: 860px;
  margin: 0 auto;
}

.home-top-title {
  margin-bottom: 40px;
  font-size: 56px;
  font-weight: 800;
  color: #000;
}

.home-top-tabs {
  display: flex;
  gap: 24px;
  justify-content: center;
  margin-bottom: 24px;
}

.tab-btn {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 8px 16px;
  font-size: 15px;
  font-weight: 600;
  color: #555;
  cursor: pointer;
  border-radius: 999px;
  transition: all 0.2s;
}

.tab-btn:hover,
.tab-btn.is-active {
  color: #000;
  background: #f0f0f0;
}

.home-top-input-bar {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 8px 8px 8px 24px;
  background: #f7f9fa;
  border: 1px solid #e0e0e0;
  border-radius: 999px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.search-input-wrapper {
  display: flex;
  flex: 1;
  align-items: center;
}

.search-input :deep(.el-input__wrapper) {
  padding: 0;
  background: transparent;
  box-shadow: none !important;
}

.search-input :deep(.el-input__inner) {
  font-size: 16px;
  color: #000;
}

.search-icon {
  margin-right: 12px;
  font-size: 22px;
  color: #002d18;
}

.search-btn {
  height: 52px;
  padding: 14px 34px;
  font-size: 17px;
  font-weight: 700;
  color: #000 !important;
  background-color: #00e676 !important;
  border: none;
}

.search-btn:hover {
  background-color: #00c665 !important;
}

/* 章节通用 */
.section-container {
  margin-bottom: 48px;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 4px 0;
  font-size: 28px;
  font-weight: 800;
  color: #002d18;
}

.section-subtitle {
  margin: 0;
  font-size: 16px;
  color: #555;
}

/* 兴趣推荐网格 */
.tag-cover {
  display: grid;
  place-items: center;
  width: 100%;
  height: 280px;
  border-radius: 12px;
  color: white;
  box-shadow: inset 0 0 40px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.tag-card:hover .tag-cover {
  transform: scale(1.02);
}

.tag-image {
  width: 100%;
  height: 100%;
  border-radius: 12px;
}

.tag-placeholder {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  border-radius: 12px;
  color: white;
}

.tag-name-large {
  font-size: 72px;
  font-weight: 900;
  opacity: 0.9;
  letter-spacing: 4px;
  text-shadow: 2px 2px 8px rgba(0,0,0,0.2);
}

/* 热门推荐横向滚动 */
.horizontal-scroll-list {
  display: flex;
  gap: 16px;
  padding-bottom: 20px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
}

.horizontal-scroll-list::-webkit-scrollbar {
  height: 8px;
}
.horizontal-scroll-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 4px;
}
.horizontal-scroll-list::-webkit-scrollbar-track {
  background: transparent;
}

.scenic-card {
  position: relative;
  flex: 0 0 auto;
  width: 280px;
  cursor: pointer;
  scroll-snap-align: start;
}

.heart-icon-wrapper {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.heart-icon-wrapper:hover {
  transform: scale(1.1);
}

.heart-icon-wrapper .el-icon {
  font-size: 20px;
  color: #000;
}

.scenic-cover {
  width: 100%;
  height: 280px;
  background: #e0e0e0;
  border-radius: 12px;
}

.image-fallback {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  color: #888;
  background: #f0f0f0;
}

.scenic-body {
  padding: 12px 4px;
}

.scenic-name {
  display: -webkit-box;
  margin-bottom: 6px;
  overflow: hidden;
  -webkit-line-clamp: 2;
  font-size: 17px;
  font-weight: 800;
  line-height: 1.4;
  color: #002d18;
  -webkit-box-orient: vertical;
}

.scenic-rating {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 6px;
}

.rating-number {
  font-size: 15px;
  color: #555;
}

.rating-dots {
  display: flex;
  gap: 3px;
}

.dot {
  width: 12px;
  height: 12px;
  background: #fff;
  border: 2px solid #00a680;
  border-radius: 50%;
}

.dot.is-active {
  background: #00a680;
}

.review-count {
  font-size: 14px;
  color: #555;
}

.scenic-price {
  font-size: 15px;
  font-weight: 600;
  color: #002d18;
}

@media (max-width: 768px) {
  .home-top-title {
    font-size: 40px;
  }
  .interest-grid {
    grid-template-columns: 1fr;
  }
  .home-top-tabs {
    gap: 12px;
    justify-content: flex-start;
    width: 100%;
    overflow-x: auto;
  }
  .scenic-card {
    width: 240px;
  }
}
</style>
