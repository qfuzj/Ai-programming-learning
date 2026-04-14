<template>
  <div class="reset-page">
    <el-card class="reset-card">
      <template #header>
        <strong>重置密码</strong>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
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

        <el-button type="primary" :loading="submitting" @click="onSubmit">提交重置</el-button>
        <el-button text @click="goLogin">返回登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";
import { useRouter } from "vue-router";
import { getCaptcha, resetPassword } from "@/api/auth";
import { ROUTE_PATHS } from "@/router/constants";

const router = useRouter();
const formRef = ref<FormInstance>();
const submitting = ref(false);
const captchaImage = ref("");

const form = reactive({
  phone: "",
  newPassword: "",
  confirmPassword: "",
  captchaId: "",
  captchaCode: "",
});

const rules: FormRules = {
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    { pattern: /^1\d{10}$/, message: "手机号格式不正确", trigger: "blur" },
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, max: 20, message: "密码长度为 6-20 位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.newPassword) {
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
  const res = await getCaptcha("reset_password");
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
    await resetPassword({
      phone: form.phone,
      newPassword: form.newPassword,
      captchaId: form.captchaId,
      captchaCode: form.captchaCode,
    });
    await router.push({ path: ROUTE_PATHS.USER_LOGIN, query: { reset: "1" } });
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
.reset-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
}

.reset-card {
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
