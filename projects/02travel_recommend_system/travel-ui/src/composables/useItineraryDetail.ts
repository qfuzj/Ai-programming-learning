import { onMounted, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  addItineraryItem,
  deleteItineraryItem,
  getItineraryDetail,
  updateItineraryItem,
  type ItineraryDayItem,
  type ItineraryItem,
} from "@/api/itinerary";
import {
  createDefaultItineraryDayForm,
  type ItineraryItemDialogMode,
  type ItineraryDayFormModel,
} from "@/types/itinerary-detail";

export function useItineraryDetail() {
  const route = useRoute();
  const planId = Number(route.params.id);

  const loading = ref(false);
  const detail = ref<ItineraryItem | null>(null);
  const activeDay = ref("1");

  const itemDialogVisible = ref(false);
  const itemDialogMode = ref<ItineraryItemDialogMode>("create");
  const submitLoading = ref(false);
  const itemForm = reactive<ItineraryDayFormModel>(createDefaultItineraryDayForm(1));

  async function fetchDetail(): Promise<void> {
    if (!planId) return;
    loading.value = true;
    try {
      const res = await getItineraryDetail(planId);
      detail.value = res;
    } catch {
      // axios 拦截器已弹错误提示
    } finally {
      loading.value = false;
    }
  }

  function getDayItems(day: number): ItineraryDayItem["items"] {
    if (!detail.value?.days) return [];
    const targetDay = detail.value.days.find((d) => d.dayNo === day);
    if (!targetDay) {
      return [];
    }
    return [...targetDay.items].sort((left, right) => {
      const timeCompare = toTimeValue(left.startTime) - toTimeValue(right.startTime);
      if (timeCompare !== 0) {
        return timeCompare;
      }
      const sortOrderCompare = (left.sortOrder ?? Number.MAX_SAFE_INTEGER) - (right.sortOrder ?? Number.MAX_SAFE_INTEGER);
      if (sortOrderCompare !== 0) {
        return sortOrderCompare;
      }
      return (left.id ?? Number.MAX_SAFE_INTEGER) - (right.id ?? Number.MAX_SAFE_INTEGER);
    });
  }

  function toTimeValue(value?: string): number {
    if (!value) {
      return Number.MAX_SAFE_INTEGER;
    }
    const [hourText = "", minuteText = ""] = value.split(":");
    const hour = Number(hourText);
    const minute = Number(minuteText);
    if (Number.isNaN(hour) || Number.isNaN(minute)) {
      return Number.MAX_SAFE_INTEGER;
    }
    return hour * 60 + minute;
  }

  function openAddItem(day: number): void {
    itemDialogMode.value = "create";
    Object.assign(itemForm, createDefaultItineraryDayForm(day));
    itemDialogVisible.value = true;
  }

  function openEditItem(item: ItineraryDayItem["items"][number]): void {
    itemDialogMode.value = "edit";
    Object.assign(itemForm, createDefaultItineraryDayForm(item.dayNo), {
      id: item.id,
      scenicSpotId: item.scenicSpotId,
      dayNo: item.dayNo,
      sortOrder: item.sortOrder,
      itemType: item.itemType ?? 5,
      title: item.title,
      description: item.description ?? "",
      startTime: item.startTime ?? "",
      endTime: item.endTime ?? "",
      location: item.location ?? "",
      longitude: item.longitude,
      latitude: item.latitude,
      estimatedCost: item.estimatedCost ?? 0,
      notes: item.notes ?? "",
    });
    itemDialogVisible.value = true;
  }

  async function submitItem(): Promise<void> {
    submitLoading.value = true;
    try {
      const payload = {
        dayNo: itemForm.dayNo,
        scenicSpotId: itemForm.scenicSpotId,
        sortOrder: itemForm.sortOrder,
        itemType: itemForm.itemType,
        title: itemForm.title,
        description: itemForm.description,
        startTime: itemForm.startTime || undefined,
        endTime: itemForm.endTime || undefined,
        location: itemForm.location,
        longitude: itemForm.longitude,
        latitude: itemForm.latitude,
        estimatedCost: itemForm.estimatedCost,
        notes: itemForm.notes,
      };
      const isEdit = itemDialogMode.value === "edit" && itemForm.id;
      if (isEdit) {
        await updateItineraryItem(planId, itemForm.id as number, payload);
        ElMessage.success("修改成功");
      } else {
        await addItineraryItem(planId, payload);
        ElMessage.success("添加成功");
      }
      itemDialogVisible.value = false;
      void fetchDetail();
    } catch {
      // axios 拦截器已弹错误提示
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
    } catch {
      // 可能来源：1) ElMessageBox.confirm 用户点取消（无需提示）
      //         2) deleteItineraryItem 失败（axios 拦截器已弹错误提示）
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
    itemDialogMode,
    submitLoading,
    itemForm,
    getDayItems,
    openAddItem,
    openEditItem,
    submitItem,
    handleDeleteItem,
  };
}
