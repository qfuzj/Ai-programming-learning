<template>
  <div class="sidebar">
    <div v-if="hasBioContent" class="sidebar-section">
      <h3 class="section-title">简介</h3>
      <div class="bio-card">
        <p v-if="profile.signature" class="signature">{{ profile.signature }}</p>
        <div class="info-list">
          <div v-if="showGender" class="info-item">
            <el-icon><User /></el-icon>
            <span>{{ profile.gender === 1 ? "男" : "女" }}</span>
          </div>
          <div v-if="profile.birthday" class="info-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ profile.birthday }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="sidebar-section">
      <div class="section-title-row">
        <h3 class="section-title">偏好标签</h3>
        <el-button link type="primary" @click="emit('open-tag-dialog')">
          {{ portrait?.preferredTags?.length ? "编辑" : "+ 添加" }}
        </el-button>
      </div>
      <div class="tags-container">
        <template v-if="portrait?.preferredTags?.length">
          <el-tag
            v-for="tag in portrait.preferredTags"
            :key="tag"
            class="pref-tag"
            effect="plain"
            round
          >
            {{ tag }}
          </el-tag>
        </template>
        <span v-else class="text-muted">还没有偏好标签，点击添加</span>
      </div>
    </div>

    <div v-if="portrait?.recentPreferences?.length" class="sidebar-section">
      <h3 class="section-title">近期浏览偏好</h3>
      <div class="tags-container">
        <el-tag
          v-for="tag in portrait.recentPreferences"
          :key="tag"
          class="pref-tag"
          effect="plain"
          round
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { User, Calendar } from "@element-plus/icons-vue";
import type { ProfileInfo, ProfilePortraitSummary } from "@/types/profile";

interface Props {
  profile: ProfileInfo;
  portrait: ProfilePortraitSummary | null;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  "open-tag-dialog": [];
}>();

const showGender = computed(() => props.profile.gender === 1 || props.profile.gender === 2);

const hasBioContent = computed(() =>
  Boolean(props.profile.signature || showGender.value || props.profile.birthday)
);
</script>

<style scoped>
.sidebar-section {
  margin-bottom: 32px;
}

.section-title {
  margin-top: 0;
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
</style>
