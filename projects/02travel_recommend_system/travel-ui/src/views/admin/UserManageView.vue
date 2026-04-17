<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">用户管理</div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            clearable
            style="width: 220px"
            placeholder="用户名/昵称/手机号"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="userList">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar">
              {{ scope.row.nickname?.charAt(0) || "U" }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column label="性别" width="80">
          <template #default="scope">
            {{ formatGender(scope.row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="birthday" label="生日" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="lastLoginTime" label="最后登录" width="180" />
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? "正常" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row.id)">详情</el-button>
            <el-popconfirm
              v-if="scope.row.status === 0"
              title="确认启用该用户吗？"
              confirm-button-text="确认"
              cancel-button-text="取消"
              @confirm="onToggleStatus(scope.row, 1)"
            >
              <template #reference>
                <el-button link type="success">启用</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm
              v-if="scope.row.status === 1"
              title="确认禁用该用户吗？"
              confirm-button-text="确认"
              cancel-button-text="取消"
              @confirm="onToggleStatus(scope.row, 0)"
            >
              <template #reference>
                <el-button link type="danger">禁用</el-button>
              </template>
            </el-popconfirm>
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
          @current-change="loadUsers"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="用户详情" width="760px" destroy-on-close>
      <div v-if="detailData" class="detail-container">
        <div class="user-avatar-section">
          <el-avatar :size="80" :src="detailData.avatar">
            {{ detailData.nickname?.charAt(0) || "U" }}
          </el-avatar>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ detailData.username || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailData.nickname || "-" }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ formatGender(detailData.gender) }}
          </el-descriptions-item>
          <el-descriptions-item label="生日">{{ detailData.birthday || "-" }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailData.phone || "-" }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detailData.email || "-" }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ detailData.status === 1 ? "正常" : "禁用" }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ detailData.createdAt || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="最后登录" :span="2">
            {{ detailData.lastLoginTime || "-" }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  getAdminUserDetail,
  getAdminUserPage,
  updateAdminUserStatus,
  type AdminUserItem,
  type AdminUserQuery,
} from "@/api/user-admin";

const loading = ref(false);
const total = ref(0);
const userList = ref<AdminUserItem[]>([]);
const detailVisible = ref(false);
const detailData = ref<AdminUserItem | null>(null);

const query = reactive<AdminUserQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  status: undefined,
});

function formatGender(gender: number | undefined): string {
  if (gender === 1) return "男";
  if (gender === 2) return "女";
  return "未知";
}

async function loadUsers(): Promise<void> {
  loading.value = true;
  try {
    const page = await getAdminUserPage({
      ...query,
      keyword: query.keyword || undefined,
    });
    userList.value = page.records;
    total.value = page.total;
  } catch {
    ElMessage.error("用户列表加载失败");
  } finally {
    loading.value = false;
  }
}

function onSearch(): void {
  query.pageNum = 1;
  void loadUsers();
}

function onReset(): void {
  query.keyword = "";
  query.status = undefined;
  query.pageNum = 1;
  void loadUsers();
}

function onSizeChange(): void {
  query.pageNum = 1;
  void loadUsers();
}

async function onToggleStatus(row: AdminUserItem, status: number): Promise<void> {
  try {
    await updateAdminUserStatus(row.id, status);
    ElMessage.success("状态已更新");
    await loadUsers();
  } catch {
    ElMessage.error("状态更新失败");
  }
}

async function openDetail(id: number): Promise<void> {
  try {
    detailData.value = await getAdminUserDetail(id);
    detailVisible.value = true;
  } catch {
    ElMessage.error("用户详情加载失败");
  }
}

onMounted(() => {
  void loadUsers();
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

.detail-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.user-avatar-section {
  margin-bottom: 24px;
}
</style>
