<template>
  <div class="admin-page">
    <el-form :model="query" class="filter-panel" inline>
      <el-form-item label="标签名称">
        <el-input v-model="query.name" clearable placeholder="输入标签名称" @keyup.enter="search" />
      </el-form-item>
      <el-form-item style="width: 170px">
        <template #label>
          <el-tooltip
            content="作用域决定标签出现在哪里：景点标签贴在景点上、偏好标签用于用户喜好、通用标签两边都能用"
            placement="top"
          >
            <span class="label-with-hint">作用域</span>
          </el-tooltip>
        </template>
        <el-select
          v-model="query.scope"
          clearable
          placeholder="全部作用域"
          @change="onScopeFilterChange"
        >
          <el-option
            v-for="item in tagScopeOptions"
            :key="item.code"
            :label="item.desc"
            :value="String(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item style="width: 170px">
        <template #label>
          <el-tooltip
            content="分类是同作用域内的二级分组，仅用于在选择器里把相关标签归到一组（如自然风光 / 美食 / 历史文化）"
            placement="top"
          >
            <span class="label-with-hint">分类</span>
          </el-tooltip>
        </template>
        <el-select
          v-model="query.category"
          clearable
          filterable
          allow-create
          placeholder="全部分类"
        >
          <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" style="width: 170px">
        <el-select v-model="query.status" clearable placeholder="全部状态">
          <el-option
            v-for="item in commonStatusOptions"
            :key="item.code"
            :label="item.desc"
            :value="Number(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="form-actions">
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openCreate">新增标签</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="name" label="标签名称" min-width="150" />
      <el-table-column label="作用域" min-width="120">
        <template #default="{ row }">{{ dictText(tagScopeOptions, row.scope) }}</template>
      </el-table-column>
      <el-table-column prop="category" label="分类" min-width="130" show-overflow-tooltip />
      <el-table-column prop="icon" label="图标" min-width="120" show-overflow-tooltip />
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ dictText(commonStatusOptions, row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="deleteItem(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page.pageNum"
        v-model:page-size="page.pageSize"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        @current-change="loadData"
        @size-change="search"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑标签' : '新增标签'" width="560px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="标签名称" required>
          <el-input v-model="form.name" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="作用域" required>
          <el-select
            v-model="form.scope"
            placeholder="请选择作用域（必填）"
            @change="loadCategories"
          >
            <el-option
              v-for="item in tagScopeOptions"
              :key="item.code"
              :label="item.desc"
              :value="String(item.code)"
            />
          </el-select>
          <div class="form-hint">决定标签出现在哪里，提交后不建议随意改动</div>
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="form.category"
            clearable
            filterable
            allow-create
            placeholder="可选，作用域内的二级分组"
          >
            <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
          </el-select>
          <div class="form-hint">仅用于在选择器里把相关标签归到一起，可留空</div>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="图标名或资源地址" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="99999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio-button
              v-for="item in commonStatusOptions"
              :key="item.code"
              :value="Number(item.code)"
            >
              {{ item.desc }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  createAdminTag,
  deleteAdminTag,
  getAdminTagPage,
  getTagCategories,
  updateAdminTag,
  type AdminTagItem,
  type AdminTagQuery,
  type TagPayload,
} from "@/api/common";
import { getCommonStatusDict, getTagScopeDict, type DictItem } from "@/api/dict";
import { findDictDesc } from "@/composables/useDictOptions";

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const list = ref<AdminTagItem[]>([]);
const total = ref(0);
const editing = ref<AdminTagItem | null>(null);
const categoryOptions = ref<string[]>([]);
const tagScopeOptions = ref<DictItem[]>([]);
const commonStatusOptions = ref<DictItem[]>([]);

const page = reactive({ pageNum: 1, pageSize: 10 });
const query = reactive<AdminTagQuery>({
  pageNum: 1,
  pageSize: 10,
  name: "",
  scope: undefined,
  category: undefined,
  status: undefined,
});
const form = reactive<TagPayload>({
  name: "",
  scope: "SCENIC",
  category: "",
  icon: "",
  sortOrder: 0,
  status: 1,
});

function dictText(options: DictItem[], code: unknown): string {
  return findDictDesc(options, code, "-");
}

async function loadDictionaries(): Promise<void> {
  [tagScopeOptions.value, commonStatusOptions.value] = await Promise.all([
    getTagScopeDict(),
    getCommonStatusDict(),
  ]);
}

async function loadCategories(scope = query.scope): Promise<void> {
  categoryOptions.value = await getTagCategories(scope || undefined);
}

async function onScopeFilterChange(): Promise<void> {
  query.category = undefined;
  await loadCategories(query.scope);
  search();
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getAdminTagPage({
      ...query,
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      name: query.name?.trim() || undefined,
    });
    list.value = res.records || [];
    total.value = res.total || 0;
  } finally {
    loading.value = false;
  }
}

function search(): void {
  page.pageNum = 1;
  void loadData();
}

function resetQuery(): void {
  page.pageNum = 1;
  query.name = "";
  query.scope = undefined;
  query.category = undefined;
  query.status = undefined;
  void loadCategories();
  void loadData();
}

function resetForm(): void {
  Object.assign(form, {
    name: "",
    scope: "SCENIC",
    category: "",
    icon: "",
    sortOrder: 0,
    status: 1,
  });
}

function openCreate(): void {
  editing.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEdit(item: AdminTagItem): void {
  editing.value = item;
  Object.assign(form, {
    name: item.name,
    scope: item.scope || "SCENIC",
    category: item.category || "",
    icon: item.icon || "",
    sortOrder: item.sortOrder ?? 0,
    status: item.status ?? 1,
  });
  void loadCategories(form.scope);
  dialogVisible.value = true;
}

async function save(): Promise<void> {
  if (!form.name.trim()) {
    ElMessage.warning("请输入标签名称");
    return;
  }
  saving.value = true;
  try {
    const payload: TagPayload = {
      name: form.name.trim(),
      scope: form.scope || undefined,
      category: form.category || undefined,
      icon: form.icon || undefined,
      sortOrder: form.sortOrder,
      status: form.status,
    };
    if (editing.value) {
      await updateAdminTag(editing.value.id, payload);
    } else {
      await createAdminTag(payload);
    }
    ElMessage.success("保存成功");
    dialogVisible.value = false;
    await loadData();
    await loadCategories();
  } finally {
    saving.value = false;
  }
}

async function deleteItem(item: AdminTagItem): Promise<void> {
  await ElMessageBox.confirm(`确认删除标签「${item.name}」？`, "删除确认", { type: "warning" });
  await deleteAdminTag(item.id);
  ElMessage.success("删除成功");
  await loadData();
}

onMounted(async () => {
  await loadDictionaries();
  await loadCategories();
  await loadData();
});
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
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

.filter-panel {
  position: relative;
}

.filter-panel {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
.filter-panel .el-form-item {
  margin-bottom: 16px;
}
.filter-panel .form-actions {
  margin-left: auto;
}

.label-with-hint {
  cursor: help;
  border-bottom: 1px dashed #c0c4cc;
}

.form-hint {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.4;
  color: #909399;
}
</style>
