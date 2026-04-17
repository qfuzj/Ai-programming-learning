import { computed, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { generateItineraryByAi } from "@/api/itinerary";
import { createDefaultItineraryAiForm, type ItineraryAiFormModel } from "@/types/itinerary-ai";

export function useItineraryAiGenerate() {
  const form = reactive<ItineraryAiFormModel>(createDefaultItineraryAiForm());
  const loading = ref(false);
  const errorMessage = ref("");
  const result = ref<unknown>(null);

  const canSubmit = computed(() => form.destination.trim().length > 0 && form.days > 0);

  function parsePreferredTags(text: string): string[] | undefined {
    const tags = text
      .split(/[,，\s]+/)
      .map((tag) => tag.trim())
      .filter(Boolean);
    return tags.length > 0 ? tags : undefined;
  }

  function resetForm(): void {
    Object.assign(form, createDefaultItineraryAiForm());
    errorMessage.value = "";
    result.value = null;
  }

  async function generateDraft(): Promise<void> {
    if (!canSubmit.value) {
      return;
    }

    loading.value = true;
    errorMessage.value = "";
    result.value = null;

    try {
      const payload = {
        destination: form.destination.trim(),
        days: form.days,
        budget: form.budget,
        companionType: form.companionType,
        travelStyle: form.travelStyle,
        preferredTags: parsePreferredTags(form.preferredTagsText),
      };

      const response = await generateItineraryByAi(payload);
      result.value = response;
      ElMessage.success("AI 行程草案生成成功");
    } catch (error: any) {
      const message = error?.message || "生成失败，请稍后重试";
      errorMessage.value = message;
      ElMessage.warning(message);
    } finally {
      loading.value = false;
    }
  }

  return {
    form,
    loading,
    errorMessage,
    result,
    canSubmit,
    resetForm,
    generateDraft,
  };
}
