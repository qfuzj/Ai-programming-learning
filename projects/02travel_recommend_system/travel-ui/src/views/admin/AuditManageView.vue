<template>
  <div class="admin-page">
    <el-form :model="query" class="filter-panel" inline>
      <el-form-item label="内容类型">
        <el-select v-model="query.contentType" clearable placeholder="全部类型">
          <el-option label="点评" value="review" />
          <el-option label="图片" value="image" />
          <el-option label="景点" value="scenic" />
          <el-option label="行程" value="plan" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态">
        <el-select v-model="query.auditStatus" clearable placeholder="全部状态">
          <el-option
            v-for="item in auditStatusOptions"
            :key="item.code"
            :label="item.desc"
            :value="Number(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="内容ID">
        <el-input-number v-model="query.contentId" :min="1" controls-position="right" />
      </el-form-item>
      <el-form-item label="提交用户ID">
        <el-input-number v-model="query.submitUserId" :min="1" controls-position="right" />
      </el-form-item>
      <el-form-item class="form-actions">
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="审核ID" width="100" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ contentTypeText(row.contentType) }}</template>
      </el-table-column>
      <el-table-column prop="contentId" label="内容ID" width="110" />
      <el-table-column prop="submitUserId" label="提交用户" width="110" />
      <el-table-column label="摘要" min-width="300" show-overflow-tooltip>
        <template #default="{ row }">{{ snapshotSummary(row.snapshot) }}</template>
      </el-table-column>
      <el-table-column prop="autoAuditScore" label="自动评分" width="110" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="auditTagType(row.auditStatus)">
            {{ auditStatusText(row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditRemark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="提交时间" min-width="170" />
      <el-table-column fixed="right" label="操作" width="260">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
          <el-button v-if="row.auditStatus === 0" link type="success" @click="approve(row)">
            通过
          </el-button>
          <el-button v-if="row.auditStatus === 0" link type="danger" @click="reject(row)">
            拒绝
          </el-button>
          <el-button v-if="row.auditStatus !== 3" link type="warning" @click="hide(row)">
            隐藏
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

    <el-drawer v-model="detailVisible" title="审核详情" size="760px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="审核ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="内容类型">
          {{ contentTypeText(detail.contentType) }}
        </el-descriptions-item>
        <el-descriptions-item label="内容ID">{{ detail.contentId || "-" }}</el-descriptions-item>
        <el-descriptions-item label="提交用户ID">
          {{ detail.submitUserId || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ auditStatusText(detail.auditStatus) }}
        </el-descriptions-item>
        <el-descriptions-item label="自动评分">
          {{ detail.autoAuditScore ?? "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="LLM日志ID">
          {{ detail.llmCallLogId || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="审核员ID">{{ detail.auditorId || "-" }}</el-descriptions-item>
        <el-descriptions-item label="审核备注">
          {{ detail.auditRemark || "-" }}
        </el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ detail.auditTime || "-" }}</el-descriptions-item>
        <el-descriptions-item label="违规类型">
          <pre class="json-box">{{ formatJson(detail.violationType) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="快照">
          <pre class="json-box">{{ formatJson(detail.snapshot) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="自动审核结果">
          <pre class="json-box">{{ formatJson(detail.autoAuditResult) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  approveAdminAudit,
  getAdminAuditDetail,
  getAdminAuditPage,
  hideAdminAudit,
  rejectAdminAudit,
  type AuditItem,
  type AuditQuery,
  type AuditSnapshot,
} from "@/api/audit";
import { getContentAuditStatusDict, type DictItem } from "@/api/dict";
import { findDictDesc } from "@/composables/useDictOptions";

const loading = ref(false);
const detailVisible = ref(false);
const list = ref<AuditItem[]>([]);
const detail = ref<AuditItem | null>(null);
const total = ref(0);
const auditStatusOptions = ref<DictItem[]>([]);

const query = reactive<AuditQuery>({
  pageNum: 1,
  pageSize: 10,
  contentType: undefined,
  auditStatus: undefined,
  contentId: undefined,
  submitUserId: undefined,
});

function contentTypeText(type?: string): string {
  return { review: "点评", image: "图片", scenic: "景点", plan: "行程" }[type || ""] || type || "-";
}

function auditStatusText(status?: number): string {
  return findDictDesc(auditStatusOptions.value, status, "-");
}

function auditTagType(status?: number): "success" | "warning" | "danger" | "info" {
  if (status === 1) return "success";
  if (status === 2) return "danger";
  if (status === 3) return "info";
  return "warning";
}

function snapshotSummary(snapshot?: AuditSnapshot): string {
  if (!snapshot) return "-";
  const parts = [
    snapshot.username ? `用户:${snapshot.username}` : "",
    snapshot.scenicName ? `景点:${snapshot.scenicName}` : "",
    (snapshot.score ?? snapshot.rating) ? `评分:${snapshot.score ?? snapshot.rating}` : "",
    snapshot.content ? `内容:${snapshot.content}` : "",
  ].filter(Boolean);
  return parts.join(" / ") || JSON.stringify(snapshot);
}

function formatJson(value: unknown): string {
  if (value === null || value === undefined || value === "") return "-";
  if (typeof value === "string") {
    try {
      return JSON.stringify(JSON.parse(value), null, 2);
    } catch {
      return value;
    }
  }
  return JSON.stringify(value, null, 2);
}

async function loadDictionaries(): Promise<void> {
  auditStatusOptions.value = await getContentAuditStatusDict();
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getAdminAuditPage(query);
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
  query.contentType = undefined;
  query.auditStatus = undefined;
  query.contentId = undefined;
  query.submitUserId = undefined;
  void loadData();
}

async function openDetail(id: number): Promise<void> {
  detailVisible.value = true;
  detail.value = await getAdminAuditDetail(id);
}

async function approve(row: AuditItem): Promise<void> {
  const { value } = await ElMessageBox.prompt(
    "审核通过备注（可选）",
    `通过 ${contentTypeText(row.contentType)} #${row.contentId}`,
    {
      inputPlaceholder: "可留空",
    }
  );
  await approveAdminAudit(row.id, value || undefined);
  ElMessage.success("已通过");
  await loadData();
}

async function reject(row: AuditItem): Promise<void> {
  const { value } = await ElMessageBox.prompt(
    "请输入拒绝原因",
    `拒绝 ${contentTypeText(row.contentType)} #${row.contentId}`,
    {
      inputValidator: (val) => !!val?.trim() || "拒绝原因不能为空",
    }
  );
  await rejectAdminAudit(row.id, value.trim());
  ElMessage.success("已拒绝");
  await loadData();
}

async function hide(row: AuditItem): Promise<void> {
  const { value } = await ElMessageBox.prompt(
    "隐藏原因（可选）",
    `隐藏 ${contentTypeText(row.contentType)} #${row.contentId}`,
    {
      inputPlaceholder: "可留空",
    }
  );
  await hideAdminAudit(row.id, value || undefined);
  ElMessage.success("已隐藏");
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
  max-height: 260px;
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
