<!-- 极简风格个人中心页 -->
<template>
  <div v-if="profile" class="page">
    <div class="header">
      <div class="avatar">{{ (profile.nickname || profile.username || "U").charAt(0) }}</div>
      <div class="info">
        <h1 class="name">{{ profile.nickname || profile.username }}</h1>
        <p class="meta">
          <span v-if="profile.gender === 1">男</span>
          <span v-if="profile.gender === 2">女</span>
          <span v-if="profile.birthday">{{ profile.birthday }}</span>
        </p>
      </div>
      <button class="btn-edit" @click="showEdit = true">编辑资料</button>
    </div>

    <!-- 标签页 -->
    <div class="tabs">
      <span
        v-for="tab in tabs"
        :key="tab.key"
        class="tab"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
      </span>
    </div>

    <!-- 收藏 -->
    <div v-if="activeTab === 'favorites'" class="tab-content">
      <div v-if="favLoading" class="loading">加载中...</div>
      <div v-else-if="favList.length === 0" class="empty">暂无收藏</div>
      <div v-else class="card-list">
        <div
          v-for="item in favList"
          :key="item.scenicId"
          class="card"
          @click="router.push(`/scenic/${item.scenicId}`)"
        >
          <img :src="item.coverImage || ''" class="card-img" />
          <div class="card-body">
            <h3 class="card-title">{{ item.scenicName }}</h3>
            <p class="card-time">收藏于 {{ item.favoriteTime }}</p>
          </div>
          <button class="btn-remove" @click.stop="removeFav(item.scenicId)">取消收藏</button>
        </div>
      </div>
    </div>

    <!-- 点评 -->
    <div v-if="activeTab === 'reviews'" class="tab-content">
      <div v-if="reviewLoading" class="loading">加载中...</div>
      <div v-else-if="reviewList.length === 0" class="empty">暂无点评</div>
      <div v-else class="card-list">
        <div v-for="item in reviewList" :key="item.id" class="card">
          <div class="card-body">
            <h3 class="card-title" @click="router.push(`/scenic/${item.scenicId}`)">
              {{ item.scenicName }}
            </h3>
            <p class="card-score">{{ item.score }}分</p>
            <p class="card-content">{{ item.content }}</p>
            <p class="card-time">{{ item.createTime }}</p>
          </div>
          <button class="btn-remove" @click="removeReview(item.id)">删除</button>
        </div>
      </div>
    </div>

    <!-- 浏览历史 -->
    <div v-if="activeTab === 'history'" class="tab-content">
      <div v-if="histLoading" class="loading">加载中...</div>
      <div v-else-if="histList.length === 0" class="empty">暂无浏览记录</div>
      <div v-else class="card-list">
        <div
          v-for="item in histList"
          :key="item.id"
          class="card"
          @click="router.push(`/scenic/${item.scenicId}`)"
        >
          <img :src="item.coverImage || ''" class="card-img" />
          <div class="card-body">
            <h3 class="card-title">{{ item.scenicName }}</h3>
            <p class="card-time">浏览于 {{ item.visitTime }}</p>
          </div>
          <button class="btn-remove" @click.stop="removeHist(item.id)">删除</button>
        </div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <div v-if="showEdit" class="modal-overlay" @click.self="showEdit = false">
      <div class="modal">
        <h2 class="modal-title">编辑资料</h2>
        <div class="form-group">
          <label class="label">昵称</label>
          <input v-model="editForm.nickname" class="input" placeholder="昵称" />
        </div>
        <div class="form-group">
          <label class="label">签名</label>
          <input v-model="editForm.signature" class="input" placeholder="个性签名" />
        </div>
        <div class="form-group">
          <label class="label">性别</label>
          <select v-model="editForm.gender" class="input">
            <option :value="0">未知</option>
            <option :value="1">男</option>
            <option :value="2">女</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showEdit = false">取消</button>
          <button class="btn-save" :disabled="saveLoading" @click="saveProfile">保存</button>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="loading-page">加载中...</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  getProfileInfo as getProfile,
  updateProfileInfo as updateProfile,
  type ProfileInfo as ProfileItem,
} from "@/api/profile";
import { getFavorites, removeFavorite } from "@/api/favorite";
import { getMyReviews, deleteMyReview } from "@/api/audit";
import { getBrowseHistoryPage as getBrowseHistory, deleteBrowseHistory } from "@/api/history";

const router = useRouter();
const profile = ref<ProfileItem | null>(null);
const activeTab = ref("favorites");
const showEdit = ref(false);
const saveLoading = ref(false);

const editForm = reactive({
  nickname: "",
  signature: "",
  gender: 0,
});

const tabs = [
  { key: "favorites", label: "收藏" },
  { key: "reviews", label: "点评" },
  { key: "history", label: "浏览历史" },
];

// 收藏
const favList = ref<any[]>([]);
const favLoading = ref(false);
async function loadFav(): Promise<void> {
  favLoading.value = true;
  try {
    const res = await getFavorites({ pageNum: 1, pageSize: 50 });
    favList.value = res || [];
  } finally {
    favLoading.value = false;
  }
}
async function removeFav(scenicId: number): Promise<void> {
  if (!confirm("确定取消收藏？")) return;
  try {
    await removeFavorite(scenicId);
    loadFav();
  } catch {
    alert("操作失败");
  }
}

// 点评
const reviewList = ref<any[]>([]);
const reviewLoading = ref(false);
async function loadReviews(): Promise<void> {
  reviewLoading.value = true;
  try {
    const res = await getMyReviews({ pageNum: 1, pageSize: 50 });
    reviewList.value = res.records || [];
  } finally {
    reviewLoading.value = false;
  }
}
async function removeReview(id: number): Promise<void> {
  if (!confirm("确定删除点评？")) return;
  try {
    await deleteMyReview(id);
    loadReviews();
  } catch {
    alert("删除失败");
  }
}

// 历史
const histList = ref<any[]>([]);
const histLoading = ref(false);
async function loadHist(): Promise<void> {
  histLoading.value = true;
  try {
    const res = await getBrowseHistory({ pageNum: 1, pageSize: 50 });
    histList.value = res.records || [];
  } finally {
    histLoading.value = false;
  }
}
async function removeHist(id: number): Promise<void> {
  if (!confirm("确定删除该记录？")) return;
  try {
    await deleteBrowseHistory(id);
    loadHist();
  } catch {
    alert("删除失败");
  }
}

function switchTab(key: string): void {
  activeTab.value = key;
  if (key === "favorites" && favList.value.length === 0) loadFav();
  if (key === "reviews" && reviewList.value.length === 0) loadReviews();
  if (key === "history" && histList.value.length === 0) loadHist();
}

async function saveProfile(): Promise<void> {
  saveLoading.value = true;
  try {
    await updateProfile({
      nickname: editForm.nickname || undefined,
      signature: editForm.signature || undefined,
      gender: editForm.gender || undefined,
    });
    showEdit.value = false;
    const p = await getProfile();
    profile.value = p;
  } catch {
    alert("保存失败");
  } finally {
    saveLoading.value = false;
  }
}

onMounted(async () => {
  try {
    profile.value = await getProfile();
    editForm.nickname = profile.value?.nickname || "";
    editForm.signature = profile.value?.signature || "";
    editForm.gender = profile.value?.gender || 0;
    loadFav();
  } catch {
    alert("加载失败");
  }
});
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 24px;
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 40px;
}

.avatar {
  width: 64px;
  height: 64px;
  display: grid;
  place-items: center;
  font-size: 24px;
  font-weight: 700;
  color: #000000;
  background: #00e676;
  border-radius: 50%;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 24px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 4px 0;
}

.meta {
  font-size: 14px;
  color: #999999;
  margin: 0;
  display: flex;
  gap: 12px;
}

.btn-edit {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-edit:hover {
  border-color: #000000;
}

/* Tabs */
.tabs {
  display: flex;
  gap: 32px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 32px;
}

.tab {
  padding: 12px 0;
  font-size: 15px;
  font-weight: 500;
  color: #999999;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.tab:hover {
  color: #000000;
}

.tab.active {
  color: #000000;
  font-weight: 600;
  border-bottom-color: #00e676;
}

/* Content */
.tab-content {
  min-height: 300px;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  transition: all 0.2s;
}

.card:hover {
  border-color: #00e676;
}

.card-img {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
  background: #f5f5f5;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 4px 0;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-title:hover {
  color: #00c665;
}

.card-score {
  font-size: 13px;
  color: #000000;
  margin: 0 0 4px 0;
}

.card-content {
  font-size: 14px;
  color: #666666;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-time {
  font-size: 12px;
  color: #999999;
  margin: 0;
}

.btn-remove {
  padding: 6px 12px;
  font-size: 13px;
  color: #999999;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-remove:hover {
  color: #ff5252;
  border-color: #ff5252;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: grid;
  place-items: center;
  z-index: 1000;
}

.modal {
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  width: 100%;
  max-width: 460px;
}

.modal-title {
  font-size: 22px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 24px 0;
}

.form-group {
  margin-bottom: 20px;
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
  padding: 10px 14px;
  font-size: 14px;
  color: #000000;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}

.input:focus {
  border-color: #00e676;
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  justify-content: flex-end;
}

.btn-cancel {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #666666;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
}

.btn-save {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #000000;
  background: #00e676;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-save:hover:not(:disabled) {
  background: #00c665;
}
.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Loading / Empty */
.loading,
.empty,
.loading-page {
  text-align: center;
  padding: 60px 20px;
  color: #999999;
  font-size: 14px;
}
</style>
