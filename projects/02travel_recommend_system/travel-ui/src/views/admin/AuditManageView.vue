<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">审核管理</div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="提交用户ID">
          <el-input-number
            v-model="query.submitUserId"
            :min="1"
            :controls="false"
            style="width: 200px"
            placeholder="请输入用户ID"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.auditStatus" clearable style="width: 120px" placeholder="全部">
            <el-option label="待审核" :value="0" />
            <el-option label="通过" :value="1" />
            <el-option label="拒绝" :value="2" />
            <el-option label="人工复审" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="reviewList">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="scenicName" label="景点名称" min-width="140" />
        <el-table-column prop="content" label="评论内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="评分" width="90">
          <template #default="scope">{{ formatRating(scope.row.rating ?? scope.row.score) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row.id)">查看详情</el-button>
            <el-popconfirm
              v-if="scope.row.status === 0"
              title="确认通过该评论吗？"
              confirm-button-text="确认"
              cancel-button-text="取消"
              @confirm="handleApprove(scope.row.id)"
            >
              <template #reference>
                <el-button link type="success">通过</el-button>
              </template>
            </el-popconfirm>
            <el-button v-if="scope.row.status === 0" link type="warning" @click="openRejectDialog(scope.row.id)">
              拒绝
            </el-button>
            <el-popconfirm
              v-if="scope.row.status === 0"
              title="确认隐藏该评论吗？"
              confirm-button-text="确认"
              cancel-button-text="取消"
              @confirm="handleHide(scope.row.id)"
            >
              <template #reference>
                <el-button link type="danger">隐藏</el-button>
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
          @current-change="loadReviewList"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailVisible"
      title="评论详情"
      width="760px"
      :close-on-click-modal="true"
      destroy-on-close
    >
      <el-descriptions v-if="detailData" :column="2" border>
        <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailData.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="景点名称">{{ detailData.scenicName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          {{ formatRating(detailData.rating ?? detailData.score) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评论内容" :span="2">{{ detailData.content || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">图片</el-divider>
      <div class="image-list" v-if="detailData?.images?.length">
        <el-image
          v-for="item in detailData.images"
          :key="item"
          :src="item"
          fit="cover"
          style="width: 120px; height: 80px; border-radius: 6px"
        />
      </div>
      <div class="text-muted" v-else>暂无图片</div>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="拒绝原因" width="520px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  approveAdminAudit,
  getAdminAuditDetail,
  getAdminAuditPage,
  hideAdminAudit,
  rejectAdminAudit,
  type ReviewItem,
  type ReviewQuery,
} from "@/api/audit";

const loading = ref(false);
const total = ref(0);
const reviewList = ref<ReviewItem[]>([]);
const detailVisible = ref(false);
const detailData = ref<ReviewItem | null>(null);
const rejectVisible = ref(false);
const rejectAuditId = ref<number | null>(null);
const rejectForm = reactive({ reason: "" });

const query = reactive<ReviewQuery>({
  pageNum: 1,
  pageSize: 10,
  contentType: "review",
  auditStatus: undefined,
  submitUserId: undefined,
  contentId: undefined,
});

function formatRating(value?: number): string {
  if (value == null || Number.isNaN(Number(value))) {
    return "-";
  }
  return Number(value).toFixed(1);
}

function statusText(status?: number): string {
  if (status === 0) return "待审核";
  if (status === 1) return "通过";
  if (status === 2) return "拒绝";
  if (status === 3) return "人工复审";
  return "-";
}

function statusTagType(status?: number): "warning" | "success" | "danger" | "info" {
  if (status === 0) return "warning";
  if (status === 1) return "success";
  if (status === 2) return "danger";
  if (status === 3) return "info";
  return "info";
}

async function loadReviewList(): Promise<void> {
  loading.value = true;
  try {
    const page = await getAdminAuditPage({ ...query });
    reviewList.value = page.records;
    total.value = page.total;
  } catch {
    ElMessage.error("评论审核列表加载失败");
  } finally {
    loading.value = false;
  }
}

function onSearch(): void {
  query.pageNum = 1;
  void loadReviewList();
}

function onReset(): void {
  query.auditStatus = undefined;
  query.submitUserId = undefined;
  query.contentId = undefined;
  query.pageNum = 1;
  void loadReviewList();
}

function onSizeChange(): void {
  query.pageNum = 1;
  void loadReviewList();
}

async function openDetail(id: number): Promise<void> {
  try {
    detailData.value = await getAdminAuditDetail(id);
    detailVisible.value = true;
  } catch {
    ElMessage.error("加载评论详情失败");
  }
}

async function handleApprove(id: number): Promise<void> {
  try {
    await approveAdminAudit(id);
    ElMessage.success("审核通过成功");
    await loadReviewList();
  } catch {
    ElMessage.error("审核通过失败");
  }
}

function openRejectDialog(id: number): void {
  rejectAuditId.value = id;
  rejectForm.reason = "";
  rejectVisible.value = true;
}

async function submitReject(): Promise<void> {
  if (!rejectAuditId.value) {
    return;
  }
  if (!rejectForm.reason.trim()) {
    ElMessage.warning("请输入拒绝原因");
    return;
  }
  try {
    await rejectAdminAudit(rejectAuditId.value, rejectForm.reason.trim());
    ElMessage.success("审核拒绝成功");
    rejectVisible.value = false;
    await loadReviewList();
  } catch {
    ElMessage.error("审核拒绝失败");
  }
}

async function handleHide(id: number): Promise<void> {
  try {
    await hideAdminAudit(id);
    ElMessage.success("隐藏成功");
    if (reviewList.value.length === 1 && query.pageNum > 1) {
      query.pageNum -= 1;
    }
    await loadReviewList();
  } catch {
    ElMessage.error("隐藏失败");
  }
}

onMounted(() => {
  void loadReviewList();
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

.text-muted {
  color: #909399;
}

.tag-gap {
  margin-right: 6px;
}

.page-container {
  padding: 0px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>