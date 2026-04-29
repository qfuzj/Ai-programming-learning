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
  margin-top: 20px;
  overflow: hidden;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  border-radius: 12px 12px 0 0;
}

.profile-header-bg {
  height: 120px;
  background: linear-gradient(135deg, #34e0a1 0%, #00b4d8 100%);
}

.header-content {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 0 40px 20px;
}

.user-info-section {
  display: flex;
  gap: 24px;
  align-items: flex-end;
}

.profile-avatar {
  z-index: 10;
  margin-top: -60px;
  font-size: 40px;
  color: #fff !important;
  background-color: #34e0a1 !important;
  border: 4px solid #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.user-text {
  padding-bottom: 6px;
}

.user-text .nickname {
  margin: 0 0 4px;
  font-size: 32px;
  font-weight: 700;
  color: #000;
}

.user-text .username {
  margin: 0;
  font-size: 16px;
  color: #666;
}

.profile-tabs {
  display: flex;
  gap: 32px;
  padding: 0 40px;
}

.tab-item {
  padding: 16px 0;
  font-size: 15px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
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
    gap: 20px;
    align-items: flex-start;
    padding: 24px;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions .el-button {
    width: 100%;
  }

  .profile-tabs {
    gap: 20px;
    padding: 0 24px;
    overflow-x: auto;
  }
}
</style>
