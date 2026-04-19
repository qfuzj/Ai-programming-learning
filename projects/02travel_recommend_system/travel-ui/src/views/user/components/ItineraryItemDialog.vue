<template>
  <el-dialog
    :model-value="visible"
    :title="mode === 'edit' ? '编辑行程项' : '添加行程项'"
    width="640px"
    append-to-body
    @update:model-value="(value) => emit('update:visible', value)"
  >
    <el-form ref="itemFormRef" :model="form" :rules="itemRules" label-width="100px">
      <el-form-item label="所属天数" prop="dayNo">
        <el-select v-model="form.dayNo" placeholder="选择天数" style="width: 100%">
          <el-option v-for="day in totalDays" :key="day" :label="`Day ${day}`" :value="day" />
        </el-select>
      </el-form-item>

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

      <el-form-item label="活动地点">
        <el-input v-model="form.location" placeholder="如：丽江古城主街 / 酒店 / 车站" />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="2" placeholder="活动描述" />
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="form.notes"
          type="textarea"
          :rows="2"
          placeholder="如：提前预约、建议购买门票、注意事项"
        />
      </el-form-item>

      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="预算(元)">
            <el-input-number v-model="form.estimatedCost" :min="0" :step="10" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序值">
            <el-input-number
              v-model="form.sortOrder"
              :min="0"
              :step="10"
              placeholder="不填则按开始时间自动计算"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

     <!--  <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="经度">
            <el-input-number
              v-model="form.longitude"
              :precision="7"
              :step="0.0000001"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="纬度">
            <el-input-number
              v-model="form.latitude"
              :precision="7"
              :step="0.0000001"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row> -->

      <el-form-item label="关联景点ID">
        <el-input-number
          v-model="form.scenicSpotId"
          :min="1"
          :step="1"
          placeholder="可选，关联已有景点"
          style="width: 100%"
        />
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
  mode: "create" | "edit";
  totalDays: number;
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
  dayNo: [{ required: true, message: "请选择所属天数", trigger: "change" }],
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  itemType: [{ required: true, message: "请选择类型", trigger: "change" }],
  endTime: [
    {
      validator: (_rule, value, callback) => {
        if (!value || !props.form.startTime || value >= props.form.startTime) {
          callback();
          return;
        }
        callback(new Error("结束时间不能早于开始时间"));
      },
      trigger: "change",
    },
  ],
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
