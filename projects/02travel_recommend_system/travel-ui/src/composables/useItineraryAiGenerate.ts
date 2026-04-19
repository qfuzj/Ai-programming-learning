import { computed, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  addItineraryItem,
  createItinerary,
  deleteItinerary,
  generateItineraryByAi,
  type ItineraryCreatePayload,
  type ItineraryItem,
  type ItineraryItemPayload,
} from "@/api/itinerary";
import { createDefaultItineraryAiForm, type ItineraryAiFormModel } from "@/types/itinerary-ai";

export function useItineraryAiGenerate() {
  const router = useRouter();
  const form = reactive<ItineraryAiFormModel>(createDefaultItineraryAiForm());
  const loading = ref(false);
  const saveLoading = ref(false);
  const errorMessage = ref("");
  const result = ref<ItineraryItem | null>(null);

  const canSubmit = computed(() => {
    return Boolean(
      form.destination.trim().length > 0 && form.days > 0 && form.startDate && form.endDate
    );
  });
  const canSave = computed(() => {
    return Boolean(
      result.value &&
      result.value.title &&
      (result.value.totalDays ?? 0) > 0 &&
      result.value.days &&
      result.value.days.length > 0
    );
  });

  function parsePreferredTags(text: string): string[] | undefined {
    const tags = text
      .split(/[,，\s]+/)
      .map((tag) => tag.trim())
      .filter(Boolean);
    return tags.length > 0 ? tags : undefined;
  }

  function calculateDays(startDate?: string, endDate?: string): number | null {
    if (!startDate || !endDate) {
      return null;
    }
    const start = new Date(`${startDate}T00:00:00`);
    const end = new Date(`${endDate}T00:00:00`);
    const diff = end.getTime() - start.getTime();
    if (Number.isNaN(diff) || diff < 0) {
      return null;
    }
    return Math.floor(diff / 86400000) + 1;
  }

  function resetForm(): void {
    Object.assign(form, createDefaultItineraryAiForm());
    errorMessage.value = "";
    result.value = null;
  }

  function toCreatePayload(draft: ItineraryItem): ItineraryCreatePayload {
    return {
      title: draft.title,
      coverImage: draft.coverImage,
      startDate: draft.startDate,
      endDate: draft.endDate,
      totalDays: draft.totalDays || form.days,
      destinationRegionId: draft.destinationRegionId,
      description: draft.description,
      estimatedBudget: draft.estimatedBudget,
      travelCompanion: draft.travelCompanion,
      isPublic: draft.isPublic ?? 0,
      status: draft.status ?? 0,
    };
  }

  function toItemPayloads(draft: ItineraryItem): ItineraryItemPayload[] {
    return (draft.days || []).flatMap((day) =>
      (day.items || []).map((item) => ({
        dayNo: day.dayNo,
        scenicSpotId: item.scenicSpotId,
        sortOrder: item.sortOrder,
        itemType: item.itemType,
        title: item.title,
        description: item.description,
        startTime: item.startTime,
        endTime: item.endTime,
        location: item.location,
        longitude: item.longitude,
        latitude: item.latitude,
        estimatedCost: item.estimatedCost,
        notes: item.notes,
      }))
    );
  }

  async function generateDraft(): Promise<void> {
    if (!canSubmit.value) {
      return;
    }

    loading.value = true;
    errorMessage.value = "";
    result.value = null;

    try {
      if (!form.startDate || !form.endDate) {
        ElMessage.warning("请选择出发日期和结束日期");
        return;
      }
      const payload = {
        destination: form.destination.trim(),
        days: form.days,
        startDate: form.startDate,
        endDate: form.endDate,
        budget: form.budget,
        companionType: form.companionType,
        travelStyle: form.travelStyle,
        preferredTags: parsePreferredTags(form.preferredTagsText),
      };

      const response = await generateItineraryByAi(payload);
      result.value = response;
      ElMessage.success("AI 行程草案生成成功");
    } catch (error: any) {
      // 仅写入 errorMessage 用于页面内联 alert 展示；axios 拦截器 (utils/request.ts)
      // 对业务错误已统一弹出 ElMessage.error，这里若再弹会造成顶部出现两条相同提示
      errorMessage.value = error?.message || "生成失败，请稍后重试";
    } finally {
      loading.value = false;
    }
  }

  async function saveDraft(): Promise<void> {
    if (!result.value || !canSave.value) {
      return;
    }

    saveLoading.value = true;
    let createdPlanId: number | null = null;
    try {
      // 第 1 步：先创建主计划；失败会直接抛到外层 catch，无需回滚
      createdPlanId = await createItinerary(toCreatePayload(result.value));
      const itemPayloads = toItemPayloads(result.value);
      // 第 2 步：并行写入所有行程项。前端无跨接口事务，并行仅是性能优化；
      // 原子性依靠失败后调用 deleteItinerary 做"软回滚"
      await Promise.all(itemPayloads.map((p) => addItineraryItem(createdPlanId as number, p)));
      ElMessage.success("AI 草案已保存为正式行程");
      void router.push(`/itinerary/${createdPlanId}`);
    } catch {
      // axios 拦截器已对原始错误弹过 ElMessage.error，这里只追加回滚结果信息，避免重复提示
      if (createdPlanId !== null) {
        try {
          await deleteItinerary(createdPlanId);
          ElMessage.info("已自动回滚本次草案，可修改后重试");
        } catch (rollbackError) {
          console.error("rollback itinerary failed", rollbackError);
          ElMessage.error(`自动回滚未完成，请手动删除行程 #${createdPlanId}`);
        }
      }
    } finally {
      saveLoading.value = false;
    }
  }

  watch(
    () => [form.startDate, form.endDate] as const,
    ([startDate, endDate]) => {
      const nextDays = calculateDays(startDate, endDate);
      if (nextDays) {
        form.days = nextDays;
      }
    },
    { immediate: true }
  );

  return {
    form,
    loading,
    saveLoading,
    errorMessage,
    result,
    canSubmit,
    canSave,
    resetForm,
    generateDraft,
    saveDraft,
  };
}
