<template>
  <div class="admin-page">
    <div class="page-head">
      <div>
        <p class="eyebrow">SCENIC INVENTORY</p>
        <h1>景点管理</h1>
      </div>
      <div class="head-actions">
        <el-button :disabled="selectedIds.length === 0" @click="batchSetStatus(1)">
          批量上架
        </el-button>
        <el-button :disabled="selectedIds.length === 0" @click="batchSetStatus(0)">
          批量下架
        </el-button>
        <el-button type="primary" @click="openCreate">新增景点</el-button>
      </div>
    </div>

    <el-form :model="query" class="filter-panel" inline>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="景点名称 / 地址"
          @keyup.enter="search"
        />
      </el-form-item>
      <el-form-item label="地区">
        <el-button class="region-picker-btn" @click="openRegionPicker('filter')">
          {{ filterRegionText || "选择地区" }}
        </el-button>
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="query.category" clearable placeholder="全部分类">
          <el-option
            v-for="item in scenicCategoryOptions"
            :key="item.code"
            :label="item.desc"
            :value="String(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="等级">
        <el-select v-model="query.level" clearable placeholder="全部等级">
          <el-option
            v-for="item in scenicLevelOptions"
            :key="item.code"
            :label="item.desc"
            :value="String(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="标签">
        <el-button class="tag-picker-btn" @click="openTagPicker('filter')">
          {{ queryTagIds.length > 0 ? `已选 ${queryTagIds.length} 个标签` : "选择标签" }}
        </el-button>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部状态">
          <el-option
            v-for="item in commonStatusOptions"
            :key="item.code"
            :label="item.desc"
            :value="Number(item.code)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="排序">
        <el-select v-model="query.sortBy" clearable placeholder="排序字段">
          <el-option label="热度" value="hot" />
          <el-option label="评分" value="score" />
          <el-option label="创建时间" value="createdAt" />
        </el-select>
        <el-select v-model="query.sortOrder" clearable placeholder="顺序" class="sort-order">
          <el-option label="升序" value="ASC" />
          <el-option label="降序" value="DESC" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-shell">
      <el-table
        v-loading="loading"
        :data="list"
        border
        stripe
        row-key="id"
        class="scenic-table"
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="48" />
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column label="景点" min-width="220">
          <template #default="{ row }">
            <div class="spot-cell">
              <el-image v-if="row.coverImage" class="cover" :src="row.coverImage" fit="cover" />
              <div>
                <div class="spot-name">{{ row.name }}</div>
                <div class="spot-sub">{{ row.address || "-" }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="regionName" label="地区" min-width="120" />
        <el-table-column prop="category" label="分类" min-width="120" />
        <el-table-column prop="level" label="等级" width="90" />
        <el-table-column prop="score" label="评分" width="90">
          <template #default="{ row }">{{ Number(row.score || 0).toFixed(1) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ dictText(commonStatusOptions, row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="tag in row.tagList || []" :key="tag" class="tag-item" size="small">
              {{ tag }}
            </el-tag>
            <span v-if="!row.tagList?.length">-</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="250">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row.id)">编辑</el-button>
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="setStatus(row)"
            >
              {{ row.status === 1 ? "下架" : "上架" }}
            </el-button>
            <el-button link type="danger" @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

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

    <el-drawer
      v-model="drawerVisible"
      :title="editingId ? '编辑景点' : '新增景点'"
      size="min(980px, calc(100vw - 280px))"
      class="scenic-drawer"
    >
      <el-form :model="form" label-width="104px">
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="景点名称" required>
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="地区" required>
              <div class="selected-region-field" @click="openRegionPicker('form')">
                <span :class="{ 'selected-placeholder': !formRegionText }">
                  {{ formRegionText || "选择地区" }}
                </span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="地址" required>
              <el-input v-model="form.address" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="经度">
              <el-input-number
                v-model="form.longitude"
                :precision="6"
                :step="0.000001"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="纬度">
              <el-input-number
                v-model="form.latitude"
                :precision="6"
                :step="0.000001"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="封面图">
              <el-input v-model="form.coverImage" placeholder="图片 URL" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="分类">
              <el-select v-model="form.category" clearable>
                <el-option
                  v-for="item in scenicCategoryOptions"
                  :key="item.code"
                  :label="item.desc"
                  :value="String(item.code)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="等级">
              <el-select v-model="form.level" clearable>
                <el-option
                  v-for="item in scenicLevelOptions"
                  :key="item.code"
                  :label="item.desc"
                  :value="String(item.code)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="开放时间">
              <el-input v-model="form.openTime" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="门票价格">
              <el-input-number
                v-model="form.ticketPrice"
                :min="0"
                :precision="2"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="门票说明">
              <el-input v-model="form.ticketInfo" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="最佳季节">
              <el-input v-model="form.bestSeason" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="建议时长">
              <el-input v-model="form.suggestedHours" placeholder="如：4小时" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
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
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="是否推荐">
              <el-radio-group v-model="form.isRecommended">
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
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sortOrder" :min="0" :max="99999" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="标签">
              <div class="selected-tags-field" @click="openTagPicker('form')">
                <template v-if="form.tagIds?.length">
                  <el-tag
                    v-for="tag in selectedFormTags"
                    :key="tag.id"
                    closable
                    @close.stop="removeFormTag(tag.id)"
                  >
                    {{ tag.name }} / {{ tag.category || "其他" }}
                  </el-tag>
                </template>
                <span v-else class="selected-placeholder">选择标签</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="景点图片">
              <div class="image-manager">
                <div v-if="scenicImages.length > 0" class="image-list">
                  <div
                    v-for="image in scenicImages"
                    :key="image.fileResourceId || image.id"
                    class="image-card"
                  >
                    <el-image class="image-thumb" :src="image.imageUrl || ''" fit="cover" />
                    <div class="image-meta">
                      <span>ID {{ image.fileResourceId || image.id }}</span>
                      <el-button link type="danger" @click="removeScenicImage(image)">
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无图片" :image-size="72" />
                <input
                  ref="imageInputRef"
                  class="hidden-input"
                  type="file"
                  accept="image/*"
                  @change="onImageSelected"
                />
                <el-button :loading="uploadingImage" @click="triggerImageUpload">
                  上传新图片
                </el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介">
              <el-input v-model="form.description" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详情内容">
              <el-input v-model="form.detailContent" type="textarea" :rows="6" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="游玩贴士">
              <el-input v-model="form.tips" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="drawerVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="tagPickerVisible" title="选择标签" width="760px" class="tag-picker-dialog">
      <div class="tag-picker-body">
        <div v-for="group in groupedTags" :key="group.category" class="modal-tag-group">
          <span class="modal-tag-group-title">{{ group.category }}</span>
          <div class="modal-tag-list">
            <button
              v-for="tag in group.tags"
              :key="tag.id"
              class="modal-tag-chip"
              :class="{ active: tempTagIds.includes(tag.id) }"
              @click="toggleTempTag(tag.id)"
            >
              <img
                v-if="tag.icon"
                :src="tag.icon"
                class="modal-tag-icon"
                @error="onTagImgError($event)"
              />
              <span class="modal-tag-text">{{ tag.name }}</span>
            </button>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="modal-selected">已选 {{ tempTagIds.length }} 个标签</span>
        <el-button @click="tagPickerVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmTagPicker">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="regionPickerVisible"
      title="选择地区"
      width="820px"
      class="region-picker-dialog"
    >
      <div class="region-picker-body">
        <div class="region-column">
          <button
            v-for="province in regionTree"
            :key="province.id"
            class="region-option"
            :class="{
              active: tempProvinceId === province.id,
              'has-children': province.children?.length,
            }"
            @click="selectRegionLevel(1, province.id)"
          >
            <span>{{ province.name }}</span>
            <span v-if="province.children?.length" class="region-arrow">›</span>
          </button>
        </div>
        <div class="region-column">
          <template v-if="tempCities.length">
            <button
              v-for="city in tempCities"
              :key="city.id"
              class="region-option"
              :class="{ active: tempCityId === city.id, 'has-children': city.children?.length }"
              @click="selectRegionLevel(2, city.id)"
            >
              <span>{{ city.name }}</span>
              <span v-if="city.children?.length" class="region-arrow">›</span>
            </button>
          </template>
          <span v-else class="region-empty">先选择一级地区</span>
        </div>
        <div class="region-column">
          <template v-if="tempDistricts.length">
            <button
              v-for="district in tempDistricts"
              :key="district.id"
              class="region-option"
              :class="{ active: tempDistrictId === district.id }"
              @click="selectRegionLevel(3, district.id)"
            >
              <span>{{ district.name }}</span>
            </button>
          </template>
          <span v-else class="region-empty">可直接确认当前地区</span>
        </div>
      </div>
      <template #footer>
        <span class="modal-selected">已选 {{ formatRegionPath(tempRegionPath) || "未选择" }}</span>
        <el-button @click="clearRegionPicker">清空</el-button>
        <el-button @click="regionPickerVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRegionPicker">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  batchUpdateAdminScenicStatus,
  createAdminScenic,
  deleteAdminScenic,
  getAdminScenicImages,
  getAdminScenicDetail,
  getAdminScenicPage,
  updateAdminScenic,
  updateAdminScenicStatus,
  type ScenicImageItem,
  type ScenicCreatePayload,
  type ScenicDetail,
  type ScenicItem,
  type ScenicQuery,
} from "@/api/scenic";
import { getRegionTree, getTags, type CommonRegionNode, type CommonTagItem } from "@/api/common";
import {
  getCommonStatusDict,
  getScenicCategoryDict,
  getScenicLevelDict,
  getYesNoFlagDict,
  type DictItem,
} from "@/api/dict";
import { getFileResource, getUploadToken, uploadCallback } from "@/api/file";
import { findDictDesc } from "@/composables/useDictOptions";

interface TagGroup {
  category: string;
  tags: CommonTagItem[];
}

type TagPickerTarget = "filter" | "form";
type RegionPickerTarget = "filter" | "form";

const loading = ref(false);
const saving = ref(false);
const drawerVisible = ref(false);
const tagPickerVisible = ref(false);
const regionPickerVisible = ref(false);
const uploadingImage = ref(false);
const list = ref<ScenicItem[]>([]);
const total = ref(0);
const selectedIds = ref<number[]>([]);
const editingId = ref<number | null>(null);
const regionTree = ref<CommonRegionNode[]>([]);
const regionPath = ref<number[]>([]);
const formRegionPath = ref<number[]>([]);
const allTags = ref<CommonTagItem[]>([]);
const scenicCategoryOptions = ref<DictItem[]>([]);
const scenicLevelOptions = ref<DictItem[]>([]);
const commonStatusOptions = ref<DictItem[]>([]);
const yesNoOptions = ref<DictItem[]>([]);
const queryTagIds = ref<number[]>([]);
const tempTagIds = ref<number[]>([]);
const tempRegionPath = ref<number[]>([]);
const tagPickerTarget = ref<TagPickerTarget>("filter");
const regionPickerTarget = ref<RegionPickerTarget>("filter");
const scenicImages = ref<ScenicImageItem[]>([]);
const imageInputRef = ref<HTMLInputElement | null>(null);

const query = reactive<ScenicQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: "",
  regionId: undefined,
  category: undefined,
  level: undefined,
  tagId: undefined,
  tagIds: undefined,
  status: undefined,
  sortBy: undefined,
  sortOrder: undefined,
});

const form = reactive<ScenicCreatePayload>({
  name: "",
  regionId: undefined as unknown as number,
  address: "",
  longitude: undefined,
  latitude: undefined,
  coverImage: "",
  description: "",
  detailContent: "",
  openTime: "",
  ticketInfo: "",
  ticketPrice: undefined,
  level: "",
  category: "",
  bestSeason: "",
  suggestedHours: "",
  tips: "",
  status: 1,
  sortOrder: 0,
  isRecommended: 0,
  tagIds: [],
  imageIds: [],
});

const groupedTags = computed<TagGroup[]>(() => {
  const map = new Map<string, CommonTagItem[]>();
  for (const tag of allTags.value) {
    if (tag.status !== undefined && tag.status !== 1) continue;
    const category = tag.category || "其他";
    if (!map.has(category)) map.set(category, []);
    map.get(category)!.push(tag);
  }
  return Array.from(map.entries())
    .sort((a, b) => (a[0] === "其他" ? 1 : b[0] === "其他" ? -1 : a[0].localeCompare(b[0])))
    .map(([category, tags]) => ({
      category,
      tags: tags.sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0)),
    }));
});

const selectedFormTags = computed(() => {
  const ids = form.tagIds || [];
  return allTags.value.filter((tag) => ids.includes(tag.id));
});

const filterRegionText = computed(() => formatRegionPath(regionPath.value));
const formRegionText = computed(() => formatRegionPath(formRegionPath.value));
const tempProvinceId = computed(() => tempRegionPath.value[0]);
const tempCityId = computed(() => tempRegionPath.value[1]);
const tempDistrictId = computed(() => tempRegionPath.value[2]);
const tempProvince = computed(() =>
  regionTree.value.find((region) => region.id === tempProvinceId.value)
);
const tempCities = computed(() => tempProvince.value?.children || []);
const tempCity = computed(() => tempCities.value.find((region) => region.id === tempCityId.value));
const tempDistricts = computed(() => tempCity.value?.children || []);

function dictText(options: DictItem[], code: unknown): string {
  return findDictDesc(options, code, "-");
}

async function loadDictionaries(): Promise<void> {
  [
    scenicCategoryOptions.value,
    scenicLevelOptions.value,
    commonStatusOptions.value,
    yesNoOptions.value,
  ] = await Promise.all([
    getScenicCategoryDict(),
    getScenicLevelDict(),
    getCommonStatusDict(),
    getYesNoFlagDict(),
  ]);
}

async function loadBasics(): Promise<void> {
  [regionTree.value, allTags.value] = await Promise.all([getRegionTree(), getTags()]);
}

async function loadData(): Promise<void> {
  loading.value = true;
  try {
    const res = await getAdminScenicPage({
      ...query,
      keyword: query.keyword?.trim() || undefined,
      tagIds: queryTagIds.value.length > 0 ? queryTagIds.value : undefined,
      tagId: undefined,
    });
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
  query.keyword = "";
  query.regionId = undefined;
  query.category = undefined;
  query.level = undefined;
  query.tagId = undefined;
  query.tagIds = undefined;
  query.status = undefined;
  query.sortBy = undefined;
  query.sortOrder = undefined;
  queryTagIds.value = [];
  regionPath.value = [];
  void loadData();
}

function onSelectionChange(rows: ScenicItem[]): void {
  selectedIds.value = rows.map((row) => row.id);
}

function formatRegionPath(path: number[]): string {
  if (!path.length) return "";
  const names: string[] = [];
  let nodes = regionTree.value;
  for (const id of path) {
    const region = nodes.find((item) => item.id === id);
    if (!region) break;
    names.push(region.name);
    nodes = region.children || [];
  }
  return names.join(" / ");
}

function openRegionPicker(target: RegionPickerTarget): void {
  regionPickerTarget.value = target;
  tempRegionPath.value = target === "filter" ? [...regionPath.value] : [...formRegionPath.value];
  regionPickerVisible.value = true;
}

function selectRegionLevel(level: 1 | 2 | 3, id: number): void {
  if (level === 1) {
    tempRegionPath.value = [id];
    return;
  }
  if (level === 2) {
    tempRegionPath.value = [tempRegionPath.value[0], id].filter(Boolean);
    return;
  }
  tempRegionPath.value = [tempRegionPath.value[0], tempRegionPath.value[1], id].filter(Boolean);
}

function clearRegionPicker(): void {
  tempRegionPath.value = [];
}

function confirmRegionPicker(): void {
  const regionId = tempRegionPath.value.at(-1);
  if (regionPickerTarget.value === "filter") {
    regionPath.value = [...tempRegionPath.value];
    query.regionId = regionId;
    regionPickerVisible.value = false;
    search();
    return;
  }
  formRegionPath.value = [...tempRegionPath.value];
  form.regionId = (regionId || undefined) as unknown as number;
  regionPickerVisible.value = false;
}

function openTagPicker(target: TagPickerTarget): void {
  tagPickerTarget.value = target;
  tempTagIds.value = target === "filter" ? [...queryTagIds.value] : [...(form.tagIds || [])];
  tagPickerVisible.value = true;
}

function toggleTempTag(id: number): void {
  const index = tempTagIds.value.indexOf(id);
  if (index >= 0) {
    tempTagIds.value.splice(index, 1);
  } else {
    tempTagIds.value.push(id);
  }
}

function confirmTagPicker(): void {
  if (tagPickerTarget.value === "filter") {
    queryTagIds.value = [...tempTagIds.value];
    query.tagIds = queryTagIds.value.length > 0 ? queryTagIds.value : undefined;
    tagPickerVisible.value = false;
    search();
    return;
  }
  form.tagIds = [...tempTagIds.value];
  tagPickerVisible.value = false;
}

function removeFormTag(id: number): void {
  form.tagIds = (form.tagIds || []).filter((tagId) => tagId !== id);
}

function onTagImgError(event: Event): void {
  (event.target as HTMLImageElement).style.display = "none";
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
    name: "",
    regionId: undefined,
    address: "",
    longitude: undefined,
    latitude: undefined,
    coverImage: "",
    description: "",
    detailContent: "",
    openTime: "",
    ticketInfo: "",
    ticketPrice: undefined,
    level: "",
    category: "",
    bestSeason: "",
    suggestedHours: "",
    tips: "",
    status: 1,
    sortOrder: 0,
    isRecommended: 0,
    tagIds: [],
    imageIds: [],
  });
  scenicImages.value = [];
  formRegionPath.value = [];
}

function openCreate(): void {
  editingId.value = null;
  resetForm();
  drawerVisible.value = true;
}

async function openEdit(id: number): Promise<void> {
  editingId.value = id;
  resetForm();
  const detail = await getAdminScenicDetail(id);
  fillForm(detail);
  await loadScenicImages(id);
  drawerVisible.value = true;
}

function fillForm(detail: ScenicDetail): void {
  Object.assign(form, {
    name: detail.name || "",
    regionId: detail.regionId as number,
    address: detail.address || "",
    longitude: detail.longitude,
    latitude: detail.latitude,
    coverImage: detail.coverImage || "",
    description: detail.description || "",
    detailContent: detail.detailContent || "",
    openTime: detail.openTime || "",
    ticketInfo: detail.ticketInfo || "",
    ticketPrice: detail.ticketPrice,
    level: detail.level || "",
    category: detail.category || "",
    bestSeason: detail.bestSeason || "",
    suggestedHours: detail.suggestedHours || "",
    tips: detail.tips || "",
    status: detail.status ?? 1,
    sortOrder: detail.sortOrder ?? 0,
    isRecommended: detail.isRecommended ?? 0,
    tagIds: detail.tagIds || [],
    imageIds:
      detail.images?.map((image) => image.fileResourceId).filter((id): id is number => !!id) || [],
  });
  scenicImages.value =
    detail.images
      ?.filter((image) => image.fileResourceId)
      .map((image) => ({
        id: image.id,
        fileResourceId: image.fileResourceId,
        imageUrl: image.imageUrl,
      })) || [];
  formRegionPath.value = findPathById(regionTree.value, detail.regionId);
}

async function loadScenicImages(id: number): Promise<void> {
  scenicImages.value = await getAdminScenicImages(id);
  form.imageIds = scenicImages.value
    .map((image) => image.fileResourceId)
    .filter((fileId): fileId is number => !!fileId);
}

function buildPayload(): ScenicCreatePayload {
  return {
    name: form.name.trim(),
    regionId: form.regionId,
    address: form.address.trim(),
    longitude: form.longitude,
    latitude: form.latitude,
    coverImage: form.coverImage || undefined,
    description: form.description || undefined,
    detailContent: form.detailContent || undefined,
    openTime: form.openTime || undefined,
    ticketInfo: form.ticketInfo || undefined,
    ticketPrice: form.ticketPrice,
    level: form.level || undefined,
    category: form.category || undefined,
    bestSeason: form.bestSeason || undefined,
    suggestedHours: form.suggestedHours || undefined,
    tips: form.tips || undefined,
    status: form.status,
    sortOrder: form.sortOrder,
    isRecommended: form.isRecommended,
    tagIds: form.tagIds || [],
    imageIds: scenicImages.value
      .map((image) => image.fileResourceId)
      .filter((fileId): fileId is number => !!fileId),
  };
}

function triggerImageUpload(): void {
  if (uploadingImage.value) return;
  imageInputRef.value?.click();
}

async function onImageSelected(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  if (!file.type.startsWith("image/")) {
    ElMessage.warning("请选择图片文件");
    input.value = "";
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning("图片大小不能超过 5MB");
    input.value = "";
    return;
  }
  uploadingImage.value = true;
  try {
    const token = await getUploadToken({
      bizType: "scenic",
      fileName: file.name,
      fileSize: file.size,
      bizId: editingId.value || undefined,
    });
    const uploadUrl = String(token.uploadUrl || "");
    const bucketName = String(token.bucketName || "");
    const objectKey = String(token.objectKey || "");
    if (!uploadUrl || !bucketName || !objectKey) {
      throw new Error("上传凭证不完整");
    }
    const uploadResponse = await fetch(uploadUrl, {
      method: "PUT",
      body: file,
      headers: { "Content-Type": file.type || "application/octet-stream" },
    });
    if (!uploadResponse.ok) {
      throw new Error("文件上传失败");
    }
    const fileId = await uploadCallback({
      bucketName,
      objectKey,
      originalName: file.name,
      bizType: "scenic",
      bizId: editingId.value || undefined,
    });
    const fileResource = await getFileResource(fileId);
    scenicImages.value.push({
      id: fileId,
      fileResourceId: fileId,
      imageUrl: fileResource.url || "",
      sortOrder: scenicImages.value.length,
      isCover: scenicImages.value.length === 0 ? 1 : 0,
    });
    form.imageIds = scenicImages.value
      .map((image) => image.fileResourceId)
      .filter((id): id is number => !!id);
    ElMessage.success("图片上传成功");
  } catch {
    ElMessage.error("图片上传失败，请重试");
  } finally {
    uploadingImage.value = false;
    input.value = "";
  }
}

function removeScenicImage(image: ScenicImageItem): void {
  const fileResourceId = image.fileResourceId;
  scenicImages.value = scenicImages.value.filter((item) => item !== image);
  form.imageIds = scenicImages.value
    .map((item) => item.fileResourceId)
    .filter((id): id is number => !!id);
  if (fileResourceId && form.coverImage === String(fileResourceId)) {
    form.coverImage = "";
  }
}

async function save(): Promise<void> {
  if (!form.name.trim()) {
    ElMessage.warning("请输入景点名称");
    return;
  }
  if (!form.regionId) {
    ElMessage.warning("请选择地区");
    return;
  }
  if (!form.address.trim()) {
    ElMessage.warning("请输入地址");
    return;
  }
  saving.value = true;
  try {
    const payload = buildPayload();
    if (editingId.value) {
      await updateAdminScenic(editingId.value, payload);
    } else {
      await createAdminScenic(payload);
    }
    ElMessage.success("保存成功");
    drawerVisible.value = false;
    await loadData();
  } finally {
    saving.value = false;
  }
}

async function setStatus(row: ScenicItem): Promise<void> {
  const nextStatus = row.status === 1 ? 0 : 1;
  await ElMessageBox.confirm(
    `确认${nextStatus === 1 ? "上架" : "下架"}景点「${row.name}」？`,
    "状态变更",
    {
      type: "warning",
    }
  );
  await updateAdminScenicStatus(row.id, nextStatus);
  ElMessage.success("状态已更新");
  await loadData();
}

async function batchSetStatus(status: number): Promise<void> {
  await ElMessageBox.confirm(
    `确认批量${status === 1 ? "上架" : "下架"} ${selectedIds.value.length} 个景点？`,
    "批量状态变更",
    {
      type: "warning",
    }
  );
  await batchUpdateAdminScenicStatus(selectedIds.value, status);
  ElMessage.success("批量更新成功");
  await loadData();
}

async function deleteItem(row: ScenicItem): Promise<void> {
  await ElMessageBox.confirm(`确认删除景点「${row.name}」？`, "删除确认", { type: "warning" });
  await deleteAdminScenic(row.id);
  ElMessage.success("删除成功");
  await loadData();
}

onMounted(async () => {
  await Promise.all([loadDictionaries(), loadBasics()]);
  await loadData();
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

.page-head {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  justify-content: space-between;
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

.head-actions {
  display: flex;
  gap: 8px;
}

.filter-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 0 14px;
  padding: 16px 16px 0;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 8px;
}

.filter-panel :deep(.el-form-item) {
  margin-right: 0;
}

.filter-panel :deep(.el-input),
.filter-panel :deep(.el-select) {
  width: 180px;
}

.filter-panel :deep(.el-cascader) {
  width: 260px;
}

.tag-picker-btn,
.region-picker-btn {
  width: 220px;
  justify-content: flex-start;
  color: #667085;
}

.region-picker-btn {
  width: 260px;
}

.sort-order {
  width: 96px;
  margin-left: 8px;
}

.table-shell {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 8px;
}

.scenic-table {
  min-width: 1300px;
}

:deep(.scenic-drawer) {
  max-width: calc(100vw - 280px);
  min-width: 640px;
}

:deep(.scenic-drawer .el-drawer__body) {
  overflow-x: hidden;
}

:deep(.scenic-drawer .el-form-item__content),
:deep(.scenic-drawer .el-input),
:deep(.scenic-drawer .el-select),
:deep(.scenic-drawer .el-cascader),
:deep(.scenic-drawer .el-input-number),
:deep(.scenic-drawer .el-textarea) {
  width: 100%;
}

.spot-cell {
  display: flex;
  gap: 10px;
  align-items: center;
}

.cover {
  width: 56px;
  height: 40px;
  border-radius: 6px;
}

.spot-name {
  font-weight: 700;
  color: #101828;
}

.spot-sub {
  max-width: 260px;
  overflow: hidden;
  font-size: 12px;
  color: #667085;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-item {
  margin: 2px 4px 2px 0;
}

.selected-tags-field,
.selected-region-field {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  width: 100%;
  min-height: 40px;
  padding: 5px 10px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.selected-region-field {
  flex-wrap: nowrap;
  overflow: hidden;
}

.selected-region-field span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-placeholder {
  color: #a8abb2;
}

.image-manager {
  width: 100%;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.image-card {
  overflow: hidden;
  border: 1px solid #e7eaf0;
  border-radius: 8px;
}

.image-thumb {
  display: block;
  width: 100%;
  height: 104px;
  background: #f6f8fb;
}

.image-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  font-size: 12px;
  color: #667085;
}

.hidden-input {
  display: none;
}

.tag-picker-body {
  max-height: 560px;
  padding: 8px 0;
  overflow-y: auto;
}

.modal-tag-group {
  padding: 10px 0 18px;
  border-bottom: 1px solid #f2f4f7;
}

.modal-tag-group:last-child {
  border-bottom: 0;
}

.modal-tag-group-title {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #8a8f98;
}

.modal-tag-group-title::before {
  display: inline-block;
  width: 4px;
  height: 18px;
  content: "";
  background: #08d878;
  border-radius: 99px;
}

.modal-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.modal-tag-chip {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  min-height: 38px;
  padding: 0 16px;
  font-size: 14px;
  font-weight: 700;
  color: #666;
  cursor: pointer;
  background: #f7f7f7;
  border: 1px solid transparent;
  border-radius: 999px;
}

.modal-tag-chip:hover {
  color: #00a65a;
  background: #f0fff6;
}

.modal-tag-chip.active {
  color: #004b24;
  background: #dffbea;
  border-color: #08d878;
}

.modal-tag-icon {
  width: 20px;
  height: 20px;
  object-fit: cover;
  border-radius: 4px;
}

.modal-selected {
  float: left;
  margin-top: 8px;
  color: #8a8f98;
}

.region-picker-body {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  height: 420px;
  overflow: hidden;
  border: 1px solid #eef1f5;
  border-radius: 8px;
}

.region-column {
  padding: 10px;
  overflow-y: auto;
  border-right: 1px solid #eef1f5;
}

.region-column:last-child {
  border-right: 0;
}

.region-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 40px;
  padding: 0 12px;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #475467;
  text-align: left;
  cursor: pointer;
  background: #fff;
  border: 1px solid transparent;
  border-radius: 8px;
}

.region-option:hover {
  color: #1677ff;
  background: #f5f9ff;
}

.region-option.active {
  color: #1677ff;
  background: #eef6ff;
  border-color: #91caff;
}

.region-arrow {
  color: #98a2b3;
}

.region-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #98a2b3;
}

.pager {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 980px) {
  :deep(.scenic-drawer) {
    width: calc(100vw - 32px) !important;
    max-width: calc(100vw - 32px);
    min-width: 0;
  }

  .filter-panel :deep(.el-input),
  .filter-panel :deep(.el-select),
  .filter-panel :deep(.el-cascader),
  .tag-picker-btn,
  .region-picker-btn {
    width: 100%;
  }

  .region-picker-body {
    grid-template-columns: 1fr;
    height: 520px;
  }

  .region-column {
    min-height: 160px;
    border-right: 0;
    border-bottom: 1px solid #eef1f5;
  }

  .filter-panel :deep(.el-form-item) {
    width: 100%;
  }
}
</style>
