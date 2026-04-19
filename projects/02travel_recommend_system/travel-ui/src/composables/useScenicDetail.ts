import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { getScenicDetail, type ScenicDetail } from "@/api/scenic";
import { reportBrowseHistory } from "@/api/history";
import { addFavorite, removeFavorite } from "@/api/favorite";
import { getScenicReviews, type ReviewItem } from "@/api/audit";
import type { PageQuery } from "@/types/api";
import { fetchSimilarRecommendations, type AiRecommendItem } from "@/api/recommend";
import { ElMessage } from "element-plus";

export function useScenicDetail() {
  const router = useRouter();
  const loading = ref(false);
  const detail = ref<ScenicDetail | null>(null);
  const introExpanded = ref(false);

  const reviewsLoading = ref(false);
  const reviews = ref<ReviewItem[]>([]);
  const reviewsTotal = ref(0);
  const reviewQuery = reactive<PageQuery>({ pageNum: 1, pageSize: 10 });

  const relatedExperiences = ref<AiRecommendItem[]>([]);

  const galleryImages = computed(() => {
    if (!detail.value) return [];
    const list: string[] = [];
    const fallback = detail.value.coverImage || "https://via.placeholder.com/800x600?text=No+Image";
    list.push(fallback);

    if (detail.value.images && detail.value.images.length > 0) {
      const validImages = detail.value.images
        .map((img) => img.imageUrl)
        .filter((url: string) => !!url && !url.includes("example.com"));
      list.push(...validImages);
    }

    while (list.length < 3) {
      list.push(fallback);
    }
    return list;
  });

  async function loadReviews() {
    if (!detail.value) return;
    reviewsLoading.value = true;
    try {
      const res = await getScenicReviews(detail.value.id, reviewQuery);
      reviews.value = res.records;
      reviewsTotal.value = res.total;
    } finally {
      reviewsLoading.value = false;
    }
  }

  async function loadSimilar() {
    if (!detail.value) return;
    try {
      const res = await fetchSimilarRecommendations(detail.value.id, { pageNum: 1, pageSize: 3 });
      relatedExperiences.value = res.records;
    } catch {
      relatedExperiences.value = [];
    }
  }

  async function loadDetail(id: number): Promise<void> {
    if (!id) {
      detail.value = null;
      return;
    }
    loading.value = true;
    try {
      const scenicDetail = await getScenicDetail(id);
      detail.value = scenicDetail;
      const isMobile = /Mobi|Android|iPhone|iPad|iPod/i.test(navigator.userAgent);
      void reportBrowseHistory({
        scenicId: scenicDetail.id,
        source: "scenic_detail",
        deviceType: isMobile ? "mobile" : "desktop",
      });
      reviewQuery.pageNum = 1;
      void loadReviews();
      void loadSimilar();
    } finally {
      loading.value = false;
    }
  }

  async function toggleFavorite() {
    if (!detail.value) return;
    try {
      if (detail.value.isFavorite) {
        await removeFavorite(detail.value.id);
        detail.value.isFavorite = false;
        detail.value.favoriteCount = Math.max(0, (detail.value.favoriteCount || 0) - 1);
        ElMessage.success("已取消收藏！");
      } else {
        await addFavorite(detail.value.id);
        detail.value.isFavorite = true;
        detail.value.favoriteCount = (detail.value.favoriteCount || 0) + 1;
        ElMessage.success("已保存至我的收藏！");
      }
    } catch {
      // axios 拦截器已弹错误提示
    }
  }

  function goWriteReview() {
    if (detail.value) {
      void router.push(`/my-reviews?action=write&scenicId=${detail.value.id}`);
    }
  }

  function goToSimilar(id: number) {
    void router.push(`/scenic/${id}`);
  }

  function scrollToReviews() {
    const el = document.getElementById("reviews-section");
    if (el) {
      el.scrollIntoView({ behavior: "smooth" });
    }
  }

  function updateReview(index: number, updated: ReviewItem): void {
    if (index < 0 || index >= reviews.value.length) return;
    reviews.value[index] = updated;
  }

  return {
    loading,
    detail,
    introExpanded,
    reviewsLoading,
    reviews,
    reviewsTotal,
    reviewQuery,
    relatedExperiences,
    galleryImages,
    loadReviews,
    loadDetail,
    toggleFavorite,
    goWriteReview,
    goToSimilar,
    scrollToReviews,
    updateReview,
  };
}
