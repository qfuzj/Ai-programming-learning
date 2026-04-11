<!-- AI 聊天页骨架：后续接入 SSE/流式聊天。 -->
<template>
  <div class="page-container">
    <el-card class="page-card">
      <template #header>AI 聊天</template>
      <el-input v-model="message" type="textarea" :rows="4" placeholder="输入你的旅行偏好..." />
      <el-button type="primary" style="margin-top: 12px">发送</el-button>
      <el-empty description="后续展示聊天消息列表" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";

const message = ref("");
</script>
