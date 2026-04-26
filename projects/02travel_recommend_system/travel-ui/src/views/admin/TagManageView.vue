<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">标签管理</div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="标签名称">
          <el-input
            v-model="query.name"
            clearable
            style="width: 140px"
            placeholder="请输入标签名"
          />
        </el-form-item>
        <el-form-item label="作用域">
          <el-select v-model="query.scope" clearable style="width: 100px" placeholder="全部">
            <el-option
              v-for="item in tagScopeOptions"
              :key="item.code"
              :label="item.desc"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 100px" placeholder="全部">
            <el-option
              v-for="item in statusOptions"
              :key="item.code"
              :label="item.desc"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item style="margin-left: auto">
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
        <el-form-item style="margin-left: auto">
          <el-button type="success" @click="openCreate">新增标签</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tagList">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图标" width="80">
          <template #default="scope">
            <el-image
              v-if="scope.row.icon"
              :src="scope.row.icon"
              :preview-src-list="[scope.row.icon]"
              preview-teleported
              fit="cover"
              class="tag-icon"
            />
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="标签名称" min-width="140" />
        <el-table-column label="作用域" width="120">
          <template #default="scope">{{ tagScopeText(scope.row.scope) }}</template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ findDictDesc(statusOptions, scope.row.status, "-") }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="warning" @click="openEdit(scope.row)">编辑</el-button>
            <el-popconfirm
              title="确认删除该标签吗？"
              confirm-button-text="确认"
              cancel-button-text="取消"
              @confirm="handleDelete(scope.row.id)"
            >
              <template #reference>
                <el-button link type="danger">删除</el-button>
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
          @current-change="loadTagList"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="formVisible"
      :title="dialogTitle"
      width="620px"
      class="tag-form-dialog"
      destroy-on-close
    >
      <div class="dialog-body-scroll">
        <el-form ref="formRef" :model="formModel" :rules="formRules" label-position="top">
          <div class="form-section">
            <div class="form-section-title">基础信息</div>
            <div class="form-section-content">
              <el-form-item label="标签名" prop="name">
                <el-input v-model="formModel.name" placeholder="请输入标签名称" />
              </el-form-item>
              <div class="form-row-grid">
                <el-form-item label="作用域" prop="scope">
                  <el-select
                    v-model="formModel.scope"
                    style="width: 100%"
                    @change="onFormScopeChange"
                  >
                    <el-option
                      v-for="item in tagScopeOptions"
                      :key="item.code"
                      :label="item.desc"
                      :value="item.code"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="分类" prop="category">
                  <el-select
                    v-model="formModel.category"
                    placeholder="请选择或输入标签分类"
                    style="width: 100%"
                    filterable
                    allow-create
                  >
                    <el-option
                      v-for="cat in dynamicCategories"
                      :key="cat"
                      :label="cat"
                      :value="cat"
                    />
                  </el-select>
                </el-form-item>
              </div>
              <el-form-item label="图标" prop="icon">
                <div v-if="iconPreviewUrl" class="icon-image-card">
                  <img :src="iconPreviewUrl" class="icon-image" />
                  <div class="icon-image-mask" @click="removeIcon">
                    <el-icon class="delete-icon"><Close /></el-icon>
                  </div>
                </div>
                <div v-else class="icon-upload-btn" @click="triggerIconUpload">
                  <el-icon v-if="!iconUploadLoading" class="upload-icon"><Plus /></el-icon>
                  <el-icon v-else class="upload-icon is-loading"><Loading /></el-icon>
                  <div class="upload-text">
                    {{ iconUploadLoading ? "上传中..." : "点击上传" }}
                  </div>
                </div>
                <input
                  ref="iconInputRef"
                  type="file"
                  accept="image/*"
                  style="display: none"
                  @change="onIconSelected"
                />
              </el-form-item>
            </div>
          </div>

          <div class="form-section">
            <div class="form-section-title">运营属性</div>
            <div class="form-section-content">
              <div class="form-row-grid">
                <el-form-item label="排序" prop="sortOrder">
                  <el-input-number
                    v-model="formModel.sortOrder"
                    :min="0"
                    :max="9999"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                  <el-select v-model="formModel.status" style="width: 100%">
                    <el-option
                      v-for="item in statusOptions"
                      :key="item.code"
                      :label="item.desc"
                      :value="item.code"
                    />
                  </el-select>
                </el-form-item>
              </div>
            </div>
          </div>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="formVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import {
  createAdminTag,
  deleteAdminTag,
  getAdminTagPage,
  updateAdminTag,
  getTagsByScope,
  type AdminTagItem,
  type AdminTagQuery,
  type TagPayload,
} from "@/api/common";
import { getCommonStatusDict, getTagScopeDict } from "@/api/dict";
import { findDictDesc, useDictOptions } from "@/composables/useDictOptions";
import { Plus, Loading, Close } from "@element-plus/icons-vue";
import { getUploadToken, uploadCallback, getFileResource } from "@/api/file";

const { options: tagScopeOptions } = useDictOptions("tag-scope", getTagScopeDict);
const { options: statusOptions } = useDictOptions("common-status", getCommonStatusDict);

const loading = ref(false);
const submitting = ref(false);
const total = ref(0);
const tagList = ref<AdminTagItem[]>([]);

const formVisible = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInstance>();

const iconInputRef = ref<HTMLInputElement>();
const iconUploadLoading = ref(false);
const iconPreviewUrl = ref("");

const query = reactive<AdminTagQuery>({
  pageNum: 1,
  pageSize: 10,
  name: "",
  scope: undefined,
  category: "",
  status: undefined,
});

const formModel = reactive<TagPayload>({
  name: "",
  scope: "SCENIC",
  category: "",
  icon: "",
  sortOrder: 0,
  status: 1,
});

const formRules: FormRules<TagPayload> = {
  name: [{ required: true, message: "请输入标签名称", trigger: "blur" }],
};

const dialogTitle = computed(() => (editingId.value ? "编辑标签" : "新增标签"));

// 保存基于当前选择 scope 从后端加载的分类选项
const remoteCategories = ref<string[]>([]);
const dynamicCategories = computed(() => {
  return Array.from(new Set([...remoteCategories.value]));
});

// 监听新增/编辑表单中作用域的变化，动态拉取对应 scope 的 category
async function onFormScopeChange(scopeVal: string) {
  formModel.category = ""; // 作用域改变时，清空已选分类
  await fetchCategoriesByScope(scopeVal);
}

// 核心：请求后端数据，获取该 scope 下所有的分类集合
async function fetchCategoriesByScope(scopeVal: string) {
  try {
    const tags = await getTagsByScope(scopeVal);
    remoteCategories.value = tags.map((t) => t.category).filter((c): c is string => Boolean(c));
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
  } catch (error) {
    ElMessage.error("动态分类加载失败");
  }
}

function tagScopeText(scope?: string): string {
  return findDictDesc(tagScopeOptions.value, scope ?? "", "-");
}

async function loadTagList(): Promise<void> {
  loading.value = true;
  try {
    const page = await getAdminTagPage({
      ...query,
      name: query.name || undefined,
      category: query.category || undefined,
    });
    tagList.value = page.records;
    total.value = page.total;
  } catch {
    ElMessage.error("标签列表加载失败");
  } finally {
    loading.value = false;
  }
}

function onSearch(): void {
  query.pageNum = 1;
  void loadTagList();
}

function onReset(): void {
  query.name = "";
  query.scope = undefined;
  query.category = "";
  query.status = undefined;
  query.pageNum = 1;
  void loadTagList();
}

function onSizeChange(): void {
  query.pageNum = 1;
  void loadTagList();
}

function resetFormModel(): void {
  formModel.name = "";
  formModel.scope = "SCENIC";
  formModel.category = "";
  formModel.icon = "";
  formModel.sortOrder = 0;
  formModel.status = 1;
  iconPreviewUrl.value = "";
}

function openCreate(): void {
  editingId.value = null;
  resetFormModel();
  formVisible.value = true;
  void fetchCategoriesByScope(formModel.scope || "SCENIC");
}

async function openEdit(row: AdminTagItem): Promise<void> {
  editingId.value = row.id;
  formModel.name = row.name;
  formModel.scope = row.scope ?? "SCENIC";
  formModel.category = row.category ?? "";
  formModel.icon = row.icon ?? "";
  formModel.sortOrder = row.sortOrder ?? 0;
  formModel.status = row.status ?? 1;

  // 图标预览：后端已解析为 URL，直接预览；若仍存旧值，尝试按 fileId 解析
  if (row.icon && row.icon.startsWith("http")) {
    iconPreviewUrl.value = row.icon;
  } else if (row.icon && /^\d+$/.test(row.icon.trim())) {
    try {
      const fileRes = await getFileResource(Number(row.icon));
      iconPreviewUrl.value = fileRes.url || "";
    } catch {
      iconPreviewUrl.value = "";
    }
  } else {
    iconPreviewUrl.value = row.icon || "";
  }

  formVisible.value = true;
  void fetchCategoriesByScope(formModel.scope || "SCENIC");
}

function triggerIconUpload(): void {
  if (iconUploadLoading.value) return;
  iconInputRef.value?.click();
}

async function onIconSelected(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  if (!file.type.startsWith("image/")) {
    ElMessage.warning("请选择图片文件");
    input.value = "";
    return;
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning("图标大小不能超过 2MB");
    input.value = "";
    return;
  }

  iconUploadLoading.value = true;
  try {
    const tokenRes = await getUploadToken({
      bizType: "tag",
      fileName: file.name,
      fileSize: file.size,
    });
    const uploadUrl = String(tokenRes.uploadUrl || "");
    const bucketName = String(tokenRes.bucketName || "");
    const objectKey = String(tokenRes.objectKey || "");
    if (!uploadUrl || !bucketName || !objectKey) {
      throw new Error("上传凭证不完整");
    }

    const uploadResponse = await fetch(uploadUrl, {
      method: "PUT",
      body: file,
      headers: { "Content-Type": file.type || "application/octet-stream" },
    });
    if (!uploadResponse.ok) throw new Error("文件上传失败");

    const fileId = await uploadCallback({
      bucketName,
      objectKey,
      originalName: file.name,
      bizType: "tag",
    });
    const fileResource = await getFileResource(fileId);
    iconPreviewUrl.value = fileResource.url || "";
    formModel.icon = String(fileId);
    ElMessage.success("图标上传成功");
  } catch {
    ElMessage.error("图标上传失败，请重试");
  } finally {
    iconUploadLoading.value = false;
    input.value = "";
  }
}

function removeIcon(): void {
  iconPreviewUrl.value = "";
  formModel.icon = "";
}

async function handleSubmit(): Promise<void> {
  const form = formRef.value;
  if (!form) {
    return;
  }
  await form.validate();
  submitting.value = true;
  try {
    const payload: TagPayload = {
      ...formModel,
      category: formModel.category || undefined,
      icon: formModel.icon || undefined,
    };
    if (editingId.value) {
      await updateAdminTag(editingId.value, payload);
      ElMessage.success("标签更新成功");
    } else {
      await createAdminTag(payload);
      ElMessage.success("标签创建成功");
    }

    formVisible.value = false;
    await loadTagList();
  } catch {
    ElMessage.error("保存失败");
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id: number): Promise<void> {
  try {
    await deleteAdminTag(id);
    ElMessage.success("删除成功");
    if (tagList.value.length === 1 && query.pageNum > 1) {
      query.pageNum -= 1;
    }
    await loadTagList();
  } catch {
    ElMessage.error("删除失败");
  }
}

onMounted(() => {
  void loadTagList();
});
</script>

<style scoped>
.card-header {
  font-size: 16px;
  font-weight: 700;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 6px;
  margin-bottom: 12px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
}

.filter-form :deep(.el-form-item__label) {
  padding-right: 6px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.tag-icon {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  vertical-align: middle;
}

.empty-text {
  color: #909399;
}

.page-container {
  padding: 0px;
}

.tag-form-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-right: 0;
}

.tag-form-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 500;
}

.tag-form-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.tag-form-dialog .dialog-body-scroll {
  max-height: calc(85vh - 120px);
  overflow-y: auto;
  padding: 20px;
}

.tag-form-dialog .form-section {
  margin-bottom: 24px;
}

.tag-form-dialog .form-section:last-child {
  margin-bottom: 0;
}

.tag-form-dialog .form-section-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.tag-form-dialog :deep(.el-form-item__label) {
  font-size: 13px;
  color: #606266;
  line-height: 20px;
  padding-bottom: 4px;
}

.tag-form-dialog :deep(.el-form-item) {
  margin-bottom: 16px;
}

.tag-form-dialog :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.tag-form-dialog .form-row-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.tag-form-dialog .form-row-grid :deep(.el-form-item) {
  margin-bottom: 16px;
}

.tag-form-dialog :deep(.el-input__wrapper),
.tag-form-dialog :deep(.el-textarea__inner),
.tag-form-dialog :deep(.el-input-number .el-input__wrapper) {
  border-radius: 4px;
}

.tag-form-dialog :deep(.el-input__wrapper) {
  border-color: #dcdfe6;
}

.tag-form-dialog :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.tag-form-dialog .dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
}

.tag-form-dialog :deep(.el-dialog__footer) {
  padding: 0;
  border-top: none;
}

.icon-image-card {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  cursor: pointer;
}

.icon-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.icon-image-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.icon-image-card:hover .icon-image-mask {
  opacity: 1;
}

.icon-upload-btn {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  border: 1px dashed #dcdfe6;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s;
  background: #fafbfc;
  gap: 4px;
}

.icon-upload-btn:hover {
  border-color: #409eff;
}
</style>
