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
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
  border: 1px solid #eaeaea;
  border-radius: 12px;
  transition: all 0.3s ease;
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
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  font-size: 24px;
  font-weight: 800;
  color: #000;
  background: rgba(255, 255, 255, 0.9);
  border-top-right-radius: 12px;
  backdrop-filter: blur(4px);
}

.favorite-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: #1a1a1a;
  cursor: pointer;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.2s ease;
}

.favorite-btn:hover {
  color: #ff4d4f;
  transform: scale(1.1);
}

.favorite-btn.active {
  color: #ff4d4f;
  background: #fff5f5;
}

.card-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 16px 20px;
}

.card-location {
  margin-bottom: 6px;
  font-size: 13px;
  color: #666;
}

.card-title {
  display: -webkit-box;
  margin: 0 0 10px 0;
  overflow: hidden;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.4;
  color: #1a1a1a;
  -webkit-box-orient: vertical;
}

.card-rating {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}

.rating-dots {
  display: flex;
  gap: 3px;
}

.dot {
  width: 12px;
  height: 12px;
  background: #fff;
  border: 1px solid #34e0a1;
  border-radius: 50%;
}

.dot.filled {
  background: #34e0a1;
}

.review-count {
  font-size: 13px;
  font-weight: 500;
  color: #666;
}

.card-meta {
  display: flex;
  flex: 1;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.card-footer {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.price {
  display: flex;
  gap: 4px;
  align-items: baseline;
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
