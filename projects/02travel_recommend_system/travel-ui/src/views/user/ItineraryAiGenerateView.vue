<!-- 极简风格AI生成行程页 -->
<template>
  <div class="page">
    <div class="content-layout">
      <section class="form-panel" :class="{ compact: !!result }">
        <h1 class="title">AI 生成行程</h1>
        <p class="subtitle">告诉AI您的偏好，自动生成行程草案</p>

        <form class="form" @submit.prevent="generate">
          <div class="form-row">
            <div class="form-group">
              <label class="label">目的地</label>
              <input
                v-model.trim="form.destination"
                type="text"
                class="input"
                placeholder="请输入目的地"
              />
            </div>
            <div class="form-group">
              <label class="label">总天数</label>
              <input
                v-model.number="form.days"
                type="number"
                class="input"
                min="1"
                :max="MAX_ITINERARY_DAYS"
                placeholder="例如3"
              />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group form-group-full">
              <label class="label">出行日期</label>
              <div class="block">
                <el-date-picker
                  v-model="dateRange"
                  type="datetimerange"
                  range-separator="To"
                  start-placeholder="Start date"
                  end-placeholder="End date"
                  class="date-range-picker"
                />
              </div>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="label">预算（可选）</label>
              <input
                v-model.number="form.budget"
                type="number"
                class="input"
                placeholder="例如：3000"
              />
            </div>
            <div class="form-group">
              <label class="label">旅行同伴</label>
              <select v-model="form.companionType" class="input">
                <option value="">请选择</option>
                <option value="solo">独自旅行</option>
                <option value="couple">情侣出游</option>
                <option value="family">家庭旅行</option>
                <option value="friends">朋友同行</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label class="label">旅行风格</label>
            <select v-model="form.travelStyle" class="input">
              <option value="">请选择</option>
              <option value="classic">经典游</option>
              <option value="slow">慢旅行</option>
              <option value="food">美食游</option>
              <option value="outdoor">户外游</option>
            </select>
          </div>

          <div class="form-group">
            <label class="label">偏好标签（可选）</label>
            <div class="tag-select">
              <span
                v-for="t in tagOptions"
                :key="t.id"
                class="tag-option"
                :class="{ active: form.preferredTagIds.includes(t.id) }"
                @click="toggleTag(t.id)"
              >
                {{ t.name }}
              </span>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="btn-reset" @click="resetForm">重置</button>
            <button type="submit" class="btn-submit" :disabled="loading">
              {{ loading ? "生成中..." : "生成行程" }}
            </button>
          </div>
        </form>
      </section>

      <section ref="resultSectionRef" class="result-panel">
        <div v-if="loading" class="loading">正在生成行程草案，请稍候...</div>

        <div v-else-if="result" class="result">
          <div class="result-header">
            <h2 class="result-title">行程草案</h2>
            <span class="result-tip">已生成，请确认后保存</span>
          </div>
          <div v-for="day in result.days" :key="day.dayNo" class="day-card">
            <h3 class="day-title">第 {{ day.dayNo }} 天</h3>
            <div v-for="item in day.items" :key="item.id" class="day-item">
              <span class="item-time">{{ item.startTime || "--:--" }}</span>
              <div class="item-content">
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
                <span v-if="item.location" class="item-location">{{ item.location }}</span>
              </div>
            </div>
          </div>

          <div class="result-actions">
            <button class="btn-save" :disabled="saveLoading" @click="save">
              {{ saveLoading ? "保存中..." : "保存行程" }}
            </button>
          </div>
        </div>

        <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, watch, nextTick } from "vue";
import { useRouter } from "vue-router";
import { generateItineraryByAi, createItinerary, addItineraryItem } from "@/api/itinerary";
import { getMyPreferenceTags } from "@/api/profile";
import type { CommonTagItem } from "@/api/common";

const router = useRouter();
const loading = ref(false);
const saveLoading = ref(false);
const result = ref<any>(null);
const resultSectionRef = ref<HTMLElement | null>(null);
const errorMsg = ref("");
const tagOptions = ref<CommonTagItem[]>([]);

const form = reactive({
  destination: "",
  days: 3,
  startDate: "",
  endDate: "",
  budget: undefined as number | undefined,
  companionType: "",
  travelStyle: "",
  preferredTagIds: [] as number[],
});

const dateRange = ref<[Date, Date] | null>(null);

const MAX_ITINERARY_DAYS = 10;
const DAY_MS = 24 * 60 * 60 * 1000;

function parseDateInput(dateStr: string): Date | null {
  if (!dateStr) return null;
  const date = new Date(`${dateStr}T00:00:00`);
  if (Number.isNaN(date.getTime())) return null;
  return date;
}

function formatDateInput(date: Date): string {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function clampDays(days: number): number {
  if (!Number.isFinite(days) || days < 1) return 1;
  if (days > MAX_ITINERARY_DAYS) return MAX_ITINERARY_DAYS;
  return Math.floor(days);
}

function normalizeRange(range: [Date, Date]): [Date, Date] {
  const start = new Date(range[0]);
  const end = new Date(range[1]);
  start.setHours(0, 0, 0, 0);
  end.setHours(0, 0, 0, 0);

  if (end.getTime() < start.getTime()) {
    end.setTime(start.getTime());
  }

  const inclusiveDays = Math.floor((end.getTime() - start.getTime()) / DAY_MS) + 1;
  if (inclusiveDays > MAX_ITINERARY_DAYS) {
    end.setTime(start.getTime() + (MAX_ITINERARY_DAYS - 1) * DAY_MS);
  }

  return [start, end];
}

function syncFormFromDateRange(range: [Date, Date]): void {
  const [start, end] = normalizeRange(range);
  const normalizedDays = Math.floor((end.getTime() - start.getTime()) / DAY_MS) + 1;
  const startDate = formatDateInput(start);
  const endDate = formatDateInput(end);

  if (
    !dateRange.value ||
    dateRange.value[0].getTime() !== start.getTime() ||
    dateRange.value[1].getTime() !== end.getTime()
  ) {
    dateRange.value = [start, end];
  }

  form.startDate = startDate;
  form.endDate = endDate;
  form.days = normalizedDays;
}

function syncDateRangeFromForm(): void {
  if (!form.startDate) return;
  const start = parseDateInput(form.startDate);
  if (!start) return;

  const endFromForm = form.endDate ? parseDateInput(form.endDate) : null;
  if (endFromForm) {
    syncFormFromDateRange([start, endFromForm]);
    return;
  }

  const days = clampDays(Number(form.days) || 1);
  const end = new Date(start.getTime() + (days - 1) * DAY_MS);
  dateRange.value = [start, end];
}

async function loadData(): Promise<void> {
  try {
    const tags = await getMyPreferenceTags().catch(() => []);
    tagOptions.value = tags || [];
  } catch {
    /* empty */
  }
}

function toggleTag(id: number): void {
  const idx = form.preferredTagIds.indexOf(id);
  if (idx >= 0) form.preferredTagIds.splice(idx, 1);
  else form.preferredTagIds.push(id);
}

function resetForm(): void {
  form.destination = "";
  form.days = 3;
  form.startDate = "";
  form.endDate = "";
  dateRange.value = null;
  form.budget = undefined;
  form.companionType = "";
  form.travelStyle = "";
  form.preferredTagIds = [];
  result.value = null;
  errorMsg.value = "";
}

watch(
  () => dateRange.value,
  (range) => {
    if (!range || range.length !== 2) {
      form.startDate = "";
      form.endDate = "";
      return;
    }
    syncFormFromDateRange(range);
  },
  { deep: true }
);

watch(
  () => form.days,
  (value) => {
    const days = clampDays(Number(value));
    if (days !== value) {
      form.days = days;
      return;
    }

    if (!form.startDate) return;

    const start = parseDateInput(form.startDate);
    if (!start) return;

    const nextEnd = new Date(start.getTime() + (days - 1) * DAY_MS);
    const nextEndDate = formatDateInput(nextEnd);
    if (form.endDate !== nextEndDate) {
      form.endDate = nextEndDate;
      syncDateRangeFromForm();
    }
  }
);

function scrollToResult(): void {
  if (!resultSectionRef.value) return;
  resultSectionRef.value.scrollIntoView({ behavior: "smooth", block: "start" });
}

async function generate(): Promise<void> {
  if (!form.destination) {
    alert("请输入目的地");
    return;
  }
  if (!form.startDate || !form.endDate) {
    alert("请选择日期");
    return;
  }

  loading.value = true;
  errorMsg.value = "";
  result.value = null;
  try {
    const res = await generateItineraryByAi({
      destination: form.destination,
      days: form.days,
      startDate: form.startDate,
      endDate: form.endDate,
      budget: form.budget,
      companionType: form.companionType || undefined,
      travelStyle: form.travelStyle || undefined,
      preferredTags: form.preferredTagIds.length > 0 ? form.preferredTagIds.map(String) : undefined,
    });
    result.value = res;
    await nextTick();
    scrollToResult();
  } catch {
    errorMsg.value = "生成失败，请重试";
  } finally {
    loading.value = false;
  }
}

async function save(): Promise<void> {
  if (!result.value) return;
  saveLoading.value = true;
  try {
    const planId = await createItinerary({
      title: result.value.title || "AI生成行程",
      destination: result.value.destination || form.destination,
      destinationRegionId: result.value.destinationRegionId,
      totalDays: result.value.totalDays || form.days,
      startDate: result.value.startDate || form.startDate,
      endDate: result.value.endDate || form.endDate,
      estimatedBudget: result.value.estimatedBudget,
      description: result.value.description,
    });

    const dayList = Array.isArray(result.value.days) ? result.value.days : [];
    for (const day of dayList) {
      const dayNo = Number(day?.dayNo);
      if (!dayNo || !Array.isArray(day?.items)) continue;
      for (const item of day.items) {
        if (!item?.title) continue;
        await addItineraryItem(planId, {
          dayNo,
          scenicSpotId: item.scenicSpotId,
          sortOrder: item.sortOrder,
          itemType: item.itemType,
          title: item.title,
          description: item.description,
          startTime: item.startTime,
          endTime: item.endTime,
          location: item.location,
          longitude: item.longitude,
          latitude: item.latitude,
          estimatedCost: item.estimatedCost,
          notes: item.notes,
        });
      }
    }

    await router.push(`/itinerary/${planId}`);
  } catch {
    alert("保存失败");
  } finally {
    saveLoading.value = false;
  }
}

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.page {
  max-width: 1240px;
  padding: 40px 24px;
  margin: 0 auto;
}

.content-layout {
  display: grid;
  grid-template-columns: 440px minmax(0, 1fr);
  gap: 32px;
  align-items: start;
}

.form-panel {
  position: sticky;
  top: 88px;
  padding: 24px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
}

.form-panel.compact {
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}

.result-panel {
  min-height: 320px;
}

@media (max-width: 960px) {
  .content-layout {
    grid-template-columns: 1fr;
  }

  .form-panel {
    position: static;
  }
}

.title {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 700;
  color: #000000;
}

.subtitle {
  margin: 0 0 40px 0;
  font-size: 15px;
  color: #999999;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

.form-group-full {
  grid-column: 1 / -1;
}

.block {
  width: 100%;
}

.date-range-picker {
  width: 100% !important;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.label {
  font-size: 14px;
  font-weight: 500;
  color: #000000;
}

.input {
  padding: 12px 16px;
  font-family: inherit;
  font-size: 15px;
  color: #000000;
  outline: none;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: border-color 0.2s;
}

.input:focus {
  border-color: #00e676;
}

.tag-select {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-option {
  padding: 6px 14px;
  font-size: 13px;
  color: #666666;
  cursor: pointer;
  background: #f5f5f5;
  border: 1px solid transparent;
  border-radius: 999px;
  transition: all 0.2s;
}

.tag-option.active {
  color: #000000;
  background: #00e676;
  border-color: #00e676;
}

.tag-option:hover {
  background: #eeeeee;
}

.tag-option.active:hover {
  background: #00c665;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.btn-reset {
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 500;
  color: #666666;
  cursor: pointer;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.2s;
}

.btn-reset:hover {
  color: #000000;
  border-color: #000000;
}

.btn-submit {
  padding: 12px 32px;
  font-size: 15px;
  font-weight: 600;
  color: #000000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
  transition: background 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background: #00c665;
}

.btn-submit:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* Result */
.result {
  margin-top: 0;
}

.result-header {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.result-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #000000;
}

.result-tip {
  font-size: 13px;
  color: #00a152;
}

.day-card {
  padding: 24px;
  margin-bottom: 20px;
  background: #f9f9f9;
  border-radius: 12px;
}

.day-title {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #000000;
}

.day-item {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.day-item:last-child {
  border-bottom: none;
}

.item-time {
  flex-shrink: 0;
  min-width: 50px;
  font-size: 13px;
  color: #999999;
}

.item-content strong {
  font-size: 15px;
  font-weight: 600;
  color: #000000;
}

.item-content p {
  margin: 4px 0;
  font-size: 14px;
  line-height: 1.6;
  color: #666666;
}

.item-location {
  font-size: 12px;
  color: #999999;
}

.result-actions {
  margin-top: 24px;
}

.btn-save {
  padding: 12px 32px;
  font-size: 15px;
  font-weight: 600;
  color: #000000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
  transition: background 0.2s;
}

.btn-save:hover:not(:disabled) {
  background: #00c665;
}

.btn-save:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.loading {
  padding: 60px 20px;
  font-size: 15px;
  color: #999999;
  text-align: center;
}

.error {
  padding: 12px 16px;
  margin-top: 24px;
  font-size: 14px;
  color: #ff5252;
  background: #fff5f5;
  border-radius: 8px;
}
</style>
