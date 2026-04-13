<!-- 评论审核页：基础查询 + 审核操作。 -->
<template>
  <div class="page-container">
    <el-card v-loading="loading" class="page-card">
      <template #header>评论审核</template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="内容类型">
          <el-input v-model="query.contentType" clearable placeholder="如 review" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.auditStatus" clearable placeholder="全部" style="width: 140px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已隐藏" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="auditList">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="contentType" label="内容类型" width="120" />
        <el-table-column prop="contentId" label="内容ID" width="100" />
        <el-table-column prop="submitUserId" label="提交用户" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">{{ statusText(scope.row.auditStatus) }}</template>
        </el-table-column>
        <el-table-column
          prop="auditRemark"
          label="审核备注"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.auditStatus === 0"
              type="success"
              text
              @click="onApprove(scope.row.id)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.auditStatus === 0"
              type="danger"
              text
              @click="onReject(scope.row.id)"
            >
              拒绝
            </el-button>
            <el-button type="warning" text @click="onHide(scope.row.id)">隐藏</el-button>
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
          @current-change="loadAudits"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  approveAudit,
  getAuditPage,
  hideAudit,
  rejectAudit,
  type AuditItem,
} from "@/api/admin-audit";

const loading = ref(false);
const auditList = ref<AuditItem[]>([]);
const total = ref(0);

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  contentType: "",
  auditStatus: undefined as number | undefined,
});

function statusText(status?: number): string {
  if (status === 0) return "待审核";
  if (status === 1) return "已通过";
  if (status === 2) return "已拒绝";
  if (status === 3) return "已隐藏";
  return "-";
}

async function loadAudits(): Promise<void> {
  loading.value = true;
  try {
    const page = await getAuditPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      contentType: query.contentType || undefined,
      auditStatus: query.auditStatus,
    });
    auditList.value = page.records;
    total.value = page.total;
  } finally {
    loading.value = false;
  }
}

function onSearch(): void {
  query.pageNum = 1;
  void loadAudits();
}

function onSizeChange(): void {
  query.pageNum = 1;
  void loadAudits();
}

async function onApprove(id: number): Promise<void> {
  await approveAudit(id);
  ElMessage.success("已通过");
  await loadAudits();
}

async function onReject(id: number): Promise<void> {
  const result = await ElMessageBox.prompt("请输入拒绝原因", "拒绝审核", {
    inputPlaceholder: "例如：包含违规内容",
    confirmButtonText: "确认",
    cancelButtonText: "取消",
  });
  await rejectAudit(id, { reason: result.value });
  ElMessage.success("已拒绝");
  await loadAudits();
}

async function onHide(id: number): Promise<void> {
  await hideAudit(id);
  ElMessage.success("已隐藏");
  await loadAudits();
}

onMounted(() => {
  void loadAudits();
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

.page-container {
  padding: 0px;
}
</style>
