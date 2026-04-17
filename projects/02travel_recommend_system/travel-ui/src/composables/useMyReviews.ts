import { computed, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { deleteMyReview, getMyReviews, submitReview, type ReviewItem } from "@/api/audit";
import type { MyReviewForm } from "@/types/my-reviews";
import { ElMessage, ElMessageBox } from "element-plus";

function createDefaultForm(): MyReviewForm {
  return {
    scenicId: 0,
    score: 5,
    content: "",
    visitDate: "",
    travelType: "",
    isAnonymousBool: false,
  };
}

export function useMyReviews() {
  const route = useRoute();
  const router = useRouter();

  const isWriting = computed(() => route.query.action === "write" && route.query.scenicId);
  const targetScenicId = computed(() => Number(route.query.scenicId) || 0);

  const submitLoading = ref(false);
  const listLoading = ref(false);

  const reviewForm = reactive<MyReviewForm>(createDefaultForm());

  const reviews = ref<ReviewItem[]>([]);
  const total = ref(0);
  const currentPage = ref(1);
  const pageSize = ref(10);

  function resetWriteForm(scenicId: number): void {
    reviewForm.scenicId = scenicId;
    reviewForm.score = 5;
    reviewForm.content = "";
    reviewForm.visitDate = "";
    reviewForm.travelType = "";
    reviewForm.isAnonymousBool = false;
  }

  async function fetchMyReviews(): Promise<void> {
    listLoading.value = true;
    try {
      const res = await getMyReviews({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
      });
      reviews.value = res.records;
      total.value = res.total;
    } catch (error) {
      console.error("Failed to fetch reviews", error);
    } finally {
      listLoading.value = false;
    }
  }

  async function submitMyReview(): Promise<void> {
    submitLoading.value = true;
    try {
      await submitReview({
        scenicId: reviewForm.scenicId,
        score: reviewForm.score,
        content: reviewForm.content,
        visitDate: reviewForm.visitDate || undefined,
        travelType: reviewForm.travelType || undefined,
        isAnonymous: reviewForm.isAnonymousBool ? 1 : 0,
      });

      ElMessage.success("点评提交成功，请等待平台审核");
      cancelWrite();
    } catch (error) {
      console.error(error);
    } finally {
      submitLoading.value = false;
    }
  }

  async function handleDelete(id: number): Promise<void> {
    try {
      await ElMessageBox.confirm("确定要删除这条点评吗？删除后不可恢复", "提示", {
        type: "warning",
        confirmButtonText: "确定",
        cancelButtonText: "取消",
      });
      await deleteMyReview(id);
      ElMessage.success("删除成功");
      if (reviews.value.length === 1 && currentPage.value > 1) {
        currentPage.value -= 1;
      }
      void fetchMyReviews();
    } catch (error) {
      if (error !== "cancel") {
        console.error(error);
      }
    }
  }

  function handlePageChange(page: number): void {
    currentPage.value = page;
    void fetchMyReviews();
  }

  function goScenic(id: number | undefined): void {
    if (id) {
      void router.push(`/scenic/${id}`);
    }
  }

  function cancelWrite(): void {
    void router.replace({ path: "/my-reviews" });
  }

  watch(
    () => route.query,
    () => {
      if (isWriting.value) {
        resetWriteForm(targetScenicId.value);
      } else {
        void fetchMyReviews();
      }
    },
    { deep: true, immediate: true }
  );

  return {
    isWriting,
    submitLoading,
    listLoading,
    reviewForm,
    reviews,
    total,
    currentPage,
    pageSize,
    submitMyReview,
    handleDelete,
    handlePageChange,
    goScenic,
    cancelWrite,
  };
}
