<!-- AI 生成行程页骨架：后续接入生成草案与保存。 -->
<template>
  <PageStub title="AI 生成行程" description="后续接入 AI 生成与草案保存。">
    <el-form :model="form" label-position="top">
      <el-row :gutter="12">
        <el-col :span="12"><el-form-item label="目的地"><el-input v-model="form.destination" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="天数"><el-input-number v-model="form.days" :min="1" /></el-form-item></el-col>
      </el-row>
      <el-button type="primary">生成草案</el-button>
    </el-form>
  </PageStub>
</template>

<script setup lang="ts">
import { reactive } from "vue";

const form = reactive({ destination: "", days: 2 });
</script>