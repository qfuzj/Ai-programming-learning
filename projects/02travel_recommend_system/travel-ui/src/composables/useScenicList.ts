import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { getRegionTree } from "@/api/common";
import { getTags, type CommonTagItem } from "@/api/common";
import {
  getScenicFilterOptions,
  getScenicPage,
  type ScenicItem,
  type ScenicQuery,
} from "@/api/scenic";
import { addFavorite, removeFavorite } from "@/api/favorite";
import type { ScenicRegionNode } from "@/types/scenic-list";

export function useScenicList() {
  const route = useRoute();

  const loading = ref(false);
  const scenicList = ref<ScenicItem[]>([]);
  const total = ref(0);
  const regionTreeData = ref<ScenicRegionNode[]>([]);
  const categoryOptions = ref<string[]>([]);
  const levelOptions = ref<string[]>([]);
  const tagOptions = ref<CommonTagItem[]>([]);
  const selectedTagPaths = ref<Array<Array<string | number>>>([]);
  const selectedProvinceId = ref<number | undefined>(undefined);
  const selectedCityId = ref<number | undefined>(undefined);
  const minScoreOptions = [1, 2, 3, 4, 4.5];

  const query = reactive<ScenicQuery>({
    pageNum: 1,
    pageSize: 12,
    keyword: "",
    regionId: undefined,
    category: undefined,
    level: undefined,
    tagId: undefined,
    tagIds: undefined,
sortBy: "hot",
    sortOrder: undefined,
  });

  const banners = [
    "https://dynamic-media-cdn.tripadvisor.com/media/photo-o/2f/c9/1b/2f/caption.jpg?w=1800&h=-1&s=1",
    "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80",
    "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80",
  ];

  const currentCities = computed(() => {
    if (!selectedProvinceId.value) return [];
    const province = regionTreeData.value.find((p) => p.id === selectedProvinceId.value);
    return province?.children || [];
  });

  const isSearchActive = computed(() => {
    return (
      !!query.keyword ||
      !!query.regionId ||
      !!query.category ||
      !!query.level ||
      !!query.tagId ||
      !!query.tagIds?.length ||
      
      query.sortBy !== "hot"
    );
  });

  const tagCascaderOptions = computed(() => {
    const scopeLabels: Record<string, string> = {
      SCENIC: "景点标签",
      PREFERENCE: "偏好标签",
      BOTH: "通用标签",
    };
    const scopeMap = new Map<string, { label: string; categories: Map<string, CommonTagItem[]> }>();
    tagOptions.value.forEach((tag) => {
      const scope = tag.scope || "SCENIC";
      const category = tag.category || "未分类";
      if (!scopeMap.has(scope)) {
        scopeMap.set(scope, { label: scopeLabels[scope] || scope, categories: new Map() });
      }
      const scopeItem = scopeMap.get(scope)!;
      const list = scopeItem.categories.get(category) || [];
      list.push(tag);
      scopeItem.categories.set(category, list);
    });

    return Array.from(scopeMap.entries()).map(([scope, scopeItem]) => ({
      value: scope,
      label: scopeItem.label,
      children: Array.from(scopeItem.categories.entries()).map(([category, tags]) => ({
        value: category,
        label: category,
        children: tags.map((tag) => ({
          value: tag.id,
          label: tag.name,
        })),
      })),
    }));
  });

  function onProvinceChange(): void {
    selectedCityId.value = undefined;
    query.regionId = selectedProvinceId.value;
  }

  function onCityChange(): void {
    query.regionId = selectedCityId.value || selectedProvinceId.value;
  }

  function getCurrentRegionName(): string {
    if (!query.regionId) return "";
    let foundName = "";
    const walk = (nodes: ScenicRegionNode[]): boolean => {
      for (const node of nodes) {
        if (node.id === query.regionId) {
          foundName = node.name;
          return true;
        }
        if (node.children?.length && walk(node.children)) {
          return true;
        }
      }
      return false;
    };
    walk(regionTreeData.value);
    return foundName;
  }

  /**
   * 加载景点列表前先加载地区树和筛选选项，以便用户选择过滤条件。
   */
  async function loadRegions(): Promise<void> {
    const regions = await getRegionTree();
    const walk = (nodes: ScenicRegionNode[]) => {
      nodes.forEach((node) => {
        if (node.children && node.children.length === 0) {
          node.children = undefined;
        } else if (node.children && node.children.length > 0) {
          walk(node.children);
        }
      });
    };
    walk(regions);
    regionTreeData.value = regions;
  }

  /**
   * 加载景点列表前先加载筛选选项，以便用户选择过滤条件。
   */
  async function loadFilterOptions(): Promise<void> {
    try {
      const [options, tags] = await Promise.all([getScenicFilterOptions(), getTags()]);
      categoryOptions.value = options.categories ?? [];
      levelOptions.value = options.levels ?? [];
      tagOptions.value = tags.filter((tag) => tag.status == null || tag.status === 1);
    } catch {
      categoryOptions.value = [];
      levelOptions.value = [];
      tagOptions.value = [];
    }
  }

  function syncTagIdsFromPaths(paths = selectedTagPaths.value): void {
    selectedTagPaths.value = paths;
    const tagIds = paths
      .map((path) => Number(path[path.length - 1]))
      .filter((id) => Number.isFinite(id));
    query.tagIds = tagIds.length > 0 ? tagIds : undefined;
    query.tagId = undefined;
    query.pageNum = 1;
  }

  /**
   * 根据当前的 query 参数加载景点列表。该函数会在用户修改过滤条件或排序选项时被调用。
   */
  async function loadScenicList(): Promise<void> {
    loading.value = true;
    try {
      const result = await getScenicPage({
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        keyword: query.keyword || undefined,
        regionId: query.regionId,
        category: query.category || undefined,
        level: query.level || undefined,
        tagId: query.tagId,
        tagIds: query.tagIds,
sortBy: query.sortBy,
        sortOrder: query.sortOrder,
      });
      scenicList.value = result.records;
      total.value = result.total;
    } finally {
      loading.value = false;
    }
  }

  async function toggleFavorite(item: ScenicItem): Promise<void> {
    try {
      if (item.isFavorite) {
        await removeFavorite(item.id);
        item.isFavorite = false;
        ElMessage.success(`已取消收藏 ${item.name}`);
        return;
      }

      await addFavorite(item.id);
      item.isFavorite = true;
      ElMessage.success(`已收藏 ${item.name}`);
    } catch {
      // axios 拦截器已弹错误提示
    }
  }

  function resetFilters(): void {
    query.keyword = "";
    query.regionId = undefined;
    selectedProvinceId.value = undefined;
    selectedCityId.value = undefined;
    query.category = undefined;
    query.level = undefined;
    query.tagId = undefined;
    query.tagIds = undefined;
    selectedTagPaths.value = [];
query.sortBy = "hot";
    query.pageNum = 1;
  }

  let loadTimer: number | null = null;
  function debouncedLoadScenicList(): void {
    if (loadTimer) {
      window.clearTimeout(loadTimer);
    }
    loadTimer = window.setTimeout(() => {
      void loadScenicList();
    }, 300);
  }

  /**
   * 组件初始化时先加载地区树和筛选选项，以便用户选择过滤条件，然后根据 URL 中的 keyword 参数加载景点列表。
   */
  async function init(): Promise<void> {
    await Promise.all([loadRegions(), loadFilterOptions()]);
    query.keyword = (route.query.keyword as string) || "";
    const routeTagId = Number(route.query.tagId);
    if (Number.isFinite(routeTagId) && routeTagId > 0) {
      query.tagId = routeTagId;
      const tag = tagOptions.value.find((item) => item.id === routeTagId);
      if (tag) {
        selectedTagPaths.value = [[tag.scope || "SCENIC", tag.category || "未分类", tag.id]];
      }
    }
    await loadScenicList();
  }

  onMounted(() => {
    void init();
  });

  onBeforeUnmount(() => {
    if (loadTimer) {
      window.clearTimeout(loadTimer);
    }
  });

  return {
    loading,
    scenicList,
    total,
    regionTreeData,
    categoryOptions,
    levelOptions,
    tagCascaderOptions,
    selectedTagPaths,
    selectedProvinceId,
    selectedCityId,
    currentCities,
    minScoreOptions,
    query,
    banners,
    isSearchActive,
    onProvinceChange,
    onCityChange,
    syncTagIdsFromPaths,
    getCurrentRegionName,
    loadScenicList,
    debouncedLoadScenicList,
    toggleFavorite,
    resetFilters,
  };
}
