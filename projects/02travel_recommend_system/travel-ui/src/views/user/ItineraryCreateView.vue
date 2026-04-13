<!-- 新建行程页骨架：后续联调手动创建。 -->
<template>
  <PageStub title="新建行程" description="后续接入手动创建行程接口。">
    <el-form :model="form" label-position="top">
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目的地"><el-input v-model="form.destination" /></el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="开始日期">
            <el-date-picker v-model="form.startDate" type="date" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束日期">
            <el-date-picker v-model="form.endDate" type="date" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-button type="primary">创建行程</el-button>
    </el-form>
  </PageStub>
</template>

<script setup lang="ts">
import { reactive } from "vue";

const form = reactive({ title: "", destination: "", startDate: "", endDate: "" });
</script>
