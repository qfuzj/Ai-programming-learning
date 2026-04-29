<!-- 极简风格浏览历史页 -->
<template>
  <div class="page">
    <div class="header">
      <h1 class="title">浏览历史</h1>
      <button v-if="list.length > 0" class="btn-clear" @click="clearAll">清空历史</button>
    </div>

    <div v-if="loading" class="skeleton-list">
      <div v-for="n in 5" :key="n" class="skeleton-item">
        <div class="skeleton-img"></div>
        <div class="skeleton-lines">
          <div class="skeleton-line"></div>
          <div class="skeleton-line short"></div>
        </div>
      </div>
    </div>

    <div v-else-if="list.length === 0" class="empty">
      <p>暂无浏览记录</p>
      <button class="btn-link" @click="router.push('/scenic')">去发现景点</button>
    </div>

    <div v-else class="list">
      <div
        v-for="item in list"
        :key="item.id"
        class="list-item"
        @click="router.push(`/scenic/${item.scenicId}`)"
      >
        <img :src="item.coverImage || ''" :alt="item.scenicName" class="item-img" />
        <div class="item-info">
          <h3 class="item-name">{{ item.scenicName }}</h3>
          <p class="item-meta">
            <span v-if="item.regionName">{{ item.regionName }}</span>
            <span v-if="item.stayDuration">停留 {{ item.stayDuration }}秒</span>
          </p>
          <p class="item-time">{{ item.visitTime }}</p>
        </div>
        <button class="btn-delete" @click.stop="deleteItem(item.id)">删除</button>
      </div>
    </div>

    <div v-if="total > 0" class="pagination">
      <button class="page-btn" :disabled="pageNum <= 1" @click="changePage(pageNum - 1)">
        上一页
      </button>
      <span class="page-info">{{ pageNum }} / {{ Math.ceil(total / pageSize) }}</span>
      <button
        class="page-btn"
        :disabled="pageNum >= Math.ceil(total / pageSize)"
        @click="changePage(pageNum + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  getBrowseHistoryPage as getBrowseHistory,
  deleteBrowseHistory,
  clearBrowseHistory,
} from "@/api/history";

const router = useRouter();
const loading = ref(false);
const list = ref<any[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getBrowseHistory({ pageNum: pageNum.value, pageSize: pageSize.value });
    list.value = res.records || [];
    total.value = res.total || 0;
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

async function deleteItem(id: number): Promise<void> {
  if (!confirm("确定删除该条记录？")) return;
  try {
    await deleteBrowseHistory(id);
    loadData();
  } catch {
    alert("删除失败");
  }
}

async function clearAll(): Promise<void> {
  if (!confirm("确定清空所有浏览历史？")) return;
  try {
    await clearBrowseHistory();
    list.value = [];
    total.value = 0;
  } catch {
    alert("清空失败");
  }
}

function changePage(page: number): void {
  pageNum.value = page;
  loadData();
}

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 24px;
}

.header {
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

.btn-clear {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #ff5252;
  background: transparent;
  border: 1px solid #ff5252;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-clear:hover {
  background: #ff5252;
  color: #ffffff;
}

/* List */
.list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.list-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  align-items: center;
}

.list-item:hover {
  border-color: #00e676;
  box-shadow: 0 2px 8px rgba(0, 230, 118, 0.1);
}

.item-img {
  width: 100px;
  height: 70px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
  background: #f5f5f5;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  font-size: 13px;
  color: #999999;
  margin: 0 0 4px 0;
  display: flex;
  gap: 12px;
}

.item-time {
  font-size: 12px;
  color: #bbbbbb;
  margin: 0;
}

.btn-delete {
  padding: 6px 14px;
  font-size: 13px;
  color: #999999;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-delete:hover {
  color: #ff5252;
  border-color: #ff5252;
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
  padding: 8px 16px;
  font-size: 14px;
  color: #000000;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
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

/* Empty */
.empty {
  text-align: center;
  padding: 80px 20px;
  color: #999999;
}

.empty p {
  font-size: 16px;
  margin: 0 0 16px 0;
}

.btn-link {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.btn-link:hover {
  background: #00c665;
}

/* Skeleton */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-item {
  display: flex;
  gap: 16px;
  padding: 16px;
}

.skeleton-img {
  width: 100px;
  height: 70px;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 8px;
  flex-shrink: 0;
}

.skeleton-lines {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.skeleton-line {
  height: 16px;
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
