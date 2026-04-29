<!-- 极简风格AI生成行程页 -->
<template>
  <div class="page">
    <h1 class="title">AI 生成行程</h1>
    <p class="subtitle">告诉AI您的偏好，自动生成行程草案</p>

    <form class="form" @submit.prevent="generate">
      <div class="form-row">
        <div class="form-group">
          <label class="label">目的地</label>
          <select v-model="form.destinationRegionId" class="input">
            <option :value="undefined">请选择地区</option>
            <option v-for="r in regionList" :key="r.id" :value="r.id">{{ r.name }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="label">总天数</label>
          <input v-model.number="form.days" type="number" class="input" min="1" max="15" />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label class="label">开始日期</label>
          <input v-model="form.startDate" type="date" class="input" />
        </div>
        <div class="form-group">
          <label class="label">结束日期</label>
          <input v-model="form.endDate" type="date" class="input" />
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

    <!-- 生成结果 -->
    <div v-if="loading" class="loading">正在生成行程草案，请稍候...</div>

    <div v-else-if="result" class="result">
      <h2 class="result-title">行程草案</h2>
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
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { generateItineraryByAi, createItinerary } from "@/api/itinerary";
import { getRegionTree } from "@/api/common";
import { getMyPreferenceTags } from "@/api/profile";
import type { CommonTagItem } from "@/api/common";

const router = useRouter();
const loading = ref(false);
const saveLoading = ref(false);
const result = ref<any>(null);
const errorMsg = ref("");
const regionList = ref<any[]>([]);
const tagOptions = ref<CommonTagItem[]>([]);

const form = reactive({
  destinationRegionId: undefined as number | undefined,
  days: 3,
  startDate: "",
  endDate: "",
  budget: undefined as number | undefined,
  companionType: "",
  travelStyle: "",
  preferredTagIds: [] as number[],
});

async function loadData(): Promise<void> {
  try {
    const [regions, tags] = await Promise.all([
      getRegionTree(),
      getMyPreferenceTags().catch(() => []),
    ]);
    regionList.value = regions || [];
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
  form.destinationRegionId = undefined;
  form.days = 3;
  form.startDate = "";
  form.endDate = "";
  form.budget = undefined;
  form.companionType = "";
  form.travelStyle = "";
  form.preferredTagIds = [];
  result.value = null;
  errorMsg.value = "";
}

async function generate(): Promise<void> {
  if (!form.destinationRegionId) {
    alert("请选择目的地");
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
    const destinationName =
      regionList.value.find((r) => r.id === form.destinationRegionId)?.name || "";
    const res = await generateItineraryByAi({
      destination: destinationName,
      days: form.days,
      startDate: form.startDate,
      endDate: form.endDate,
      budget: form.budget,
      companionType: form.companionType || undefined,
      travelStyle: form.travelStyle || undefined,
      preferredTags: form.preferredTagIds.length > 0 ? form.preferredTagIds.map(String) : undefined,
    });
    result.value = res;
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
    const res = await createItinerary({
      title: result.value.title || "AI生成行程",
      destinationRegionId: result.value.destinationRegionId || form.destinationRegionId,
      totalDays: result.value.totalDays || form.days,
      startDate: result.value.startDate || form.startDate,
      endDate: result.value.endDate || form.endDate,
      estimatedBudget: result.value.estimatedBudget,
      description: result.value.description,
    });
    await router.push(`/itinerary/${res}`);
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
  max-width: 800px;
  padding: 40px 24px;
  margin: 0 auto;
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
  margin-top: 48px;
}

.result-title {
  margin: 0 0 24px 0;
  font-size: 24px;
  font-weight: 700;
  color: #000000;
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
