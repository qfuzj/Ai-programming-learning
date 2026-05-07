import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  addItineraryItem,
  deleteItinerary,
  deleteItineraryItem,
  getItineraryDetail,
  updateItinerary,
  updateItineraryItem,
  type ItineraryDayItem,
  type ItineraryItem,
} from "@/api/itinerary";
import { createDefaultItineraryForm, type ItineraryFormModel } from "@/types/itinerary-list";
import {
  createDefaultItineraryDayForm,
  type ItineraryItemDialogMode,
  type ItineraryDayFormModel,
} from "@/types/itinerary-detail";

export function useItineraryDetail() {
  const route = useRoute();
  const router = useRouter();
  const planId = Number(route.params.id);

  const loading = ref(false);
  const detail = ref<ItineraryItem | null>(null);
  const activeDay = ref("1");
  const planDialogVisible = ref(false);
  const planSubmitLoading = ref(false);
  const planForm = reactive<ItineraryFormModel>(createDefaultItineraryForm());

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
      const sortOrderCompare =
        (left.sortOrder ?? Number.MAX_SAFE_INTEGER) - (right.sortOrder ?? Number.MAX_SAFE_INTEGER);
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

  function openEditPlan(): void {
    if (!detail.value) return;
    Object.assign(planForm, {
      title: detail.value.title,
      coverImage: detail.value.coverImage,
      startDate: detail.value.startDate,
      endDate: detail.value.endDate,
      totalDays: detail.value.totalDays || 1,
      destinationRegionId: detail.value.destinationRegionId,
      description: detail.value.description,
      estimatedBudget: detail.value.estimatedBudget,
      travelCompanion: detail.value.travelCompanion,
      isPublic: detail.value.isPublic,
      status: detail.value.status,
    });
    planDialogVisible.value = true;
  }

  async function submitPlanForm(): Promise<void> {
    if (!planId) return;
    planSubmitLoading.value = true;
    try {
      await updateItinerary(planId, planForm);
      ElMessage.success("行程已更新");
      planDialogVisible.value = false;
      await fetchDetail();
    } catch {
      // axios 拦截器已弹错误提示
    } finally {
      planSubmitLoading.value = false;
    }
  }

  async function handleAddDay(): Promise<void> {
    if (!planId || !detail.value) return;
    const nextTotalDays = (detail.value.totalDays || 1) + 1;
    planSubmitLoading.value = true;
    try {
      await updateItinerary(planId, {
        title: detail.value.title,
        coverImage: detail.value.coverImage,
        startDate: detail.value.startDate,
        endDate: getNextEndDate(detail.value.endDate),
        totalDays: nextTotalDays,
        destinationRegionId: detail.value.destinationRegionId,
        description: detail.value.description,
        estimatedBudget: detail.value.estimatedBudget,
        travelCompanion: detail.value.travelCompanion,
        isPublic: detail.value.isPublic,
        status: detail.value.status,
      });
      ElMessage.success(`已新增 Day ${nextTotalDays}`);
      await fetchDetail();
      activeDay.value = String(nextTotalDays);
    } catch {
      // axios 拦截器已弹错误提示
    } finally {
      planSubmitLoading.value = false;
    }
  }

  function getNextEndDate(endDate?: string): string | undefined {
    if (!endDate) return undefined;
    const date = new Date(`${endDate}T00:00:00`);
    if (Number.isNaN(date.getTime())) return endDate;
    date.setDate(date.getDate() + 1);
    return date.toISOString().slice(0, 10);
  }

  async function handleDeletePlan(): Promise<void> {
    if (!planId) return;
    try {
      await ElMessageBox.confirm(
        "确定删除该行程吗？行程项也会一并删除，此操作不可恢复。",
        "删除行程",
        {
          confirmButtonText: "删除",
          cancelButtonText: "取消",
          type: "warning",
        }
      );
      await deleteItinerary(planId);
      ElMessage.success("行程已删除");
      void router.push("/itinerary");
    } catch {
      // 用户取消或接口失败，失败提示由拦截器处理
    }
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
    planDialogVisible,
    planSubmitLoading,
    planForm,
    itemDialogVisible,
    itemDialogMode,
    submitLoading,
    itemForm,
    getDayItems,
    openEditPlan,
    submitPlanForm,
    handleAddDay,
    handleDeletePlan,
    openAddItem,
    openEditItem,
    submitItem,
    handleDeleteItem,
  };
}
