<template>
  <div class="admin-page">
    <el-form :model="query" class="filter-panel" inline>
      <el-form-item label="地区名称">
        <el-input v-model="query.name" clearable placeholder="输入地区名称" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="层级">
        <el-select v-model="query.level" clearable placeholder="全部层级">
          <el-option
            v-for="item in regionLevelOptions"
            :key="item.code"
            :label="item.desc"
            :value="Number(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="父级">
        <el-cascader
          v-model="queryParentPath"
          clearable
          filterable
          :options="regionTree"
          :props="cascaderProps"
          placeholder="先选一级再选二级"
          @change="syncParentQuery"
        />
      </el-form-item>
      <el-form-item label="编码">
        <el-input v-model="query.code" clearable placeholder="行政区划编码" @keyup.enter="search" />
      </el-form-item>
      <el-form-item label="热门">
        <el-select v-model="query.isHot" clearable placeholder="全部">
          <el-option
            v-for="item in yesNoOptions"
            :key="item.code"
            :label="item.desc"
            :value="Number(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="form-actions">
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openCreate">新增地区</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe row-key="id">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="name" label="地区名称" min-width="150" />
      <el-table-column prop="shortName" label="简称" min-width="110" />
      <el-table-column label="层级" width="100">
        <template #default="{ row }">{{ dictText(regionLevelOptions, row.level) }}</template>
      </el-table-column>
      <el-table-column prop="parentId" label="父级ID" width="100" />
      <el-table-column prop="code" label="编码" min-width="130" />
      <el-table-column prop="pinyin" label="拼音" min-width="130" show-overflow-tooltip />
      <el-table-column label="经纬度" min-width="170">
        <template #default="{ row }">
          {{ row.longitude ?? "-" }}, {{ row.latitude ?? "-" }}
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="90" />
      <el-table-column label="热门" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isHot === 1 ? 'warning' : 'info'">
            {{ dictText(yesNoOptions, row.isHot) }}
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

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑地区' : '新增地区'" width="720px">
      <el-form :model="form" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="地区名称" required>
              <el-input v-model="form.name" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="简称">
              <el-input v-model="form.shortName" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="层级" required>
              <el-select v-model="form.level" @change="onFormLevelChange">
                <el-option
                  v-for="item in regionLevelOptions"
                  :key="item.code"
                  :label="item.desc"
                  :value="Number(item.code)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="父级地区" required>
              <el-cascader
                v-model="formParentPath"
                :disabled="form.level === 1"
                clearable
                filterable
                :options="parentOptions"
                :props="cascaderProps"
                :placeholder="form.level === 1 ? '一级地区父级固定为0' : '先选一级再选二级'"
                @change="syncFormParent"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="编码">
              <el-input v-model="form.code" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拼音">
              <el-input v-model="form.pinyin" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度">
              <el-input-number
                v-model="form.longitude"
                :precision="6"
                :step="0.000001"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度">
              <el-input-number
                v-model="form.latitude"
                :precision="6"
                :step="0.000001"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" :max="99999" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="热门">
              <el-radio-group v-model="form.isHot">
                <el-radio-button
                  v-for="item in yesNoOptions"
                  :key="item.code"
                  :value="Number(item.code)"
                >
                  {{ item.desc }}
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  createAdminRegion,
  deleteAdminRegion,
  getAdminRegionPage,
  getRegionTree,
  updateAdminRegion,
  type AdminRegionItem,
  type AdminRegionQuery,
  type CommonRegionNode,
  type RegionPayload,
} from "@/api/common";
import { getRegionLevelDict, getYesNoFlagDict, type DictItem } from "@/api/dict";
import { findDictDesc } from "@/composables/useDictOptions";

const cascaderProps = {
  value: "id",
  label: "name",
  children: "children",
  checkStrictly: true,
  emitPath: true,
};
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const list = ref<AdminRegionItem[]>([]);
const regionTree = ref<CommonRegionNode[]>([]);
const total = ref(0);
const editing = ref<AdminRegionItem | null>(null);
const regionLevelOptions = ref<DictItem[]>([]);
const yesNoOptions = ref<DictItem[]>([]);
const queryParentPath = ref<number[]>([]);
const formParentPath = ref<number[]>([]);

const page = reactive({ pageNum: 1, pageSize: 10 });
const query = reactive<AdminRegionQuery>({
  pageNum: 1,
  pageSize: 10,
  name: "",
  level: undefined,
  code: "",
  isHot: undefined,
  parentId: undefined,
});
const form = reactive<RegionPayload>({
  parentId: 0,
  name: "",
  shortName: "",
  level: 1,
  code: "",
  pinyin: "",
  longitude: undefined,
  latitude: undefined,
  sortOrder: 0,
  isHot: 0,
});

const parentOptions = computed(() => {
  if (form.level === 2) {
    return regionTree.value.map((node) => ({ ...node, children: undefined }));
  }
  if (form.level === 3) {
    return regionTree.value.map((province) => ({
      ...province,
      children: province.children?.map((city) => ({ ...city, children: undefined })) || [],
    }));
  }
  return [];
});

function dictText(options: DictItem[], code: unknown): string {
  return findDictDesc(options, code, "-");
}

async function loadDictionaries(): Promise<void> {
  [regionLevelOptions.value, yesNoOptions.value] = await Promise.all([
    getRegionLevelDict(),
    getYesNoFlagDict(),
  ]);
}

async function refreshTree(): Promise<void> {
  regionTree.value = await getRegionTree();
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getAdminRegionPage({
      ...query,
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      name: query.name?.trim() || undefined,
      code: query.code?.trim() || undefined,
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
  query.level = undefined;
  query.code = "";
  query.isHot = undefined;
  query.parentId = undefined;
  queryParentPath.value = [];
  void loadData();
}

function syncParentQuery(value: unknown): void {
  const path = Array.isArray(value) ? (value as number[]) : [];
  query.parentId = path.length ? path[path.length - 1] : undefined;
  search();
}

function findPathById(nodes: CommonRegionNode[], id?: number): number[] {
  if (!id) return [];
  for (const node of nodes) {
    if (node.id === id) return [node.id];
    const childPath = findPathById(node.children || [], id);
    if (childPath.length) return [node.id, ...childPath];
  }
  return [];
}

function resetForm(): void {
  Object.assign(form, {
    parentId: 0,
    name: "",
    shortName: "",
    level: 1,
    code: "",
    pinyin: "",
    longitude: undefined,
    latitude: undefined,
    sortOrder: 0,
    isHot: 0,
  });
  formParentPath.value = [];
}

function openCreate(): void {
  editing.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEdit(item: AdminRegionItem): void {
  editing.value = item;
  Object.assign(form, {
    parentId: item.parentId ?? 0,
    name: item.name,
    shortName: item.shortName || "",
    level: item.level,
    code: item.code || "",
    pinyin: item.pinyin || "",
    longitude: item.longitude,
    latitude: item.latitude,
    sortOrder: item.sortOrder ?? 0,
    isHot: item.isHot ?? 0,
  });
  formParentPath.value = findPathById(regionTree.value, item.parentId);
  dialogVisible.value = true;
}

function onFormLevelChange(): void {
  form.parentId = form.level === 1 ? 0 : (undefined as unknown as number);
  formParentPath.value = [];
}

function syncFormParent(value: unknown): void {
  const path = Array.isArray(value) ? (value as number[]) : [];
  form.parentId = path.length
    ? path[path.length - 1]
    : form.level === 1
      ? 0
      : (undefined as unknown as number);
}

async function save(): Promise<void> {
  if (!form.name.trim()) {
    ElMessage.warning("请输入地区名称");
    return;
  }
  if (form.level !== 1 && !form.parentId) {
    ElMessage.warning("请选择父级地区");
    return;
  }
  saving.value = true;
  try {
    const payload: RegionPayload = {
      parentId: form.level === 1 ? 0 : form.parentId,
      name: form.name.trim(),
      shortName: form.shortName || undefined,
      level: form.level,
      code: form.code || undefined,
      pinyin: form.pinyin || undefined,
      longitude: form.longitude,
      latitude: form.latitude,
      sortOrder: form.sortOrder,
      isHot: form.isHot,
    };
    if (editing.value) {
      await updateAdminRegion(editing.value.id, payload);
    } else {
      await createAdminRegion(payload);
    }
    ElMessage.success("保存成功");
    dialogVisible.value = false;
    await Promise.all([loadData(), refreshTree()]);
  } finally {
    saving.value = false;
  }
}

async function deleteItem(item: AdminRegionItem): Promise<void> {
  await ElMessageBox.confirm(
    `确认删除地区「${item.name}」？有子地区时后端会阻止删除。`,
    "删除确认",
    {
      type: "warning",
    }
  );
  await deleteAdminRegion(item.id);
  ElMessage.success("删除成功");
  await Promise.all([loadData(), refreshTree()]);
}

onMounted(async () => {
  await Promise.all([loadDictionaries(), refreshTree()]);
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
</style>
