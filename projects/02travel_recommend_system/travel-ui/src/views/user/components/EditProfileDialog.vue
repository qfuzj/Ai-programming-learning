<template>
  <el-dialog v-model="visible" title="编辑个人资料" width="500px" destroy-on-close>
    <el-form :model="form" label-position="top">
      <el-divider content-position="left">基本信息</el-divider>

      <el-form-item label="昵称">
        <el-input v-model="form.nickname" maxlength="20" show-word-limit />
      </el-form-item>

      <el-form-item label="头像">
        <div class="avatar-upload-wrapper" @click="triggerUpload">
          <el-avatar :size="80" :src="form.avatar" class="upload-avatar">
            {{ (form.nickname || fallbackName || "?").charAt(0) }}
          </el-avatar>
          <div class="avatar-upload-mask">
            <el-icon><Camera /></el-icon>
            <span>更换头像</span>
          </div>
          <el-progress
            v-if="uploading"
            type="circle"
            :percentage="uploadProgress"
            :width="80"
            class="avatar-upload-progress"
          />
        </div>
        <input
          ref="fileInputRef"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleFileChange"
        />
      </el-form-item>

      <el-divider content-position="left">个人简介</el-divider>

      <el-form-item label="个性签名">
        <el-input
          v-model="form.signature"
          type="textarea"
          :rows="3"
          placeholder="分享你的旅行态度，例如：走走停停，看遍山河"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-divider content-position="left">个人信息</el-divider>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="性别">
            <el-select v-model="form.gender" style="width: 100%">
              <el-option label="保密" :value="0" />
              <el-option label="男" :value="1" />
              <el-option label="女" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="生日">
            <el-date-picker
              v-model="form.birthday"
              type="date"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button text @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" class="btn-primary" @click="handleSubmit">
          保存更改
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { Camera } from "@element-plus/icons-vue";
import { getUploadToken, uploadCallback, getFileResource } from "@/api/file";
import type { ProfileInfo, UpdateProfilePayload } from "@/types/profile";

interface Props {
  modelValue: boolean;
  profile: ProfileInfo;
  saving?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  "update:modelValue": [value: boolean];
  submit: [payload: UpdateProfilePayload];
}>();

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val),
});

const fallbackName = computed(() => props.profile.username);

const form = reactive<UpdateProfilePayload>({
  nickname: "",
  avatar: "",
  signature: "",
  gender: 0,
  birthday: "",
});

watch(
  () => props.modelValue,
  (isOpen) => {
    if (isOpen) {
      form.nickname = props.profile.nickname || "";
      form.avatar = props.profile.avatar || "";
      form.signature = props.profile.signature || "";
      form.gender = props.profile.gender || 0;
      form.birthday = props.profile.birthday || "";
    }
  }
);

const fileInputRef = ref<HTMLInputElement>();
const uploading = ref(false);
const uploadProgress = ref(0);

function triggerUpload(): void {
  fileInputRef.value?.click();
}

async function handleFileChange(event: Event): Promise<void> {
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

  uploading.value = true;
  uploadProgress.value = 0;

  try {
    const tokenRes = await getUploadToken({
      bizType: "avatar",
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
    uploadProgress.value = 80;

    const fileId = await uploadCallback({
      bucketName,
      objectKey,
      originalName: file.name,
      bizType: "avatar",
    });
    uploadProgress.value = 90;

    const fileResource = await getFileResource(fileId);
    form.avatar = fileResource.url || "";
    uploadProgress.value = 100;
    ElMessage.success("头像上传成功");
  } catch (error) {
    ElMessage.error("头像上传失败，请重试");
  } finally {
    uploading.value = false;
    input.value = "";
  }
}

function handleSubmit(): void {
  emit("submit", { ...form });
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
}

.btn-primary {
  background-color: #34e0a1;
  border-color: #34e0a1;
}

.upload-avatar {
  background-color: #34e0a1;
  color: #fff;
}

.avatar-upload-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-upload-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  color: #fff;
  font-size: 12px;
  gap: 4px;
}

.avatar-upload-wrapper:hover .avatar-upload-mask {
  opacity: 1;
}

.avatar-upload-progress {
  position: absolute;
  inset: 0;
}
</style>
