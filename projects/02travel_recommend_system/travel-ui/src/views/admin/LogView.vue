<!-- 极简风格操作日志页 -->
<template>
  <div class="page">
    <h1 class="title">操作日志</h1>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="list.length === 0" class="empty">暂无日志数据</div>

    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>管理员</th>
            <th>模块</th>
            <th>操作</th>
            <th>时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.adminName || "管理员" }}</td>
            <td>
              <span class="badge">{{ item.module || "-" }}</span>
            </td>
            <td>{{ item.operation || "-" }}</td>
            <td>{{ item.createdAt || "-" }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getOperationLogPage as getLogPage } from "@/api/log";

const loading = ref(false);
const list = ref<any[]>([]);

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getLogPage({ pageNum: 1, pageSize: 50 });
    list.value = res.records || [];
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
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
  color: #000;
  background: #f0f0f0;
  border-radius: 999px;
}
.loading,
.empty {
  padding: 60px 20px;
  font-size: 14px;
  color: #999;
  text-align: center;
}
</style>
