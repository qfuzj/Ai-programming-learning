<!-- 操作日志页：基础检索与分页。 -->
<template>
  <div class="page-container">
    <el-card v-loading="loading" class="page-card">
      <template #header>操作日志</template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="模块">
          <el-input v-model="query.module" clearable placeholder="如 scenic" />
        </el-form-item>
        <el-form-item label="动作">
          <el-input v-model="query.action" clearable placeholder="如 update" />
        </el-form-item>
        <el-form-item label="管理员">
          <el-input v-model="query.adminUsername" clearable placeholder="管理员用户名" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logList">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="action" label="动作" width="120" />
        <el-table-column prop="adminUsername" label="管理员" width="140" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="requestUrl" label="请求地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="executionTimeMs" label="耗时(ms)" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? "成功" : "失败" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadLogs"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { getOperationLogPage, type OperationLogItem } from "@/api/admin-log";

const loading = ref(false);
const total = ref(0);
const logList = ref<OperationLogItem[]>([]);

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  module: "",
  action: "",
  adminUsername: "",
  status: undefined as number | undefined,
});

async function loadLogs(): Promise<void> {
  loading.value = true;
  try {
    const page = await getOperationLogPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      module: query.module || undefined,
      action: query.action || undefined,
      adminUsername: query.adminUsername || undefined,
      status: query.status,
    });
    logList.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
}

function onSearch(): void {
  query.pageNum = 1;
  void loadLogs();
}

function onSizeChange(): void {
  query.pageNum = 1;
  void loadLogs();
}

onMounted(() => {
  void loadLogs();
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