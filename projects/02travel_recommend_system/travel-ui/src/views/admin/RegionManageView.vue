<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">地区管理</div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="地区名称">
          <el-input v-model="query.name" clearable style="width: 220px" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="层级">
          <el-select v-model="query.level" clearable style="width: 120px" placeholder="全部">
            <el-option label="省" :value="1" />
            <el-option label="市" :value="2" />
            <el-option label="区县" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="热门">
          <el-select v-model="query.isHot" clearable style="width: 120px" placeholder="全部">
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="openCreate">新增地区</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="regionList">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="地区名称" min-width="160" />
        <el-table-column prop="shortName" label="简称" width="120" />
        <el-table-column label="层级" width="90">
          <template #default="scope">{{ levelText(scope.row.level) }}</template>
        </el-table-column>
        <el-table-column prop="code" label="编码" width="120" />
        <el-table-column prop="pinyin" label="拼音" width="120" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="热门" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.isHot === 1 ? 'success' : 'info'">
              {{ scope.row.isHot === 1 ? "是" : "否" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="warning" @click="openEdit(scope.row)">编辑</el-button>
            <el-popconfirm
              title="确认删除该地区吗？"
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
          @current-change="loadRegionList"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="formVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="88px">
        <el-form-item label="层级" prop="level">
          <el-select v-model="formModel.level" style="width: 100%" @change="onLevelChange">
            <el-option label="省" :value="1" />
            <el-option label="市" :value="2" />
            <el-option label="区县" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="父级ID" prop="parentId">
          <!-- 省：禁用，固定显示"无（顶级）" -->
          <el-input v-if="formModel.level === 1" value="无（顶级）" disabled />

          <!-- 市：从省列表选 -->
          <el-select
            v-else-if="formModel.level === 2"
            v-model="formModel.parentId"
            placeholder="请选择所属省份"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in provinceList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>

          <!-- 区县：从市列表选 -->
          <el-select
            v-else-if="formModel.level === 3"
            v-model="formModel.parentId"
            placeholder="请选择所属城市"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in cityList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="地区名称" prop="name">
          <el-input v-model="formModel.name" placeholder="请输入地区名称" />
        </el-form-item>
        <el-form-item label="简称" prop="shortName">
          <el-input v-model="formModel.shortName" placeholder="请输入简称" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="formModel.code" placeholder="请输入编码" />
        </el-form-item>
        <el-form-item label="拼音" prop="pinyin">
          <el-input v-model="formModel.pinyin" placeholder="请输入拼音" />
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input-number
            v-model="formModel.longitude"
            :min="-180"
            :max="180"
            :step="0.0001"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input-number
            v-model="formModel.latitude"
            :min="-90"
            :max="90"
            :step="0.0001"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formModel.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="热门" prop="isHot">
          <el-select v-model="formModel.isHot" style="width: 100%">
            <el-option label="否" :value="0" />
            <el-option label="是" :value="1" />
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
  createAdminRegion,
  deleteAdminRegion,
  getAdminRegionPage,
  updateAdminRegion,
  getRegionTree,
  type AdminRegionItem,
  type AdminRegionQuery,
  type RegionPayload,
  type CommonRegionNode,
} from "@/api/common";

const loading = ref(false);
const submitting = ref(false);
const total = ref(0);
const regionList = ref<AdminRegionItem[]>([]);
const regionTree = ref<CommonRegionNode[]>([]);

const provinceList = computed(() => {
  return regionTree.value;
});

const cityList = computed(() => {
  return regionTree.value.flatMap((p) => p.children || []);
});

const formVisible = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInstance>();

const query = reactive<AdminRegionQuery>({
  pageNum: 1,
  pageSize: 10,
  parentId: undefined,
  name: "",
  level: undefined,
  code: "",
  isHot: undefined,
});

const formModel = reactive<RegionPayload>({
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

const formRules: FormRules<RegionPayload> = {
  name: [{ required: true, message: "请输入地区名称", trigger: "blur" }],
  level: [{ required: true, message: "请选择层级", trigger: "change" }],
};

const onLevelChange = (val: number) => {
  formModel.parentId = val === 1 ? 0 : 0; // default 0 if not set
};

const fetchRegionTree = async (): Promise<void> => {
  try {
    regionTree.value = await getRegionTree();
  } catch (error) {
    ElMessage.error("地区树加载失败");
    console.error("加载地区树失败", error);
  }
};

const dialogTitle = computed(() => (editingId.value ? "编辑地区" : "新增地区"));

function levelText(level: number): string {
  if (level === 1) return "省";
  if (level === 2) return "市";
  if (level === 3) return "区县";
  return "-";
}

async function loadRegionList(): Promise<void> {
  loading.value = true;
  try {
    const page = await getAdminRegionPage({
      ...query,
      name: query.name || undefined,
      code: query.code || undefined,
    });
    regionList.value = page.records;
    total.value = page.total;
  } catch {
    ElMessage.error("地区列表加载失败");
  } finally {
    loading.value = false;
  }
}

function onSearch(): void {
  query.pageNum = 1;
  void loadRegionList();
}

function onReset(): void {
  query.name = "";
  query.level = undefined;
  query.isHot = undefined;
  query.code = "";
  query.parentId = undefined;
  query.pageNum = 1;
  void loadRegionList();
}

function onSizeChange(): void {
  query.pageNum = 1;
  void loadRegionList();
}

function resetFormModel(): void {
  formModel.parentId = 0;
  formModel.name = "";
  formModel.shortName = "";
  formModel.level = 1;
  formModel.code = "";
  formModel.pinyin = "";
  formModel.longitude = undefined;
  formModel.latitude = undefined;
  formModel.sortOrder = 0;
  formModel.isHot = 0;
}

function openCreate(): void {
  editingId.value = null;
  resetFormModel();
  formVisible.value = true;
}

function openEdit(row: AdminRegionItem): void {
  editingId.value = row.id;
  formModel.parentId = row.parentId;
  formModel.name = row.name;
  formModel.shortName = row.shortName ?? "";
  formModel.level = row.level;
  formModel.code = row.code ?? "";
  formModel.pinyin = row.pinyin ?? "";
  formModel.longitude = row.longitude;
  formModel.latitude = row.latitude;
  formModel.sortOrder = row.sortOrder ?? 0;
  formModel.isHot = row.isHot ?? 0;
  formVisible.value = true;
}

async function handleSubmit(): Promise<void> {
  const form = formRef.value;
  if (!form) {
    return;
  }
  await form.validate();
  submitting.value = true;
  try {
    const payload: RegionPayload = {
      ...formModel,
      shortName: formModel.shortName || undefined,
      code: formModel.code || undefined,
      pinyin: formModel.pinyin || undefined,
    };
    if (editingId.value) {
      await updateAdminRegion(editingId.value, payload);
      ElMessage.success("地区更新成功");
    } else {
      await createAdminRegion(payload);
      ElMessage.success("地区创建成功");
    }
    formVisible.value = false;
    await Promise.all([loadRegionList(), fetchRegionTree()]);
  } catch {
    ElMessage.error("保存失败");
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id: number): Promise<void> {
  try {
    await deleteAdminRegion(id);
    ElMessage.success("删除成功");
    if (regionList.value.length === 1 && query.pageNum > 1) {
      query.pageNum -= 1;
    }
    await Promise.all([loadRegionList(), fetchRegionTree()]);
  } catch {
    ElMessage.error("删除失败");
  }
}

onMounted(() => {
  void loadRegionList();
  void fetchRegionTree();
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

.page-container {
  padding: 0px;
}
</style>
