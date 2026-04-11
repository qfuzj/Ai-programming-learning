<!-- 个人中心页面：展示资料、画像摘要和偏好标签。 -->
<template>
  <PageStub title="个人中心" description="可编辑基础资料，并同步偏好标签。">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="14">
        <el-card v-loading="loading" shadow="never">
          <template #header>基础资料</template>
          <el-form label-position="top">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="昵称">
                  <el-input v-model="form.nickname" placeholder="请输入昵称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="头像地址">
                  <el-input v-model="form.avatar" placeholder="请输入头像地址" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="个性签名">
              <el-input
                v-model="form.signature"
                type="textarea"
                :rows="3"
                placeholder="写点旅行宣言"
              />
            </el-form-item>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item label="性别">
                  <el-select v-model="form.gender" placeholder="请选择">
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
                    placeholder="请选择日期"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="偏好标签">
              <el-select
                v-model="selectedTagIds"
                multiple
                filterable
                collapse-tags
                collapse-tags-tooltip
                placeholder="请选择偏好标签"
              >
                <el-option
                  v-for="tag in tagOptions"
                  :key="tag.id"
                  :label="tag.name"
                  :value="tag.id"
                />
              </el-select>
            </el-form-item>
            <div class="action-row">
              <el-button :loading="loading" @click="loadProfileData">刷新</el-button>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header>用户画像</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">
              {{ profile.username || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="昵称">
              {{ profile.nickname || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              {{ profile.role || "USER" }}
            </el-descriptions-item>
            <el-descriptions-item label="旅行风格">
              {{ portrait?.travelStyle || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="预算层级">
              {{ portrait?.budgetLevel || "-" }}
            </el-descriptions-item>
            <el-descriptions-item label="画像摘要">
              {{ portrait?.summary || "-" }}
            </el-descriptions-item>
          </el-descriptions>
          <el-divider />
          <el-space wrap>
            <el-tag v-for="tag in portrait?.preferredTags || []" :key="tag" type="info">
              {{ tag }}
            </el-tag>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </PageStub>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { getTags, getTagsByType, type CommonTagItem } from "@/api/common";
import {
  getProfileInfo,
  getProfilePortrait,
  updatePreferenceTags,
  updateProfileInfo,
  type ProfileInfo,
  type ProfilePortraitSummary,
} from "@/api/profile";

const loading = ref(false);
const saving = ref(false);
const profile = reactive<ProfileInfo>({
  id: 0,
  username: "",
  nickname: "",
  role: "USER",
});
const portrait = ref<ProfilePortraitSummary | null>(null);
const tagOptions = ref<CommonTagItem[]>([]);
const selectedTagIds = ref<number[]>([]);

const form = reactive({
  nickname: "",
  avatar: "",
  signature: "",
  gender: 0 as number | undefined,
  birthday: "",
});

function syncSelectedTagIds(): void {
  if (!portrait.value?.preferredTags?.length || tagOptions.value.length === 0) {
    return;
  }

  const preferredTagNames = new Set(portrait.value.preferredTags);
  selectedTagIds.value = tagOptions.value
    .filter((tag) => preferredTagNames.has(tag.name))
    .map((tag) => tag.id);
}

async function loadTagOptions(): Promise<void> {
  try {
    const tagsByType = await getTagsByType("travel_preference");
    tagOptions.value = tagsByType.length > 0 ? tagsByType : await getTags();
  } catch {
    tagOptions.value = await getTags();
  }

  syncSelectedTagIds();
}

async function loadProfileData(): Promise<void> {
  loading.value = true;
  try {
    const [profileResult, portraitResult] = await Promise.all([
      getProfileInfo(),
      getProfilePortrait(),
    ]);
    Object.assign(profile, profileResult);
    portrait.value = portraitResult;
    form.nickname = profileResult.nickname ?? "";
    form.avatar = profileResult.avatar ?? "";
    form.signature = profileResult.signature ?? "";
    form.gender = profileResult.gender;
    form.birthday = profileResult.birthday ?? "";
    syncSelectedTagIds();
  } finally {
    loading.value = false;
  }
}

async function saveProfile(): Promise<void> {
  saving.value = true;
  try {
    await updateProfileInfo({
      nickname: form.nickname,
      avatar: form.avatar,
      signature: form.signature,
      gender: form.gender,
      birthday: form.birthday,
    });
    await updatePreferenceTags(selectedTagIds.value);
    ElMessage.success("个人中心已更新");
    await loadProfileData();
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  await Promise.all([loadTagOptions(), loadProfileData()]);
  syncSelectedTagIds();
});
</script>

<style scoped>
.action-row {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
