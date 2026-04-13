<!-- 管理端登录页：用于管理员登录入口。 -->
<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <strong>管理员登录</strong>
      </template>
      <el-form :model="form" label-position="top" @submit.prevent>
        <el-form-item label="管理员账号">
          <el-input v-model="form.username" placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" maxlength="6" placeholder="请输入验证码" />
            <el-image class="captcha-image" :src="captchaImage" fit="contain" @click="loadCaptcha">
              <template #error>
                <div class="captcha-placeholder" @click="loadCaptcha">点击刷新</div>
              </template>
            </el-image>
          </div>
        </el-form-item>
        <el-button type="primary" :loading="submitting" @click="onSubmit">登录后台</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/store";
import { getCaptcha } from "@/api/auth";
import { ROUTE_PATHS } from "@/router/constants";

const userStore = useUserStore();
const route = useRoute();
const router = useRouter();
const submitting = ref(false);
const captchaImage = ref("");

const form = reactive({
  username: "",
  password: "",
  loginType: "username" as const,
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
    ElMessage.warning("请先输入验证码");
    return;
  }

  try {
    submitting.value = true;
    await userStore.loginAsAdmin(form);
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
  void loadCaptcha();
});
</script>

<style scoped>
.login-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
}

.login-card {
  width: 420px;
}

.captcha-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-image {
  width: 130px;
  height: 48px;
  cursor: pointer;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
}

.captcha-placeholder {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  font-size: 12px;
  color: #909399;
}
</style>
