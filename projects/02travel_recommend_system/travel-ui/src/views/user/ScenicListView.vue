<template>
  <div class="scenic-list-page">
    <!-- Top Filter Section -->
    <div class="toolbar-section" :class="{ collapsed: isCollapsed }">
      <el-form :model="query" class="filter-form" @submit.prevent label-position="right" label-width="68px">
        <div class="form-row always">
          <el-form-item label="关键词" class="form-item form-item-keyword">
            <el-input
              v-model="query.keyword"
              placeholder="景点名/地区"
              clearable
              @keyup.enter="debouncedLoadScenicList"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="省份" class="form-item">
            <el-select v-model="selectedProvinceId" placeholder="全部" clearable @change="onProvinceChange">
              <el-option v-for="prov in regionTreeData" :key="prov.id" :label="prov.name" :value="prov.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="城市" class="form-item">
            <el-select v-model="selectedCityId" placeholder="全部城市" clearable :disabled="!selectedProvinceId" @change="onCityChange">
              <el-option v-for="city in currentCities" :key="city.id" :label="city.name" :value="city.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="分类" class="form-item">
            <el-select v-model="query.category" placeholder="全部" clearable>
              <el-option v-for="category in categoryOptions" :key="category" :label="category" :value="category" />
            </el-select>
          </el-form-item>

          <div class="action-group">
            <el-button type="primary" :loading="loading" @click="debouncedLoadScenicList" round class="fixed-btn search-btn">查询</el-button>
            <el-button @click="toggleExpand" round class="fixed-btn filter-btn">{{ expanded ? '收起' : '筛选' }}</el-button>
          </div>
        </div>

        <transition name="collapse">
          <div v-show="!isCollapsed || expanded" class="form-row expandable">
            <el-form-item label="等级" class="form-item">
              <el-select v-model="query.level" placeholder="全部" clearable>
                <el-option v-for="level in levelOptions" :key="level" :label="level" :value="level" />
              </el-select>
            </el-form-item>

            <el-form-item label="最低评分" class="form-item score">
              <el-select v-model="query.minScore" placeholder="不限" clearable>
                <el-option v-for="score in minScoreOptions" :key="score" :label="`${score} 分以上`" :value="score" />
              </el-select>
            </el-form-item>

            <el-form-item label="排序" class="form-item">
              <el-select v-model="query.sortBy" placeholder="热门" clearable>
                <el-option label="热门" value="hot" />
                <el-option label="评分" value="score" />
                <el-option label="创建时间" value="createTime" />
              </el-select>
            </el-form-item>

            <div class="form-actions">
              <el-button @click="resetFilters" round class="fixed-btn reset-btn">重置</el-button>
            </div>
          </div>
        </transition>
      </el-form>
    </div>

    <!-- Hero Banner Carousel -->
    <div class="hero-banner" v-if="!isSearchActive">
      <el-carousel height="380px" indicator-position="" :interval="5000">
        <el-carousel-item v-for="(bg, idx) in banners" :key="idx">
          <div class="banner-slide" :style="{ backgroundImage: `url('${bg}')` }">
            <div class="banner-overlay">
              <h1 class="banner-title">预订旅行者支持的景点玩乐</h1>
              <div class="banner-search-box">
                <el-icon class="search-icon"><Search /></el-icon>
                <input type="text" placeholder="按目的地搜索" v-model="query.keyword" @keyup.enter="loadScenicList" />
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- Main Content Area -->
    <div class="main-content">
      <div class="section-header">
        <h2 class="section-title">{{ isSearchActive ? '搜索结果' : '为您推荐' }}</h2>
        <p class="section-subtitle" v-if="!isSearchActive">正在浏览 {{ getCurrentRegionName() || '热门精选' }}？我们认为您会喜欢这些。</p>
        <p class="section-subtitle" v-else>共找到 <strong>{{ total }}</strong> 个符合条件的景点</p>
      </div>

      <div class="scenic-grid" v-loading="loading">
        <div 
          v-for="(item, index) in scenicList" 
          :key="item.id" 
          class="scenic-card"
          @click="goDetail(item, $event)"
        >
          <!-- Image Section -->
          <div class="card-image">
            <img :src="item.coverImage || 'https://via.placeholder.com/400x300?text=No+Image'" :alt="item.name" />
            <div class="card-rank">{{ index + 1 + (query.pageNum - 1) * query.pageSize }}</div>
            <div class="favorite-btn" :class="{ active: item.isFavorite }" @click.stop.prevent="toggleFavorite(item, $event)">
              <el-icon @click.stop.prevent="toggleFavorite(item, $event)"><Star /></el-icon>
            </div>
          </div>

          <!-- Content Section -->
          <div class="card-content">
            <div class="card-location">{{ item.regionName || '未知地区' }}</div>
            <h3 class="card-title">{{ item.name }}</h3>
            
            <div class="card-rating">
              <div class="rating-dots">
                <span v-for="i in 5" :key="i" class="dot" :class="{ filled: i <= Math.round(item.score || 0) }"></span>
              </div>
              <span class="review-count">{{ item.score ? item.score.toFixed(1) + ' 分' : '暂无评分' }}</span>
            </div>

            <div class="card-meta">
              <span class="location-text">{{ item.level ? item.level + '景区' : '普通景点' }}</span>
            </div>

            <div class="card-footer">
              <div class="price">
                <span class="price-prefix">低至</span>
                <span class="price-val">
                  {{ item.ticketPrice != null && Number(item.ticketPrice) > 0 ? `¥${item.ticketPrice}` : '免费' }}
                </span>
              </div>
              <span class="details-link">了解详情</span>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-if="!loading && scenicList.length === 0" description="未找到符合条件的景点" />

      <!-- Pagination -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[8, 12, 24, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadScenicList"
          @size-change="loadScenicList"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, reactive, ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { getRegionTree } from "@/api/common";
import {
  getScenicFilterOptions,
  getScenicPage,
  type ScenicItem,
  type ScenicQuery,
} from "@/api/scenic";
import { addFavorite, removeFavorite } from "@/api/favorite";
// Icons
import { Search, Star } from '@element-plus/icons-vue'
import { ElMessage } from "element-plus";

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const scenicList = ref<ScenicItem[]>([]);
const total = ref(0);
const regionTreeData = ref<any[]>([]);
const categoryOptions = ref<string[]>([]);
const levelOptions = ref<string[]>([]);
const selectedProvinceId = ref<number | undefined>(undefined);
const selectedCityId = ref<number | undefined>(undefined);

const currentCities = computed(() => {
  if (!selectedProvinceId.value) return [];
  const province = regionTreeData.value.find(p => p.id === selectedProvinceId.value);
  return province?.children || [];
});

function onProvinceChange(): void {
  selectedCityId.value = undefined;
  query.regionId = selectedProvinceId.value;
}

function onCityChange(): void {
  query.regionId = selectedCityId.value || selectedProvinceId.value;
}
const minScoreOptions = [1, 2, 3, 4, 4.5];
const isCollapsed = ref(false);
const expanded = ref(false);
let loadTimer: number | null = null;
let scrollListener: (() => void) | null = null;

const banners = [
  'https://dynamic-media-cdn.tripadvisor.com/media/photo-o/2f/c9/1b/2f/caption.jpg?w=1800&h=-1&s=1',
  'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80',
  'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80'
];

const query = reactive<ScenicQuery>({
  pageNum: 1,
  pageSize: 12,
  keyword: "",
  regionId: undefined,
  category: undefined,
  level: undefined,
  minScore: undefined,
  sortBy: "hot",
});

const isSearchActive = computed(() => {
  return (
    !!query.keyword ||
    !!query.regionId ||
    !!query.category ||
    !!query.level ||
    query.minScore !== undefined ||
    query.sortBy !== "hot"
  );
});

function getCurrentRegionName() {
  if (!query.regionId) return '';
  let foundName = '';
  const walk = (nodes: any[]) => {
    for (const node of nodes) {
      if (node.id === query.regionId) {
        foundName = node.name;
        return true;
      }
      if (node.children?.length && walk(node.children)) {
        return true;
      }
    }
    return false;
  };
  walk(regionTreeData.value);
  return foundName;
}

async function loadRegions(): Promise<void> {
  const regions = await getRegionTree();
  // Clear empty children arrays to avoid rendering empty folders
  const walk = (nodes: any[]) => {
    nodes.forEach(node => {
      if (node.children && node.children.length === 0) {
        node.children = undefined;
      } else if (node.children && node.children.length > 0) {
        walk(node.children);
      }
    });
  };
  walk(regions);
  regionTreeData.value = regions;
}

async function loadFilterOptions(): Promise<void> {
  try {
    const options = await getScenicFilterOptions();
    categoryOptions.value = options.categories ?? [];
    levelOptions.value = options.levels ?? [];
  } catch {
    categoryOptions.value = [];
    levelOptions.value = [];
  }
}

function debounceLoadScenicList(): void {
  if (loadTimer) {
    window.clearTimeout(loadTimer);
  }
  loadTimer = window.setTimeout(() => {
    void loadScenicList();
  }, 300);
}

function debouncedLoadScenicList(): void {
  debounceLoadScenicList();
}

function toggleExpand(): void {
  expanded.value = !expanded.value;
}

function handleScroll(): void {
  isCollapsed.value = window.scrollY > 120;
  if (!isCollapsed.value) {
    expanded.value = false;
  }
}

function resetFilters(): void {
  query.keyword = "";
  query.regionId = undefined;
  selectedProvinceId.value = undefined;
  selectedCityId.value = undefined;
  query.category = undefined;
  query.level = undefined;
  query.minScore = undefined;
  query.sortBy = "hot";
  query.pageNum = 1;
}

async function loadScenicList(): Promise<void> {
  loading.value = true;
  try {
    const result = await getScenicPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      regionId: query.regionId,
      category: query.category || undefined,
      level: query.level || undefined,
      minScore: query.minScore,
      sortBy: query.sortBy,
    });
    scenicList.value = result.records;
    total.value = result.total;
  } finally {
    loading.value = false;
  }
}

function goDetail(row: ScenicItem, event?: MouseEvent): void {
  const target = event?.target as HTMLElement | null;
  if (target?.closest(".favorite-btn")) {
    return;
  }
  void router.push(`/scenic/${row.id}`);
}

async function toggleFavorite(item: ScenicItem, event?: MouseEvent): Promise<void> {
  event?.preventDefault();
  event?.stopPropagation();

  try {
    if (item.isFavorite) {
      await removeFavorite(item.id);
      item.isFavorite = false;
      ElMessage.success(`已取消收藏 ${item.name}`);
      return;
    }

    await addFavorite(item.id);
    item.isFavorite = true;
    ElMessage.success(`已收藏 ${item.name}`);
  } catch {
    ElMessage.error("收藏操作失败，请稍后重试");
  }
}

onMounted(async () => {
  await Promise.all([loadRegions(), loadFilterOptions()]);
  query.keyword = (route.query.keyword as string) || "";
  await loadScenicList();
  scrollListener = handleScroll;
  window.addEventListener("scroll", handleScroll, { passive: true });
});

onBeforeUnmount(() => {
  if (scrollListener) {
    window.removeEventListener("scroll", handleScroll);
  }
  if (loadTimer) {
    window.clearTimeout(loadTimer);
  }
});
</script>

<style scoped>
.scenic-list-page {
  background-color: #fafafa;
  min-height: 100vh;
  padding-bottom: 60px;
}

.toolbar-section {
  position: sticky;
  top: 0;
  z-index: 100;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 24px;
  background: #fff;
  border-bottom: 1px solid #eaeaea;
  box-sizing: border-box;
}

.toolbar-section.collapsed {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  margin: 0;
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  width: 100%;
}

.form-row.always {
  justify-content: space-between;
}

.form-item {
  width: 180px;
}

.form-item-keyword {
  width: 240px;
}

.form-item.score {
  width: 200px;
}

.form-item :deep(.el-input),
.form-item :deep(.el-select),
.form-item :deep(.el-tree-select) {
  width: 100%;
}

.action-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.form-actions {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  padding-top: 8px;
  border-top: 1px solid #f5f5f5;
}

.fixed-btn {
  width: 100px;
  margin-left: 0;
  box-sizing: border-box;
  text-align: center;
}

.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.25s ease;
}

.collapse-enter-from,
.collapse-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 1023px) {
  .form-item,
  .form-item.score,
  .form-item-keyword {
    width: calc(50% - 8px);
  }
}

@media (max-width: 767px) {
  .form-item,
  .form-item.score,
  .form-item-keyword,
  .action-group {
    width: 100%;
  }
  .action-group {
    justify-content: flex-end;
  }
}

.hero-banner {
  width: 100%;
  margin-bottom: 40px;
  background: #fff;
}

.banner-slide {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
}

.banner-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.banner-title {
  color: #fff;
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 32px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.5);
  text-align: center;
}

.banner-search-box {
  background: #fff;
  border-radius: 999px;
  width: 600px;
  max-width: 90%;
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 28px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
}

.search-icon {
  font-size: 22px;
  color: #333;
  margin-right: 16px;
}

.banner-search-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 18px;
  color: #333;
  background: transparent;
}

.banner-search-box input::placeholder {
  color: #888;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 28px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.section-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.scenic-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

@media (max-width: 1024px) {
  .scenic-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 768px) {
  .scenic-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 480px) {
  .scenic-grid {
    grid-template-columns: 1fr;
  }
}

.scenic-card {
  border: 1px solid #eaeaea;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.scenic-card:hover {
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
  transform: translateY(-4px);
}

.card-image {
  position: relative;
  width: 100%;
  height: 220px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.scenic-card:hover .card-image img {
  transform: scale(1.05);
}

.card-rank {
  position: absolute;
  bottom: 0;
  left: 0;
  background: rgba(255,255,255,0.9);
  color: #000;
  font-size: 24px;
  font-weight: 800;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top-right-radius: 12px;
  backdrop-filter: blur(4px);
}

.favorite-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1a1a1a;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transition: all 0.2s ease;
}

.favorite-btn:hover {
  transform: scale(1.1);
  color: #ff4d4f;
}

.favorite-btn.active {
  color: #ff4d4f;
  background: #fff5f5;
}

.card-content {
  padding: 16px 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-location {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 10px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.rating-dots {
  display: flex;
  gap: 3px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid #34e0a1;
  background: #fff;
}

.dot.filled {
  background: #34e0a1;
}

.review-count {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.card-meta {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-prefix {
  font-size: 12px;
  color: #666;
}

.price-val {
  font-size: 20px;
  font-weight: 800;
  color: #1a1a1a;
}

.details-link {
  font-size: 14px;
  font-weight: 700;
  color: #1a1a1a;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.details-link:hover {
  color: #000;
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
}
</style>
