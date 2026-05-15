<template>
  <div class="admin-page">
    <el-form :model="query" class="filter-panel" inline>
      <el-form-item label="管理员">
        <el-input
          v-model="query.adminUsername"
          clearable
          placeholder="管理员用户名"
          @keyup.enter="search"
        />
      </el-form-item>
      <el-form-item label="模块" style="width: 150px">
        <el-select v-model="query.module" clearable placeholder="全部模块">
          <el-option
            v-for="item in moduleOptions"
            :key="item.code"
            :label="item.desc"
            :value="String(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="动作">
        <el-input
          v-model="query.action"
          clearable
          placeholder="create / update / delete"
          @keyup.enter="search"
        />
      </el-form-item>
      <el-form-item label="状态" style="width: 150px">
        <el-select v-model="query.status" clearable placeholder="全部状态">
          <el-option
            v-for="item in statusOptions"
            :key="item.code"
            :label="item.desc"
            :value="Number(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="form-actions">
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="adminUsername" label="管理员" min-width="130" />
      <el-table-column label="模块" min-width="130">
        <template #default="{ row }">{{ dictText(moduleOptions, row.module) }}</template>
      </el-table-column>
      <el-table-column prop="action" label="动作" min-width="110" />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="requestUrl" label="请求地址" min-width="220" show-overflow-tooltip />
      <el-table-column prop="executionTimeMs" label="耗时(ms)" width="110" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ dictText(statusOptions, row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" min-width="170" />
      <el-table-column fixed="right" label="操作" width="90">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="loadData"
        @size-change="search"
      />
    </div>

    <el-drawer v-model="detailVisible" title="操作日志详情" size="680px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="日志ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="管理员">
          {{ detail.adminUsername || detail.adminUserId || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="模块">
          {{ dictText(moduleOptions, detail.module) }}
        </el-descriptions-item>
        <el-descriptions-item label="动作">{{ detail.action || "-" }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ detail.description || "-" }}</el-descriptions-item>
        <el-descriptions-item label="方法">{{ detail.requestMethod || "-" }}</el-descriptions-item>
        <el-descriptions-item label="URL">{{ detail.requestUrl || "-" }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.ipAddress || "-" }}</el-descriptions-item>
        <el-descriptions-item label="User-Agent">
          {{ detail.userAgent || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="耗时">
          {{ detail.executionTimeMs ?? "-" }} ms
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ dictText(statusOptions, detail.status) }}
        </el-descriptions-item>
        <el-descriptions-item label="时间">{{ detail.createdAt || "-" }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <pre class="json-box">{{ formatJson(detail.requestParams) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应数据">
          <pre class="json-box">{{ formatJson(detail.responseData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息">
          {{ detail.errorMessage || "-" }}
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { getOperationLogModuleDict, getOperationLogStatusDict, type DictItem } from "@/api/dict";
import {
  getOperationLogDetail,
  getOperationLogPage,
  type OperationLogDetailItem,
  type OperationLogListItem,
  type OperationLogQuery,
} from "@/api/log";
import { findDictDesc } from "@/composables/useDictOptions";

const loading = ref(false);
const detailVisible = ref(false);
const list = ref<OperationLogListItem[]>([]);
const detail = ref<OperationLogDetailItem | null>(null);
const total = ref(0);
const moduleOptions = ref<DictItem[]>([]);
const statusOptions = ref<DictItem[]>([]);

const query = reactive<OperationLogQuery>({
  pageNum: 1,
  pageSize: 10,
  module: undefined,
  action: "",
  status: undefined,
  adminUsername: "",
});

function dictText(options: DictItem[], code: unknown): string {
  return findDictDesc(options, code, "-");
}

async function loadDictionaries(): Promise<void> {
  [moduleOptions.value, statusOptions.value] = await Promise.all([
    getOperationLogModuleDict(),
    getOperationLogStatusDict(),
  ]);
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getOperationLogPage({
      ...query,
      action: query.action?.trim() || undefined,
      adminUsername: query.adminUsername?.trim() || undefined,
    });
    list.value = res.records || [];
    total.value = res.total || 0;
  } finally {
    loading.value = false;
  }
}

function search(): void {
  query.pageNum = 1;
  void loadData();
}

function resetQuery(): void {
  query.pageNum = 1;
  query.module = undefined;
  query.action = "";
  query.status = undefined;
  query.adminUsername = "";
  void loadData();
}

async function openDetail(id: number): Promise<void> {
  detailVisible.value = true;
  detail.value = await getOperationLogDetail(id);
}

function formatJson(value?: string): string {
  if (!value) return "-";
  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
}

onMounted(async () => {
  await loadDictionaries();
  await loadData();
});
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.page-head h1 {
  margin: 0;
  font-size: 28px;
  color: #101828;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  color: #4f7cff;
  letter-spacing: 0.08em;
}

.filter-panel {
  padding: 16px 16px 0;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 8px;
}

.pager {
  display: flex;
  justify-content: flex-end;
}

.json-box {
  max-height: 220px;
  padding: 10px;
  margin: 0;
  overflow: auto;
  font-size: 12px;
  line-height: 1.6;
  background: #f6f8fb;
  border-radius: 6px;
}

.filter-panel {
  position: relative;
}
</style>
