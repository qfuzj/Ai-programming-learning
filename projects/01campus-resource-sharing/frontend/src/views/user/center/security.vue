<template>
  <div class="security-container">
    <n-card title="账号安全" :bordered="false">
      <n-tabs type="card">
        <n-tab-pane name="password" tab="修改密码">
          <div class="password-form">
            <n-form ref="formRef" :model="passwordForm" :rules="rules" label-placement="left" label-width="120px">
              <n-form-item label="当前密码" path="oldPassword">
                <n-input 
                  v-model:value="passwordForm.oldPassword" 
                  type="password"
                  placeholder="请输入当前密码"
                  show-password-on="mousedown"
                />
              </n-form-item>

              <n-form-item label="新密码" path="newPassword">
                <n-input 
                  v-model:value="passwordForm.newPassword" 
                  type="password"
                  placeholder="6-32个字符"
                  show-password-on="mousedown"
                />
              </n-form-item>

              <n-form-item label="确认密码" path="confirmPassword">
                <n-input 
                  v-model:value="passwordForm.confirmPassword" 
                  type="password"
                  placeholder="再次输入新密码"
                  show-password-on="mousedown"
                />
              </n-form-item>

              <div class="form-actions">
                <n-button type="primary" @click="handleChangePassword" :loading="saving">
                  确认修改
                </n-button>
                <n-button @click="handleResetPasswordForm" style="margin-left: 10px;">
                  重置
                </n-button>
              </div>
            </n-form>

            <n-alert type="info" style="margin-top: 20px;">
              <template #icon>
                <span>💡</span>
              </template>
              密码修改后，请重新登录。为了用户安全，请定期修改密码。
            </n-alert>
          </div>
        </n-tab-pane>

        <n-tab-pane name="security" tab="安全设置">
          <div class="security-settings">
            <n-list>
              <n-list-item>
                <template #prefix>
                  <span>🔐</span>
                </template>
                <n-thing title="账号绑定">
                  <template #description>
                    <p>已绑定手机号和邮箱，可用于账号恢复和找回</p>
                  </template>
                </n-thing>
              </n-list-item>

              <n-list-item>
                <template #prefix>
                  <span>🔔</span>
                </template>
                <n-thing title="登录提醒">
                  <template #description>
                    <p>已启用，账号异常登录时会发送提醒通知</p>
                  </template>
                </n-thing>
              </n-list-item>

              <n-list-item>
                <template #prefix>
                  <span>⏱️</span>
                </template>
                <n-thing title="会话管理">
                  <template #description>
                    <p>当前登录的设备：浏览器 | 最后登录时间：昨天 14:30</p>
                  </template>
                </n-thing>
              </n-list-item>
            </n-list>

            <n-divider />

            <n-alert type="warning" style="margin-top: 20px;">
              <template #icon>
                <span>⚠️</span>
              </template>
              <p>危险操作区域 - 请谨慎操作</p>
            </n-alert>

            <div class="danger-zone" style="margin-top: 20px;">
              <n-button type="error" @click="handleLogoutAll">
                登出所有设备
              </n-button>
              <n-button type="error" @click="handleDeleteAccount" style="margin-left: 10px;">
                注销账号
              </n-button>
            </div>
          </div>
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { NCard, NTabs, NTabPane, NForm, NFormItem, NInput, NButton, NAlert, NList, NListItem, NThing, NDivider, useDialog, useMessage } from 'naive-ui'
import { changePassword } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const dialog = useDialog()

const formRef = ref(null)
const saving = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  oldPassword: {
    required: true,
    message: '请输入当前密码',
    trigger: 'blur'
  },
  newPassword: {
    required: true,
    message: '请输入新密码',
    trigger: 'blur',
    min: 6,
    max: 32
  },
  confirmPassword: {
    required: true,
    message: '请再次输入新密码',
    trigger: 'blur',
    validator(rule, value) {
      if (value !== passwordForm.newPassword) {
        return new Error('两次输入的密码不相同')
      }
      return true
    }
  }
}

const handleChangePassword = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    message.success('密码修改成功，请重新登录')
    
    // 清空存储的认证信息
    userStore.logout()
    
    // 跳转到登录页
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    message.error(error.message || '密码修改失败')
  } finally {
    saving.value = false
  }
}

const handleResetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const handleLogoutAll = () => {
  dialog.warning({
    title: '确认操作',
    content: '确定要登出所有设备吗？这会在所有地方都退出登录。',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: () => {
      message.info('已登出所有设备，请重新登录')
      userStore.logout()
      router.push('/login')
    }
  })
}

const handleDeleteAccount = () => {
  dialog.error({
    title: '确认注销',
    content: '注销账号后，您的所有数据将被永久删除，此操作无法撤销。请谨慎操作！',
    positiveText: '确定注销',
    negativeText: '取消',
    onPositiveClick: () => {
      message.warning('账号注销功能将在后续版本中实现')
    }
  })
}
</script>

<style scoped>
.security-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.password-form {
  max-width: 500px;
}

.form-actions {
  margin-top: 20px;
  display: flex;
}

.security-settings {
  max-width: 600px;
}

.danger-zone {
  display: flex;
  gap: 10px;
}

:deep(.n-card) {
  background: white;
}

:deep(.n-list) {
  background: transparent;
}

:deep(.n-list-item) {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.n-list-item:last-child) {
  border-bottom: none;
}
</style>

