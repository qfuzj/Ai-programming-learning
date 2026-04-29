<!-- 极简风格行程详情页 -->
<template>
  <div v-if="detail" class="page">
    <div class="header">
      <button class="btn-back" @click="router.back()">← 返回</button>
      <h1 class="title">{{ detail.title || "未命名行程" }}</h1>
      <div class="meta">
        <span v-if="detail.destinationRegionName">{{ detail.destinationRegionName }}</span>
        <span>{{ detail.startDate }} ~ {{ detail.endDate }}</span>
        <span v-if="detail.totalDays">{{ detail.totalDays }}天</span>
        <span v-if="detail.estimatedBudget">预算 ¥{{ detail.estimatedBudget }}</span>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="days.length === 0" class="empty">
      <p>暂无行程安排</p>
    </div>

    <div v-else class="days">
      <div v-for="day in days" :key="day.dayNo" class="day-card">
        <h2 class="day-title">第 {{ day.dayNo }} 天</h2>
        <div v-if="day.items && day.items.length > 0" class="item-list">
          <div v-for="item in day.items" :key="item.id" class="item">
            <div class="item-time">{{ item.startTime || "--:--" }}</div>
            <div class="item-content">
              <strong>{{ item.title }}</strong>
              <p v-if="item.description">{{ item.description }}</p>
              <span v-if="item.location" class="item-loc">{{ item.location }}</span>
            </div>
          </div>
        </div>
        <div v-else class="day-empty">暂无安排</div>
      </div>
    </div>
  </div>
  <div v-else class="empty-page">行程不存在</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getItineraryDetail } from "@/api/itinerary";

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const detail = ref<any>(null);
const days = ref<any[]>([]);

async function loadData(): Promise<void> {
  const id = Number(route.params.id);
  if (!id) return;
  loading.value = true;
  try {
    const res = await getItineraryDetail(id);
    detail.value = res;
    days.value = res.days || [];
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
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
  margin-bottom: 40px;
}

.btn-back {
  padding: 6px 12px;
  font-size: 14px;
  color: #666666;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 16px;
}

.btn-back:hover {
  border-color: #000000;
  color: #000000;
}

.title {
  font-size: 32px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 12px 0;
}

.meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #999999;
  flex-wrap: wrap;
}

/* Days */
.days {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.day-card {
  background: #f9f9f9;
  border-radius: 12px;
  padding: 24px;
}

.day-title {
  font-size: 20px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 20px 0;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.item-time {
  font-size: 13px;
  color: #999999;
  flex-shrink: 0;
  min-width: 50px;
  padding-top: 2px;
}

.item-content strong {
  font-size: 15px;
  font-weight: 600;
  color: #000000;
}

.item-content p {
  font-size: 14px;
  color: #666666;
  margin: 6px 0 0 0;
  line-height: 1.6;
}

.item-loc {
  font-size: 12px;
  color: #999999;
  display: block;
  margin-top: 6px;
}

.day-empty {
  font-size: 14px;
  color: #999999;
  padding: 20px 0;
  text-align: center;
}

.loading,
.empty,
.empty-page {
  text-align: center;
  padding: 80px 20px;
  color: #999999;
  font-size: 15px;
}
</style>
