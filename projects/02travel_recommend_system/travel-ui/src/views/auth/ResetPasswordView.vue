<!-- 重置密码页：后续联调短信验证码与密码重置。 -->
<template>
  <PageStub title="重置密码" description="后续接入 /api/user/auth/reset-password。">
    <el-form :model="form" label-position="top">
      <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-button type="primary">提交重置</el-button>
    </el-form>
  </PageStub>
</template>

<script setup lang="ts">
import { reactive } from "vue";

const form = reactive({ phone: "", newPassword: "" });
</script>
