<template>
  <div class="auth-page">
    <div class="auth-panel-left">
      <div class="panel-content">
        <div class="panel-brand">智游</div>
        <h2 class="panel-title">开启你的旅行之旅</h2>
        <p class="panel-desc">注册账户，获取个性化旅行推荐，发现更多精彩目的地</p>
        <div class="panel-decoration">
          <div class="deco-circle deco-circle-1"></div>
          <div class="deco-circle deco-circle-2"></div>
          <div class="deco-circle deco-circle-3"></div>
        </div>
      </div>
    </div>

    <div class="auth-panel-right">
      <div class="auth-container">
        <div class="auth-header">
          <h1 class="title">创建账户</h1>
          <p class="subtitle">填写以下信息完成注册</p>
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
  display: flex;
  min-height: 100vh;
}

.auth-panel-left {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 45%;
  padding: 60px;
  background: linear-gradient(135deg, #0f7b4a 0%, #00e676 50%, #69f0ae 100%);
  position: relative;
  overflow: hidden;
}

.panel-content {
  position: relative;
  z-index: 1;
  max-width: 380px;
}

.panel-brand {
  font-size: 36px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: -0.5px;
  margin-bottom: 24px;
}

.panel-title {
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 16px 0;
  line-height: 1.3;
}

.panel-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.6;
  margin: 0;
}

.panel-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.deco-circle-1 {
  width: 300px;
  height: 300px;
  top: -80px;
  right: -60px;
}

.deco-circle-2 {
  width: 200px;
  height: 200px;
  bottom: -40px;
  left: -40px;
}

.deco-circle-3 {
  width: 120px;
  height: 120px;
  bottom: 30%;
  right: 10%;
}

.auth-panel-right {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 40px;
  background: #fafafa;
}

.auth-container {
  width: 100%;
  max-width: 380px;
  padding: 40px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.title {
  margin: 0 0 8px 0;
  font-size: 26px;
  font-weight: 700;
  color: #1a1a1a;
}

.subtitle {
  margin: 0 0 32px 0;
  font-size: 14px;
  color: #888888;
}

.form-group {
  margin-bottom: 18px;
}

.form-label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #555555;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  font-size: 14px;
  color: #1a1a1a;
  outline: none;
  background: #f8f9fa;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  transition: all 0.2s;
}

.form-input:focus {
  background: #ffffff;
  border-color: #00e676;
  box-shadow: 0 0 0 3px rgba(0, 230, 118, 0.1);
}

.form-input::placeholder {
  color: #aaaaaa;
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
  width: 110px;
  height: 42px;
  cursor: pointer;
  object-fit: cover;
  border-radius: 10px;
  border: 1px solid #e8e8e8;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  margin-top: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #ffffff;
  cursor: pointer;
  background: linear-gradient(135deg, #00c853, #00e676);
  border: none;
  border-radius: 10px;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(0, 200, 83, 0.3);
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 200, 83, 0.4);
}

.submit-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
  box-shadow: none;
}

.auth-footer {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  font-size: 13px;
}

.link {
  font-weight: 500;
  color: #666666;
  cursor: pointer;
  transition: color 0.2s;
}

.link:hover {
  color: #00c853;
}

@media (max-width: 768px) {
  .auth-panel-left {
    display: none;
  }

  .auth-panel-right {
    padding: 20px;
  }

  .auth-container {
    box-shadow: none;
    background: transparent;
    padding: 20px 0;
  }
}
</style>
