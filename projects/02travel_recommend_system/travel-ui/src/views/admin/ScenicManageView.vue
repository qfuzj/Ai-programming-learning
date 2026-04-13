<!-- 景点管理页：支持列表查询、查看详情、新增/编辑、删除。 -->
<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">景点管理</div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="景点名" clearable style="width: 200px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="省份">
          <el-select v-model="query.provinceId" clearable placeholder="请选择省份" style="width: 160px"
            @change="onProvinceChange">
            <el-option v-for="item in provinceOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-select v-model="query.cityId" clearable placeholder="请选择城市" style="width: 160px"
            :disabled="!query.provinceId">
            <el-option v-for="item in cityOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="openCreateDialog">新增景点</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="scenicList" style="margin-top: 12px">
        <el-table-column prop="name" label="景点名称" min-width="100" />
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <el-image v-if="row.coverImage" :src="row.coverImage" fit="cover"
              style="width: 72px; height: 48px; border-radius: 6px" />
            <span v-else class="text-muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="120">
          <template #default="{ row }">
            <template v-if="row.tags?.length">
              <el-tag v-for="tag in row.tags.slice(0, 3)" :key="tag" size="small" class="tag-gap">
                {{ tag }}
              </el-tag>
            </template>
            <span v-else class="text-muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="isOnlineStatus(row.status) ? 'success' : 'info'">
              {{ isOnlineStatus(row.status) ? "上架" : "下架" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="regionName" label="地区" min-width="60" />
        <el-table-column label="评分" width="70">
          <template #default="{ row }">
            {{ Number(row.score ?? 0).toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column prop="level" label="等级" width="60">
          <template #default="{ row }">
            {{ row.level || "未设置" }}
          </template>
        </el-table-column>
        <el-table-column prop="address" label="详细地址" min-width="80" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.address || "暂无" }}
          </template>
        </el-table-column>
        <el-table-column prop="openTime" label="开放时间" min-width="60" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.openTime || "暂无" }}
          </template>
        </el-table-column>
        <el-table-column label="票价" width="80" align="right">
          <template #default="{ row }">
            {{ formatTicketPrice(row.ticketPrice) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetailDialog(row)">查看详情</el-button>
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除该景点吗？" confirm-button-text="确认" cancel-button-text="取消"
              @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" @current-change="loadScenicList"
          @size-change="onSizeChange" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="景点详情" width="760px" :close-on-click-modal="true" destroy-on-close>
      <el-card v-if="detailData" class="detail-card" shadow="never">
        <template #header>
          <div class="detail-title-row">
            <span class="detail-title">{{ detailData.name }}</span>
            <el-tag :type="detailData.status === 1 ? 'success' : 'info'">
              {{ detailData.status === 1 ? "上架" : "下架" }}
            </el-tag>
          </div>
        </template>

        <el-row :gutter="16">
          <el-col :span="10">
            <el-image :src="detailData.coverImage || ''" fit="cover"
              style="width: 100%; height: 190px; border-radius: 8px">
              <template #error>
                <div class="image-empty">暂无封面</div>
              </template>
            </el-image>
          </el-col>
          <el-col :span="14">
            <div class="info-item"><span class="label">地区：</span>{{ detailData.regionName || "未知" }}</div>
            <div class="info-item"><span class="label">评分：</span>{{ detailData.score ?? 0 }}</div>
            <div class="info-item"><span class="label">等级：</span>{{ detailData.level || "未设置" }}</div>
            <div class="info-item">
              <span class="label">标签：</span>
              <template v-if="detailData.tags?.length">
                <el-tag v-for="tag in detailData.tags" :key="tag" size="small" class="tag-gap">{{ tag }}</el-tag>
              </template>
              <span v-else class="text-muted">暂无</span>
            </div>
          </el-col>
        </el-row>

        <el-divider />
        <div class="info-block">
          <div class="label">简介</div>
          <div class="value">{{ detailData.intro || "暂无简介" }}</div>
        </div>
        <div class="info-block">
          <div class="label">地址</div>
          <div class="value">{{ detailData.address || "暂无地址" }}</div>
        </div>
      </el-card>
    </el-dialog>

    <el-dialog v-model="formVisible" :title="isEdit ? '编辑景点' : '新增景点'" width="640px" :close-on-click-modal="false"
      destroy-on-close>
      <el-form ref="formRef" :model="formModel" :rules="rules" label-width="100px">
        <el-form-item label="景点名称" prop="name">
          <el-input v-model="formModel.name" placeholder="请输入景点名称" />
        </el-form-item>
        <el-form-item label="地区" prop="regionId">
          <el-select v-model="formModel.regionId" placeholder="请选择地区" filterable>
            <el-option v-for="item in regionOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图URL" prop="coverImage">
          <el-input v-model="formModel.coverImage" placeholder="请输入封面图 URL" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="formModel.description" type="textarea" :rows="3" placeholder="请输入简介" />
        </el-form-item>
        <el-form-item label="标签" prop="tagIds">
          <el-select v-model="formModel.tagIds" multiple placeholder="请选择标签" filterable>
            <el-option v-for="item in tagOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formModel.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import {
  createAdminScenic,
  deleteAdminScenic,
  getAdminScenicPage,
  getScenicDetail,
  type ScenicCreatePayload,
  type ScenicDetail,
  type ScenicItem,
  type ScenicQuery,
  type ScenicUpdatePayload,
  updateAdminScenic,
} from "@/api/scenic";
import { getRegionTree, getTags, type CommonTagItem, type CommonRegionNode } from "@/api/common";

interface OptionItem {
  id: number;
  name: string;
}

interface SearchQuery extends ScenicQuery {
  provinceId?: number;
  cityId?: number;
}

interface ScenicFormModel {
  id?: number;
  name: string;
  regionId?: number;
  coverImage: string;
  description: string;
  tagIds: number[];
  status: number;
}

const loading = ref(false);
const submitLoading = ref(false);
const scenicList = ref<ScenicItem[]>([]);
const total = ref(0);

const detailVisible = ref(false);
const detailData = ref<ScenicDetail | null>(null);

const formVisible = ref(false);
const isEdit = ref(false);
const formRef = ref<FormInstance>();

const regionOptions = ref<OptionItem[]>([]);
const provinceOptions = ref<CommonRegionNode[]>([]);
const cityOptions = ref<CommonRegionNode[]>([]);
const tagOptions = ref<CommonTagItem[]>([]);

const query = reactive<SearchQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  provinceId: undefined,
  cityId: undefined,
});

const formModel = reactive<ScenicFormModel>({
  name: "",
  regionId: undefined,
  coverImage: "",
  description: "",
  tagIds: [],
  status: 1,
});

const rules: FormRules<ScenicFormModel> = {
  name: [{ required: true, message: "请输入景点名称", trigger: "blur" }],
  regionId: [{ required: true, message: "请选择地区", trigger: "change" }],
};

function isOnlineStatus(status: unknown): boolean {
  return Number(status) === 1;
}

function formatTicketPrice(ticketPrice?: number): string {
  if (ticketPrice == null) {
    return "暂无";
  }
  if (Number(ticketPrice) <= 0) {
    return "免费";
  }
  return `￥${Number(ticketPrice).toFixed(0)}`;
}

function flattenRegions(list: CommonRegionNode[]): OptionItem[] {
  const result: OptionItem[] = [];
  const walk = (nodes: CommonRegionNode[]): void => {
    nodes.forEach((node) => {
      result.push({ id: Number(node.id), name: node.name });
      if (node.children?.length) {
        walk(node.children);
      }
    });
  };
  walk(list);
  return result;
}

function resetFormModel(): void {
  formModel.id = undefined;
  formModel.name = "";
  formModel.regionId = undefined;
  formModel.coverImage = "";
  formModel.description = "";
  formModel.tagIds = [];
  formModel.status = 1;
}

async function loadMetaData(): Promise<void> {
  const [regions, tags] = await Promise.all([getRegionTree(), getTags()]);
  provinceOptions.value = regions;
  regionOptions.value = flattenRegions(regions);
  tagOptions.value = tags;
}

async function loadScenicList(): Promise<void> {
  loading.value = true;
  try {
    const result = await getAdminScenicPage(buildSearchParams());
    scenicList.value = result.records;
    total.value = result.total;
  } finally {
    loading.value = false;
  }
}

function buildSearchParams(): ScenicQuery {
  return {
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    keyword: query.keyword || undefined,
    // 城市优先；仅选择省份时按省份过滤；都不选则不传。
    regionId: query.cityId ?? query.provinceId ?? undefined,
  };
}

function onSearch(): void {
  query.pageNum = 1;
  void loadScenicList();
}

function onProvinceChange(provinceId?: number): void {
  const currentProvince = provinceOptions.value.find((item) => Number(item.id) === Number(provinceId));
  cityOptions.value = currentProvince?.children ?? [];
  query.cityId = undefined;
}

function handleReset(): void {
  query.keyword = "";
  query.provinceId = undefined;
  query.cityId = undefined;
  cityOptions.value = [];
  query.pageNum = 1;
  void loadScenicList();
}

function onSizeChange(): void {
  query.pageNum = 1;
  void loadScenicList();
}

async function openDetailDialog(row: ScenicItem): Promise<void> {
  try {
    detailData.value = await getScenicDetail(row.id);
    detailVisible.value = true;
  } catch {
    ElMessage.error("获取景点详情失败");
  }
}

function openCreateDialog(): void {
  isEdit.value = false;
  resetFormModel();
  formVisible.value = true;
}

async function openEditDialog(row: ScenicItem): Promise<void> {
  isEdit.value = true;
  resetFormModel();
  try {
    // 编辑前拉取详情，确保回填信息尽可能完整。
    const detail = await getScenicDetail(row.id);
    const tagNameSet = new Set(detail.tags ?? []);
    formModel.id = row.id;
    formModel.name = detail.name;
    formModel.regionId = detail.regionId;
    formModel.coverImage = detail.coverImage || "";
    formModel.description = detail.intro || "";
    formModel.status = detail.status ?? 1;
    formModel.tagIds = tagOptions.value
      .filter((item) => tagNameSet.has(item.name))
      .map((item) => item.id);
    formVisible.value = true;
  } catch {
    ElMessage.error("加载编辑数据失败");
  }
}

async function submitForm(): Promise<void> {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  const regionName =
    regionOptions.value.find((item) => item.id === formModel.regionId)?.name || formModel.name;

  const payload: ScenicCreatePayload = {
    name: formModel.name,
    regionId: Number(formModel.regionId),
    coverImage: formModel.coverImage || undefined,
    description: formModel.description || undefined,
    tagIds: formModel.tagIds,
    status: formModel.status,
    // 后端新增接口要求地址必填，当前页面未单独暴露地址字段，采用地区名兜底。
    address: regionName,
  };

  submitLoading.value = true;
  try {
    if (isEdit.value && formModel.id) {
      const updatePayload: ScenicUpdatePayload = {
        ...payload,
      };
      await updateAdminScenic(formModel.id, updatePayload);
      ElMessage.success("编辑成功");
    } else {
      await createAdminScenic(payload);
      ElMessage.success("新增成功");
    }

    formVisible.value = false;
    void loadScenicList();
  } catch {
    ElMessage.error(isEdit.value ? "编辑失败" : "新增失败");
  } finally {
    submitLoading.value = false;
  }
}

async function handleDelete(row: ScenicItem): Promise<void> {
  try {
    await deleteAdminScenic(row.id);
    ElMessage.success("删除成功");
    if (scenicList.value.length === 1 && query.pageNum && query.pageNum > 1) {
      query.pageNum -= 1;
    }
    await loadScenicList();
  } catch {
    ElMessage.error("删除失败");
  }
}

onMounted(async () => {
  try {
    await loadMetaData();
  } catch {
    ElMessage.warning("基础数据加载失败，部分功能可能受影响");
  }
  await loadScenicList();
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

.tag-gap {
  margin-right: 6px;
}

.text-muted {
  color: #909399;
}

.detail-card {
  border: 1px solid #ebeef5;
}

.detail-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-title {
  font-size: 18px;
  font-weight: 700;
}

.image-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #909399;
  background: #f5f7fa;
  border-radius: 8px;
}

.info-item {
  margin-bottom: 10px;
  line-height: 1.6;
}

.info-block {
  margin-bottom: 14px;
}

.label {
  margin-right: 6px;
  font-weight: 600;
  color: #303133;
}

.value {
  color: #606266;
}

.page-container {
  padding: 0px;
}
</style>
