<template>
  <div class="auth-shell">
    <div class="auth-hero">
      <div class="hero-badge">
        <n-icon><SchoolOutline /></n-icon>
        校园闲置物资共享平台
      </div>
      <h1>让每一件闲置物资，找到下一个需要它的人</h1>
      <p>安全认证、同校交易、流程透明，打造可信赖的校园二手交换空间。</p>
      <ul>
        <li><n-icon><ShieldCheckmarkOutline /></n-icon> 实名账号体系，降低交易风险</li>
        <li><n-icon><SwapHorizontalOutline /></n-icon> 全流程订单状态可追踪</li>
        <li><n-icon><ChatbubblesOutline /></n-icon> 买卖双方可直接留言沟通</li>
      </ul>
    </div>

    <div class="auth-card">
      <div class="title-row">
        <h2>账号登录</h2>
        <span>欢迎回来</span>
      </div>

      <n-form ref="formRef" :model="loginForm" :rules="rules" @submit.prevent="handleLogin">
        <n-form-item label="用户名" path="username">
          <n-input
            v-model:value="loginForm.username"
            placeholder="请输入用户名"
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
          <n-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >登 录</n-button>
        </div>
      </n-form>

      <div class="auth-links">
        <span>没有账号？</span>
        <n-button text tag="a" @click="goToRegister">
          立即注册
          <template #icon>
            <n-icon><ArrowForwardOutline /></n-icon>
          </template>
        </n-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { NButton, NForm, NFormItem, NIcon, NInput, useMessage } from 'naive-ui'
import {
  ArrowForwardOutline,
  ChatbubblesOutline,
  LockClosedOutline,
  PersonOutline,
  SchoolOutline,
  ShieldCheckmarkOutline,
  SwapHorizontalOutline
} from '@vicons/ionicons5'
import { login } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'blur'
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: 'blur'
  }
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const res = await login({
      username: loginForm.username,
      password: loginForm.password
    })
    
    // 与路由守卫使用同一 store，避免登录状态不一致
    userStore.setLogin(res.token, res.userInfo)
    
    message.success('登录成功')
    const role = (userStore.role || '').toString().trim().toLowerCase()
    if (role === 'admin') {
      router.push('/admin/dashboard')
    } else {
      router.push('/')
    }
  } catch (error) {
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.auth-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 28px;
  padding: 24px;
}

.auth-hero {
  width: 520px;
  min-height: 540px;
  border-radius: 22px;
  padding: 40px 36px;
  background: linear-gradient(155deg, #1d4ed8 0%, #1e3a8a 55%, #172554 100%);
  color: #e2e8f0;
  box-shadow: 0 20px 35px rgba(30, 58, 138, 0.35);
  animation: fade-up 0.35s ease;
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

.auth-hero h1 {
  margin: 26px 0 10px;
  font-size: 32px;
  line-height: 1.3;
  color: #f8fafc;
}

.auth-hero p {
  margin: 0;
  color: #cbd5e1;
  line-height: 1.75;
}

.auth-hero ul {
  margin: 26px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 12px;
}

.auth-hero li {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
}

.auth-card {
  width: 430px;
  min-height: 540px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 22px;
  padding: 34px;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.1);
  animation: fade-up 0.3s ease;
}

.title-row {
  margin-bottom: 22px;
}

.title-row h2 {
  margin: 0;
  font-size: 26px;
  color: #0f172a;
}

.title-row span {
  display: inline-block;
  margin-top: 6px;
  color: #64748b;
  font-size: 14px;
}

.form-actions {
  margin-top: 24px;
}

.auth-links {
  text-align: center;
  margin-top: 18px;
  color: #666;
  font-size: 14px;
}

.auth-links span {
  margin-right: 8px;
}

.n-button {
  padding: 0 !important;
  font-size: 14px !important;
}

@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1080px) {
  .auth-shell {
    flex-direction: column;
    padding: 18px;
  }

  .auth-hero,
  .auth-card {
    width: 100%;
    max-width: 760px;
    min-height: auto;
  }

  .auth-hero h1 {
    font-size: 26px;
  }
}
</style>

