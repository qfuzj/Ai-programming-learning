<template>
  <div class="write-review-section">
    <el-form
      ref="reviewFormRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      label-position="top"
    >
      <el-form-item label="整体评价" prop="score">
        <el-rate v-model="form.score" allow-half show-score />
      </el-form-item>

      <el-form-item label="您的体验和感受" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="分享您的游玩体验，为其他旅行者提供参考（至少15个字）"
        />
      </el-form-item>

      <el-form-item label="出游时间">
        <el-date-picker
          v-model="form.visitDate"
          type="date"
          placeholder="选择出游日期"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="出游类型">
        <el-select v-model="form.travelType" placeholder="请选择">
          <el-option label="个人游" value="个人游" />
          <el-option label="情侣游" value="情侣游" />
          <el-option label="家庭游" value="家庭游" />
          <el-option label="朋友出游" value="朋友出游" />
          <el-option label="商务出行" value="商务出行" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-checkbox v-model="form.isAnonymousBool">匿名评价</el-checkbox>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          :loading="loading"
          class="submit-btn"
          size="large"
          @click="handleSubmit"
        >
          提交点评
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type { MyReviewForm } from "@/types/my-reviews";

interface Props {
  form: MyReviewForm;
  loading: boolean;
}

defineProps<Props>();

const emit = defineEmits<{
  submit: [];
}>();

const reviewFormRef = ref<FormInstance>();

const rules = reactive<FormRules>({
  score: [{ required: true, message: "请打分", trigger: "change" }],
  content: [
    { required: true, message: "请填写评价内容", trigger: "blur" },
    { min: 15, message: "评价内容不能少于15个字", trigger: "blur" },
  ],
});

async function handleSubmit(): Promise<void> {
  if (!reviewFormRef.value) return;
  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      emit("submit");
    }
  });
}
</script>

<style scoped>
.write-review-section {
  padding: 20px;
}

.submit-btn {
  width: 200px;
  font-weight: bold;
}
</style>
