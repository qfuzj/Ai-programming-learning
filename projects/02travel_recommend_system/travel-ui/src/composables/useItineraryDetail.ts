import { onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  addItineraryItem,
  deleteItineraryItem,
  getItineraryDetail,
  type ItineraryDayItem,
  type ItineraryItem,
} from "@/api/itinerary";
import {
  createDefaultItineraryDayForm,
  type ItineraryDayFormModel,
} from "@/types/itinerary-detail";

export function useItineraryDetail() {
  const route = useRoute();
  const planId = Number(route.params.id);

  const loading = ref(false);
  const detail = ref<ItineraryItem | null>(null);
  const activeDay = ref("1");

  const itemDialogVisible = ref(false);
  const submitLoading = ref(false);
  const itemForm = reactive<ItineraryDayFormModel>(createDefaultItineraryDayForm(1));

  async function fetchDetail(): Promise<void> {
    if (!planId) return;
    loading.value = true;
    try {
      const res = await getItineraryDetail(planId);
      detail.value = res;
    } catch (error: any) {
      ElMessage.error(error?.message || "获取详情失败");
    } finally {
      loading.value = false;
    }
  }

  function getDayItems(day: number): ItineraryDayItem["items"] {
    if (!detail.value?.days) return [];
    const targetDay = detail.value.days.find((d) => d.dayNo === day);
    return targetDay ? targetDay.items : [];
  }

  function openAddItem(day: number): void {
    Object.assign(itemForm, createDefaultItineraryDayForm(day));
    itemDialogVisible.value = true;
  }

  async function submitItem(): Promise<void> {
    submitLoading.value = true;
    try {
      await addItineraryItem(planId, itemForm);
      ElMessage.success("添加成功");
      itemDialogVisible.value = false;
      void fetchDetail();
    } catch (error: any) {
      ElMessage.error(error?.message || "添加失败");
    } finally {
      submitLoading.value = false;
    }
  }

  async function handleDeleteItem(itemId: number): Promise<void> {
    try {
      await ElMessageBox.confirm("确定要删除该行程项吗？此操作不可恢复", "提示", {
        confirmButtonText: "删除",
        cancelButtonText: "取消",
        type: "warning",
      });
      await deleteItineraryItem(planId, itemId);
      ElMessage.success("已删除");
      void fetchDetail();
    } catch (cancelOrError: any) {
      if (cancelOrError !== "cancel") {
        ElMessage.error(cancelOrError?.message || "删除失败");
      }
    }
  }

  onMounted(() => {
    void fetchDetail();
  });

  return {
    loading,
    detail,
    activeDay,
    itemDialogVisible,
    submitLoading,
    itemForm,
    getDayItems,
    openAddItem,
    submitItem,
    handleDeleteItem,
  };
}
