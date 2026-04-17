<template>
  <div class="scenic-list-page">
    <ScenicListFilterBar
      :query="query"
      :loading="loading"
      :region-tree-data="regionTreeData"
      :category-options="categoryOptions"
      :level-options="levelOptions"
      :selected-province-id="selectedProvinceId"
      :selected-city-id="selectedCityId"
      :current-cities="currentCities"
      :min-score-options="minScoreOptions"
      @update:selected-province-id="selectedProvinceId = $event"
      @update:selected-city-id="selectedCityId = $event"
      @province-change="onProvinceChange"
      @city-change="onCityChange"
      @search="debouncedLoadScenicList"
      @reset="resetFilters"
    />

    <ScenicListBanner
      :is-search-active="isSearchActive"
      :keyword="query.keyword || ''"
      :banners="banners"
      @update:keyword="query.keyword = $event"
      @search="loadScenicList"
    />

    <div class="main-content">
      <div class="section-header">
        <h2 class="section-title">{{ isSearchActive ? "搜索结果" : "为您推荐" }}</h2>
        <p v-if="!isSearchActive" class="section-subtitle">
          正在浏览 {{ getCurrentRegionName() || "热门精选" }}？我们认为您会喜欢这些。
        </p>
        <p v-else class="section-subtitle">
          共找到
          <strong>{{ total }}</strong>
          个符合条件的景点
        </p>
      </div>

      <div v-loading="loading" class="scenic-grid">
        <ScenicListCard
          v-for="(item, index) in scenicList"
          :key="item.id"
          :item="item"
          :rank="index + 1 + (query.pageNum - 1) * query.pageSize"
          @detail="goDetail"
          @toggle-favorite="toggleFavorite"
        />
      </div>

      <el-empty v-if="!loading && scenicList.length === 0" description="未找到符合条件的景点" />

      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[8, 12, 24, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadScenicList"
          @size-change="loadScenicList"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import type { ScenicItem } from "@/api/scenic";
import { useScenicList } from "@/composables/useScenicList";
import ScenicListFilterBar from "./components/ScenicListFilterBar.vue";
import ScenicListBanner from "./components/ScenicListBanner.vue";
import ScenicListCard from "./components/ScenicListCard.vue";

const router = useRouter();

const {
  loading,
  scenicList,
  total,
  regionTreeData,
  categoryOptions,
  levelOptions,
  selectedProvinceId,
  selectedCityId,
  currentCities,
  minScoreOptions,
  query,
  banners,
  isSearchActive,
  onProvinceChange,
  onCityChange,
  getCurrentRegionName,
  loadScenicList,
  debouncedLoadScenicList,
  toggleFavorite,
  resetFilters,
} = useScenicList();

function goDetail(row: ScenicItem): void {
  void router.push(`/scenic/${row.id}`);
}
</script>

<style scoped>
.scenic-list-page {
  background-color: #fafafa;
  min-height: 100vh;
  padding-bottom: 60px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 28px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.section-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.scenic-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

@media (max-width: 1024px) {
  .scenic-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .scenic-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .scenic-grid {
    grid-template-columns: 1fr;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
}
</style>
