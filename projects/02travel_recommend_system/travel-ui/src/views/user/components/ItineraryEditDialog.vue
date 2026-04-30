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

      <el-form-item label="出发日期" prop="startDate">
        <el-date-picker
          v-model="form.startDate"
          type="date"
          placeholder="选择出发日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker
          v-model="form.endDate"
          type="date"
          placeholder="选择结束日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="总天数" prop="totalDays">
        <el-input-number v-model="form.totalDays" :min="1" :max="90" />
      </el-form-item>

      <el-form-item label="目的地区域">
        <el-input-number
          v-model="form.destinationRegionId"
          :min="1"
          :step="1"
          placeholder="可选，填写区域 ID"
          style="width: 100%"
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

const rules = reactive<FormRules>({
  title: [{ required: true, message: "请输入行程标题", trigger: "blur" }],
  totalDays: [{ required: true, message: "请填写总天数", trigger: "blur" }],
});

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      formRef.value?.clearValidate();
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
