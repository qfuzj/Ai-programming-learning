<template>
  <div class="profile-container">
    <!-- 顶部个人资料头部 -->
    <div class="profile-header">
      <div class="header-content">
        <div class="user-info-section">
          <el-avatar :size="120" :src="profile.avatar" class="profile-avatar">
            {{ (profile.nickname || profile.username || '旅行者').charAt(0) }}
          </el-avatar>
          <div class="user-text">
            <h1 class="nickname">{{ profile.nickname || profile.username || '旅行者' }}</h1>
            <p class="username">@{{ profile.username }}</p>
            <div class="stats-row">
              <span class="stat-item"><b>12</b> 贡献</span>
              <span class="stat-item"><b>45</b> 粉丝</span>
              <span class="stat-item"><b>89</b> 正在关注</span>
            </div>
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
        <!-- 左侧侧边栏：成就与简介 -->
        <el-col :md="8" :sm="24">
          <div class="sidebar-section">
            <h3 class="section-title">简介</h3>
            <div class="bio-card">
              <p class="signature">{{ profile.signature || '填个旅行宣言，让大家了解你' }}</p>
              <div class="info-list">
                <div class="info-item">
                  <el-icon>
                    <Location />
                  </el-icon>
                  <span>{{ portrait?.location || '未知目的地' }}</span>
                </div>
                <div class="info-item">
                  <el-icon>
                    <Calendar />
                  </el-icon>
                  <span>2024年4月加入</span>
                </div>
              </div>
            </div>
          </div>

          <div class="sidebar-section">
            <h3 class="section-title">成就</h3>
            <div class="achievements-grid">
              <div class="badge-item platinum">
                <div class="badge-icon">🏛️</div>
                <span class="badge-label">文化学者</span>
              </div>
              <div class="badge-item gold">
                <div class="badge-icon">📷</div>
                <span class="badge-label">摄影达人</span>
              </div>
              <div class="badge-item silver">
                <div class="badge-icon">🍜</div>
                <span class="badge-label">美食家</span>
              </div>
            </div>
          </div>

          <div class="sidebar-section" v-if="portrait?.preferredTags?.length">
            <h3 class="section-title">偏好标签</h3>
            <div class="tags-container">
              <el-tag v-for="tag in portrait.preferredTags" :key="tag" class="pref-tag" effect="plain" round>
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </el-col>

        <!-- 右侧主内容：动态/内容展示 -->
        <el-col :md="16" :sm="24">
          <div class="main-content-section" v-loading="loading">
            <div v-if="activeTab === 'timeline'" class="empty-state">
              <el-empty description="暂无新动态，开始分享你的旅行吧">
                <el-button type="primary">发布点评</el-button>
              </el-empty>
            </div>

            <div v-if="activeTab === 'photos'" class="empty-state">
              <el-empty description="暂无照片" />
            </div>

            <div v-if="activeTab === 'reviews'" class="empty-state">
              <el-empty description="暂无点评" />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人资料" width="500px" destroy-on-close>
      <el-form :model="editForm" label-position="top">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="头像地址">
          <el-input v-model="editForm.avatar" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input v-model="editForm.signature" type="textarea" :rows="3" placeholder="写点什么介绍下自己..." maxlength="100"
            show-word-limit />
        </el-form-item>
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
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleUpdateProfile">
            保存更改
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { Location, Calendar } from "@element-plus/icons-vue";
import {
  getProfileInfo,
  getProfilePortrait,
  updateProfileInfo,
  type ProfileInfo,
  type ProfilePortraitSummary,
} from "@/api/profile";

const loading = ref(false);
const saving = ref(false);
const editDialogVisible = ref(false);
const activeTab = ref("timeline");

const tabs = [
  { label: "时间线", key: "timeline" },
  { label: "照片", key: "photos" },
  { label: "点评", key: "reviews" },
  { label: "旅行贴士", key: "tips" },
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

async function loadProfileData(): Promise<void> {
  loading.value = true;
  try {
    const [profileResult, portraitResult] = await Promise.all([
      getProfileInfo(),
      getProfilePortrait(),
    ]);
    Object.assign(profile, profileResult);
    portrait.value = portraitResult;

    // 初始化编辑表单
    editForm.nickname = profileResult.nickname || "";
    editForm.avatar = profileResult.avatar || "";
    editForm.signature = profileResult.signature || "";
    editForm.gender = profileResult.gender || 0;
    editForm.birthday = profileResult.birthday || "";
  } catch (error) {
    ElMessage.error("获取个人资料失败");
  } finally {
    loading.value = false;
  }
}

async function handleUpdateProfile(): Promise<void> {
  saving.value = true;
  try {
    await updateProfileInfo({
      nickname: editForm.nickname,
      avatar: editForm.avatar,
      signature: editForm.signature,
      gender: editForm.gender,
      birthday: editForm.birthday,
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

onMounted(() => {
  loadProfileData();
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
}

.header-content {
  padding: 40px 40px 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.user-info-section {
  display: flex;
  align-items: center;
  gap: 24px;
}

.profile-avatar {
  border: 4px solid #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  background-color: #34e0a1 !important;
  color: #fff !important;
  font-size: 40px;
}

.user-text .nickname {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 4px;
  color: #000;
}

.user-text .username {
  color: #666;
  margin: 0 0 12px;
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
  color: #000;
}

.tab-item.active {
  color: #000;
  border-bottom-color: #000;
}

/* 主体内容 */
.profile-body {
  margin-top: 24px;
}

.sidebar-section {
  margin-bottom: 32px;
}

.section-title {
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
