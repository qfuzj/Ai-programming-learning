<template>
  <div class="admin-page">
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
      <el-form-item class="form-actions">
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-shell">
      <el-table v-loading="loading" :data="list" border stripe class="user-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户" min-width="220">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="40" :src="row.avatar || ''" class="user-avatar">
                {{ avatarFallback(row) }}
              </el-avatar>
              <div class="user-meta">
                <el-tooltip
                  :content="row.username || '-'"
                  placement="top"
                  :disabled="!(row.username && row.username.length > 12)"
                >
                  <div class="user-name ellipsis">{{ row.username || "-" }}</div>
                </el-tooltip>
                <el-tooltip
                  :content="row.nickname || '-'"
                  placement="top"
                  :disabled="!(row.nickname && row.nickname.length > 12)"
                >
                  <div class="user-nick ellipsis">{{ row.nickname || "-" }}</div>
                </el-tooltip>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ dictText(genderOptions, row.gender) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ dictText(commonStatusOptions, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="170" />
        <el-table-column fixed="right" label="操作" width="160">
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
    </div>

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
      <template v-if="detail">
        <div class="detail-header">
          <el-avatar :size="72" :src="detail.avatar || ''" class="detail-avatar">
            {{ avatarFallback(detail) }}
          </el-avatar>
          <div class="detail-header-meta">
            <div class="detail-name">{{ detail.nickname || detail.username || "未命名用户" }}</div>
            <div class="detail-sub">@{{ detail.username || "-" }}</div>
            <el-tag
              :type="detail.status === 1 ? 'success' : 'info'"
              size="small"
              class="detail-status"
            >
              {{ dictText(commonStatusOptions, detail.status) }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="1" border class="detail-descriptions">
          <el-descriptions-item label="用户ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.phone || "-" }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detail.email || "-" }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ dictText(genderOptions, detail.gender) }}
          </el-descriptions-item>
          <el-descriptions-item label="生日">{{ detail.birthday || "-" }}</el-descriptions-item>
          <el-descriptions-item label="地区">
            {{ detail.regionName || detail.regionId || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="最后登录">
            {{ detail.lastLoginTime || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="登录IP">
            {{ detail.lastLoginIp || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ detail.createdAt || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ detail.updatedAt || "-" }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
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

function avatarFallback(user: AdminUserItem | null): string {
  if (!user) return "?";
  const source = user.nickname || user.username || "";
  return source ? source.trim().charAt(0).toUpperCase() : "?";
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
  width: 100%;
  min-width: 0;
  overflow: hidden;
}

.filter-panel {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  gap: 0 14px;
  align-items: center;
  padding: 16px 16px 0;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 8px;
}

.filter-panel :deep(.el-form-item) {
  margin-right: 0;
  margin-bottom: 16px;
}

.filter-panel :deep(.el-input),
.filter-panel :deep(.el-select) {
  width: 200px;
}

.filter-panel .form-actions {
  margin-left: auto;
}

.table-shell {
  width: 100%;
  max-width: 100%;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 8px;
}

.user-table {
  width: 100%;
}

.user-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}

.user-avatar {
  flex-shrink: 0;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #5b8def 0%, #08d878 100%);
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  max-width: 220px;
  overflow: hidden;
}

.user-name {
  font-weight: 700;
  color: #101828;
}

.user-nick {
  font-size: 12px;
  color: #667085;
}

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-header {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 16px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #f5f9ff 0%, #f0fff6 100%);
  border: 1px solid #e7eaf0;
  border-radius: 12px;
}

.detail-avatar {
  flex-shrink: 0;
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #5b8def 0%, #08d878 100%);
  box-shadow: 0 4px 12px rgb(91 141 239 / 25%);
}

.detail-header-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.detail-name {
  overflow: hidden;
  font-size: 18px;
  font-weight: 700;
  color: #101828;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-sub {
  font-size: 13px;
  color: #667085;
}

.detail-status {
  align-self: flex-start;
  margin-top: 4px;
}

.detail-descriptions :deep(.el-descriptions__label) {
  width: 100px;
  color: #667085;
}

.pager {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 980px) {
  .filter-panel :deep(.el-input),
  .filter-panel :deep(.el-select) {
    width: 100%;
  }

  .filter-panel :deep(.el-form-item) {
    width: 100%;
  }

  .user-meta {
    max-width: 140px;
  }
}
</style>
