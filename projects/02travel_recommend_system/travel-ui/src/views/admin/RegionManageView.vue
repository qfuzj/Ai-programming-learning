<template>
  <div class="admin-page">
    <el-form :model="filter" class="filter-panel" inline>
      <el-form-item label="地区名称">
        <el-input
          v-model="filter.keyword"
          clearable
          placeholder="按名称/简称/拼音/编码筛选"
          @keyup.enter="onFilterChange"
          @clear="onFilterChange"
          @input="onFilterChange"
        />
      </el-form-item>
      <el-form-item label="层级">
        <el-select v-model="filter.level" clearable placeholder="全部层级" @change="onFilterChange">
          <el-option
            v-for="item in regionLevelOptions"
            :key="item.code"
            :label="item.desc"
            :value="Number(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="热门">
        <el-select v-model="filter.isHot" clearable placeholder="全部" @change="onFilterChange">
          <el-option
            v-for="item in yesNoOptions"
            :key="item.code"
            :label="item.desc"
            :value="Number(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="form-actions">
        <el-button @click="resetFilter">重置</el-button>
        <el-button @click="expandAll(true)">全部展开</el-button>
        <el-button @click="expandAll(false)">全部收起</el-button>
        <el-button type="success" @click="openCreate(null)">新增一级地区</el-button>
      </el-form-item>
    </el-form>

    <div class="table-shell">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="filteredTree"
        border
        stripe
        row-key="id"
        :tree-props="{ children: 'children' }"
        :default-expand-all="expandAllFlag"
        class="region-table"
      >
        <el-table-column prop="name" label="地区名称" min-width="220" />
        <el-table-column prop="shortName" label="简称" min-width="110" />
        <el-table-column label="层级" width="90">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" size="small">
              {{ dictText(regionLevelOptions, row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="编码" min-width="120" />
        <el-table-column prop="pinyin" label="拼音" min-width="120" show-overflow-tooltip />
        <el-table-column label="经纬度" min-width="170">
          <template #default="{ row }">
            {{ row.longitude ?? "-" }}, {{ row.latitude ?? "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="热门" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isHot === 1 ? 'warning' : 'info'" size="small">
              {{ dictText(yesNoOptions, row.isHot) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="{ row }">
            <el-button
              v-if="row.level !== undefined && row.level < 3"
              link
              type="success"
              @click="openCreate(row)"
            >
              新增子级
            </el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑地区' : `新增${dictText(regionLevelOptions, form.level)}`"
      width="720px"
    >
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
              <el-select v-model="form.level" disabled>
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
            <el-form-item label="父级地区">
              <el-input :model-value="parentDisplay" disabled placeholder="无父级" />
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
  getRegionTree,
  updateAdminRegion,
  type AdminRegionItem,
  type CommonRegionNode,
  type RegionPayload,
} from "@/api/common";
import { getRegionLevelDict, getYesNoFlagDict, type DictItem } from "@/api/dict";
import { findDictDesc } from "@/composables/useDictOptions";

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const tree = ref<CommonRegionNode[]>([]);
const editing = ref<CommonRegionNode | null>(null);
const parent = ref<CommonRegionNode | null>(null);
const regionLevelOptions = ref<DictItem[]>([]);
const yesNoOptions = ref<DictItem[]>([]);
const expandAllFlag = ref(true);
const tableRef = ref();

const filter = reactive({
  keyword: "",
  level: undefined as number | undefined,
  isHot: undefined as number | undefined,
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

const parentDisplay = computed(() => {
  if (!parent.value) return "（一级地区）";
  return parent.value.name;
});

const filteredTree = computed(() => {
  const hasFilter =
    !!filter.keyword?.trim() || filter.level !== undefined || filter.isHot !== undefined;
  if (!hasFilter) return tree.value;
  return filterNodes(tree.value, filter.keyword?.trim() || "", filter.level, filter.isHot);
});

function filterNodes(
  nodes: CommonRegionNode[],
  keyword: string,
  level: number | undefined,
  isHot: number | undefined
): CommonRegionNode[] {
  const lower = keyword.toLowerCase();
  const result: CommonRegionNode[] = [];
  for (const node of nodes) {
    const matchKeyword =
      !keyword ||
      node.name?.toLowerCase().includes(lower) ||
      node.shortName?.toLowerCase().includes(lower) ||
      node.pinyin?.toLowerCase().includes(lower) ||
      node.code?.toLowerCase().includes(lower);
    const matchLevel = level === undefined || node.level === level;
    const matchHot = isHot === undefined || node.isHot === isHot;

    const filteredChildren = node.children?.length
      ? filterNodes(node.children, keyword, level, isHot)
      : [];

    if ((matchKeyword && matchLevel && matchHot) || filteredChildren.length > 0) {
      result.push({ ...node, children: filteredChildren });
    }
  }
  return result;
}

function dictText(options: DictItem[], code: unknown): string {
  return findDictDesc(options, code, "-");
}

function levelTagType(level?: number): "primary" | "success" | "info" | "warning" {
  if (level === 1) return "primary";
  if (level === 2) return "success";
  return "info";
}

async function loadDictionaries(): Promise<void> {
  [regionLevelOptions.value, yesNoOptions.value] = await Promise.all([
    getRegionLevelDict(),
    getYesNoFlagDict(),
  ]);
}

async function loadTree(): Promise<void> {
  loading.value = true;
  try {
    tree.value = await getRegionTree();
  } finally {
    loading.value = false;
  }
}

function onFilterChange(): void {
  // computed 自动响应，无需主动调用，但保留钩子以便后续接入服务端筛选。
}

function resetFilter(): void {
  filter.keyword = "";
  filter.level = undefined;
  filter.isHot = undefined;
}

function expandAll(expand: boolean): void {
  expandAllFlag.value = expand;
  walkTree(filteredTree.value, (node) => {
    tableRef.value?.toggleRowExpansion?.(node, expand);
  });
}

function walkTree(nodes: CommonRegionNode[], visit: (node: CommonRegionNode) => void): void {
  for (const node of nodes) {
    visit(node);
    if (node.children?.length) walkTree(node.children, visit);
  }
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
}

function openCreate(parentNode: CommonRegionNode | null): void {
  if (parentNode && (parentNode.level ?? 0) >= 3) {
    ElMessage.warning("已是最末级，无法继续添加子级");
    return;
  }
  editing.value = null;
  parent.value = parentNode;
  resetForm();
  form.parentId = parentNode?.id ?? 0;
  form.level = (parentNode?.level ?? 0) + 1;
  dialogVisible.value = true;
}

function openEdit(item: CommonRegionNode): void {
  editing.value = item;
  parent.value = findParent(tree.value, item.id);
  Object.assign(form, {
    parentId: item.parentId ?? 0,
    name: item.name,
    shortName: item.shortName || "",
    level: item.level ?? 1,
    code: item.code || "",
    pinyin: item.pinyin || "",
    longitude: item.longitude,
    latitude: item.latitude,
    sortOrder: item.sortOrder ?? 0,
    isHot: item.isHot ?? 0,
  });
  dialogVisible.value = true;
}

function findParent(nodes: CommonRegionNode[], id: number): CommonRegionNode | null {
  for (const node of nodes) {
    if (node.children?.some((c) => c.id === id)) return node;
    const found = node.children ? findParent(node.children, id) : null;
    if (found) return found;
  }
  return null;
}

async function save(): Promise<void> {
  if (!form.name.trim()) {
    ElMessage.warning("请输入地区名称");
    return;
  }
  saving.value = true;
  try {
    const payload: RegionPayload = {
      parentId: form.parentId ?? 0,
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
    await loadTree();
  } finally {
    saving.value = false;
  }
}

async function deleteItem(item: AdminRegionItem | CommonRegionNode): Promise<void> {
  await ElMessageBox.confirm(
    `确认删除地区「${item.name}」？有子地区时后端会阻止删除。`,
    "删除确认",
    {
      type: "warning",
    }
  );
  await deleteAdminRegion(item.id);
  ElMessage.success("删除成功");
  await loadTree();
}

onMounted(async () => {
  await Promise.all([loadDictionaries(), loadTree()]);
});
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  width: 100%;
  min-width: 0;
  overflow: hidden;
}

.filter-panel {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  gap: 0 14px;
  align-items: center;
  padding: 16px 16px 0;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 8px;
}

.filter-panel :deep(.el-form-item) {
  margin-right: 0;
  margin-bottom: 16px;
}

.filter-panel :deep(.el-input),
.filter-panel :deep(.el-select) {
  width: 200px;
}

.filter-panel .form-actions {
  margin-left: auto;
}

.table-shell {
  width: 100%;
  max-width: 100%;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 8px;
}

.region-table {
  width: 100%;
}
</style>
