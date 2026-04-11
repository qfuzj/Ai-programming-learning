<!-- 景点管理骨架：后续联调管理端景点 CRUD。 -->
<template>
  <div class="page-container">
    <el-card v-loading="loading" class="page-card">
      <template #header>景点管理</template>
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="景点名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadScenicList">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">新增景点</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="scenicList" style="margin-top: 12px">
        <el-table-column prop="name" label="景点名" />
        <el-table-column prop="regionName" label="地区" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadScenicList"
          @size-change="loadScenicList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { getAdminScenicPage, type ScenicItem, type ScenicQuery } from "@/api/scenic";

const loading = ref(false);
const scenicList = ref<ScenicItem[]>([]);
const total = ref(0);

const query = reactive<ScenicQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
});

async function loadScenicList(): Promise<void> {
  loading.value = true;
  try {
    const result = await getAdminScenicPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
    });
    scenicList.value = result.records;
    total.value = result.total;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadScenicList();
});
</script>

<style scoped>
.filter-form {
  margin-bottom: 12px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
