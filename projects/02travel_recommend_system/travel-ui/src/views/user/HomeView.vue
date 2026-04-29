<!-- 精致极简风格用户首页 -->
<template>
  <div class="home">
    <!-- 搜索区域 -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-inner">
        <h1 class="hero-title">去哪里？</h1>
        <p class="hero-subtitle">发现最适合您的旅行目的地</p>

        <div class="search-wrapper">
          <div class="search-bar">
            <svg
              class="search-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
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

        <div v-else class="hot-grid">
          <div
            v-for="(item, idx) in hotList"
            :key="item.id"
            class="card"
            :style="{ animationDelay: `${idx * 0.08}s` }"
            @click="goDetail(item.id)"
          >
            <div class="card-img">
              <img :src="item.coverImage || ''" :alt="item.name" />
              <span class="card-rank">TOP {{ idx + 1 }}</span>
              <span class="card-hot-badge">热门</span>
            </div>
            <div class="card-body">
              <div class="card-title-row">
                <h3 class="card-title">{{ item.name }}</h3>
                <span v-if="item.category" class="card-category-tag">{{ item.category }}</span>
              </div>
              <p class="card-location">{{ item.regionName || "未知" }}</p>
              <div class="card-meta">
                <span class="score">{{ item.score ? item.score.toFixed(1) : "5.0" }}</span>
                <span
                  v-if="item.ticketPrice !== undefined && item.ticketPrice !== null"
                  class="price"
                >
                  {{ item.ticketPrice === 0 ? "免费" : "¥" + item.ticketPrice }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 探索兴趣 -->
      <section class="section">
        <div class="section-header">
          <h2 class="section-title">探索兴趣</h2>
          <p class="section-desc">发现您可能喜欢的景点类型</p>
        </div>

        <div v-if="loading" class="tag-grid">
          <div v-for="n in 6" :key="n" class="skeleton-tag"></div>
        </div>

        <div v-else-if="tagList.length === 0" class="empty">暂无标签</div>

        <div v-else class="tag-grid">
          <div
            v-for="(tag, idx) in tagList"
            :key="tag.id"
            class="tag-card"
            :style="{ animationDelay: `${idx * 0.06}s` }"
            @click="goSearchByTag(tag)"
          >
            <div class="tag-icon-wrapper">
              <img
                v-if="tag.icon"
                :src="tag.icon"
                :alt="tag.name"
                class="tag-icon-img"
                @error="onTagImageError($event)"
              />
              <span v-else class="tag-icon-text">{{ tag.name.substring(0, 1) }}</span>
            </div>
            <div class="tag-info">
              <span class="tag-name">{{ tag.name }}</span>
              <span class="tag-category">{{ tag.category }}</span>
            </div>
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
import { getTagsByScope } from "@/api/common";
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

function onTagImageError(event: Event): void {
  const img = event.target as HTMLImageElement;
  img.style.display = "none";
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    hotList.value = await getScenicHotList();
    try {
      tagList.value = await getTagsByScope("SCENIC");
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
  position: relative;
  background: linear-gradient(180deg, #fafffe 0%, #ffffff 100%);
  padding: 100px 24px 80px;
  text-align: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  top: -50%;
  left: -10%;
  width: 120%;
  height: 200%;
  background:
    radial-gradient(ellipse at 30% 20%, rgba(0, 230, 118, 0.08) 0%, transparent 60%),
    radial-gradient(ellipse at 70% 80%, rgba(0, 230, 118, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.hero-inner {
  position: relative;
  z-index: 1;
  max-width: 680px;
  margin: 0 auto;
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  color: #000000;
  margin: 0 0 16px 0;
  letter-spacing: -1.5px;
  line-height: 1.1;
  animation: fadeInUp 0.6s ease-out both;
}

.hero-subtitle {
  font-size: 18px;
  color: #666666;
  margin: 0 0 48px 0;
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

.search-wrapper {
  max-width: 600px;
  margin: 0 auto;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 0;
  background: #ffffff;
  border: 1.5px solid #e0e0e0;
  border-radius: 999px;
  padding: 6px 6px 6px 20px;
  transition:
    border-color 0.3s,
    box-shadow 0.3s;
}

.search-bar:focus-within {
  border-color: #00e676;
  box-shadow: 0 0 0 4px rgba(0, 230, 118, 0.1);
}

.search-icon {
  width: 20px;
  height: 20px;
  color: #999999;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
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
  padding: 12px 28px;
  font-size: 15px;
  font-weight: 600;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition:
    background 0.2s,
    transform 0.1s;
  white-space: nowrap;
}

.search-btn:hover {
  background: #00c665;
}

.search-btn:active {
  transform: scale(0.97);
}

/* Page content */
.page-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 24px;
}

/* Section */
.section {
  margin-bottom: 100px;
  animation: fadeInUp 0.6s ease-out both;
}

.section-header {
  margin-bottom: 48px;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
}

.section-desc {
  font-size: 16px;
  color: #999999;
  margin: 0;
}

/* Hot grid */
.hot-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

@media (max-width: 1024px) {
  .hot-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .hot-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .hot-grid {
    grid-template-columns: 1fr;
  }
}

.card {
  cursor: pointer;
  transition:
    transform 0.4s cubic-bezier(0.4, 0, 0.2, 1),
    box-shadow 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.6s ease-out both;
  border-radius: 12px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #f0f0f0;
}

.card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  border-color: transparent;
}

.card:active {
  transform: translateY(-4px);
}

.card-img {
  position: relative;
  width: 100%;
  height: 220px;
  overflow: hidden;
  background: #f5f5f5;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.card:hover .card-img img {
  transform: scale(1.05);
}

.card-img::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.3) 0%, transparent 100%);
  pointer-events: none;
}

.card-rank {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #ffffff;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.7) 0%, rgba(0, 0, 0, 0.4) 100%);
  border-radius: 8px;
  z-index: 2;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.card-hot-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 5px 12px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.3px;
  color: #ffffff;
  background: linear-gradient(135deg, #ff5252 0%, #ff1744 100%);
  border-radius: 999px;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(255, 82, 82, 0.3);
}

.card-body {
  padding: 16px 4px;
}

.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.card-category-tag {
  flex-shrink: 0;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 500;
  color: #000000;
  background: #e8f5e9;
  border-radius: 4px;
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
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.5s ease-out both;
}

.tag-card:hover {
  border-color: #00e676;
  background: #f9fff9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 230, 118, 0.1);
}

.tag-card:active {
  transform: translateY(0);
}

.tag-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  background: #00e676;
  display: grid;
  place-items: center;
  transition: transform 0.2s;
}

.tag-card:hover .tag-icon-wrapper {
  transform: scale(1.1);
}

.tag-icon-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.tag-icon-text {
  font-size: 18px;
  font-weight: 700;
  color: #000000;
}

.tag-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.tag-name {
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  transition: color 0.2s;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-card:hover .tag-name {
  color: #00c665;
}

.tag-category {
  font-size: 11px;
  color: #999999;
}

/* Skeleton */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

@media (max-width: 1024px) {
  .skeleton-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .skeleton-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .skeleton-grid {
    grid-template-columns: 1fr;
  }
}

.skeleton-card {
  border-radius: 12px;
  overflow: hidden;
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

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.empty {
  padding: 40px;
  text-align: center;
  color: #999999;
  font-size: 15px;
}
</style>
