<template>
  <div class="profile-container">
    <n-card title="个人资料" :bordered="false">
      <n-form ref="formRef" :model="profileForm" :rules="rules" label-placement="left" label-width="120px">
        <n-form-item label="用户ID">
          <n-input v-model:value="profileForm.id" readonly />
        </n-form-item>

        <n-form-item label="用户名">
          <n-input v-model:value="profileForm.username" readonly />
        </n-form-item>

        <n-form-item label="昵称" path="nickname">
          <n-input 
            v-model:value="profileForm.nickname" 
            placeholder="请输入昵称"
          />
        </n-form-item>

        <n-form-item label="头像">
          <div class="avatar-section">
            <n-avatar
              :src="avatarSrc"
              :size="80"
              round
            />
            <n-button text type="primary" @click="handleUploadClick" style="margin-left: 20px;">
              上传头像
            </n-button>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              style="display: none;"
              @change="handleAvatarUpload"
            />
          </div>
        </n-form-item>

        <n-form-item label="手机号" path="phone">
          <n-input 
            v-model:value="profileForm.phone" 
            placeholder="11位手机号"
          />
        </n-form-item>

        <n-form-item label="邮箱" path="email">
          <n-input 
            v-model:value="profileForm.email" 
            type="email"
            placeholder="邮箱地址"
          />
        </n-form-item>

        <n-form-item label="性别" path="gender">
          <n-select
            v-model:value="profileForm.gender"
            :options="genderOptions"
            clearable
          />
        </n-form-item>

        <n-form-item label="学院" path="college">
          <n-input 
            v-model:value="profileForm.college" 
            placeholder="所在学院"
          />
        </n-form-item>

        <n-form-item label="账户角色">
          <n-input 
            v-model:value="profileRoleText" 
            readonly
          />
        </n-form-item>

        <n-form-item label="账户状态">
          <n-input 
            v-model:value="profileStatusText" 
            readonly
          />
        </n-form-item>

        <div class="form-actions">
          <n-button type="primary" @click="handleSave" :loading="saving">
            保存修改
          </n-button>
          <n-button @click="handleReset" style="margin-left: 10px;">
            重置
          </n-button>
        </div>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { NCard, NForm, NFormItem, NInput, NButton, NAvatar, NSelect, useMessage } from 'naive-ui'
import { getProfile, updateProfile, uploadAvatar } from '@/api/user'
import { resolveFileUrl } from '@/utils/file'

const message = useMessage()
const formRef = ref(null)
const fileInput = ref(null)
const saving = ref(false)

const profileForm = reactive({
  id: 0,
  username: '',
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  gender: null,
  college: '',
  role: '',
  status: 1
})

const originalForm = {}

const genderOptions = [
  { label: '未知', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

const rules = {
  nickname: {
    required: true,
    message: '昵称不能为空',
    trigger: 'blur'
  },
  phone: {
    validator(rule, value) {
      if (!value) return true
      if (!/^1[3-9]\d{9}$/.test(value)) {
        return new Error('请输入有效的11位手机号')
      }
      return true
    },
    trigger: 'blur'
  },
  email: {
    validator(rule, value) {
      if (!value) return true
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        return new Error('请输入有效的邮箱地址')
      }
      return true
    },
    trigger: 'blur'
  }
}

const profileRoleText = computed(() => {
  return profileForm.role === 'admin' ? '管理员' : '普通用户'
})

const profileStatusText = computed(() => {
  return profileForm.status === 1 ? '正常' : '禁用'
})

const avatarSrc = computed(() => resolveFileUrl(profileForm.avatar))

const loadProfile = async () => {
  try {
    const data = await getProfile()
    Object.assign(profileForm, data)
    Object.assign(originalForm, profileForm)
  } catch (error) {
    message.error('加载个人资料失败')
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    await updateProfile({
      nickname: profileForm.nickname,
      phone: profileForm.phone || undefined,
      email: profileForm.email || undefined,
      gender: profileForm.gender,
      college: profileForm.college || undefined
    })
    
    message.success('修改成功')
    await loadProfile()
  } catch (error) {
    message.error(error.message || '修改失败')
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  Object.assign(profileForm, originalForm)
}

const handleUploadClick = () => {
  fileInput.value?.click()
}

const handleAvatarUpload = async (event) => {
  try {
    const file = event.target.files?.[0]
    if (!file) return
    
    if (file.size > 5 * 1024 * 1024) {
      message.error('文件大小不能超过5MB')
      return
    }
    
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await uploadAvatar(formData)
    profileForm.avatar = response.avatarUrl
    message.success('头像上传成功')
  } catch (error) {
    message.error(error.message || '头像上传失败')
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.avatar-section {
  display: flex;
  align-items: center;
}

.form-actions {
  margin-top: 20px;
  display: flex;
}

:deep(.n-card) {
  background: white;
}
</style>

