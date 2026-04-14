<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">操作日志</div>
      </template>

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
        <el-form-item>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="logList">
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
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row.id)">详情</el-button>
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
          @current-change="loadLogs"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="日志详情" width="860px" destroy-on-close>
      <el-descriptions v-if="detailData" :column="2" border>
        <el-descriptions-item label="日志ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="管理员">{{ detailData.adminUsername || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ detailData.module || '-' }}</el-descriptions-item>
        <el-descriptions-item label="动作">{{ detailData.action || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ detailData.requestMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求地址">{{ detailData.requestUrl || '-' }}</el-descriptions-item>
        <el-descriptions-item label="耗时(ms)">{{ detailData.executionTimeMs ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailData.status === 1 ? '成功' : '失败' }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ detailData.ipAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">请求参数</el-divider>
      <el-input :model-value="detailData?.requestParams || '-'" type="textarea" :rows="4" readonly />

      <el-divider content-position="left">响应数据</el-divider>
      <el-input :model-value="detailData?.responseData || '-'" type="textarea" :rows="4" readonly />

      <el-divider content-position="left">错误信息</el-divider>
      <el-input :model-value="detailData?.errorMessage || '-'" type="textarea" :rows="3" readonly />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  getOperationLogDetail,
  getOperationLogPage,
  type OperationLogItem,
  type OperationLogQuery,
} from "@/api/log";

const loading = ref(false);
const total = ref(0);
const logList = ref<OperationLogItem[]>([]);
const detailVisible = ref(false);
const detailData = ref<OperationLogItem | null>(null);

const query = reactive<OperationLogQuery>({
  pageNum: 1,
  pageSize: 10,
  module: "",
  action: "",
  adminUsername: "",
  status: undefined,
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
  } catch {
    ElMessage.error("操作日志加载失败");
  } finally {
    loading.value = false;
  }
}

function onSearch(): void {
  query.pageNum = 1;
  void loadLogs();
}

function onReset(): void {
  query.module = "";
  query.action = "";
  query.adminUsername = "";
  query.status = undefined;
  query.pageNum = 1;
  void loadLogs();
}

function onSizeChange(): void {
  query.pageNum = 1;
  void loadLogs();
}

async function openDetail(id: number): Promise<void> {
  try {
    detailData.value = await getOperationLogDetail(id);
    detailVisible.value = true;
  } catch {
    ElMessage.error("日志详情加载失败");
  }
}

onMounted(() => {
  void loadLogs();
});
</script>

<style scoped>
.card-header {
  font-size: 16px;
  font-weight: 700;
}

.filter-form {
  margin-bottom: 12px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.page-container {
  padding: 0px;
}
</style>
