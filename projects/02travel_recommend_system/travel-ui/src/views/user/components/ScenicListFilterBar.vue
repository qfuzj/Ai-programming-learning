<template>
  <div class="toolbar-section">
    <div class="toolbar-container">
      <el-form
        :model="query"
        class="filter-form"
        label-position="right"
        label-width="70px"
        @submit.prevent
      >
        <el-row :gutter="24">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="关键词" class="form-item">
              <el-input
                v-model="query.keyword"
                placeholder="景点名/地区/简介"
                clearable
                @keyup.enter="emit('search')"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="省份" class="form-item">
              <el-select
                v-model="provinceModel"
                placeholder="全部省份"
                clearable
                @change="emit('province-change')"
              >
                <el-option
                  v-for="prov in regionTreeData"
                  :key="prov.id"
                  :label="prov.name"
                  :value="prov.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="城市" class="form-item">
              <el-select
                v-model="cityModel"
                placeholder="全部城市"
                clearable
                :disabled="!selectedProvinceId"
                @change="emit('city-change')"
              >
                <el-option
                  v-for="city in currentCities"
                  :key="city.id"
                  :label="city.name"
                  :value="city.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="分类" class="form-item">
              <el-select v-model="query.category" placeholder="全部分类" clearable>
                <el-option
                  v-for="category in categoryOptions"
                  :key="category"
                  :label="category"
                  :value="category"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="等级" class="form-item">
              <el-select v-model="query.level" placeholder="全部等级" clearable>
                <el-option
                  v-for="level in levelOptions"
                  :key="level"
                  :label="level"
                  :value="level"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="最低评分" class="form-item">
              <el-select v-model="query.minScore" placeholder="不限评分" clearable>
                <el-option
                  v-for="score in minScoreOptions"
                  :key="score"
                  :label="`${score} 分以上`"
                  :value="score"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="排序" class="form-item">
              <el-select v-model="query.sortBy" placeholder="默认排序" clearable>
                <el-option label="热门排序" value="hot" />
                <el-option label="评分排序" value="score" />
                <el-option label="最新发布" value="createdAt" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="24" :md="8" :lg="6" class="action-col">
            <div class="action-buttons">
              <el-button class="reset-btn" @click="emit('reset')">重 置</el-button>
              <el-button
                type="primary"
                :loading="loading"
                class="search-btn"
                @click="emit('search')"
              >
                <el-icon class="el-icon--left"><Search /></el-icon>
                查 询
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { Search } from "@element-plus/icons-vue";
import type { ScenicQuery } from "@/api/scenic";
import type { ScenicRegionNode } from "@/types/scenic-list";

interface Props {
  query: ScenicQuery;
  loading: boolean;
  regionTreeData: ScenicRegionNode[];
  categoryOptions: string[];
  levelOptions: string[];
  selectedProvinceId?: number;
  selectedCityId?: number;
  currentCities: ScenicRegionNode[];
  minScoreOptions: number[];
}

const props = defineProps<Props>();
const emit = defineEmits<{
  search: [];
  reset: [];
  "update:selectedProvinceId": [value: number | undefined];
  "update:selectedCityId": [value: number | undefined];
  "province-change": [];
  "city-change": [];
}>();

const provinceModel = computed({
  get: () => props.selectedProvinceId,
  set: (value: number | undefined) => emit("update:selectedProvinceId", value),
});

const cityModel = computed({
  get: () => props.selectedCityId,
  set: (value: number | undefined) => emit("update:selectedCityId", value),
});
</script>

<style scoped>
.toolbar-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 20px 0;
}

.toolbar-container {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px 24px 4px 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f2f5;
  transition: all 0.3s ease;
}

.toolbar-container:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
}

.filter-form {
  width: 100%;
}

.form-item {
  width: 100%;
  margin-bottom: 20px;
}

.form-item :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.form-item :deep(.el-input__wrapper),
.form-item :deep(.el-select__wrapper) {
  box-shadow: 0 0 0 1px #e4e7ed inset;
  border-radius: 8px;
  transition: all 0.2s;
}

.form-item :deep(.el-input__wrapper:hover),
.form-item :deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.form-item :deep(.el-input__wrapper.is-focus),
.form-item :deep(.el-select__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

.action-col {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.search-btn,
.reset-btn {
  padding: 0 24px;
  height: 32px;
  border-radius: 8px;
  font-weight: 500;
  letter-spacing: 1px;
}

.search-btn {
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.search-btn:hover {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transform: translateY(-1px);
}

.reset-btn {
  border-color: #dcdfe6;
  color: #606266;
}

.reset-btn:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-7);
  background-color: var(--el-color-primary-light-9);
}

@media (max-width: 768px) {
  .toolbar-section {
    padding: 12px 16px 0;
  }
  .toolbar-container {
    padding: 16px 16px 0 16px;
  }
  .action-col {
    justify-content: center;
  }
  .action-buttons {
    width: 100%;
    justify-content: space-between;
  }
  .search-btn,
  .reset-btn {
    flex: 1;
  }
}
</style>
