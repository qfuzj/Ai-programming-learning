<template>
  <div class="onboarding-page">
    <div class="onboarding-container">
      <header class="onboarding-header">
        <div class="brand">智游</div>
        <h1 class="title">完善个人偏好</h1>
        <p class="subtitle">花一分钟告诉我们更多，AI 会更懂你</p>
      </header>

      <div class="step-indicator">
        <div class="step" :class="{ active: step >= 1, done: step > 1 }">
          <span class="step-num">1</span>
          <span class="step-label">基本资料</span>
        </div>
        <div class="step-line" :class="{ active: step > 1 }"></div>
        <div class="step" :class="{ active: step >= 2 }">
          <span class="step-num">2</span>
          <span class="step-label">兴趣偏好</span>
        </div>
      </div>

      <section v-if="step === 1" class="card">
        <div class="form-group">
          <label class="form-label">昵称</label>
          <input
            v-model="profile.nickname"
            class="form-input"
            placeholder="给自己起个昵称"
            maxlength="30"
          />
        </div>

        <div class="form-group">
          <label class="form-label">性别</label>
          <div class="radio-row">
            <label class="radio-chip" :class="{ active: profile.gender === 1 }">
              <input v-model.number="profile.gender" type="radio" :value="1" />
              <span>男</span>
            </label>
            <label class="radio-chip" :class="{ active: profile.gender === 2 }">
              <input v-model.number="profile.gender" type="radio" :value="2" />
              <span>女</span>
            </label>
            <label class="radio-chip" :class="{ active: profile.gender === 0 }">
              <input v-model.number="profile.gender" type="radio" :value="0" />
              <span>不愿透露</span>
            </label>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">生日</label>
          <input v-model="profile.birthday" class="form-input" type="date" :max="todayStr" />
        </div>

        <div class="form-group">
          <label class="form-label">所在城市</label>
          <div class="region-row">
            <select v-model="provinceId" class="form-input form-select">
              <option :value="undefined">选择省/直辖市</option>
              <option v-for="p in provinces" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
            <select v-model="cityId" class="form-input form-select">
              <option :value="undefined">选择城市</option>
              <option v-for="c in cityOptions" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
        </div>

        <div class="actions">
          <button class="btn-text" @click="skip">先跳过</button>
          <button class="btn-primary" @click="goNext">下一步</button>
        </div>
      </section>

      <section v-else class="card">
        <p class="card-hint">至少选择 3 个标签，让推荐更准</p>

        <div v-if="tagLoading" class="loading-text">加载中...</div>
        <div v-else-if="groupedTags.length === 0" class="loading-text">暂无可选标签</div>

        <div v-else class="tag-groups">
          <div v-for="group in groupedTags" :key="group.category" class="tag-group">
            <div class="tag-group-title">{{ group.category }}</div>
            <div class="tag-list">
              <button
                v-for="tag in group.tags"
                :key="tag.id"
                type="button"
                class="tag-chip"
                :class="{ active: selectedTagIds.includes(tag.id) }"
                @click="toggleTag(tag.id)"
              >
                {{ tag.name }}
              </button>
            </div>
          </div>
        </div>

        <div class="actions">
          <button class="btn-text" @click="step = 1">上一步</button>
          <button class="btn-primary" :disabled="submitting" @click="finish">
            {{ submitting ? "保存中..." : "完成，开始探索" }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import {
  getProfileInfo,
  getMyPreferenceTags,
  updateProfileInfo,
  updatePreferenceTags,
} from "@/api/profile";
import {
  getRegionTree,
  getTagsByScope,
  type CommonRegionNode,
  type CommonTagItem,
} from "@/api/common";
import { ROUTE_PATHS } from "@/router/constants";

const router = useRouter();
const step = ref<1 | 2>(1);
const submitting = ref(false);
const tagLoading = ref(false);

const profile = reactive({
  nickname: "",
  gender: 0,
  birthday: "",
});

const todayStr = new Date().toISOString().slice(0, 10);

const regionTree = ref<CommonRegionNode[]>([]);
const provinceId = ref<number | undefined>();
const cityId = ref<number | undefined>();

const provinces = computed(() => regionTree.value);
const cityOptions = computed(() => {
  const p = regionTree.value.find((r) => r.id === provinceId.value);
  return p?.children ?? [];
});

const allTags = ref<CommonTagItem[]>([]);
const selectedTagIds = ref<number[]>([]);

const groupedTags = computed(() => {
  const map = new Map<string, CommonTagItem[]>();
  for (const tag of allTags.value) {
    if (tag.status !== undefined && tag.status !== 1) continue;
    const cat = tag.category || "其他";
    if (!map.has(cat)) map.set(cat, []);
    map.get(cat)!.push(tag);
  }
  return Array.from(map.entries())
    .sort(([a], [b]) => (a === "其他" ? 1 : b === "其他" ? -1 : a.localeCompare(b)))
    .map(([category, tags]) => ({
      category,
      tags: tags.sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0)),
    }));
});

function toggleTag(id: number): void {
  const idx = selectedTagIds.value.indexOf(id);
  if (idx >= 0) selectedTagIds.value.splice(idx, 1);
  else selectedTagIds.value.push(id);
}

function findRegionPath(id?: number): { provinceId?: number; cityId?: number } {
  if (!id) return {};
  for (const p of regionTree.value) {
    if (p.id === id) return { provinceId: p.id };
    const child = p.children?.find((c) => c.id === id);
    if (child) return { provinceId: p.id, cityId: child.id };
  }
  return {};
}

async function goNext(): Promise<void> {
  if (!profile.nickname.trim()) {
    alert("请输入昵称");
    return;
  }
  await saveProfileStep();
  step.value = 2;
  if (allTags.value.length === 0) {
    await loadTags();
  }
}

async function saveProfileStep(): Promise<void> {
  const regionId = cityId.value ?? provinceId.value;
  await updateProfileInfo({
    nickname: profile.nickname.trim(),
    gender: profile.gender,
    birthday: profile.birthday || undefined,
    regionId,
  });
}

async function finish(): Promise<void> {
  submitting.value = true;
  try {
    await updatePreferenceTags(selectedTagIds.value);
    sessionStorage.removeItem("auth_onboarding_pending");
    await router.replace(ROUTE_PATHS.USER_HOME);
  } finally {
    submitting.value = false;
  }
}

function skip(): void {
  sessionStorage.removeItem("auth_onboarding_pending");
  router.replace(ROUTE_PATHS.USER_HOME);
}

async function loadProfile(): Promise<void> {
  try {
    const p = await getProfileInfo();
    profile.nickname = p.nickname || "";
    profile.gender = p.gender ?? 0;
    profile.birthday = p.birthday || "";
    const path = findRegionPath(p.regionId ?? undefined);
    provinceId.value = path.provinceId;
    cityId.value = path.cityId;
  } catch {
    /* ignore */
  }
}

async function loadRegions(): Promise<void> {
  try {
    regionTree.value = (await getRegionTree()) ?? [];
  } catch {
    /* ignore */
  }
}

async function loadTags(): Promise<void> {
  tagLoading.value = true;
  try {
    const [tags, mine] = await Promise.all([
      getTagsByScope("PREFERENCE"),
      getMyPreferenceTags().catch(() => []),
    ]);
    allTags.value = tags;
    if (Array.isArray(mine) && mine.length > 0) {
      selectedTagIds.value = mine
        .map((t: { id?: number }) => t?.id)
        .filter((id): id is number => typeof id === "number");
    }
  } finally {
    tagLoading.value = false;
  }
}

onMounted(async () => {
  await loadRegions();
  await loadProfile();
});
</script>

<style scoped>
.onboarding-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 32px 16px;
  background: #fafbfc;
}

.onboarding-container {
  width: 100%;
  max-width: 560px;
}

.onboarding-header {
  margin-bottom: 28px;
  text-align: center;
}

.brand {
  margin-bottom: 16px;
  font-size: 24px;
  font-weight: 800;
  color: #00e676;
  letter-spacing: -0.5px;
}

.title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  color: #000;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #999;
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 24px;
}

.step {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 13px;
  color: #999;
}

.step .step-num {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  font-size: 12px;
  font-weight: 700;
  color: #999;
  background: #f0f0f0;
  border-radius: 999px;
}

.step.active .step-num {
  color: #000;
  background: #00e676;
}

.step.active {
  font-weight: 600;
  color: #000;
}

.step.done .step-num {
  color: #fff;
  background: #00c665;
}

.step-line {
  width: 48px;
  height: 1px;
  background: #e0e0e0;
}

.step-line.active {
  background: #00e676;
}

.card {
  padding: 28px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 16px;
}

.card-hint {
  margin: 0 0 20px;
  font-size: 13px;
  color: #999;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #000;
}

.form-input {
  width: 100%;
  padding: 11px 14px;
  font-size: 15px;
  color: #000;
  outline: none;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: border-color 0.2s;
}

.form-input:focus {
  border-color: #00e676;
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%23999' stroke-width='2'><polyline points='6 9 12 15 18 9'/></svg>");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
}

.region-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.radio-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.radio-chip {
  position: relative;
  padding: 8px 18px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  background: #f6f7f9;
  border: 1px solid transparent;
  border-radius: 999px;
  transition: all 0.15s;
}

.radio-chip input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.radio-chip.active {
  font-weight: 600;
  color: #00763a;
  background: #e6fff1;
  border-color: #00e676;
}

.tag-groups {
  display: flex;
  flex-direction: column;
  gap: 22px;
  max-height: 360px;
  padding-right: 4px;
  overflow-y: auto;
}

.tag-group-title {
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #888;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-chip {
  padding: 8px 16px;
  font-size: 14px;
  color: #444;
  cursor: pointer;
  background: #f6f7f9;
  border: 1px solid transparent;
  border-radius: 999px;
  transition: all 0.15s;
}

.tag-chip:hover {
  background: #ecf2ee;
}

.tag-chip.active {
  font-weight: 600;
  color: #00763a;
  background: #e6fff1;
  border-color: #00e676;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  margin-top: 28px;
}

.btn-primary {
  flex: 1;
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 600;
  color: #000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #00c665;
}

.btn-primary:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-text {
  padding: 8px 12px;
  font-size: 14px;
  color: #888;
  cursor: pointer;
  background: transparent;
  border: none;
}

.btn-text:hover {
  color: #000;
}

.loading-text {
  padding: 32px;
  font-size: 14px;
  color: #999;
  text-align: center;
}

@media (max-width: 480px) {
  .region-row {
    grid-template-columns: 1fr;
  }

  .step-line {
    width: 24px;
  }

  .step-label {
    display: none;
  }
}
</style>
