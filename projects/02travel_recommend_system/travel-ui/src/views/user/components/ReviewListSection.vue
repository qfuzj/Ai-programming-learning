<template>
  <div v-loading="loading" class="reviews-list-section">
    <template v-if="reviews.length > 0">
      <div v-for="item in reviews" :key="item.id" class="review-item">
        <div class="review-header">
          <div class="scenic-info">
            <h4 @click="emit('go-scenic', item.scenicId)">{{ item.scenicName || "未知景点" }}</h4>
            <el-rate :model-value="item.score" disabled text-color="#ff9900" />
          </div>
          <div class="review-meta">
            <span class="status-tag" :class="getStatusClass(item.status)">
              {{ getStatusText(item.status) }}
            </span>
            <span
              v-if="isRejected(item.status) && (item.rejectReason || item.auditRemark)"
              class="reject-reason"
            >
              拒绝原因：{{ item.rejectReason || item.auditRemark }}
            </span>
            <span class="time">{{ formatTime(item.createdAt) }}</span>
          </div>
        </div>
        <div class="review-content">
          {{ item.content }}
        </div>
        <div class="review-actions">
          <el-button type="danger" link @click="emit('delete', item.id)">删除点评</el-button>
        </div>
      </div>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          :current-page="currentPage"
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          @current-change="(page) => emit('page-change', page)"
        />
      </div>
    </template>
    <el-empty v-else description="暂无点评记录" />
  </div>
</template>

<script setup lang="ts">
import type { ReviewItem } from "@/api/audit";

interface Props {
  reviews: ReviewItem[];
  total: number;
  currentPage: number;
  pageSize: number;
  loading: boolean;
}

defineProps<Props>();

const emit = defineEmits<{
  delete: [id: number];
  "page-change": [page: number];
  "go-scenic": [id: number | undefined];
}>();

function formatTime(timeStr?: string): string {
  if (!timeStr) return "";
  return timeStr.replace("T", " ").substring(0, 16);
}

function getStatusText(status: number | undefined): string {
  const map: Record<number, string> = {
    0: "待审核",
    1: "已通过",
    2: "未通过",
    3: "已隐藏",
  };
  return status !== undefined ? map[status] || "未知状态" : "未知状态";
}

function getStatusClass(status: number | undefined): string {
  const map: Record<number, string> = {
    0: "status-pending",
    1: "status-approved",
    2: "status-rejected",
    3: "status-hidden",
  };
  return status !== undefined ? map[status] || "" : "";
}

function isRejected(status: number | undefined): boolean {
  return status === 2;
}
</script>

<style scoped>
.review-item {
  padding: 24px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.scenic-info h4 {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  color: #1a1a1a;
}

.scenic-info h4:hover {
  text-decoration: underline;
  color: #000;
}

.review-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
}

.status-pending {
  background-color: #fff8e6;
  color: #f59e0b;
}

.status-approved {
  background-color: #ecfdf5;
  color: #10b981;
}

.status-rejected {
  background-color: #fef2f2;
  color: #ef4444;
}

.status-hidden {
  background-color: #f3f4f6;
  color: #6b7280;
}

.time {
  font-size: 13px;
  color: #6b7280;
}

.reject-reason {
  max-width: 320px;
  font-size: 12px;
  color: #ef4444;
  line-height: 1.4;
  text-align: right;
  word-break: break-word;
}

.review-content {
  font-size: 15px;
  color: #374151;
  line-height: 1.6;
  white-space: pre-wrap;
  margin-bottom: 16px;
}

.review-actions {
  display: flex;
  justify-content: flex-end;
}

.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
}
</style>
