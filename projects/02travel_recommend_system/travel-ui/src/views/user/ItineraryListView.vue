<template>
  <div class="itinerary-list-container">
    <el-card class="list-card">
      <ItineraryListToolbar
        :query="queryParams"
        :loading="loading"
        @create="handleCreate"
        @search="fetchList"
        @reset="resetQuery"
      />

      <ItineraryListTable
        :loading="loading"
        :list="list"
        :total="total"
        :current-page="queryParams.pageNum"
        :page-size="queryParams.pageSize"
        @detail="handleDetail"
        @edit="handleEdit"
        @delete="handleDelete"
        @page-change="handlePageChange"
        @size-change="handlePageSizeChange"
      />
    </el-card>

    <ItineraryEditDialog
      :visible="dialogVisible"
      :dialog-type="dialogType"
      :form="form"
      :submit-loading="submitLoading"
      @submit="submitForm"
      @update:visible="dialogVisible = $event"
    />
  </div>
</template>

<script setup lang="ts">
import { useItineraryList } from "@/composables/useItineraryList";
import ItineraryEditDialog from "./components/ItineraryEditDialog.vue";
import ItineraryListTable from "./components/ItineraryListTable.vue";
import ItineraryListToolbar from "./components/ItineraryListToolbar.vue";

const {
  loading,
  list,
  total,
  queryParams,
  dialogVisible,
  dialogType,
  submitLoading,
  form,
  fetchList,
  resetQuery,
  handleCreate,
  handleEdit,
  handleDelete,
  submitForm,
  handleDetail,
  handlePageChange,
  handlePageSizeChange,
} = useItineraryList();
</script>

<style scoped>
.itinerary-list-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.list-card {
  border-radius: 8px;
}
</style>
