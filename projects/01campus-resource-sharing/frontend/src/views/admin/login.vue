<template>
  <div class="admin-auth-shell">
    <div class="admin-auth-hero">
      <div class="hero-badge">
        <n-icon><ShieldCheckmarkOutline /></n-icon>
        Campus Admin Console
      </div>
      <h1>校园资源管理后台</h1>
      <p>集中管理用户、商品、订单与风控审核，实时掌握平台运行状态。</p>
      <ul>
        <li><n-icon><AnalyticsOutline /></n-icon> 仪表盘统计实时更新</li>
        <li><n-icon><ShieldOutline /></n-icon> 举报审核与商品下架闭环</li>
        <li><n-icon><PeopleOutline /></n-icon> 用户状态快速禁用/启用</li>
      </ul>
    </div>

    <div class="admin-auth-box">
      <h2>管理员登录</h2>

      <n-form ref="formRef" :model="loginForm" :rules="rules" @submit.prevent="handleLogin">
        <n-form-item label="用户名" path="username">
          <n-input
            v-model:value="loginForm.username"
            placeholder="请输入管理员账号"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <n-icon><PersonOutline /></n-icon>
            </template>
          </n-input>
        </n-form-item>

        <n-form-item label="密码" path="password">
          <n-input
            v-model:value="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password-on="mousedown"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <n-icon><LockClosedOutline /></n-icon>
            </template>
          </n-input>
        </n-form-item>

        <div class="form-actions">
          <n-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">登 录</n-button>
        </div>
      </n-form>

      <div class="auth-links">
        <n-button text @click="goUserLogin">
          返回用户登录
          <template #icon>
            <n-icon><ArrowBackOutline /></n-icon>
          </template>
        </n-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NForm, NFormItem, NIcon, NInput, useMessage } from 'naive-ui'
import {
  AnalyticsOutline,
  ArrowBackOutline,
  LockClosedOutline,
  PeopleOutline,
  PersonOutline,
  ShieldCheckmarkOutline,
  ShieldOutline
} from '@vicons/ionicons5'
import { adminLogin } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

const handleLogin = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    const res = await adminLogin({
      username: loginForm.username,
      password: loginForm.password
    })

    userStore.setLogin(res.token, res.userInfo)

    const role = (userStore.role || '').toString().trim().toLowerCase()
    if (role !== 'admin') {
      userStore.logout()
      message.error('当前账号不是管理员，无法进入后台')
      return
    }

    message.success('登录成功')
    router.push('/admin/dashboard')
  } catch (error) {
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const goUserLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.admin-auth-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 28px;
  padding: 24px;
}

.admin-auth-hero {
  width: 500px;
  min-height: 520px;
  border-radius: 20px;
  padding: 36px;
  color: #e2e8f0;
  background: linear-gradient(160deg, #0f172a 0%, #1e293b 45%, #1d4ed8 100%);
  box-shadow: 0 20px 34px rgba(15, 23, 42, 0.35);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 13px;
}

.admin-auth-hero h1 {
  margin: 24px 0 10px;
  font-size: 30px;
  color: #f8fafc;
}

.admin-auth-hero p {
  margin: 0;
  line-height: 1.75;
  color: #cbd5e1;
}

.admin-auth-hero ul {
  margin: 24px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 12px;
}

.admin-auth-hero li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.admin-auth-box {
  width: 430px;
  min-height: 520px;
  background: #fff;
  border: 1px solid #dbe3f0;
  border-radius: 20px;
  padding: 36px;
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.14);
}

h2 {
  margin: 4px 0 24px;
  font-size: 24px;
  color: #0f172a;
}

.form-actions {
  margin-top: 18px;
}

.auth-links {
  margin-top: 16px;
  text-align: center;
}

@media (max-width: 1080px) {
  .admin-auth-shell {
    flex-direction: column;
    padding: 18px;
  }

  .admin-auth-hero,
  .admin-auth-box {
    width: 100%;
    max-width: 760px;
    min-height: auto;
  }
}
</style>
