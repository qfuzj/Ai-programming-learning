<template>
  <div class="admin-page">
    <div class="page-head">
      <div>
        <p class="eyebrow">USER OPS</p>
        <h1>用户管理</h1>
      </div>
    </div>

    <el-form :model="query" class="filter-panel" inline>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="用户名 / 昵称 / 手机号"
          @keyup.enter="loadData"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部状态">
          <el-option
            v-for="item in commonStatusOptions"
            :key="item.code"
            :label="item.desc"
            :value="Number(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="username" label="用户名" min-width="130" />
      <el-table-column prop="nickname" label="昵称" min-width="130" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机号" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column label="性别" width="90">
        <template #default="{ row }">{{ dictText(genderOptions, row.gender) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ dictText(commonStatusOptions, row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录" min-width="170" />
      <el-table-column fixed="right" label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
          <el-button
            link
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? "禁用" : "启用" }}
          </el-button>
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

    <el-drawer v-model="detailVisible" title="用户详情" size="520px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="用户ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detail.username || "-" }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detail.nickname || "-" }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone || "-" }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detail.email || "-" }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ dictText(genderOptions, detail.gender) }}
        </el-descriptions-item>
        <el-descriptions-item label="生日">{{ detail.birthday || "-" }}</el-descriptions-item>
        <el-descriptions-item label="地区">
          {{ detail.regionName || detail.regionId || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ dictText(commonStatusOptions, detail.status) }}
        </el-descriptions-item>
        <el-descriptions-item label="最后登录">
          {{ detail.lastLoginTime || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="登录IP">{{ detail.lastLoginIp || "-" }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdAt || "-" }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updatedAt || "-" }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { getCommonStatusDict, getGenderDict, type DictItem } from "@/api/dict";
import {
  getAdminUserDetail,
  getAdminUserPage,
  updateAdminUserStatus,
  type AdminUserItem,
  type AdminUserQuery,
} from "@/api/user-admin";
import { findDictDesc } from "@/composables/useDictOptions";

const loading = ref(false);
const list = ref<AdminUserItem[]>([]);
const total = ref(0);
const detailVisible = ref(false);
const detail = ref<AdminUserItem | null>(null);
const commonStatusOptions = ref<DictItem[]>([]);
const genderOptions = ref<DictItem[]>([]);

const query = reactive<AdminUserQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  status: undefined,
});

function dictText(options: DictItem[], code: unknown): string {
  return findDictDesc(options, code, "-");
}

async function loadDictionaries(): Promise<void> {
  [commonStatusOptions.value, genderOptions.value] = await Promise.all([
    getCommonStatusDict(),
    getGenderDict(),
  ]);
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getAdminUserPage({
      ...query,
      keyword: query.keyword?.trim() || undefined,
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
  query.keyword = "";
  query.status = undefined;
  void loadData();
}

async function openDetail(id: number): Promise<void> {
  detailVisible.value = true;
  detail.value = await getAdminUserDetail(id);
}

async function toggleStatus(row: AdminUserItem): Promise<void> {
  const nextStatus = row.status === 1 ? 0 : 1;
  await ElMessageBox.confirm(
    `确认${nextStatus === 1 ? "启用" : "禁用"}用户「${row.username || row.id}」？`,
    "状态变更",
    {
      type: "warning",
    }
  );
  await updateAdminUserStatus(row.id, nextStatus);
  ElMessage.success("状态已更新");
  await loadData();
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

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
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
</style>
