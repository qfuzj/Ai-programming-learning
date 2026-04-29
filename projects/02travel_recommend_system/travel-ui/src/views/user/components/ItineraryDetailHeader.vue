<template>
  <div class="header-section">
    <el-page-header @back="emit('back')">
      <template #content>
        <div class="title-with-tags">
          <span class="text-large font-bold mr-3">{{ detail?.title || "行程详情" }}</span>
          <el-tag :type="detail?.status === 2 ? 'success' : 'info'" size="small">
            {{ findDictDesc(statusOptions, detail?.status, "-") }}
          </el-tag>
          <el-tag v-if="detail?.isPublic === 1" type="success" size="small">
            {{ findDictDesc(publicOptions, 1, "公开") }}
          </el-tag>
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
import { getPublicStatusDict, getTravelPlanStatusDict } from "@/api/dict";
import { findDictDesc, useDictOptions } from "@/composables/useDictOptions";

const { options: statusOptions } = useDictOptions("travel-plan-status", getTravelPlanStatusDict);
const { options: publicOptions } = useDictOptions("public-status", getPublicStatusDict);

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
  padding: 20px;
  margin-bottom: 20px;
  background: #fff;
  border-radius: 8px;
}

.title-with-tags {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
