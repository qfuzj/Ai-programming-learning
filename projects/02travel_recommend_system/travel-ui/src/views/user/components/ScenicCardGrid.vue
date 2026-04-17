<template>
  <div v-if="items.length > 0">
    <el-row :gutter="16">
      <el-col v-for="item in items" :key="item.id" :span="12">
        <el-card
          class="scenic-card"
          shadow="hover"
          body-style="padding: 12px"
          @click="emit('click', item.scenicId)"
        >
          <div class="card-inner">
            <el-image class="card-image" :src="item.coverImage || DEFAULT_IMAGE" fit="cover" />
            <div class="card-info">
              <h4 class="card-title">{{ item.scenicName }}</h4>
              <p class="card-meta">{{ timeLabel }}：{{ formatDate(item.time) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
  <div v-else class="empty-state">
    <slot name="empty">
      <el-empty :description="emptyText" />
    </slot>
  </div>
</template>

<script setup lang="ts">
const DEFAULT_IMAGE = "https://via.placeholder.com/80";

export interface ScenicCardItem {
  id: number;
  scenicId: number;
  scenicName: string;
  coverImage?: string;
  time: string | null;
}

interface Props {
  items: ScenicCardItem[];
  timeLabel: string;
  emptyText?: string;
}

withDefaults(defineProps<Props>(), {
  emptyText: "暂无数据",
});

const emit = defineEmits<{
  click: [scenicId: number];
}>();

function formatDate(time: string | null): string {
  return time ? time.substring(0, 10) : "-";
}
</script>

<style scoped>
.card-inner {
  display: flex;
  gap: 12px;
  cursor: pointer;
}

.card-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  flex-shrink: 0;
}

.card-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-title {
  margin: 0;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.scenic-card {
  cursor: pointer;
  margin-bottom: 16px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
</style>
