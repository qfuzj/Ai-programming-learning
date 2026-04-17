<template>
  <div v-loading="loading" class="itinerary-detail-container">
    <ItineraryDetailHeader :detail="detail" @back="router.back()" />

    <ItineraryDaysPanel
      :detail="detail"
      :active-day="activeDay"
      :get-day-items="getDayItems"
      @update:active-day="activeDay = $event"
      @add-item="openAddItem"
      @delete-item="handleDeleteItem"
    />

    <ItineraryItemDialog
      :visible="itemDialogVisible"
      :form="itemForm"
      :submit-loading="submitLoading"
      @submit="submitItem"
      @update:visible="itemDialogVisible = $event"
    />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { useItineraryDetail } from "@/composables/useItineraryDetail";
import ItineraryDaysPanel from "./components/ItineraryDaysPanel.vue";
import ItineraryDetailHeader from "./components/ItineraryDetailHeader.vue";
import ItineraryItemDialog from "./components/ItineraryItemDialog.vue";

const router = useRouter();

const {
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
} = useItineraryDetail();
</script>

<style scoped>
.itinerary-detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}
</style>
