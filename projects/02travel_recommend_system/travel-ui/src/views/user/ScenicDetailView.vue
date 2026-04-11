<!-- 景点详情骨架：后续接入详情、点评、相关推荐。 -->
<template>
  <div class="page-container">
    <el-card v-loading="loading" class="page-card">
      <template #header>景点详情</template>
      <el-empty v-if="!detail" description="暂无景点数据" />
      <template v-else>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="景点ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="名称">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="地区">{{ detail.regionName }}</el-descriptions-item>
          <el-descriptions-item label="评分">{{ detail.score }}</el-descriptions-item>
          <el-descriptions-item label="等级">{{ detail.level || '-' }}</el-descriptions-item>
          <el-descriptions-item label="门票">{{ detail.ticketPrice ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="开放时间">{{ detail.openTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ detail.address || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <el-image v-if="detail.coverImage" :src="detail.coverImage" fit="cover" class="cover-image" />
        <el-alert v-if="detail.intro" :title="detail.intro" type="info" :closable="false" show-icon />
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { getScenicDetail, type ScenicDetail } from "@/api/scenic";
import { reportBrowseHistory } from "@/api/history";

const route = useRoute();
const scenicId = computed(() => Number(route.params.id));
const loading = ref(false);
const detail = ref<ScenicDetail | null>(null);

async function loadDetail(id: number): Promise<void> {
  if (!id) {
    detail.value = null;
    return;
  }

  loading.value = true;
  try {
    const scenicDetail = await getScenicDetail(id);
    detail.value = scenicDetail;
    void reportBrowseHistory({ scenicId: scenicDetail.id, source: "scenic_detail" });
  } finally {
    loading.value = false;
  }
}

watch(
  scenicId,
  (id) => {
    void loadDetail(id);
  },
  { immediate: true }
);
</script>

<style scoped>
.cover-image {
  width: 100%;
  max-height: 360px;
  margin-bottom: 16px;
  border-radius: 12px;
}
</style>
