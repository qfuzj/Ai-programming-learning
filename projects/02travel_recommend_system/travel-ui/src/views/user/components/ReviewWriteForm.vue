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
          placeholder="分享您的游玩体验，为其他旅行者提供参考（至少5个字）"
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

      <el-form-item label="上传图片">
        <div class="review-image-uploader">
          <div
            v-for="img in uploadedImages"
            :key="img.id"
            class="review-image-card"
          >
            <img :src="img.url" class="review-img" />
            <div class="review-image-mask" @click="removeImage(img.id)">
              <el-icon class="delete-icon"><Close /></el-icon>
            </div>
          </div>
          <div
            v-if="uploadedImages.length < 6"
            class="review-upload-btn"
            @click="triggerImageUpload"
          >
            <el-icon v-if="!uploadLoading" class="upload-icon">
              <Plus />
            </el-icon>
            <el-icon v-else class="upload-icon is-loading">
              <Loading />
            </el-icon>
            <div class="upload-text">
              {{ uploadLoading ? "上传中..." : "点击上传" }}
            </div>
          </div>
        </div>
        <input
          ref="imageInputRef"
          type="file"
          accept="image/*"
          style="display: none"
          @change="onImageSelected"
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
import { ref, reactive, watch } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import type { MyReviewForm } from "@/types/my-reviews";
import { Plus, Loading, Close } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { getUploadToken, uploadCallback, getFileResource } from "@/api/file";

interface Props {
  form: MyReviewForm;
  loading: boolean;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  submit: [];
}>();

const reviewFormRef = ref<FormInstance>();

interface UploadedImage {
  id: number;
  url: string;
}

const uploadedImages = ref<UploadedImage[]>([]);
const imageInputRef = ref<HTMLInputElement>();
const uploadLoading = ref(false);

watch(
  () => props.form.imageIds,
  (ids) => {
    if (!ids || ids.length === 0) {
      uploadedImages.value = [];
    }
  },
  { immediate: true }
);

function triggerImageUpload(): void {
  if (uploadLoading.value) return;
  imageInputRef.value?.click();
}

async function onImageSelected(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  if (!file.type.startsWith("image/")) {
    ElMessage.warning("请选择图片文件");
    input.value = "";
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning("图片大小不能超过 5MB");
    input.value = "";
    return;
  }

  uploadLoading.value = true;
  try {
    const tokenRes = await getUploadToken({
      bizType: "review",
      fileName: file.name,
      fileSize: file.size,
    });
    const uploadUrl = String(tokenRes.uploadUrl || "");
    const bucketName = String(tokenRes.bucketName || "");
    const objectKey = String(tokenRes.objectKey || "");
    if (!uploadUrl || !bucketName || !objectKey) {
      throw new Error("上传凭证不完整");
    }

    const uploadResponse = await fetch(uploadUrl, {
      method: "PUT",
      body: file,
      headers: { "Content-Type": file.type || "application/octet-stream" },
    });
    if (!uploadResponse.ok) throw new Error("文件上传失败");

    const fileId = await uploadCallback({
      bucketName,
      objectKey,
      originalName: file.name,
      bizType: "review",
    });
    const fileResource = await getFileResource(fileId);
    uploadedImages.value.push({
      id: fileId,
      url: fileResource.url || "",
    });
    props.form.imageIds.push(fileId);
    ElMessage.success("图片上传成功");
  } catch {
    ElMessage.error("图片上传失败，请重试");
  } finally {
    uploadLoading.value = false;
    input.value = "";
  }
}

function removeImage(id: number): void {
  uploadedImages.value = uploadedImages.value.filter((img) => img.id !== id);
  props.form.imageIds = props.form.imageIds.filter((imgId: number) => imgId !== id);
}

const rules = reactive<FormRules>({
  score: [{ required: true, message: "请打分", trigger: "change" }],
  content: [
    { required: true, message: "请填写评价内容", trigger: "blur" },
    { min: 5, message: "评价内容不能少于5个字", trigger: "blur" },
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

.review-image-uploader {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.review-image-card {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  cursor: pointer;
  flex-shrink: 0;
}

.review-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.review-image-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.review-image-card:hover .review-image-mask {
  opacity: 1;
}

.delete-icon {
  color: #fff;
  font-size: 18px;
}

.review-upload-btn {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  border: 1px dashed #dcdfe6;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s;
  background: #fafbfc;
  gap: 4px;
  flex-shrink: 0;
}

.review-upload-btn:hover {
  border-color: #409eff;
}

.upload-icon {
  font-size: 20px;
  color: #909399;
}

.upload-text {
  font-size: 12px;
  color: #909399;
}
</style>
