<template>
  <div class="admin-page">
    <el-form :model="query" class="filter-panel" inline>
      <el-form-item label="内容类型">
        <el-select
          v-model="query.contentType"
          clearable
          placeholder="全部类型"
          style="width: 100px"
        >
          <el-option label="点评" value="review" />
          <el-option label="图片" value="image" />
          <el-option label="景点" value="scenic" />
          <el-option label="行程" value="plan" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态">
        <el-select
          v-model="query.auditStatus"
          clearable
          placeholder="全部状态"
          style="width: 100px"
        >
          <el-option
            v-for="item in auditStatusOptions"
            :key="item.code"
            :label="item.desc"
            :value="Number(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="内容ID" style="width: 150px">
        <el-input-number v-model="query.contentId" :min="1" controls-position="right" />
      </el-form-item>
      <el-form-item label="提交用户ID" style="width: 180px">
        <el-input-number v-model="query.submitUserId" :min="1" controls-position="right" />
      </el-form-item>
      <el-form-item class="form-actions">
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="审核ID" width="70" />
      <el-table-column label="类型" width="70">
        <template #default="{ row }">{{ contentTypeText(row.contentType) }}</template>
      </el-table-column>
      <el-table-column prop="contentId" label="内容ID" width="80" />
      <el-table-column prop="submitUserId" label="提交用户" width="90" />
      <el-table-column label="摘要" min-width="300" show-overflow-tooltip>
        <template #default="{ row }">{{ snapshotSummary(row.snapshot) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="auditTagType(row.auditStatus)">
            {{ auditStatusText(row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="auditRemark" label="备注" min-width="110" show-overflow-tooltip />
      <el-table-column prop="autoAuditScore" label="自动评分" width="90" />
      <el-table-column label="提交时间" min-width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
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
      <template v-if="detail">
        <section class="detail-section">
          <div class="section-title">内容快照</div>
          <div v-if="parsedSnapshot">
            <div v-if="detail.contentType === 'review'" class="review-snapshot-card">
              <div class="review-userInfo">
                <span class="review-uname">{{ parsedSnapshot.username || "匿名用户" }}</span>
                <el-tag v-if="parsedSnapshot.isAnonymous" size="small" type="info" class="meta-tag">
                  匿名点评
                </el-tag>
                <el-tag
                  v-if="parsedSnapshot.travelType"
                  size="small"
                  type="success"
                  class="meta-tag"
                >
                  {{ parsedSnapshot.travelType }}
                </el-tag>
                <span v-if="parsedSnapshot.visitDate" class="review-time">
                  出游时间: {{ parsedSnapshot.visitDate }}
                </span>
              </div>

              <div v-if="parsedSnapshot.scenicName" class="review-scenic">
                🔗 关联景点：
                <b>{{ parsedSnapshot.scenicName }}</b>
              </div>

              <div v-if="parsedSnapshot.rating || parsedSnapshot.score" class="review-rating">
                <span style="margin-right: 8px; font-size: 14px; color: #666">综合评分:</span>
                <el-rate
                  :model-value="Number(parsedSnapshot.rating || parsedSnapshot.score)"
                  disabled
                  show-score
                />
              </div>

              <div v-if="parsedSnapshot.content" class="review-text">
                {{ parsedSnapshot.content }}
              </div>

              <div
                v-if="
                  Array.isArray(parsedSnapshot.images) &&
                  parsedSnapshot.images.length > 0 &&
                  typeof parsedSnapshot.images[0] === 'string'
                "
                class="snapshot-images"
              >
                <el-image
                  v-for="(img, idx) in parsedSnapshot.images"
                  :key="idx"
                  :src="img"
                  :preview-src-list="parsedSnapshot.images"
                  :initial-index="idx"
                  fit="cover"
                  class="snapshot-img"
                />
              </div>
            </div>

            <div v-else class="generic-snapshot-grid">
              <div v-for="(val, key) in parsedSnapshot" :key="key" class="generic-field">
                <div class="generic-label">{{ key }}</div>
                <div class="generic-value">
                  <template
                    v-if="
                      Array.isArray(val) &&
                      val.length > 0 &&
                      typeof val[0] === 'string' &&
                      (val[0].startsWith('http') || val[0].startsWith('/'))
                    "
                  >
                    <div class="snapshot-images">
                      <el-image
                        v-for="(img, idx) in val"
                        :key="idx"
                        :src="img"
                        :preview-src-list="val"
                        :initial-index="idx"
                        fit="cover"
                        class="snapshot-img"
                      />
                    </div>
                  </template>
                  <template
                    v-else-if="
                      typeof val === 'string' &&
                      (val.startsWith('http://') || val.startsWith('https://')) &&
                      (val.includes('.jpg') || val.includes('.png') || val.includes('.jpeg'))
                    "
                  >
                    <el-image
                      :src="val"
                      :preview-src-list="[val]"
                      fit="cover"
                      class="snapshot-img"
                    />
                  </template>
                  <template v-else>
                    {{ val }}
                  </template>
                </div>
              </div>
            </div>
          </div>
          <pre v-else class="json-box">{{ formatJson(detail.snapshot) }}</pre>
        </section>

        <section class="detail-section">
          <div class="section-title">基础信息</div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="审核ID">{{ detail.id }}</el-descriptions-item>
            <el-descriptions-item label="内容类型">
              {{ contentTypeText(detail.contentType) }}
            </el-descriptions-item>
            <el-descriptions-item label="内容ID">
              {{ detail.contentId || "-" }}
            </el-descriptions-item>
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
            <el-descriptions-item label="审核员ID">
              {{ detail.auditorId || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="审核备注">
              {{ detail.auditRemark || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ formatTime(detail.auditTime) || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="违规类型">
              <pre class="json-box">{{ formatJson(detail.violationType) }}</pre>
            </el-descriptions-item>
            <el-descriptions-item label="自动审核结果">
              <pre class="json-box">{{ formatJson(detail.autoAuditResult) }}</pre>
            </el-descriptions-item>
          </el-descriptions>
        </section>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from "vue";
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

const parsedSnapshot = computed(() => {
  if (!detail.value || !detail.value.snapshot) return null;
  return detail.value.snapshot;
});

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

function formatTime(timeStr?: string): string {
  if (!timeStr) return "-";
  return timeStr.replace("T", " ").substring(0, 16);
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

.detail-section {
  margin-bottom: 20px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 700;
  color: #101828;
}

.section-title::before {
  display: inline-block;
  width: 4px;
  height: 16px;
  content: "";
  background: #4f7cff;
  border-radius: 99px;
}

.filter-panel {
  position: relative;
}

.snapshot-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.snapshot-field {
  display: flex;
  flex-direction: column;
}

.snapshot-label {
  margin-bottom: 4px;
  font-weight: bold;
  color: #606266;
}

.snapshot-value {
  color: #333;
  word-break: break-all;
  white-space: pre-wrap;
}

.snapshot-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.snapshot-img {
  width: 100px;
  height: 100px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.review-snapshot-card {
  padding: 16px;
  background: #f8f9fa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.review-userInfo {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}

.review-uname {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.meta-tag {
  margin-left: 4px;
}

.review-time {
  margin-left: auto;
  font-size: 13px;
  color: #909399;
}

.review-scenic {
  display: inline-block;
  padding: 6px 12px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #409eff;
  background: #ecf5ff;
  border-radius: 4px;
}

.review-rating {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.review-text {
  padding: 12px;
  margin-bottom: 16px;
  font-size: 15px;
  line-height: 1.6;
  color: #333;
  white-space: pre-wrap;
  background: #fff;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
}

.generic-snapshot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.generic-field {
  padding: 10px;
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.generic-label {
  margin-bottom: 6px;
  font-size: 12px;
  color: #909399;
  text-transform: uppercase;
}

.generic-value {
  font-size: 14px;
  color: #303133;
  word-break: break-all;
}
</style>
