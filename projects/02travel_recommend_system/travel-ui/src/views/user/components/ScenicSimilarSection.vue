<template>
  <section v-if="items.length > 0" class="recommend-experiences">
    <h2>{{ title }}</h2>
    <div class="experience-grid">
      <div
        v-for="(exp, idx) in items"
        :key="idx"
        class="exp-card"
        @click="emit('select', exp.scenicId)"
      >
        <div
          class="exp-image"
          :style="{
            backgroundImage: `url(${exp.coverImage || 'https://via.placeholder.com/400x300?text=No+Image'})`,
          }"
        ></div>
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
</template>

<script setup lang="ts">
import type { ScenicRecommendItem } from "@/types/scenic-detail";

interface Props {
  title: string;
  items: ScenicRecommendItem[];
}

defineProps<Props>();
const emit = defineEmits<{
  select: [id: number];
}>();
</script>

<style scoped>
.recommend-experiences {
  margin-top: 40px;
}

.recommend-experiences h2 {
  margin: 0 0 24px 0;
  font-size: 22px;
  font-weight: 800;
}

.experience-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.exp-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.exp-card:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.exp-image {
  height: 140px;
  background-position: center;
  background-size: cover;
}

.exp-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 16px;
}

.exp-content h4 {
  flex: 1;
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.4;
}

.exp-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #666;
}

.exp-reason {
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  color: #888;
  -webkit-box-orient: vertical;
}

.book-btn {
  width: 100%;
  font-weight: 700;
  color: #fff;
  background-color: #34e0a1;
  border-color: #34e0a1;
}

@media (max-width: 992px) {
  .experience-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .experience-grid {
    grid-template-columns: 1fr;
  }
}
</style>
