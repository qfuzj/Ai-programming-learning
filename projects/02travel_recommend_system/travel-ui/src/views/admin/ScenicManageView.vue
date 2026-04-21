<!-- 景点管理页：支持列表查询、查看详情、新增/编辑、删除。 -->
<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">景点管理</div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="景点名"
            clearable
            style="width: 140px"
            @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="景点分类">
          <el-select v-model="query.category" clearable placeholder="全部" style="width: 120px">
            <el-option
              v-for="item in scenicCategoryOptions"
              :key="item.code"
              :label="item.desc"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标签作用域">
          <el-select
            v-model="query.tagScope"
            clearable
            placeholder="全部"
            style="width: 100px"
            @change="onTagScopeChange"
          >
            <el-option
              v-for="item in tagScopeOptions"
              :key="item.code"
              :label="item.desc"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标签分类">
          <el-select
            v-model="query.tagCategory"
            clearable
            placeholder="全部"
            style="width: 120px"
            :disabled="!query.tagScope"
            @change="onTagCategoryChange"
          >
            <el-option v-for="item in tagCategoryOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="query.tagId"
            clearable
            placeholder="全部"
            style="width: 120px"
            :disabled="!query.tagCategory"
          >
            <el-option
              v-for="item in searchTagOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="省份">
          <el-select
            v-model="query.provinceId"
            clearable
            placeholder="请选择省份"
            style="width: 120px"
            @change="onProvinceChange"
          >
            <el-option
              v-for="item in provinceOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-select
            v-model="query.cityId"
            clearable
            placeholder="请选择城市"
            style="width: 120px"
            :disabled="!query.provinceId"
          >
            <el-option
              v-for="item in cityOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 100px">
            <el-option
              v-for="item in commonStatusOptions"
              :key="item.code"
              :label="item.desc"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="query.level" clearable placeholder="全部" style="width: 100px">
            <el-option
              v-for="item in scenicLevelOptions"
              :key="item.code"
              :label="item.desc"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item style="margin-left: auto">
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="margin-left: auto">
          <el-button type="success" @click="openCreateDialog">新增景点</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="scenicList" style="margin-top: 12px">
        <el-table-column prop="name" label="景点名称" min-width="100" />
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              fit="cover"
              style="width: 72px; height: 48px; border-radius: 6px"
            />
            <span v-else class="text-muted">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="120">
          <template #default="{ row }">
            <template v-if="row.tagList?.length">
              <el-tag
                v-for="tag in row.tagList.slice(0, 3)"
                :key="tag"
                size="small"
                class="tag-gap"
              >
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
            <el-popconfirm
              title="确认删除该景点吗？"
              confirm-button-text="确认"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
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
          @current-change="loadScenicList"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailVisible"
      title="景点详情"
      width="760px"
      :close-on-click-modal="true"
      destroy-on-close
    >
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
          <el-col :span="24">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="名称">{{ detailData.name || "-" }}</el-descriptions-item>
              <el-descriptions-item label="地区">
                {{ detailData.regionName || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="地址">
                {{ detailData.address || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="等级">
                {{ detailData.level || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="分类">
                {{ detailData.category || "-" }}
              </el-descriptions-item>
              <el-descriptions-item label="经纬度">
                {{ formatLngLat(detailData.longitude, detailData.latitude) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>

        <el-divider content-position="left">评价数据</el-divider>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="评分">
            {{ formatNumber(detailData.score) }}
          </el-descriptions-item>
          <el-descriptions-item label="评分人数">
            {{ formatInteger(detailData.ratingCount) }}
          </el-descriptions-item>
          <el-descriptions-item label="收藏数">
            {{ formatInteger(detailData.favoriteCount) }}
          </el-descriptions-item>
          <el-descriptions-item label="评论数">
            {{ formatInteger(detailData.reviewCount) }}
          </el-descriptions-item>
          <el-descriptions-item label="浏览数">
            {{ formatInteger(detailData.viewCount) }}
          </el-descriptions-item>
          <el-descriptions-item label="热度分">
            {{ formatNumber(detailData.hotScore) }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">时间票务</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="开放时间">
            {{ detailData.openTime || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="票价">
            {{ formatTicketPrice(detailData.ticketPrice) }}
          </el-descriptions-item>
          <el-descriptions-item label="票务说明" :span="2">
            {{ detailData.ticketInfo || "-" }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">内容介绍</el-divider>
        <div class="info-block">
          <div class="label">简介</div>
          <div class="value">{{ detailData.description || "-" }}</div>
        </div>
        <div class="info-block">
          <div class="label">详细内容</div>
          <div class="value">{{ detailData.detailContent || "-" }}</div>
        </div>

        <el-divider content-position="left">旅游建议</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="最佳季节">
            {{ detailData.bestSeason || "-" }}
          </el-descriptions-item>
          <el-descriptions-item label="建议游玩时长">
            {{ formatHours(detailData.suggestedHours) }}
          </el-descriptions-item>
          <el-descriptions-item label="游玩提示" :span="2">
            {{ detailData.tips || "-" }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">配置信息</el-divider>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="状态">
            {{ detailData.status === 1 ? "上架" : "下架" }}
          </el-descriptions-item>
          <el-descriptions-item label="排序值">
            {{ formatInteger(detailData.sortOrder) }}
          </el-descriptions-item>
          <el-descriptions-item label="是否推荐">
            {{ detailData.isRecommended === 1 ? "是" : "否" }}
          </el-descriptions-item>
          <el-descriptions-item label="是否收藏">
            {{ detailData.isFavorite ? "是" : "否" }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">标签</el-divider>
        <div class="info-item">
          <template v-if="detailData.tagList?.length">
            <el-tag v-for="tag in detailData.tagList" :key="tag" size="small" class="tag-gap">
              {{ tag }}
            </el-tag>
          </template>
          <span v-else class="text-muted">暂无</span>
        </div>

        <el-divider content-position="left">图片列表</el-divider>
        <div v-if="detailData.images?.length" class="image-list">
          <el-image
            v-for="image in detailData.images"
            :key="image.id"
            :src="image.imageUrl"
            fit="cover"
            class="detail-image"
          />
        </div>
        <div v-else class="text-muted">暂无图片</div>
      </el-card>
    </el-dialog>

    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑景点' : '新增景点'"
      width="620px"
      class="scenic-form-dialog"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="dialog-body-scroll">
        <el-form ref="formRef" :model="formModel" :rules="rules" label-position="top">
          <div class="form-section">
            <div class="form-section-title">基本信息</div>
            <div class="form-section-content">
              <el-form-item label="景点名称" prop="name">
                <el-input v-model="formModel.name" placeholder="请输入景点名称" />
              </el-form-item>
              <el-form-item label="地区" prop="regionId">
                <el-cascader
                  v-model="formModel.regionId"
                  :options="provinceOptions"
                  :props="regionCascaderProps"
                  placeholder="请选择省/市/区县"
                  filterable
                  clearable
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item label="地址" prop="address">
                <el-input v-model="formModel.address" placeholder="请输入详细地址" />
              </el-form-item>
              <el-form-item label="封面图URL" prop="coverImage">
                <el-input v-model="formModel.coverImage" placeholder="请输入封面图 URL" />
              </el-form-item>
              <el-form-item label="开放时间">
                <el-input v-model="formModel.openTime" placeholder="例如：08:00-17:30" />
              </el-form-item>
            </div>
          </div>

          <div class="form-section">
            <div class="form-section-title">描述信息</div>
            <div class="form-section-content">
              <el-form-item label="简介" prop="description">
                <el-input
                  v-model="formModel.description"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入简介"
                />
              </el-form-item>
              <el-form-item label="详细内容">
                <el-input
                  v-model="formModel.detailContent"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入详细内容"
                />
              </el-form-item>
              <el-form-item label="票务说明">
                <el-input
                  v-model="formModel.ticketInfo"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入票务说明"
                />
              </el-form-item>
              <el-form-item label="游玩提示">
                <el-input
                  v-model="formModel.tips"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入游玩提示"
                />
              </el-form-item>
            </div>
          </div>

          <div class="form-section">
            <div class="form-section-title">景点属性</div>
            <div class="form-section-content">
              <div class="form-row-grid">
                <el-form-item label="票价">
                  <el-input-number
                    v-model="formModel.ticketPrice"
                    :min="0"
                    :precision="2"
                    :step="1"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="等级">
                  <el-select
                    v-model="formModel.level"
                    clearable
                    placeholder="请选择等级"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="item in scenicLevelOptions"
                      :key="item.code"
                      :label="item.desc"
                      :value="item.code"
                    />
                  </el-select>
                </el-form-item>
              </div>
              <div class="form-row-grid">
                <el-form-item label="分类">
                  <el-select
                    v-model="formModel.category"
                    clearable
                    placeholder="请选择分类"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="item in scenicCategoryOptions"
                      :key="item.code"
                      :label="item.desc"
                      :value="item.code"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="最佳季节">
                  <el-input v-model="formModel.bestSeason" placeholder="例如：春秋" />
                </el-form-item>
              </div>
              <div class="form-row-grid">
                <el-form-item label="建议游玩时长">
                  <el-input
                    v-model="formModel.suggestedHours"
                    placeholder="例如：2-3 小时 / 半天 / 1 天"
                  />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="formModel.status">
                    <el-radio :value="1">上架</el-radio>
                    <el-radio :value="0">下架</el-radio>
                  </el-radio-group>
                </el-form-item>
              </div>
              <div class="form-row-grid">
                <el-form-item label="排序值">
                  <el-input-number
                    v-model="formModel.sortOrder"
                    :precision="0"
                    :step="1"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="是否推荐">
                  <el-radio-group v-model="formModel.isRecommended">
                    <el-radio :value="1">是</el-radio>
                    <el-radio :value="0">否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </div>
            </div>
          </div>

          <div class="form-section">
            <div class="form-section-title">标签与图片</div>
            <div class="form-section-content">
              <el-form-item label="标签" prop="tagIds">
                <el-cascader
                  v-model="tagCascaderValue"
                  :options="tagCascaderOptions"
                  :props="tagCascaderProps"
                  placeholder="请选择标签"
                  style="width: 100%"
                  filterable
                  collapse-tags
                  :show-all-levels="false"
                />
              </el-form-item>
              <el-form-item label="图片ID列表">
                <el-select
                  v-model="imageIdTagValues"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="输入数字后回车，可多选"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in imageIdTagValues"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
            </div>
          </div>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="formVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, type CascaderOption, type FormInstance, type FormRules } from "element-plus";
import {
  createAdminScenic,
  getAdminScenicDetail,
  deleteAdminScenic,
  getAdminScenicPage,
  type ScenicCreatePayload,
  type ScenicDetail,
  type ScenicItem,
  type ScenicQuery,
  type ScenicUpdatePayload,
  updateAdminScenic,
} from "@/api/scenic";
import {
  getRegionTree,
  getTags,
  getTagsByScope,
  getTagCategories,
  type CommonTagItem,
  type CommonRegionNode,
} from "@/api/common";
import {
  getScenicLevelDict,
  getScenicCategoryDict,
  getCommonStatusDict,
  getTagScopeDict,
  type DictItem,
} from "@/api/dict";

interface OptionItem {
  id: number;
  name: string;
}

interface SearchQuery extends ScenicQuery {
  provinceId?: number;
  cityId?: number;
  status?: number;
  level?: string;
  category?: string;
  tagId?: number;
  tagScope?: string;
  tagCategory?: string;
}

interface ScenicFormModel {
  id?: number;
  name: string;
  regionId?: number;
  address: string;
  coverImage: string;
  description: string;
  detailContent: string;
  openTime: string;
  ticketInfo: string;
  ticketPrice?: number;
  level: string;
  category: string;
  bestSeason: string;
  suggestedHours?: string;
  tips: string;
  sortOrder?: number;
  isRecommended: number;
  tagIds: number[];
  imageIds: number[];
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

// 级联选择器配置：支持省/市/区县任意层级选中，只回传当前节点 id
const regionCascaderProps = {
  value: "id",
  label: "name",
  children: "children",
  emitPath: false,
  checkStrictly: true,
};
const cityOptions = ref<CommonRegionNode[]>([]);
const tagOptions = ref<CommonTagItem[]>([]);
const tagScopeOptions = ref<DictItem[]>([]);
const tagCategoryOptions = ref<string[]>([]);
const searchTagOptions = ref<CommonTagItem[]>([]);
const scenicLevelOptions = ref<DictItem[]>([]);
const scenicCategoryOptions = ref<DictItem[]>([]);
const commonStatusOptions = ref<DictItem[]>([]);

const tagCascaderProps = {
  multiple: true,
  checkStrictly: false,
  emitPath: true,
};

const tagCascaderOptions = computed<CascaderOption[]>(() => {
  const scopeMap = new Map<string, { label: string; categories: Map<string, CascaderOption[]> }>();
  for (const tag of tagOptions.value) {
    if (!tag.scope || !tag.category) continue;
    if (!scopeMap.has(tag.scope)) {
      const scopeLabel = tagScopeOptions.value.find((s) => s.code === tag.scope)?.desc ?? tag.scope;
      scopeMap.set(tag.scope, { label: scopeLabel, categories: new Map() });
    }
    const scopeData = scopeMap.get(tag.scope)!;
    if (!scopeData.categories.has(tag.category)) {
      scopeData.categories.set(tag.category, []);
    }
    scopeData.categories.get(tag.category)!.push({
      value: tag.id,
      label: tag.name,
    } as CascaderOption);
  }
  return Array.from(scopeMap.entries()).map(([scopeValue, scopeData]) => ({
    value: scopeValue,
    label: scopeData.label,
    children: Array.from(scopeData.categories.entries()).map(([catValue, tags]) => ({
      value: catValue,
      label: catValue,
      children: tags,
    })),
  })) as CascaderOption[];
});

const tagCascaderValue = computed<(string | number)[][]>({
  get() {
    return formModel.tagIds
      .map((id) => {
        const tag = tagOptions.value.find((t) => t.id === id);
        if (!tag) return null;
        return [tag.scope, tag.category, tag.id];
      })
      .filter((item): item is [string, string, number] => item !== null);
  },
  set(val) {
    if (!val || val.length === 0) {
      formModel.tagIds = [];
      return;
    }
    formModel.tagIds = Array.from(
      new Set(val.map((path) => Number(path[path.length - 1])).filter((id) => Number.isFinite(id)))
    );
  },
});

const query = reactive<SearchQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  provinceId: undefined,
  cityId: undefined,
  status: undefined,
  level: undefined,
  category: undefined,
  tagId: undefined,
  tagScope: undefined,
  tagCategory: undefined,
});

const formModel = reactive<ScenicFormModel>({
  name: "",
  regionId: undefined,
  address: "",
  coverImage: "",
  description: "",
  detailContent: "",
  openTime: "",
  ticketInfo: "",
  ticketPrice: undefined,
  level: "",
  category: "",
  bestSeason: "",
  suggestedHours: undefined,
  tips: "",
  sortOrder: undefined,
  isRecommended: 0,
  tagIds: [],
  imageIds: [],
  status: 1,
});

const imageIdTagValues = computed<string[]>({
  get: () => formModel.imageIds.map((id) => String(id)),
  set: (values) => {
    formModel.imageIds = Array.from(
      new Set(
        values
          .map((item) => Number(item))
          .filter((value) => Number.isFinite(value) && value > 0)
          .map((value) => Math.trunc(value))
      )
    );
  },
});

const rules: FormRules<ScenicFormModel> = {
  name: [{ required: true, message: "请输入景点名称", trigger: "blur" }],
  regionId: [{ required: true, message: "请选择地区", trigger: "change" }],
  address: [{ required: true, message: "请输入地址", trigger: "blur" }],
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

function formatNumber(value?: number): string {
  if (value == null || Number.isNaN(Number(value))) {
    return "-";
  }
  return String(value);
}

function formatInteger(value?: number): string {
  if (value == null || Number.isNaN(Number(value))) {
    return "-";
  }
  return String(Math.trunc(value));
}

function formatHours(value?: string | number): string {
  if (value == null || value === "" || Number.isNaN(Number(value))) {
    return "-";
  }
  return `${value} 小时`;
}

function formatLngLat(longitude?: number, latitude?: number): string {
  if (longitude == null || latitude == null) {
    return "-";
  }
  return `${longitude}, ${latitude}`;
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
  formModel.address = "";
  formModel.coverImage = "";
  formModel.description = "";
  formModel.detailContent = "";
  formModel.openTime = "";
  formModel.ticketInfo = "";
  formModel.ticketPrice = undefined;
  formModel.level = "";
  formModel.category = "";
  formModel.bestSeason = "";
  formModel.suggestedHours = undefined;
  formModel.tips = "";
  formModel.sortOrder = undefined;
  formModel.isRecommended = 0;
  formModel.tagIds = [];
  formModel.imageIds = [];
  formModel.status = 1;
}

async function loadMetaData(): Promise<void> {
  const [regions, tags, levels, categories, statuses, scopes] = await Promise.all([
    getRegionTree(),
    getTags(),
    getScenicLevelDict(),
    getScenicCategoryDict(),
    getCommonStatusDict(),
    getTagScopeDict(),
  ]);
  provinceOptions.value = regions;
  regionOptions.value = flattenRegions(regions);
  tagOptions.value = tags;
  scenicLevelOptions.value = levels;
  scenicCategoryOptions.value = categories;
  commonStatusOptions.value = statuses;
  tagScopeOptions.value = scopes;
}

async function onTagScopeChange(scope?: string): Promise<void> {
  query.tagCategory = undefined;
  query.tagId = undefined;
  searchTagOptions.value = [];
  if (scope) {
    try {
      tagCategoryOptions.value = await getTagCategories(scope);
    } catch {
      tagCategoryOptions.value = [];
    }
  } else {
    tagCategoryOptions.value = [];
  }
}

async function onTagCategoryChange(category?: string): Promise<void> {
  query.tagId = undefined;
  if (category && query.tagScope) {
    try {
      const allTags = await getTagsByScope(query.tagScope);
      searchTagOptions.value = allTags.filter((t) => t.category === category);
    } catch {
      searchTagOptions.value = [];
    }
  } else {
    searchTagOptions.value = [];
  }
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
    status: query.status ?? undefined,
    level: query.level || undefined,
    category: query.category || undefined,
    tagId: query.tagId ?? undefined,
    tagScope: query.tagScope || undefined,
    tagCategory: query.tagCategory || undefined,
  };
}

function onSearch(): void {
  query.pageNum = 1;
  void loadScenicList();
}

function onProvinceChange(provinceId?: number): void {
  const currentProvince = provinceOptions.value.find(
    (item) => Number(item.id) === Number(provinceId)
  );
  cityOptions.value = currentProvince?.children ?? [];
  query.cityId = undefined;
}

function handleReset(): void {
  query.keyword = "";
  query.provinceId = undefined;
  query.cityId = undefined;
  query.status = undefined;
  query.level = undefined;
  query.category = undefined;
  query.tagId = undefined;
  query.tagScope = undefined;
  query.tagCategory = undefined;
  tagCategoryOptions.value = [];
  searchTagOptions.value = [];
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
    detailData.value = await getAdminScenicDetail(row.id);
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
    const detail = await getAdminScenicDetail(row.id);
    formModel.id = row.id;
    formModel.name = detail.name;
    formModel.regionId = detail.regionId;
    formModel.address = detail.address || "";
    formModel.coverImage = detail.coverImage || "";
    formModel.description = detail.description || "";
    formModel.detailContent = detail.detailContent || "";
    formModel.openTime = detail.openTime || "";
    formModel.ticketInfo = detail.ticketInfo || "";
    formModel.ticketPrice = detail.ticketPrice;
    formModel.level = detail.level || "";
    formModel.category = detail.category || "";
    formModel.bestSeason = detail.bestSeason || "";
    formModel.suggestedHours = detail.suggestedHours;
    formModel.tips = detail.tips || "";
    formModel.sortOrder = detail.sortOrder;
    formModel.isRecommended = detail.isRecommended ?? 0;
    formModel.status = detail.status ?? 1;
    formModel.tagIds = detail.tagIds ?? [];
    formModel.imageIds = (detail.images ?? [])
      .map((item) => Number(item.fileResourceId))
      .filter((id) => Number.isFinite(id));
    formVisible.value = true;
  } catch {
    ElMessage.error("加载编辑数据失败");
  }
}

async function submitForm(): Promise<void> {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  const payload: ScenicCreatePayload = {
    name: formModel.name,
    regionId: Number(formModel.regionId),
    address: formModel.address,
    coverImage: formModel.coverImage || undefined,
    description: formModel.description || undefined,
    detailContent: formModel.detailContent || undefined,
    openTime: formModel.openTime || undefined,
    ticketInfo: formModel.ticketInfo || undefined,
    ticketPrice: formModel.ticketPrice,
    level: formModel.level || undefined,
    category: formModel.category || undefined,
    bestSeason: formModel.bestSeason || undefined,
    suggestedHours: formModel.suggestedHours,
    tips: formModel.tips || undefined,
    sortOrder: formModel.sortOrder,
    isRecommended: formModel.isRecommended,
    tagIds: formModel.tagIds,
    imageIds: formModel.imageIds,
    status: formModel.status,
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

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-image {
  width: 120px;
  height: 80px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.page-container {
  padding: 0px;
}

.scenic-form-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-right: 0;
}

.scenic-form-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 500;
}

.scenic-form-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.scenic-form-dialog .dialog-body-scroll {
  max-height: calc(85vh - 120px);
  overflow-y: auto;
  padding: 20px;
}

.scenic-form-dialog .form-section {
  margin-bottom: 24px;
}

.scenic-form-dialog .form-section:last-child {
  margin-bottom: 0;
}

.scenic-form-dialog .form-section-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.scenic-form-dialog :deep(.el-form-item__label) {
  font-size: 13px;
  color: #606266;
  line-height: 20px;
  padding-bottom: 4px;
}

.scenic-form-dialog :deep(.el-form-item) {
  margin-bottom: 16px;
}

.scenic-form-dialog :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.scenic-form-dialog .form-row-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.scenic-form-dialog .form-row-grid :deep(.el-form-item) {
  margin-bottom: 16px;
}

.scenic-form-dialog :deep(.el-input__wrapper),
.scenic-form-dialog :deep(.el-textarea__inner),
.scenic-form-dialog :deep(.el-input-number .el-input__wrapper) {
  border-radius: 4px;
}

.scenic-form-dialog :deep(.el-input__wrapper),
.scenic-form-dialog :deep(.el-textarea__inner) {
  border-color: #dcdfe6;
}

.scenic-form-dialog :deep(.el-input__wrapper.is-focus),
.scenic-form-dialog :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.scenic-form-dialog .dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
}

.scenic-form-dialog :deep(.el-dialog__footer) {
  padding: 0;
  border-top: none;
}
</style>
