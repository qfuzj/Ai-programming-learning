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
import { getBrowseHistoryPage, type BrowseHistoryItem } from "@/api/history";
import { getFavorites, type FavoriteItem } from "@/api/favorite";
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
      } else {
        ElMessage.error("获取个人资料失败");
      }

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
      ElMessage.error("保存失败，请检查数据");
      return false;
    } finally {
      saving.value = false;
    }
  }

  async function loadHistory(): Promise<void> {
    tabLoading.value = true;
    try {
      const page = await getBrowseHistoryPage({ pageNum: 1, pageSize: 20 });
      historyList.value = page.records || [];
    } catch {
      ElMessage.error("获取浏览历史失败");
    } finally {
      tabLoading.value = false;
    }
  }

  async function loadFavorites(): Promise<void> {
    tabLoading.value = true;
    try {
      favoriteList.value = await getFavorites({ pageNum: 1, pageSize: 20 });
    } catch {
      ElMessage.error("获取收藏列表失败");
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
      ElMessage.error("标签数据加载失败");
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
      ElMessage.error("保存失败，请重试");
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

  return {
    loading,
    saving,
    profile,
    portrait,
    historyList,
    favoriteList,
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
  };
}
