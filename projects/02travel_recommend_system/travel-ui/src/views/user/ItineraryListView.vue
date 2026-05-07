<template>
  <div class="itinerary-page">
    <section class="page-hero">
      <div>
        <p class="eyebrow">My Trips</p>
        <h1>我的行程</h1>
        <p class="hero-desc">管理已创建的旅行计划，支持查看详情、编辑基础信息和删除行程。</p>
      </div>
      <div class="hero-actions">
        <button class="btn ai-generate-btn" @click="handleAiGenerate">✨ AI 生成</button>
        <button class="btn btn-primary" @click="handleCreate">新建行程</button>
      </div>
    </section>

    <section class="filter-card">
      <div class="field grow">
        <label>关键词</label>
        <input
          v-model="queryParams.keyword"
          class="input"
          placeholder="搜索标题或描述"
          @keyup.enter="fetchList"
        />
      </div>
      <div class="field">
        <label>状态</label>
        <select v-model="queryParams.status" class="input" @change="fetchList">
          <option :value="undefined">全部</option>
          <option v-for="item in statusOptions" :key="item.code" :value="item.code">
            {{ item.desc }}
          </option>
        </select>
      </div>
      <div class="field">
        <label>公开性</label>
        <select v-model="queryParams.isPublic" class="input" @change="fetchList">
          <option :value="undefined">全部</option>
          <option v-for="item in publicOptions" :key="item.code" :value="item.code">
            {{ item.desc }}
          </option>
        </select>
      </div>
      <div class="filter-actions">
        <button class="btn btn-primary" :disabled="loading" @click="fetchList">查询</button>
        <button class="btn" @click="resetQuery">重置</button>
      </div>
    </section>

    <section v-if="loading" class="list-stack">
      <div v-for="item in 3" :key="item" class="trip-card skeleton-card">
        <div class="skeleton title-line"></div>
        <div class="skeleton meta-line"></div>
      </div>
    </section>

    <section v-else-if="list.length === 0" class="empty-state">
      <div class="empty-state-icon">🧭</div>
      <div class="empty-state-text">暂无行程</div>
      <button class="btn btn-primary" @click="handleCreate">创建第一个行程</button>
    </section>

    <section v-else class="list-stack">
      <article v-for="item in list" :key="item.id" class="trip-card" @click="handleDetail(item)">
        <div class="trip-main">
          <div class="trip-title-row">
            <h2>{{ item.title || "未命名行程" }}</h2>
            <span class="pill">{{ findDictDesc(statusOptions, item.status, "未设置") }}</span>
            <span v-if="item.isPublic === 1" class="pill public">公开</span>
          </div>
          <p class="trip-desc">{{ item.description || "暂无描述" }}</p>
          <div class="trip-meta">
            <span>{{ formatDateRange(item.startDate, item.endDate) }}</span>
            <span>{{ item.totalDays || 1 }} 天</span>
            <span v-if="item.estimatedBudget">预算 ¥{{ item.estimatedBudget }}</span>
            <span v-if="item.travelCompanion">{{ companionText(item.travelCompanion) }}</span>
          </div>
        </div>
        <div class="trip-actions" @click.stop>
          <button class="link-btn" @click="handleDetail(item)">详情</button>
          <button class="link-btn" @click="handleEdit(item)">编辑</button>
          <el-popconfirm
            title="确定删除该行程吗？此操作不可恢复"
            confirm-button-text="删除"
            confirm-button-type="danger"
            @confirm="handleDelete(item)"
          >
            <template #reference>
              <button class="link-btn danger">删除</button>
            </template>
          </el-popconfirm>
        </div>
      </article>
    </section>

    <div v-if="total > 0" class="pager">
      <el-pagination
        :current-page="queryParams.pageNum"
        :page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        background
        layout="total, sizes, prev, pager, next"
        @size-change="handlePageSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <ItineraryEditDialog
      v-model:visible="dialogVisible"
      :dialog-type="dialogType"
      :form="form"
      :submit-loading="submitLoading"
      @submit="submitForm"
    />
  </div>
</template>

<script setup lang="ts">
import { getPublicStatusDict, getTravelPlanStatusDict } from "@/api/dict";
import { findDictDesc, useDictOptions } from "@/composables/useDictOptions";
import { useItineraryList } from "@/composables/useItineraryList";
import ItineraryEditDialog from "@/views/user/components/ItineraryEditDialog.vue";

const { options: statusOptions } = useDictOptions("travel-plan-status", getTravelPlanStatusDict);
const { options: publicOptions } = useDictOptions("public-status", getPublicStatusDict);

const {
  loading,
  list,
  total,
  queryParams,
  dialogVisible,
  dialogType,
  submitLoading,
  form,
  fetchList,
  resetQuery,
  handleCreate,
  handleAiGenerate,
  handleEdit,
  handleDelete,
  submitForm,
  handleDetail,
  handlePageChange,
  handlePageSizeChange,
} = useItineraryList();

function formatDateRange(start?: string, end?: string): string {
  if (!start && !end) return "日期未设置";
  if (!end || start === end) return start || end || "日期未设置";
  return `${start} ~ ${end}`;
}

function companionText(value: string): string {
  const map: Record<string, string> = {
    solo: "独自旅行",
    couple: "情侣出游",
    family: "家庭旅行",
    friends: "朋友同行",
  };
  return map[value] || value;
}
</script>

<style scoped>
.itinerary-page {
  max-width: 1040px;
  padding: 20px 24px 64px;
  margin: 0 auto;
}

.page-hero,
.filter-card,
.trip-card {
  border: 1px solid var(--color-gray-200);
  border-radius: 18px;
}

.page-hero {
  display: flex;
  gap: 74px;
  align-items: flex-end;
  justify-content: space-between;
  padding: 32px;
  margin-bottom: 18px;
  background: linear-gradient(135deg, #ffffff 0%, #f7fff9 100%);
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 700;
  color: var(--color-primary-dark);
  text-transform: uppercase;
  letter-spacing: 0.16em;
}

.page-hero h1 {
  margin: 0;
  font-size: 34px;
  line-height: 1.15;
}

.hero-desc {
  max-width: 560px;
  margin: 10px 0 0;
  color: var(--color-gray-600);
}

.hero-actions,
.filter-actions,
.trip-actions,
.trip-title-row,
.trip-meta {
  display: flex;
  align-items: center;
}

.hero-actions,
.filter-actions,
.trip-actions {
  gap: 10px;
}

.ai-generate-btn {
  color: #111827;
  background:
    linear-gradient(#ffffff, #ffffff) padding-box,
    linear-gradient(135deg, #00e676, #3b82f6, #a855f7) border-box;
  border: 1px solid transparent;
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.12);
}

.ai-generate-btn:hover {
  background:
    linear-gradient(135deg, rgba(0, 230, 118, 0.18), rgba(59, 130, 246, 0.16)) padding-box,
    linear-gradient(135deg, #00e676, #3b82f6, #a855f7) border-box;
}

.filter-card {
  display: flex;
  gap: 14px;
  align-items: flex-end;
  padding: 18px;
  margin-bottom: 18px;
  background: var(--color-white);
}

.field {
  min-width: 150px;
}

.field.grow {
  flex: 1;
}

.field label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
}

.list-stack {
  display: grid;
  gap: 14px;
}

.trip-card {
  display: flex;
  gap: 20px;
  justify-content: space-between;
  padding: 22px;
  cursor: pointer;
  background: var(--color-white);
  transition:
    border-color var(--transition-fast),
    box-shadow var(--transition-fast),
    transform var(--transition-fast);
}

.trip-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 14px 28px rgba(0, 230, 118, 0.08);
  transform: translateY(-1px);
}

.trip-main {
  flex: 1;
  min-width: 0;
}

.trip-title-row {
  flex-wrap: wrap;
  gap: 8px;
}

.trip-title-row h2 {
  margin: 0;
  font-size: 20px;
}

.trip-desc {
  display: -webkit-box;
  margin: 10px 10px 12px;
  overflow: hidden;
  -webkit-line-clamp: 2;
  color: var(--color-gray-600);
  -webkit-box-orient: vertical;
}

.trip-meta {
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: var(--color-gray-500);
}

.trip-actions {
  flex-shrink: 0;
  flex-wrap: nowrap;
  justify-content: flex-end;
  min-width: 176px;
}

.pill {
  padding: 3px 9px;
  font-size: 12px;
  color: var(--color-gray-700);
  background: var(--color-gray-100);
  border-radius: var(--radius-full);
}

.pill.public {
  color: var(--color-primary-dark);
  background: var(--color-primary-light);
}

.link-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font: inherit;
  font-size: 14px;
  color: var(--color-gray-800);
  white-space: nowrap;
  cursor: pointer;
  background: transparent;
  border: 0;
  writing-mode: horizontal-tb;
}

.trip-actions .link-btn {
  min-width: 34px;
}

.link-btn:hover {
  color: var(--color-primary-dark);
}

.link-btn.danger:hover {
  color: var(--color-danger);
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

.skeleton-card {
  cursor: default;
}

.title-line {
  width: 36%;
  height: 22px;
  margin-bottom: 16px;
}

.meta-line {
  width: 62%;
  height: 14px;
}

@media (max-width: 760px) {
  .page-hero,
  .filter-card,
  .trip-card {
    flex-direction: column;
    align-items: stretch;
  }

  .trip-actions {
    justify-content: flex-end;
  }
}
</style>
