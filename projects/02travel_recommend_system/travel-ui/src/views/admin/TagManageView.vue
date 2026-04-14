<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">标签管理</div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="标签名称">
          <el-input v-model="query.name" clearable style="width: 220px" placeholder="请输入标签名" />
        </el-form-item>
        <el-form-item label="类型">
          <!-- 标签类型：1 景点标签 2 用户偏好标签 -->
          <el-select v-model="query.type" clearable style="width: 140px" placeholder="全部">
            <el-option label="景点标签" :value="1" />
            <el-option label="偏好标签" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable style="width: 120px" placeholder="全部">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
        <el-form-item>
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
        <el-table-column label="类型" width="120">
          <template #default="scope">{{ tagTypeText(scope.row.type) }}</template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="颜色" width="100">
          <template #default="scope">
            <span
              class="color-block"
              :style="{ backgroundColor: scope.row.color || '#dcdfe6' }"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? "启用" : "禁用" }}
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

    <el-dialog v-model="formVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="88px">
        <el-form-item label="标签名" prop="name">
          <el-input v-model="formModel.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <!-- 标签类型选择框：根据业务规定，限制为 1: 景点标签、2: 用户偏好标签 -->
          <el-select v-model="formModel.type" style="width: 100%" @change="onFormTypeChange">
            <el-option label="景点标签" :value="1" />
            <el-option label="偏好标签" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <!-- 标签分类选择框：支持下拉选择（主题/风格/设施等）并允许直接输入创建新分类 -->
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
        <el-form-item label="图标" prop="icon">
          <el-input v-model="formModel.icon" placeholder="图标 URL" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-color-picker v-model="formModel.color" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formModel.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formModel.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
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
  getTagsByType,
  type AdminTagItem,
  type AdminTagQuery,
  type TagPayload,
} from "@/api/common";

const loading = ref(false);
const submitting = ref(false);
const total = ref(0);
const tagList = ref<AdminTagItem[]>([]);

const formVisible = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInstance>();

const query = reactive<AdminTagQuery>({
  pageNum: 1,
  pageSize: 10,
  name: "",
  type: undefined,
  category: "",
  status: undefined,
});

const formModel = reactive<TagPayload>({
  name: "",
  type: 1,
  category: "",
  icon: "",
  color: "",
  sortOrder: 0,
  status: 1,
});

const formRules: FormRules<TagPayload> = {
  name: [{ required: true, message: "请输入标签名称", trigger: "blur" }],
};

const dialogTitle = computed(() => (editingId.value ? "编辑标签" : "新增标签"));

// 保存基于当前选择 type 从后端加载的分类选项
const remoteCategories = ref<string[]>([]);
const dynamicCategories = computed(() => {
  return Array.from(new Set([...remoteCategories.value]));
});

// 监听新增/编辑表单中类型的变化，动态拉取对应类型的 category
async function onFormTypeChange(typeVal: number) {
  formModel.category = ""; // 类型改变时，清空已选分类
  await fetchCategoriesByType(typeVal);
}

// 核心：请求后端数据，获取该类型下所有的分类集合
async function fetchCategoriesByType(typeVal: number) {
  try {
    const tags = await getTagsByType(typeVal);
    remoteCategories.value = tags
      .map((t) => t.category)
      .filter((c): c is string => Boolean(c));
  } catch (error) {
    ElMessage.error("动态分类加载失败");
  }
}

function tagTypeText(type: number): string {
  // 标签类型判断：1 表示景点标签，2 表示用户偏好标签
  if (type === 1) return "景点标签";
  if (type === 2) return "偏好标签";
  return "-";
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
  query.type = undefined;
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
  formModel.type = 1;
  formModel.category = "";
  formModel.icon = "";
  formModel.color = "";
  formModel.sortOrder = 0;
  formModel.status = 1;
}

function openCreate(): void {
  editingId.value = null;
  resetFormModel();
  formVisible.value = true;
  void fetchCategoriesByType(Number(formModel.type || 1));
}

function openEdit(row: AdminTagItem): void {
  editingId.value = row.id;
  formModel.name = row.name;
  formModel.type = row.type;
  formModel.category = row.category ?? "";
  formModel.icon = row.icon ?? "";
  formModel.color = row.color ?? "";
  formModel.sortOrder = row.sortOrder ?? 0;
  formModel.status = row.status ?? 1;
  formVisible.value = true;
  void fetchCategoriesByType(Number(formModel.type || 1));
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
      type: Number(formModel.type),
      category: formModel.category || undefined,
      icon: formModel.icon || undefined,
      color: formModel.color || undefined,
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
  margin-bottom: 12px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.color-block {
  display: inline-block;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
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
</style>
