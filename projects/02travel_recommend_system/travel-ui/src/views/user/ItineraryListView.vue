<!-- 极简风格行程列表页 -->
<template>
  <div class="page">
    <div class="header">
      <h1 class="title">我的行程</h1>
      <div class="header-actions">
        <button class="btn-ai" @click="router.push('/itinerary/ai-generate')">AI 生成</button>
        <button class="btn-new" @click="router.push('/itinerary/create')">新建行程</button>
      </div>
    </div>

    <div v-if="loading" class="skeleton-list">
      <div v-for="n in 3" :key="n" class="skeleton-item">
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
      </div>
    </div>

    <div v-else-if="list.length === 0" class="empty">
      <p>暂无行程</p>
      <button class="btn-link" @click="router.push('/itinerary/create')">创建第一个行程</button>
    </div>

    <div v-else class="list">
      <div
        v-for="item in list"
        :key="item.id"
        class="list-item"
        @click="router.push(`/itinerary/${item.id}`)"
      >
        <div class="item-main">
          <h3 class="item-title">{{ item.title || "未命名行程" }}</h3>
          <p class="item-meta">
            <span v-if="item.destinationRegionName">{{ item.destinationRegionName }}</span>
            <span v-if="item.totalDays">{{ item.totalDays }}天</span>
            <span v-if="item.estimatedBudget">预算 ¥{{ item.estimatedBudget }}</span>
          </p>
          <p class="item-time">{{ item.startDate }} ~ {{ item.endDate }}</p>
        </div>
        <div class="item-actions" @click.stop>
          <button class="btn-edit" @click="router.push(`/itinerary/${item.id}`)">查看</button>
          <button class="btn-delete" @click="deleteItem(item.id)">删除</button>
        </div>
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
import { getItineraryPage, deleteItinerary } from "@/api/itinerary";

const router = useRouter();
const loading = ref(false);
const list = ref<any[]>([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getItineraryPage({ pageNum: pageNum.value, pageSize: pageSize.value });
    list.value = res.records || [];
    total.value = res.total || 0;
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

async function deleteItem(id: number): Promise<void> {
  if (!confirm("确定删除该行程？")) return;
  try {
    await deleteItinerary(id);
    loadData();
  } catch {
    alert("删除失败");
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

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-ai {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-ai:hover {
  background: #00c665;
}

.btn-new {
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

.btn-new:hover {
  border-color: #000000;
}

/* List */
.list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.list-item:hover {
  border-color: #00e676;
  box-shadow: 0 2px 8px rgba(0, 230, 118, 0.1);
}

.item-main {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 17px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 8px 0;
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

.item-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 16px;
}

.btn-edit {
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-edit:hover {
  background: #00c665;
}

.btn-delete {
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 500;
  color: #999999;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
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
  padding: 20px;
  border-radius: 12px;
}

.skeleton-line {
  height: 16px;
  margin-bottom: 12px;
  background: linear-gradient(90deg, #f5f5f5 25%, #eeeeee 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-line.short {
  width: 60%;
  margin-bottom: 0;
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
