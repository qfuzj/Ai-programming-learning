<template>
  <div class="my-reviews-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h2 v-if="isWriting">评价体验</h2>
          <h2 v-else>我的点评</h2>
          <el-button v-if="isWriting" @click="cancelWrite">返回点评列表</el-button>
        </div>
      </template>

      <ReviewWriteForm
        v-if="isWriting"
        :form="reviewForm"
        :loading="submitLoading"
        @submit="submitMyReview"
      />

      <ReviewListSection
        v-else
        :reviews="reviews"
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        :loading="listLoading"
        @delete="handleDelete"
        @page-change="handlePageChange"
        @go-scenic="goScenic"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useMyReviews } from "@/composables/useMyReviews";
import ReviewListSection from "./components/ReviewListSection.vue";
import ReviewWriteForm from "./components/ReviewWriteForm.vue";

const {
  isWriting,
  submitLoading,
  listLoading,
  reviewForm,
  reviews,
  total,
  currentPage,
  pageSize,
  submitMyReview,
  handleDelete,
  handlePageChange,
  goScenic,
  cancelWrite,
} = useMyReviews();
</script>

<style scoped>
.my-reviews-container {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 20px;
}

.box-card {
  border-radius: 12px;
  border: 1px solid #eaeaea;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #1a1a1a;
}
</style>
