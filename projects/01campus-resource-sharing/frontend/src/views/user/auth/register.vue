<template>
  <div class="auth-shell">
    <div class="auth-hero">
      <div class="hero-badge">
        <n-icon><SparklesOutline /></n-icon>
        Join Campus Trade
      </div>
      <h1>现在注册，开始你的校园闲置循环</h1>
      <p>只需一分钟创建账号，即可发布物品、沟通交易、追踪订单。</p>
      <ul>
        <li><n-icon><RocketOutline /></n-icon> 低门槛发布，快速触达同校同学</li>
        <li><n-icon><ShieldCheckmarkOutline /></n-icon> 交易记录留痕，更安全放心</li>
        <li><n-icon><PeopleOutline /></n-icon> 让闲置资源在校园内高效流转</li>
      </ul>
    </div>

    <div class="auth-card">
      <div class="title-row">
        <h2>创建账号</h2>
        <span>填写基础信息完成注册</span>
      </div>

      <n-form ref="formRef" :model="registerForm" :rules="rules" @submit.prevent="handleRegister">
        <n-form-item label="用户名" path="username">
          <n-input
            v-model:value="registerForm.username"
            placeholder="4-20个字符"
            clearable
          >
            <template #prefix>
              <n-icon><PersonOutline /></n-icon>
            </template>
          </n-input>
        </n-form-item>

        <n-form-item label="密码" path="password">
          <n-input
            v-model:value="registerForm.password"
            type="password"
            placeholder="6-32个字符"
            show-password-on="mousedown"
          >
            <template #prefix>
              <n-icon><LockClosedOutline /></n-icon>
            </template>
          </n-input>
        </n-form-item>

        <n-form-item label="昵称" path="nickname">
          <n-input
            v-model:value="registerForm.nickname"
            placeholder="请输入昵称"
            clearable
          >
            <template #prefix>
              <n-icon><HappyOutline /></n-icon>
            </template>
          </n-input>
        </n-form-item>

        <n-grid :cols="2" :x-gap="12">
          <n-form-item-gi label="手机号" path="phone">
            <n-input
              v-model:value="registerForm.phone"
              placeholder="11位手机号"
              clearable
            >
              <template #prefix>
                <n-icon><CallOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item-gi>

          <n-form-item-gi label="邮箱" path="email">
            <n-input
              v-model:value="registerForm.email"
              type="email"
              placeholder="邮箱地址"
              clearable
            >
              <template #prefix>
                <n-icon><MailOutline /></n-icon>
              </template>
            </n-input>
          </n-form-item-gi>
        </n-grid>

        <div class="form-actions">
          <n-button
            type="primary"
            :loading="loading"
            @click="handleRegister"
            style="width: 100%"
          >注 册</n-button>
        </div>
      </n-form>

      <div class="auth-links">
        <span>已有账号？</span>
        <n-button text tag="a" @click="goToLogin">
          立即登录
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
import { NButton, NForm, NFormItem, NFormItemGi, NGrid, NIcon, NInput, useMessage } from 'naive-ui'
import {
  ArrowForwardOutline,
  CallOutline,
  HappyOutline,
  LockClosedOutline,
  MailOutline,
  PeopleOutline,
  PersonOutline,
  RocketOutline,
  ShieldCheckmarkOutline,
  SparklesOutline
} from '@vicons/ionicons5'
import { register } from '@/api/auth'

const router = useRouter()
const message = useMessage()

const formRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: ''
})

const rules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'blur',
    min: 4,
    max: 20
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: 'blur',
    min: 6,
    max: 32
  },
  nickname: {
    required: true,
    message: '请输入昵称',
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

const handleRegister = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    await register({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname,
      phone: registerForm.phone || undefined,
      email: registerForm.email || undefined
    })
    
    message.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    message.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.auth-shell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 28px;
  min-height: 100vh;
  padding: 24px;
}

.auth-hero {
  width: 520px;
  min-height: 640px;
  border-radius: 22px;
  padding: 40px 36px;
  background: linear-gradient(165deg, #0f766e 0%, #0f766e 40%, #134e4a 100%);
  color: #e2e8f0;
  box-shadow: 0 20px 35px rgba(20, 83, 45, 0.28);
  animation: fade-up 0.35s ease;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
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
  color: #ccfbf1;
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
  width: 460px;
  min-height: 640px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 22px;
  padding: 34px;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.1);
  animation: fade-up 0.3s ease;
}

.title-row {
  margin-bottom: 20px;
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
  margin-top: 20px;
}

.auth-links {
  text-align: center;
  margin-top: 20px;
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

@media (max-width: 760px) {
  :deep(.n-grid) {
    grid-template-columns: 1fr !important;
  }
}
</style>

