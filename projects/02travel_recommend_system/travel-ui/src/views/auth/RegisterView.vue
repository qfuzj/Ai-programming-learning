<!-- 极简风格用户注册页 -->
<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <div class="brand">智游</div>
        <h1 class="title">注册</h1>
        <p class="subtitle">创建您的账户</p>
      </div>

      <form @submit.prevent="onSubmit">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input
            v-model="form.username"
            type="text"
            class="form-input"
            placeholder="请输入用户名"
            autocomplete="username"
          />
        </div>

        <div class="form-group">
          <label class="form-label">手机号</label>
          <input
            v-model="form.phone"
            type="tel"
            class="form-input"
            placeholder="请输入手机号"
            autocomplete="tel"
          />
        </div>

        <div class="form-group">
          <label class="form-label">密码</label>
          <input
            v-model="form.password"
            type="password"
            class="form-input"
            placeholder="请输入密码（6-20位）"
            autocomplete="new-password"
          />
        </div>

        <div class="form-group">
          <label class="form-label">确认密码</label>
          <input
            v-model="form.confirmPassword"
            type="password"
            class="form-input"
            placeholder="请再次输入密码"
            autocomplete="new-password"
          />
        </div>

        <div class="form-group">
          <label class="form-label">验证码</label>
          <div class="captcha-row">
            <input
              v-model="form.captchaCode"
              type="text"
              class="form-input"
              placeholder="验证码"
              maxlength="6"
            />
            <img :src="captchaImage" class="captcha-img" alt="验证码" @click="loadCaptcha" />
          </div>
        </div>

        <button type="submit" class="submit-btn" :disabled="submitting">
          {{ submitting ? "注册中..." : "注册" }}
        </button>
      </form>

      <div class="auth-footer">
        <span class="link" @click="goLogin">已有账号，去登录</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { getCaptcha, registerUser } from "@/api/auth";
import { ROUTE_PATHS } from "@/router/constants";
import type { RegisterPayload } from "@/types/auth";

const router = useRouter();
const submitting = ref(false);
const captchaImage = ref("");

const form = reactive<RegisterPayload>({
  username: "",
  phone: "",
  password: "",
  confirmPassword: "",
  captchaId: "",
  captchaCode: "",
});

async function loadCaptcha(): Promise<void> {
  const res = await getCaptcha("register");
  form.captchaId = res.captchaId;
  captchaImage.value = res.captchaBase64;
  form.captchaCode = "";
}

async function onSubmit(): Promise<void> {
  if (!form.username.trim()) {
    alert("请输入用户名");
    return;
  }
  if (!/^1\d{10}$/.test(form.phone.trim())) {
    alert("请输入正确的手机号");
    return;
  }
  if (form.password.length < 6 || form.password.length > 20) {
    alert("密码长度为6-20位");
    return;
  }
  if (form.password !== form.confirmPassword) {
    alert("两次输入的密码不一致");
    return;
  }
  if (!form.captchaId || !form.captchaCode) {
    alert("请先输入验证码");
    return;
  }

  submitting.value = true;
  try {
    const payload: RegisterPayload = {
      username: form.username.trim(),
      phone: form.phone.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword,
      captchaId: form.captchaId,
      captchaCode: form.captchaCode.trim(),
    };
    await registerUser(payload);
    sessionStorage.setItem("auth_prefill_username", payload.username);
    sessionStorage.setItem("auth_onboarding_pending", "1");
    await router.push({
      path: ROUTE_PATHS.USER_LOGIN,
      query: { registered: "1", username: payload.username },
    });
  } catch {
    await loadCaptcha();
  } finally {
    submitting.value = false;
  }
}

function goLogin(): void {
  router.push(ROUTE_PATHS.USER_LOGIN);
}

onMounted(() => {
  loadCaptcha();
});
</script>

<style scoped>
.auth-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
  background: #ffffff;
}

.auth-container {
  width: 100%;
  max-width: 400px;
  padding: 40px 0;
}

.brand {
  margin-bottom: 32px;
  font-size: 28px;
  font-weight: 800;
  color: #00e676;
  text-align: center;
  letter-spacing: -0.5px;
}

.title {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 700;
  color: #000000;
}

.subtitle {
  margin: 0 0 40px 0;
  font-size: 15px;
  color: #999999;
}

.form-group {
  margin-bottom: 24px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #000000;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  font-size: 15px;
  color: #000000;
  outline: none;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: border-color 0.2s;
}

.form-input:focus {
  border-color: #00e676;
}

.form-input::placeholder {
  color: #999999;
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-row .form-input {
  flex: 1;
}

.captcha-img {
  width: 120px;
  height: 44px;
  cursor: pointer;
  object-fit: cover;
  border-radius: 8px;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  margin-top: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #000000;
  cursor: pointer;
  background: #00e676;
  border: none;
  border-radius: 8px;
  transition: background 0.2s;
}

.submit-btn:hover {
  background: #00c665;
}

.submit-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.auth-footer {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  font-size: 14px;
}

.link {
  font-weight: 500;
  color: #000000;
  cursor: pointer;
}

.link:hover {
  color: #00c665;
}
</style>
