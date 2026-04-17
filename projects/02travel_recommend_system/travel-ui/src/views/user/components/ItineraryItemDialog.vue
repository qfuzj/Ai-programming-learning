<template>
  <el-dialog
    :model-value="visible"
    title="添加行程项"
    width="500px"
    append-to-body
    @update:model-value="(value) => emit('update:visible', value)"
  >
    <el-form ref="itemFormRef" :model="form" :rules="itemRules" label-width="100px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="如: 锦里古街游玩" />
      </el-form-item>

      <el-form-item label="活动类型" prop="itemType">
        <el-select v-model="form.itemType" placeholder="选择类型" style="width: 100%">
          <el-option label="景点" :value="1" />
          <el-option label="餐饮" :value="2" />
          <el-option label="住宿" :value="3" />
          <el-option label="交通" :value="4" />
          <el-option label="其他" :value="5" />
        </el-select>
      </el-form-item>

      <el-form-item label="开始时间">
        <el-time-select
          v-model="form.startTime"
          start="06:00"
          step="00:30"
          end="23:30"
          placeholder="选择时间"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="结束时间">
        <el-time-select
          v-model="form.endTime"
          start="06:00"
          step="00:30"
          end="23:30"
          placeholder="选择时间"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="2" placeholder="活动描述" />
      </el-form-item>

      <el-form-item label="预算(元)">
        <el-input-number v-model="form.estimatedCost" :min="0" :step="10" />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="emit('update:visible', false)">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type { ItineraryDayFormModel } from "@/types/itinerary-detail";

interface Props {
  visible: boolean;
  form: ItineraryDayFormModel;
  submitLoading: boolean;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  submit: [];
  "update:visible": [value: boolean];
}>();

const itemFormRef = ref<FormInstance>();

const itemRules = reactive<FormRules>({
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  itemType: [{ required: true, message: "请选择类型", trigger: "change" }],
});

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      itemFormRef.value?.clearValidate();
    }
  }
);

async function handleSubmit(): Promise<void> {
  if (!itemFormRef.value) return;
  await itemFormRef.value.validate(async (valid) => {
    if (valid) {
      emit("submit");
    }
  });
}
</script>
