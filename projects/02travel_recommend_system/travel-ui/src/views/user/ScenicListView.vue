<!-- 极简风格景点列表页 -->
<template>
  <div class="page">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar">
      <div class="filter-inner">
        <input
          v-model="query.keyword"
          type="text"
          class="filter-search"
          placeholder="搜索景点..."
          @keyup.enter="loadData"
        />

        <select v-model="query.regionId" class="filter-select" @change="loadData">
          <option :value="undefined">全部地区</option>
          <option v-for="r in regionList" :key="r.id" :value="r.id">{{ r.name }}</option>
        </select>

        <select v-model="query.category" class="filter-select" @change="loadData">
          <option :value="undefined">全部分类</option>
          <option value="自然风光">自然风光</option>
          <option value="人文古迹">人文古迹</option>
          <option value="主题乐园">主题乐园</option>
          <option value="海滨度假">海滨度假</option>
          <option value="乡村旅游">乡村旅游</option>
        </select>

        <select v-model="query.level" class="filter-select" @change="loadData">
          <option :value="undefined">全部等级</option>
          <option value="5A">5A</option>
          <option value="4A">4A</option>
          <option value="3A">3A</option>
        </select>

        <select v-model="query.sortBy" class="filter-select" @change="loadData">
          <option value="">默认排序</option>
          <option value="hot">热门优先</option>
          <option value="score">评分最高</option>
          <option value="createdAt">最新</option>
        </select>

        <button class="btn-reset" @click="resetFilters">重置</button>
      </div>
    </div>

    <div class="content">
      <div class="content-header">
        <h2 class="title">{{ query.keyword ? "搜索结果" : "全部景点" }}</h2>
        <span v-if="total > 0" class="count">共 {{ total }} 个景点</span>
      </div>

      <div v-if="loading" class="grid">
        <div v-for="n in 8" :key="n" class="skeleton-card">
          <div class="skeleton-img"></div>
          <div class="skeleton-line"></div>
          <div class="skeleton-line short"></div>
        </div>
      </div>

      <div v-else-if="scenicList.length === 0" class="empty">未找到符合条件的景点</div>

      <div v-else class="grid">
        <div v-for="item in scenicList" :key="item.id" class="card" @click="goDetail(item.id)">
          <div class="card-img">
            <img :src="item.coverImage || ''" :alt="item.name" />
            <span v-if="item.isFavorite" class="card-badge">已收藏</span>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ item.name }}</h3>
            <p class="card-meta">
              <span class="location">{{ item.regionName || "未知" }}</span>
              <span v-if="item.score" class="score">{{ item.score.toFixed(1) }}分</span>
            </p>
            <p v-if="item.category" class="card-category">{{ item.category }}</p>
            <p v-if="item.ticketPrice" class="card-price">¥{{ item.ticketPrice }}</p>
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
        <select v-model="query.pageSize" class="page-size" @change="loadData">
          <option :value="8">8条/页</option>
          <option :value="12">12条/页</option>
          <option :value="24">24条/页</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getScenicPage as getScenicList, type ScenicItem } from "@/api/scenic";
import { getRegionTree } from "@/api/common";
import type { CommonRegionNode } from "@/api/common";

const router = useRouter();
const loading = ref(false);
const scenicList = ref<ScenicItem[]>([]);
const total = ref(0);
const regionList = ref<CommonRegionNode[]>([]);

const query = reactive({
  keyword: undefined as string | undefined,
  regionId: undefined as number | undefined,
  category: undefined as string | undefined,
  level: undefined as string | undefined,
  sortBy: undefined as "hot" | "score" | "createdAt" | undefined,
  sortOrder: "DESC" as "ASC" | "DESC" | undefined,
  pageNum: 1,
  pageSize: 12,
});

function goDetail(id: number): void {
  router.push(`/scenic/${id}`);
}

function changePage(page: number): void {
  query.pageNum = page;
  loadData();
}

function resetFilters(): void {
  query.keyword = undefined;
  query.regionId = undefined;
  query.category = undefined;
  query.level = undefined;
  query.sortBy = undefined;
  query.sortOrder = "DESC";
  query.pageNum = 1;
  loadData();
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getScenicList({
      keyword: query.keyword,
      regionId: query.regionId,
      category: query.category,
      level: query.level,
      sortBy: query.sortBy || undefined,
      sortOrder: query.sortBy ? query.sortOrder : undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    scenicList.value = res.records;
    total.value = res.total;
  } catch {
    alert("加载失败，请稍后重试");
  } finally {
    loading.value = false;
  }
}

async function loadRegions(): Promise<void> {
  try {
    const tree = await getRegionTree();
    regionList.value = tree;
  } catch {
    // ignore
  }
}

onMounted(() => {
  loadData();
  loadRegions();
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #ffffff;
}

/* Filter bar */
.filter-bar {
  position: sticky;
  top: 64px;
  z-index: 50;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 0;
}

.filter-inner {
  display: flex;
  gap: 12px;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  flex-wrap: wrap;
}

.filter-search {
  flex: 1;
  min-width: 200px;
  padding: 10px 16px;
  font-size: 14px;
  color: #000000;
  background: #f5f5f5;
  border: 1px solid transparent;
  border-radius: 8px;
  outline: none;
  transition: all 0.2s;
}

.filter-search:focus {
  background: #ffffff;
  border-color: #00e676;
}

.filter-select {
  padding: 10px 12px;
  font-size: 14px;
  color: #000000;
  background: #f5f5f5;
  border: 1px solid transparent;
  border-radius: 8px;
  outline: none;
  cursor: pointer;
  min-width: 120px;
}

.filter-select:focus {
  border-color: #00e676;
}

.btn-reset {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #999999;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-reset:hover {
  color: #000000;
  border-color: #000000;
}

/* Content */
.content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #000000;
  margin: 0;
}

.count {
  font-size: 14px;
  color: #999999;
}

/* Grid */
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

@media (max-width: 1024px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
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
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: 10px;
  overflow: hidden;
  background: #f5f5f5;
}

.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 500;
  color: #000000;
  background: #00e676;
  border-radius: 999px;
}

.card-body {
  padding: 14px 4px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #999999;
  margin: 0 0 6px 0;
}

.score {
  font-weight: 600;
  color: #000000;
}

.card-category {
  font-size: 13px;
  color: #999999;
  margin: 0 0 6px 0;
}

.card-price {
  font-size: 15px;
  font-weight: 600;
  color: #000000;
  margin: 0;
}

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;
}

.page-btn {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #00e676;
  background: #f9fff9;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #999999;
}

.page-size {
  padding: 10px 12px;
  font-size: 14px;
  color: #000000;
  background: #f5f5f5;
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
}

/* Empty */
.empty {
  padding: 80px 20px;
  text-align: center;
  color: #999999;
  font-size: 15px;
}

/* Skeleton */
.skeleton-card {
  border-radius: 10px;
  overflow: hidden;
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
  animation: loading 1.5s infinite;
  border-radius: 4px;
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
