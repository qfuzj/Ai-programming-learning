<template>
  <div v-loading="loading" class="profile-container">
    <ProfileHeader
      :profile="profile"
      :tabs="TABS"
      :active-tab="activeTab"
      @edit="editDialogVisible = true"
      @tab-change="handleTabChange"
    />

    <div class="profile-body">
      <el-row :gutter="24">
        <el-col :md="8" :sm="24">
          <ProfileSidebar
            :profile="profile"
            :portrait="portrait"
            @open-tag-dialog="handleOpenTagDialog"
          />
        </el-col>

        <el-col :md="16" :sm="24">
          <div v-loading="tabLoading" class="main-content-section">
            <template v-if="activeTab === 'favorites'">
              <div class="tab-panel">
                <div class="tab-actions">
                  <el-button
                    type="danger"
                    link
                    :disabled="favoriteTotal === 0"
                    @click="handleClearFavorites"
                  >
                    删除全部收藏
                  </el-button>
                </div>
                <ScenicCardGrid
                  :items="favoriteMapped"
                  time-label="收藏于"
                  empty-text="暂无收藏景区，去发现你喜欢的景点吧"
                  show-remove
                  @click="goToScenic"
                  @remove="handleDeleteFavorite"
                >
                  <template #empty>
                    <el-empty description="暂无收藏景区，去发现你喜欢的景点吧">
                      <el-button type="primary" @click="goToScenicList">去逛景点</el-button>
                    </el-empty>
                  </template>
                </ScenicCardGrid>
                <div v-if="favoriteTotal > favoritePageSize" class="tab-pagination">
                  <el-pagination
                    :current-page="favoritePageNum"
                    :page-size="favoritePageSize"
                    :total="favoriteTotal"
                    background
                    layout="prev, pager, next"
                    @current-change="handleFavoritePageChange"
                  />
                </div>
              </div>
            </template>

            <template v-if="activeTab === 'reviews'">
              <div class="tab-panel">
                <ReviewListSection
                  :reviews="reviews"
                  :total="reviewTotal"
                  :current-page="reviewCurrentPage"
                  :page-size="reviewPageSize"
                  :loading="tabLoading"
                  compact
                  @delete="handleDeleteReview"
                  @page-change="handleReviewPageChange"
                  @go-scenic="handleGoScenicFromReview"
                />
              </div>
            </template>

            <template v-if="activeTab === 'history'">
              <div class="tab-panel">
                <div class="tab-actions">
                  <el-button
                    type="danger"
                    link
                    :disabled="historyTotal === 0"
                    @click="handleClearHistory"
                  >
                    删除全部近期浏览
                  </el-button>
                </div>
                <ScenicCardGrid
                  :items="historyMapped"
                  time-label="浏览于"
                  empty-text="暂无浏览记录"
                  show-remove
                  @click="goToScenic"
                  @remove="handleDeleteHistory"
                />
                <div v-if="historyTotal > historyPageSize" class="tab-pagination">
                  <el-pagination
                    :current-page="historyPageNum"
                    :page-size="historyPageSize"
                    :total="historyTotal"
                    background
                    layout="prev, pager, next"
                    @current-change="handleHistoryPageChange"
                  />
                </div>
              </div>
            </template>
          </div>
        </el-col>
      </el-row>
    </div>

    <EditProfileDialog
      v-model="editDialogVisible"
      :profile="profile"
      :saving="saving"
      @submit="handleSaveProfile"
    />

    <TagDialog
      v-model="tagDialogVisible"
      :all-tags="allTags"
      :initial-selected-ids="selectedTagIds"
      :saving="tagSaving"
      @save="handleSaveTags"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import ProfileHeader from "./components/ProfileHeader.vue";
import ProfileSidebar from "./components/ProfileSidebar.vue";
import EditProfileDialog from "./components/EditProfileDialog.vue";
import TagDialog from "./components/TagDialog.vue";
import ScenicCardGrid from "./components/ScenicCardGrid.vue";
import ReviewListSection from "./components/ReviewListSection.vue";
import { useProfile } from "@/composables/useProfile";
import { deleteMyReview, getMyReviews, type ReviewItem } from "@/api/audit";
import type { ScenicCardItem } from "./components/ScenicCardGrid.vue";
import type { TabItem, UpdateProfilePayload } from "@/types/profile";

const TABS: TabItem[] = [
  { label: "我的收藏", key: "favorites" },
  { label: "我的点评", key: "reviews" },
  { label: "近期浏览", key: "history" },
];

const {
  loading,
  saving,
  profile,
  portrait,
  historyList,
  favoriteList,
  historyTotal,
  favoriteTotal,
  historyPageNum,
  favoritePageNum,
  historyPageSize,
  favoritePageSize,
  tabLoading,
  allTags,
  selectedTagIds,
  tagSaving,
  loadProfile,
  saveProfile,
  loadHistory,
  loadFavorites,
  openTagDialog,
  saveTags,
  goToScenic,
  goToScenicList,
  removeHistoryItem,
  clearHistoryItems,
  removeFavoriteItem,
  clearFavoriteItems,
} = useProfile();

const editDialogVisible = ref(false);
const tagDialogVisible = ref(false);
const activeTab = ref("favorites");
const reviews = ref<ReviewItem[]>([]);
const reviewTotal = ref(0);
const reviewCurrentPage = ref(1);
const reviewPageSize = ref(3);
const favoritesLoaded = ref(false);
const reviewsLoaded = ref(false);
const historyLoaded = ref(false);

const favoriteMapped = computed<ScenicCardItem[]>(() =>
  favoriteList.value.map((item) => ({
    id: item.scenicId,
    scenicId: item.scenicId,
    scenicName: item.scenicName,
    coverImage: item.coverImage,
    time: item.favoriteTime ?? null,
  }))
);

const historyMapped = computed<ScenicCardItem[]>(() =>
  historyList.value.map((item) => ({
    id: item.id,
    scenicId: item.scenicId,
    scenicName: item.scenicName,
    coverImage: item.coverImage,
    time: item.browseTime ?? null,
  }))
);

function handleTabChange(key: string): void {
  activeTab.value = key;
}

async function loadMyReviews(): Promise<void> {
  tabLoading.value = true;
  try {
    const page = await getMyReviews({
      pageNum: reviewCurrentPage.value,
      pageSize: reviewPageSize.value,
    });
    reviews.value = page.records ?? [];
    reviewTotal.value = page.total ?? 0;
    reviewsLoaded.value = true;
  } catch {
    ElMessage.error("获取点评列表失败");
  } finally {
    tabLoading.value = false;
  }
}

async function handleDeleteReview(id: number): Promise<void> {
  try {
    await ElMessageBox.confirm("确定要删除这条点评吗？删除后不可恢复", "提示", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
    });
    await deleteMyReview(id);
    ElMessage.success("删除成功");
    if (reviews.value.length === 1 && reviewCurrentPage.value > 1) {
      reviewCurrentPage.value -= 1;
    }
    await loadMyReviews();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除点评失败");
    }
  }
}

function handleReviewPageChange(page: number): void {
  reviewCurrentPage.value = page;
  void loadMyReviews();
}

function handleFavoritePageChange(page: number): void {
  void loadFavorites(page, favoritePageSize.value);
}

function handleHistoryPageChange(page: number): void {
  void loadHistory(page, historyPageSize.value);
}

async function handleDeleteHistory(id: number): Promise<void> {
  try {
    await ElMessageBox.confirm("确定删除这条浏览记录吗？", "提示", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
    });
    const success = await removeHistoryItem(id);
    if (success) {
      ElMessage.success("删除成功");
    }
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("删除浏览记录失败");
    }
  }
}

async function handleClearHistory(): Promise<void> {
  try {
    await ElMessageBox.confirm("确定清空全部近期浏览吗？此操作不可恢复。", "提示", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
    });
    const success = await clearHistoryItems();
    if (success) {
      historyLoaded.value = true;
      ElMessage.success("已清空近期浏览");
    }
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("清空近期浏览失败");
    }
  }
}

async function handleDeleteFavorite(scenicId: number): Promise<void> {
  try {
    await ElMessageBox.confirm("确定要取消收藏该景点吗？", "提示", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
    });
    const success = await removeFavoriteItem(scenicId);
    if (success) {
      ElMessage.success("已取消收藏");
    }
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("取消收藏失败");
    }
  }
}

async function handleClearFavorites(): Promise<void> {
  try {
    await ElMessageBox.confirm("确定清空全部收藏吗？此操作不可恢复。", "提示", {
      type: "warning",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
    });
    const success = await clearFavoriteItems();
    if (success) {
      favoritesLoaded.value = true;
      ElMessage.success("已清空全部收藏");
    }
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error("清空收藏失败");
    }
  }
}

function handleGoScenicFromReview(id: number | undefined): void {
  if (id) {
    goToScenic(id);
  }
}

async function handleSaveProfile(payload: UpdateProfilePayload): Promise<void> {
  const success = await saveProfile(payload);
  if (success) editDialogVisible.value = false;
}

async function handleOpenTagDialog(): Promise<void> {
  const success = await openTagDialog();
  if (success) tagDialogVisible.value = true;
}

async function handleSaveTags(ids: number[]): Promise<void> {
  const success = await saveTags(ids);
  if (success) tagDialogVisible.value = false;
}

watch(activeTab, (newTab) => {
  if (newTab === "favorites" && !favoritesLoaded.value) {
    favoritesLoaded.value = true;
    void loadFavorites(1, favoritePageSize.value);
  }
  if (newTab === "reviews" && !reviewsLoaded.value) {
    void loadMyReviews();
  }
  if (newTab === "history" && !historyLoaded.value) {
    historyLoaded.value = true;
    void loadHistory(1, historyPageSize.value);
  }
});

onMounted(async () => {
  await loadProfile();
  favoritesLoaded.value = true;
  void loadFavorites(1, favoritePageSize.value);
});
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.profile-body {
  margin-top: 24px;
}

.main-content-section {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  height: 560px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  padding: 28px;
}

.tab-panel {
  display: flex;
  flex: 1;
  flex-direction: column;
  height: 100%;
}

.tab-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.tab-pagination {
  display: flex;
  margin-top: auto;
  justify-content: flex-end;
  padding-top: 12px;
}
</style>
