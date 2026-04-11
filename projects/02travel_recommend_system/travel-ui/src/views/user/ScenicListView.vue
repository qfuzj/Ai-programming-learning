<!-- 景点列表骨架：后续联调分页查询与筛选。 -->
<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>景点列表</template>
      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="景点名/地区" clearable />
        </el-form-item>
        <el-form-item label="地区">
          <el-select v-model="query.regionId" placeholder="全部" clearable style="width: 180px">
            <el-option
              v-for="region in regionOptions"
              :key="region.id"
              :label="region.name"
              :value="region.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="query.sortBy" placeholder="默认" clearable style="width: 140px">
            <el-option label="热门" value="hot" />
            <el-option label="评分" value="score" />
            <el-option label="创建时间" value="createTime" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadScenicList">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="scenicList" v-loading="loading" @row-click="goDetail">
        <el-table-column prop="name" label="景点名" />
        <el-table-column prop="regionName" label="地区" />
        <el-table-column prop="score" label="评分" width="100" />
        <el-table-column prop="level" label="等级" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" text @click.stop="goDetail(scope.row)">详情</el-button>
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
import { useRouter } from "vue-router";
import { getRegionTree } from "@/api/common";
import { getScenicPage, type ScenicItem, type ScenicQuery } from "@/api/scenic";

const router = useRouter();
const loading = ref(false);
const scenicList = ref<ScenicItem[]>([]);
const total = ref(0);
const regionOptions = ref<Array<{ id: number; name: string }>>([]);

const query = reactive<ScenicQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  regionId: undefined,
  sortBy: "hot",
});

async function loadRegions(): Promise<void> {
  const regions = await getRegionTree();
  const flatten: Array<{ id: number; name: string }> = [];
  const walk = (nodes: typeof regions): void => {
    nodes.forEach((node) => {
      flatten.push({ id: node.id, name: node.name });
      if (node.children?.length) {
        walk(node.children);
      }
    });
  };
  walk(regions);
  regionOptions.value = flatten;
}

async function loadScenicList(): Promise<void> {
  loading.value = true;
  try {
    const result = await getScenicPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      regionId: query.regionId,
      sortBy: query.sortBy,
    });
    scenicList.value = result.records;
    total.value = result.total;
  } finally {
    loading.value = false;
  }
}

function goDetail(row: ScenicItem): void {
  void router.push(`/scenic/${row.id}`);
}

onMounted(async () => {
  await Promise.all([loadRegions(), loadScenicList()]);
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
