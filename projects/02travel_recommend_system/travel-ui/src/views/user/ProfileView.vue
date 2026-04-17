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
              <ScenicCardGrid
                :items="favoriteMapped"
                time-label="收藏于"
                empty-text="暂无收藏景区，去发现你喜欢的景点吧"
                @click="goToScenic"
              >
                <template #empty>
                  <el-empty description="暂无收藏景区，去发现你喜欢的景点吧">
                    <el-button type="primary" @click="goToScenicList">去逛景点</el-button>
                  </el-empty>
                </template>
              </ScenicCardGrid>
            </template>

            <template v-if="activeTab === 'reviews'">
              <ReviewListSection
                :reviews="reviews"
                :total="reviewTotal"
                :current-page="reviewCurrentPage"
                :page-size="reviewPageSize"
                :loading="tabLoading"
                @delete="handleDeleteReview"
                @page-change="handleReviewPageChange"
                @go-scenic="handleGoScenicFromReview"
              />
            </template>

            <template v-if="activeTab === 'history'">
              <ScenicCardGrid
                :items="historyMapped"
                time-label="浏览于"
                empty-text="暂无浏览记录"
                @click="goToScenic"
              />
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
} = useProfile();

const editDialogVisible = ref(false);
const tagDialogVisible = ref(false);
const activeTab = ref("favorites");
const reviews = ref<ReviewItem[]>([]);
const reviewTotal = ref(0);
const reviewCurrentPage = ref(1);
const reviewPageSize = ref(10);
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
    void loadFavorites();
  }
  if (newTab === "reviews" && !reviewsLoaded.value) {
    void loadMyReviews();
  }
  if (newTab === "history" && !historyLoaded.value) {
    historyLoaded.value = true;
    void loadHistory();
  }
});

onMounted(async () => {
  await loadProfile();
  favoritesLoaded.value = true;
  void loadFavorites();
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
  min-height: 500px;
  padding: 40px;
}
</style>
