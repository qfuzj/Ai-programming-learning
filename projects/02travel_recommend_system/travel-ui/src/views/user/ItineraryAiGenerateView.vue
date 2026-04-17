<template>
  <PageStub title="AI 生成行程" description="输入偏好后生成可编辑的行程草案。">
    <ItineraryAiForm
      :form="form"
      :loading="loading"
      :can-submit="canSubmit"
      @submit="generateDraft"
      @reset="resetForm"
    />

    <ItineraryAiResult :loading="loading" :error-message="errorMessage" :result="result" />
  </PageStub>
</template>

<script setup lang="ts">
import { useItineraryAiGenerate } from "@/composables/useItineraryAiGenerate";
import ItineraryAiForm from "./components/ItineraryAiForm.vue";
import ItineraryAiResult from "./components/ItineraryAiResult.vue";

const { form, loading, errorMessage, result, canSubmit, resetForm, generateDraft } =
  useItineraryAiGenerate();
</script>
