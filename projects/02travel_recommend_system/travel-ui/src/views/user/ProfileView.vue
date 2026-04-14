<template>
  <div class="profile-container" v-loading="loading">
    <!-- 顶部个人资料头部 -->
    <div class="profile-header">
      <div class="profile-header-bg"></div>
      <div class="header-content">
        <div class="user-info-section">
          <el-avatar :size="120" :src="profile.avatar" class="profile-avatar">
            {{ (profile.nickname || profile.username || '旅行者').charAt(0) }}
          </el-avatar>
          <div class="user-text">
            <h1 class="nickname">{{ profile.nickname || profile.username || '旅行者' }}</h1>
            <p v-if="profile.username" class="username">@{{ profile.username }}</p>
          </div>
        </div>
        <div class="header-actions" v-if="profile.id">
          <el-button type="primary" plain round @click="editDialogVisible = true">编辑个人资料</el-button>
        </div>
      </div>

      <!-- 自定义选项卡导航 -->
      <div class="profile-tabs">
        <div v-for="tab in tabs" :key="tab.key" class="tab-item" :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key">
          {{ tab.label }}
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="profile-body">
      <el-row :gutter="24">
        <!-- 左侧侧边栏：简介与标签 -->
        <el-col :md="8" :sm="24">
          <div v-if="hasBioContent" class="sidebar-section">
            <h3 class="section-title">简介</h3>
            <div class="bio-card">
              <p v-if="profile.signature" class="signature">{{ profile.signature }}</p>
              <div class="info-list">
                <div v-if="profile.gender === 1 || profile.gender === 2" class="info-item">
                  <el-icon>
                    <User />
                  </el-icon>
                  <span>{{ profile.gender === 1 ? '男' : '女' }}</span>
                </div>
                <div v-if="profile.birthday" class="info-item">
                  <el-icon>
                    <Calendar />
                  </el-icon>
                  <span>{{ profile.birthday }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="sidebar-section">
            <div class="section-title-row">
              <h3 class="section-title">偏好标签</h3>
              <el-button link type="primary" @click="openTagDialog">
                {{ portrait?.preferredTags?.length ? '编辑' : '+ 添加' }}
              </el-button>
            </div>
            <div class="tags-container">
              <template v-if="portrait?.preferredTags?.length">
                <el-tag v-for="tag in portrait.preferredTags" :key="tag" class="pref-tag" effect="plain" round>
                  {{ tag }}
                </el-tag>
              </template>
              <span v-else class="text-muted">还没有偏好标签，点击添加</span>
            </div>
          </div>

          <div class="sidebar-section" v-if="portrait?.recentPreferences?.length">
            <h3 class="section-title">近期浏览偏好</h3>
            <div class="tags-container">
              <el-tag v-for="tag in portrait.recentPreferences" :key="tag" class="pref-tag" effect="plain" round>
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <el-dialog v-model="tagDialogVisible" title="编辑偏好标签" width="560px" destroy-on-close
            :close-on-click-modal="true">
            <div class="tag-group" v-for="group in tagGroups" :key="group.type">
              <div class="tag-group-title">{{ group.label }}</div>
              <div class="tag-group-list">
                <el-tag v-for="tag in group.tags" :key="tag.id" class="selectable-tag"
                  :effect="selectedTagIds.includes(tag.id) ? 'dark' : 'plain'" round
                  style="cursor: pointer; margin: 4px;" @click="toggleTag(tag.id)">
                  {{ tag.name }}
                </el-tag>
              </div>
            </div>
            <template #footer>
              <el-button @click="tagDialogVisible = false">取消</el-button>
              <el-button type="primary" :loading="tagSaving" style="background-color: #34e0a1; border-color: #34e0a1;"
                @click="handleSaveTags">
                保存（已选 {{ selectedTagIds.length }} 个）
              </el-button>
            </template>
          </el-dialog>
        </el-col>

        <!-- 右侧主内容：动态/内容展示 -->
        <el-col :md="16" :sm="24">
          <div class="main-content-section" v-loading="tabLoading">
            <div v-if="activeTab === 'favorites'" class="favorites-section">
              <div v-if="favoriteList.length > 0" class="favorites-list">
                <el-row :gutter="16">
                  <el-col :span="12" v-for="item in favoriteList" :key="item.scenicId">
                    <el-card class="history-card" shadow="hover" body-style="padding: 12px"
                      @click="goToScenic(item.scenicId)">
                      <div style="display: flex; gap: 12px; cursor: pointer;">
                        <el-image style="width: 80px; height: 80px; border-radius: 8px; flex-shrink: 0;"
                          :src="item.coverImage || 'https://via.placeholder.com/80'" fit="cover" />
                        <div
                          style="flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: space-between;">
                          <h4
                            style="margin: 0; font-size: 15px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                            {{ item.scenicName }}
                          </h4>
                          <p style="margin: 0; font-size: 12px; color: #999;">
                            收藏于：{{ item.favoriteTime ? item.favoriteTime.substring(0, 10) : '-' }}
                          </p>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
              <div v-else class="empty-state">
                <el-empty description="暂无收藏景区，去发现你喜欢的景点吧">
                  <el-button type="primary" @click="goToScenicList">去逛景点</el-button>
                </el-empty>
              </div>
            </div>


            <!-- 近期浏览区 -->
            <div v-if="activeTab === 'history'" class="history-section">
              <div v-if="historyList.length > 0" class="history-list">
                <el-row :gutter="16">
                  <el-col :span="12" v-for="item in historyList" :key="item.id">
                    <el-card class="history-card" shadow="hover" body-style="padding: 12px"
                      @click="goToScenic(item.scenicId)">
                      <div style="display: flex; gap: 12px; cursor: pointer;">
                        <el-image style="width: 80px; height: 80px; border-radius: 8px; flex-shrink: 0;"
                          :src="item.coverImage || 'https://via.placeholder.com/80'" fit="cover" />
                        <div
                          style="flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: space-between;">
                          <h4
                            style="margin: 0; font-size: 15px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                            {{ item.scenicName }}
                          </h4>
                          <p style="margin: 0; font-size: 12px; color: #999;">
                            浏览于：{{ item.browseTime ? item.browseTime.substring(0, 10) : '-' }}
                          </p>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
              <div v-else class="empty-state">
                <el-empty description="暂无浏览记录" />
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人资料" width="500px" destroy-on-close>
      <el-form :model="editForm" label-position="top">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-upload-wrapper" @click="triggerAvatarUpload">
            <el-avatar :size="80" :src="editForm.avatar" style="background-color: #34e0a1; color: #fff;">
              {{ (editForm.nickname || profile.username || '?').charAt(0) }}
            </el-avatar>
            <div class="avatar-upload-mask">
              <el-icon>
                <Camera />
              </el-icon>
              <span>更换头像</span>
            </div>
            <el-progress v-if="avatarUploading" type="circle" :percentage="avatarUploadProgress" :width="80"
              class="avatar-upload-progress" />
          </div>
          <input ref="avatarInputRef" type="file" accept="image/*" style="display: none"
            @change="handleAvatarFileChange" />
        </el-form-item>

        <el-divider content-position="left">个人简介</el-divider>
        <el-form-item label="个性签名">
          <el-input v-model="editForm.signature" type="textarea" :rows="3" placeholder="分享你的旅行态度，例如：走走停停，看遍山河"
            maxlength="100" show-word-limit />
        </el-form-item>

        <el-divider content-position="left">个人信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="editForm.gender" style="width: 100%">
                <el-option label="保密" :value="0" />
                <el-option label="男" :value="1" />
                <el-option label="女" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生日">
              <el-date-picker v-model="editForm.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer" style="display: flex; gap: 16px; justify-content: flex-end;">
          <el-button text @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" style="background-color: #34e0a1; border-color: #34e0a1;"
            @click="handleUpdateProfile">
            保存更改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { Location, Calendar, User, Compass, Wallet, InfoFilled, Camera } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store";
import {
  getProfileInfo,
  getProfilePortrait,
  updateProfileInfo,
  updatePreferenceTags,
  getMyPreferenceTags,
  type ProfileInfo,
  type ProfilePortraitSummary,
} from "@/api/profile";
import { getTags, type CommonTagItem } from "@/api/common";
import { getBrowseHistoryList, type BrowseHistoryItem } from "@/api/history";
import { getFavorites, type FavoriteItem } from "@/api/favorite";
import { getUploadToken, uploadCallback, getFileResource } from "@/api/file";

const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const saving = ref(false);
const editDialogVisible = ref(false);
const tagDialogVisible = ref(false);
const tagSaving = ref(false);
const allTags = ref<CommonTagItem[]>([]);
const selectedTagIds = ref<number[]>([]);
const activeTab = ref("favorites");
const tabLoading = ref(false);
const historyList = ref<BrowseHistoryItem[]>([]);
const favoriteList = ref<FavoriteItem[]>([]);
const avatarInputRef = ref<HTMLInputElement>();
const avatarUploading = ref(false);
const avatarUploadProgress = ref(0);

const tabs = [
  { label: "我的收藏", key: "favorites" },
  { label: "近期浏览", key: "history" },
];

const profile = reactive<ProfileInfo>({
  id: 0,
  username: "",
  nickname: "",
  avatar: "",
  signature: "",
  gender: 0,
  birthday: "",
  role: "USER",
});

const portrait = ref<ProfilePortraitSummary | null>(null);

const editForm = reactive({
  nickname: "",
  avatar: "",
  signature: "",
  gender: 0,
  birthday: "",
});

const hasBioContent = computed(() =>
  Boolean(
    profile.signature ||
    (profile.gender && profile.gender !== 0) ||
    profile.birthday
  )
);

const tagGroups = computed(() => {
  const groups = [
    { type: 1, label: "景点标签", tags: allTags.value.filter((t) => t.type === 1) },
    { type: 2, label: "偏好标签", tags: allTags.value.filter((t) => t.type === 2) },
  ];
  return groups.filter((g) => g.tags.length > 0);
});

async function loadProfileData(): Promise<void> {
  loading.value = true;
  try {
    const [profileResult, portraitResult] = await Promise.allSettled([
      getProfileInfo(),
      getProfilePortrait(),
    ]);

    if (profileResult.status === "fulfilled") {
      Object.assign(profile, profileResult.value);
      // 初始化编辑表单
      editForm.nickname = profileResult.value.nickname || "";
      editForm.avatar = profileResult.value.avatar || "";
      editForm.signature = profileResult.value.signature || "";
      editForm.gender = profileResult.value.gender || 0;
      editForm.birthday = profileResult.value.birthday || "";

      // 同步到全局用户态，确保右上角头像/昵称在资料更新后即时刷新
      if (userStore.profile) {
        userStore.profile = {
          ...userStore.profile,
          username: profileResult.value.username || userStore.profile.username,
          nickname: profileResult.value.nickname || userStore.profile.nickname,
          avatar: profileResult.value.avatar || "",
        };
      }
    } else {
      ElMessage.error("获取个人资料失败");
    }

    if (portraitResult.status === "fulfilled") {
      portrait.value = portraitResult.value;
    }
    // portrait 失败时静默处理，不提示用户
  } finally {
    loading.value = false;
  }
}

async function handleUpdateProfile(): Promise<void> {
  saving.value = true;
  try {
    await updateProfileInfo({
      nickname: editForm.nickname || undefined,
      avatar: editForm.avatar || undefined,
      signature: editForm.signature || undefined,
      gender: editForm.gender,
      birthday: editForm.birthday || undefined,
    });
    ElMessage.success("个人资料已更新");
    editDialogVisible.value = false;
    await loadProfileData();
  } catch (error) {
    ElMessage.error("保存失败，请检查数据");
  } finally {
    saving.value = false;
  }
}

function toggleTag(id: number): void {
  const index = selectedTagIds.value.indexOf(id);
  if (index === -1) {
    selectedTagIds.value.push(id);
  } else {
    selectedTagIds.value.splice(index, 1);
  }
}

async function openTagDialog(): Promise<void> {
  try {
    const [tags, myTags] = await Promise.all([getTags(), getMyPreferenceTags()]);
    allTags.value = tags;
    selectedTagIds.value = myTags.map((t: any) => Number(t.id));
  } catch {
    ElMessage.error("标签数据加载失败");
    return;
  }
  tagDialogVisible.value = true;
}

async function handleSaveTags(): Promise<void> {
  tagSaving.value = true;
  try {
    await updatePreferenceTags(selectedTagIds.value);
    ElMessage.success("偏好标签已更新");
    tagDialogVisible.value = false;
    const portraitResult = await getProfilePortrait();
    portrait.value = portraitResult;
  } catch {
    ElMessage.error("保存失败，请重试");
  } finally {
    tagSaving.value = false;
  }
}

function triggerAvatarUpload(): void {
  avatarInputRef.value?.click();
}

async function handleAvatarFileChange(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) {
    return;
  }

  if (!file.type.startsWith("image/")) {
    ElMessage.warning("请选择图片文件");
    input.value = "";
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning("图片大小不能超过 5MB");
    input.value = "";
    return;
  }

  avatarUploading.value = true;
  avatarUploadProgress.value = 0;

  try {
    const tokenRes = await getUploadToken({
      bizType: "avatar",
      fileName: file.name,
      fileSize: file.size,
    });

    const uploadUrl = String(tokenRes.uploadUrl || "");
    const bucketName = String(tokenRes.bucketName || "");
    const objectKey = String(tokenRes.objectKey || "");
    if (!uploadUrl || !bucketName || !objectKey) {
      throw new Error("上传凭证不完整");
    }

    const uploadResponse = await fetch(uploadUrl, {
      method: "PUT",
      body: file,
      headers: { "Content-Type": file.type || "application/octet-stream" },
    });
    if (!uploadResponse.ok) {
      throw new Error("上传失败");
    }
    avatarUploadProgress.value = 80;

    const fileId = await uploadCallback({
      bucketName,
      objectKey,
      originalName: file.name,
      bizType: "avatar",
    });
    avatarUploadProgress.value = 90;

    const fileResource = await getFileResource(fileId);
    editForm.avatar = fileResource.url || "";
    avatarUploadProgress.value = 100;
    ElMessage.success("头像上传成功");
  } catch (error) {
    ElMessage.error("头像上传失败，请重试");
  } finally {
    avatarUploading.value = false;
    input.value = "";
  }
}

async function loadHistoryData(): Promise<void> {
  tabLoading.value = true;
  try {
    const list = await getBrowseHistoryList({ pageNum: 1, pageSize: 20 });
    historyList.value = list;
  } catch (error) {
    ElMessage.error("获取浏览历史失败");
  } finally {
    tabLoading.value = false;
  }
}

async function loadFavoriteData(): Promise<void> {
  tabLoading.value = true;
  try {
    favoriteList.value = await getFavorites({ pageNum: 1, pageSize: 20 });
  } catch (error) {
    ElMessage.error("获取收藏列表失败");
  } finally {
    tabLoading.value = false;
  }
}

function goToScenic(id: number) {
  router.push(`/scenic/${id}`);
}

function goToScenicList(): void {
  router.push("/scenic");
}

watch(activeTab, (newTab) => {
  if (newTab === "favorites" && favoriteList.value.length === 0) {
    loadFavoriteData();
  }
  if (newTab === "history" && historyList.value.length === 0) {
    loadHistoryData();
  }
});

onMounted(() => {
  loadProfileData();
  loadFavoriteData();
});
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

/* 头部样式 */
.profile-header {
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  margin-top: 20px;
  border-radius: 12px 12px 0 0;
  overflow: hidden;
}

.profile-header-bg {
  height: 120px;
  background: linear-gradient(135deg, #34e0a1 0%, #00b4d8 100%);
}

.header-content {
  padding: 0 40px 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.user-info-section {
  display: flex;
  align-items: flex-end;
  gap: 24px;
}

.profile-avatar {
  margin-top: -60px;
  /* 头像上移叠在封面上 */
  border: 4px solid #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  background-color: #34e0a1 !important;
  color: #fff !important;
  font-size: 40px;
  z-index: 10;
}

.user-text {
  padding-bottom: 6px;
}

.user-text .nickname {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 4px;
  color: #000;
}

.user-text .username {
  color: #666;
  margin: 0;
  font-size: 16px;
}

.stats-row {
  display: flex;
  gap: 20px;
  color: #333;
  font-size: 14px;
}

.stats-row b {
  font-weight: 700;
}

/* 选项卡样式 */
.profile-tabs {
  display: flex;
  padding: 0 40px;
  gap: 32px;
}

.tab-item {
  padding: 16px 0;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
  font-size: 15px;
}

.tab-item:hover {
  color: #34e0a1;
}

.tab-item.active {
  color: #34e0a1;
  /* 品牌绿色 */
  border-bottom-color: #34e0a1;
}

/* 主体内容 */
.profile-body {
  margin-top: 24px;
  padding: 0px;
}

.sidebar-section {
  margin-bottom: 32px;
}

.section-title {
  margin-top: 0px;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #000;
}

.bio-card {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
}

.signature {
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 20px;
  white-space: pre-wrap;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #666;
  font-size: 14px;
}

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title-row .section-title {
  margin-bottom: 0;
}

.text-muted {
  color: #909399;
  font-size: 14px;
}

.tag-group {
  margin-bottom: 20px;
}

.tag-group-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid #f0f0f0;
}

.tag-group-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.selectable-tag {
  transition: all 0.2s;
}

.selectable-tag:hover {
  opacity: 0.8;
}

/* 勋章卡片 */
.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 16px;
}

.badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 8px;
  background: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #eee;
  transition: all 0.2s;
}

.badge-item:hover {
  transform: translateY(-2px);
  background: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.badge-icon {
  font-size: 24px;
}

.badge-label {
  font-size: 11px;
  font-weight: 600;
  text-align: center;
  color: #666;
}

.badge-item.platinum {
  border-top: 3px solid #e5e4e2;
}

.badge-item.gold {
  border-top: 3px solid #ffd700;
}

.badge-item.silver {
  border-top: 3px solid #c0c0c0;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
}

.pref-tag {
  color: #333;
  border-color: #dcdfe6;
}

.main-content-section {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  min-height: 500px;
  padding: 40px;
}

.avatar-upload-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-upload-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  color: #fff;
  font-size: 12px;
  gap: 4px;
}

.avatar-upload-wrapper:hover .avatar-upload-mask {
  opacity: 1;
}

.avatar-upload-progress {
  position: absolute;
  inset: 0;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    padding: 24px;
    gap: 20px;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions .el-button {
    width: 100%;
  }

  .profile-tabs {
    padding: 0 24px;
    overflow-x: auto;
    gap: 20px;
  }
}
</style>
