<template>
  <div>
    <div class="breadcrumb-container">
      <el-breadcrumb separator=">">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/scenic' }">景点玩乐</el-breadcrumb-item>
        <el-breadcrumb-item>{{ detail.regionName || "地区" }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ detail.name }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header-section">
      <div class="header-left">
        <h1 class="scenic-title">{{ detail.name }}</h1>
        <div class="scenic-rating">
          <div class="rating-dots">
            <span
              v-for="i in 5"
              :key="i"
              class="dot"
              :class="{ filled: i <= Math.round(detail.score || 0) }"
            ></span>
          </div>
          <span class="rating-score">{{ Number(detail.score || 0).toFixed(1) }} 分</span>
          <span v-if="detail.reviewCount" class="review-count" @click="emit('scroll-reviews')">
            ({{ detail.reviewCount }} 条点评)
          </span>
          <span v-if="detail.regionName" class="rank-text">
            在 {{ detail.regionName }} 的相关分类中排名前列
          </span>
          <span class="metric-item" title="浏览次数">
            <el-icon><View /></el-icon>
            {{ detail.viewCount || 0 }}
          </span>
          <span class="metric-item" title="收藏次数">
            <el-icon><Star /></el-icon>
            {{ detail.favoriteCount || 0 }}
          </span>
        </div>
      </div>
      <div class="header-right">
        <el-button round class="action-btn" @click="emit('toggle-favorite')">
          <el-icon :class="{ 'red-star': detail.isFavorite }"><Star /></el-icon>
          {{ detail.isFavorite ? "已保存" : "保存" }}
        </el-button>
        <el-button round class="action-btn" @click="emit('write-review')">
          <el-icon><EditPen /></el-icon>
          点评
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ScenicDetailModel } from "@/types/scenic-detail";
import { Star, EditPen, View } from "@element-plus/icons-vue";

interface Props {
  detail: ScenicDetailModel;
}

defineProps<Props>();
const emit = defineEmits<{
  "scroll-reviews": [];
  "toggle-favorite": [];
  "write-review": [];
}>();
</script>

<style scoped>
.breadcrumb-container {
  margin-bottom: 24px;
  font-size: 13px;
}

.breadcrumb-container :deep(.el-breadcrumb__inner) {
  color: #666;
  font-weight: 400;
}

.breadcrumb-container :deep(.el-breadcrumb__inner:hover) {
  color: #000;
  text-decoration: underline;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left {
  flex: 1;
}

.scenic-title {
  font-size: 36px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  line-height: 1.2;
}

.scenic-rating {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 14px;
  color: #333;
}

.rating-dots {
  display: flex;
  gap: 4px;
}

.dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid #34e0a1;
  background: #fff;
}

.dot.filled {
  background: #34e0a1;
  border-color: #34e0a1;
}

.rating-score {
  font-weight: 700;
}

.review-count {
  text-decoration: underline;
  cursor: pointer;
  color: #1a1a1a;
}

.rank-text {
  color: #333;
}

.metric-item {
  display: flex;
  align-items: center;
  color: #666;
  gap: 4px;
}

.header-right {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}

.action-btn {
  border: 1px solid #1a1a1a;
  color: #1a1a1a;
  font-weight: 700;
  height: 40px;
  padding: 0 20px;
}

.red-star {
  color: #ff4d4f;
}
</style>
