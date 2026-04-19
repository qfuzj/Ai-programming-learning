import { reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";
import { useUserStore } from "@/store";
import {
  getProfileInfo,
  getProfilePortrait,
  updateProfileInfo,
  updatePreferenceTags,
  getMyPreferenceTags,
} from "@/api/profile";
import { getTags, type CommonTagItem } from "@/api/common";
import {
  clearBrowseHistory,
  deleteBrowseHistory,
  getBrowseHistoryPage,
  type BrowseHistoryItem,
} from "@/api/history";
import {
  getFavoritesPage,
  removeFavorite,
  clearFavorites,
  type FavoriteItem,
} from "@/api/favorite";
import type { ProfileInfo, ProfilePortraitSummary, UpdateProfilePayload } from "@/types/profile";

export function useProfile() {
  const router = useRouter();
  const userStore = useUserStore();

  const loading = ref(false);
  const saving = ref(false);

  const profile = reactive<ProfileInfo>({
    id: 0,
    username: "",
    nickname: "",
    avatar: "",
    signature: "",
    gender: 0,
    birthday: "",
    role: "USER",
  });

  const portrait = ref<ProfilePortraitSummary | null>(null);

  const historyList = ref<BrowseHistoryItem[]>([]);
  const favoriteList = ref<FavoriteItem[]>([]);
  const historyTotal = ref(0);
  const favoriteTotal = ref(0);
  const historyPageNum = ref(1);
  const favoritePageNum = ref(1);
  const historyPageSize = ref(6);
  const favoritePageSize = ref(6);
  const tabLoading = ref(false);

  const allTags = ref<CommonTagItem[]>([]);
  const selectedTagIds = ref<number[]>([]);
  const tagSaving = ref(false);

  function syncUserStore(info: ProfileInfo): void {
    if (!userStore.profile) return;
    userStore.profile = {
      ...userStore.profile,
      username: info.username || userStore.profile.username,
      nickname: info.nickname || userStore.profile.nickname,
      avatar: info.avatar || "",
    };
  }

  async function loadProfile(): Promise<void> {
    loading.value = true;
    try {
      const [profileResult, portraitResult] = await Promise.allSettled([
        getProfileInfo(),
        getProfilePortrait(),
      ]);

      if (profileResult.status === "fulfilled") {
        Object.assign(profile, profileResult.value);
        syncUserStore(profileResult.value);
      }
      // 失败分支无需弹 toast：axios 拦截器已对每个失败请求统一提示

      if (portraitResult.status === "fulfilled") {
        portrait.value = portraitResult.value;
      }
    } finally {
      loading.value = false;
    }
  }

  async function saveProfile(payload: UpdateProfilePayload): Promise<boolean> {
    saving.value = true;
    try {
      await updateProfileInfo({
        nickname: payload.nickname || undefined,
        avatar: payload.avatar || undefined,
        signature: payload.signature || undefined,
        gender: payload.gender,
        birthday: payload.birthday || undefined,
      });
      ElMessage.success("个人资料已更新");
      await loadProfile();
      return true;
    } catch {
      // axios 拦截器已弹错误提示
      return false;
    } finally {
      saving.value = false;
    }
  }

  async function loadHistory(
    pageNum = historyPageNum.value,
    pageSize = historyPageSize.value
  ): Promise<void> {
    tabLoading.value = true;
    try {
      historyPageNum.value = pageNum;
      historyPageSize.value = pageSize;
      const page = await getBrowseHistoryPage({ pageNum, pageSize });
      historyList.value = page.records || [];
      historyTotal.value = page.total || 0;
    } catch {
      // axios 拦截器已弹错误提示
    } finally {
      tabLoading.value = false;
    }
  }

  async function loadFavorites(
    pageNum = favoritePageNum.value,
    pageSize = favoritePageSize.value
  ): Promise<void> {
    tabLoading.value = true;
    try {
      favoritePageNum.value = pageNum;
      favoritePageSize.value = pageSize;
      const page = await getFavoritesPage({ pageNum, pageSize });
      favoriteList.value = page.records || [];
      favoriteTotal.value = page.total || 0;
    } catch {
      // axios 拦截器已弹错误提示
    } finally {
      tabLoading.value = false;
    }
  }

  async function openTagDialog(): Promise<boolean> {
    try {
      const [tags, myTags] = await Promise.all([getTags(), getMyPreferenceTags()]);
      allTags.value = tags;
      selectedTagIds.value = myTags.map((t: { id: number | string }) => Number(t.id));
      return true;
    } catch {
      // axios 拦截器已弹错误提示
      return false;
    }
  }

  async function saveTags(ids: number[]): Promise<boolean> {
    tagSaving.value = true;
    try {
      await updatePreferenceTags(ids);
      ElMessage.success("偏好标签已更新");
      portrait.value = await getProfilePortrait();
      return true;
    } catch {
      // axios 拦截器已弹错误提示
      return false;
    } finally {
      tagSaving.value = false;
    }
  }

  function goToScenic(id: number): void {
    router.push(`/scenic/${id}`);
  }

  function goToScenicList(): void {
    router.push("/scenic");
  }

  async function removeHistoryItem(id: number): Promise<boolean> {
    try {
      await deleteBrowseHistory(id);
      if (historyList.value.length === 1 && historyPageNum.value > 1) {
        historyPageNum.value -= 1;
      }
      await loadHistory(historyPageNum.value, historyPageSize.value);
      return true;
    } catch {
      // axios 拦截器已弹错误提示
      return false;
    }
  }

  async function clearHistoryItems(): Promise<boolean> {
    try {
      await clearBrowseHistory();
      historyList.value = [];
      historyTotal.value = 0;
      historyPageNum.value = 1;
      return true;
    } catch {
      // axios 拦截器已弹错误提示
      return false;
    }
  }

  async function removeFavoriteItem(scenicId: number): Promise<boolean> {
    try {
      await removeFavorite(scenicId);
      if (favoriteList.value.length === 1 && favoritePageNum.value > 1) {
        favoritePageNum.value -= 1;
      }
      await loadFavorites(favoritePageNum.value, favoritePageSize.value);
      portrait.value = await getProfilePortrait();
      return true;
    } catch {
      // axios 拦截器已弹错误提示
      return false;
    }
  }

  async function clearFavoriteItems(): Promise<boolean> {
    try {
      await clearFavorites();
      favoriteList.value = [];
      favoriteTotal.value = 0;
      favoritePageNum.value = 1;
      portrait.value = await getProfilePortrait();
      return true;
    } catch {
      // axios 拦截器已弹错误提示
      return false;
    }
  }

  return {
    loading,
    saving,
    profile,
    portrait,
    historyList,
    favoriteList,
    historyTotal,
    favoriteTotal,
    historyPageNum,
    favoritePageNum,
    historyPageSize,
    favoritePageSize,
    tabLoading,
    allTags,
    selectedTagIds,
    tagSaving,
    loadProfile,
    saveProfile,
    loadHistory,
    loadFavorites,
    openTagDialog,
    saveTags,
    goToScenic,
    goToScenicList,
    removeHistoryItem,
    clearHistoryItems,
    removeFavoriteItem,
    clearFavoriteItems,
  };
}
