<template>
  <div class="toolbar-wrap">
    <div class="card-header">
      <span>我的行程</span>
      <el-button type="primary" @click="emit('create')">
        <el-icon><Plus /></el-icon>
        新建行程
      </el-button>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="query" @submit.prevent="emit('search')">
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="搜行程标题或描述"
            clearable
            style="width: 220px"
            @clear="emit('search')"
            @keyup.enter="emit('search')"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="query.status"
            placeholder="全部状态"
            clearable
            style="width: 120px"
            @change="emit('search')"
          >
            <el-option label="草稿" :value="1" />
            <el-option label="已发布" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="是否公开">
          <el-select
            v-model="query.isPublic"
            placeholder="全部分类"
            clearable
            style="width: 120px"
            @change="emit('search')"
          >
            <el-option label="私有" :value="0" />
            <el-option label="公开" :value="1" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading">查询</el-button>
          <el-button @click="emit('reset')">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus } from "@element-plus/icons-vue";
import type { ItineraryQuery } from "@/api/itinerary";

interface Props {
  query: ItineraryQuery;
  loading: boolean;
}

defineProps<Props>();

const emit = defineEmits<{
  create: [];
  search: [];
  reset: [];
}>();
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.search-bar {
  margin-top: 16px;
  margin-bottom: 4px;
}
</style>
