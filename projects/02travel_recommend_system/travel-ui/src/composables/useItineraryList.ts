import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  createItinerary,
  deleteItinerary,
  getItineraryPage,
  updateItinerary,
  type ItineraryItem,
  type ItineraryQuery,
} from "@/api/itinerary";
import {
  createDefaultItineraryForm,
  type ItineraryDialogType,
  type ItineraryFormModel,
} from "@/types/itinerary-list";

export function useItineraryList() {
  const router = useRouter();

  const loading = ref(false);
  const list = ref<ItineraryItem[]>([]);
  const total = ref(0);

  const queryParams = reactive<ItineraryQuery>({
    pageNum: 1,
    pageSize: 10,
    status: undefined,
    keyword: undefined,
    isPublic: undefined,
  });

  const dialogVisible = ref(false);
  const dialogType = ref<ItineraryDialogType>("create");
  const submitLoading = ref(false);
  const currentEditId = ref<number | undefined>(undefined);

  const form = reactive<ItineraryFormModel>(createDefaultItineraryForm());

  function resetForm(): void {
    Object.assign(form, createDefaultItineraryForm());
  }

  async function fetchList(): Promise<void> {
    loading.value = true;
    try {
      const res = await getItineraryPage(queryParams);
      list.value = res.records || [];
      total.value = res.total || 0;
    } catch (error: any) {
      ElMessage.error(error?.message || "获取行程列表失败");
    } finally {
      loading.value = false;
    }
  }

  function resetQuery(): void {
    queryParams.status = undefined;
    queryParams.keyword = undefined;
    queryParams.isPublic = undefined;
    queryParams.pageNum = 1;
    void fetchList();
  }

  function handleCreate(): void {
    dialogType.value = "create";
    currentEditId.value = undefined;
    resetForm();
    dialogVisible.value = true;
  }

  function handleEdit(row: ItineraryItem): void {
    dialogType.value = "edit";
    currentEditId.value = row.id;
    Object.assign(form, {
      title: row.title,
      startDate: row.startDate,
      endDate: row.endDate,
      totalDays: row.totalDays || 1,
      status: row.status,
      isPublic: row.isPublic,
      description: row.description,
    });
    dialogVisible.value = true;
  }

  async function handleDelete(row: ItineraryItem): Promise<void> {
    try {
      await deleteItinerary(row.id);
      ElMessage.success("删除成功");
      void fetchList();
    } catch (error: any) {
      ElMessage.error(error?.message || "删除失败");
    }
  }

  async function submitForm(): Promise<void> {
    submitLoading.value = true;
    try {
      if (dialogType.value === "create") {
        await createItinerary(form);
        ElMessage.success("创建成功");
      } else if (currentEditId.value) {
        await updateItinerary(currentEditId.value, form);
        ElMessage.success("更新成功");
      }
      dialogVisible.value = false;
      void fetchList();
    } catch (error: any) {
      ElMessage.error(error?.message || "保存失败");
    } finally {
      submitLoading.value = false;
    }
  }

  function handleDetail(row: ItineraryItem): void {
    void router.push(`/itinerary/${row.id}`);
  }

  function handlePageChange(page: number): void {
    queryParams.pageNum = page;
    void fetchList();
  }

  function handlePageSizeChange(size: number): void {
    queryParams.pageSize = size;
    queryParams.pageNum = 1;
    void fetchList();
  }

  onMounted(() => {
    void fetchList();
  });

  return {
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
  };
}
