<!-- 极简风格行程创建页 -->
<template>
  <div class="page">
    <h1 class="title">创建行程</h1>
    <p class="subtitle">规划您的下一段旅程</p>

    <form class="form" @submit.prevent="submit">
      <div class="form-group">
        <label class="label">行程标题</label>
        <input v-model="form.title" type="text" class="input" placeholder="例如：杭州三日游" />
      </div>

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
          <input v-model.number="form.totalDays" type="number" class="input" min="1" max="15" />
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

      <div class="form-group">
        <label class="label">预算（可选）</label>
        <input
          v-model.number="form.estimatedBudget"
          type="number"
          class="input"
          placeholder="例如：3000"
        />
      </div>

      <div class="form-group">
        <label class="label">旅行同伴</label>
        <select v-model="form.travelCompanion" class="input">
          <option value="">请选择</option>
          <option value="solo">独自旅行</option>
          <option value="couple">情侣出游</option>
          <option value="family">家庭旅行</option>
          <option value="friends">朋友同行</option>
        </select>
      </div>

      <div class="form-group">
        <label class="label">描述（可选）</label>
        <textarea
          v-model="form.description"
          class="textarea"
          rows="3"
          placeholder="简单描述您的行程计划..."
        ></textarea>
      </div>

      <div class="form-actions">
        <button type="button" class="btn-cancel" @click="router.back()">取消</button>
        <button type="submit" class="btn-submit" :disabled="submitting">
          {{ submitting ? "创建中..." : "创建行程" }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { createItinerary } from "@/api/itinerary";
import { getRegionTree } from "@/api/common";

const router = useRouter();
const submitting = ref(false);
const regionList = ref<any[]>([]);

const form = reactive({
  title: "",
  destinationRegionId: undefined as number | undefined,
  totalDays: 3,
  startDate: "",
  endDate: "",
  estimatedBudget: undefined as number | undefined,
  travelCompanion: "",
  description: "",
});

async function loadRegions(): Promise<void> {
  try {
    const tree = await getRegionTree();
    regionList.value = tree || [];
  } catch {
    /* empty */
  }
}

async function submit(): Promise<void> {
  if (!form.title.trim()) {
    alert("请输入行程标题");
    return;
  }
  if (!form.destinationRegionId) {
    alert("请选择目的地");
    return;
  }
  if (!form.startDate || !form.endDate) {
    alert("请选择日期");
    return;
  }

  submitting.value = true;
  try {
    const res = await createItinerary({
      title: form.title,
      destinationRegionId: form.destinationRegionId,
      totalDays: form.totalDays,
      startDate: form.startDate,
      endDate: form.endDate,
      estimatedBudget: form.estimatedBudget,
      travelCompanion: form.travelCompanion || undefined,
      description: form.description || undefined,
    });
    await router.push(`/itinerary/${res}`);
  } catch {
    alert("创建失败");
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  loadRegions();
});
</script>

<style scoped>
.page {
  max-width: 700px;
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

.textarea {
  padding: 12px 16px;
  font-family: inherit;
  font-size: 15px;
  color: #000000;
  resize: vertical;
  outline: none;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: border-color 0.2s;
}

.textarea:focus {
  border-color: #00e676;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.btn-cancel {
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

.btn-cancel:hover {
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
</style>
