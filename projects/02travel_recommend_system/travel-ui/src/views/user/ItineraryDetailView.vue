<template>
  <div class="detail-page">
    <section class="detail-hero">
      <button class="back-btn" @click="router.back()">← 返回</button>
      <div class="hero-row">
        <div>
          <p class="eyebrow">Itinerary</p>
          <h1>{{ detail?.title || "行程详情" }}</h1>
          <p class="hero-desc">{{ detail?.description || "暂无描述" }}</p>
        </div>
        <div class="hero-actions">
          <button class="btn" @click="openEditPlan">编辑行程</button>
          <button class="btn btn-danger" @click="handleDeletePlan">删除行程</button>
        </div>
      </div>

      <div class="meta-grid">
        <div class="meta-item">
          <span>日期</span>
          <strong>{{ formatDateRange(detail?.startDate, detail?.endDate) }}</strong>
        </div>
        <div class="meta-item">
          <span>天数</span>
          <strong>{{ detail?.totalDays || 1 }} 天</strong>
        </div>
        <div class="meta-item">
          <span>预算</span>
          <strong>{{ detail?.estimatedBudget ? `¥${detail.estimatedBudget}` : "未设置" }}</strong>
        </div>
        <div class="meta-item">
          <span>状态</span>
          <strong>{{ findDictDesc(statusOptions, detail?.status, "未设置") }}</strong>
        </div>
      </div>
    </section>

    <div v-if="loading" class="loading-card">加载中...</div>

    <section v-else-if="!detail" class="empty-state">
      <div class="empty-state-icon">🧳</div>
      <div class="empty-state-text">行程不存在或已被删除</div>
    </section>

    <section v-else class="days-shell">
      <aside class="day-nav">
        <button
          v-for="day in detail.totalDays || 1"
          :key="day"
          :class="['day-nav-btn', { active: activeDay === String(day) }]"
          @click="activeDay = String(day)"
        >
          Day {{ day }}
        </button>
      </aside>

      <div class="day-panel">
        <div class="day-panel-header">
          <div>
            <p class="eyebrow">Day {{ activeDay }}</p>
            <h2>第 {{ activeDay }} 天行程</h2>
          </div>
          <button class="btn btn-primary" @click="openAddItem(Number(activeDay))">
            添加行程项
          </button>
        </div>

        <div v-if="getDayItems(Number(activeDay)).length === 0" class="empty-day">
          <p>这一天还没有安排。</p>
          <button class="btn" @click="openAddItem(Number(activeDay))">添加第一个项目</button>
        </div>

        <div v-else class="timeline">
          <article
            v-for="item in getDayItems(Number(activeDay))"
            :key="item.id ?? `${item.dayNo}-${item.title}`"
            class="timeline-item"
          >
            <div class="time-column">
              <span>{{ item.startTime || "--:--" }}</span>
              <small v-if="item.endTime">{{ item.endTime }}</small>
            </div>
            <div class="item-card">
              <div class="item-card-header">
                <div>
                  <span class="pill">
                    {{ findDictDesc(itemTypeOptions, item.itemType, "自定义") }}
                  </span>
                  <h3>{{ item.title }}</h3>
                </div>
                <div class="item-actions">
                  <button class="link-btn" @click="openEditItem(item)">编辑</button>
                  <button
                    class="link-btn danger"
                    :disabled="item.id == null"
                    @click="item.id != null && handleDeleteItem(item.id)"
                  >
                    删除
                  </button>
                </div>
              </div>
              <p v-if="item.description" class="item-desc">{{ item.description }}</p>
              <div class="item-meta">
                <span v-if="item.location">📍 {{ item.location }}</span>
                <span v-if="item.estimatedCost">¥{{ item.estimatedCost }}</span>
                <span v-if="item.scenicSpotId">景点 ID {{ item.scenicSpotId }}</span>
              </div>
              <p v-if="item.notes" class="notes">备注：{{ item.notes }}</p>
            </div>
          </article>
        </div>
      </div>
    </section>

    <ItineraryEditDialog
      v-model:visible="planDialogVisible"
      dialog-type="edit"
      :form="planForm"
      :submit-loading="planSubmitLoading"
      @submit="submitPlanForm"
    />

    <ItineraryItemDialog
      v-model:visible="itemDialogVisible"
      :mode="itemDialogMode"
      :total-days="detail?.totalDays || 1"
      :form="itemForm"
      :submit-loading="submitLoading"
      @submit="submitItem"
    />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { getTravelPlanItemTypeDict, getTravelPlanStatusDict } from "@/api/dict";
import { findDictDesc, useDictOptions } from "@/composables/useDictOptions";
import { useItineraryDetail } from "@/composables/useItineraryDetail";
import ItineraryEditDialog from "@/views/user/components/ItineraryEditDialog.vue";
import ItineraryItemDialog from "@/views/user/components/ItineraryItemDialog.vue";

const router = useRouter();
const { options: statusOptions } = useDictOptions("travel-plan-status", getTravelPlanStatusDict);
const { options: itemTypeOptions } = useDictOptions(
  "travel-plan-item-type",
  getTravelPlanItemTypeDict
);

const {
  loading,
  detail,
  activeDay,
  planDialogVisible,
  planSubmitLoading,
  planForm,
  itemDialogVisible,
  itemDialogMode,
  submitLoading,
  itemForm,
  getDayItems,
  openEditPlan,
  submitPlanForm,
  handleDeletePlan,
  openAddItem,
  openEditItem,
  submitItem,
  handleDeleteItem,
} = useItineraryDetail();

function formatDateRange(start?: string, end?: string): string {
  if (!start && !end) return "日期未设置";
  if (!end || start === end) return start || end || "日期未设置";
  return `${start} ~ ${end}`;
}
</script>

<style scoped>
.detail-page {
  max-width: 1040px;
  padding: 40px 24px 64px;
  margin: 0 auto;
}

.detail-hero,
.days-shell,
.loading-card,
.item-card {
  border: 1px solid var(--color-gray-200);
  border-radius: 18px;
}

.detail-hero {
  padding: 30px;
  margin-bottom: 18px;
  background: linear-gradient(135deg, #ffffff 0%, #f7fff9 100%);
}

.back-btn,
.link-btn {
  padding: 0;
  font: inherit;
  color: var(--color-gray-700);
  cursor: pointer;
  background: transparent;
  border: 0;
}

.back-btn {
  margin-bottom: 18px;
}

.back-btn:hover,
.link-btn:hover {
  color: var(--color-primary-dark);
}

.hero-row,
.day-panel-header,
.item-card-header,
.item-actions,
.item-meta {
  display: flex;
  gap: 16px;
  align-items: center;
}

.hero-row,
.day-panel-header,
.item-card-header {
  justify-content: space-between;
}

.hero-actions,
.item-actions {
  flex-shrink: 0;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 700;
  color: var(--color-primary-dark);
  text-transform: uppercase;
  letter-spacing: 0.16em;
}

h1,
h2,
h3 {
  margin: 0;
  color: var(--color-black);
}

h1 {
  font-size: 34px;
}

.hero-desc {
  max-width: 680px;
  margin: 12px 0 0;
  color: var(--color-gray-600);
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 28px;
}

.meta-item {
  padding: 14px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--color-gray-200);
  border-radius: 14px;
}

.meta-item span {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--color-gray-500);
}

.meta-item strong {
  font-size: 15px;
}

.days-shell {
  display: grid;
  grid-template-columns: 150px 1fr;
  overflow: hidden;
  background: var(--color-white);
}

.day-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  background: var(--color-gray-100);
  border-right: 1px solid var(--color-gray-200);
}

.day-nav-btn {
  padding: 12px 14px;
  font-weight: 600;
  color: var(--color-gray-700);
  text-align: left;
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 12px;
}

.day-nav-btn.active,
.day-nav-btn:hover {
  color: var(--color-black);
  background: var(--color-white);
}

.day-panel {
  min-width: 0;
  padding: 24px;
}

.empty-day,
.loading-card {
  padding: 56px 20px;
  color: var(--color-gray-500);
  text-align: center;
}

.empty-day .btn {
  margin-top: 12px;
}

.timeline {
  display: grid;
  gap: 14px;
  margin-top: 24px;
}

.timeline-item {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 16px;
}

.time-column {
  padding-top: 16px;
  color: var(--color-gray-500);
}

.time-column span,
.time-column small {
  display: block;
}

.time-column span {
  font-weight: 700;
  color: var(--color-black);
}

.item-card {
  padding: 18px;
  background: var(--color-white);
}

.pill {
  display: inline-flex;
  padding: 3px 9px;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--color-primary-dark);
  background: var(--color-primary-light);
  border-radius: var(--radius-full);
}

.item-desc {
  margin: 12px 0;
  line-height: 1.7;
  color: var(--color-gray-700);
}

.item-meta {
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
  color: var(--color-gray-500);
}

.notes {
  padding: 10px 12px;
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--color-gray-600);
  background: var(--color-gray-100);
  border-radius: 12px;
}

.link-btn.danger:hover {
  color: var(--color-danger);
}

.link-btn:disabled {
  color: var(--color-gray-400);
  cursor: not-allowed;
}

@media (max-width: 780px) {
  .hero-row,
  .day-panel-header,
  .item-card-header {
    flex-direction: column;
    align-items: stretch;
  }

  .meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .days-shell {
    grid-template-columns: 1fr;
  }

  .day-nav {
    flex-direction: row;
    overflow-x: auto;
    border-right: 0;
    border-bottom: 1px solid var(--color-gray-200);
  }

  .timeline-item {
    grid-template-columns: 1fr;
  }
}
</style>
