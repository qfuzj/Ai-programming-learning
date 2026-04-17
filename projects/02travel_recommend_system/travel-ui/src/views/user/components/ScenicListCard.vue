<template>
  <div class="scenic-card" @click="emit('detail', item)">
    <div class="card-image">
      <img
        :src="item.coverImage || 'https://via.placeholder.com/400x300?text=No+Image'"
        :alt="item.name"
      />
      <div class="card-rank">{{ rank }}</div>
      <div
        class="favorite-btn"
        :class="{ active: item.isFavorite }"
        @click.stop.prevent="emit('toggle-favorite', item)"
      >
        <el-icon><Star /></el-icon>
      </div>
    </div>

    <div class="card-content">
      <div class="card-location">{{ item.regionName || "未知地区" }}</div>
      <h3 class="card-title">{{ item.name }}</h3>

      <div class="card-rating">
        <div class="rating-dots">
          <span
            v-for="i in 5"
            :key="i"
            class="dot"
            :class="{ filled: i <= Math.round(item.score || 0) }"
          ></span>
        </div>
        <span class="review-count">
          {{ item.score ? item.score.toFixed(1) + " 分" : "暂无评分" }}
        </span>
      </div>

      <div class="card-meta">
        <span class="location-text">{{ item.level ? item.level + "景区" : "普通景点" }}</span>
      </div>

      <div class="card-footer">
        <div class="price">
          <span class="price-prefix">低至</span>
          <span class="price-val">
            {{
              item.ticketPrice != null && Number(item.ticketPrice) > 0
                ? `¥${item.ticketPrice}`
                : "免费"
            }}
          </span>
        </div>
        <span class="details-link">了解详情</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ScenicItem } from "@/api/scenic";
import { Star } from "@element-plus/icons-vue";

interface Props {
  item: ScenicItem;
  rank: number;
}

defineProps<Props>();
const emit = defineEmits<{
  detail: [item: ScenicItem];
  "toggle-favorite": [item: ScenicItem];
}>();
</script>

<style scoped>
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
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
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
  background: rgba(255, 255, 255, 0.9);
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
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
  line-clamp: 2;
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
</style>
