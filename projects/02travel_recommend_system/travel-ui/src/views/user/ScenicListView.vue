<!-- 极简风格景点列表页 -->
<template>
  <div class="page">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar">
      <div class="filter-inner">
        <div class="filter-row">
          <input
            v-model="query.keyword"
            type="text"
            class="filter-search"
            placeholder="搜索景点、地区、关键词..."
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

          <!-- 标签选择按钮 -->
          <button class="btn-tag" @click="openTagModal">
            <svg
              class="btn-tag-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h20z" />
              <path d="M18 8V2" />
            </svg>
            标签
            <span v-if="query.tagIds.length > 0" class="btn-tag-count">
              {{ query.tagIds.length }}
            </span>
          </button>

          <button class="btn-reset" @click="resetFilters">重置</button>
        </div>
      </div>
    </div>

    <!-- 标签选择弹窗 -->
    <div v-if="showTagModal" class="modal-overlay" @click.self="cancelTagSelect">
      <div class="modal-content">
        <div class="modal-header">
          <h3 class="modal-title">选择标签</h3>
          <button class="modal-close" @click="cancelTagSelect">&times;</button>
        </div>
        <div class="modal-body">
          <div v-for="group in groupedTags" :key="group.category" class="modal-tag-group">
            <span class="modal-tag-group-title">{{ group.category }}</span>
            <div class="modal-tag-list">
              <button
                v-for="tag in group.tags"
                :key="tag.id"
                class="modal-tag-chip"
                :class="{ active: tempTagIds.includes(tag.id) }"
                @click="toggleTempTag(tag.id)"
              >
                <img
                  v-if="tag.icon"
                  :src="tag.icon"
                  class="modal-tag-icon"
                  @error="onTagImgError($event)"
                />
                <span class="modal-tag-text">{{ tag.name }}</span>
              </button>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <span class="modal-selected">已选 {{ tempTagIds.length }} 个标签</span>
          <div class="modal-actions">
            <button class="modal-btn modal-btn-cancel" @click="cancelTagSelect">取消</button>
            <button class="modal-btn modal-btn-confirm" @click="confirmTagSelect">确认</button>
          </div>
        </div>
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
import { ref, reactive, computed, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { getScenicPage as getScenicList, type ScenicItem } from "@/api/scenic";
import { getRegionTree, getTags, type CommonRegionNode, type CommonTagItem } from "@/api/common";

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const scenicList = ref<ScenicItem[]>([]);
const total = ref(0);
const regionList = ref<CommonRegionNode[]>([]);
const allTags = ref<CommonTagItem[]>([]);
const showTagModal = ref(false);
const tempTagIds = ref<number[]>([]);

interface TagGroup {
  category: string;
  tags: CommonTagItem[];
}

const groupedTags = computed<TagGroup[]>(() => {
  const map = new Map<string, CommonTagItem[]>();
  for (const tag of allTags.value) {
    const cat = tag.category || "其他";
    if (!map.has(cat)) map.set(cat, []);
    map.get(cat)!.push(tag);
  }
  return Array.from(map.entries())
    .sort((a, b) => (a[0] === "其他" ? 1 : b[0] === "其他" ? -1 : a[0].localeCompare(b[0])))
    .map(([category, tags]) => ({ category, tags }));
});

const query = reactive({
  keyword: undefined as string | undefined,
  regionId: undefined as number | undefined,
  category: undefined as string | undefined,
  level: undefined as string | undefined,
  tagIds: [] as number[],
  sortBy: "" as "" | "hot" | "score" | "createdAt",
  sortOrder: "DESC" as "ASC" | "DESC" | undefined,
  pageNum: 1,
  pageSize: 8,
});

/**
 * 根据路由查询参数初始化筛选条件，支持 keyword、regionId、tagId 三个参数。
 */
function applyRouteQuery(): void {
  const q = route.query.keyword as string | undefined;
  if (q && q.trim()) {
    query.keyword = q.trim();
  }
  const rid = route.query.regionId as string | undefined;
  if (rid) {
    const id = Number(rid);
    if (!isNaN(id)) {
      query.regionId = id;
    }
  }
  const tid = route.query.tagId as string | undefined;
  if (tid) {
    const id = Number(tid);
    if (!isNaN(id) && !query.tagIds.includes(id)) {
      query.tagIds.push(id);
    }
  }
}

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
  query.tagIds = [];
  query.sortBy = "";
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
      tagIds: query.tagIds.length > 0 ? query.tagIds : undefined,
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

async function loadTags(): Promise<void> {
  try {
    allTags.value = await getTags();
  } catch {
    // ignore
  }
}

function openTagModal(): void {
  tempTagIds.value = [...query.tagIds];
  showTagModal.value = true;
}

function toggleTempTag(id: number): void {
  const idx = tempTagIds.value.indexOf(id);
  if (idx === -1) {
    tempTagIds.value.push(id);
  } else {
    tempTagIds.value.splice(idx, 1);
  }
}

function confirmTagSelect(): void {
  query.tagIds = [...tempTagIds.value];
  showTagModal.value = false;
  query.pageNum = 1;
  loadData();
}

function cancelTagSelect(): void {
  showTagModal.value = false;
}

function onTagImgError(event: Event): void {
  (event.target as HTMLImageElement).style.display = "none";
}

onMounted(() => {
  applyRouteQuery();
  loadData();
  loadRegions();
  loadTags();
});

watch(
  () => route.query.keyword,
  (newKeyword) => {
    const q = newKeyword as string | undefined;
    query.keyword = q?.trim() || undefined;
    query.pageNum = 1;
    loadData();
  }
);
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
  padding: 20px 0 16px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
}

.filter-inner {
  max-width: 1200px;
  padding: 0 24px;
  margin: 0 auto;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.filter-search {
  flex: 1;
  min-width: 220px;
  padding: 10px 16px;
  font-size: 14px;
  color: #000000;
  outline: none;
  background: #f7f7f7;
  border: 1.5px solid #eeeeee;
  border-radius: 10px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.filter-search:focus {
  background: #ffffff;
  border-color: #00e676;
  box-shadow: 0 0 0 3px rgba(0, 230, 118, 0.12);
}

.filter-select {
  min-width: 120px;
  padding: 10px 14px;
  padding-right: 32px;
  font-size: 14px;
  color: #000000;
  appearance: none;
  cursor: pointer;
  outline: none;
  background: #f7f7f7
    url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%23999' stroke-width='1.5' fill='none'/%3E%3C/svg%3E")
    no-repeat right 12px center;
  border: 1.5px solid #eeeeee;
  border-radius: 10px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.filter-select:focus {
  border-color: #00e676;
  box-shadow: 0 0 0 3px rgba(0, 230, 118, 0.12);
}

.btn-reset {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #999999;
  cursor: pointer;
  background: transparent;
  border: 1.5px solid #e0e0e0;
  border-radius: 10px;
  transition: all 0.2s;
}

.btn-reset:hover {
  color: #000000;
  border-color: #000000;
}

/* Tag button */
.btn-tag {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #666666;
  cursor: pointer;
  background: #f7f7f7;
  border: 1.5px solid #eeeeee;
  border-radius: 10px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-tag:hover {
  color: #000000;
  background: #ffffff;
  border-color: #00e676;
  box-shadow: 0 0 0 3px rgba(0, 230, 118, 0.12);
}

.btn-tag-icon {
  width: 16px;
  height: 16px;
}

.btn-tag-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  font-size: 12px;
  font-weight: 600;
  color: #ffffff;
  background: #00e676;
  border-radius: 999px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease-out;
}

.modal-content {
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow: hidden;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #000000;
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  font-size: 20px;
  color: #999999;
  cursor: pointer;
  background: transparent;
  border: none;
  border-radius: 8px;
  transition: all 0.2s;
}

.modal-close:hover {
  color: #000000;
  background: #f5f5f5;
}

.modal-body {
  max-height: 50vh;
  padding: 20px 24px;
  overflow-y: auto;
}

.modal-body::-webkit-scrollbar {
  width: 4px;
}

.modal-body::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 2px;
}

.modal-tag-group {
  margin-bottom: 20px;
}

.modal-tag-group:last-child {
  margin-bottom: 0;
}

.modal-tag-group-title {
  display: flex;
  gap: 8px;
  align-items: center;
  height: 16px;
  margin-bottom: 12px;
  font-size: 11px;
  font-weight: 600;
  line-height: 16px;
  color: #999999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.modal-tag-group-title::before {
  display: block;
  flex-shrink: 0;
  width: 3px;
  height: 12px;
  content: "";
  background: #00e676;
  border-radius: 2px;
}

.modal-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.modal-tag-chip {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  height: 32px;
  padding: 0 14px;
  font-size: 13px;
  font-weight: 500;
  color: #666666;
  cursor: pointer;
  background: #f7f7f7;
  border: 1.5px solid transparent;
  border-radius: 999px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.modal-tag-chip:hover {
  color: #000000;
  background: #f0faf2;
  border-color: rgba(0, 230, 118, 0.3);
}

.modal-tag-chip.active {
  font-weight: 600;
  color: #000000;
  background: #e8f5e9;
  border-color: #00e676;
  box-shadow: 0 2px 8px rgba(0, 230, 118, 0.15);
}

.modal-tag-icon {
  width: 16px;
  height: 16px;
  object-fit: cover;
  border-radius: 4px;
}

.modal-tag-text {
  line-height: 1;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.modal-selected {
  font-size: 13px;
  color: #999999;
}

.modal-actions {
  display: flex;
  gap: 8px;
}

.modal-btn {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: 1.5px solid transparent;
  border-radius: 8px;
  transition: all 0.2s;
}

.modal-btn-cancel {
  color: #666666;
  background: #f5f5f5;
  border-color: #eeeeee;
}

.modal-btn-cancel:hover {
  color: #000000;
  background: #ffffff;
  border-color: #cccccc;
}

.modal-btn-confirm {
  color: #000000;
  background: #00e676;
  border-color: #00e676;
}

.modal-btn-confirm:hover {
  background: #00c665;
  border-color: #00c665;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Content */
.content {
  max-width: 1200px;
  padding: 40px 24px;
  margin: 0 auto;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #000000;
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
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card:hover {
  transform: translateY(-4px);
}

.card-img {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
  border-radius: 10px;
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
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 0 6px 0;
  font-size: 13px;
  color: #999999;
}

.score {
  font-weight: 600;
  color: #000000;
}

.card-category {
  margin: 0 0 6px 0;
  font-size: 13px;
  color: #999999;
}

.card-price {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #000000;
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
  font-weight: 500;
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

.page-size {
  padding: 10px 12px;
  font-size: 14px;
  color: #000000;
  cursor: pointer;
  background: #f5f5f5;
  border: 1px solid transparent;
  border-radius: 8px;
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
  border-radius: 10px;
}

.skeleton-img {
  width: 100%;
  height: 200px;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  border-radius: 10px;
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
