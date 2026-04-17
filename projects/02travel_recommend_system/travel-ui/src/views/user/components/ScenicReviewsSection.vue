<template>
  <section :id="sectionId" class="reviews-section">
    <div class="reviews-header">
      <h2>访客点评</h2>
      <el-button round class="write-review-btn" @click="emit('write-review')">写点评</el-button>
    </div>

    <div v-loading="loading" class="reviews-list">
      <template v-if="reviews.length > 0">
        <ReviewItemCard
          v-for="(review, index) in reviews"
          :key="review.id"
          :review="review"
          @update-review="(updated) => emit('update-review', { index, review: updated })"
        />

        <div v-if="total > 0" class="pagination-container">
          <el-pagination
            :current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="(v) => emit('update:pageNum', v)"
          />
        </div>
      </template>
      <el-empty v-else description="暂无点评，快来发布第一条！" />
    </div>
  </section>
</template>

<script setup lang="ts">
import type { ScenicReviewItem } from "@/types/scenic-detail";
import ReviewItemCard from "./ReviewItemCard.vue";

interface Props {
  sectionId?: string;
  loading: boolean;
  reviews: ScenicReviewItem[];
  total: number;
  pageNum: number;
  pageSize: number;
}

withDefaults(defineProps<Props>(), {
  sectionId: "reviews-section",
});

const emit = defineEmits<{
  "write-review": [];
  "update-review": [{ index: number; review: ScenicReviewItem }];
  "update:pageNum": [value: number];
}>();
</script>

<style scoped>
.reviews-section {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid #eaeaea;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.reviews-header h2 {
  font-size: 24px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0;
}

.write-review-btn {
  border: 1px solid #1a1a1a;
  color: #1a1a1a;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
