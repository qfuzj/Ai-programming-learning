<!-- 极简风格用户管理页 -->
<template>
  <div class="page">
    <h1 class="title">用户管理</h1>

    <div class="filter-bar">
      <input
        v-model="query.keyword"
        class="filter-input"
        placeholder="搜索用户名/手机号..."
        @keyup.enter="loadData"
      />
      <select v-model="query.status" class="filter-select" @change="loadData">
        <option :value="undefined">全部状态</option>
        <option :value="1">启用</option>
        <option :value="0">禁用</option>
      </select>
      <button class="btn-search" @click="loadData">查询</button>
      <button class="btn-reset" @click="resetQuery">重置</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="list.length === 0" class="empty">暂无用户数据</div>

    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>手机号</th>
            <th>性别</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.username }}</td>
            <td>{{ item.nickname || "-" }}</td>
            <td>{{ item.phone || "-" }}</td>
            <td>{{ item.gender === 1 ? "男" : item.gender === 2 ? "女" : "未知" }}</td>
            <td>
              <span class="badge" :class="item.status === 1 ? 'on' : 'off'">
                {{ item.status === 1 ? "启用" : "禁用" }}
              </span>
            </td>
            <td>
              <span class="link" @click="toggleStatus(item)">
                {{ item.status === 1 ? "禁用" : "启用" }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import {
  getAdminUserPage as getUserPage,
  updateAdminUserStatus as updateUserStatus,
} from "@/api/user-admin";

const loading = ref(false);
const list = ref<any[]>([]);

const query = reactive({
  keyword: "",
  status: undefined as number | undefined,
});

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getUserPage({
      pageNum: 1,
      pageSize: 50,
      keyword: query.keyword || undefined,
    });
    list.value = res.records || [];
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

function resetQuery(): void {
  query.keyword = "";
  query.status = undefined;
  loadData();
}

async function toggleStatus(item: any): Promise<void> {
  const newStatus = item.status === 1 ? 0 : 1;
  if (!confirm(`确认${newStatus === 1 ? "启用" : "禁用"}该用户？`)) return;
  try {
    await updateUserStatus(item.id, newStatus);
    loadData();
  } catch {
    alert("操作失败");
  }
}

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.page {
  max-width: 1200px;
  padding: 40px 24px;
  margin: 0 auto;
}
.title {
  margin: 0 0 32px 0;
  font-size: 28px;
  font-weight: 700;
  color: #000;
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}
.filter-input {
  flex: 1;
  min-width: 200px;
  padding: 10px 14px;
  font-size: 14px;
  color: #000;
  outline: none;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}
.filter-input:focus {
  border-color: #00e676;
}
.filter-select {
  padding: 10px 12px;
  font-size: 14px;
  color: #000;
  cursor: pointer;
  outline: none;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}
.btn-search {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
}
.btn-reset {
  padding: 10px 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}
.table-wrap {
  overflow: hidden;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
}
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th {
  padding: 14px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #666;
  text-align: left;
  background: #f9f9f9;
  border-bottom: 1px solid #f0f0f0;
}
.table td {
  padding: 14px 16px;
  font-size: 14px;
  color: #000;
  border-bottom: 1px solid #f0f0f0;
}
.table tr:hover {
  background: #f9fff9;
}
.badge {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 999px;
}
.badge.on {
  color: #000;
  background: #e8f5e9;
}
.badge.off {
  color: #999;
  background: #f5f5f5;
}
.link {
  font-size: 13px;
  color: #000;
  cursor: pointer;
}
.link:hover {
  color: #00c665;
}
.loading,
.empty {
  padding: 60px 20px;
  font-size: 14px;
  color: #999;
  text-align: center;
}
</style>
