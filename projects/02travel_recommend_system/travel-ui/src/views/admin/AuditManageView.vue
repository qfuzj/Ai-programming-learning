<!-- 极简风格审核管理页 -->
<template>
  <div class="page">
    <h1 class="title">审核管理</h1>

    <div class="filter-bar">
      <select v-model="query.auditStatus" class="filter-select" @change="loadData">
        <option :value="undefined">全部状态</option>
        <option :value="0">待审核</option>
        <option :value="1">通过</option>
        <option :value="2">拒绝</option>
        <option :value="3">隐藏</option>
      </select>
      <button class="btn-search" @click="loadData">查询</button>
      <button class="btn-reset" @click="resetQuery">重置</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="list.length === 0" class="empty">暂无审核数据</div>

    <div v-else class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>用户</th>
            <th>景点</th>
            <th>评分</th>
            <th>内容</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td>{{ item.snapshot?.username || "用户" }}</td>
            <td>{{ item.snapshot?.scenicName || "-" }}</td>
            <td>{{ item.snapshot?.score || "-" }}</td>
            <td class="content-cell">{{ item.snapshot?.content || "-" }}</td>
            <td>
              <span class="badge" :class="statusClass(item.auditStatus)">
                {{ statusText(item.auditStatus) }}
              </span>
            </td>
            <td>
              <span v-if="item.auditStatus === 0" class="link" @click="approve(item.id)">通过</span>
              <span v-if="item.auditStatus === 0" class="link danger" @click="reject(item.id)">
                拒绝
              </span>
              <span class="link" @click="openDetail(item.id)">详情</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="detailVisible" class="modal-overlay" @click.self="detailVisible = false">
      <div class="modal">
        <h2 class="modal-title">审核详情</h2>
        <div v-if="detailLoading" class="loading">加载中...</div>
        <div v-else-if="detailSnapshot">
          <p>
            <strong>用户：</strong>
            {{ detailSnapshot.username }}
          </p>
          <p>
            <strong>景点：</strong>
            {{ detailSnapshot.scenicName }}
          </p>
          <p>
            <strong>评分：</strong>
            {{ detailSnapshot.score }}
          </p>
          <p>
            <strong>内容：</strong>
            {{ detailSnapshot.content }}
          </p>
        </div>
        <button class="btn-cancel" @click="detailVisible = false">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import {
  getAdminAuditPage as getAudits,
  approveAdminAudit as approveAudit,
  rejectAdminAudit as rejectAudit,
  getAdminAuditDetail as getAuditDetail,
} from "@/api/audit";

const loading = ref(false);
const list = ref<any[]>([]);
const detailVisible = ref(false);
const detailLoading = ref(false);
const detailSnapshot = ref<any>(null);

const query = reactive({
  auditStatus: undefined as number | undefined,
});

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getAudits({ pageNum: 1, pageSize: 50, auditStatus: query.auditStatus });
    list.value = res.records || [];
  } catch {
    alert("加载失败");
  } finally {
    loading.value = false;
  }
}

function resetQuery(): void {
  query.auditStatus = undefined;
  loadData();
}

function statusText(s: number): string {
  return { 0: "待审核", 1: "通过", 2: "拒绝", 3: "隐藏" }[s] || "-";
}

function statusClass(s: number): string {
  return { 0: "pending", 1: "approved", 2: "rejected", 3: "hidden" }[s] || "";
}

async function approve(id: number): Promise<void> {
  if (!confirm("确认通过？")) return;
  try {
    await approveAudit(id);
    loadData();
  } catch {
    alert("操作失败");
  }
}

async function reject(id: number): Promise<void> {
  const reason = prompt("请输入拒绝原因：");
  if (reason === null) return;
  try {
    await rejectAudit(id, reason || "拒绝");
    loadData();
  } catch {
    alert("操作失败");
  }
}

async function openDetail(id: number): Promise<void> {
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const detail = await getAuditDetail(id);
    detailSnapshot.value = detail.snapshot;
  } catch {
    /* empty */
  } finally {
    detailLoading.value = false;
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
.content-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.badge {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 999px;
}
.badge.pending {
  color: #000;
  background: #fff3e0;
}
.badge.approved {
  color: #000;
  background: #e8f5e9;
}
.badge.rejected {
  color: #000;
  background: #ffebee;
}
.badge.hidden {
  color: #999;
  background: #f5f5f5;
}
.link {
  margin-right: 12px;
  font-size: 13px;
  color: #000;
  cursor: pointer;
}
.link:hover {
  color: #00c665;
}
.link.danger {
  color: #ff5252;
}
.loading,
.empty {
  padding: 60px 20px;
  font-size: 14px;
  color: #999;
  text-align: center;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.3);
}
.modal {
  width: 100%;
  max-width: 500px;
  padding: 32px;
  background: #fff;
  border-radius: 16px;
}
.modal-title {
  margin: 0 0 24px 0;
  font-size: 22px;
  font-weight: 700;
  color: #000;
}
.modal p {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #333;
}
.modal p strong {
  color: #000;
}
.btn-cancel {
  padding: 10px 20px;
  margin-top: 24px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  background: transparent;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}
</style>
