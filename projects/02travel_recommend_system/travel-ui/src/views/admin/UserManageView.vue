<!-- 用户管理页：分页查询 + 启用禁用。 -->
<template>
  <div class="page-container">
    <el-card v-loading="loading" class="page-card">
      <template #header>用户管理</template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="userList">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="160" />
        <el-table-column prop="nickname" label="昵称" min-width="180" />
        <el-table-column label="状态" width="140">
          <template #default="scope">
            <el-switch
              :model-value="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(value) => onToggleStatus(scope.row.id, value as number)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态文案" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? "正常" : "禁用" }}
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
          @current-change="loadUsers"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { getAdminUserPage, updateAdminUserStatus, type AdminUserItem } from "@/api/user";

const loading = ref(false);
const total = ref(0);
const userList = ref<AdminUserItem[]>([]);

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: undefined as number | undefined,
});

async function loadUsers(): Promise<void> {
  loading.value = true;
  try {
    const page = await getAdminUserPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      status: query.status,
    });
    userList.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
}

function onSearch(): void {
  query.pageNum = 1;
  void loadUsers();
}

function onSizeChange(): void {
  query.pageNum = 1;
  void loadUsers();
}

async function onToggleStatus(id: number, status: number): Promise<void> {
  await updateAdminUserStatus(id, status);
  ElMessage.success("状态已更新");
  await loadUsers();
}

onMounted(() => {
  void loadUsers();
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
