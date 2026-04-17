<template>
  <div class="profile-header">
    <div class="profile-header-bg" />
    <div class="header-content">
      <div class="user-info-section">
        <el-avatar :size="120" :src="profile.avatar" class="profile-avatar">
          {{ displayName.charAt(0) }}
        </el-avatar>
        <div class="user-text">
          <h1 class="nickname">{{ displayName }}</h1>
          <p v-if="profile.username" class="username">@{{ profile.username }}</p>
        </div>
      </div>
      <div v-if="profile.id" class="header-actions">
        <el-button type="primary" plain round @click="emit('edit')">编辑个人资料</el-button>
      </div>
    </div>

    <div class="profile-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="emit('tab-change', tab.key)"
      >
        {{ tab.label }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { ProfileInfo, TabItem } from "@/types/profile";

interface Props {
  profile: ProfileInfo;
  tabs: TabItem[];
  activeTab: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  edit: [];
  "tab-change": [key: string];
}>();

const displayName = computed(() => props.profile.nickname || props.profile.username || "旅行者");
</script>

<style scoped>
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
  border-bottom-color: #34e0a1;
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
