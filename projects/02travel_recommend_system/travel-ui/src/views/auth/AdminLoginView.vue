<!-- 极简风格管理员登录页 -->
<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <div class="brand">智游</div>
        <h1 class="title">管理员登录</h1>
        <p class="subtitle">登录管理后台</p>
      </div>

      <form @submit.prevent="onSubmit">
        <div class="form-group">
          <label class="form-label">管理员账号</label>
          <input
            v-model="form.username"
            type="text"
            class="form-input"
            placeholder="请输入管理员账号"
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
          {{ submitting ? "登录中..." : "登录后台" }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
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
    await userStore.loginAsAdmin({ ...form, loginType: "username" as const });
    const redirect =
      typeof route.query.redirect === "string" ? route.query.redirect : ROUTE_PATHS.ADMIN_DASHBOARD;
    await router.push(redirect);
  } catch {
    await loadCaptcha();
  } finally {
    submitting.value = false;
  }
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
</style>
