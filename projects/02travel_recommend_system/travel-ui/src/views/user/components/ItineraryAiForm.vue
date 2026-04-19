<template>
  <el-form :model="form" label-position="top" @submit.prevent="emit('submit')">
    <el-row :gutter="12">
      <el-col :span="12">
        <el-form-item label="目的地">
          <el-input v-model="form.destination" placeholder="例如：重庆" clearable />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="天数">
          <el-input-number
            v-model="form.days"
            :min="1"
            :max="15"
            :disabled="true"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item label="出发日期 / 结束日期">
      <el-date-picker
        v-model="travelDateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="请选择出发日期"
        end-placeholder="请选择结束日期"
        value-format="YYYY-MM-DD"
        unlink-panels
        clearable
        style="width: 100%"
      />
    </el-form-item>

    <el-row :gutter="12">
      <el-col :span="12">
        <el-form-item label="预算（可选）">
          <el-input-number v-model="form.budget" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="出游同伴（可选）">
          <el-select
            v-model="form.companionType"
            clearable
            placeholder="请选择"
            style="width: 100%"
          >
            <el-option label="独自出行" value="solo" />
            <el-option label="情侣" value="couple" />
            <el-option label="家庭" value="family" />
            <el-option label="朋友" value="friends" />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item label="旅行风格（可选）">
      <el-select v-model="form.travelStyle" clearable placeholder="请选择" style="width: 100%">
        <el-option label="经典打卡" value="classic" />
        <el-option label="深度慢游" value="slow" />
        <el-option label="美食优先" value="food" />
        <el-option label="自然户外" value="outdoor" />
      </el-select>
    </el-form-item>

    <el-form-item label="偏好标签（可选）">
      <el-input
        v-model="form.preferredTagsText"
        placeholder="多个标签用逗号或空格分隔，例如：古镇, 夜景, 火锅"
        clearable
      />
    </el-form-item>

    <div class="action-row">
      <el-button type="primary" :loading="loading" :disabled="!canSubmit" native-type="submit">
        生成草案
      </el-button>
      <el-button :disabled="loading" @click="emit('reset')">重置</el-button>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { ItineraryAiFormModel } from "@/types/itinerary-ai";

interface Props {
  form: ItineraryAiFormModel;
  loading: boolean;
  canSubmit: boolean;
}

const props = defineProps<Props>();
const form = props.form;

const emit = defineEmits<{
  submit: [];
  reset: [];
}>();

const travelDateRange = computed<[string, string] | []>({
  get() {
    if (props.form.startDate && props.form.endDate) {
      return [props.form.startDate, props.form.endDate];
    }
    return [];
  },
  set(value) {
    if (Array.isArray(value) && value.length === 2) {
      [props.form.startDate, props.form.endDate] = value;
      return;
    }
    props.form.startDate = undefined;
    props.form.endDate = undefined;
  },
});
</script>

<style scoped>
.action-row {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>
