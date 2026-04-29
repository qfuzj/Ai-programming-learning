<!-- 极简风格用户登录页 -->
<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <div class="brand">智游</div>
        <h1 class="title">登录</h1>
        <p class="subtitle">登录您的账户以继续使用</p>
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
          <label class="form-label">密码</label>
          <input
            v-model="form.password"
            type="password"
            class="form-input"
            placeholder="请输入密码"
            autocomplete="current-password"
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
          {{ submitting ? "登录中..." : "登录" }}
        </button>
      </form>

      <div class="auth-footer">
        <span class="link" @click="goRegister">没有账号？去注册</span>
        <span class="divider">|</span>
        <span class="link" @click="goResetPassword">忘记密码</span>
      </div>

      <div v-if="route.query.registered" class="success-msg">
        注册成功，欢迎 {{ route.query.username }}，请登录
      </div>
      <div v-if="route.query.reset === '1'" class="success-msg">密码重置成功，请使用新密码登录</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/store";
import { getCaptcha } from "@/api/auth";
import { ROUTE_PATHS } from "@/router/constants";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const submitting = ref(false);
const captchaImage = ref("");

const form = reactive({
  username: "",
  password: "",
  captchaId: "",
  captchaCode: "",
});

async function loadCaptcha(): Promise<void> {
  const res = await getCaptcha("login");
  form.captchaId = res.captchaId;
  captchaImage.value = res.captchaBase64;
  form.captchaCode = "";
}

async function onSubmit(): Promise<void> {
  if (!form.captchaId || !form.captchaCode) {
    alert("请先输入验证码");
    return;
  }

  submitting.value = true;
  try {
    await userStore.loginAsUser({ ...form, loginType: "username" as const });
    const redirect =
      typeof route.query.redirect === "string" ? route.query.redirect : ROUTE_PATHS.USER_HOME;
    await router.push(redirect);
  } catch {
    await loadCaptcha();
  } finally {
    submitting.value = false;
  }
}

function goRegister(): void {
  router.push("/register");
}

function goResetPassword(): void {
  router.push("/reset-password");
}

onMounted(() => {
  const prefillUsername = sessionStorage.getItem("auth_prefill_username");
  if (prefillUsername) {
    form.username = prefillUsername;
    sessionStorage.removeItem("auth_prefill_username");
  }

  nextTick(() => {
    if (route.query.registered === "1") {
      const username = typeof route.query.username === "string" ? route.query.username : "";
      alert(username ? `注册成功，欢迎 ${username}，请登录` : "注册成功，请登录");
    }
  });

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
  gap: 16px;
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

.divider {
  color: #e0e0e0;
}

.success-msg {
  padding: 12px 16px;
  margin-top: 16px;
  font-size: 14px;
  color: #000000;
  text-align: center;
  background: #e8f5e9;
  border-radius: 8px;
}
</style>
