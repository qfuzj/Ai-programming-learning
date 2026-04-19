<template>
  <div class="result-panel">
    <el-alert
      v-if="errorMessage"
      type="warning"
      show-icon
      :closable="false"
      :title="errorMessage"
      class="result-alert"
    />

    <el-empty v-else-if="!loading && !result" description="生成后将在此处展示 AI 行程草案" />

    <el-skeleton v-else-if="loading" animated :rows="6" />

    <el-card v-else shadow="never" class="result-card">
      <template #header>
        <div class="result-header">
          <div>
            <div class="result-title">{{ result?.title || "AI 草案结果" }}</div>
            <div class="result-summary">
              {{ result?.description || "可先预览，再保存为正式行程" }}
            </div>
          </div>
          <el-button
            type="primary"
            :loading="saveLoading"
            :disabled="!canSave"
            @click="emit('save')"
          >
            保存为正式行程
          </el-button>
        </div>
      </template>

      <div class="overview-grid">
        <div class="overview-item">
          <span class="label">总天数</span>
          <strong>{{ result?.totalDays || 0 }} 天</strong>
        </div>
        <div class="overview-item">
          <span class="label">预算</span>
          <strong>{{ formatBudget(result?.estimatedBudget) }}</strong>
        </div>
        <div class="overview-item">
          <span class="label">同伴</span>
          <strong>{{ result?.travelCompanion || "未指定" }}</strong>
        </div>
        <div class="overview-item">
          <span class="label">草案状态</span>
          <strong>{{ formatStatus(result?.status) }}</strong>
        </div>
      </div>

      <div class="day-list">
        <section v-for="day in result?.days || []" :key="day.dayNo" class="day-card">
          <div class="day-header">
            <h3>Day {{ day.dayNo }}</h3>
            <span>{{ day.items?.length || 0 }} 项安排</span>
          </div>

          <el-empty
            v-if="!day.items || day.items.length === 0"
            description="当天暂无可展示的行程项"
            :image-size="56"
          />

          <div v-else class="item-list">
            <article
              v-for="(item, index) in day.items"
              :key="`${day.dayNo}-${index}-${item.title}`"
              class="item-card"
            >
              <div class="item-time">
                <span>{{ formatTime(item.startTime) || "--:--" }}</span>
                <em>{{ formatTime(item.endTime) || "--:--" }}</em>
              </div>
              <div class="item-content">
                <div class="item-top">
                  <h4>{{ item.title }}</h4>
                  <el-tag size="small" type="success">{{ formatItemType(item.itemType) }}</el-tag>
                </div>
                <p v-if="item.description" class="item-desc">{{ item.description }}</p>
                <div class="item-meta">
                  <span v-if="item.location">地点：{{ item.location }}</span>
                  <span v-if="item.estimatedCost != null">费用：¥{{ item.estimatedCost }}</span>
                  <span v-if="item.notes">备注：{{ item.notes }}</span>
                </div>
              </div>
            </article>
          </div>
        </section>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { ItineraryItem } from "@/api/itinerary";

interface Props {
  loading: boolean;
  saveLoading: boolean;
  canSave: boolean;
  errorMessage: string;
  result: ItineraryItem | null;
}

defineProps<Props>();

const emit = defineEmits<{
  save: [];
}>();

function formatTime(value?: string): string {
  if (!value) {
    return "";
  }
  return value.slice(0, 5);
}

function formatItemType(itemType?: number): string {
  if (itemType === 1) return "景点";
  if (itemType === 2) return "餐饮";
  if (itemType === 3) return "住宿";
  if (itemType === 4) return "交通";
  return "自定义";
}

function formatBudget(value?: number): string {
  return typeof value === "number" ? `¥${value}` : "未指定";
}

function formatStatus(status?: number): string {
  if (status === 1) return "正常";
  if (status === 2) return "已完成";
  return "草稿";
}
</script>

<style scoped>
.result-panel {
  margin-top: 20px;
}

.result-alert {
  margin-bottom: 12px;
}

.result-card {
  border: 1px solid #ebeef5;
}

.result-header {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  justify-content: space-between;
}

.result-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.result-summary {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.7;
  color: #6b7280;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.overview-item {
  padding: 12px 14px;
  background: #f8fafc;
  border: 1px solid #e5edf5;
  border-radius: 12px;
}

.overview-item .label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: #6b7280;
}

.overview-item strong {
  font-size: 16px;
  color: #111827;
}

.day-list {
  display: grid;
  gap: 16px;
}

.day-card {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
}

.day-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.day-header h3 {
  margin: 0;
  font-size: 18px;
  color: #111827;
}

.day-header span {
  font-size: 12px;
  color: #6b7280;
}

.item-list {
  display: grid;
  gap: 12px;
}

.item-card {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  gap: 14px;
  padding: 12px;
  background: #fbfdff;
  border: 1px solid #ebeff5;
  border-radius: 12px;
}

.item-time {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #4b5563;
}

.item-time span {
  font-weight: 700;
  color: #111827;
}

.item-time em {
  font-style: normal;
}

.item-content {
  min-width: 0;
}

.item-top {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
}

.item-top h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.item-desc {
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.7;
  color: #4b5563;
}

.item-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #6b7280;
}

@media (max-width: 900px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .result-header {
    flex-direction: column;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }

  .item-card {
    grid-template-columns: 1fr;
  }
}
</style>
