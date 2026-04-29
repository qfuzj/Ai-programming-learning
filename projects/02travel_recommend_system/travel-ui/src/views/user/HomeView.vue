<!-- 极简风格用户首页 -->
<template>
  <div class="home">
    <!-- 搜索区域 -->
    <section class="hero">
      <div class="hero-inner">
        <h1 class="hero-title">去哪里？</h1>
        <p class="hero-subtitle">发现最适合您的旅行目的地</p>

        <div class="search-bar">
          <input
            v-model="keyword"
            type="text"
            class="search-input"
            placeholder="搜索景点、地区、关键词..."
            @keyup.enter="goSearch"
          />
          <button class="search-btn" @click="goSearch">搜索</button>
        </div>
      </div>
    </section>

    <div class="page-content">
      <!-- 热门推荐 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">热门推荐</h2>
          <p class="section-desc">正在浏览热门景点？我们认为您会喜欢这些</p>
        </div>

        <div v-if="loading" class="skeleton-grid">
          <div v-for="n in 4" :key="n" class="skeleton-card">
            <div class="skeleton-img"></div>
            <div class="skeleton-line"></div>
            <div class="skeleton-line short"></div>
          </div>
        </div>

        <div v-else-if="hotList.length === 0" class="empty">暂无推荐景点</div>

        <div v-else class="scroll-list">
          <div
            v-for="(item, idx) in hotList"
            :key="item.id"
            class="card"
            @click="goDetail(item.id)"
          >
            <div v-if="idx < 3" class="rank">TOP {{ idx + 1 }}</div>
            <div class="card-img">
              <img :src="item.coverImage || ''" :alt="item.name" />
              <div v-if="item.category" class="card-tag">{{ item.category }}</div>
            </div>
            <div class="card-body">
              <h3 class="card-title">{{ item.name }}</h3>
              <p class="card-location">{{ item.regionName || "未知" }}</p>
              <div class="card-meta">
                <span class="score">{{ item.score ? item.score.toFixed(1) : "5.0" }}</span>
                <span v-if="item.ticketPrice" class="price">¥{{ item.ticketPrice }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 标签分类 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">探索分类</h2>
          <p class="section-desc">按兴趣发现景点</p>
        </div>

        <div v-if="loading" class="tag-grid">
          <div v-for="n in 6" :key="n" class="skeleton-tag"></div>
        </div>

        <div v-else-if="tagList.length === 0" class="empty">暂无分类</div>

        <div v-else class="tag-grid">
          <div v-for="tag in tagList" :key="tag.id" class="tag-card" @click="goSearchByTag(tag)">
            <div class="tag-icon" :style="{ background: '#00e676' }">
              {{ tag.name.substring(0, 1) }}
            </div>
            <span class="tag-name">{{ tag.name }}</span>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getScenicHotList, type ScenicItem } from "@/api/scenic";
import { getMyPreferenceTags } from "@/api/profile";
import type { CommonTagItem } from "@/api/common";

const router = useRouter();
const loading = ref(false);
const keyword = ref("");
const hotList = ref<ScenicItem[]>([]);
const tagList = ref<CommonTagItem[]>([]);

function goDetail(id: number): void {
  router.push(`/scenic/${id}`);
}

function goSearch(): void {
  const q = keyword.value.trim();
  router.push({ path: "/scenic", query: q ? { keyword: q } : undefined });
}

function goSearchByTag(tag: CommonTagItem): void {
  router.push({ path: "/scenic", query: { tagId: String(tag.id) } });
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    hotList.value = await getScenicHotList();
    try {
      tagList.value = await getMyPreferenceTags();
    } catch {
      // ignore
    }
  } catch {
    alert("景点加载失败，请稍后重试");
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.home {
  width: 100%;
}

/* Hero */
.hero {
  background: #ffffff;
  padding: 80px 24px 60px;
  text-align: center;
}

.hero-inner {
  max-width: 680px;
  margin: 0 auto;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  color: #000000;
  margin: 0 0 12px 0;
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: 16px;
  color: #999999;
  margin: 0 0 40px 0;
}

.search-bar {
  display: flex;
  gap: 0;
  max-width: 600px;
  margin: 0 auto;
  border: 1px solid #e0e0e0;
  border-radius: 999px;
  overflow: hidden;
  background: #ffffff;
}

.search-input {
  flex: 1;
  padding: 16px 24px;
  font-size: 16px;
  color: #000000;
  border: none;
  outline: none;
  background: transparent;
}

.search-input::placeholder {
  color: #999999;
}

.search-btn {
  padding: 16px 32px;
  font-size: 15px;
  font-weight: 600;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s;
  margin: 4px;
}

.search-btn:hover {
  background: #00c665;
}

/* Page content */
.page-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 24px;
}

/* Section */
.section {
  margin-bottom: 80px;
}

.section-header {
  margin-bottom: 40px;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 8px 0;
}

.section-desc {
  font-size: 15px;
  color: #999999;
  margin: 0;
}

/* Scroll list */
.scroll-list {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  padding-bottom: 20px;
}

.scroll-list::-webkit-scrollbar {
  height: 4px;
}

.scroll-list::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 4px;
}

.card {
  flex: 0 0 auto;
  width: 280px;
  cursor: pointer;
  transition: transform 0.2s;
}

.card:hover {
  transform: translateY(-4px);
}

.card-img {
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  background: #f5f5f5;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 500;
  color: #000000;
  background: #00e676;
  border-radius: 999px;
}

.rank {
  font-size: 14px;
  font-weight: 700;
  color: #ff5252;
  margin-bottom: 8px;
}

.card-body {
  padding: 16px 4px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-location {
  font-size: 13px;
  color: #999999;
  margin: 0 0 8px 0;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score {
  font-size: 14px;
  font-weight: 600;
  color: #000000;
}

.price {
  font-size: 14px;
  color: #666666;
}

/* Tag grid */
.tag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.tag-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.tag-card:hover {
  border-color: #00e676;
  background: #f9fff9;
}

.tag-icon {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  font-size: 18px;
  font-weight: 700;
  color: #000000;
  border-radius: 10px;
  flex-shrink: 0;
}

.tag-name {
  font-size: 14px;
  font-weight: 500;
  color: #000000;
}

/* Skeleton */
.skeleton-grid {
  display: flex;
  gap: 20px;
}

.skeleton-card {
  flex: 0 0 auto;
  width: 280px;
}

.skeleton-img {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 12px;
}

.skeleton-line {
  height: 16px;
  margin-top: 12px;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-line.short {
  width: 60%;
}

.skeleton-tag {
  height: 72px;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 12px;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.empty {
  padding: 40px;
  text-align: center;
  color: #999999;
  font-size: 15px;
}
</style>
