<!-- 管理工作台骨架：后续接入统计看板。 -->
<template>
  <div class="page-container">
    <el-card v-loading="loading" class="page-card">
      <template #header>工作台</template>
      <el-row :gutter="12">
        <el-col :span="8"><el-statistic title="用户总数" :value="overview.totalUsers" /></el-col>
        <el-col :span="8">
          <el-statistic title="景点总数" :value="overview.totalScenicSpots" />
        </el-col>
        <el-col :span="8"><el-statistic title="点评总数" :value="overview.totalReviews" /></el-col>
      </el-row>
      <el-row :gutter="12" style="margin-top: 12px">
        <el-col :span="8"><el-statistic title="行程总数" :value="overview.totalPlans" /></el-col>
        <el-col :span="8"><el-statistic title="LLM 调用" :value="overview.totalLlmCalls" /></el-col>
      </el-row>
      <el-divider />
      <el-table :data="hotRanking" style="width: 100%">
        <el-table-column prop="scenicName" label="热门景点" />
        <el-table-column prop="heatScore" label="热度分" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import {
  getDashboardOverview,
  getDashboardScenicHotRanking,
  type DashboardOverview,
  type ScenicHotRankingItem,
} from "@/api/dashboard";

const loading = ref(false);
const overview = reactive<DashboardOverview>({
  totalUsers: 0,
  totalScenicSpots: 0,
  totalReviews: 0,
  totalPlans: 0,
  totalLlmCalls: 0,
});
const hotRanking = ref<ScenicHotRankingItem[]>([]);

async function loadDashboardData(): Promise<void> {
  loading.value = true;
  try {
    const [overviewResult, hotRankingResult] = await Promise.all([
      getDashboardOverview(),
      getDashboardScenicHotRanking(),
    ]);
    Object.assign(overview, overviewResult);
    hotRanking.value = hotRankingResult;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadDashboardData();
});
</script>
