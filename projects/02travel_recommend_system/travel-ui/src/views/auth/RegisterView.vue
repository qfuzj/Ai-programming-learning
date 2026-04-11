<!-- 用户注册页：后续联调注册与验证码。 -->
<template>
  <PageStub title="用户注册" description="后续接入 /api/user/auth/register 与验证码流程。">
    <el-form :model="form" label-position="top">
      <el-row :gutter="12">
        <el-col :span="12"><el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12"><el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="确认密码"><el-input v-model="form.confirmPassword" type="password" show-password /></el-form-item></el-col>
      </el-row>
      <el-button type="primary">注册</el-button>
    </el-form>
  </PageStub>
</template>

<script setup lang="ts">
import { reactive } from "vue";

const form = reactive({
  username: "",
  phone: "",
  password: "",
  confirmPassword: "",
});
</script>