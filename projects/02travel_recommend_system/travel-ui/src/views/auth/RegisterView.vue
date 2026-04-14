<template>
  <div class="register-page">
    <el-card class="register-card">
      <template #header>
        <strong>用户注册</strong>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入密码"
          />
        </el-form-item>
        <el-form-item label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" maxlength="6" placeholder="请输入验证码" />
            <el-image class="captcha-image" :src="captchaImage" fit="contain" @click="loadCaptcha">
              <template #error>
                <div class="captcha-placeholder" @click="loadCaptcha">点击刷新</div>
              </template>
            </el-image>
          </div>
        </el-form-item>

        <el-button type="primary" :loading="submitting" @click="onSubmit">注册</el-button>
        <el-button text @click="goLogin">已有账号，去登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { useRouter } from "vue-router";
import { getCaptcha, registerUser } from "@/api/auth";
import { ROUTE_PATHS } from "@/router/constants";
import type { RegisterPayload } from "@/types/auth";

const router = useRouter();
const formRef = ref<FormInstance>();
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

const rules: FormRules<RegisterPayload> = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1\d{10}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度为 6-20 位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入密码", trigger: "blur" },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error("两次输入的密码不一致"));
          return;
        }
        callback();
      },
      trigger: "blur",
    },
  ],
  captchaCode: [{ required: true, message: "请输入验证码", trigger: "blur" }],
};

async function loadCaptcha(): Promise<void> {
  const res = await getCaptcha("register");
  form.captchaId = res.captchaId;
  captchaImage.value = res.captchaBase64;
  form.captchaCode = "";
}

async function onSubmit(): Promise<void> {
  const formIns = formRef.value;
  if (!formIns) {
    return;
  }
  const valid = await formIns.validate().catch(() => false);
  if (!valid) {
    return;
  }
  if (!form.captchaId) {
    ElMessage.warning("验证码已失效，请刷新后重试");
    await loadCaptcha();
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
  void router.push(ROUTE_PATHS.USER_LOGIN);
}

onMounted(() => {
  void loadCaptcha();
});
</script>

<style scoped>
.register-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
}

.register-card {
  width: 430px;
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
