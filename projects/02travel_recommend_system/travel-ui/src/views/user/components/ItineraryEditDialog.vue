<template>
  <el-dialog
    :model-value="visible"
    :title="dialogType === 'create' ? '新建行程' : '编辑行程'"
    width="600px"
    append-to-body
    @update:model-value="(value) => emit('update:visible', value)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="行程标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入行程标题 (必填，例: 重庆三日游)" />
      </el-form-item>

      <el-form-item label="出行日期" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="To"
          start-placeholder="Start date"
          end-placeholder="End date"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="总天数" prop="totalDays">
        <el-input-number v-model="form.totalDays" :min="1" :max="90" style="width: 100%" />
      </el-form-item>

      <el-form-item label="目的地">
        <el-input
          v-model="form.destination"
          placeholder="可选，输入任意目的地（如：川西、成都+乐山）"
        />
      </el-form-item>

      <el-form-item label="预计预算">
        <el-input-number
          v-model="form.estimatedBudget"
          :min="0"
          :step="100"
          placeholder="可选，单位元"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="旅行同伴">
        <el-select
          v-model="form.travelCompanion"
          placeholder="请选择"
          clearable
          style="width: 100%"
        >
          <el-option label="独自旅行" value="solo" />
          <el-option label="情侣出游" value="couple" />
          <el-option label="家庭旅行" value="family" />
          <el-option label="朋友同行" value="friends" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio v-for="item in statusOptions" :key="item.code" :label="item.code">
            {{ item.desc }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="是否公开" prop="isPublic">
        <el-radio-group v-model="form.isPublic">
          <el-radio v-for="item in publicOptions" :key="item.code" :label="item.code">
            {{ item.desc }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="行程简要描述..."
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="emit('update:visible', false)">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type { ItineraryDialogType, ItineraryFormModel } from "@/types/itinerary-list";
import { getPublicStatusDict, getTravelPlanStatusDict } from "@/api/dict";
import { useDictOptions } from "@/composables/useDictOptions";

const { options: statusOptions } = useDictOptions("travel-plan-status", getTravelPlanStatusDict);
const { options: publicOptions } = useDictOptions("public-status", getPublicStatusDict);

const DAY_MS = 24 * 60 * 60 * 1000;
const MAX_ITINERARY_DAYS = 90;

interface Props {
  visible: boolean;
  dialogType: ItineraryDialogType;
  form: ItineraryFormModel;
  submitLoading: boolean;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  submit: [];
  "update:visible": [value: boolean];
}>();

const formRef = ref<FormInstance>();
const dateRange = ref<[Date, Date] | null>(null);

const rules = reactive<FormRules>({
  title: [{ required: true, message: "请输入行程标题", trigger: "blur" }],
  startDate: [{ required: true, message: "请选择出行日期", trigger: "change" }],
  totalDays: [{ required: true, message: "请填写总天数", trigger: "blur" }],
});

function parseDateInput(dateStr: string): Date | null {
  if (!dateStr) return null;
  const date = new Date(`${dateStr}T00:00:00`);
  if (Number.isNaN(date.getTime())) return null;
  return date;
}

function formatDateInput(date: Date): string {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function clampDays(days: number): number {
  if (!Number.isFinite(days) || days < 1) return 1;
  if (days > MAX_ITINERARY_DAYS) return MAX_ITINERARY_DAYS;
  return Math.floor(days);
}

function normalizeRange(range: [Date, Date]): [Date, Date] {
  const start = new Date(range[0]);
  const end = new Date(range[1]);
  start.setHours(0, 0, 0, 0);
  end.setHours(0, 0, 0, 0);

  if (end.getTime() < start.getTime()) {
    end.setTime(start.getTime());
  }

  const inclusiveDays = Math.floor((end.getTime() - start.getTime()) / DAY_MS) + 1;
  if (inclusiveDays > MAX_ITINERARY_DAYS) {
    end.setTime(start.getTime() + (MAX_ITINERARY_DAYS - 1) * DAY_MS);
  }

  return [start, end];
}

function syncFormFromDateRange(range: [Date, Date]): void {
  const [start, end] = normalizeRange(range);
  const normalizedDays = Math.floor((end.getTime() - start.getTime()) / DAY_MS) + 1;

  if (
    !dateRange.value ||
    dateRange.value[0].getTime() !== start.getTime() ||
    dateRange.value[1].getTime() !== end.getTime()
  ) {
    dateRange.value = [start, end];
  }

  props.form.startDate = formatDateInput(start);
  props.form.endDate = formatDateInput(end);
  props.form.totalDays = normalizedDays;
}

function syncDateRangeFromForm(): void {
  const start = parseDateInput(props.form.startDate || "");
  if (!start) {
    dateRange.value = null;
    return;
  }

  const endFromForm = props.form.endDate ? parseDateInput(props.form.endDate) : null;
  if (endFromForm) {
    syncFormFromDateRange([start, endFromForm]);
    return;
  }

  const days = clampDays(Number(props.form.totalDays) || 1);
  const end = new Date(start.getTime() + (days - 1) * DAY_MS);
  dateRange.value = [start, end];
  props.form.endDate = formatDateInput(end);
}

watch(
  () => dateRange.value,
  (range) => {
    if (!range || range.length !== 2) {
      props.form.startDate = "";
      props.form.endDate = "";
      return;
    }
    syncFormFromDateRange(range);
  },
  { deep: true }
);

watch(
  () => props.form.totalDays,
  (value) => {
    const days = clampDays(Number(value));
    if (days !== value) {
      props.form.totalDays = days;
      return;
    }

    const start = parseDateInput(props.form.startDate || "");
    if (!start) return;

    const nextEnd = new Date(start.getTime() + (days - 1) * DAY_MS);
    const nextEndDate = formatDateInput(nextEnd);
    if (props.form.endDate !== nextEndDate) {
      props.form.endDate = nextEndDate;
      syncDateRangeFromForm();
    }
  }
);

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      formRef.value?.clearValidate();
      syncDateRangeFromForm();
    }
  }
);

async function handleSubmit(): Promise<void> {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      emit("submit");
    }
  });
}
</script>
