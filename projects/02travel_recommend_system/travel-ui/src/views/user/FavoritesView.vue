<template>
  <div class="favorites-container">
    <div class="favorites-header">
      <div>
        <h1 class="favorites-title">我的收藏</h1>
        <p class="favorites-subtitle">集中查看已收藏景点，并继续回到详情页。</p>
      </div>
      <el-button type="primary" plain @click="goToScenicList">去发现更多景点</el-button>
    </div>

    <div v-loading="loading" class="favorites-content">
      <ScenicCardGrid
        :items="favoriteMapped"
        time-label="收藏于"
        empty-text="暂无收藏景区，去发现你喜欢的景点吧"
        @click="goToScenic"
      >
        <template #empty>
          <el-empty description="暂无收藏景区，去发现你喜欢的景点吧">
            <el-button type="primary" @click="goToScenicList">去逛景点</el-button>
          </el-empty>
        </template>
      </ScenicCardGrid>

      <div v-if="page.total > page.pageSize" class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :current-page="page.pageNum"
          :page-size="page.pageSize"
          :total="page.total"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import ScenicCardGrid, { type ScenicCardItem } from "./components/ScenicCardGrid.vue";
import { getFavoritesPage, type FavoriteItem } from "@/api/favorite";

const router = useRouter();

const loading = ref(false);
const favorites = ref<FavoriteItem[]>([]);
const page = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0,
});

const favoriteMapped = computed<ScenicCardItem[]>(() =>
  favorites.value.map((item) => ({
    id: item.scenicId,
    scenicId: item.scenicId,
    scenicName: item.scenicName,
    coverImage: item.coverImage,
    time: item.favoriteTime ?? null,
  }))
);

async function loadFavorites(): Promise<void> {
  loading.value = true;
  try {
    const result = await getFavoritesPage({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
    });
    favorites.value = result.records ?? [];
    page.total = result.total ?? 0;
  } catch {
    ElMessage.error("获取收藏列表失败");
  } finally {
    loading.value = false;
  }
}

function handlePageChange(nextPage: number): void {
  page.pageNum = nextPage;
  void loadFavorites();
}

function goToScenic(scenicId: number): void {
  void router.push(`/scenic/${scenicId}`);
}

function goToScenicList(): void {
  void router.push("/scenic");
}

onMounted(() => {
  void loadFavorites();
});
</script>

<style scoped>
.favorites-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px 40px;
}

.favorites-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.favorites-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.favorites-subtitle {
  margin: 8px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.favorites-content {
  min-height: 320px;
  padding: 24px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .favorites-header {
    align-items: stretch;
    flex-direction: column;
  }

  .favorites-content {
    padding: 16px;
  }
}
</style>
