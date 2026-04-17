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

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">草稿</el-radio>
          <el-radio :label="2">已发布</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="是否公开" prop="isPublic">
        <el-radio-group v-model="form.isPublic">
          <el-radio :label="0">私有</el-radio>
          <el-radio :label="1">公开</el-radio>
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
