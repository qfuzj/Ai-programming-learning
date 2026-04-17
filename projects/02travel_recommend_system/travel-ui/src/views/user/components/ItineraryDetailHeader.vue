<template>
  <div class="header-section">
    <el-page-header @back="emit('back')">
      <template #content>
        <div class="title-with-tags">
          <span class="text-large font-bold mr-3">{{ detail?.title || "行程详情" }}</span>
          <el-tag v-if="detail?.status === 2" type="success" size="small">已发布</el-tag>
          <el-tag v-else type="info" size="small">草稿</el-tag>
          <el-tag v-if="detail?.isPublic === 1" type="success" size="small">公开</el-tag>
        </div>
      </template>
    </el-page-header>

    <el-descriptions :column="3" border style="margin-top: 20px">
      <el-descriptions-item label="出发日期">{{ detail?.startDate || "-" }}</el-descriptions-item>
      <el-descriptions-item label="结束日期">{{ detail?.endDate || "-" }}</el-descriptions-item>
      <el-descriptions-item label="总天数">{{ detail?.totalDays || 1 }} 天</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ detail?.createdAt || "-" }}</el-descriptions-item>
      <el-descriptions-item label="描述" :span="2">
        {{ detail?.description || "暂无描述" }}
      </el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup lang="ts">
import type { ItineraryItem } from "@/api/itinerary";

interface Props {
  detail: ItineraryItem | null;
}

defineProps<Props>();

const emit = defineEmits<{
  back: [];
}>();
</script>

<style scoped>
.header-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.title-with-tags {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
