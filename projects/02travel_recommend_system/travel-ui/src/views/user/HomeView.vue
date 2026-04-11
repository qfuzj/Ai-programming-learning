<!-- 用户首页骨架：后续可接入首页推荐和 banner。 -->
<template>
  <div class="page-container">
    <el-card class="page-card hero-card">
      <template #header>
        <div class="hero-header">
          <span>首页</span>
          <el-button text type="primary" @click="router.push('/scenic')">进入景点列表</el-button>
        </div>
      </template>
      <el-row :gutter="16" class="hero-grid">
        <el-col :xs="24" :sm="14">
          <div class="hero-copy">
            <h2>发现更适合你的旅行目的地</h2>
            <p>先把热门景点和推荐入口接起来，再逐步补全收藏、行程和 AI 能力。</p>
          </div>
        </el-col>
        <el-col :xs="24" :sm="10">
          <el-skeleton v-if="loading" animated :rows="6" />
          <el-empty v-else-if="hotScenicList.length === 0" description="暂无热门景点" />
          <el-space v-else direction="vertical" fill>
            <el-card
              v-for="item in hotScenicList"
              :key="item.id"
              class="hot-card"
              shadow="hover"
              @click="goScenicDetail(item.id)"
            >
              <div class="hot-card-body">
                <el-image :src="item.coverImage || ''" fit="cover" class="hot-cover">
                  <template #error>
                    <div class="hot-cover-placeholder">暂无图片</div>
                  </template>
                </el-image>
                <div class="hot-card-content">
                  <div class="hot-title">{{ item.name }}</div>
                  <div class="hot-meta">{{ item.regionName }} · {{ item.score.toFixed(1) }} 分</div>
                </div>
              </div>
            </el-card>
          </el-space>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { getScenicHotList, type ScenicItem } from "@/api/scenic";

const router = useRouter();
const loading = ref(false);
const hotScenicList = ref<ScenicItem[]>([]);

async function loadHomeData(): Promise<void> {
  loading.value = true;
  try {
    hotScenicList.value = await getScenicHotList();
  } finally {
    loading.value = false;
  }
}

function goScenicDetail(id: number): void {
  void router.push(`/scenic/${id}`);
}

onMounted(() => {
  void loadHomeData();
});
</script>

<style scoped>
.hero-card :deep(.el-card__header) {
  padding-bottom: 8px;
}

.hero-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-copy {
  min-height: 220px;
  padding: 12px 8px 12px 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.hero-copy h2 {
  margin: 0 0 12px;
  font-size: 32px;
  line-height: 1.2;
}

.hero-copy p {
  margin: 0;
  color: #5f6470;
}

.hero-grid {
  align-items: stretch;
}

.hot-card {
  cursor: pointer;
}

.hot-card-body {
  display: flex;
  gap: 12px;
  align-items: center;
}

.hot-cover {
  width: 72px;
  height: 72px;
  border-radius: 10px;
  overflow: hidden;
  flex: none;
}

.hot-cover-placeholder {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  background: #eef2ff;
  color: #4c5bd4;
  font-size: 12px;
}

.hot-title {
  font-weight: 600;
}

.hot-meta {
  margin-top: 6px;
  color: #8a8f99;
  font-size: 13px;
}
</style>
